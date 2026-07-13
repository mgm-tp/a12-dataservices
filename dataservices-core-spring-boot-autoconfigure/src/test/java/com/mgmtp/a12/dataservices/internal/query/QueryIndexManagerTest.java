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
package com.mgmtp.a12.dataservices.internal.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.query.QueryIndexingException;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = { InitialITConfiguration.class, QueryIndexManagerTest.FailingIndexingBehaviourConfiguration.class })
@Slf4j public class QueryIndexManagerTest extends AbstractSpringContextIT {

	// Following constants are kept intentionally for manual testing.
	// It lasts too long to run in regular tests,
	// so it's reduced just to 10 documents to test at least regular functionality, but in case of issues uncomment critical amount and run it locally.
	public static final int NUMBER_OF_REINDEXING_THREADS = 5;
	public static final int CRITICAL_AMMOUNTOF_PARAMETERS_FOR_SQL_QUERY = 65535;
	public static final int DOUBLING_FOR_SAFE_NUBER_OF_DOCUMENTS = 2;
	public static final int DOCUMENT_COUNT = 10;

	@Autowired private QueryIndexManager queryIndexManager;
	@Autowired private DocumentFieldsJpaRepository documentFieldsJpaRepository;
	@Autowired private DocumentSearchJpaRepository documentSearchJpaRepository;
	@Autowired private TransactionHandler transactionHandler;

