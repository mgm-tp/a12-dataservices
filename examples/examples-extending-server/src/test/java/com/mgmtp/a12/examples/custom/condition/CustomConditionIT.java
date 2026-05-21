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
package com.mgmtp.a12.examples.custom.condition;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CustomConditionIT extends AbstractITBase {

	public static final String MODEL_NAME = "CustomConditionModel";
	@Autowired KernelDocumentService kernelDocumentService;

	private DocumentV2 validDoc;
	private DocumentV2 inValidDoc;
	private static final String CUSTOMCONDITION_PATH = "custom_condition/model/";
	private static final String CUSTOMCONDITION_DOCUMENT_PATH = "custom_condition/document/";

	@BeforeClass
	public void init() throws IOException {
		createModel(SRC_TEST_RESOURCES_PATH, CUSTOMCONDITION_PATH + "CustomConditionModel.json");
		validDoc = loadFromResource(MODEL_NAME, SRC_TEST_RESOURCES_PATH + CUSTOMCONDITION_DOCUMENT_PATH + "CustomCondition-valid.json");
		inValidDoc = loadFromResource(MODEL_NAME, SRC_TEST_RESOURCES_PATH + CUSTOMCONDITION_DOCUMENT_PATH + "CustomCondition-invalid.json");
	}

	@Test
	public void checkValidDocument() {
		Optional<IDocumentValidationResult> testResults = kernelDocumentService.validateDocument(validDoc, null);
		assertNotNull(testResults);
		assertTrue(testResults.isPresent());
		assertTrue(testResults.get().noErrorOccurred());
	}

	@Test
	public void checkInvalidDocument() {
		Optional<IDocumentValidationResult> testResults = kernelDocumentService.validateDocument(inValidDoc, null);
		assertNotNull(testResults);
		assertTrue(testResults.isPresent());
		IMessage message = testResults.get().getMessages().get(0);
		assertEquals(message.getErrorCode(), "SampleErrorCode");
		assertEquals(message.getErrorText(), "numberB should be greater than 0");
		assertEquals(message.getRulePath().orElse(""), "/top/CustomCondition1");
	}
}
