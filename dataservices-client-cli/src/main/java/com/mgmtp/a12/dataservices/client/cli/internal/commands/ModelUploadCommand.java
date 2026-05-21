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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.client.cli.internal.IApplicationOutput;
import com.mgmtp.a12.dataservices.client.cli.internal.resources.DelegatingResourceLoader;
import com.mgmtp.a12.dataservices.client.model.ModelsClient;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.client.cli.internal.commands.HelpCommand.JAVA_COMMAND;

@Slf4j
@Component public class ModelUploadCommand extends AbstractRPCCommand<List<String>> {

	public static final String MODEL_UPLOAD_COMMAND = "model upload";
	private final ModelsClient modelsClient;
	private final DelegatingResourceLoader resourceLoader;

	public ModelUploadCommand(@NonNull ModelsClient modelsClient, @NonNull IApplicationOutput applicationOutput, @NonNull DelegatingResourceLoader resourceLoader) {
		super(applicationOutput);
		this.modelsClient = modelsClient;
		this.resourceLoader = resourceLoader;
	}

	@Override public CommandWithParams<List<String>> prepareInput(ApplicationArguments args) {
		Pair<Integer, Integer> commandPosition = findCommand(args, MODEL_UPLOAD_COMMAND);
		boolean applicable = commandPosition != null;
		return new CommandWithParams<>(this, applicable ? getPaths(args, commandPosition) : List.of(), applicable);
	}

	private static List<String> getPaths(ApplicationArguments args, Pair<Integer, Integer> commandPosition) {
		return args.getNonOptionArgs().subList(commandPosition.getRight(), args.getNonOptionArgs().size());
	}

	protected CommandResponse<List<String>> executeRemoteCommand(List<String> paths) {
		PrintWriter output = new PrintWriter(applicationOutput.getDataOutput());
		try {
			log.info("Importing models: {}", paths);
			if (CollectionUtils.isEmpty(paths)) {
				return new CommandResponse<>(EXIT_INVALID_ARGS, this);
			}
			Stream<InputStream> paramFiles = paths.stream()
				.map(path -> {
					try {
						log.debug(path);
						Resource resource = resourceLoader.getResource(path);
						log.debug("Importing file {}", resource);
						if (path.endsWith(".json")) {
							return zipJson(resource);
						} else {
							return resource.getInputStream();
						}
					} catch (IOException e) {
						log.error("Unable to get data for {}", path);
						return null;
					}
				});
			long importedModelsCount = paramFiles
				.filter(Objects::nonNull)
				.flatMap(is -> modelsClient.importModelBulk(is).stream())
				.peek(output::println)
				.count();
			output.flush();
			return new CommandResponse<>(importedModelsCount > 0 ? EXIT_SUCCESS : EXIT_FAILURE, this);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			PrintWriter pw = applicationOutput.getDialogOutput();
			pw.printf("Failed to upload models: %s%n", e.getMessage());

			printA12ClientException(output, e);

			output.flush();
			return new CommandResponse<>(EXIT_FAILURE, this);
		}
	}

	@SneakyThrows
	private InputStream zipJson(Resource resource) {
		try (
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(bout)
		) {
			ZipEntry ze = new ZipEntry(Optional.of(resource).map(Resource::getFilename).orElse("unnamed_model.json"));
			zos.putNextEntry(ze);
			IOUtils.copy(resource.getInputStream(), zos);
			zos.closeEntry();
			byte[] data = bout.toByteArray();
			if (log.isDebugEnabled()) {
				File tmp = File.createTempFile("dataservices-client-cli-model-upload", ".zip");
				tmp.deleteOnExit();
				FileUtils.writeByteArrayToFile(tmp, data);
				log.debug("Temporary zip file: {}", tmp);
			}
			return new ByteArrayInputStream(data);
		}
	}

	@Override public IHelpInfo getHelp() {
		return new IHelpInfo() {
			@Override public String getName() {
				return MODEL_UPLOAD_COMMAND;
			}

			@Override public Map<String, String> getArguments() {
				return Map.of(
					"MODEL.json", "upload single model",
					"MODEL.zip", "upload all models in zip file");
			}

			@Override public List<String> getExamples() {
				return List.of(
					String.format("%s %s my_model.json", JAVA_COMMAND, MODEL_UPLOAD_COMMAND),
					String.format("%s %s my_models.zip", JAVA_COMMAND, MODEL_UPLOAD_COMMAND)
				);
			}
		};
	}
}
