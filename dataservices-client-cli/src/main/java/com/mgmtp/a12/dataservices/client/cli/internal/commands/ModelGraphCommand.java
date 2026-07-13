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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.cli.internal.IApplicationOutput;
import com.mgmtp.a12.dataservices.client.relationship.RelationshipClient;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

// Class provides option to retrieve Model Graph from server using CLI Tool
@Slf4j
@Component public class ModelGraphCommand extends AbstractRPCCommand<ApplicationArguments> {

	public static final String MODEL_GRAPH_COMMAND = "model graph";

	private final RelationshipClient relationshipClient;
	private final ObjectMapper objectMapper;

	public ModelGraphCommand(@NonNull IApplicationOutput applicationOutput, @NonNull RelationshipClient relationshipClient, @NonNull ObjectMapper objectMapper) {
		super(applicationOutput);
		this.relationshipClient = relationshipClient;
		this.objectMapper = objectMapper;
	}

	@Override protected CommandResponse<ApplicationArguments> executeRemoteCommand(ApplicationArguments args) {
		PrintWriter output = new PrintWriter(resolveOutputArgument(args)
			.map(this::makeFile)
			.map(ModelGraphCommand::makeOutputStream)
			.orElse(applicationOutput.getDataOutput()));

		try {
			objectMapper.writeValue(output, relationshipClient.getModelGraph());
			output.flush();
			return new CommandResponse<>(EXIT_SUCCESS, this);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			printA12ClientException(output, e);
			output.flush();
			return new CommandResponse<>(EXIT_FAILURE, this);
		}
	}

	@SneakyThrows
	private static OutputStream makeOutputStream(File f) {
		return new FileOutputStream(f);
	}

	private File makeFile(String p) {
		File f = new File(p);
		try {
			FileUtils.createParentDirectories(f);
			FileUtils.touch(f);
		} catch (IOException e) {
			throw new UnexpectedException(e).withAnonymityMessage("File creation failed.");
		}
		log.info("Saving Model Graph to file {}", f);
		return f;
	}

	@Override public CommandWithParams<ApplicationArguments> prepareInput(ApplicationArguments args) {
		return new CommandWithParams<>(this, args, findCommand(args, MODEL_GRAPH_COMMAND) != null);
	}

	@Override public IHelpInfo getHelp() {
		return new IHelpInfo() {

			@Override public Map<String, String> getOptions() {
				return Map.of(
					"output", "Puts retrieved Model Graph to file"
				);
			}

			@Override public String getName() {
				return MODEL_GRAPH_COMMAND;
			}

			@Override public List<String> getExamples() {
				return List.of(
					"%s %s".formatted(HelpCommand.JAVA_COMMAND, MODEL_GRAPH_COMMAND),
					"%s %s --output=./example/output.json".formatted(HelpCommand.JAVA_COMMAND, MODEL_GRAPH_COMMAND)
				);
			}
		};
	}

	private static Optional<String> resolveOutputArgument(final ApplicationArguments args) {
		Optional<String> outputPath = Optional.empty();
		String outputFilePath = "output";

		if (args.getOptionNames().contains(outputFilePath) && !args.getOptionValues(outputFilePath).isEmpty()) {
			outputPath = Optional.of(args.getOptionValues(outputFilePath).getFirst());
		}

		return outputPath;
	}
}
