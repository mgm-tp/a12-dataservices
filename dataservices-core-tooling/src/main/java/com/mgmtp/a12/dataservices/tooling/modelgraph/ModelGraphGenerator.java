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
package com.mgmtp.a12.dataservices.tooling.modelgraph;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * Main class for generating a model graph from input files.
 */
@Slf4j
@RequiredArgsConstructor
@SpringBootApplication(scanBasePackages = { "com.mgmtp.a12.dataservices.tooling.modelgraph" })
public class ModelGraphGenerator implements ApplicationRunner, ExitCodeGenerator {

	// TODO: remove unsued properties in the next breaking change release
	private final ModelGraphService modelGraphService;
	private final ObjectMapper objectMapper;
	private final ModelService modelGraphModelService;
	private final ResourcePatternResolver resourcePatternResolver;

	private CommandLine.IFactory factory;

	private GenerateGraphCommand generateGraphCommand;
	private int exitCode = 1;


	@Autowired
	public void setFactory(CommandLine.IFactory factory) {
		this.factory = factory;
	}

	@Autowired
	public void setGenerateGraphCommand(GenerateGraphCommand generateGraphCommand) {
		this.generateGraphCommand = generateGraphCommand;
	}
	/**
	 * Executes on application startup and constructs the model graph from the provided input resources.
	 * Supported application arguments:
	 *
	 * - Non-option arguments: List of input file paths, which can be specified as individual arguments or as a single argument with paths separated by commas.
	 *   The provided path examples:
	 *   - `file:/home/user/data/models`
	 *   - `file:/C:\data\models`
	 * - `--output`: Specifies the output file path. Only one output file is supported. If multiple output paths are provided, the application logs an error and exits with a failure code.
	 *
	 * @param args Application arguments; never `null`.
	 * @throws IOException If reading input or writing output fails.
	 * @throws URISyntaxException If an input path cannot be parsed as a valid URI.
	 */
	@Override public void run(ApplicationArguments args) {
		exitCode = new CommandLine(generateGraphCommand, factory).execute(args.getSourceArgs());
	}

	/**
	 * Returns the exit code for this process.
	 * The code reflects the outcome of {@link #run(ApplicationArguments)}:
	 * 0 indicates success; any non-zero value indicates failure.
	 *
	 * @return The exit code to be used by {@link org.springframework.boot.ExitCodeGenerator}.
	 */
	@Override public int getExitCode() {
		return exitCode;
	}

	/**
	 * Application entry point that bootstraps the Spring Boot runtime to generate the model graph.
	 *
	 * @param args Command-line arguments; may include input paths and `--output=<file>` for the destination.
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplicationBuilder(ModelGraphGenerator.class)
			.bannerMode(Banner.Mode.OFF)
			.web(WebApplicationType.NONE)
			.logStartupInfo(false)
			.build();
		try {
			System.exit(SpringApplication.exit(app.run(args)));
		} catch (Throwable e) {
			log.error("Unable to start the application.", e);
			PrintStream dialogOutput = System.err;
			dialogOutput.println();
			dialogOutput.println("===================================================");
			dialogOutput.println("!!   Unable to start the application             !!");
			dialogOutput.println();
			dialogOutput.flush();
			System.exit(1);
		}
	}
}
