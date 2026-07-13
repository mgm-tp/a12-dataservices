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
package com.mgmtp.a12.dataservices.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.VariadicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

/**
 * Fluent assertion builder for verifying enriched QueryRoot constraints.
 *
 * Example usage:
 * assertThat(enrichedRoot)
 *     .hasAndConstraint()
 *     .withOperandCount(2)
 *     .containsNotOperator(FieldPaths.CONTRACT_TYPE, "Travel")
 *     .containsRangeOperator(FieldPaths.CONTRACT_VALUE, null, 100000.0);
 */
public class QueryAssertions {

	private final QueryRoot enrichedRoot;
	private AndOperator currentAndOperator;

	/**
	 * Creates a new QueryAssertions instance for the given QueryRoot.
	 *
	 * @param enrichedRoot the QueryRoot to perform assertions on
	 */
	public QueryAssertions(QueryRoot enrichedRoot) {
		this.enrichedRoot = enrichedRoot;
	}

	/**
	 * Entry point for fluent assertion API on QueryRoot objects.
	 *
	 * @param root the QueryRoot to assert on, must not be `null`
	 * @return a new QueryAssertions instance for chaining assertions
	 * @throws AssertionError if root is `null`
	 */
	public static QueryAssertions assertThat(QueryRoot root) {
		assertNotNull(root, "QueryRoot should not be null");
		return new QueryAssertions(root);
	}

	/**
	 * Asserts that the QueryRoot has the expected target document model.
	 *
	 * @param expectedModel the expected target document model name
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if the target model does not match
	 */
	public QueryAssertions hasTargetModel(String expectedModel) {
		assertEquals(enrichedRoot.getTargetDocumentModel(), expectedModel,
				"Expected target model [%s] but got [%s]".formatted(expectedModel, enrichedRoot.getTargetDocumentModel()));
		return this;
	}

	/**
	 * Asserts that the QueryRoot has a non-null constraint.
	 *
	 * This is typically used to verify that authorization constraints have been added.
	 *
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if the constraint is `null`
	 */
	public QueryAssertions hasConstraint() {
		assertNotNull(enrichedRoot.getConstraint(), "ABAC should add authorization constraints");
		return this;
	}

	/**
	 * Asserts that the QueryRoot's constraint is an AndOperator and sets it as the current context for further assertions.
	 *
	 * This method must be called before using `withOperandCount()`, `containsNotOperator()`, `containsRangeOperator()`, or `containsExactMatch()`.
	 *
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if the constraint is `null` or not an AndOperator
	 */
	public QueryAssertions hasAndConstraint() {
		hasConstraint();
		assertEquals(enrichedRoot.getConstraint().getClass(), AndOperator.class,
				"Expected AndOperator but got [%s]".formatted(enrichedRoot.getConstraint().getClass().getSimpleName()));
		currentAndOperator = (AndOperator) enrichedRoot.getConstraint();
		return this;
	}

	/**
	 * Asserts that the current AndOperator has the expected number of operands.
	 *
	 * Requires `hasAndConstraint()` to be called first.
	 *
	 * @param expectedCount the expected number of operands in the AndOperator
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if `hasAndConstraint()` was not called first or if the operand count does not match
	 */
	public QueryAssertions withOperandCount(int expectedCount) {
		assertNotNull(currentAndOperator, "Call hasAndConstraint() first");
		assertEquals(currentAndOperator.getOperands().size(), expectedCount,
				"Expected [%d] operands but got [%d]".formatted(expectedCount, currentAndOperator.getOperands().size()));
		return this;
	}

	/**
	 * Asserts that the current AndOperator contains a NotOperator wrapping an ExactMatchOperator with the specified field and value.
	 *
	 * Requires `hasAndConstraint()` to be called first.
	 *
	 * @param field the expected field name
	 * @param value the expected field value
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if `hasAndConstraint()` was not called first, NotOperator is not found, or field/value do not match
	 */
	public QueryAssertions containsNotOperator(String field, String value) {
		assertNotNull(currentAndOperator, "Call hasAndConstraint() first");
		NotOperator notOp = findOperatorOfType(currentAndOperator, NotOperator.class);
		assertExactMatchOperator(notOp.getOperand(), field, value);
		return this;
	}

