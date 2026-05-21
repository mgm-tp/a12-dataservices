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
import com.mgmtp.a12.dataservices.query.fields.aggregation.AggregationProjector;
import com.mgmtp.a12.dataservices.query.fields.ProjectionField;
import com.mgmtp.a12.dataservices.query.fields.aggregation.function.Count;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;

import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.DocumentModelConstants.EMPLOYEE_DM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.ID_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.LEVEL_NAME_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.FieldConstants.SORT_ORDER_FIELD;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.GroupConstants.TEAM_CATEGORY_GROUP;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RelationshipModelConstants.TEAM_CATEGORY_EMPLOYEE_RM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RelationshipModelConstants.TEAM_TEAM_RM;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.CHILD_ROLE;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.RoleConstants.TEAM_ROLE;
import static com.mgmtp.a12.dataservices.internal.query.generator.sql.operator.timension.QueryTestSample.ValueConstants.EMPLOYEE_DOC_ID;

public class AggregationSample extends QueryTestSample {

	@Override protected String getTitle() {
		return "Aggregation example, not related to original queries";
	}

	@Override protected String getDescription() {
		return null;
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot
			.builder()
			.targetDocumentModel(EMPLOYEE_DM)
			.constraint(
				ExactMatchOperator
					.builder()
					.field(ID_FIELD)
					.value(EMPLOYEE_DOC_ID)
					.build())
			.link(QueryLink.builder()
				.aggregation(AggregationProjector.builder()
					.aggregation(Count.builder()
						.alias(TEAM_CATEGORY_GROUP + SORT_ORDER_FIELD)
						.field(TEAM_CATEGORY_GROUP + SORT_ORDER_FIELD)
						.build())
					.groupingField(ProjectionField.builder()
						.alias(TEAM_CATEGORY_GROUP + LEVEL_NAME_FIELD)
						.field(TEAM_CATEGORY_GROUP + LEVEL_NAME_FIELD)
						.build())
					.build())
				.relationshipModel(TEAM_CATEGORY_EMPLOYEE_RM)
				.targetRole(TEAM_ROLE)
				.ordered(true)
				.link(QueryLink.builder()
					.relationshipModel(TEAM_TEAM_RM)
					.targetRole(CHILD_ROLE)
					.backReference("2nd level")
					.ordered(true)
					.build())
				.backReference("1st level")
				.build())
			.link(QueryLink.builder()
				.field(TEAM_CATEGORY_GROUP + LEVEL_NAME_FIELD)
				.relationshipModel(TEAM_CATEGORY_EMPLOYEE_RM)
				.targetRole(TEAM_ROLE)
				.ordered(true)
				.link(QueryLink.builder()
					.aggregation(AggregationProjector.builder()
						.docRef("AggregatedTeamsOfEmployee")
						.aggregation(Count.builder()
							.alias(TEAM_CATEGORY_GROUP + SORT_ORDER_FIELD)
							.field(TEAM_CATEGORY_GROUP + SORT_ORDER_FIELD)
							.build())
						.groupingField(ProjectionField.builder()
							.alias(TEAM_CATEGORY_GROUP + LEVEL_NAME_FIELD)
							.field(TEAM_CATEGORY_GROUP + LEVEL_NAME_FIELD)
							.build())
						.build())
					.relationshipModel(TEAM_TEAM_RM)
					.targetRole(CHILD_ROLE)
					.backReference("2nd level agg")
					.ordered(true)
					.build())
				.backReference("1st level agg")
				.build())
			.build();
	}
}