	@Override protected void initializeWithSecurityBypass() throws Exception {
		modelsFunctions.createModels(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		log.info("Creating {} documents", DOCUMENT_COUNT);
		StopWatch stopWatch = StopWatch.createStarted();
		IntStream.rangeClosed(1, DOCUMENT_COUNT)
			.forEach(i -> createDocument(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json"));
		log.info("{} documents created in {}.", DOCUMENT_COUNT, stopWatch.formatTime());
	}

	@Test public void testIndexing() {
		Map<DocumentReference, List<Long>> indexedDocumentFields = getActualIndexedDocumentFields();
		List<DocumentReference> indexedDocumentsSearch = getActualIndexedDocumentSearch();
		Assert.assertEquals(indexedDocumentFields.size(), DOCUMENT_COUNT);
		Assert.assertEquals(indexedDocumentsSearch.size(), DOCUMENT_COUNT);
		AtomicReference<List<Long>> droppedFieldIds = new AtomicReference<>(new ArrayList<>());
		transactionHandler.runMethodInNewTransaction(() -> droppedFieldIds.set(dropFieldsToTestPartialUpdate(indexedDocumentFields)));
		Assert.assertEquals(getActualIndexedDocumentFields().size(), 5);
		Assert.assertEquals(getActualIndexedDocumentSearch().size(), 5);

		dataServicesCoreProperties.getQuery().getReindexing().setMode(DataServicesCoreProperties.Query.Reindexing.Mode.INDEX_NEW_ONLY);
		queryIndexManager.indexNew(List.of(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL), true);
		Map<DocumentReference, List<Long>> fieldsAfterIndexNew = getActualIndexedDocumentFields();
		List<DocumentReference> documentsSearchAfterIndexNew = getActualIndexedDocumentSearch();
		Assert.assertEquals(fieldsAfterIndexNew.size(), DOCUMENT_COUNT);
		Assert.assertEquals(documentsSearchAfterIndexNew.size(), DOCUMENT_COUNT);
		Assert.assertTrue(CollectionUtils.isEmpty(documentFieldsJpaRepository.findAllById(droppedFieldIds.get())));

		dataServicesCoreProperties.getQuery().getReindexing().setMode(DataServicesCoreProperties.Query.Reindexing.Mode.REBUILD_INDEX);
		transactionHandler.runMethodInNewTransaction(this::cleanIndex);
		queryIndexManager.index(List.of(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL), true);
		Map<DocumentReference, List<Long>> fieldsAfterReindex = getActualIndexedDocumentFields();
		List<DocumentReference> documentsSearchAfterReindex = getActualIndexedDocumentSearch();
		Assert.assertEquals(fieldsAfterReindex.size(), DOCUMENT_COUNT);
		Assert.assertEquals(documentsSearchAfterReindex.size(), DOCUMENT_COUNT);
		Assert.assertTrue(CollectionUtils.isEmpty(documentFieldsJpaRepository.findAllById(getFieldIds(fieldsAfterIndexNew))));
	}

	private void cleanIndex() {
		documentFieldsJpaRepository.truncateTable();
		documentSearchJpaRepository.truncateTable();
	}

	private List<Long> dropFieldsToTestPartialUpdate(Map<DocumentReference, List<Long>> originalDocuments) {
		Map<DocumentReference, List<Long>> fieldsToDrop =
			originalDocuments.keySet().stream().limit(5).collect(Collectors.toMap(k -> k, originalDocuments::get));

		List<Long> fieldIdsToDrop = getFieldIds(fieldsToDrop);
		documentFieldsJpaRepository.deleteAllByIdInBatch(fieldIdsToDrop);
		documentSearchJpaRepository.deleteDocumentSearchEntitiesByDocRefIn(fieldsToDrop.keySet().stream().map(DocumentReference::toString).toList());
		return fieldIdsToDrop;
	}

	private static @NonNull List<Long> getFieldIds(Map<DocumentReference, List<Long>> fieldsToDrop) {
		return fieldsToDrop.values().stream().flatMap(Collection::stream).toList();
	}

	private Map<DocumentReference, List<Long>> getActualIndexedDocumentFields() {
		return documentFieldsJpaRepository.findAll().stream()
			.collect(Collectors.groupingBy(r -> new DocumentReference(r.getDocRef()), Collectors.mapping(DocumentFieldEntity::getId, Collectors.toList())));
	}

	private List<DocumentReference> getActualIndexedDocumentSearch() {
		return documentSearchJpaRepository.findAll().stream().map(r -> new DocumentReference(r.getDocRef())).toList();
	}

	@DataProvider(name = "reindexingConfigurations")
	public Object[][] reindexingConfigurations() {
		List<Object[]> data = new ArrayList<>();
		boolean[] ignoreErrorsVals = { false, true };
		int[] threadVals = { 1, 2, 5 };
		int[] batchVals = { 1, 2, 3, 20 };
		for (boolean ignoreErrors : ignoreErrorsVals) {
			for (int threads : threadVals) {
				for (int batch : batchVals) {
					data.add(new Object[] { ignoreErrors, threads, batch });
				}
			}
		}
		return data.toArray(Object[][]::new);
	}

	@DataProvider(name = "reindexingConfigurationsNewOnly")
	public Object[][] reindexingConfigurationsNewOnly() {
		return reindexingConfigurations(); // reuse same combinations
	}

	// New DataProvider (strict mode: ignoreErrors = false)
	@DataProvider(name = "reindexingFailureConfigurationsStrict")
	public Object[][] reindexingFailureConfigurationsStrict() {
		return new Object[][] {
			// threads, batchSize
			{ 1, 1 },
			{ 2, 3 }
		};
	}

	// New DataProvider (lenient mode: ignoreErrors = true)
	@DataProvider(name = "reindexingFailureConfigurationsLenient")
	public Object[][] reindexingFailureConfigurationsLenient() {
		return new Object[][] {
			// threads, batchSize
			{ 1, 1 },
			{ 2, 3 },
			{ 5, 5 }
		};
	}

	@Test(dataProvider = "reindexingConfigurations")
	public void testReindexingCombinationsRebuild(boolean ignoreErrors, int numberOfThreads, int batchSize) {
		runIndexingCombination(ignoreErrors, numberOfThreads, batchSize, DataServicesCoreProperties.Query.Reindexing.Mode.REBUILD_INDEX);
	}

	@Test(dataProvider = "reindexingConfigurationsNewOnly")
	public void testReindexingCombinationsIndexNewOnly(boolean ignoreErrors, int numberOfThreads, int batchSize) {
		// Clean index first because INDEX_NEW_ONLY only fills missing documents
		transactionHandler.runMethodInNewTransaction(this::cleanIndex);
		runIndexingCombination(ignoreErrors, numberOfThreads, batchSize, DataServicesCoreProperties.Query.Reindexing.Mode.INDEX_NEW_ONLY);
	}

	@Test(dataProvider = "reindexingFailureConfigurationsStrict")
	public void testReindexingFailureRebuildStrict(int numberOfThreads, int batchSize) {
		// ignoreErrors = false
		transactionHandler.runMethodInNewTransaction(this::cleanIndex);

		DataServicesCoreProperties.Query.Reindexing re = dataServicesCoreProperties.getQuery().getReindexing();
		re.setMode(DataServicesCoreProperties.Query.Reindexing.Mode.REBUILD_INDEX);
		re.setIgnoreErrors(false);
		re.setNumberOfThreads(numberOfThreads);
		re.setBatchSize(batchSize);

		FailingIndexingControl.enableAllBatchesFailure(); // every batch fails

		try {
			queryIndexManager.indexQuery();
			Assert.fail("Expected QueryIndexingException when ignoreErrors=false");
		} catch (QueryIndexingException e) {
			// expected
		} finally {
			// Assert failure executed
			Assert.assertTrue(FailingIndexingControl.getLastFailedBatchSize() > 0,
				"Simulated failure did not trigger (strict mode)");
			FailingIndexingControl.disableFailure();
		}

		Assert.assertEquals(getActualIndexedDocumentFields().size(), 0, "Expected 0 indexed documents after ALL_BATCHES failure with ignoreErrors=false");
		Assert.assertEquals(getActualIndexedDocumentSearch().size(), 0, "Expected 0 indexed search docs after ALL_BATCHES failure with ignoreErrors=false");

		transactionHandler.runMethodInNewTransaction(this::cleanIndex);
	}

	@Test(dataProvider = "reindexingFailureConfigurationsLenient")
	public void testReindexingFailureRebuildLenient(int numberOfThreads, int batchSize) {
		// ignoreErrors = true
		transactionHandler.runMethodInNewTransaction(this::cleanIndex);

		DataServicesCoreProperties.Query.Reindexing re = dataServicesCoreProperties.getQuery().getReindexing();
		re.setMode(DataServicesCoreProperties.Query.Reindexing.Mode.REBUILD_INDEX);
		re.setIgnoreErrors(true);
		re.setNumberOfThreads(numberOfThreads);
		re.setBatchSize(batchSize);

		FailingIndexingControl.enableFirstBatchFailure();

		queryIndexManager.indexQuery();

		int failedBatchSize = FailingIndexingControl.getLastFailedBatchSize();
		Assert.assertTrue(failedBatchSize > 0, "Simulated failure did not trigger (lenient mode)");
		int expected = DOCUMENT_COUNT - failedBatchSize;
		Assert.assertEquals(getActualIndexedDocumentFields().size(), expected,
			"Expected partial indexing (fields) after FIRST_BATCH failure with ignoreErrors=true");
		Assert.assertEquals(getActualIndexedDocumentSearch().size(), expected,
			"Expected partial indexing (search) after FIRST_BATCH failure with ignoreErrors=true");

		FailingIndexingControl.disableFailure();
		transactionHandler.runMethodInNewTransaction(this::cleanIndex);
	}

	private void runIndexingCombination(boolean ignoreErrors, int numberOfThreads, int batchSize,
		DataServicesCoreProperties.Query.Reindexing.Mode mode) {
		// Configure
		DataServicesCoreProperties.Query.Reindexing re = dataServicesCoreProperties.getQuery().getReindexing();
		re.setIgnoreErrors(ignoreErrors);
		re.setNumberOfThreads(numberOfThreads);
		re.setBatchSize(batchSize);
		re.setMode(mode);

		// Execute
		queryIndexManager.indexQuery();

		// Verify
		int fieldDocs = getActualIndexedDocumentFields().size();
		int searchDocs = getActualIndexedDocumentSearch().size();
		Assert.assertEquals(fieldDocs, DOCUMENT_COUNT,
			"[REINDEX mode=" + mode + "] Unexpected document_fields size for ignoreErrors=" + ignoreErrors +
				", threads=" + numberOfThreads + ", batchSize=" + batchSize);
		Assert.assertEquals(searchDocs, DOCUMENT_COUNT,
			"[REINDEX mode=" + mode + "] Unexpected document_search size for ignoreErrors=" + ignoreErrors +
				", threads=" + numberOfThreads + ", batchSize=" + batchSize);
	}

	// ---- Failure simulation support (test-only) ----
	static final class FailingIndexingControl {
		private enum FailureMode {NONE, FIRST_BATCH, ALL_BATCHES}

		private static volatile FailureMode mode = FailureMode.NONE;
		private static final AtomicReference<Boolean> FIRST_CONSUMED = new AtomicReference<>(false);
		private static final AtomicInteger LAST_FAILED_BATCH_SIZE = new AtomicInteger(0);

		static void enableFirstBatchFailure() {
			mode = FailureMode.FIRST_BATCH;
			FIRST_CONSUMED.set(false);
			LAST_FAILED_BATCH_SIZE.set(0);
		}

		static void enableAllBatchesFailure() {
			mode = FailureMode.ALL_BATCHES;
			FIRST_CONSUMED.set(false);
			LAST_FAILED_BATCH_SIZE.set(0);
		}

		static void disableFailure() {
			mode = FailureMode.NONE;
			FIRST_CONSUMED.set(false);
			LAST_FAILED_BATCH_SIZE.set(0);
		}

		static boolean shouldFailNow() {
			return switch (mode) {
				case NONE -> false;
				case ALL_BATCHES -> true;
				case FIRST_BATCH -> FIRST_CONSUMED.compareAndSet(false, true);
			};
		}

		static void recordFailedBatchSize(int size) {
			LAST_FAILED_BATCH_SIZE.compareAndSet(0, size);
		}

		static int getLastFailedBatchSize() {
			return LAST_FAILED_BATCH_SIZE.get();
		}
	}

	@Configuration
	static class FailingIndexingBehaviourConfiguration {

		@Bean
		static BeanPostProcessor failingIndexBehaviourPostProcessor() {
			return new BeanPostProcessor() {
				@Override
				public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
					if (bean instanceof DocumentSearchIndexBehaviour original) {
						// Wrap original bean with CGLIB proxy (Spring AOP) preserving its constructor state
						ProxyFactory pf = new ProxyFactory(original);
						pf.setProxyTargetClass(true); // force CGLIB
						pf.addAdvice((MethodInterceptor) this::maybeFail);
						return pf.getProxy();
					}
					return bean;
				}

				private Object maybeFail(MethodInvocation invocation) throws Throwable {
					if ("indexBatch".equals(invocation.getMethod().getName()) && FailingIndexingControl.shouldFailNow()) {
						Object[] args = invocation.getArguments();
						if (args.length > 0 && args[0] instanceof List<?> l) {
							FailingIndexingControl.recordFailedBatchSize(l.size());
							log.debug("Simulated failure injected (batch size={})", l.size());
						} else {
							FailingIndexingControl.recordFailedBatchSize(0);
							log.debug("Simulated failure injected (batch size unknown)");
						}
						throw new RuntimeException("Simulated indexing failure (test only)");
					}
					return invocation.proceed();
				}
			};
		}
	}
}
