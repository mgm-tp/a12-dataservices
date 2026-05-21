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
package com.mgmtp.a12.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import ch.qos.logback.classic.spi.ILoggingEvent;

import com.mgmtp.a12.examples.migration.ExamplesTestConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@SpringBootTest(classes = { ExampleInitApplication.class, ExamplesTestConfiguration.class })
public class ExampleInitApplicationIT extends AbstractTestNGSpringContextTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private LogCaptureListener logCaptureListener;


	@Test
	void contextLoads_applicationContextNotNull_success() {
		Assert.assertNotNull(applicationContext);
	}

	@Test
	public void captureMigrationLog_stepsOrdered_success() {
		List<ILoggingEvent> logs = logCaptureListener.getLogAppender().list;

		String step1Msg = "Running the first migration of the version 34.0.0 (model update) according to the Order defined - using the annotation @MigrationTask here";
		String step2Msg = "Running the second migration of the version 34.0.0 (field update) according to the Order defined. Migration of public method here, it's optional to annotate with @MigrationTask";
		String step3Msg = "Running the migration check of the version 34.0.0. It will run every time the init app is started after all ordered tasks, since it has the runAlways flag as true";

		Map<String, Integer> stepIndices = new HashMap<>();
		IntStream.range(0, logs.size())
			.forEach(i -> {
				String message = logs.get(i).getFormattedMessage();
				if (message.contains(step1Msg) ) {
					stepIndices.put(step1Msg, i);
				} else if (message.contains(step2Msg)) {
					stepIndices.put(step2Msg, i);
				} else if (message.contains(step3Msg)) {
					stepIndices.put(step3Msg, i);
				}
			});

		Assert.assertTrue(stepIndices.containsKey(step1Msg), "Step1 log not found");
		Assert.assertTrue(stepIndices.containsKey(step2Msg), "Step2 log not found");
		Assert.assertTrue(stepIndices.containsKey(step3Msg), "Step3 log not found");

		int step1Index = stepIndices.get(step1Msg);
		int step2Index = stepIndices.get(step2Msg);
		int step3Index = stepIndices.get(step3Msg);

		Assert.assertTrue(step1Index < step2Index,
			String.format("Step1 (index %d) should come before Step2 (index %d)", step1Index, step2Index));
		Assert.assertTrue(step2Index < step3Index,
			String.format("Step2 (index %d) should come before Step3 (index %d)", step2Index, step3Index));
	}
}
