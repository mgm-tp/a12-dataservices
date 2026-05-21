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
package com.mgmtp.a12.dataservices.query.constraint.matching;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.ConstraintAware;
import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Operator for querying linked documents.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data @NoArgsConstructor @SuperBuilder @DocumentationDiagram
@QueryOperator("has") public class HasOperator implements ILogicOperator, LinkAware, ConstraintAware {

	/**
	 * Name of the relationship model.
	 */
	private String relationshipModel;
	/**
	 * Target document role.
	 */
	private String targetRole;

	/**
	 * Constraint applied on target document.
	 */
	private ILogicOperator constraint;
	/**
	 * Constraint applied on link document.
	 */
	private ILogicOperator linkDocumentConstraint;
	/**
	 * Maximum depth of nesting.
	 */
	private Integer maxDepth;

	/**
	 * True, if custom ordering of links in enabled.
	 */
	private Boolean ordered;

	@Override public boolean isAggregated() {
		return false;
	}
}
