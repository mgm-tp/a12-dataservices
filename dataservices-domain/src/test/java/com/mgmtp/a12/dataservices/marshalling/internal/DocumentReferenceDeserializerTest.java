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
package com.mgmtp.a12.dataservices.marshalling.internal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link DocumentReferenceDeserializer} in isolation.
 * The mapper is configured explicitly with the deserializer so these tests are
 * independent of the `@JsonDeserialize` annotation on {@link DocumentReference}.
 */
public class DocumentReferenceDeserializerTest {

	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(DocumentReference.class, new DocumentReferenceDeserializer());
		objectMapper = JsonMapper.builder().addModule(module).build();
	}

	@Test(description = "Should deserialize DocumentReference from a plain string")
	public void shouldDeserializeFromPlainString() throws JacksonException {
		DocumentReference result = objectMapper.readValue("\"Contract/42\"", DocumentReference.class);
		assertEquals(result, new DocumentReference("Contract", "42"));
	}

	@Test(description = "Should deserialize DocumentReference from an object with a docRef field")
	public void shouldDeserializeFromObjectWithDocRefField() throws JacksonException {
		DocumentReference result = objectMapper.readValue("{\"docRef\":\"Contract/42\"}", DocumentReference.class);
		assertEquals(result, new DocumentReference("Contract", "42"));
	}

	@Test(description = "Should throw when the string value contains no valid separator",
		expectedExceptions = JacksonException.class)
	public void shouldThrowForStringWithoutSeparator() throws JacksonException {
		objectMapper.readValue("\"#{#addDocument.metadata.docRef}\"", DocumentReference.class);
	}

	@Test(description = "Should throw when the object input is missing the docRef field",
		expectedExceptions = JacksonException.class)
	public void shouldThrowWhenObjectIsMissingDocRefField() throws JacksonException {
		objectMapper.readValue("{\"other\":\"Contract/42\"}", DocumentReference.class);
	}

	@Test(description = "Should produce identical DocumentReference from both plain string and object input")
	public void shouldProduceIdenticalResultFromBothInputForms() throws JacksonException {
		DocumentReference fromString = objectMapper.readValue("\"Contract/42\"", DocumentReference.class);
		DocumentReference fromObject = objectMapper.readValue("{\"docRef\":\"Contract/42\"}", DocumentReference.class);
		assertEquals(fromString, fromObject);
	}
}
