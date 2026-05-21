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
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

public class Case3 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 3: Team assignments";
	}

	@Override protected String getDescription() {
		return """
			WITH RECURSIVE team_rel AS (
			    SELECT etr.employee_team_rel_id, etr.parent_id, etr.percentage, tc.level_name, tc.team_category_name
			    FROM employee_team_rel etr
			        INNER JOIN team_category tc ON etr.team_category_id = tc.team_category_id
			    WHERE etr.core_assign_flag AND etr.employee_id = '%1$s' AND etr.start_end_date && daterange('%2$s', '%3$s', '[]')
			    UNION ALL
			    SELECT etr2.employee_team_rel_id, etr2.parent_id, etr2.percentage, tc2.level_name, tc2.team_category_name
			    FROM employee_team_rel etr2
			        INNER JOIN team_rel tr ON tr.parent_id = etr2.employee_team_rel_id
			        INNER JOIN team_category tc2 ON etr2.team_category_id = tc2.team_category_id
			)
			SELECT DISTINCT tr2.team_category_name, tr2.percentage
			FROM team_rel tr2
			WHERE tr2.parent_id ISNULL
			ORDER BY tr2.team_category_name
			""".formatted(ValueConstants.EMPLOYEE_ID, ValueConstants.WEEK_START, ValueConstants.WEEK_END);
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.EMPLOYEE_DM)
			.constraint(
				ExactMatchOperator.builder()
					.field(FieldConstants.ID_FIELD)
					.value(ValueConstants.EMPLOYEE_DOC_ID)
					.build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModelConstants.TEAM_CATEGORY_EMPLOYEE_RM)
				.targetRole(RoleConstants.TEAM_ROLE)
				.ordered(true)
				.linkDocumentConstraint(DateRangeOperator.builder()
					.field(GroupConstants.EMPLOYEE_TEAM_REL_GROUP + FieldConstants.START_END_DATE_FIELD)
					.from(ValueConstants.WEEK_START)
					.to(ValueConstants.WEEK_END)
					.build())
				.link(QueryLink.builder()
					.relationshipModel(RelationshipModelConstants.TEAM_TEAM_RM)
					.targetRole(RoleConstants.CHILD_ROLE)
					.ordered(true)
					.build())
				.build())
			.build();
	}
}
