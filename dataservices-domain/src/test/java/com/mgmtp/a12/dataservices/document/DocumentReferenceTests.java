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
package com.mgmtp.a12.dataservices.document;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSerializationTest;
import com.mgmtp.a12.dataservices.document.exception.InvalidDocumentReferenceException;

import lombok.NonNull;
import lombok.SneakyThrows;

import static org.testng.Assert.assertEquals;

public class DocumentReferenceTests extends AbstractSerializationTest {

	@DataProvider
	public static Object[][] invalidDocRefs() {
		return new Object[][] {
			{ "DocumentModelName/" },
			{ "/DocumentId" },
			{ "DocumentModelNameDocumentId" },
			{ "" },
			{ "/" },
			{ "//////////////" },
			{ "#{#addDocument.docRef}" }
		};
	}

	@Test(
		dataProvider = "invalidDocRefs",
		expectedExceptions = InvalidDocumentReferenceException.class,
		expectedExceptionsMessageRegExp = "docRef \\[.*] is not a valid DocumentReference")
	public void invalidDocumentReferences(String docRef) {
		new DocumentReference(docRef);
	}

	@DataProvider
	public static Object[][] maliciousDocRefs() {
		return new Object[][] {
			{ "../.." },
			{ "./../virus.sh" },
			{ "../../../virus.sh" },
			{ "./virus.sh" }
		};
	}

	@Test(dataProvider = "maliciousDocRefs", expectedExceptions = InvalidDocumentReferenceException.class)
	public void maliciousDocumentReferences(String docRef) {
		new DocumentReference(docRef);
	}

	@Test
	public void okReference() {
		DocumentReference documentReference = new DocumentReference("DocumentModel/DocumentId");
		assertEquals(documentReference.getDocumentModelName(), "DocumentModel");
		assertEquals(documentReference.getDocumentId(), "DocumentId");
		assertEquals(documentReference, new DocumentReference("DocumentModel", "DocumentId"));
	}

	@DataProvider
	public static Object[][] docRefs() {
		return new Object[][] {
			{ "Model1/1", "Model1/2", -1 },
			{ "Model1/2", "Model1/1", 1 },
			{ "Model1/2", "Model1/2", 0 },
			{ "Model1/1", null, 1 },
			{ "Model2/1", "Model1/2", 1 },
			{ "Model2/2", "Model1/1", 1 },
			{ "Model2/1", "Model1/1", 1 },
			{ "Model1/1", "Model2/2", -1 },
			{ "Model1/2", "Model2/1", -1 },
			{ "Model1/1", "Model2/1", -1 },
		};
	}

	@Test(dataProvider = "docRefs")
	public void compareTo(@NonNull String first, String second, int expected) {
		DocumentReference secondDocRef = second == null ? null : new DocumentReference(second);
		assertEquals(new DocumentReference(first).compareTo(secondDocRef), expected);
	}

	@SneakyThrows
	@Test public void testSerialization() {
		String result = objectMapper.writeValueAsString(new DocumentReference("Contract/14"));
		assertEquals(result, "{\"docRef\":\"Contract/14\"}");
	}

	@DataProvider public static Object[][] deserializationDataProvider() {
		return new Object[][] {
			{ "{\"docRef\":\"Contract/14\"}", "Contract", "14" },
			{ "\"Contract/14\"", "Contract", "14" }
		};
	}

	@SneakyThrows
	@Test(dataProvider = "deserializationDataProvider")
	public void testDeserialization(String json, String model, String id) {
		DocumentReference result = objectMapper.readValue(json, DocumentReference.class);
		String stringValue = String.format("%s/%s", model, id);
		assertEquals(result.toString(), stringValue);
		assertEquals(result, new DocumentReference(stringValue));
		assertEquals(result, DocumentReference.builder().documentModelName(model).documentId(id).build());
		assertEquals(result.getDocumentModelName(), model);
		assertEquals(result.getDocumentId(), id);
	}
}
