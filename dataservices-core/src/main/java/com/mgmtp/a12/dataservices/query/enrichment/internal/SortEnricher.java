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
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;

import lombok.RequiredArgsConstructor;

/**
 * Enriches relationship-based sort orders on a {@link QueryRoot}. For each
 * {@link RelationshipOrder} found in the sort list, it walks the chain of nested orders and
 * enriches field types of every target document model along the way, so that typed sorting
 * (string collation, dates, enumerations) works correctly during query execution.
 */
@RequiredArgsConstructor
public class SortEnricher {

	private final ModelTypeService modelTypeService;
	private final DocumentModelUtils documentModelUtils;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	public void enrich(QueryRoot queryRoot, QueryContext context) {
		if (queryRoot.getSort() == null) {
			return;
		}
		queryRoot.getSort().stream()
			.filter(RelationshipOrder.class::isInstance)
			.map(RelationshipOrder.class::cast)
			.forEach(order -> enrichRelationshipOrderRecursively(order, context));
	}

	private void enrichRelationshipOrderRecursively(Order sortSpec, QueryContext context) {

		if (!(sortSpec instanceof RelationshipOrder relOrder) || relOrder.relationshipModel() == null) {
			return;
		}

		RelationshipModel relationshipModel = context.getRelationshipModel(relOrder.relationshipModel());

		String targetDocumentModelName = relationshipModel.getEntityCharacteristicsFromRole(relOrder.targetRole()).getDocumentModel();
		FieldTypeEnrichmentHelper.enrichFieldTypes(targetDocumentModelName, context, modelTypeService, documentModelUtils, documentModelServiceFactory);

		String sourceRoleName = relationshipModel.getTargetEntityCharacteristicsFromSourceRole(relOrder.targetRole()).getRole();
		context.getEnrichments().setSourceRoleForRelationshipOrder(relOrder, sourceRoleName);

		enrichRelationshipOrderRecursively(relOrder.sortBy(), context);
	}
}
