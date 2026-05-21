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
package com.mgmtp.a12.dataservices.query.constraint;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.internal.UnknownOperator;

/**
 * Defines a logic operator that serves as a base interface for all query operators.
 * Operators implementing this interface should define behavior or constraints
 * for querying data.
 *
 * This interface is annotated with {@link QueryOperator} to specify the name
 * of the operator in queries. If no specific operator is recognized during
 * deserialization, the associated {@link UnknownOperator} implementation is used.
 *
 * This interface ensures consistent handling of logical operators within a
 * query processing framework and supports serialization/deserialization
 * via Jackson annotations.
 */
@DocumentationDiagram
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "operator", defaultImpl = UnknownOperator.class)
@JsonClassDescription("The base type of all operators and constraints.")
public interface ILogicOperator {

	/**
	 * Returns the operator identifier defined by the {@link QueryOperator} annotation
	 * on the implementing class.
	 *
	 * The returned value is used when serializing and processing queries that involve this operator.
	 *
	 * @return the logical operator name declared via {@link QueryOperator}.
	 */
	@JsonIgnore default String getOperator() {
		return this.getClass().getAnnotation(QueryOperator.class).value();
	}
}
