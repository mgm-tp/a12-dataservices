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
package com.mgmtp.a12.dataservices.client.cli.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.mgmtp.a12.dataservices.client.cli.internal.configuration.ClientCliConfiguration;
import com.mgmtp.a12.dataservices.client.cli.internal.commands.HelpCommand;
import com.mgmtp.a12.dataservices.client.cli.internal.commands.ICommandProcessor;
import com.mgmtp.a12.dataservices.client.cli.internal.commands.ICommandProcessor.CommandWithParams;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan(basePackages = { "com.mgmtp.a12.dataservices.client" }, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
@SpringBootApplication public class Client implements ApplicationRunner, ExitCodeGenerator {

	private final Optional<List<ICommandProcessor<?>>> commands;

	private final HelpCommand helpCommand;

	private final IApplicationOutput applicationOutput;

	private int exitCode = 0;

	public Client(Optional<List<ICommandProcessor<?>>> commands, HelpCommand helpCommand, IApplicationOutput applicationOutput) {
		this.commands = commands;
		this.helpCommand = helpCommand;
		this.applicationOutput = applicationOutput;
	}

	@Override public void run(ApplicationArguments args) {
		List<ICommandProcessor<?>> commandProcessors = commands.orElseThrow(() -> new UnexpectedException("No commands are available"));
		CommandWithParams<ApplicationArguments> helpCommandInput = helpCommand.prepareInput(args);
		if (helpCommandInput.isApplicable()) {
			exitCode = 2;
		} else {
			exitCode = commandProcessors.stream()
				.map(c -> c.prepareInput(args))
				.filter(CommandWithParams::isApplicable)
				.findAny()
				.map(c -> c.getCommand().execute(c.getParams()))
				.map(ICommandProcessor.CommandResponse::getExitStatus)
				.orElse(2);
		}
		if (exitCode == 2) {
			helpCommand.execute(helpCommandInput.getParams());
		}
		try {
			applicationOutput.flush();
		} catch (IOException e) {
			log.info("Unable to flush application output", e);
			exitCode = 1;
		}
	}

	@Override public int getExitCode() {
		return exitCode;
	}

	public static void main(String[] args) throws IOException {
		SpringApplication app = new SpringApplication(new DefaultResourceLoader(), Client.class);
		ResourceLoader resourceLoader = app.getResourceLoader();
		Resource resource = resourceLoader.getResource("banner-cli.txt");
		app.setBanner(new ResourceBanner(resource));
		app.setBannerMode(Banner.Mode.LOG);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.setLogStartupInfo(false);
		try {
			System.exit(SpringApplication.exit(app.run(args)));
		} catch (Throwable e) {
			log.error("Unable to start the application. Check whether you have connection and other properties configured correctly.", e);
			IApplicationOutput applicationOutput = new ClientCliConfiguration().applicationOutput();
			PrintWriter dialogOutput = applicationOutput.getDialogOutput();
			dialogOutput.println();
			dialogOutput.println("===================================================");
			dialogOutput.println("!!   Unable to start the application             !!");
			dialogOutput.println();
			dialogOutput.println("Read documentation to see how to configure the application properly.");
			dialogOutput.println("You have to setup connection to server.");
			dialogOutput.println();
			dialogOutput.println("Start application with right UAA client configuration properties.");
			dialogOutput.println();
			applicationOutput.flush();
			System.exit(1);
		}
	}
}
