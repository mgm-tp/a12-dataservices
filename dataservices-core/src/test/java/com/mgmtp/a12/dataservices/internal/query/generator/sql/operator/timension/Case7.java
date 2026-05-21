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
package com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension;

import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;

import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.DocumentModelConstants.JIRA_TICKET_DM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.JIRA_TICKET_ID_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.GroupConstants.JIRA_TICKET_GROUP;

public class Case7 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 7: Collect all A12 Jira tickets for A12 estimation report";
	}

	@Override protected String getDescription() {
		return "SELECT DISTINCT split_part(jira_ticket_id, '-', 1) FROM jira_ticket WHERE jira_ticket_id LIKE 'A12%' ORDER BY 1";
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.field(JIRA_TICKET_GROUP + JIRA_TICKET_ID_FIELD)
			.targetDocumentModel(JIRA_TICKET_DM)
			.constraint(ExactMatchOperator.builder()
				.field(JIRA_TICKET_GROUP + JIRA_TICKET_ID_FIELD)
				.value("A12")
				.build())
			.build();
	}
}
