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
package com.mgmtp.a12.dataservices.document.operation.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResponse;
import com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResult;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

/**
 * Unit tests for {@link CheckUniquenessOperation}.
 *
 * Tests are written against the `rpc(String documentModelName, JsonNode document, String docRef)`
 * signature. The operation converts the JsonNode to DocumentV2 via DocumentSupport before delegating
 * to UniqueConstraintValidator.
 */
public class CheckUniquenessOperationTest {

	private UniqueConstraintValidator uniqueConstraintValidator;
	private DocumentService documentService;
	private Anonymizer anonymizer;
	private DocumentSupport documentSupport;
	private DocumentV2 convertedDocument;
	private CheckUniquenessOperation operation;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeMethod
	public void setUp() {
		uniqueConstraintValidator = mock(UniqueConstraintValidator.class);
		documentService = mock(DocumentService.class);
		anonymizer = mock(Anonymizer.class);
		documentSupport = mock(DocumentSupport.class);
		convertedDocument = mock(DocumentV2.class);
		// Anonymizer returns the input unchanged for test predictability
		Mockito.lenient().when(anonymizer.apply(any())).thenAnswer(inv -> inv.getArgument(0));
		// DocumentSupport always returns the same mock DocumentV2 regardless of inputs
		Mockito.lenient().when(documentSupport.convertJSONToDocument(any(String.class), any(JsonNode.class), any()))
			.thenReturn(convertedDocument);
		operation = new CheckUniquenessOperation(documentService, anonymizer, uniqueConstraintValidator, documentSupport);
	}

	// -------------------------------------------------------------------------
	// shouldReturnEmptyListWhenModelHasNoCriteria
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should return empty violations when model has no criteria")
	public void shouldReturnEmptyListWhenModelHasNoCriteria() {
		// Given
		JsonNode document = objectMapper.createObjectNode();
		when(uniqueConstraintValidator.checkAllConstraints(eq("PersonModel"), any(DocumentV2.class), eq(null)))
			.thenReturn(List.of());

		// When
		CheckUniquenessResponse response = operation.rpc("PersonModel", document, null);

		// Then
		Assert.assertNotNull(response);
		Assert.assertTrue(response.violations().isEmpty());
		verify(uniqueConstraintValidator).checkAllConstraints(eq("PersonModel"), any(DocumentV2.class), eq(null));
	}

	// -------------------------------------------------------------------------
	// shouldReturnEmptyListWhenNoConflict
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should return empty violations when the document does not conflict")
	public void shouldReturnEmptyListWhenNoConflict() {
		// Given
		ObjectNode personRoot = objectMapper.createObjectNode();
		personRoot.put("firstName", "Alice");
		ObjectNode document = objectMapper.createObjectNode();
		document.set("PersonRoot", personRoot);

		when(uniqueConstraintValidator.checkAllConstraints(eq("PersonModel"), any(DocumentV2.class), eq(null)))
			.thenReturn(List.of());

		// When
		CheckUniquenessResponse response = operation.rpc("PersonModel", document, null);

		// Then
		Assert.assertNotNull(response);
		Assert.assertTrue(response.violations().isEmpty());
	}

	// -------------------------------------------------------------------------
	// shouldReturnViolationWhenConflictExists
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should return a violation with errorKey when the document conflicts with another document")
	public void shouldReturnViolationWhenConflictExists() {
		// Given
		ObjectNode personRoot = objectMapper.createObjectNode();
		personRoot.put("firstName", "Alice");
		ObjectNode document = objectMapper.createObjectNode();
		document.set("PersonRoot", personRoot);

		String expectedErrorKey = ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY + ".PersonModel.UniquePersonName";
		CheckUniquenessResult violation = new CheckUniquenessResult(
			"PersonModel",
			"UniquePersonName",
			new DocumentReference("PersonModel/doc-002"),
			Map.of("en", "Name must be unique"),
			List.of("/PersonRoot/firstName"),
			expectedErrorKey
		);
		when(uniqueConstraintValidator.checkAllConstraints(eq("PersonModel"), any(DocumentV2.class), eq(null)))
			.thenReturn(List.of(violation));

		// When
		CheckUniquenessResponse response = operation.rpc("PersonModel", document, null);

		// Then
		Assert.assertEquals(response.violations().size(), 1);
		CheckUniquenessResult result = response.violations().get(0);
		Assert.assertEquals(result.constraintName(), "UniquePersonName");
		Assert.assertEquals(result.conflictingDocRef().toString(), "PersonModel/doc-002");
		Assert.assertEquals(result.errorKey(), expectedErrorKey);
		Assert.assertEquals(result.modelName(), "PersonModel");
	}

	// -------------------------------------------------------------------------
	// shouldPassDocRefToValidatorForSelfExclusion
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should pass docRef to validator for self-exclusion on update")
	public void shouldPassDocRefToValidatorForSelfExclusion() {
		// Given
		JsonNode document = objectMapper.createObjectNode();
		DocumentReference docRef = new DocumentReference("PersonModel/doc-001");
		when(uniqueConstraintValidator.checkAllConstraints(eq("PersonModel"), any(DocumentV2.class), eq(docRef)))
			.thenReturn(List.of());

		// When
		CheckUniquenessResponse response = operation.rpc("PersonModel", document, docRef);

		// Then
		Assert.assertNotNull(response);
		Assert.assertTrue(response.violations().isEmpty());
		// Verify docRef was forwarded to the validator
		verify(uniqueConstraintValidator).checkAllConstraints(eq("PersonModel"), any(DocumentV2.class), eq(docRef));
	}

	// -------------------------------------------------------------------------
	// shouldRethrowBaseExceptionUnwrapped
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should re-throw BaseException without wrapping")
	public void shouldRethrowBaseExceptionUnwrapped() {
		// Given
		JsonNode document = objectMapper.createObjectNode();
		BaseException baseException = mock(BaseException.class);
		when(uniqueConstraintValidator.checkAllConstraints(any(), any(DocumentV2.class), any()))
			.thenThrow(baseException);

		// When / Then
		try {
			operation.rpc("PersonModel", document, null);
			fail("Expected BaseException to be re-thrown");
		} catch (BaseException e) {
			Assert.assertSame(e, baseException, "Expected the original BaseException to be re-thrown unchanged");
		} catch (Exception e) {
			fail("Expected BaseException but got: " + e.getClass().getName());
		}
	}

	// -------------------------------------------------------------------------
	// shouldWrapUnexpectedExceptionAsRpcException
	// -------------------------------------------------------------------------

	@Test(enabled = true, description = "Should wrap unexpected exceptions as RPC exceptions")
	public void shouldWrapUnexpectedExceptionAsRpcException() {
		// Given
		JsonNode document = objectMapper.createObjectNode();
		when(uniqueConstraintValidator.checkAllConstraints(any(), any(DocumentV2.class), any()))
			.thenThrow(new RuntimeException("unexpected failure"));

		// When / Then
		try {
			operation.rpc("PersonModel", document, null);
			fail("Expected RpcException to be thrown");
		} catch (RpcException e) {
			Assert.assertEquals(
				e.getOperationError().getOperationId(),
				RemoteOperation.RemoteOperationHelper.getOperationId(CheckUniquenessOperation.class));
		} catch (Exception e) {
			fail("Expected RpcException but got: " + e.getClass().getName());
		}
	}
}
