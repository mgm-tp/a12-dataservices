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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.cli.internal.AbstractCliIT;
import com.mgmtp.a12.dataservices.client.model.ModelsClient;

import static com.mgmtp.a12.dataservices.client.cli.internal.commands.AbstractRPCCommand.EXIT_FAILURE;
import static com.mgmtp.a12.dataservices.client.cli.internal.commands.AbstractRPCCommand.EXIT_INVALID_ARGS;
import static com.mgmtp.a12.dataservices.client.cli.internal.commands.AbstractRPCCommand.EXIT_SUCCESS;
import static com.mgmtp.a12.dataservices.client.cli.internal.commands.HelpCommand.USAGE_MSG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class JsonRpcCommandIT extends AbstractCliIT {

	@Value("${working.directory}")
	private File workingDirectory;

	private static final String ORIGINAL_RPC_FOLDER = "src/test/resources/rpc";
	private static final String FOLDER_PATH = "/bulk/input";
	private static final String FOLDER_OUTPUT_PATH = "/my_output";

	private static final String FILE_1_PATH = "/bulk/input/folder1/1-addDoc.json";
	private static final String FILE_2_PATH = "/bulk/input/folder1/2-listDocs.json";
	private static final String FILE_3_PATH = "/bulk/input/folder2/1-wrong.json";
	private static final String COMPLEX_REQUEST_PATH = "/bulk/complexRequest.json";
	private static final String FILE_NOT_JSON_RPC_PATH = "/bulk/notJsonRPC.json";

	private static final String FOLDER_1_OUTPUT_PATH = "/my_output/folder1";
	private static final String FOLDER_2_OUTPUT_PATH = "/my_output/folder2";
	private static final String FILE_1_OUTPUT_PATH = FOLDER_1_OUTPUT_PATH + "/output-1-addDoc.json";
	private static final String FILE_2_OUTPUT_PATH = FOLDER_1_OUTPUT_PATH + "/output-2-listDocs.json";
	private static final String FILE_3_OUTPUT_PATH = FOLDER_2_OUTPUT_PATH + "/output-1-wrong.json";

	private static final String COMPLEX_REQUEST_OUTPUT_FOLDER = "/complex-output";
	private static final String COMPLEX_REQUEST_OUTPUT_PATH = COMPLEX_REQUEST_OUTPUT_FOLDER + "/output-complexRequest.json";
	private static final String MODEL_NAME = "Contract";
	private static final String MODEL_FILE = CLASSPATH_MODELS_DOCUMENT_CONTRACT_JSON;

	@Autowired private ObjectMapper objectMapper;
	@Autowired private ModelsClient modelsClient;
	@Autowired JsonRpcCommand jsonRpcCommand;
	@Autowired private ResourcePatternResolver resourcePatternResolver;

	@Override
	@BeforeMethod
	public void setUp() throws IOException {
		super.setUp();
		FileUtils.deleteDirectory(workingDirectory);
		File srcDir = new File(ORIGINAL_RPC_FOLDER);
		FileUtils.copyDirectory(srcDir, workingDirectory);

		modelsClient.deleteModel(MODEL_NAME);
		String model = resourcePatternResolver.getResource(MODEL_FILE).getContentAsString(StandardCharsets.UTF_8);
		modelsClient.createModel(new StringReader(model));
	}

	@AfterMethod
	public void cleanUp() {
		modelsClient.deleteModel(MODEL_NAME);
	}

	@DataProvider(name = "output-path-data")
	public Object[][] outputPathData() {
		return new Object[][] {
			{ FOLDER_PATH + "/asd/a/b/c.json", FOLDER_PATH, "def", ".*.def.asd.a.b.output-c\\.json" },
			{ FOLDER_PATH + "/single/file/b/c.json", FOLDER_PATH + "/single/file/b/c.json", "output_folder", ".*.output_folder.output-c\\.json" },
			{ FOLDER_PATH + "/./ghi/a/b/c.json", FOLDER_PATH, "./def", ".*.def.ghi.a.b.output-c\\.json" }
		};
	}

	@Test
	public void testInvalidOption() {
		client.run(new DefaultApplicationArguments("json", "rpc", "--invalidOne=%s".formatted(FILE_1_OUTPUT_PATH)));
		Assertions.assertThat(getCleanLineEndings(stderr.toString())).isEqualTo(USAGE_MSG + RPC_HELP_OUTPUT);
		assertExit(EXIT_INVALID_ARGS);
	}

	@Test
	public void testPathMissing() {
		client.run(new DefaultApplicationArguments("json", "rpc", "--output_dir=%s".formatted(FILE_1_OUTPUT_PATH)));
		Assertions.assertThat(getCleanLineEndings(stderr.toString())).isEqualTo(USAGE_MSG + RPC_HELP_OUTPUT);
		assertExit(EXIT_INVALID_ARGS);
	}

	@Test
	public void testOutputDirMissing() {
		client.run(new DefaultApplicationArguments("json", "rpc", "--path=%s".formatted(FILE_1_PATH)));
		Assertions.assertThat(getCleanLineEndings(stderr.toString())).isEqualTo(USAGE_MSG + RPC_HELP_OUTPUT);
		assertExit(EXIT_INVALID_ARGS);
	}

	@Test
	public void testSingleFirstFile() throws IOException {
		runClientWithPath(FILE_1_PATH, FOLDER_1_OUTPUT_PATH);

		assertFirstOutputIsCorrect();
		assertExit(EXIT_SUCCESS);
	}

	@Test
	public void testSingleSecondFile() throws IOException {
		runClientWithPath(FILE_2_PATH, FOLDER_1_OUTPUT_PATH);

		assertSecondOutputIsCorrect();
		assertExit(EXIT_SUCCESS);
	}

	@Test
	public void testSingleThirdFile() throws IOException {
		runClientWithPath(FILE_3_PATH, FOLDER_2_OUTPUT_PATH);

		assertThirdOutputIsCorrect();
		assertExit(EXIT_SUCCESS);
	}

	@Test
	public void testNotJsonRpcFile() {
		runClientWithPath(FILE_NOT_JSON_RPC_PATH, FOLDER_2_OUTPUT_PATH);
		assertExit(EXIT_FAILURE);
	}

	@Test
	public void testFolder() throws IOException {
		runClientWithPath(FOLDER_PATH, FOLDER_OUTPUT_PATH);

		assertFirstOutputIsCorrect();
		assertSecondOutputIsCorrect();
		assertThirdOutputIsCorrect();

		assertExit(EXIT_SUCCESS);
	}

	@Test
	public void testRequestWithSpel() throws IOException {
		runClientWithPath(COMPLEX_REQUEST_PATH, COMPLEX_REQUEST_OUTPUT_FOLDER);
		JsonNode json = getJsonFromFile(COMPLEX_REQUEST_OUTPUT_PATH);
		assertTrue(json.isArray());
		assertThat(json.get(0).at("/result/docRef").toString(),
			matchesPattern("\"Contract/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\""));
		assertEquals("null", json.get(1).at("/result").toString());
		assertExit(EXIT_SUCCESS);
	}

	@Test(dataProvider = "output-path-data")
	public void testOutputPath(String filePath, String inputPath, String outputDirPath, String expectedPattern) throws IOException {
		String outputPath = jsonRpcCommand.buildOutputPath(new File(getFullPath(filePath)), getFullPath(inputPath), outputDirPath);
		assertTrue(outputPath.matches(expectedPattern));
	}

	private String getFullPath(String relativePath) {
		return workingDirectory.getPath() + relativePath;
	}

	private void assertExit(int status) {
		Assertions.assertThat(client.getExitCode()).isEqualTo(status);
	}

	private void runClientWithPath(String relativePath, String relativeOutputDir) {
		client.run(new DefaultApplicationArguments("json", "rpc",
			"--input_path=%s".formatted(getFullPath(relativePath)),
			"--output_dir=%s".formatted(getFullPath(relativeOutputDir))
		));
	}

	private void assertThirdOutputIsCorrect() throws IOException {
		JsonNode json3 = getJsonFromFile(FILE_3_OUTPUT_PATH);
		assertTrue(json3.at("/result").isNull());
	}

	private void assertSecondOutputIsCorrect() throws IOException {
		JsonNode json2 = getJsonFromFile(FILE_2_OUTPUT_PATH);
		assertThat(json2.at("/result/fullSize").asInt(), greaterThan(0));
	}

	private void assertFirstOutputIsCorrect() throws IOException {
		JsonNode json1 = getJsonFromFile(FILE_1_OUTPUT_PATH);
		assertThat(json1.at("/result/docRef").toString(),
			matchesPattern("\"Contract/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\""));
	}

	private JsonNode getJsonFromFile(String relativePath) throws IOException {
		File outputFile = new File(getFullPath(relativePath));
		String content = removeFormatting(FileUtils.readFileToString(outputFile, "UTF-8"));
		return objectMapper.readTree(content);
	}

	private String removeFormatting(String s) {
		return s.replaceAll("\n", "")
			.replaceAll("\r", "")
			.replaceAll("\t", "")
			.replaceAll(" ", "");
	}
}
