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
package com.mgmtp.a12.dataservices.query.internal;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryRepository;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.DefaultQueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.RootCteGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.DocumentTreeEntity;
import com.mgmtp.a12.dataservices.query.projection.internal.ExportCddCsvProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.request.internal.QueryPagingHelper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_EXECUTION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_LINKS_LIMIT_EXCEEDED_ERROR_KEY;

@Slf4j
@RequiredArgsConstructor
@Repository public class DefaultQueryRepository implements QueryRepository {

	@PersistenceContext(unitName = "dsPersistenceUnit") private EntityManager entityManager;
	private final DefaultQueryGeneratorContext.QueryGeneratorContextFactory queryGeneratorContextFactory;
	private final DocumentTreeMapper documentTreeMapper;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * Returns a page of document tree entries according to the passed query and page request.
	 *
	 * @param queryRoot The query containing all query parameters like fields, constraints, links.
	 * @param types A list of tree node types to be used for query tailoring.
	 * @param queryContext The query context containing the document models, relationships and enrichments.
	 * @return The result page of document tree entries.
	 */
	@Transactional(readOnly = true)
	@Override public @NonNull Page<DocumentTreeResult> query(QueryRoot queryRoot, Collection<DocumentTreeNodeType> types,
		QueryContext queryContext) {
		queryGeneratorContextFactory.setJsonColumnName(QueryGeneratorConstants.ColumnNames.ORIGINAL_VALUE_COLUMN_NAME);
		QueryGeneratorContext queryGeneratorContext = queryGeneratorContextFactory.createContext(queryContext);
		Paging validatedPageable = queryRoot.getPaging();
		queryGeneratorContext.setHardLimit(getHardLimitByProjection(queryRoot) * dataServicesCoreProperties.getQuery().getPageRequest().getPageSizeLimit());
		queryGeneratorContext.setPageLimit(validatedPageable.pageSize());
		queryGeneratorContext.setPageOffset(validatedPageable.pageNumber() * validatedPageable.pageSize());
		queryGeneratorContext.setTargetDocumentModel(queryRoot.getTargetDocumentModel());
		queryGeneratorContext.setLocale(queryContext.getLocale());
		queryGeneratorContext.setExclude(queryRoot.isExclude());
		queryGeneratorContext.setAggregationDefaultPrecision(dataServicesCoreProperties.getQuery().getAggregation().getDefaultPrecision());
		queryGeneratorContext.setProjectionName(queryRoot.getProjectionName());

		RootCteGenerator rootGenerator = RootCteGenerator.builder()
			.query(queryRoot)
			.generatorContext(queryGeneratorContext)
			.types(types)
			.build();

		StopWatch stopWatch = StopWatch.createStarted();
		Query preparedQuery = SqlGeneratorHelpersInternal.prepareQuery(entityManager, rootGenerator);
		log.atDebug().log("Query SQL Generation took {} ms", stopWatch.getDuration().toMillis());

		stopWatch = StopWatch.createStarted();

		try {
			@SuppressWarnings("unchecked")
			List<DocumentTreeEntity> listOfEntities = (List<DocumentTreeEntity>) preparedQuery.getResultList();

			List<DocumentTreeResult> resultList = listOfEntities
				.stream()
				.filter(Objects::nonNull)
				.map(entity -> {
					// Hibernate first level cache cannot be disabled thus we clean it up manually to not cache partial data.
					entityManager.detach(entity);
					return documentTreeMapper.mapToDocumentTreeResult(entity);
				})
				.toList();

			checkLinksLimitExceeded(resultList);

			log.atDebug().log("Query SQL Execution took {} ms", stopWatch.getDuration().toMillis());

			return new RootBasedPageImpl<>(resultList, QueryPagingHelper.preparePageRequest(queryRoot.getPaging(), queryRoot.getSort()),
				totalCount(listOfEntities, validatedPageable, queryGeneratorContext, queryRoot, types));
		} catch (PersistenceException e) {
			if (log.isDebugEnabled()) {
				log.warn(e.getMessage());
			} else {
				log.warn("Error in SQL.");
			}
			throw e;
		}
	}

	private void checkLinksLimitExceeded(List<DocumentTreeResult> resultList) {
		if (resultList.stream()
			.filter(Objects::nonNull)
			.filter(r -> r.getType().equals(DocumentTreeNodeType.LINK) || r.getType().equals(DocumentTreeNodeType.CHILD))
			.toList()
			.size() >= dataServicesCoreProperties.getQuery().getMaxLinksSize() + 1) {
			throw new QueryInvalidInputException(QUERY_EXECUTION, QUERY_LINKS_LIMIT_EXCEEDED_ERROR_KEY, null)
				.withAnonymityMessage("The maximum number [%d] of linked documents is exceeded, please consider to redesign your query to be pageable."
					.formatted(dataServicesCoreProperties.getQuery().getMaxLinksSize()));
		}
	}

	private long totalCount(List<DocumentTreeEntity> resultList, Paging validatedPage, QueryGeneratorContext queryGeneratorContext,
		QueryRoot queryRoot, Collection<DocumentTreeNodeType> types) {

		return resultList.stream()
			.filter(Objects::nonNull)
			.filter(e -> QueryPagingHelper.isPageable(e.getId().getType(), queryGeneratorContext.isExclude()))
			.findFirst()
			.map(DocumentTreeEntity::getTotalCount)
			.orElseGet(() -> {
				if (validatedPage.pageNumber() == 0) {
					return 0L;
				} else {
					queryGeneratorContext.setPageOffset(0);
					queryGeneratorContext.getParamHolder().clear();
					RootCteGenerator rootGenerator = RootCteGenerator.builder()
						.query(queryRoot)
						.generatorContext(queryGeneratorContext)
						.types(types)
						.build();
					Query unpagedPreparedQuery = SqlGeneratorHelpersInternal.prepareQuery(entityManager, rootGenerator);
					Query query = unpagedPreparedQuery.setFirstResult(0).setMaxResults(1);
					return typedStreamQuery(query)
						.filter(result -> result.getTotalCount() != null)
						.mapToLong(DocumentTreeEntity::getTotalCount)
						.findAny()
						.orElse(0L);
				}
			});
	}

	// Suppress warning here because query.getResultList() returns untyped List, and we are sure to receive the list of DocumentTreeEntity.
	@SuppressWarnings("unchecked")
	private static Stream<DocumentTreeEntity> typedStreamQuery(Query preparedQuery) {
		return preparedQuery.getResultStream();
	}

	private int getHardLimitByProjection(QueryRoot queryRoot) {
		return ExportCddCsvProjectionImplementation.PROJECTION_NAME.equals(queryRoot.getProjectionName())
			? dataServicesCoreProperties.getCdd().getExport().getMaxRowSize()
			: dataServicesCoreProperties.getQuery().getPageRequest().getPageNumberLimit();
	}
}
