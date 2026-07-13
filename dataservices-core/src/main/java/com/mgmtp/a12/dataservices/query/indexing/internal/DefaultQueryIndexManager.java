/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.dataservices.query.indexing.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import javax.sql.DataSource;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.Query.Reindexing;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.exception.query.QueryIndexingException;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_INDEXING;

/**
 * The QueryIndexManager is a component responsible for managing the indexing and re-indexing processes of query documents.
 *
 * It provides mechanisms for handling different indexing modes:
 *
 * - Disabled: Indexing is not performed.
 * - Rebuild Index: Entire index is rebuilt by clearing existing data and starting fresh.
 * - Index New Only: Only new data is added to the index.
 *
 * This class utilizes parallel processing to optimize the indexing process and supports transactional handling
 * to ensure consistency and reliability, even in the event of errors.
 *
 * Key responsibilities of the QueryIndexManager include:
 *
 * - Managing the lifecycle of query document indexing, from initialization to cleanup.
 * - Handling and coordinating interactions with repositories and auxiliary services.
 * - Operating under configurable parameters such as batch sizes, thread pools, and error-handling policies.
 *
 * Note that some methods are explicitly designed to avoid being transactional to handle safe cleanup operations and avoid rollback issues.
 *
 * Dependencies and injected components:
 *
 * - Repositories for accessing and manipulating document and model-related data.
 * - Transaction handler for fine-grained transactional control.
 * - Index behavior strategies for customizable indexing logic.
 * - Utility classes for model and document handling.
 * - Executor services to manage multithreaded indexing tasks.
 */
@Slf4j @RequiredArgsConstructor
@ConditionalOnProperty(name = "mgmtp.a12.dataservices.query.search-indexing.enabled", havingValue = "true", matchIfMissing = true)
@Component public class DefaultQueryIndexManager implements QueryIndexManager {

	private final DocumentFieldsJpaRepository documentFieldsJpaRepository;
	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final AggregatedDocumentRepository aggregatedDocumentRepository;
	private final DocumentSearchIndexBehaviour indexBehavior;
	private final IModelLoader<IDocumentModel> documentModelLoader;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final ModelFieldsJpaRepository modelFieldsJpaRepository;
	private final TransactionHandler transactionHandler;
	private final DocumentModelFieldsIndexer documentModelFieldsIndexer;
	private final DocumentSearchJpaRepository documentSearchJpaRepository;
	@Qualifier("dsDataSource") private final DataSource dataSource;
	@NonNull private final BackendAuthenticationService backendAuthenticationService;

	/**
	 * Manages the indexing of query-related documents according to the given reindexing configuration.
	 *
	 * This method serves as the main entry point to handle the reindexing of query documents. Based on the configured
	 * reindexing mode, it performs the following operations:
	 *
	 * 1. Retrieves the list of document models that need to be reindexed.
	 * 2. Executes reindexing logic according to the configured mode:
	 * - DISABLED: No changes are made to the index.
	 * - REBUILD_INDEX: The indexing is performed after cleaning up the necessary search tables.
	 * - INDEX_NEW_ONLY: Only newly added documents are indexed into the existing index.
	 * 3. Logs the completion status or captures errors during the reindexing process.
	 * 4. Ensures temporary table cleanup is always performed by executing clean-up logic in the final stage.
	 *
	 * Note:
	 * - This method is intentionally not annotated with @Transactional since it involves multiple transactions
	 * (e.g., cleaning search tables, reindexing in parallel, and clean-up tasks).
	 * - Cleaning and indexing operations are designed to commit in separate transactions to prevent rollback of
	 * clean-up operations in case of an error.
	 * - Errors encountered during the process are either logged or propagated, depending on the ignoreErrors flag
	 * in the reindexing configuration.
	 *
	 * Throws:
	 * - QueryIndexingException: If a critical error occurs and ignoring errors is not allowed according to the configuration.
	 */
	@Override public void indexQuery() {
		// This method is not annotated with @Transactional because of need to clean-up tmp table in the end of execution.
		// Having clean-up in the same transaction with re-indexing can cause rollback of clean-up or abnormal behaviour in case clean-up in separate transaction
		StopWatch stopWatch = StopWatch.createStarted();
		log.info("Re-indexing of Query documents has been started. The server is not available until the re-indexing is finished.");

		Reindexing.Mode mode = getReindexingConfig().getMode();
		boolean ignoreErrors = getReindexingConfig().isIgnoreErrors();
		try {
			Collection<String> modelNamesToIndex = getModelNamesToReindex(getReindexingConfig().getApplyToModels());
			switch (mode) {
			case DISABLED -> log.info("Query indexing is disabled. No changes will be made to the index.");
			case REBUILD_INDEX -> {
				// The search tables are cleaned using TRUNCATE for efficency reasons
				// As the indexing is done in multiple (parallel) transactions, the TRUNCATE needs to run it its
				// own transaction so that the command is committed and the exclusive lock on the table is released.
				// before the real indexing starts.
				transactionHandler.runMethodInDefaultTransaction(this::cleanIndex);

				transactionHandler.runMethodInNewTransaction(() -> index(modelNamesToIndex, ignoreErrors));

			}
			case INDEX_NEW_ONLY -> indexNew(modelNamesToIndex, ignoreErrors);
			default -> throw new QueryIndexingException(QUERY_INDEXING, "Unsupported indexing mode: %s".formatted(mode));
			}

			log.atInfo().setMessage("Re-indexing of Query documents for models {} has been completed successfully in {}")
				.addArgument(modelNamesToIndex)
				.addArgument(stopWatch::formatTime)
				.log();
		} catch (Exception e) {
			if (ignoreErrors) {
				log.error("Error encountered during Query reindexing, but ignoreErrors is true => continuing.", e);
			} else {
				throw new QueryIndexingException(QUERY_INDEXING, "Error encountered during Query reindexing.", e);
			}
		}
	}

