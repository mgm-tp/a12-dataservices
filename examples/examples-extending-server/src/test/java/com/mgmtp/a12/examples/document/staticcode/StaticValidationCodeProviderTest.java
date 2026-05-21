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
package com.mgmtp.a12.examples.document.staticcode;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StaticValidationCodeProviderTest {

	private StaticValidationCodeProvider codeProvider;
	private ListIProblemReporter problemReporter;
	private final String dir = "js/";

	@BeforeMethod
	public void setUp() {
		codeProvider = Mockito.spy(new StaticValidationCodeProvider(dir));
		problemReporter = Mockito.mock(ListIProblemReporter.class);
	}

	@Test
	public void getValidationCode_existingFile_returnsJs() throws Exception {
		String modelId = "modelA";
		String js = "function validate() {}";
		InputStream stream = new ByteArrayInputStream(js.getBytes(StandardCharsets.UTF_8));
		Mockito.doReturn(stream).when(codeProvider).getResourceAsStream(dir + modelId + ".js");

		String result = codeProvider.getValidationCode(modelId, problemReporter);

		Assert.assertEquals(result, js);
		Mockito.verify(problemReporter, Mockito.never()).validate(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(codeProvider).getResourceAsStream(dir + modelId + ".js");
	}

	@Test
	public void getValidationCode_missingFile_reportsProblem() {
		String modelId = "missing";
		Mockito.doReturn(null).when(codeProvider).getResourceAsStream(dir + modelId + ".js");

		String result = codeProvider.getValidationCode(modelId, problemReporter);

		Assert.assertNull(result);
		Mockito.verify(problemReporter, Mockito.times(1)).validate(
			Mockito.eq(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE),
			Mockito.eq(ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY),
			Mockito.anyString()
		);
	}

	@Test
	public void getValidationCode_ioException_reportsProblem() throws Exception {
		String modelId = "ioerror";
		InputStream faulty = Mockito.mock(InputStream.class);
		Mockito.doReturn(faulty).when(codeProvider).getResourceAsStream(dir + modelId + ".js");
		Mockito.when(faulty.readAllBytes()).thenThrow(new IOException("boom"));

		String result = codeProvider.getValidationCode(modelId, problemReporter);

		Assert.assertNull(result);
		Mockito.verify(problemReporter, Mockito.times(1)).validate(
			Mockito.eq(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE),
			Mockito.eq(ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY),
			Mockito.anyString()
		);
		Mockito.verify(faulty, Mockito.times(1)).close();
	}
}
