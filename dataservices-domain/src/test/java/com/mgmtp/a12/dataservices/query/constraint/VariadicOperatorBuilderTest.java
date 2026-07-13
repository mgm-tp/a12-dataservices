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
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for the PECS-compliant builder methods on {@link VariadicOperator}.
 *
 * Verifies that collections of concrete operator subtypes (e.g. `List<ExactMatchOperator>`)
 * can be passed directly to the `operands()` builder method without requiring an explicit upcast.
 */
public class VariadicOperatorBuilderTest {

	@Test(description = "Should accept list of concrete operator subtypes directly")
	public void shouldAcceptConcreteSubtypesList() {
		// Given
		List<ExactMatchOperator<String>> exactMatches = List.of(
			ExactMatchOperator.<String>builder().field("field1").value("value1").build(),
			ExactMatchOperator.<String>builder().field("field2").value("value2").build()
		);

		// When - this compiles without new ArrayList<>() wrapper thanks to PECS
		OrOperator orOperator = OrOperator.builder()
			.operands(exactMatches)
			.build();

		// Then
		assertNotNull(orOperator);
		assertEquals(orOperator.getOperands().size(), 2);
	}

	@Test(description = "Should accept stream result directly for OrOperator")
	public void shouldAcceptStreamResultDirectlyForOrOperator() {
		// Given
		List<String> docRefs = List.of("Model1/123", "Model1/456", "Model1/789");

		// When - stream produces List<ExactMatchOperator>, which is a subtype list
		OrOperator orOperator = OrOperator.builder()
			.operands(docRefs.stream()
				.map(ref -> ExactMatchOperator.builder()
					.field("/docRef")
					.value(ref)
					.build())
				.toList())
			.build();

		// Then
		assertNotNull(orOperator);
		assertEquals(orOperator.getOperands().size(), 3);
	}

	@Test(description = "Should accept stream result directly for AndOperator")
	public void shouldAcceptStreamResultDirectlyForAndOperator() {
		// Given
		List<String> fields = List.of("field1", "field2");

		// When
		AndOperator andOperator = AndOperator.builder()
			.operands(fields.stream()
				.map(field -> ExactMatchOperator.builder()
					.field(field)
					.value("expectedValue")
					.build())
				.toList())
			.build();

		// Then
		assertNotNull(andOperator);
		assertEquals(andOperator.getOperands().size(), 2);
	}

	@Test(description = "Should continue to accept exact type Collection<ILogicOperator>")
	public void shouldContinueToAcceptExactTypeCollection() {
		// Given
		Collection<ILogicOperator> operators = new ArrayList<>();
		operators.add(ExactMatchOperator.builder().field("f1").value("v1").build());
		operators.add(ExactMatchOperator.builder().field("f2").value("v2").build());

		// When - backward compatibility: exact type still works
		OrOperator orOperator = OrOperator.builder()
			.operands(operators)
			.build();

		// Then
		assertNotNull(orOperator);
		assertEquals(orOperator.getOperands().size(), 2);
	}

	@Test(description = "Should continue to accept Set<ILogicOperator>")
	public void shouldContinueToAcceptSetOfILogicOperator() {
		// Given - mirrors usage in DefaultQueryAuthorizationService
		Set<ILogicOperator> operators = Set.of(
			ExactMatchOperator.builder().field("f1").value("v1").build(),
			ExactMatchOperator.builder().field("f2").value("v2").build()
		);

		// When
		AndOperator andOperator = AndOperator.builder()
			.operands(operators)
			.build();

		// Then
		assertNotNull(andOperator);
		assertEquals(andOperator.getOperands().size(), 2);
	}

	@Test(description = "Should accept single operand via convenience method")
	public void shouldAcceptSingleOperandViaConvenienceMethod() {
		// Given
		ExactMatchOperator<String> operand = ExactMatchOperator.<String>builder()
			.field("field1")
			.value("value1")
			.build();

		// When - singular operand() method still works
		OrOperator orOperator = OrOperator.builder()
			.operand(operand)
			.build();

		// Then
		assertNotNull(orOperator);
		assertEquals(orOperator.getOperands().size(), 1);
	}

	@Test(description = "Should create defensive copy of operands collection")
	public void shouldCreateDefensiveCopyOfOperandsCollection() {
		// Given
		List<ILogicOperator> mutableList = new ArrayList<>();
		mutableList.add(ExactMatchOperator.builder().field("f1").value("v1").build());

		// When
		OrOperator orOperator = OrOperator.builder()
			.operands(mutableList)
			.build();

		// Then - modifying original list should not affect the operator
		mutableList.add(ExactMatchOperator.builder().field("f2").value("v2").build());
		assertEquals(orOperator.getOperands().size(), 1);
	}
}