	/**
	 * Indexes new documents for the provided document model names.
	 *
	 * This method processes the provided collection of document model names and indexes
	 * only the newly added documents into the existing index configuration. It delegates
	 * the internal indexing operation to the `indexInternal` method, which handles
	 * the core logic for indexing based on the provided parameters.
	 *
	 * @param documentModelNames a collection of document model names specifying the set of
	 * models to be indexed
	 * @param ignoreErrors a flag indicating whether errors encountered during the
	 * indexing process should be ignored or reported; if true,
	 * errors are logged, and the process continues; if false,
	 * the process halts and throws an exception upon encountering an error
	 */
	@Override public void indexNew(Collection<String> documentModelNames, boolean ignoreErrors) {
		transactionHandler.runMethodInNewTransaction(() -> indexInternal(documentModelNames, ignoreErrors, true));
		vacuumAnalyze();
	}

	/**
	 * Triggers indexing all documents for the specified document models.
	 *
	 * This method delegates the indexing process to an internal method
	 * that handles the core indexing logic. It ensures that the provided
	 * document models are indexed according to the specified parameters.
	 * Note: This method does not clean old indexes, so please ensure that you clean old indexes beforehand.
	 *
	 * @param documentModelNames a collection of document model names that need to be indexed
	 * @param ignoreErrors a flag indicating whether errors encountered during indexed
	 * should be ignored; if true, errors are logged and the process
	 * continues; otherwise, the process halts and throws an exception
	 */
	@Override public void index(Collection<String> documentModelNames, boolean ignoreErrors) {
		transactionHandler.runMethodInNewTransaction(() -> indexInternal(documentModelNames, ignoreErrors, false));
		vacuumAnalyze();
	}

	@Override public void cleanIndex() {
		StopWatch stopWatch = StopWatch.createStarted();
		documentFieldsJpaRepository.truncateTable();
		documentSearchJpaRepository.truncateTable();

		log.debug("Search data removed from database in {}.", stopWatch.formatTime());
	}

	private void vacuumAnalyze() {
		if (!dataServicesCoreProperties.getQuery().getReindexing().getVacuum().isEnabled()) {
			return;
		}
		log.debug("Vacuum analyze...");
		StopWatch sw = StopWatch.createStarted();
		Connection conn = null;
		try {
			conn = DataSourceUtils.getConnection(dataSource);
			boolean originalAutoCommit = conn.getAutoCommit();
			try (Statement stmt = conn.createStatement()) {
				if (!originalAutoCommit) {
					conn.setAutoCommit(true);
				}
				stmt.execute("vacuum analyze document_search, document_fields");
				log.info("Vacuum analyze completed in {}.", sw.formatTime());
			} finally {
				conn.setAutoCommit(originalAutoCommit);
			}
		} catch (SQLException e) {
			throw new UnexpectedException("VACUUM failed", e);
		} finally {
			if (conn != null) {
				DataSourceUtils.releaseConnection(conn, dataSource);
			}
		}
	}

	@SneakyThrows private void indexInternal(Collection<String> documentModelNames, boolean ignoreErrors, boolean indexForNewDocumentOnly) {

		List<IDocumentModel> m = documentModelNames.stream()
			.map(documentModelLoader::loadModel)
			.toList();
		m.parallelStream()
			.forEach(documentModel -> backendAuthenticationService.executeWithBackendAuthentication(
				dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
				() -> {
					if (dataServicesCoreProperties.getQuery().getReindexing().getModelFields().isEnabled()) {
						StopWatch sw = StopWatch.createStarted();
						transactionHandler.runMethodInNewTransaction(() -> documentModelFieldsIndexer.indexDocumentModelFieldsOnUpdate(documentModel));
						log.atDebug().setMessage("Re-indexing model {} took: {}")
							.addArgument(documentModel.getHeader().getId())
							.addArgument(sw::formatTime)
							.log();
					}
					indexBatches(ignoreErrors, indexForNewDocumentOnly, documentModel,
						documentModelServiceFactory.createDocumentModelSearchService(documentModel),
						prepareFieldTypeIdCache(documentModel.getHeader().getId(), documentModelServiceFactory.createDocumentModelService(), documentModel));
					return null;
				}));
	}

