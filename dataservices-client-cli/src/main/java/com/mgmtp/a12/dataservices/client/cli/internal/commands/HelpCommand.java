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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.client.cli.internal.IApplicationOutput;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.client.cli.internal.commands.AbstractRPCCommand.EXIT_INVALID_ARGS;

@Component public class HelpCommand implements ICommandProcessor<ApplicationArguments> {

	public static final String JAVA_COMMAND = "java -jar dataservices-client-cli-VERSION-fatjar.jar";
	public static final String USAGE_MSG = "Usage: " + JAVA_COMMAND + " COMMAND OPTIONS ARGUMENTS\n";
	private final Optional<List<ICommandProcessor<?>>> commandProcessors;
	private final IApplicationOutput applicationOutput;

	public HelpCommand(@NonNull Optional<List<ICommandProcessor<?>>> commandProcessors, @NonNull IApplicationOutput applicationOutput) {
		this.commandProcessors = commandProcessors;
		this.applicationOutput = applicationOutput;
	}

	@Override public CommandWithParams<ApplicationArguments> prepareInput(ApplicationArguments args) {
		boolean isApplicable =
			Arrays.stream(args.getSourceArgs()).anyMatch(o -> o.startsWith("-h")) || args.getNonOptionArgs().stream().anyMatch("help"::equals);
		ApplicationArguments argsWithoutHelp = new DefaultApplicationArguments(
			Arrays.stream(args.getSourceArgs()).filter(a -> !a.startsWith("-h")).filter(a -> !"help".equals(a)).toArray(String[]::new));
		return new CommandWithParams<>(this, argsWithoutHelp, isApplicable);
	}

	@Override public CommandResponse<ApplicationArguments> execute(ApplicationArguments args) {
		applicationOutput.getDialogOutput().print(USAGE_MSG);
		commandProcessors.stream().flatMap(Collection::stream)
			.map(c -> c.prepareInput(args))
			.filter(CommandWithParams::isApplicable)
			.map(CommandWithParams::getCommand)
			.map(ICommandProcessor::getHelp)
			.sorted(Comparator.comparing(IHelpInfo::getName))
			.sequential()
			.peek(this::printHelp)
			.findAny().orElseGet(() -> {
				Stream.concat(commandProcessors.stream().flatMap(Collection::stream), Stream.of(this))
					.map(ICommandProcessor::getHelp)
					.sorted(Comparator.comparing(IHelpInfo::getName))
					.sequential()
					.forEach(this::printHelp);
				return null;
			});
		return new CommandResponse<>(EXIT_INVALID_ARGS, this);
	}

	private void printHelp(IHelpInfo info) {
		PrintWriter dialogOutput = applicationOutput.getDialogOutput();
		dialogOutput.printf("%nCommand [%s]%n", info.getName());
		if (MapUtils.isNotEmpty(info.getOptions())) {
			dialogOutput.println("  Options:");
			info.getOptions().entrySet().stream()
				.sorted((a, b) -> StringUtils.compare(a.getKey(), b.getKey(), true))
				.forEach(e -> dialogOutput.printf("  %10s   %s%n", e.getKey(), e.getValue()));
		}
		if (MapUtils.isNotEmpty(info.getArguments())) {
			dialogOutput.println("  Arguments:");
			info.getArguments().entrySet().stream()
				.sorted((a, b) -> StringUtils.compare(a.getKey(), b.getKey(), true))
				.forEach(e -> dialogOutput.printf("  %10s   %s%n", e.getKey(), e.getValue()));
		}
		if (CollectionUtils.isNotEmpty(info.getExamples())) {
			dialogOutput.println("  Examples:");
			info.getExamples().forEach(value -> dialogOutput.printf("   %s%n", value));
		}
	}

	@Override public IHelpInfo getHelp() {
		return new IHelpInfo() {

			@Override public String getName() {
				return "help";
			}

			@Override public Map<String, String> getArguments() {
				return Collections.singletonMap("help", "print help message");
			}

			@Override public Map<String, String> getOptions() {
				return Map.of("-h", "print help message");
			}
		};
	}
}
