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

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.internal.AbstractQueryTopology;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.internal.RootBasedPageImpl;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.RELATIONSHIP_GROUP_NAME;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_PREPROCESSING;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_INVALID_INPUT_ERROR_KEY;
import static com.mgmtp.a12.dataservices.query.projection.internal.CddProjectionImplementation.PROJECTION_NAME;
import static com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils.getCrdModelName;

@Slf4j @RequiredArgsConstructor
@QueryProjection(PROJECTION_NAME) public class CddProjectionImplementation implements IQueryProjection<DocumentSpec> {

	public static final String PROJECTION_NAME = "cdd";
	public static final String INVALID_TARGET_DOCUMENT_MODEL =
		"CDD projection expects a CDM in the targetDocumentModel";

	private final IDocumentModelService documentModelService;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final ObjectMapper objectMapper;
	private final DocumentTreeHelper documentTreeHelper;
	private final Optional<KernelDocumentService> kernelDocumentService;
	private final DocumentSupport documentSupport;
	private final CdmHelper cdmHelper;

	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		IDocumentModel cdm = context.getDocumentModel(originalQuery.getTargetDocumentModel());
		String crdModelName = getCrdModelName(cdm);
		if (StringUtils.isBlank(crdModelName)) {
			throw new QueryInvalidInputException(QUERY_PREPROCESSING, QUERY_INVALID_INPUT_ERROR_KEY, null)
				.withAnonymityMessage(INVALID_TARGET_DOCUMENT_MODEL);
		}
		QueryRoot.QueryRootBuilder<?, ?> queryRootBuilder = originalQuery.toBuilder()
			.targetDocumentModel(crdModelName)
			.links(null);
		// For aggregations, we must not preprocess field names.
		if (!originalQuery.isAggregated()) {
			validateCddQuery(originalQuery, cdm);
			queryRootBuilder
				.fields(documentTreeHelper.getAllFieldNamesOfCdm(originalQuery.getTargetDocumentModel(), context, documentModelService));
		}
		return queryRootBuilder.build();
	}

	@Override
	public @NonNull QueryPage<DocumentSpec> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult, QueryContext context) {
		return originalQuery.isAggregated()
			? postprocessAggregated(queryResult)
			: postprocessNonAggregated(originalQuery, queryResult, context);
	}

	@NotNull private RootBasedPageImpl<DocumentSpec> postprocessNonAggregated(@NotNull QueryRoot originalQuery, @NotNull Page<DocumentTreeResult> queryResult,
		QueryContext context) {
		LoadedDocumentReferencesContextHolder.addDocumentReferencesFromDocumentTreeResults(queryResult.getContent());

		String rootDocumentModel = originalQuery.getTargetDocumentModel();
		IDocumentModel cdm = context.getDocumentModel(rootDocumentModel);

		Collection<DocumentTreeResult> linkResults = queryForLinks(getCrdDocRefs(queryResult), cdm, context);

		Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linksBySourceDocRefAndBackReference = mergeLinkDocumentsToLinkedDocuments(linkResults)
			.collect(
				Collectors.groupingBy(DocumentTreeResult::getSourceDocRef,
					Collectors.groupingBy(DocumentTreeResult::getBackReference, Collectors.toList())));

		boolean computationsAllowed = kernelDocumentService.isPresent() && isComputationEnabledForModel(rootDocumentModel);
		List<DocumentSpec> enrichedCrds = queryResult.stream()
			.filter(r -> DocumentTreeNodeType.ROOT.equals(r.getType()))
			.map(crd -> constructDocumentSpec(context, crd, rootDocumentModel, cdm, linksBySourceDocRefAndBackReference, computationsAllowed))
			.toList();

		return new RootBasedPageImpl<>(enrichedCrds, queryResult.getPageable(), queryResult.getTotalElements());
	}

	@NotNull private static RootBasedPageImpl<DocumentSpec> postprocessAggregated(@NotNull Page<DocumentTreeResult> queryResult) {
		// For aggregations, we do not apply cdd postprocessing but just return the list of aggregated results.
		List<DocumentSpec> aggregations = queryResult.stream()
			.map(r -> new DocumentSpec(r.getDocRef(), r.getDocument().toString()))
			.toList();
		return new RootBasedPageImpl<>(aggregations, queryResult.getPageable(), queryResult.getTotalElements());
	}

	@NotNull private DocumentSpec constructDocumentSpec(QueryContext context, DocumentTreeResult crd, String rootDocumentModel, IDocumentModel cdm,
		Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linkedDocuments, boolean computationsAllowed) {
		DocumentReference docRef = new DocumentReference(rootDocumentModel, crd.getDocRef().toString());
		String documentContent = handleComputations(rootDocumentModel,
			context.getLocale(),
			objectMapper.writeValueAsString(documentTreeHelper.constructDocumentFromFieldsAndLinks(crd, cdm, linkedDocuments)),
			computationsAllowed);
		return new DocumentSpec(docRef, documentContent);
	}

	@NotNull private static Set<String> getCrdDocRefs(@NotNull Page<DocumentTreeResult> queryResult) {
		return queryResult.stream()
			.filter(r -> DocumentTreeNodeType.ROOT.equals(r.getType()))
			.map(DocumentTreeResult::getDocRef)
			.map(DocumentReference::toString)
			.collect(Collectors.toSet());
	}

	@NotNull private Collection<DocumentTreeResult> queryForLinks(Set<String> docRefs, IDocumentModel cdm, QueryContext context) {
		boolean hasLinks = CollectionUtils.isNotEmpty(context.getOriginalQuery().getLinks());
		return Optional.of(docRefs)
			.filter(CollectionUtils::isNotEmpty)
			.map(dr -> constructDocRefConstraints(dr.stream()))
			.filter(CollectionUtils::isNotEmpty)
			.map(dr -> constructQueryFromLinks(cdm, dr,
				hasLinks
					? context.getOriginalQuery().getLinks()
					: cdmHelper.cdmToLinks(cdm.getContent().getDocumentModelRoot(), context).toList(),
				dataServicesCoreProperties.getQuery().getPageRequest()))
			.filter(q -> CollectionUtils.isNotEmpty(q.getLinks()))
			.map(q -> context.query(List.of(DocumentTreeNodeType.LINK, DocumentTreeNodeType.CHILD), q).getContent())
			.orElse(Collections.emptyList());
	}

	@NotNull static List<ILogicOperator> constructDocRefConstraints(Stream<String> dr) {
		return dr
			.filter(StringUtils::isNotBlank)
			.map(docRef -> (ILogicOperator) ExactMatchOperator.builder()
				.field(DocumentMetadataConstants.DOCREF_METADATA_PATH)
				.value(docRef)
				.build())
			.toList();
	}

	private String handleComputations(String targetCDMName, String locale, String documentStr, boolean computationsAllowed) {

		if (!computationsAllowed) {
			return documentStr;
		}

		DocumentV2 computedDocument = kernelDocumentService.get()
			.computeDocument(
				documentSupport.convertJSONToDocument(targetCDMName, new StringReader(documentStr)),
				Optional.ofNullable(locale)
					.filter(StringUtils::isNotBlank)
					.map(Locale::of)
					.orElse(Locale.getDefault())
			);
		return objectMapper.writeValueAsString(computedDocument);
	}

	@NotNull private static Stream<DocumentTreeResult> mergeLinkDocumentsToLinkedDocuments(Collection<DocumentTreeResult> linksAndLinkDocuments) {

		Map<String, MutablePair<DocumentTreeResult, DocumentTreeResult>> linksWithLinkDocuments = new LinkedHashMap<>();
		for (DocumentTreeResult entity : linksAndLinkDocuments) {
			MutablePair<DocumentTreeResult, DocumentTreeResult> p = linksWithLinkDocuments
				.computeIfAbsent(entity.getLinkId(), k -> MutablePair.of(null, null));

			if (p.getLeft() == null || p.getRight() == null) {

				if (DocumentTreeNodeType.LINK.equals(entity.getType())) {
					p.setRight(entity);
				} else if (DocumentTreeNodeType.CHILD.equals(entity.getType())) {
					p.setLeft(entity);
				} else {
					throw new UnexpectedException("Illegal type.");
				}

				if (p.getLeft() != null && p.getRight() != null) {
					ObjectNode relationshipNode = p.getLeft().getDocument().withObject(RELATIONSHIP_GROUP_NAME);
					p.getRight().getDocument().properties().iterator()
						.forEachRemaining(ld -> relationshipNode.set(ld.getKey(), ld.getValue()));
				}
			}
		}
		return linksWithLinkDocuments.values().stream()
			.map(MutablePair::getLeft);
	}

	QueryRoot constructQueryFromLinks(IDocumentModel cdm, Collection<ILogicOperator> docRefsConstraint, Collection<QueryLink> links,
		DataServicesCoreProperties.Query.PageRequest pageRequest) {
		return QueryRoot.builder()
			.targetDocumentModel(getCrdModelName(cdm))
			.exclude(true)
			.links(links)
			.paging(new Paging(0, pageRequest.getPageNumberLimit() * pageRequest.getPageSizeLimit()))
			.sort(null)
			.constraint(OrOperator.builder()
				.operands(docRefsConstraint)
				.build())
			.build();

	}

	private void validateCddQuery(@NonNull QueryRoot originalQuery, IDocumentModel cdm) {
		if (originalQuery.getFields() != null) {
			throw new QueryValidationException(ExceptionKeys.ExecutionPhase.QUERY_VALIDATION, ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE,
				ExceptionKeys.INVALID_QUERY_ERROR_KEY, "Fields are not allowed in query with cdd projection.");
		}
		if (dataServicesCoreProperties.getQuery().getValidation().isEnabled()) {
			validateLinks(originalQuery.getLinks(), documentModelServiceFactory.createDocumentModelSearchService(cdm));
		}
	}

	private void validateLinks(@Nullable Collection<QueryLink> links, IDocumentModelSearchService documentModelSearchService) {
		Optional.ofNullable(links)
			.stream()
			.flatMap(Collection::stream)
			.filter(Objects::nonNull)
			.forEach(l -> {
				Optional<String> backReference = Optional.ofNullable(l)
					.map(AbstractQueryTopology::getBackReference)
					.filter(StringUtils::isNotBlank);
				if (backReference.isEmpty()) {
					log.warn("Link does not have a back reference defined.");
				} else if (backReference
					.flatMap(documentModelSearchService::getByPath)
					.isEmpty()) {
					log.warn("Link with back reference '{}' does not point to a valid relationship group.", l.getBackReference());
				}
				validateLinks(l.getLinks(), documentModelSearchService);
			});
	}

	private boolean isComputationEnabledForModel(String targetDocumentModel) {
		return Optional.of(dataServicesCoreProperties)
			.map(DataServicesCoreProperties::getDocuments)
			.map(DataServicesCoreProperties.Document::getComputation)
			.map(DataServicesCoreProperties.Document.Computation::getEnabledForModels)
			.filter(computationModels -> GenericUtils.matchOrAll(targetDocumentModel, computationModels))
			.isPresent();
	}
}