	private void indexBatches(boolean ignoreErrors, boolean indexForNewDocumentOnly, IDocumentModel model,
		IDocumentModelSearchService documentModelSearchService, BiFunction<String, String, Long> fieldTypeIdProvider) {

		ListUtils.partition(getSuitableDocumentReferences(model.getHeader().getId(), indexForNewDocumentOnly), getReindexingConfig().getBatchSize())
			.parallelStream()
			.forEach(batch ->
				backendAuthenticationService.executeWithBackendAuthentication(
					dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
					() -> {
						indexSingleBatch(ignoreErrors, model, documentModelSearchService, fieldTypeIdProvider, batch);
						return null;
					}));
	}

	private void indexSingleBatch(boolean ignoreErrors, IDocumentModel model, IDocumentModelSearchService documentModelSearchService,
		BiFunction<String, String, Long> fieldTypeIdProvider, List<DocumentReference> batch) {

		StopWatch outerBatch = indexBehavior.getConcurrentStopWatch().getStarted("outerBatch");
		indexBehavior.getConcurrentStopWatch().reset();
		try {
			indexBehavior.indexBatch(batch, documentModelSearchService, fieldTypeIdProvider);
		} catch (Exception e) {
			log.error("Batch indexing failed (rolled back the batch, {}). model={}, batchSize={}, firstDoc={}", ignoreErrors ? "continuing" : "interrupting",
				model.getHeader().getId(), batch.size(), batch.isEmpty() ? "n/a" : batch.getFirst(), e);
			if (!ignoreErrors) {
				throw e;
			}
		} finally {
			outerBatch.stop();
			log.debug("Timings for single indexing batch:\n{}", indexBehavior.getConcurrentStopWatch());
		}
	}

	private @NonNull BiFunction<String, String, Long> prepareFieldTypeIdCache(String modelName, IDocumentModelService documentModelService,
		IDocumentModel documentModel) {

		Map<String, Map<String, Long>> fieldTypeIdCache = new ConcurrentHashMap<>();

		// Pre-fill fields of a current model
		Map<String, Long> currentModel = fieldTypeIdCache.computeIfAbsent(modelName, m -> new ConcurrentHashMap<>());
		new DocumentModelWalker().acceptDocumentModel(documentModel, new DocumentModelVisitor() {
			@Override public DocumentModelWalker.VisitProcess visitField(@NonNull IField field) {
				if (documentModelFieldsIndexer.isIndexable(field)) {
					currentModel.put(documentModelService.getPath(field),
						modelFieldsJpaRepository.getByModelNameAndFieldName(modelName, documentModelService.getPath(field)).getId());
				}
				return super.visitField(field);
			}
		});

		return (m, f) -> fieldTypeIdCache
			.computeIfAbsent(m, m1 -> new ConcurrentHashMap<>())
			.computeIfAbsent(f, f1 -> modelFieldsJpaRepository.getByModelNameAndFieldName(m, f).getId());
	}

	private @NonNull List<DocumentReference> getSuitableDocumentReferences(String modelName, boolean indexForNewDocumentOnly) {
		StopWatch stopWatch = StopWatch.createStarted();
		List<DocumentReference> docRefForIndexes = aggregatedDocumentRepository.findAllDocRefsForModel(modelName);

		if (indexForNewDocumentOnly) {
			Set<String> models = HashSet.newHashSet(1);
			String[] ids = new String[docRefForIndexes.size()];
			for (int i = 0; i < docRefForIndexes.size(); i++) {
				ids[i] = docRefForIndexes.get(i).toString();
				models.add(docRefForIndexes.get(i).getDocumentModelName());
			}
			List<String> documentsToBeIndexed = documentSearchJpaRepository.findMissingDocRefs(models, ids);

			docRefForIndexes = documentsToBeIndexed.stream().map(DocumentReference::new).toList();
		}

		log.atDebug().setMessage("Found {} documents of model {} suitable for reindexing in {}")
			.addArgument(docRefForIndexes.size())
			.addArgument(modelName)
			.addArgument(stopWatch::formatTime)
			.log();
		return docRefForIndexes;
	}

	private List<String> getModelNamesToReindex(List<String> documentModelNames) {
		return GenericUtils.isSingleAsterisk(documentModelNames)
			? modelHeaderJpaRepository.findIdsByModelType(ModelConstants.DOCUMENT_MODEL_TYPE)
			: documentModelNames;
	}

	private Reindexing getReindexingConfig() {
		return dataServicesCoreProperties.getQuery().getReindexing();
	}

}
