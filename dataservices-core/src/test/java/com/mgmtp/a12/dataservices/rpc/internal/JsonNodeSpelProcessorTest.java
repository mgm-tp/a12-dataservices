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
package com.mgmtp.a12.dataservices.rpc.internal;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.utils.internal.JsonUtils;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class JsonNodeSpelProcessorTest {

	public static final String DOCREF_STRING = "model/13";
	public static final String TEST_STRING = "tEsT123";
	public static final double TEST_NUMBER = 5345.23452;
	public static final double TEST_INT = 2134345;
	final ObjectMapper objectMapper = new ObjectMapper();
	private final JsonUtils jsonUtils = new JsonUtils(objectMapper);

	@BeforeMethod public void setUp() {
		OperationContextHolder.clear();
		OperationContextHolder.id("testOperation1");
		OperationContextHolder.put(new DocumentReference(DOCREF_STRING));
		OperationContextHolder.put("testOperation2", TEST_STRING);
		OperationContextHolder.id("testOperation3");
		OperationContextHolder.put(TEST_INT);
		OperationContextHolder.put("testOperation4", TEST_NUMBER);

	}

	@Test public void testEvaluateSpel() throws IOException {
		JsonNodeSpelProcessor spelProcessor = new JsonNodeSpelProcessor(jsonUtils);
		InputStream is = getClass().getResourceAsStream("/spelTest.json");
		JsonNode rootNode = spelProcessor.evaluateSpel(objectMapper.readTree(is));

		assertThatJson(rootNode).inPath("$.integerValue").isIntegralNumber().isEqualTo(1);
		assertThatJson(rootNode).inPath("$.floatValue").isNumber().isEqualTo(BigDecimal.valueOf(1.345));
		assertThatJson(rootNode).inPath("$.trueValue").isBoolean().isEqualTo(true);
		assertThatJson(rootNode).inPath("$.falseValue").isBoolean().isEqualTo(false);
		assertThatJson(rootNode).inPath("$.stringValue").isString().isEqualTo("someString");
		assertThatJson(rootNode).inPath("$.spelValue").isString().isEqualTo(TEST_STRING);
		assertThatJson(rootNode).inPath("$.arrayValue").isArray();

		assertThatJson(rootNode).inPath("$.innerObject.innerInteger").isIntegralNumber().isEqualTo(3);
		assertThatJson(rootNode).inPath("$.innerObject.innerFloat").isNumber().isEqualTo(BigDecimal.valueOf(4.567));
		assertThatJson(rootNode).inPath("$.innerObject.innerTrue").isBoolean().isEqualTo(true);
		assertThatJson(rootNode).inPath("$.innerObject.innerFalse").isBoolean().isEqualTo(false);
		assertThatJson(rootNode).inPath("$.innerObject.innerString").isString().isEqualTo("stringvalue");
		assertThatJson(rootNode).inPath("$.innerObject.spelValue").isString().isEqualTo(DOCREF_STRING);

		assertThatJson(rootNode).inPath("$.arrayValue[0]").isString().isEqualTo("string");
		assertThatJson(rootNode).inPath("$.arrayValue[1]").isIntegralNumber().isEqualTo(5);
		assertThatJson(rootNode).inPath("$.arrayValue[2]").isNumber().isEqualTo(BigDecimal.valueOf(6.965));
		assertThatJson(rootNode).inPath("$.arrayValue[3]").isObject();
		assertThatJson(rootNode).inPath("$.arrayValue[4]").isString().isEqualTo(String.valueOf(TEST_INT));

		assertThatJson(rootNode).inPath("$.arrayValue[3].innerInteger").isIntegralNumber().isEqualTo(3);
		assertThatJson(rootNode).inPath("$.arrayValue[3].innerFloat").isNumber().isEqualTo(BigDecimal.valueOf(4.567));
		assertThatJson(rootNode).inPath("$.arrayValue[3].innerTrue").isBoolean().isEqualTo(true);
		assertThatJson(rootNode).inPath("$.arrayValue[3].innerFalse").isBoolean().isEqualTo(false);
		assertThatJson(rootNode).inPath("$.arrayValue[3].innerString").isString().isEqualTo("stringvalue");
		assertThatJson(rootNode).inPath("$.arrayValue[3].spelValue").isString().isEqualTo(String.valueOf(TEST_NUMBER));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Parameter deserialization error occurred!")
	public void testEvaluateInvalidSpel() throws IOException {
		JsonNodeSpelProcessor spelProcessor = new JsonNodeSpelProcessor(jsonUtils);
		InputStream is = getClass().getResourceAsStream("/spelTestInvalid.json");
		JsonNode rootNode = objectMapper.readTree(is);
		spelProcessor.evaluateSpel(rootNode);
	}
}
