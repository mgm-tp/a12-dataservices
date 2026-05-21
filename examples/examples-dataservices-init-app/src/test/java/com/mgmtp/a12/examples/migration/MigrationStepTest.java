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
package com.mgmtp.a12.examples.migration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class MigrationStepTest {

	private ListAppender<ILoggingEvent> logAppender;
	private Logger logger;

	@DataProvider(name = "migrationSteps")
	public Object[][] migrationStepsProvider() {
		return new Object[][] {
			{
				MigrationStepOne.class,
				new MigrationStepOne(),
				"34.0.0",
				"MigrationStepOne",
				"This is an example of a migration step",
				1,
				"migrationOne",
				false,
				"Running the first migration of the version 34.0.0 (model update) according to the Order defined - using the annotation @MigrationTask here"
			},
			{
				MigrationStepTwo.class,
				new MigrationStepTwo(),
				"34.0.0",
				"MigrationStepTwo",
				"This is an example of a migration step",
				2,
				"migrationTwo",
				false,
				"Running the second migration of the version 34.0.0 (field update) according to the Order defined. Migration of public method here, it's optional to annotate with @MigrationTask"
			},
			{
				MigrationStepThree.class,
				new MigrationStepThree(),
				"34.0.0",
				"MigrationStepThree",
				"This is an example of a migration step",
				null,
				"migrationThree",
				true,
				"Running the migration check of the version 34.0.0. It will run every time the init app is started after all ordered tasks, since it has the runAlways flag as true"
			}
		};
	}

	@Test(dataProvider = "migrationSteps")
	void classAnnotations_presentAndCorrect(Class<?> clazz, Object instance, String version,
		String name, String description, Integer order,
		String methodName, boolean runAlways, String logMessage) {
		ExtendedServerMigrationStep annotation = clazz.getAnnotation(ExtendedServerMigrationStep.class);

		assertNotNull(annotation, "Class should have @ExtendedServerMigrationStep annotation");
		assertEquals(annotation.version(), version);
		assertEquals(annotation.name(), name);
		assertEquals(annotation.description(), description);

		Order orderAnnotation = clazz.getAnnotation(Order.class);
		if (order == null) {
			assertNull(orderAnnotation);
		} else {
			assertNotNull(orderAnnotation, "Class should have @Order annotation");
			assertEquals(orderAnnotation.value(), order);
		}
	}

	@Test(dataProvider = "migrationSteps")
	void logging_singleInfoMessageWithExpectedText(Class<?> clazz, Object instance, String version,
		String name, String description, Integer order,
		String methodName, boolean runAlways, String logMessage) throws Exception {
		logger = (Logger) LoggerFactory.getLogger(clazz);
		logAppender = new ListAppender<>();
		logAppender.start();
		logger.addAppender(logAppender);

		try {
			clazz.getMethod(methodName).invoke(instance);

			List<ILoggingEvent> logs = logAppender.list;
			assertEquals(logs.size(), 1, "Should have exactly one log entry");
			assertEquals(logs.get(0).getLevel(), Level.INFO);
			assertEquals(logs.get(0).getFormattedMessage(), logMessage);
		} finally {
			logger.detachAppender(logAppender);
		}
	}

	@Test(dataProvider = "migrationSteps")
	void methodAnnotation_presentAndCorrect(Class<?> clazz, Object instance, String version,
		String name, String description, Integer order,
		String methodName, boolean runAlways, String logMessage) throws Exception {
		var method = clazz.getMethod(methodName);
		var migrationTaskAnnotation = method.getAnnotation(com.mgmtp.a12.dataservices.migration.MigrationTask.class);

		assertNotNull(migrationTaskAnnotation, "Method should have @MigrationTask annotation");
		assertEquals(migrationTaskAnnotation.name(), methodName);
		assertEquals(migrationTaskAnnotation.runAlways(), runAlways);
	}
}
