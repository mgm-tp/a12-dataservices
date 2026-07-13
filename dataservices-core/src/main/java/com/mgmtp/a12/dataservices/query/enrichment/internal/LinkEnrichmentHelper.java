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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;

import lombok.NoArgsConstructor;

/**
 * Helper for `LinkAware` / relationship-model lookups used by the link-related enrichers
 * (`LinkEnricher`, `HasOperatorEnricher`).
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class LinkEnrichmentHelper {

	/**
	 * Carries the per-`LinkAware` data resolved by {@link #enrichLinkAware} so callers can apply
	 * their type-specific follow-up enrichments (`ordered`, ABAC, locale, etc.) without repeating
	 * the underlying lookups.
	 *
	 * @param targetDocumentModel the document model resolved from the target role.
	 * @param linkDocumentModel the link-document model of the relationship, or `null` when none.
	 * @param sourceCharacteristics the source-side {@link EntityCharacteristics} of the relationship.
	 */
	record LinkAwareEnrichment(String targetDocumentModel, String linkDocumentModel, EntityCharacteristics sourceCharacteristics) {
	}

	/**
	 * Performs the common `LinkAware` enrichment block shared by `LinkEnricher` and
	 * `HasOperatorEnricher`: resolves target/source role characteristics, stores the target
	 * document model and source role on the context, computes target-model subtypes, enriches
	 * field types, and resolves the link-document-model subtypes.
	 *
	 * Callers receive the resolved data via the returned {@link LinkAwareEnrichment} record so
	 * they can layer their type-specific follow-up (e.g. `ordered` flag, ABAC injection,
	 * locale and recursion) without redoing the lookups.
	 */
	public static LinkAwareEnrichment enrichLinkAware(LinkAware linkAware, QueryContext context, ModelTypeService modelTypeService,
		DocumentModelUtils documentModelUtils, DocumentModelServiceFactory documentModelServiceFactory) {

		Enrichments enrichments = context.getEnrichments();
		EntityCharacteristics targetCharacteristics = getTargetRoleCharacteristics(linkAware, context);
		EntityCharacteristics sourceCharacteristics = getSourceRoleCharacteristics(linkAware, context);
		String targetDocumentModel = targetCharacteristics.getDocumentModel();

		enrichments.setTargetDocumentModel(linkAware, targetDocumentModel);
		enrichments.setSourceRole(linkAware, sourceCharacteristics.getRole());
		enrichments.computeModelSubtypes(targetDocumentModel, modelTypeService::findAllSubtypes);
		FieldTypeEnrichmentHelper.enrichFieldTypes(targetDocumentModel, context, modelTypeService, documentModelUtils, documentModelServiceFactory);
		String linkDocumentModel = computeLinkDocumentModelSubtypes(linkAware.getRelationshipModel(), context, modelTypeService);

		return new LinkAwareEnrichment(targetDocumentModel, linkDocumentModel, sourceCharacteristics);
	}

	private static EntityCharacteristics getTargetRoleCharacteristics(LinkAware linkAware, QueryContext context) {
		RelationshipModel relationshipModel = context.getRelationshipModel(linkAware.getRelationshipModel());
		return relationshipModel.getEntityCharacteristicsFromRole(linkAware.getTargetRole());
	}

	private static EntityCharacteristics getSourceRoleCharacteristics(LinkAware linkAware, QueryContext context) {
		RelationshipModel relationshipModel = context.getRelationshipModel(linkAware.getRelationshipModel());
		return relationshipModel.getTargetEntityCharacteristicsFromSourceRole(linkAware.getTargetRole());
	}

	/**
	 * Resolves the link document model for `relationshipModelName` and, when non-null, eagerly
	 * computes its subtypes in the context.
	 *
	 * @return the link document model name, or `null` if the relationship has no link document model.
	 */
	private static String computeLinkDocumentModelSubtypes(String relationshipModelName, QueryContext context, ModelTypeService modelTypeService) {
		String linkDocumentModel = context.getRelationshipModel(relationshipModelName)
			.getContent()
			.getLinkDocumentModel();
		if (linkDocumentModel != null) {
			context.getEnrichments().computeModelSubtypes(linkDocumentModel, modelTypeService::findAllSubtypes);
		}
		return linkDocumentModel;
	}
}
