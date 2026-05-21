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
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.fields.aggregation.function.Sum;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

public class Case2b extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 2, alt B (from Employee): Booking aggregate of week-header";
	}

	@Override protected String getDescription() {
		return """
			WITH aggregated_booking AS (
			    SELECT wdr.working_day, sum(ttr.duration) AS sum
			    FROM task_time_recording ttr
			             INNER JOIN working_day_recording wdr ON ttr.working_day_recording_id = wdr.working_day_recording_id
			    WHERE daterange('%2$s', '%3$s', '[]') @> wdr.working_day AND wdr.employee_id = '%1$s'
			    GROUP BY wdr.working_day
			)
			SELECT wdr.working_day_recording_id, wdr.working_day, wdr.start_time, wdr.end_time, wdr.break_in_minute, pv.absence_type, ab.sum
			FROM working_day_recording wdr
			    INNER JOIN aggregated_booking ab ON wdr.working_day = ab.working_day
			    LEFT JOIN personal_vacation pv ON wdr.employee_id = pv.employee_id AND wdr.working_day = pv.absence_date
			WHERE daterange('%2$s', '%3$s', '[]') @> wdr.working_day AND wdr.employee_id = '%1$s'
			""".formatted(ValueConstants.EMPLOYEE_ID, ValueConstants.WEEK_START, ValueConstants.WEEK_END);
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.field(FieldConstants.DOC_REF_FIELD)
			.targetDocumentModel(DocumentModelConstants.EMPLOYEE_DM)
			.constraint(ExactMatchOperator.builder()
				.field(FieldConstants.ID_FIELD)
				.value(ValueConstants.EMPLOYEE_DOC_ID)
				.build())
			.link(QueryLink.builder()
				.field(GroupConstants.PERSONAL_VACATION_GROUP + FieldConstants.ABSENCE_TYPE_FIELD)
				.field(GroupConstants.PERSONAL_VACATION_GROUP + FieldConstants.ABSENCE_DATE_FIELD)
				.relationshipModel(RelationshipModelConstants.EMPLOYEE_PERSONAL_VACATION_RM)
				.targetRole(RoleConstants.PERSONAL_VACATION_ROLE)
				.ordered(true)
				.constraint(DateRangeOperator.builder()
					.field(GroupConstants.PERSONAL_VACATION_GROUP + FieldConstants.ABSENCE_DATE_FIELD)
					.from(ValueConstants.WEEK_START)
					.to(ValueConstants.WEEK_END)
					.build())
				.build())
			.link(QueryLink.builder()
				.field(FieldConstants.DOC_REF_FIELD)
				.field(GroupConstants.WORKING_DAY_RECORDING_GROUP + FieldConstants.WORKING_DAY_FIELD)
				.field(GroupConstants.WORKING_DAY_RECORDING_GROUP + FieldConstants.START_TIME_FIELD)
				.field(GroupConstants.WORKING_DAY_RECORDING_GROUP + FieldConstants.END_TIME_FIELD)
				.field(GroupConstants.WORKING_DAY_RECORDING_GROUP + FieldConstants.BREAK_IN_MINUTE_FIELD)
				.relationshipModel(RelationshipModelConstants.EMPLOYEE_WORKING_DAY_RECORDING_RM)
				.targetRole(RoleConstants.WORKING_DAY_RECORDING_ROLE)
				.ordered(true)
				.constraint(DateRangeOperator.builder()
					.field(GroupConstants.WORKING_DAY_RECORDING_GROUP + FieldConstants.WORKING_DAY_FIELD)
					.from(ValueConstants.WEEK_START)
					.to(ValueConstants.WEEK_END)
					.build())
				.link(QueryLink.builder()
					.aggregation(AggregationProjector.builder()
						.aggregation(Sum.builder()
							.field(GroupConstants.TASK_TIME_RECORDING_GROUP + FieldConstants.DURATION_FIELD)
							.build())
						.build())
					.relationshipModel(RelationshipModelConstants.WORKING_DAY_RECORDING_TASK_TIME_RECORDING_RM)
					.targetRole(RoleConstants.BOOKING_ROLE)
					.ordered(true)
					.build())
				.build())
			.build();
	}
}
