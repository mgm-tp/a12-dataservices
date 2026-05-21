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

public class Case8 extends QueryTestSample {

	@Override protected String getTitle() {
		return "Case 8: Get list teams ( include children ) for All project time report";
	}

	@Override protected String getDescription() {
		return """
			WITH RECURSIVE teams AS (
			    SELECT tc.* FROM team_category tc WHERE tc.team_category_name = '%s'
			    UNION ALL
			    SELECT child.* FROM team_category child
			                 INNER JOIN teams ON child.parent_id = teams.team_category_id
			) SELECT * FROM teams
			""".formatted(ValueConstants.TEAM_CATEGORY);
	}

	@Override protected QueryRoot getQuery() {
		return QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.TEAM_CATEGORY_DM)
			.constraint(ExactMatchOperator.builder()
				.field(GroupConstants.TEAM_CATEGORY_GROUP + FieldConstants.TEAM_CATEGORY_NAME_FIELD)
				.value(ValueConstants.TEAM_CATEGORY)
				.build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModelConstants.TEAM_TEAM_RM)
				.ordered(true)
				.build())
			.build();
	}
}
