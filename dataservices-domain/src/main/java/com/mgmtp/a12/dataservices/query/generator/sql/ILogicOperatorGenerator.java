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
package com.mgmtp.a12.dataservices.query.generator.sql;

import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;

/**
 * Represents the interface for generating SQL conditions based on a specific logic operator.
 * Implementations of this interface are responsible for providing the SQL rendering logic
 * for various custom logic operators that extend the {@link ILogicOperator} interface.
 *
 * @param <T> the type of logic operator this generator is associated with,
 *           must extend the {@link ILogicOperator} interface.
 */
public interface ILogicOperatorGenerator<T extends ILogicOperator> {

	/**
	 * Constructs and appends an SQL condition to the provided StringBuilder based on the given logic operator
	 * and the current context provided by the QueryGeneratorContext.
	 *
	 * @param sb the StringBuilder to which the SQL condition will be appended
	 * @param operator the logic operator that defines the condition to be rendered
	 * @param queryGeneratorContext the context containing the state and utilities for query generation
	 * @return the updated StringBuilder with the appended SQL condition
	 */
	StringBuilder renderCondition(StringBuilder sb, T operator, QueryGeneratorContext queryGeneratorContext);
}
