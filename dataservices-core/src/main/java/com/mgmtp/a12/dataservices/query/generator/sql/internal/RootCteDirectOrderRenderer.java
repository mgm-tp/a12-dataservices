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
package com.mgmtp.a12.dataservices.query.generator.sql.internal;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;

/**
 * Renders `ORDER BY` clauses for direct field sorting on the root CTE query.
 *
 * Handles the original, non-relationship-based sorting where sort fields
 * reference columns directly accessible from the root document table.
 */
class RootCteDirectOrderRenderer extends RootCteOrderRenderer {

	RootCteDirectOrderRenderer(List<Order> sort, QueryGeneratorContext context) {
		super(sort, context);
	}

	@Override
	void appendSelectClause(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.ASTERISK);
	}

	@Override
	void renderJoinsAndOrderBy(StringBuilder sb) {
		if (CollectionUtils.isNotEmpty(sort)) {
			sb.append(QueryGeneratorConstants.ORDER_BY_KEYWORD)
				.append(sort.stream()
					.filter(DirectFieldOrder.class::isInstance)
					.map(DirectFieldOrder.class::cast)
					.map(order -> {
						StringBuilder exprSb = new StringBuilder();
						appendDirectFieldOrderColumns(exprSb, order);
						appendOrderDirection(exprSb, order);
						appendNullHandling(exprSb, order);
						return exprSb.toString();
					})
					.collect(Collectors.joining(QueryGeneratorConstants.COMMA)));
		}
	}

	@Override
	void appendFinalSelectOrderBy(StringBuilder sb) {
		// no-op: direct field ordering does not require a rank column in the outer SELECT
	}
}
