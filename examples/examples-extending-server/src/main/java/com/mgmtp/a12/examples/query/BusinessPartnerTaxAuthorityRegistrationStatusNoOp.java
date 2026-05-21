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
package com.mgmtp.a12.examples.query;

import java.util.Collections;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.NonNull;

import static com.mgmtp.a12.examples.query.BusinessPartnerTaxAuthorityRegistrationStatus.PROJECTION_NAME;

/**
 * No-op projection overriding the original implementation; returns results unchanged.
 * Delegates all shaping/aggregation to the SQL query instead of performing transformations in Java. Each projection
 * provided by DS can be overridden like this.
 * This bean should exist next to `BusinessPartnerTaxAuthorityRegistrationStatus` bean to demonstrate that multiple implementations of the same projection
 * can exist while only one is active.
 */
@Order(99)
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "business-partner-tax-authority-registration-status-projection.enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "business-partner-tax-authority-registration-status-projection.no-op.enabled", havingValue = "true")
@QueryProjection(PROJECTION_NAME)
@Component public class BusinessPartnerTaxAuthorityRegistrationStatusNoOp implements IQueryProjection<DocumentTreeResult> {

	/**
	 * Projection name used to register this NoOp implementation.
	 */
	public static final String PROJECTION_NAME = "businessPartnerTaxAuthorityRegistrationStatus";

	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		return originalQuery;
	}

	@Override
	public @NonNull QueryPage<DocumentTreeResult> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult,
		QueryContext context) {
		return QueryPage.of(queryResult.getContent(), queryResult.getTotalElements(), queryResult.getSize(), queryResult.getNumber(), Collections.emptyMap());
	}
}
