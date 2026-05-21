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
package com.mgmtp.a12.dataservices.gradle.java

import com.mgmtp.a12.dataservices.gradle.JavaOptsConstants
import com.mgmtp.a12.dataservices.gradle.dependency.DependencyManagementPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.toolchain.JavaLanguageVersion

class JavaConventionsPlugin implements Plugin<Project> {

	public static final String DEFAULT_JDK_VERSION = JavaVersion.VERSION_21;

	@Override
	void apply(final Project project) {
		project.pluginManager.apply('java-library')
		project.pluginManager.apply(DependencyManagementPlugin)
		project.pluginManager.apply(project.libs.plugins.lombokPlugin.get().pluginId)

		String javacVersion = project.findProperty("jdk.javac") ?: DEFAULT_JDK_VERSION;
		String javaVersion = project.findProperty("jdk.java") ?: DEFAULT_JDK_VERSION;

		project.java {
			toolchain {
				languageVersion = JavaLanguageVersion.of(javacVersion)
			}
		}

		project.dependencies {
			annotationProcessor project.libs.springBootConfigurationProcessor
		}

		project.tasks.withType(JavaCompile).configureEach {
			options.encoding = 'UTF-8'
			options.compilerArgs << '-parameters'
			javaCompiler = project.javaToolchains.compilerFor {
				languageVersion = JavaLanguageVersion.of(javacVersion)
			}
		}

		project.test {
			useTestNG()
			jvmArgs += JavaOptsConstants.TEST_ARGUMENTS
		}

		project.tasks.withType(Test).configureEach {

			it.javaLauncher.set project.javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(javaVersion) }

			it.testLogging {
				// set options for log level LIFECYCLE
				events TestLogEvent.FAILED,
						TestLogEvent.PASSED,
						TestLogEvent.SKIPPED

				exceptionFormat = TestExceptionFormat.FULL
				showExceptions = true
				showCauses = true
				showStackTraces = true

				// set options for log level DEBUG and INFO
				debug {
					it.events TestLogEvent.STARTED,
							TestLogEvent.FAILED,
							TestLogEvent.PASSED,
							TestLogEvent.SKIPPED,
							TestLogEvent.STANDARD_ERROR,
							TestLogEvent.STANDARD_OUT
					it.exceptionFormat = TestExceptionFormat.FULL
				}
				info.events = debug.events
				info.exceptionFormat = debug.exceptionFormat

				afterSuite { desc, result ->
					if (!desc.parent) { // will match the outermost suite
						def output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
						def startItem = '|  ', endItem = '  |'
						def repeatLength = startItem.length() + output.length() + endItem.length()
						println('\n' + ('-' * repeatLength) + '\n' + startItem + output + endItem + '\n' + ('-' * repeatLength))
					}
				}
			}
		}

		project.tasks.withType(JavaCompile).configureEach {
			it.javaCompiler.set project.javaToolchains.compilerFor { languageVersion = JavaLanguageVersion.of(javacVersion) }
		}
	}
}
