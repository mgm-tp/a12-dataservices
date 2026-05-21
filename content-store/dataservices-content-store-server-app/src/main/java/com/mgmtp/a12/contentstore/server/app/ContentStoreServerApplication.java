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
package com.mgmtp.a12.contentstore.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.web.context.WebApplicationContext;

import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreAutoConfiguration;
import com.mgmtp.a12.contentstore.server.autoconfigure.ContentStoreServerAutoConfiguration;

import lombok.extern.slf4j.Slf4j;

/**
 * Main class for starting up a Content Store server in standalone mode.
 */
@Slf4j
@Import(ContentStoreAutoConfiguration.class)
@SpringBootApplication public class ContentStoreServerApplication extends SpringBootServletInitializer {

	/**
	 * Starts the Content Store Spring Boot application in standalone mode.
	 * After the application context is built, emits a {@link ContextStartedEvent} by invoking {@link Lifecycle#start()} if supported.
	 * Terminates the JVM with exit code `1` on unrecoverable startup errors.
	 *
	 * @param args command-line arguments; may be empty but never `null` as per JVM contract.
	 */
	public static void main(final String[] args) {
		try {
			new SpringApplicationBuilder(ContentStoreServerApplication.class)
				.run(args)
				.start();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	@Import({ ContentStoreServerAutoConfiguration.class })
	@Configuration static class PropertySourcesConfiguration { }

	/**
	 * {@inheritDoc}
	 *
	 * Adds {@link ContentStoreServerApplication} as a primary source when deployed as a WAR.
	 *
	 * @param builder the Spring application builder to configure; never `null`.
	 * @return the configured builder including this application's primary source.
	 */
	@Override protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
		return builder.sources(ContentStoreServerApplication.class);
	}

	/**
	 * Initializes the application when deployed as a WAR and triggers a start event for regular servlet containers.
	 * Delegates to {@link SpringBootServletInitializer#run(SpringApplication)} and then calls {@link Lifecycle#start()}
	 * on the returned {@link WebApplicationContext} if supported.
	 *
	 * @param application the Spring application to run in a servlet environment; never `null`.
	 * @return the initialized web application context; may implement {@link Lifecycle}.
	 */
	@Override
	protected WebApplicationContext run(SpringApplication application) {
		WebApplicationContext webApplicationContext = super.run(application);
		if (webApplicationContext instanceof Lifecycle lifecycle) {
			//fire the event.
			lifecycle.start();
		}
		return webApplicationContext;
	}

}
