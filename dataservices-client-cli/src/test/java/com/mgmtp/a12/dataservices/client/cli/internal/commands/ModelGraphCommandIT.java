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
package com.mgmtp.a12.dataservices.client.cli.internal.commands;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.cli.internal.AbstractCliIT;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelGraphCommandIT extends AbstractCliIT {

	@Autowired private ObjectMapper objectMapper;
	@Autowired private ResourcePatternResolver resourcePatternResolver;

	private final String customOutputDirPath = "./testOutput";
	private final File customOutputFolder = new File(customOutputDirPath);

	@BeforeClass
	public void setup() throws IOException {
		client.run(new DefaultApplicationArguments("model", "upload", CLASSPATH_BULK_MODELS_JAR));
	}

	@AfterClass
	public void cleanUp() throws IOException {
		LIST_INSURANCE_BULK_MODELS.forEach(this::cleanUpByDocumentModel);
		FileUtils.deleteDirectory(customOutputFolder);
	}

	@DataProvider(name = "application-arguments")
	public Object[][] validArguments() {
		return new Object[][] {
			{ "model", "graph" },
			{ "model", "graph", "--output" }
		};
	}

	@Test(dataProvider = "application-arguments")
	public void modelGraphCommandToCommandLine(String... args) throws IOException {
		client.run(new DefaultApplicationArguments(args));
		String actualModelGraphResponse = getCleanLineEndings(stdout.toString());

		JsonNode jsonNode = objectMapper.readTree(actualModelGraphResponse);
		Assert.assertEquals(jsonNode.size(), 4);
		Assert.assertFalse(jsonNode.path("documentModels").isMissingNode());
		Assert.assertFalse(jsonNode.path("relationshipModels").isMissingNode());
		Assert.assertFalse(jsonNode.path("composeDocumentModels").isMissingNode());
		Assert.assertFalse(jsonNode.path("genericModels").isMissingNode());

		String expectedResponse = resourcePatternResolver.getResource(MODEL_GRAPH_RESPONSE_PATH).getContentAsString(StandardCharsets.UTF_8);
		JSONAssert.assertEquals(expectedResponse, jsonNode.toString(), JSONCompareMode.LENIENT);
		assertThat(getCleanLineEndings(stderr.toString())).isBlank();
		assertThat(client.getExitCode()).isZero();
	}

	@Test
	public void modelGraphCommandToFile() throws IOException {
		String filePath = String.format("%s/%s", customOutputDirPath, "ExampleModelGraph.json");
		client.run(new DefaultApplicationArguments("model", "graph", String.format("--output=%s", filePath)));
		File createFile = new File(filePath);
		Assert.assertTrue(customOutputFolder.exists());
		Assert.assertTrue(createFile.exists());

		String fileContent = FileUtils.readFileToString(createFile, "UTF-8");
		JsonNode jsonNode = objectMapper.readTree(fileContent);
		Assert.assertEquals(jsonNode.size(), 4);
		Assert.assertFalse(jsonNode.path("documentModels").isMissingNode());
		Assert.assertFalse(jsonNode.path("relationshipModels").isMissingNode());
		Assert.assertFalse(jsonNode.path("composeDocumentModels").isMissingNode());
		Assert.assertFalse(jsonNode.path("genericModels").isMissingNode());
	}
}
