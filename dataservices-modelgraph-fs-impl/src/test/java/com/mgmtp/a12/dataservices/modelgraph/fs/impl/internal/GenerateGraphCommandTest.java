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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;

import picocli.CommandLine;

public class GenerateGraphCommandTest {

	private ModelGraphService modelGraphService;
	private ObjectMapper objectMapper;
	private ModelService modelService;
	private GenerateGraphCommand command;

	@BeforeMethod
	public void setUp() {
		modelGraphService = mock(ModelGraphService.class);
		objectMapper = mock(ObjectMapper.class);
		modelService = mock(ModelService.class);
		command = new GenerateGraphCommand(modelGraphService, objectMapper, modelService);
	}

	@DataProvider
	public Object[][] addSchemeIfMissingCases() {
		return new Object[][] {
			{ "/some/path/file.json", "file:/some/path/file.json" },
			{ "file:/already/has/scheme.json", "file:/already/has/scheme.json" },
			{ "classpath:models/model.json", "classpath:models/model.json" },
		};
	}

	@Test(dataProvider = "addSchemeIfMissingCases",
		description = "Should add file scheme when path has no URI scheme, or leave it unchanged when one is present")
	public void shouldAddFileSchemeWhenPathHasNoUriScheme(String input, String expected) {
		assertThat(GenerateGraphCommand.addSchemeIfMissing(input)).isEqualTo(expected);
	}

	@Test(description = "Should return exit code 2 when no input paths are provided")
	public void shouldReturn2WhenNoInputPathsAreProvided() throws Exception {
		int exitCode = command.call();

		assertThat(exitCode).isEqualTo(2);
	}

	@Test(description = "Should return exit code 0 and invoke graph construction when directory with JSON files is provided")
	public void shouldReturn0AndConstructGraphWhenDirectoryWithJsonFilesIsProvided() throws Exception {
		// Given
		Path tempDir = Files.createTempDirectory("generate-graph-test");
		try {
			Files.writeString(tempDir.resolve("model.json"), "{\"key\":\"value\"}", StandardCharsets.UTF_8);
			new CommandLine(command).parseArgs(tempDir.toString());

			// When
			int exitCode = command.call();

			// Then
			assertThat(exitCode).isEqualTo(0);
			verify(modelGraphService).constructModelGraph();
			verify(objectMapper).writeValue(any(OutputStream.class), any());
		} finally {
			Files.walk(tempDir)
				.sorted(Comparator.reverseOrder())
				.forEach(p -> p.toFile().delete());
		}
	}
}