	/**
	 * Asserts that the current AndOperator contains a DoubleRangeOperator with the specified field and range bounds.
	 *
	 * Requires `hasAndConstraint()` to be called first. Use `null` for `from` or `to` to skip validation of that bound.
	 *
	 * @param field the expected field name
	 * @param from the expected lower bound, or `null` to skip validation
	 * @param to the expected upper bound, or `null` to skip validation
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if `hasAndConstraint()` was not called first, DoubleRangeOperator is not found, or field/bounds do not match
	 */
	public QueryAssertions containsRangeOperator(String field, Double from, Double to) {
		assertNotNull(currentAndOperator, "Call hasAndConstraint() first");
		DoubleRangeOperator rangeOp = findOperatorOfType(currentAndOperator, DoubleRangeOperator.class);
		assertRangeOperator(rangeOp, field, from, to);
		return this;
	}

	/**
	 * Asserts that the current AndOperator contains an ExactMatchOperator with the specified field and value.
	 *
	 * Requires `hasAndConstraint()` to be called first.
	 *
	 * @param field the expected field name
	 * @param value the expected field value
	 * @return this QueryAssertions instance for method chaining
	 * @throws AssertionError if `hasAndConstraint()` was not called first, ExactMatchOperator is not found, or field/value do not match
	 */
	public QueryAssertions containsExactMatch(String field, String value) {
		assertNotNull(currentAndOperator, "Call hasAndConstraint() first");
		ExactMatchOperator<?> exactMatch = findOperatorOfType(currentAndOperator, ExactMatchOperator.class);
		assertExactMatchOperator(exactMatch, field, value);
		return this;
	}

	/**
	 * Finds the first operator of the specified type within the parent's operands.
	 *
	 * @param parent the parent VariadicOperator to search within
	 * @param operatorClass the class of the operator to find
	 * @param <T> the type of operator to find, must extend ILogicOperator
	 * @return the first matching operator
	 * @throws AssertionError if no operator of the specified type is found
	 */
	private <T extends ILogicOperator> T findOperatorOfType(VariadicOperator parent, Class<T> operatorClass) {
		return parent.getOperands().stream()
				.filter(operatorClass::isInstance)
				.map(operatorClass::cast)
				.findFirst()
				.orElseThrow(() -> new AssertionError(
						"Expected " + operatorClass.getSimpleName() + " in " + parent.getClass().getSimpleName()));
	}

	/**
	 * Asserts that the given operator is an ExactMatchOperator with the specified field and value.
	 *
	 * @param operator the operator to verify
	 * @param field the expected field name
	 * @param value the expected field value
	 * @throws AssertionError if the operator is not an ExactMatchOperator or field/value do not match
	 */
	private void assertExactMatchOperator(ILogicOperator operator, String field, String value) {
		assertEquals(operator.getClass(), ExactMatchOperator.class);
		ExactMatchOperator<?> exactMatch = (ExactMatchOperator<?>) operator;
		assertEquals(exactMatch.getField(), field);
		assertEquals(exactMatch.getValue(), value);
	}

	/**
	 * Asserts that the given DoubleRangeOperator has the specified field and range bounds.
	 *
	 * @param operator the DoubleRangeOperator to verify
	 * @param field the expected field name
	 * @param from the expected lower bound, or `null` to skip validation
	 * @param to the expected upper bound, or `null` to skip validation
	 * @throws AssertionError if the field or bounds do not match
	 */
	private void assertRangeOperator(DoubleRangeOperator operator, String field, Double from, Double to) {
		assertEquals(operator.getField(), field);
		if (from != null) {
			assertEquals(operator.getFrom(), from);
		}
		if (to != null) {
			assertEquals(operator.getTo(), to);
		}
	}
}
