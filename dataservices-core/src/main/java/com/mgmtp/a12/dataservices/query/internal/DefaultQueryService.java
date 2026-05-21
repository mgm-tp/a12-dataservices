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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase;
import com.mgmtp.a12.dataservices.exception.query.QueryException;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryRepository;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.events.QueryAfterPostProcessPhaseEvent;
import com.mgmtp.a12.dataservices.query.events.QueryBeforeExecutionPhaseEvent;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.security.IQueryResultAuthorization;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.validation.internal.QueryValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.ValidationResult;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.FormattingUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.INVALID_QUERY_ERROR_KEY;

@Slf4j
@RequiredArgsConstructor
@Service public class DefaultQueryService implements QueryService {

	private final IModelLoader<IDocumentModel> documentModelLoader;
	private final IModelLoader<RelationshipModel> relationshipModelLoader;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final DocumentPermissionEvaluator documentPermissionEvaluator;
	private final ProjectionProvider projectionProvider;
	private final QueryValidator queryValidator;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final QueryRepository queryRepository;
	private final QueryEnricher queryEnricher;
	private final Optional<IQueryResultAuthorization> queryResultAuthorization;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final QueryContextHelper queryContextHelper;
	private final IndexedModelFieldCache indexedModelFieldCache;

	/**
	 * @param queryRoot The query to be executed.
	 * @param locale The locale to be used for searching for localized values.
	 * @return The document graph resulting from the passed query.
	 */
	@Override public <T> QueryPage<T> query(QueryRoot queryRoot, String locale) {
		try {
			StopWatch stopWatch = StopWatch.createStarted();
			documentPermissionEvaluator.checkDocumentQueryPermission(queryRoot.getTargetDocumentModel());
			QueryPage<T> results = queryInternal(queryRoot, Arrays.asList(DocumentTreeNodeType.values()), locale);
			log.debug("Query Executed in {} ms", stopWatch.getDuration().toMillis());
			QueryAfterPostProcessPhaseEvent<T> afterPostProcessPhaseEvent = new QueryAfterPostProcessPhaseEvent<>(results, queryRoot, locale);
			applicationEventPublisher.publishEvent(afterPostProcessPhaseEvent);

			return afterPostProcessPhaseEvent.getResults();
		} catch (AccessDeniedException | BaseException e) {
			throw e;
		} catch (Exception e) {
			throw new QueryException(ExecutionPhase.QUERY_GENERAL, ExceptionCodes.QUERY_GENERAL_ERROR_CODE, INVALID_QUERY_ERROR_KEY, null, e)
				.withAnonymityMessage("Unexpected error during query execution.");
		}
	}

	private <T> QueryPage<T> queryInternal(QueryRoot originalQuery, List<DocumentTreeNodeType> types, String locale) {
		IQueryProjection<T> projection = projectionProvider.getMatchingProjection(originalQuery.getProjectionName());
		QueryContext context =
			new DefaultQueryContext(documentModelLoader, relationshipModelLoader, this::queryWithoutProjection, documentModelServiceFactory, queryContextHelper,
				indexedModelFieldCache, locale, originalQuery.toBuilder().build());
		StopWatch stopWatch = StopWatch.createStarted();
		QueryRoot queryRoot = projection.preprocess(originalQuery, context);
		log.debug("Query Projection preprocess took {} ms", stopWatch.getDuration().toMillis());

		ValidationResult validationResult = queryValidator.validate(queryRoot, context, dataServicesCoreProperties.getQuery().getValidation().isEnabled());
		if (validationResult.hasErrors()) {
			throw new QueryValidationException(QUERY_VALIDATION, ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE, INVALID_QUERY_ERROR_KEY,
				FormattingUtils.toYaml(validationResult.getErrors()))
				.withAnonymityMessage("Query is not valid.");
		}

		Page<DocumentTreeResult> securedResults = context.query(types, queryRoot);

		stopWatch = StopWatch.createStarted();
		QueryPage<T> postProcessed = projection.postprocess(originalQuery, securedResults, context);
		log.debug("Query Projection postprocess took {} ms", stopWatch.getDuration().toMillis());
		return postProcessed;
	}

	public @NonNull Page<DocumentTreeResult> queryWithoutProjection(List<DocumentTreeNodeType> types, QueryRoot queryRoot, QueryContext context) {

		queryEnricher.enrichQuery(queryRoot, context);

		QueryBeforeExecutionPhaseEvent queryBeforeExecutionEvent = new QueryBeforeExecutionPhaseEvent(queryRoot, context);
		applicationEventPublisher.publishEvent(queryBeforeExecutionEvent);
		Page<DocumentTreeResult> results = queryBeforeExecutionEvent.getResults() == null
			? queryRepository.query(queryRoot, types, context)
			: queryBeforeExecutionEvent.getResults();

		List<DocumentTreeResult> dsSecuredResults = queryResultAuthorization
			.map(authorization -> authorization.authorizeQueryResult(results.getContent()))
			.orElse(results.getContent());

		return new RootBasedPageImpl<>(dsSecuredResults, results.getPageable(), results.getTotalElements());
	}
}
