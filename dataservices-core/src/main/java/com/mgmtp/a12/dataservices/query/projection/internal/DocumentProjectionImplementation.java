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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.internal.RootBasedPageImpl;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesIOUtils;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;
import static com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation.PROJECTION_NAME;

@RequiredArgsConstructor
@Order
@Component
@QueryProjection(PROJECTION_NAME) public class DocumentProjectionImplementation implements IQueryProjection<DocumentTreeResult> {

	public static final String PROJECTION_NAME = "document";

	private final ObjectMapper objectMapper;
	private final DocumentTreeHelper documentTreeHelper;
	private final AggregatedDocumentRepository documentRepository;
	private final DocumentSupport documentSupport;

	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		return originalQuery;
	}

	@Override
	public @NonNull QueryPage<DocumentTreeResult> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult,
		QueryContext context) {

		Map<UUID, QueryTopology> topologyMap = createTopologyMapRecursive(originalQuery)
			.collect(Collectors.toMap(Pair::getKey, Pair::getValue));

		LoadedDocumentReferencesContextHolder.addDocumentReferencesFromDocumentTreeResults(queryResult.getContent());
		if (!originalQuery.isAggregated()) {
			for (DocumentTreeResult result : queryResult.getContent()) {
				Collection<String[]> fieldsForCurrentNode = getFieldsForCurrentNode(result.getInternalId(), topologyMap, result.getType());
				if (CollectionUtils.isNotEmpty(fieldsForCurrentNode)) {
					updateDocumentFromFields(result, fieldsForCurrentNode);
				} else {
					updateDocumentFromDocumentRepository(result);
				}
			}
		}
		return new RootBasedPageImpl<>(queryResult.getContent(), queryResult.getPageable(), queryResult.getTotalElements());
	}

	@NotNull private static List<String[]> getFieldsForCurrentNode(UUID internalId, Map<UUID, QueryTopology> topologyMap, DocumentTreeNodeType type) {
		return Optional.ofNullable(topologyMap).map(m -> m.get(internalId))
			.map(DocumentTreeNodeType.LINK.equals(type) ? QueryTopology::getLinkDocumentFields : QueryTopology::getFields)
			.stream()
			.flatMap(Collection::stream)
			.map(f -> f.substring(1))
			.map(f -> f.split(FIELD_SEPARATOR))
			.toList();
	}

	private Stream<Pair<UUID, QueryTopology>> createTopologyMapRecursive(@NonNull QueryTopology parent) {
		return Stream.concat(Stream.of(Pair.of(parent.getInternalId(), parent)),
			Optional.of(parent)
				.map(QueryTopology::getLinks)
				.filter(CollectionUtils::isNotEmpty).stream()
				.flatMap(Collection::stream)
				.flatMap(this::createTopologyMapRecursive));
	}

	private void updateDocumentFromDocumentRepository(DocumentTreeResult result) {
		result.setDocument(documentRepository.getByDocumentReference(result.getDocRef())
			.map(this::dataServicesDocumentToJsonNode)
			.orElse(null));
	}

	private void updateDocumentFromFields(DocumentTreeResult documentTreeResult, Collection<String[]> fieldsForCurrentNode) {
		documentTreeHelper.removeUnwantedFields(documentTreeResult, fieldsForCurrentNode);
	}

	private JsonNode dataServicesDocumentToJsonNode(DataServicesDocument dataServicesDocument) {
		if (dataServicesDocument == null) {
			return objectMapper.nullNode();
		}

		return objectMapper.readTree(
			DataServicesIOUtils.writeString(sw -> documentSupport.convertDocumentToJSON(dataServicesDocument.getKernelDocument(), sw)));
	}
}
