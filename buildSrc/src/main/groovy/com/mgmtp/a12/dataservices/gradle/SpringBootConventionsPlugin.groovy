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
package com.mgmtp.a12.dataservices.gradle


import com.mgmtp.a12.dataservices.gradle.java.JavaConventionsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

class SpringBootConventionsPlugin implements Plugin<Project> {

	@Override
	void apply(final Project project) {
		VersionCatalogsExtension varsionCatalogs = project.extensions.findByType(VersionCatalogsExtension)
		project.getPluginManager().apply(JavaConventionsPlugin)
		project.getPluginManager().apply(varsionCatalogs.named('libs').findPlugin('springBootPlugin').get().get().pluginId)

		project.tasks.withType(Jar).named('jar').configure {
			it.archiveClassifier.set ''
			it.enabled = true
		}

		project.tasks.withType(BootRun).named('bootRun').configure {
			it.jvmArgs += JavaOptsConstants.HAZELCAST_JVM_ARGUMENTS
			it.systemProperty 'workDir', ''
			it.workingDir project.rootDir.path
		}

		project.tasks.withType(BootJar).named('bootJar').configure {
			it.archiveClassifier.set 'fatjar'
		}
	}
}
