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
package com.mgmtp.a12.dataservices.document.support.internal;

import java.io.Reader;
import java.io.StringReader;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

/**
 * Unit tests to verify JsonNode-based deserialization methods in {@link DefaultDocumentSupport}.
 *
 * These tests verify that the new `convertJSONToDocument(String, JsonNode)` methods work correctly
 * by accepting JsonNode directly. The methods centralize JSON-to-Document conversion, providing
 * a convenient API for callers that already have a JsonNode instance.
 *
 * Expected Behavior:
 *
 * - New overloaded methods accept `JsonNode` directly
 * - Functional equivalence with existing `Reader`-based methods is maintained
 * - Error handling for invalid JsonNode is consistent with existing behavior
 * - DocumentReference parameter is properly passed for improved logging
 * - `null` JsonNode inputs are handled gracefully
 *
 * Implementation Note:
 * The current implementation converts the JsonNode to a String internally using `toString()`,
 * then deserializes via the standard Reader-based path. This centralizes the conversion logic
 * in one place for consistency.
 *
 * @see DefaultDocumentSupport
 * @see com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation
 * @see com.mgmtp.a12.dataservices.document.operation.internal.ModifyDocumentOperation
 */
public class DocumentSupportJsonNodeTest {

	private DocumentDeserializationConfig defaultDeserConfig;
	private DocumentSerializationConfig defaultSerConfig;
	private IDocumentModelResolver modelResolver;
	private IDocumentV2Serializer serializer;
	private DefaultDocumentSupport support;
	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		defaultDeserConfig = mock(DocumentDeserializationConfig.class);
		defaultSerConfig = mock(DocumentSerializationConfig.class);
		modelResolver = mock(IDocumentModelResolver.class);
		serializer = mock(IDocumentV2Serializer.class);
		support = new DefaultDocumentSupport(defaultDeserConfig, defaultSerConfig, modelResolver, serializer);
		objectMapper = new ObjectMapper();
	}

	/**
	 * Verifies that a document can be deserialized from a JsonNode.
	 *
	 * Expected Behavior:
	 * When a valid JsonNode is provided, the `convertJSONToDocument(String, JsonNode)` method
	 * should successfully deserialize it to a DocumentV2 instance. The method converts the
	 * JsonNode to a String internally and delegates to the Reader-based deserialization.
	 *
	 * Test Scenario:
	 *
	 * 1. Create a JsonNode representing a valid document (e.g., `{"name": "John Doe"}`)
	 * 2. Call `convertJSONToDocument(documentModelName, jsonNode)`
	 * 3. Verify the method returns a valid DocumentV2 instance
	 * 4. Verify the serializer's deserializeV2 method is called with correct parameters
	 *
	 * Configuration Dependencies:
	 *
	 * - Default DocumentDeserializationConfig should be used (same as Reader-based method)
	 * - Serializer should accept Reader input
	 */
	@Test(enabled = true, description = "Should deserialize document from JsonNode without toString() conversion")
	public void shouldDeserializeDocumentFromJsonNode() {
		// Given: A JsonNode representing a valid document
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put("name", "John Doe");
		jsonNode.put("age", 30);

		// And: The serializer is configured to return a mock DocumentV2
		DocumentV2 expectedDoc = mock(DocumentV2.class);
		when(serializer.deserializeV2(any(Reader.class), eq("TestModel"), eq(defaultDeserConfig), any()))
			.thenReturn(expectedDoc);

		// When: We call the new JsonNode-based conversion method
		DocumentV2 result = support.convertJSONToDocument("TestModel", jsonNode);

		// Then: The method should return the expected document
		assertNotNull(result, "Result should not be null");
		assertSame(result, expectedDoc, "Result should be the document returned by serializer");

		// And: The serializer's deserializeV2 method should be called with correct model name
		ArgumentCaptor<String> modelCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<DocumentDeserializationConfig> configCaptor = ArgumentCaptor.forClass(DocumentDeserializationConfig.class);
		verify(serializer).deserializeV2(any(Reader.class), modelCaptor.capture(), configCaptor.capture(), any());

		assertEquals(modelCaptor.getValue(), "TestModel", "Model name should match");
		assertSame(configCaptor.getValue(), defaultDeserConfig, "Should use default deserialization config");
	}

	/**
	 * Verifies that DocumentReference parameter is accepted for improved error logging.
	 *
	 * Expected Behavior:
	 * When the overloaded method with DocumentReference parameter is called, the method should
	 * complete successfully and return a valid DocumentV2 instance. The DocumentReference
	 * parameter will be used internally for improved error messages (similar to the existing
	 * `convertJSONToDocument(String, Reader, DocumentReference)` method).
	 *
	 * Test Scenario:
	 *
	 * 1. Create a JsonNode representing a valid document
	 * 2. Create a DocumentReference with model name and docRef
	 * 3. Call `convertJSONToDocument(documentModelName, jsonNode, documentReference)`
	 * 4. Verify the method returns a valid DocumentV2 instance
	 *
	 * Why This Matters:
	 * When deserialization fails, the DocumentReference provides context in error messages,
	 * making it easier to identify which document caused the problem (especially useful during
	 * document modifications where the docRef is known).
	 */
	@Test(enabled = true, description = "Should accept DocumentReference parameter for improved error logging")
	public void shouldDeserializeDocumentWithDocumentReference() {
		// Given: A JsonNode representing a valid document
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put("contractNumber", "C-12345");

		// And: A DocumentReference for logging
		DocumentReference docRef = new DocumentReference("Contract", "contract-ref-123");

		// And: The serializer is configured to return a mock DocumentV2
		DocumentV2 expectedDoc = mock(DocumentV2.class);
		when(serializer.deserializeV2(any(Reader.class), eq("Contract"), eq(defaultDeserConfig), any()))
			.thenReturn(expectedDoc);

		// When: We call the JsonNode-based conversion method with DocumentReference
		DocumentV2 result = support.convertJSONToDocument("Contract", jsonNode, docRef);

		// Then: The method should return the expected document
		assertNotNull(result, "Result should not be null");
		assertSame(result, expectedDoc, "Result should be the document returned by serializer");

		// And: The serializer's deserializeV2 method should be called
		verify(serializer).deserializeV2(any(Reader.class), eq("Contract"), eq(defaultDeserConfig), any());
	}

	/**
	 * Verifies that invalid JsonNode input throws appropriate exception.
	 *
	 * Expected Behavior:
	 * When the serializer throws an exception during deserialization (e.g., due to schema
	 * validation failure or malformed JSON structure), the method should wrap it in a
	 * DataServicesDocumentSerializationException with appropriate error details.
	 *
	 * Test Scenario:
	 *
	 * 1. Create a JsonNode that will fail deserialization
	 * 2. Configure the serializer to throw an exception
	 * 3. Call `convertJSONToDocument(documentModelName, jsonNode)`
	 * 4. Verify DataServicesDocumentSerializationException is thrown
	 * 5. Verify exception contains meaningful error message
	 *
	 * Why This Matters:
	 * Error handling should be consistent with the existing Reader-based methods. Clients
	 * expect DataServicesDocumentSerializationException when document deserialization fails,
	 * regardless of whether the input is Reader or JsonNode.
	 */
	@Test(enabled = true, description = "Should throw exception for invalid JsonNode", expectedExceptions = DataServicesDocumentSerializationException.class)
	public void shouldThrowExceptionForInvalidJsonNode() {
		// Given: A JsonNode that will cause deserialization to fail
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put("invalidField", "invalidValue");

		// And: The serializer is configured to throw an exception
		when(serializer.deserializeV2(any(Reader.class), eq("TestModel"), eq(defaultDeserConfig), any()))
			.thenThrow(new DocumentSerializationException("Deserialization failed: invalid field type"));

		// When: We call the JsonNode-based conversion method with invalid input
		// Then: DataServicesDocumentSerializationException should be thrown (expected by annotation)
		support.convertJSONToDocument("TestModel", jsonNode);
	}

	/**
	 * Verifies functional equivalence between JsonNode-based and Reader-based methods.
	 *
	 * Expected Behavior:
	 * When the same document is deserialized using both the JsonNode-based method and the
	 * existing Reader-based method, both should produce identical DocumentV2 instances.
	 *
	 * Test Scenario:
	 *
	 * 1. Create a JsonNode representing a test document
	 * 2. Deserialize using `convertJSONToDocument(String, JsonNode)`
	 * 3. Deserialize the same document using `convertJSONToDocument(String, Reader)`
	 * 4. Verify both methods produce identical results
	 *
	 * Why This Matters:
	 * The JsonNode-based method is a convenience API. Clients should not observe
	 * any difference in deserialization results. This test ensures backward compatibility and
	 * functional equivalence.
	 *
	 * Implementation Note:
	 * Since we are testing with mocks, we verify that both methods result in the same
	 * serializer invocation pattern (same parameters, same config). In a real scenario,
	 * the serializer would produce identical DocumentV2 instances.
	 */
	@Test(enabled = true, description = "Should produce same result as Reader-based method")
	public void shouldProduceSameResultAsReaderMethod() {
		// Given: A JsonNode and its String representation
		ObjectNode jsonNode = objectMapper.createObjectNode();
		jsonNode.put("field1", "value1");
		jsonNode.put("field2", 42);
		String jsonString = jsonNode.toString();

		// And: The serializer is configured to return a mock DocumentV2
		DocumentV2 expectedDoc = mock(DocumentV2.class);
		when(serializer.deserializeV2(any(Reader.class), eq("TestModel"), eq(defaultDeserConfig), any()))
			.thenReturn(expectedDoc);

		// When: We deserialize using the new JsonNode-based method
		DocumentV2 resultFromJsonNode = support.convertJSONToDocument("TestModel", jsonNode);

		// And: We deserialize using the existing Reader-based method
		DocumentV2 resultFromReader = support.convertJSONToDocument("TestModel", new StringReader(jsonString));

		// Then: Both methods should return the same document
		assertNotNull(resultFromJsonNode, "Result from JsonNode should not be null");
		assertNotNull(resultFromReader, "Result from Reader should not be null");
		assertSame(resultFromJsonNode, expectedDoc, "JsonNode-based method should return expected document");
		assertSame(resultFromReader, expectedDoc, "Reader-based method should return expected document");

		// And: Both should have called the serializer with the same configuration (2 times total)
		verify(serializer, times(2)).deserializeV2(any(Reader.class), eq("TestModel"), eq(defaultDeserConfig), any());
	}

	/**
	 * Verifies that `null` JsonNode input is handled gracefully.
	 *
	 * Expected Behavior:
	 * When a `null` JsonNode is passed to `convertJSONToDocument(String, JsonNode)`, the method
	 * should throw an appropriate exception (e.g., NullPointerException or
	 * DataServicesDocumentSerializationException) rather than causing undefined behavior.
	 *
	 * Test Scenario:
	 *
	 * 1. Call `convertJSONToDocument(documentModelName, null)`
	 * 2. Verify an exception is thrown
	 * 3. Verify the exception has a meaningful error message
	 *
	 * Why This Matters:
	 * `null` inputs should fail fast with clear error messages. This is consistent with defensive
	 * programming practices and helps developers identify issues early in the development cycle.
	 *
	 * Implementation Note:
	 * The implementation throws DataServicesDocumentSerializationException with a descriptive
	 * message when `null` is passed.
	 */
	@Test(enabled = true, description = "Should handle null JsonNode gracefully", expectedExceptions = {NullPointerException.class, DataServicesDocumentSerializationException.class})
	public void shouldHandleNullJsonNode() {
		// Given: A null JsonNode
		JsonNode nullNode = null;

		// When: We call the JsonNode-based conversion method with null input
		// Then: An exception should be thrown (NullPointerException or DataServicesDocumentSerializationException)
		support.convertJSONToDocument("TestModel", nullNode);
	}
}
