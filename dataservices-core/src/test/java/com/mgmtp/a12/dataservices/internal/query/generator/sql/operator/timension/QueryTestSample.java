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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;

import lombok.Data;
import lombok.SneakyThrows;

@Data
public abstract class QueryTestSample {

	@SneakyThrows
	public static <T extends QueryTestSample> Object[] getTestSample(Class<T> clzz) {
		T o = clzz.getDeclaredConstructor().newInstance();
		return new Object[] { o.getTitle(), o.getDescription(), o.getQuery() };
	}

	protected static String listAsString(Collection<String> projectIdList) {
		return projectIdList.stream().map("'%s'"::formatted).collect(Collectors.joining(","));
	}

	protected static ILogicOperator makeProjectsConstraint(Collection<String> projectIdList, String f) {
		OrOperator.OrOperatorBuilder<?, ?> b = OrOperator.builder();
		projectIdList.forEach(p -> b.operand(ExactMatchOperator.builder()
			.field(f)
			.value(p)
			.build()));
		return b.build();
	}

	protected abstract String getTitle();

	protected abstract String getDescription();

	protected abstract QueryRoot getQuery();

	public interface ValueConstants {

		Collection<String> PROJECT_CODE_LIST = List.of("XQUU-2", "XSHR-91", "WIQZ-531", "PIQS-513");
		Collection<String> PROJECT_ID_LIST = List.of("PR-4I3TLKME60V7", "PR-FIZTZXOKU6HY", "PR-S3A9EUGYR5M1");
		Collection<String> PROJECT_STATUS_LIST = List.of("ON_HOLD", "ACTIVE", "ARCHIVED", "FINISHED");
		Collection<String> PROJECT_TYPE_LIST = List.of("FOLDER", "PROJECT");
		String DAY_DATE = "2024-01-05";
		String EMPLOYEE_DOC_ID = "1863328";
		String EMPLOYEE_ID = "EM-TC70W1TS53478YYUQ";
		String STATUS_IN_PROGRESS = "IN_PROGRESS";
		String STATUS_SUBMITTED = "SUBMITTED";
		String TEAM_CATEGORY = "Data Services";
		String WEEK_END = "2023-12-17";
		String WEEK_START = "2023-12-11";
	}

	public interface DocumentModelConstants {

		String EMPLOYEE_DM = "Employee";
		String EMPLOYEE_TEAM_REL_DM = "EmployeeTeamRel";
		String NODE_TASK_DM = "NodeTask";
		String PERSONAL_VACATION_DM = "PersonalVacation";
		String PROJECT_DM = "Project";
		String PURCHASE_ORDER_DM = "PurchaseOrder";
		String TASK_TIME_RECORDING_DM = "TaskTimeRecording";
		String TEAM_CATEGORY_DM = "TeamCategory";
		String WORKING_DAY_RECORDING_DM = "WorkingDayRecording";
		String JIRA_TICKET_DM = "JiraTicket";
	}

	public interface RelationshipModelConstants {

		String EMPLOYEE_PERSONAL_VACATION_RM = "EmployeePersonalVacation";
		String EMPLOYEE_WORKING_DAY_RECORDING_RM = "EmployeeWorkingDayRecording";
		String NODE_TASK_EMPLOYEE_RM = "NodeTaskEmployee";
		String PROJECT_NODE_TASK_RM = "ProjectNodeTask";
		String PROJECT_PROJECT_RM = "ProjectProject";
		String PROJECT_PURCHASE_ORDER_RM = "ProjectPurchaseOrder";
		String TEAM_CATEGORY_EMPLOYEE_RM = "TeamCategoryEmployee";
		String TEAM_TEAM_RM = "TeamTeam";
		String WORKING_DAY_RECORDING_TASK_TIME_RECORDING_RM = "WorkingDayRecordingTaskTimeRecording";
		String TASK_TIME_RECORDING_JIRA_TICKET_RM = "TaskTimeRecordingJiraTicket";
		String NODE_TASK_TASK_TIME_RECORDING_RM = "NodeTaskTaskTimeRecording";
		String TEAM_CATEGORY_PROJECT_RM = "TeamCategoryProject";
	}

	public interface RoleConstants {

		String BOOKING_ROLE = "booking";
		String CHILD_ROLE = "child";
		String EMPLOYEE_ROLE = "employee";
		String NODE_TASK_ROLE = "nodeTask";
		String PARENT_ROLE = "parent";
		String PERSONAL_VACATION_ROLE = "personalVacation";
		String PROJECT_ROLE = "project";
		String PURCHASE_ORDER_ROLE = "purchaseOrder";
		String TASK_ROLE = "task";
		String TEAM_ROLE = "team";
		String WORKING_DAY_RECORDING_ROLE = "workingDayRecording";
		String JIRA_TICKET_ROLE = "jiraTicket";
		String TASK_TIME_RECORDING_ROLE = "taskTimeRecording";
	}

	public interface FieldConstants {

		String ABSENCE_DATE_FIELD = "/absenceDate";
		String ABSENCE_TYPE_FIELD = "/absenceType";
		String BREAK_IN_MINUTE_FIELD = "/breakInMinute";
		String COST_UNIT_FIELD = "/costUnit";
		String DOC_REF_FIELD = "docRef";
		String DURATION_FIELD = "/duration";
		String END_DATE_FIELD = "/endDate";
		String END_TIME_FIELD = "/endTime";
		String ID_FIELD = "id";
		String LEVEL_NAME_FIELD = "/levelName";
		String ORDER_ID_FIELD = "/orderId";
		String PATH_NAME_FIELD = "/pathName";
		String PROJECT_CODE_FIELD = "/projectCode";
		String PROJECT_ID_FIELD = "/projectId";
		String PROJECT_NAME_FIELD = "/projectName";
		String PROJECT_STATUS_FIELD = "/projectStatus";
		String PROJECT_TYPE_FIELD = "/projectType";
		String SORT_ORDER_FIELD = "/sortOrder";
		String START_DATE_FIELD = "/startDate";
		String START_END_DATE_FIELD = "/startEndDate";
		String START_TIME_FIELD = "/startTime";
		String STATUS_FIELD = "/status";
		String WORKING_DAY_FIELD = "/workingDay";
		String TEAM_CATEGORY_NAME_FIELD = "/teamCategoryName";
		String TRIGGER_FIELD = "/trigger";
		String JIRA_TICKET_ID_FIELD = "/jiraTicketId";
		String JOIN_LEAVE_DATE_FIELD = "/joinLeaveDate";
		String FIRST_NAME_FIELD = "/firstName";
		String MIDDLE_NAME_FIELD = "/middleName";
		String LAST_NAME_FIELD = "/lastName";
	}

	public interface GroupConstants {

		String EMPLOYEE_TASK_ASSIGNMENT_GROUP = "/employeeTaskAssignment";
		String EMPLOYEE_TEAM_REL_GROUP = "/employeeTeamRel";
		String NODE_TASK_GROUP = "/nodeTask";
		String PERSONAL_VACATION_GROUP = "/personalVacation";
		String PROJECT_GROUP = "/project";
		String PURCHASE_ORDER_GROUP = "/purchaseOrder";
		String TASK_TIME_RECORDING_GROUP = "/taskTimeRecording";
		String TEAM_CATEGORY_GROUP = "/teamCategory";
		String WORKING_DAY_RECORDING_GROUP = "/workingDayRecording";
		String JIRA_TICKET_GROUP = "/jiraTicket";
		String EMPLOYEE_GROUP = "/employee";
	}
}
