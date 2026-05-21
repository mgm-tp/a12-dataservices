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
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

public class Case10 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 10: Get project and employee related for working day report";
	}

	@Override protected String getDescription() {
		return """
			SELECT DISTINCT p.project_id, e.employee_id, concat_ws(' ', e.first_name, e.middle_name, e.last_name) as display_name
			FROM project p
			         INNER JOIN team_category tc ON tc.team_category_id = p.team_category_id
			         INNER JOIN employee_team_rel etr ON tc.team_category_id = etr.team_category_id
			         INNER JOIN employee e ON etr.employee_id = e.employee_id
			WHERE project_code IN (%s) AND e.join_leave_date @> date('%s')
			""".formatted(listAsString(ValueConstants.PROJECT_CODE_LIST), ValueConstants.DAY_DATE);
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.field("docRef")
			.targetDocumentModel(DocumentModelConstants.PROJECT_DM)
			.constraint(AndOperator.builder()
				.operand(makeProjectsConstraint(ValueConstants.PROJECT_CODE_LIST, GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_CODE_FIELD))
				.operand(HasOperator.builder()
					.relationshipModel(RelationshipModelConstants.TEAM_CATEGORY_PROJECT_RM)
					.targetRole(RoleConstants.TEAM_ROLE)
					.constraint(HasOperator.builder()
						.relationshipModel(RelationshipModelConstants.TEAM_CATEGORY_EMPLOYEE_RM)
						.targetRole(RoleConstants.EMPLOYEE_ROLE)
						.constraint(DateRangeOperator.builder()
							.reverse(true)
							.rangeType(true)
							.field(GroupConstants.EMPLOYEE_GROUP + FieldConstants.JOIN_LEAVE_DATE_FIELD)
							.value(ValueConstants.DAY_DATE)
							.build())
						.build())
					.build())
				.build())
			.link(QueryLink.builder()
				.exclude(true)
				.relationshipModel(RelationshipModelConstants.TEAM_CATEGORY_PROJECT_RM)
				.targetRole(RoleConstants.TEAM_ROLE)
				.ordered(true)
				.link(QueryLink.builder()
					.field("docRef")
					.field(GroupConstants.EMPLOYEE_GROUP + FieldConstants.FIRST_NAME_FIELD)
					.field(GroupConstants.EMPLOYEE_GROUP + FieldConstants.MIDDLE_NAME_FIELD)
					.field(GroupConstants.EMPLOYEE_GROUP + FieldConstants.LAST_NAME_FIELD)
					.relationshipModel(RelationshipModelConstants.TEAM_CATEGORY_EMPLOYEE_RM)
					.targetRole(RoleConstants.EMPLOYEE_ROLE)
					.ordered(true)
					.constraint(DateRangeOperator.builder()
						.reverse(true)
						.rangeType(true)
						.field(GroupConstants.EMPLOYEE_GROUP + FieldConstants.JOIN_LEAVE_DATE_FIELD)
						.value(ValueConstants.DAY_DATE)
						.build())
					.build())
				.build())
			.build();
	}
}
