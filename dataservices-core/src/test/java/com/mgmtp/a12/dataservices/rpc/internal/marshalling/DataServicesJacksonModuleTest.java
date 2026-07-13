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
package com.mgmtp.a12.dataservices.rpc.internal.marshalling;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;

import com.mgmtp.a12.dataservices.document.DocumentReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DataServicesJacksonModule} verifying that serialize and deserialize
 * operations on {@link DocumentReference} work correctly end-to-end when the module is registered.
 */
public class DataServicesJacksonModuleTest {

	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		objectMapper = JsonMapper.builder().addModule(new DataServicesJacksonModule(Collections.emptyList())).build();
	}

	@Test(description = "Should serialize DocumentReference to a plain JSON string via module")
	public void shouldSerializeDocumentReferenceViaModule() throws JacksonException {
		String result = objectMapper.writeValueAsString(new DocumentReference("Contract", "42"));
		assertEquals(result, "\"Contract/42\"");
	}

	@Test(description = "Should deserialize DocumentReference from a plain string via module")
	public void shouldDeserializePlainStringViaModule() throws JacksonException {
		DocumentReference result = objectMapper.readValue("\"Contract/42\"", DocumentReference.class);
		assertEquals(result, new DocumentReference("Contract", "42"));
	}

	@Test(description = "Should deserialize DocumentReference from an object form via module")
	public void shouldDeserializeObjectFormViaModule() throws JacksonException {
		DocumentReference result = objectMapper.readValue("{\"docRef\":\"Contract/42\"}", DocumentReference.class);
		assertEquals(result, new DocumentReference("Contract", "42"));
	}

	@Test(description = "Should return subtypes passed to constructor and reject mutation")
	public void shouldReturnSubtypesAndRejectMutation() {
		NamedType namedType = new NamedType(DocumentReference.class, "doc_ref");
		DataServicesJacksonModule module = new DataServicesJacksonModule(List.of(namedType));

		Collection<NamedType> subtypes = module.getSubtypes();

		assertTrue(subtypes.contains(namedType), "Returned subtypes must contain the registered NamedType");
		assertThrows(UnsupportedOperationException.class, () -> subtypes.add(new NamedType(String.class, "str")));
	}
}
