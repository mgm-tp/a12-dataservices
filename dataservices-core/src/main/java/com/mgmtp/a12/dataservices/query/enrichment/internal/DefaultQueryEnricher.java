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

import org.apache.commons.lang3.time.StopWatch;

import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.enrichement.internal.FieldPathValidator;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultQueryEnricher implements QueryEnricher {

	private final ModelTypeService modelTypeService;
	private final DocumentModelUtils documentModelUtils;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final SortEnricher sortEnricher;
	private final ConstraintEnricher constraintEnricher;
	private final AggregationEnricher aggregationEnricher;
	private final LinkEnricher linkEnricher;

	/**
	 * Enriches the provided query topology node by delegating to focused enrichers
	 * (heterogeneity, field types, sort, constraint, locale, links and aggregation).
	 *
	 * @param queryRoot The topology node to be enriched.
	 * @param context Locale to use for localized queries and responses.
	 */
	@Override public void enrichQuery(QueryRoot queryRoot, QueryContext context) {
		StopWatch stopWatch = StopWatch.createStarted();

		Enrichments enrichments = context.getEnrichments();
		String targetDocumentModel = enrichments.getTargetDocumentModel(queryRoot);

		enrichments.computeModelSubtypes(targetDocumentModel, modelTypeService::findAllSubtypes);
		FieldTypeEnrichmentHelper.enrichFieldTypes(targetDocumentModel, context, modelTypeService, documentModelUtils, documentModelServiceFactory);
		sortEnricher.enrich(queryRoot, context);
		validateProjectionFieldPaths(queryRoot.getFields());
		queryRoot.setConstraint(constraintEnricher.enrich(queryRoot.getConstraint(), targetDocumentModel, context));

		enrichments.computeModelLocale(queryRoot.getTargetDocumentModel(),
			k -> LocaleEnrichmentHelper.determineEnrichedLocale(context, k));

		Collection<QueryLink> links = queryRoot.getLinks();
		if (links != null) {
			links.forEach(link -> linkEnricher.enrich(link, context));
		}

		aggregationEnricher.enrich(queryRoot, context);

		log.atDebug().log("Query enrichment took in {} ms", stopWatch.getDuration().toMillis());
	}

	private static void validateProjectionFieldPaths(List<String> fields) {
		if (fields != null) {
			fields.forEach(FieldPathValidator::validateFieldPath);
		}
	}
}
