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

import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

public class Case1 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 1: Available task assignments of a person on a day";
	}

	@Override protected String getDescription() {
		return """
			SELECT p.project_name, p.cost_unit, nt.path_name, nt.cost_unit
			FROM employee_task_assignment eta
			    INNER JOIN node_task nt ON nt.node_task_id = eta.task_id
			    INNER JOIN project p ON nt.project_id = p.project_id
			WHERE eta.start_end_date @> date('%2$s') AND eta.employee_id = '%1$s'
			ORDER BY p.project_name, nt.path_name
			""".formatted(ValueConstants.EMPLOYEE_ID, ValueConstants.DAY_DATE);
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.field(GroupConstants.NODE_TASK_GROUP + FieldConstants.PATH_NAME_FIELD)
			.field(GroupConstants.NODE_TASK_GROUP + FieldConstants.COST_UNIT_FIELD)
			.targetDocumentModel(DocumentModelConstants.NODE_TASK_DM)
			.constraint(HasOperator.builder()
				.relationshipModel(RelationshipModelConstants.NODE_TASK_EMPLOYEE_RM)
				.targetRole(RoleConstants.EMPLOYEE_ROLE)
				.constraint(ExactMatchOperator.builder()
					.field(FieldConstants.ID_FIELD)
					.value(ValueConstants.EMPLOYEE_DOC_ID)
					.build())
				.linkDocumentConstraint(DateRangeOperator.builder()
					.field(GroupConstants.EMPLOYEE_TASK_ASSIGNMENT_GROUP + FieldConstants.START_END_DATE_FIELD)
					.reverse(true)
					.rangeType(true)
					.value(ValueConstants.DAY_DATE)
					.build())
				.build())
			.link(QueryLink.builder()
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_NAME_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.COST_UNIT_FIELD)
				.relationshipModel(RelationshipModelConstants.PROJECT_NODE_TASK_RM)
				.targetRole(RoleConstants.PROJECT_ROLE)
				.ordered(true)
				.build())
			.build();
	}
}
