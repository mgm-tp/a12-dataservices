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

import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.DocumentModelConstants.JIRA_TICKET_DM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.ORDER_ID_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.PROJECT_CODE_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.GroupConstants.JIRA_TICKET_GROUP;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.GroupConstants.PROJECT_GROUP;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RelationshipModelConstants.NODE_TASK_TASK_TIME_RECORDING_RM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RelationshipModelConstants.PROJECT_NODE_TASK_RM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RelationshipModelConstants.TASK_TIME_RECORDING_JIRA_TICKET_RM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.PROJECT_ROLE;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.TASK_ROLE;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.TASK_TIME_RECORDING_ROLE;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.ValueConstants.PROJECT_CODE_LIST;

public class Case6 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 6: Get order of jira ticket for filter suggestion in Budget Report";
	}

	@Override protected String getDescription() {
		return """
			SELECT DISTINCT order_id FROM jira_ticket
			           INNER JOIN task_time_recording ttr ON jira_ticket.jira_ticket_id = ttr.work_for
			           INNER JOIN node_task nt ON ttr.node_task_id = nt.node_task_id
			                    INNER JOIN project p ON nt.project_id = p.project_id AND p.project_code IN (%s)
			""".formatted(listAsString(PROJECT_CODE_LIST));
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.field(JIRA_TICKET_GROUP + ORDER_ID_FIELD)
			.field(JIRA_TICKET_GROUP + ORDER_ID_FIELD)
			.targetDocumentModel(JIRA_TICKET_DM)
			.constraint(HasOperator.builder()
				.relationshipModel(TASK_TIME_RECORDING_JIRA_TICKET_RM)
				.targetRole(TASK_TIME_RECORDING_ROLE)
				.constraint(HasOperator.builder()
					.relationshipModel(NODE_TASK_TASK_TIME_RECORDING_RM)
					.targetRole(TASK_ROLE)
					.constraint(HasOperator.builder()
						.relationshipModel(PROJECT_NODE_TASK_RM)
						.targetRole(PROJECT_ROLE)
						.constraint(makeProjectsConstraint(PROJECT_CODE_LIST, PROJECT_GROUP + PROJECT_CODE_FIELD))
						.build())
					.build())
				.build())
			.build();
	}
}
