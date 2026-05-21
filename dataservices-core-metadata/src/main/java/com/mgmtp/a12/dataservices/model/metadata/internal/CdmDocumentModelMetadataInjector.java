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
package com.mgmtp.a12.dataservices.model.metadata.internal;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Annotation;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.RELATIONSHIP_GROUP_NAME;
import static com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

public class CdmDocumentModelMetadataInjector extends DmDocumentModelMetadataInjector {

	public CdmDocumentModelMetadataInjector(IDocumentModel documentModel, DocumentModelJoiningService documentModelJoiningService,
		DocumentModelService documentModelService, Locale locale) {

		super(documentModel, documentModelJoiningService, documentModelService, locale);
	}

	/**
	 * Add document metadata to all groups annotated by `cdm.relationship` and also to its children group named "relationship" if it exists.
	 *
	 * @param enrichedModel         document model to enrich.
	 * @param documentMetadataGroup metadata group to add to CDM groups.
	 */
	@Override protected void addMetadata(Group documentMetadataGroup, DocumentModel enrichedModel) {
		getVisitorAfterWalk(enrichedModel, new CdmMetadataVisitor(documentMetadataGroup));
	}

	/**
	 * {@link DocumentModelVisitor} adding metadata to CDM groups.
	 */
	@RequiredArgsConstructor
	private class CdmMetadataVisitor extends DocumentModelVisitor {
		private final Group documentMetadataGroup;

		@Override public VisitProcess visitGroup(Group group) {

			if (hasAnnotation(group, CDM_RELATIONSHIP_ANNOTATION)) {
				enrichRelationshipChild(group);
				if (group.getElements().stream()
					.noneMatch(g -> DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME.equals(g.getName()) && g instanceof Group)) {
					enrichCdmGroup(group);
				}
			}

			return super.visitGroup(group);
		}

		private void enrichCdmGroup(Group group) {
			group.addElement(copyGroupAndAddParentIdPrefixToAllElements(documentMetadataGroup, group));
		}

		private void enrichRelationshipChild(Group group) {
			group.getElements().stream()
				.filter(e -> RELATIONSHIP_GROUP_NAME.equals(e.getName()))
				.filter(Group.class::isInstance)
				.map(Group.class::cast)
				.forEach(g -> g.addElement(copyGroupAndAddParentIdPrefixToAllElements(documentMetadataGroup, g)));
		}

		private static boolean hasAnnotation(Group group, String annotationName) {
			return Optional.ofNullable(group)
				.map(Element::getAnnotations)
				.stream()
				.flatMap(Collection::stream)
				.map(Annotation::getName)
				.anyMatch(annotationName::equals);
		}
	}
}
