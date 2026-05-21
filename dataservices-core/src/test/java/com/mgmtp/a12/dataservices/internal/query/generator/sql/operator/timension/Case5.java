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
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;

public class Case5 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 5: Get employee suggestions of a project for assign employee to purchase order skill and rate";
	}

	@Override protected String getDescription() {
		return """
			SELECT e.* FROM employee e
			WHERE EXISTS(
			    SELECT 1 FROM employee_task_assignment eta
			                 INNER JOIN node_task nt on eta.task_id = nt.node_task_id
			                 INNER JOIN project p ON nt.project_id = p.project_id AND p.project_code IN (%s)
			    WHERE eta.employee_id = e.employee_id
			)
			""".formatted(listAsString(ValueConstants.PROJECT_CODE_LIST));
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.EMPLOYEE_DM)
			.constraint(HasOperator.builder()
				.relationshipModel(RelationshipModelConstants.NODE_TASK_EMPLOYEE_RM)
				.targetRole(RoleConstants.TASK_ROLE)
				.constraint(HasOperator.builder()
					.relationshipModel(RelationshipModelConstants.PROJECT_NODE_TASK_RM)
					.targetRole(RoleConstants.PROJECT_ROLE)
					.constraint(makeProjectsConstraint(ValueConstants.PROJECT_CODE_LIST, GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_CODE_FIELD))
					.build())
				.build())
			.build();
	}
}
