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
package com.mgmtp.a12.dataservices.query.projection.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.internal.CddSkeletonFactory;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.exception.query.QueryNotFoundException;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.internal.QueryConstants;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_PREPROCESSING;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.projection.internal.DocumentGraphProjectionImplementation.DOCUMENT_GRAPH_PROJECTION_NAME;

/**
 * This projection is replacement for `LOAD_DOCUMENT_GRAPH` operation, which will return documents graph from input CDM.
 *
 * The `path` parameter from `LOAD_DOCUMENT_GRAPH` will be replaced by {@link QueryRoot#getFields()}.
 * In this case `fields` should contain only one element, represent for `path` to root group which documents graph will be built from.
 * If the query contains `links` no CDM relationships are resolved but the links and link documents are returned as specified by the client.
 *
 * An example of documents graph query request:
 *
 * == Example:
 *
 * [source,json]
 * --
 * {
 * 	"jsonrpc": "2.0",
 * 	"id": "DocumentGraph",
 * 	"method": "QUERY",
 * 	"params": {
 * 		"query": {
 * 			"projectionName": "document-graph",
 * 			"targetDocumentModel": "ContractCDM",
 * 			"fields": [
 * 				"/ContractBusinessPartner"
 * 			],
 * 			"constraint": {
 * 				"operator": "exact_match",
 * 				"field": "/__meta/docRef",
 * 				"value": "Contract/6111bf68-292b-4b70-b046-9ab2d352e137"
 *                        },
 * 			"paging": {
 * 				"pageNumber": 0,
 * 				"pageSize": 100
 *            }
 *        },
 *    }
 * }
 * --
 */
@RequiredArgsConstructor
@Order
@Component
@QueryProjection(DOCUMENT_GRAPH_PROJECTION_NAME) public class DocumentGraphProjectionImplementation implements IQueryProjection<DocumentTreeResult> {

	public static final String DOCUMENT_GRAPH_PROJECTION_NAME = "document-graph";
	public static final String INVALID_MULTI_VALUES_INPUT_FIELDS_EXCEPTION =
		"`document-graph` projection requires only 1 `field` as path to root graph. Empty or `/` value represent for CDM root document model";
	public static final String NO_MODEL_AVAILABLE_FOR_CDDS = "No model %s available for CDDs.";
	public static final String INVALID_TARGET_DOCUMENT_MODEL =
		"'targetDocumentModel' must not be null in query with document-graph projection";

	private final IModelLoader<ComposeDocumentModel> composeDocumentModelLoader;
	private final IModelLoader<RelationshipModel> relationshipModelLoader;
	private final DocumentProjectionImplementation documentProjectionImplementation;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final Optional<CddSkeletonFactory> cddSkeletonFactory;

	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		if (CollectionUtils.isNotEmpty(originalQuery.getFields()) && originalQuery.getFields().size() != 1) {
			throw new QueryInvalidInputException(QUERY_PREPROCESSING, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage(INVALID_MULTI_VALUES_INPUT_FIELDS_EXCEPTION);
		}
		if (StringUtils.isBlank(originalQuery.getTargetDocumentModel())) {
			throw new QueryInvalidInputException(QUERY_PREPROCESSING, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage(INVALID_TARGET_DOCUMENT_MODEL);
		}
		IDocumentModel cdm = composeDocumentModelLoader.loadModel(originalQuery.getTargetDocumentModel());
		if (!ComposeDocumentModelUtils.isComposeDocumentModel(cdm.getHeader())) {
			throw new QueryNotFoundException(QUERY_PREPROCESSING, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage(NO_MODEL_AVAILABLE_FOR_CDDS.formatted(originalQuery.getTargetDocumentModel()));
		}
		String path = getPathFromFields(originalQuery);
		originalQuery.setFields(null);
		IGroup rootGroup = ComposeDocumentModelUtils.getRootGroup(path, cdm, documentModelServiceFactory);
		cddSkeletonFactory.ifPresent(prepareDocumentGraphQuery(originalQuery, path, rootGroup, cdm, context));
		return originalQuery;
	}

	@NotNull private Consumer<CddSkeletonFactory> prepareDocumentGraphQuery(@NotNull QueryRoot originalQuery, String path, IGroup rootGroup, IDocumentModel cdm,
		QueryContext queryContext) {
		return csf -> {
			List<CddSkeletonGroup> cddSkeletonGroups = csf.constructSkeletonFromRootGroup(null, null, rootGroup).toList();
			if (path == null) {
				originalQuery.setTargetDocumentModel(ComposeDocumentModelUtils.getCrdModelName(cdm.getHeader()));
			} else if (CollectionUtils.isNotEmpty(cddSkeletonGroups)) {
				RelationshipModel relationshipModel = relationshipModelLoader.loadModel(cddSkeletonGroups.getFirst().getRelationshipModelName());
				originalQuery.setTargetDocumentModel(
					relationshipModel.getEntityCharacteristicsFromRole(cddSkeletonGroups.getFirst().getTargetRole()).getDocumentModel());
			}
			if (originalQuery.getLinks() == null) {
				originalQuery.setLinks(new ArrayList<>());
			}

			// If the originalQuery contains links, we do not add any CDM links to the query but do return what the client specified.
			if (CollectionUtils.isEmpty(originalQuery.getLinks())) {
				cddSkeletonGroups.forEach(skeletonGroup -> addLinksFromGroups(originalQuery.getLinks(), skeletonGroup.getChildren(), queryContext));
			}
		};
	}

	@Nullable private static String getPathFromFields(@NotNull QueryRoot originalQuery) {
		return CollectionUtils.isNotEmpty(originalQuery.getFields()) ? originalQuery.getFields().removeFirst() : null;
	}

	private static void addLinksFromGroups(Collection<QueryLink> links, List<CddSkeletonGroup> cddSkeletonGroups, QueryContext queryContext) {
		cddSkeletonGroups
			.forEach(group -> {
				if (group.isRelationship()) {
					QueryLink queryLink = QueryLink.builder()
						.relationshipModel(group.getRelationshipModelName())
						.targetRole(group.getTargetRole())
						.links(new ArrayList<>())
						.maxDepth(QueryConstants.CDM_LINK_MAX_DEPTH_FOR_NO_RECURSION)
						.build();
					links.add(queryLink);
					queryContext.getEnrichments().setSourceRole(queryLink, group.getSourceRole());
					addLinksFromGroups(queryLink.getLinks(), group.getChildren(), queryContext);
				} else {
					addLinksFromGroups(links, group.getChildren(), queryContext);
				}
			});
	}

	@Override public @NonNull QueryPage<DocumentTreeResult> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult,
		QueryContext context) {
		return documentProjectionImplementation.postprocess(originalQuery, queryResult, context);
	}
}
