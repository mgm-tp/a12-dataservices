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
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.client.cli.internal.IApplicationOutput;
import com.mgmtp.a12.dataservices.client.cli.internal.resources.DelegatingResourceLoader;
import com.mgmtp.a12.dataservices.client.rpc.RpcOperationsClient;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Request;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component public class JsonRpcCommand extends AbstractRPCCommand<ApplicationArguments> {

	public static final String JSON_RPC_COMMAND = "json rpc";
	public static final String PATH_OPTION_NAME = "input_path";
	public static final String OPTION_OUTPUT_DIR = "output_dir";

	private final RpcOperationsClient rpcOperationsClient;
	private final DelegatingResourceLoader resourceLoader;
	private final ObjectMapper objectMapper;

	public JsonRpcCommand(IApplicationOutput applicationOutput, RpcOperationsClient rpcOperationsClient, DelegatingResourceLoader resourceLoader,
		ObjectMapper objectMapper) {
		super(applicationOutput);
		this.rpcOperationsClient = rpcOperationsClient;
		this.resourceLoader = resourceLoader;
		this.objectMapper = objectMapper.rebuild().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).build();
	}

	@Override protected CommandResponse<ApplicationArguments> executeRemoteCommand(ApplicationArguments args) {
		try {
			if (!args.containsOption(PATH_OPTION_NAME)
				|| !args.containsOption(OPTION_OUTPUT_DIR)) {
				return new CommandResponse<>(EXIT_INVALID_ARGS, this);
			}

			String pathArg = args.getOptionValues(PATH_OPTION_NAME).getFirst();
			String outputDir = args.getOptionValues(OPTION_OUTPUT_DIR).getFirst();

			getAllFiles(new File(resourceLoader.getResource(pathArg).getURI()))
				.sorted(Comparator.comparing(File::getPath))
				.forEachOrdered(f -> {
					try {
						List<JsonRpc2Request> request = objectMapper.readValue(f, new TypeReference<>() {});
						List<JsonRpc2Response> response = rpcOperationsClient.invoke(request);
						assertResponseHasNoErrors(f, response);
						writeResponse(buildOutputPath(f, pathArg, outputDir), response);
					} catch (IOException e) {
						log.warn("Error while deserializing file '{}'", f.getPath(), e);
					}
				});

			return new CommandResponse<>(EXIT_SUCCESS, this);
		} catch (Exception e) {
			log.error("Exception occurred: ", e);
			return new CommandResponse<>(EXIT_FAILURE, this);
		}
	}

	private void assertResponseHasNoErrors(File file, List<JsonRpc2Response> response) {
		response.stream()
			.filter(r -> !r.isSuccess())
			.map(JsonRpc2Response::getError)
			.map(JsonRpc2ResponseError::toString)
			.forEach(e -> {
				log.error("Json-rpc error for path '{}': {}", file, e);
				throw JsonRpcCommandException.of(e, file.getPath());
			});

		if (log.isDebugEnabled()) {
			log.debug("Json-rpc success for path '{}': {}", file, response);
		} else {
			log.info("Json-rpc success for path '{}'", file);
		}
	}

	@Override public CommandWithParams<ApplicationArguments> prepareInput(ApplicationArguments args) {
		return new CommandWithParams<>(this, args, findCommand(args, JSON_RPC_COMMAND) != null);
	}

	@Override public IHelpInfo getHelp() {
		return new IHelpInfo() {
			@Override public Map<String, String> getOptions() {
				return Map.of(
					PATH_OPTION_NAME, "json file or folder to the files to execute the json-rpc requests",
					OPTION_OUTPUT_DIR, "output directory to store the responses of the requests"
				);
			}

			@Override public String getName() {
				return JSON_RPC_COMMAND;
			}

			@Override public List<String> getExamples() {
				return List.of(
					"%s %s --%s=my_request.json --%s=my_output_dir".formatted(HelpCommand.JAVA_COMMAND, JSON_RPC_COMMAND, PATH_OPTION_NAME,
						OPTION_OUTPUT_DIR),
					"%s %s --%s=my_folder --%s=my_output_dir".formatted(HelpCommand.JAVA_COMMAND, JSON_RPC_COMMAND, PATH_OPTION_NAME, OPTION_OUTPUT_DIR)
				);
			}
		};
	}

	private void writeResponse(String outputPath, List<JsonRpc2Response> responses) {
		File newFile = new File(outputPath);
		try (PrintStream out = new PrintStream(new FileOutputStream(newFile))) {
			out.print(objectMapper.writeValueAsString(responses.size() == 1 ? responses.getFirst() : responses));
		} catch (IOException e) {
			log.error("Error while writing response to file '{}'", outputPath, e);
			throw JsonRpcCommandException.of(e.getMessage(), outputPath);
		}
	}

	String buildOutputPath(File file, String inputBaseDir, String outputBaseDir) throws IOException {
		File inputBaseDirFile = new File(inputBaseDir).getCanonicalFile();
		File outputDir = new File(outputBaseDir).getCanonicalFile();
		if (inputBaseDirFile.isDirectory()) {
			Path relativePathBetweenFileAndItsBaseDir = inputBaseDirFile.toPath().relativize(file.getParentFile().getCanonicalFile().toPath());
			outputDir = outputDir.toPath().resolve(relativePathBetweenFileAndItsBaseDir).toFile();
		}

		outputDir.mkdirs();
		return "%s/output-%s".formatted(outputDir.getCanonicalPath(), file.getName());
	}

	private Stream<File> getAllFiles(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			return Arrays.stream(files).flatMap(this::getAllFiles);
		} else {
			return Stream.of(file);
		}
	}
}
