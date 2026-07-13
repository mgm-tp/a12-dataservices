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
package com.mgmtp.a12.dataservices.server.app;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.Lifecycle;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.web.context.WebApplicationContext;

import com.mgmtp.a12.dataservices.DataServicesApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * Bootstraps the Data Services server application as a Spring Boot application.
 * Provides entry points for standalone execution and WAR deployment via {@link SpringBootServletInitializer}.
 * After startup, the application publishes a {@link ContextStartedEvent} to notify interested listeners.
 */
@Slf4j
@DataServicesApplication public class ServerApplication extends SpringBootServletInitializer {

	/**
	 * Starts the application in standalone mode.
	 * Delegates to {@link #start(String[], Class[])} and publishes {@link ContextStartedEvent} after startup.
	 *
	 * @param args command line arguments; never `null`, may be empty.
	 */
	public static void main(final String[] args) {
		start(args);
	}

	/**
	 * Runs the server application with optional additional source classes.
	 * Publishes {@link ContextStartedEvent} after startup to align behavior with WAR deployment.
	 * If startup fails, logs the error and terminates the JVM with exit code `1`.
	 *
	 * @param args command line arguments; may be empty. Passing `null` is not expected.
	 * @param sources additional Spring sources (e.g., `@Configuration`, `@SpringBootApplication`); may be empty.
	 */
	public static void start(final String[] args, Class<?>... sources) {
		try {
			new SpringApplicationBuilder(ArrayUtils.addAll(new Class[] { ServerApplication.class }, sources))
				.run(args)
				.start();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	/**
	 * Initializes the application when deployed as a WAR to a traditional servlet container.
	 * Invokes {@link Lifecycle#start()} on the {@link WebApplicationContext} to publish {@link ContextStartedEvent}.
	 *
	 * @param application the Spring Boot application to run; never `null`.
	 * @return the initialized {@link WebApplicationContext}.
	 */
	@Override protected WebApplicationContext run(SpringApplication application) {
		WebApplicationContext webApplicationContext = super.run(application);
		if (webApplicationContext instanceof Lifecycle lifecycle) {
			//fire the event.
			lifecycle.start();
		}
		return webApplicationContext;
	}
}
