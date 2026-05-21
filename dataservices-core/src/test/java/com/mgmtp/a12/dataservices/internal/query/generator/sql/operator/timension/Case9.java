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
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;

import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.DocumentModelConstants.TEAM_CATEGORY_DM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.TEAM_CATEGORY_NAME_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.GroupConstants.TEAM_CATEGORY_GROUP;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RelationshipModelConstants.TEAM_TEAM_RM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.CHILD_ROLE;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.PARENT_ROLE;

public class Case9 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 9: Get list cross funtion teams for custom report";
	}

	@Override protected String getDescription() {
		return """
			WITH RECURSIVE parent_team AS
			                   (
			                       SELECT
			                           level1.team_category_id,
			                           level1.parent_id,
			                           cast(level1.team_category_name AS text),
			                           level1.global_assignment
			                       FROM team_category level1
			                                INNER JOIN team_category industry ON level1.parent_id = industry.team_category_id
			                       WHERE industry.parent_id IS NULL
			                       UNION ALL
			                       SELECT
			                           sub_team.team_category_id,
			                           sub_team.parent_id,
			                           parent_team.team_category_name || '/' || sub_team.team_category_name,
			                           sub_team.global_assignment
			                       FROM team_category sub_team INNER JOIN parent_team ON sub_team.parent_id = parent_team.team_category_id
			                   )
			SELECT
			    team_category_id AS teamCategoryId,
			    team_category_name AS teamHierarchyName
			FROM parent_team
			WHERE global_assignment IS TRUE
			""";
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.field(FieldConstants.DOC_REF_FIELD)
			.field(TEAM_CATEGORY_GROUP + TEAM_CATEGORY_NAME_FIELD)
			.targetDocumentModel(TEAM_CATEGORY_DM)
			.constraint(NotOperator.builder()
				.operand(HasOperator.builder()
					.relationshipModel(TEAM_TEAM_RM)
					.targetRole(PARENT_ROLE)
					.build())
				.build())
			.link(QueryLink.builder()
				.relationshipModel(TEAM_TEAM_RM)
				.targetRole(CHILD_ROLE)
				.ordered(true)
				.build())
			.build();
	}
}
