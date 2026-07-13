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
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.enrichment.internal.LinkEnrichmentHelper.LinkAwareEnrichment;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;

import lombok.RequiredArgsConstructor;

/**
 * Enriches `HasOperator` nodes during the query enrichment phase.
 *
 * Implements `IQueryAPIOperatorEnricher` to populate enrichment data for `HasOperator` nodes:
 * the resolved target document model, source role, model subtypes, field types, and link
 * document model subtypes (all delegated to `LinkEnrichmentHelper.enrichLinkAware`). It also
 * injects ABAC rules into the operator's constraint.
 *
 * This class does not recurse into nested operators; recursion is the responsibility of
 * `QueryAPIOperatorWalker`.
 */
@RequiredArgsConstructor
public class HasOperatorEnricher implements IQueryAPIOperatorEnricher {

	private final ModelTypeService modelTypeService;
	private final DocumentModelUtils documentModelUtils;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final QueryAuthorizationService queryAuthorizationService;

	@Override
	public boolean enrich(ILogicOperator operator, QueryContext context) {
		if (operator instanceof HasOperator hasOperator) {
			LinkAwareEnrichment enrichment = LinkEnrichmentHelper.enrichLinkAware(hasOperator, context, modelTypeService, documentModelUtils,
				documentModelServiceFactory);
			hasOperator.setConstraint(queryAuthorizationService.addAbacRules(hasOperator.getConstraint(), enrichment.targetDocumentModel()));
			return true;
		}
		return false;
	}
}
