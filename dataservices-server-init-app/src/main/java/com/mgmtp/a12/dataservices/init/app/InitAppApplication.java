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
package com.mgmtp.a12.dataservices.init.app;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import org.springframework.boot.ResourceBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.mgmtp.a12.dataservices.DataServicesApplication;
import com.mgmtp.a12.dataservices.autoconfigure.DataServicesCoreAutoconfiguration;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializes Data Services without starting a web server.
 * Loads core configuration and performs initialization tasks, then terminates the JVM.
 * Configures {@link org.springframework.boot.WebApplicationType#NONE} and a synchronous task executor to avoid lingering threads.
 */
@Slf4j
@PropertySource("classpath:dataservices-init-app-default.properties")
@ComponentScan(basePackages = { DataServicesCoreProperties.DS_PACKAGE_PREFIX }, excludeFilters = {
	@ComponentScan.Filter(pattern = DataServicesCoreProperties.DS_PACKAGE_PREFIX + ".server.*", type = FilterType.REGEX),
	@ComponentScan.Filter(pattern = DataServicesCoreProperties.DS_PACKAGE_PREFIX + ".client.*", type = FilterType.REGEX),
	@ComponentScan.Filter(pattern = "com.mgmtp.a12.connector.*", type = FilterType.REGEX),
	@ComponentScan.Filter(pattern = "com.mgmtp.a12.uaa.client.*", type = FilterType.REGEX),
	@ComponentScan.Filter(pattern = "orq.quartz.*", type = FilterType.REGEX)
})
@DataServicesApplication public class InitAppApplication {

	/**
	 * Runs the init application with the provided command line arguments.
	 * Delegates to {@link #run(String[], Class[])}.
	 *
	 * @param args command line arguments; may be empty.
	 */
	public static void main(String[] args) {
		run(args);
	}

	/**
	 * Runs the init application with the given command line arguments and source classes.
	 * Builds a {@link org.springframework.boot.SpringApplication} with a banner, disables the web environment,
	 * and terminates the JVM using {@link java.lang.System#exit(int)} with the application exit code.
	 *
	 * @param args command line arguments; may be empty.
	 * @param sources optional additional configuration classes; may be `null`.
	 */
	public static void run(String[] args, Class<?>... sources) {
		Class<?>[] sourceClasses = combineSourceClasses(sources);

		SpringApplication app = new SpringApplicationBuilder(new DefaultResourceLoader(), sourceClasses)
			.build();

		ResourceLoader resourceLoader = app.getResourceLoader();
		Resource resource = resourceLoader.getResource("banner-init-app.txt");
		app.setBanner(new ResourceBanner(resource));
		app.setWebApplicationType(WebApplicationType.NONE);
		app.setLogStartupInfo(false);

		try {
			System.exit(SpringApplication.exit(app.run(args)));
		} catch (Throwable e) {
			log.error("Data Services init application exited with error:", e);
			System.exit(1);
		}

	}

	private static Class<?>[] combineSourceClasses(Class<?>[] sources) {
		List<Class<?>> sourceList = new LinkedList<>();
		sourceList.add(InitAppApplication.class);

		if (Objects.nonNull(sources)) {
			sourceList.addAll(Arrays.asList(sources));
		}

		Class<?>[] sourceArr = new Class[sourceList.size()];
		sourceList.toArray(sourceArr);

		return sourceArr;
	}

	/*
	 * @Import is applied on static inner class on purpose, this is used to maintain correct property sources ordering.
	 * See this for an explanation https://github.com/spring-projects/spring-framework/issues/15042?focusedCommentId=101393&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#issuecomment-453401972
	 */
	@Import(DataServicesCoreAutoconfiguration.class)
	@Configuration static class PropertySourcesConfiguration { }


	/**
	 * We need to disable async processing for application to stop, currently only cache preloading is being done asynchronously which does not make any
	 * sense to have in this init application.
	 * @see com.mgmtp.a12.dataservices.utils.internal.CachePreloader
	 *
	 * Since we can't disable async processing which is enabled in {@link DataServicesCoreAutoconfiguration},
	 * we have to configure the task executor with the implementation that executes tasks in sequence.
	 * @see SyncTaskExecutor
	 */
	@EnableAsync
	@Configuration static class SpringAsyncConfig implements AsyncConfigurer {
		/**
		 * Provides a synchronous task executor for this init application.
		 * Executes tasks sequentially on the calling thread to ensure clean shutdown without background work.
		 *
		 * @return a {@link org.springframework.core.task.SyncTaskExecutor} that runs tasks synchronously.
		 */
		@Override public Executor getAsyncExecutor() {
			return new SyncTaskExecutor();
		}
	}
}
