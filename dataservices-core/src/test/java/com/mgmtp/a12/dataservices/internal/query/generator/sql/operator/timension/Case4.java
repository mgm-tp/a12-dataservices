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
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.UndefinedMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

import java.time.LocalDate;

public class Case4 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 4: Filter: Name, Type, Status";
	}

	@Override protected String getDescription() {
		return """
			WITH RECURSIVE projet AS (
			    SELECT p1.project_id AS parent, p1.project_id, p1.parent_id, p1.project_name, p1.project_type, p1.project_status, p1.cost_unit, p1.start_date, p1.end_date, p1.project_code
			    FROM project p1
			    WHERE p1.parent_id ISNULL-- AND (p1.end_date IS NULL OR p1.end_date > current_date)
			    UNION ALL
			    SELECT p.parent, p2.project_id, p2.parent_id, p2.project_name, p2.project_type, p2.project_status, p2.cost_unit, p2.start_date, p2.end_date, p2.project_code
			    FROM project p2
			             INNER JOIN projet p ON p2.parent_id = p.project_id
			    WHERE p2.end_date IS NULL OR p2.end_date > current_date
			), parent_p AS (
			    SELECT DISTINCT p.parent_id FROM projet p WHERE p.parent_id NOTNULL
			), projet_filter AS (
			    SELECT p.*
			    FROM projet p
			    WHERE ('ALL' IN (%1$s) OR p.project_id IN (%1$s))
			        AND ('ALL' IN (%2$s) OR p.project_type IN (%2$s))
			        AND ('ALL' IN (%3$s) OR p.project_status IN (%3$s))
			), projet_po AS (
			    SELECT pf.project_id, array_agg(po.purchase_order_id)
			    FROM purchase_order po
			             INNER JOIN projet_filter pf ON po.project_id = pf.project_id
			    WHERE po.status IN ('IN_PROGRESS', 'SUBMITTED') AND NOT EXISTS(SELECT 1 FROM parent_p pp WHERE pp.parent_id = po.project_id)
			    GROUP BY pf.project_id
			)
			SELECT *
			FROM projet p
			     LEFT JOIN projet_po pp ON p.project_id = pp.project_id
			WHERE EXISTS(
			    SELECT 1
			    FROM projet_filter pf
			    WHERE p.project_id = pf.project_id OR p.project_id = pf.parent_id
			)
			ORDER BY p.parent, p.project_id
			""".formatted(listAsString(ValueConstants.PROJECT_ID_LIST), listAsString(ValueConstants.PROJECT_TYPE_LIST), listAsString(
			ValueConstants.PROJECT_STATUS_LIST));
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.exclude(true)
			.targetDocumentModel(DocumentModelConstants.PURCHASE_ORDER_DM)
			.constraint(OrOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field(GroupConstants.PURCHASE_ORDER_GROUP + FieldConstants.STATUS_FIELD)
					.value(ValueConstants.STATUS_IN_PROGRESS)
					.build())
				.operand(ExactMatchOperator.builder()
					.field(GroupConstants.PURCHASE_ORDER_GROUP + FieldConstants.STATUS_FIELD)
					.value(ValueConstants.STATUS_SUBMITTED)
					.build())
				.build())
			.link(QueryLink.builder()
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_NAME_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_TYPE_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_STATUS_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.COST_UNIT_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.START_DATE_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.END_DATE_FIELD)
				.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_ID_FIELD)
				.relationshipModel(RelationshipModelConstants.PROJECT_PURCHASE_ORDER_RM)
				.targetRole(RoleConstants.PROJECT_ROLE)
				.ordered(true)
				.constraint(AndOperator.builder()
					.operand(OrOperator.builder()
						.operand(DateRangeOperator.builder()
							.field(GroupConstants.PROJECT_GROUP + FieldConstants.END_DATE_FIELD)
							.from(LocalDate.now().toString())
							.build())
						.operand(UndefinedMatchOperator.builder()
							.field(GroupConstants.PROJECT_GROUP + FieldConstants.END_DATE_FIELD)
							.build())
						.build())
					.operand(OrOperator.builder()
						.operand(makeProjectsConstraint(ValueConstants.PROJECT_ID_LIST, GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_ID_FIELD))
						.build())
					.build())
				.link(QueryLink.builder()
					.field(GroupConstants.PURCHASE_ORDER_GROUP + FieldConstants.ORDER_ID_FIELD)
					.relationshipModel(RelationshipModelConstants.PROJECT_PURCHASE_ORDER_RM)
					.targetRole(RoleConstants.PURCHASE_ORDER_ROLE)
					.ordered(true)
					.constraint(OrOperator.builder()
						.operand(ExactMatchOperator.builder()
							.field(GroupConstants.PURCHASE_ORDER_GROUP + FieldConstants.STATUS_FIELD)
							.value(ValueConstants.STATUS_IN_PROGRESS)
							.build())
						.operand(ExactMatchOperator.builder()
							.field(GroupConstants.PURCHASE_ORDER_GROUP + FieldConstants.STATUS_FIELD)
							.value(ValueConstants.STATUS_SUBMITTED)
							.build())
						.build())
					.build())
				.link(QueryLink.builder()
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_NAME_FIELD)
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_TYPE_FIELD)
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_STATUS_FIELD)
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.COST_UNIT_FIELD)
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.START_DATE_FIELD)
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.END_DATE_FIELD)
					.field(GroupConstants.PROJECT_GROUP + FieldConstants.PROJECT_ID_FIELD)
					.relationshipModel(RelationshipModelConstants.PROJECT_PROJECT_RM)
					.targetRole(RoleConstants.PARENT_ROLE)
					.ordered(true)
					.build())
				.build())
			.build();
	}
}
