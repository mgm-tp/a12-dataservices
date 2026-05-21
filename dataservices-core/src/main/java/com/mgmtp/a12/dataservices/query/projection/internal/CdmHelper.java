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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.header.Annotation;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_SOURCE_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_TARGET_DOCUMENT_MODEL_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_TARGET_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.RELATIONSHIP_GROUP_NAME;

@RequiredArgsConstructor
@Component public class CdmHelper {

	private final IDocumentModelService documentModelService;

	public static Stream<IGroup> getDirectChildGroups(IGroup group) {
		return group.getElements().stream()
			.filter(IGroup.class::isInstance)
			.map(IGroup.class::cast);
	}

	@NotNull public static CdmAnnotations getCdmAnnotations(IGroup group) {
		Map<String, String> annotations = group.getAnnotations().stream().collect(Collectors.toMap(Annotation::getName, Annotation::getValue));
		return new CdmAnnotations(annotations.get(CDM_TARGET_DOCUMENT_MODEL_ANNOTATION), annotations.get(CDM_RELATIONSHIP_ANNOTATION),
			annotations.get(CDM_SOURCE_ROLE_ANNOTATION), annotations.get(CDM_TARGET_ROLE_ANNOTATION));
	}

	Stream<QueryLink> cdmToLinks(IGroup group, QueryContext context) {
		CdmAnnotations annotations = CdmHelper.getCdmAnnotations(group);
		Stream<IGroup> directChildGroups = CdmHelper.getDirectChildGroups(group);
		if (StringUtils.isNotBlank(annotations.relationshipModelName())) {
			QueryLink link = QueryLink.builder()
				.relationshipModel(annotations.relationshipModelName())
				.targetRole(annotations.targetRole())
				.links(CdmHelper.getDirectChildGroups(group)
					.filter(g -> !(RELATIONSHIP_GROUP_NAME.equals(g.getName())))
					.flatMap(g -> cdmToLinks(g, context))
					.toList())
				.maxDepth(1)
				.backReference(documentModelService.getPath(group))
				.build();
			context.getEnrichments().setTargetDocumentModel(link, annotations.targetDocumentModel());
			context.getEnrichments().setSourceRole(link, annotations.sourceRole());
			return Stream.of(link);
		} else {
			return directChildGroups
				.flatMap(g -> cdmToLinks(g, context));
		}
	}

	public record CdmAnnotations(String targetDocumentModel, String relationshipModelName, String sourceRole, String targetRole) {}
}
