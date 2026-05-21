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
package com.mgmtp.a12.dataservices.document;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeleton;
import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.internal.CddSkeletonFactory;
import com.mgmtp.a12.dataservices.cdd.internal.CddSupport;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.graph.DocumentGraph;
import com.mgmtp.a12.dataservices.document.graph.DocumentGraphLink;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import lombok.extern.slf4j.Slf4j;

import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY;

/**
 * Service for constructing a {@link DocumentGraph} starting from a source document and a CDM name.
 *
 * @deprecated as of 38.1.0 and will be removed in a future release. Use QueryService using `documentGraph` projection instead.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
@Slf4j
@Component public class DocumentGraphService {

	@Autowired private IModelLoader<ComposeDocumentModel> composeDocumentModelLoader;
	@Autowired private DocumentService documentService;
	@Autowired private DocumentSupport documentSupport;
	@Autowired private Optional<CddSkeletonFactory> cddSkeletonFactory;
	@Autowired private DocumentModelServiceFactory documentModelServiceFactory;

	/**
	 * Constructs a {@link DocumentGraph} for the given CDM name starting at the source document reference and path.
	 * The method walks the CDM skeleton, collects relationship links, and loads referenced documents.
	 *
	 * @param cdmName the compose document model name to use; must not be null
	 * @param sourceDocRef the source document reference from which the graph originates; must not be null
	 * @param path the CDM path (group) to start from; must not be null
	 * @return a {@link DocumentGraph} containing links and resolved document specs
	 */
	@Transactional(readOnly = true)
	public DocumentGraph constructDocumentGraph(String cdmName, DocumentReference sourceDocRef, String path) {
		Set<DocumentGraphLink> links = new OrderedHashSet<>();
		IDocumentModel cdm = composeDocumentModelLoader.loadModel(cdmName);

		cddSkeletonFactory.ifPresent(csf -> CddSupport.walkSkeleton(CddSkeleton.builder()
				.targetDocRef(sourceDocRef)
				.children(csf.constructSkeletonFromRootGroup(sourceDocRef, null,
					ComposeDocumentModelUtils.getRootGroup(path, cdm, documentModelServiceFactory))
					.toList())
				.build().getChildren(),
			g -> {
				Optional.of(g)
					.map(CddSkeletonGroup::getLink)
					.map(this::makeDocumentGraphLink)
					.ifPresent(links::add);
				return true;
			}));

		return new DocumentGraph(links.stream().toList(), Stream.concat(Stream.of(sourceDocRef), links.stream()
				.flatMap(l -> Stream.concat(Stream.of(l.getLinkDocRef()), l.getLinkDescriptor().getEntities().stream().map(RelationshipRoleSpec::getDocRef))))
			.distinct()
			.filter(Objects::nonNull)
			.map(docRef -> documentService.load(docRef)
				.orElseThrow(() -> new NotFoundException(DOCUMENT_NOT_FOUND_ERROR_KEY, String.format("Document [%s] not found", docRef))))
			.map(documentSupport::convertToDocumentSpec)
			.toList());
	}

	private DocumentGraphLink makeDocumentGraphLink(RelationshipLink l) {
		return new DocumentGraphLink(String.valueOf(l.getId()), makeLinkDescriptor(l), l.getLinkDocumentDocRef());
	}

	private DocumentGraphLink.DocumentGraphLinkDescriptor makeLinkDescriptor(RelationshipLink l) {
		return new DocumentGraphLink.DocumentGraphLinkDescriptor(l.getRelationshipModel(),
			l.getRoles().values().stream()
				.map(this::makeRelationshipRoleSpec)
				.toList());
	}

	private RelationshipRoleSpec makeRelationshipRoleSpec(RelationshipRole r) {
		return new RelationshipRoleSpec(r.getName(), r.getDocRef());
	}

}
