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

import java.util.Collection;
import java.util.List;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.enrichement.internal.FieldPathValidator;
import com.mgmtp.a12.dataservices.query.enrichment.internal.LinkEnrichmentHelper.LinkAwareEnrichment;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;

import lombok.RequiredArgsConstructor;

/**
 * Enriches a {@link QueryLink} subtree with link-specific data: the common `LinkAware` block
 * (target/source role characteristics, model subtypes, field types, link-document model
 * subtypes — delegated to `LinkEnrichmentHelper.enrichLinkAware`), plus the link-only steps:
 * `ordered` flag, regular and link-document constraint enrichment, model locale and the
 * configured `maxLinksSize` guard. Recurses into nested `QueryLink`s so a single call enriches
 * the entire link branch.
 */
@RequiredArgsConstructor
public class LinkEnricher {

	private final ModelTypeService modelTypeService;
	private final DocumentModelUtils documentModelUtils;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final QueryAPIOperatorWalker queryAPIOperatorWalker;
	private final ConstraintEnricher constraintEnricher;

	public void enrich(QueryLink queryLink, QueryContext context) {

		LinkAwareEnrichment enrichment = LinkEnrichmentHelper.enrichLinkAware(queryLink, context, modelTypeService, documentModelUtils,
			documentModelServiceFactory);

		queryLink.setOrdered(Boolean.TRUE.equals(enrichment.sourceCharacteristics().getOrdered()));

		validateProjectionFieldPaths(queryLink.getFields());
		validateProjectionFieldPaths(queryLink.getLinkDocumentFields());
		queryLink.setConstraint(constraintEnricher.enrich(queryLink.getConstraint(), enrichment.targetDocumentModel(), context));
		queryAPIOperatorWalker.walkConstraint(queryLink.getLinkDocumentConstraint(), enrichment.linkDocumentModel(), context);

		// We set max links size to configured value plus 1 because we want to throw an InvalidInputException if this limit is exceeded
		queryLink.setMaxLinksSize(dataServicesCoreProperties.getQuery().getMaxLinksSize() + 1);

		Collection<QueryLink> links = queryLink.getLinks();
		if (links != null) {
			links.forEach(link -> enrich(link, context));
		}
	}

	private static void validateProjectionFieldPaths(List<String> fields) {
		if (fields != null) {
			fields.forEach(FieldPathValidator::validateFieldPath);
		}
	}
}
