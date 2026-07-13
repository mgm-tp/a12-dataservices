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
package com.mgmtp.a12.dataservices.uniqueconstraint;

import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import org.testng.Assert;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

/**
 * Unit tests for {@link CheckUniquenessResult}.
 *
 * Tests are written against the six-field record shape:
 * `CheckUniquenessResult(String modelName, String constraintName, DocumentReference conflictingDocRef,
 * Map<String, String> errorMessage, SequencedCollection<String> fieldFullNames, String errorKey)`.
 */
public class CheckUniquenessResultTest {

	// -------------------------------------------------------------------------
	// shouldHoldAllFields
	// -------------------------------------------------------------------------

	@Test(description = "Should hold all six fields")
	public void shouldHoldAllFields() {
		// Given
		String modelName = "PersonModel";
		String constraintName = "UniquePersonName";
		DocumentReference conflictingDocRef = new DocumentReference("Person/42");
		Map<String, String> errorMessage = Map.of("en", "Name must be unique", "de", "Name muss eindeutig sein");
		SequencedCollection<String> fieldFullNames = List.of("/PersonRoot/firstName", "/PersonRoot/lastName");
		String errorKey = ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY;

		// When
		CheckUniquenessResult result = new CheckUniquenessResult(modelName, constraintName, conflictingDocRef, errorMessage, fieldFullNames, errorKey);

		// Then
		Assert.assertEquals(result.modelName(), modelName);
		Assert.assertEquals(result.constraintName(), constraintName);
		Assert.assertEquals(result.conflictingDocRef(), conflictingDocRef);
		Assert.assertEquals(result.errorMessage(), errorMessage);
		Assert.assertEquals(result.fieldFullNames(), fieldFullNames);
		Assert.assertEquals(result.errorKey(), ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);
	}

	// -------------------------------------------------------------------------
	// shouldBeEqualWhenValuesMatch
	// -------------------------------------------------------------------------

	@Test(description = "Should be equal to another CheckUniquenessResult with same values")
	public void shouldBeEqualWhenValuesMatch() {
		// Given
		Map<String, String> errorMessage = Map.of("en", "Name must be unique");
		SequencedCollection<String> fieldFullNames = List.of("/PersonRoot/firstName");

		CheckUniquenessResult result1 = new CheckUniquenessResult(
			"PersonModel", "UniquePersonName", new DocumentReference("Person/42"), errorMessage, fieldFullNames,
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);
		CheckUniquenessResult result2 = new CheckUniquenessResult(
			"PersonModel", "UniquePersonName", new DocumentReference("Person/42"), errorMessage, fieldFullNames,
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);

		// Then
		Assert.assertEquals(result1, result2);
		Assert.assertEquals(result1.hashCode(), result2.hashCode());
	}

	// -------------------------------------------------------------------------
	// shouldExposeConstraintName
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should expose constraintName via accessor")
	public void shouldExposeConstraintName() {
		// Given
		CheckUniquenessResult result = new CheckUniquenessResult(
			"PersonModel", "UniqueEmail", new DocumentReference("Person/7"), Map.of(), List.of("/PersonRoot/email"),
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);

		// Then
		Assert.assertEquals(result.constraintName(), "UniqueEmail");
	}

	// -------------------------------------------------------------------------
	// shouldExposeConflictingDocRef
	// -------------------------------------------------------------------------

	@Test(description = "Should expose conflictingDocRef via accessor")
	public void shouldExposeConflictingDocRef() {
		// Given
		CheckUniquenessResult result = new CheckUniquenessResult(
			"PersonModel", "UniqueEmail", new DocumentReference("Person/7"), Map.of(), List.of("/PersonRoot/email"),
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);

		// Then
		Assert.assertEquals(result.conflictingDocRef().toString(), "Person/7");
	}

	// -------------------------------------------------------------------------
	// shouldExposeErrorMessage
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should expose errorMessage map via accessor")
	public void shouldExposeErrorMessage() {
		// Given
		Map<String, String> errorMessage = Map.of("en", "Email must be unique", "de", "E-Mail muss eindeutig sein");
		CheckUniquenessResult result = new CheckUniquenessResult(
			"PersonModel", "UniqueEmail", new DocumentReference("Person/7"), errorMessage, List.of("/PersonRoot/email"),
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);

		// Then
		Assert.assertEquals(result.errorMessage(), errorMessage);
		Assert.assertEquals(result.errorMessage().get("en"), "Email must be unique");
		Assert.assertEquals(result.errorMessage().get("de"), "E-Mail muss eindeutig sein");
	}

	// -------------------------------------------------------------------------
	// shouldExposeFieldFullNames
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should expose fieldFullNames via accessor")
	public void shouldExposeFieldFullNames() {
		// Given
		SequencedCollection<String> fieldFullNames = List.of("/PersonRoot/firstName", "/PersonRoot/lastName");
		CheckUniquenessResult result = new CheckUniquenessResult(
			"PersonModel", "UniquePersonName", new DocumentReference("Person/42"), Map.of(), fieldFullNames,
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);

		// Then
		Assert.assertEquals(result.fieldFullNames(), fieldFullNames);
		Assert.assertTrue(result.fieldFullNames().contains("/PersonRoot/firstName"));
		Assert.assertTrue(result.fieldFullNames().contains("/PersonRoot/lastName"));
	}

	// -------------------------------------------------------------------------
	// shouldSerializeConflictingDocRefAsPlainString
	// -------------------------------------------------------------------------

	@Test(description = "Should serialize conflictingDocRef as a plain string, not a wrapped object")
	public void shouldSerializeConflictingDocRefAsPlainString() {
		// Given
		CheckUniquenessResult result = new CheckUniquenessResult(
			"PersonModel",
			"UniquePersonName",
			new DocumentReference("PersonModel/doc-042"),
			Map.of("en", "Name must be unique"),
			List.of("/PersonRoot/firstName"),
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY);
		ObjectMapper objectMapper = JsonMapper.builder().build();

		// When
		JsonNode json = objectMapper.valueToTree(result);

		// Then
		JsonNode conflictingDocRef = json.get("conflictingDocRef");
		Assert.assertTrue(conflictingDocRef.isTextual(),
			"conflictingDocRef must serialize as a plain string, not " + conflictingDocRef.getNodeType());
		Assert.assertEquals(conflictingDocRef.textValue(), "PersonModel/doc-042");
	}
}
