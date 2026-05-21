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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.internal.MetadataUtils;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.internal.QueryConstants;
import com.mgmtp.a12.dataservices.query.internal.RootBasedPageImpl;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.header.Annotation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_SOURCE_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_TARGET_DOCUMENT_MODEL_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_TARGET_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.RELATIONSHIP_GROUP_NAME;
import static com.mgmtp.a12.dataservices.query.projection.internal.CddProjectionImplementation.PROJECTION_NAME;
import static com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils.getCrdModelName;

/**
 * @deprecated To be replaced by {@link JsonbCddProjectionImplementation}.
 */
//TODO A12S-5972: replace by JsonbCddProjectionImplementation
@Deprecated(since = "38.0.0")
@RequiredArgsConstructor
@QueryProjection(PROJECTION_NAME) public class CddProjectionImplementation implements IQueryProjection<DocumentSpec> {

	public static final String PROJECTION_NAME = "cdd";

	private final IDocumentModelService documentModelService;
	private final DataServicesDocumentFactory dataServicesDocumentFactory;
	private final DocumentSupport documentSupport;
	private final MetadataUtils metadataUtils;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final DocumentTreeHelper documentTreeHelper;

	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		validateCddQuery(originalQuery);

		return originalQuery.toBuilder()
			// TODO A12S-5398: Check that field paths of CDM matches CRD.
			.targetDocumentModel(getCrdModelName(context.getDocumentModel(originalQuery.getTargetDocumentModel())))
			.fields(documentTreeHelper.getAllFieldNamesOfCdm(originalQuery.getTargetDocumentModel(), context, documentModelService))
			.build();
	}

	@Override
	public @NonNull QueryPage<DocumentSpec> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> crds, QueryContext context) {
		IDocumentModel cdm = context.getDocumentModel(originalQuery.getTargetDocumentModel());
		List<DocumentSpec> enrichedCrds = crds.stream()
			.map(crd -> enrichCrdWithLinkedDocuments(context, crd, cdm.getHeader().getId()))
			.map(dataServicesDocumentFactory::newDataServicesDocument)
			.map(documentSupport::convertToDocumentSpec)
			.toList();
		LoadedDocumentReferencesContextHolder.addDocumentReferencesFromDocumentTreeResults(crds.getContent());

		return new RootBasedPageImpl<>(enrichedCrds, crds.getPageable(), crds.getTotalElements());
	}

	private static void validateCddQuery(@NonNull QueryRoot originalQuery) {
		Assert.isNull(originalQuery.getAggregation(), "Aggregations are not allowed in query with cdd projection.");
		Assert.isNull(originalQuery.getFields(), "Fields are not allowed in query with cdd projection.");
		Assert.isTrue(CollectionUtils.isEmpty(originalQuery.getLinks()), "Links are not allowed in query with cdd projection.");
	}

	private DocumentV2 enrichCrdWithLinkedDocuments(QueryContext context, DocumentTreeResult crd, String cdmName) {
		IDocumentModel cdm = context.getDocumentModel(cdmName);

		AtomicReference<DocumentV2> documentAtomicReference = new AtomicReference<>(DocumentV2.empty(cdmName));

		Page<DocumentTreeResult> links = getLinkedDocuments(context, crd, cdm);
		DocumentTreeHelper.LinkHierarchy linkedDocumentsHierarchy = new DocumentTreeHelper.LinkHierarchy(links);
		documentTreeHelper.toFields("", crd, linkedDocumentsHierarchy, cdm)
			.forEach(
				e -> documentAtomicReference.set(
					documentAtomicReference.get().withField(
						KernelUtils.fromPathAndRepetitions(e.getPath(), e.getRepetitions()),
						KernelUtils.fromV1Value(e)
					)
				)
			);

		links.forEach(l -> documentTreeHelper.toFields(l.getBackReference(), l, linkedDocumentsHierarchy, cdm)
			.forEach(e -> documentAtomicReference.set(
					documentAtomicReference.get().withField(
						KernelUtils.fromPathAndRepetitions(e.getPath(), e.getRepetitions()),
						KernelUtils.fromV1Value(e)
					)
				)
			));

		return metadataUtils.createDocumentMetadata(
			documentAtomicReference.get(),
			DocumentReference.builder().documentModelName(cdmName).documentId(crd.getDocRef().toString()).build(),
			UaaConnector.getCurrentUserName(), Instant.now(), null);
	}

	private Page<DocumentTreeResult> getLinkedDocuments(QueryContext context, DocumentTreeResult crd, IDocumentModel cdm) {

		ExactMatchOperator<Object> docRefConstraint = ExactMatchOperator.builder()
			.field(DocumentMetadataConstants.DOCREF_METADATA_PATH)
			.value(crd.getDocRef().toString())
			.build();

		DataServicesCoreProperties.Query.PageRequest pageRequest = dataServicesCoreProperties.getQuery().getPageRequest();
		QueryRoot linkedDocumentsQuery = QueryRoot.builder()
			.targetDocumentModel(getCrdModelName(cdm))
			.exclude(true)
			.links(processGroupsForCdmAnnotations(cdm.getContent().getDocumentModelRoot(), context).toList())
			.paging(new Paging(0, pageRequest.getPageNumberLimit() * pageRequest.getPageSizeLimit()))
			.sort(null)
			.constraint(docRefConstraint)
			.build();

		return context.query(List.of(DocumentTreeNodeType.LINK, DocumentTreeNodeType.CHILD), linkedDocumentsQuery);
	}

	private Stream<QueryLink> processGroupsForCdmAnnotations(IGroup group, QueryContext queryContext) {
		Map<String, String> annotations = group.getAnnotations().stream()
			.filter(a -> Set.of(CDM_SOURCE_ROLE_ANNOTATION, CDM_TARGET_ROLE_ANNOTATION, CDM_RELATIONSHIP_ANNOTATION, CDM_TARGET_DOCUMENT_MODEL_ANNOTATION)
				.contains(a.getName()))
			.collect(Collectors.toMap(Annotation::getName, Annotation::getValue));
		return annotations.containsKey(CDM_RELATIONSHIP_ANNOTATION)
			? Stream.of(addLink(annotations, group, queryContext))
			: getDirectChildGroups(group).flatMap(g -> processGroupsForCdmAnnotations(g, queryContext));
	}

	private QueryLink addLink(Map<String, String> annotations, IGroup group, QueryContext queryContext) {

		QueryLink link = QueryLink.builder()
			.relationshipModel(annotations.get(CDM_RELATIONSHIP_ANNOTATION))
			.targetRole(annotations.get(CDM_TARGET_ROLE_ANNOTATION))
			.links(getDirectChildGroups(group)
				.filter(g -> !(RELATIONSHIP_GROUP_NAME.equals(g.getName())))
				.flatMap(g -> processGroupsForCdmAnnotations(g, queryContext)).toList())
			.backReference(documentModelService.getPath(group))
			.maxDepth(QueryConstants.CDM_LINK_MAX_DEPTH_FOR_NO_RECURSION)
			.build();

		queryContext.getEnrichments().setTargetDocumentModel(link, annotations.get(CDM_TARGET_DOCUMENT_MODEL_ANNOTATION));
		queryContext.getEnrichments().setSourceRole(link, annotations.get(CDM_SOURCE_ROLE_ANNOTATION));

		return link;
	}

	public static Stream<IGroup> getDirectChildGroups(IGroup group) {
		return group.getElements().stream()
			.filter(IGroup.class::isInstance)
			.map(IGroup.class::cast);
	}
}
