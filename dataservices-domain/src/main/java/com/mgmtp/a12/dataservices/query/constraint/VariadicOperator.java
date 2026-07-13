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

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Kind of operator that can accept arbitrary number of arguments.
 */
@Data @NoArgsConstructor @SuperBuilder
public abstract class VariadicOperator implements ILogicOperator {
	@JsonProperty(required = true)
	@JsonPropertyDescription("Operands of the variadic operator. The number of operands is limited by configuration.")
	private Collection<ILogicOperator> operands;

	/**
	 * Builder for variadic operators, providing convenience methods to add operands.
	 *
	 * @param <C> concrete operator type.
	 * @param <B> builder type for fluent chaining.
	 */
	public abstract static class VariadicOperatorBuilder<C extends VariadicOperator, B extends VariadicOperatorBuilder<C, B>> {

		/**
		 * Adds a single operand to this operator being built.
		 *
		 * @param operand operator to add; must not be null.
		 * @return this builder for fluent chaining.
		 */
		public B operand(ILogicOperator operand) {
			if (operands == null) {
				operands = new ArrayList<>();
			}
			operands.add(operand);
			return self();
		}

		/**
		 * Sets the operands for this variadic operator.
		 *
		 * Honors the PECS principle by accepting `Collection<? extends ILogicOperator>`,
		 * allowing collections of concrete operator subtypes (e.g. `List<ExactMatchOperator>`)
		 * to be passed directly without requiring an explicit upcast.
		 *
		 * @param operands collection of operands; a defensive copy is created.
		 * @return this builder for fluent chaining.
		 */
		public B operands(Collection<? extends ILogicOperator> operands) {
			this.operands = new ArrayList<>(operands);
			return self();
		}
	}
}
