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
package com.mgmtp.a12.dataservices.gradle.dependency


import com.mgmtp.a12.dataservices.gradle.common.VersionUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

class DependencyManagementPlugin implements Plugin<Project> {

	public static final String DEPENDENCY_MANAGEMENT_TASK_NAME = 'dependencyManagement'

	@Override
	void apply(final Project project) {
		def dependencyExtension = project.extensions.create(DependencyExtension.NAME, DependencyExtension)
		def embeddedPostgresServer = project.extensions.getByType(VersionCatalogsExtension)
				.named('libs')
				.findLibrary('embeddedPostgresServer')
				.orElseThrow()

		configureDependencyManagementTask(project, dependencyExtension)

		project.dependencies {
			def applyVersion = { version, body -> body(version) }

			api platform(project.libs.springFrameworkBom)
			api platform(project.libs.springSecurityBom)
			api platform(project.libs.jacksonBom)

			api platform(project.libs.kernelBom)
			api platform(project.libs.baseBom)
			api platform(project.libs.uaaBom)

		}

		project.configurations {
			all*.exclude group: 'com.mgmtp.a12.utils', module: 'utils-utf8-resource-bundle'
			// This causes duplicate classes with `com.mgmtp.a12:utf8-resource-bundle:3.0.0`
			all*.exclude group: 'javax.activation', module: 'javax.activation-api'
			all*.exclude group: 'javax.inject', module: 'javax.inject'
			all*.exclude group: 'com.sun.xml.bind', module: 'jaxb-core'
			all*.exclude group: 'org.glassfish.jaxb', module: 'jaxb-runtime'
			all*.exclude group: 'org.codehaus.woodstox', module: 'woodstox-core-asl'
			all*.exclude group: 'commons-logging', module: 'commons-logging'
			all*.exclude group: 'com.zaxxer', module: 'HikariCP-java7'
			all*.exclude group: 'com.vaadin.external.google', module: 'android-json'
			all*.exclude group: 'org.slf4j', module: 'jcl-over-slf4j'
			all*.exclude group: 'org.glassfish.hk2.external', module: 'aopalliance-repackaged'
			all*.exclude group: 'org.eclipse.jetty.toolchain', module: 'jetty-servlet-api'
			all*.exclude group: 'org.apache.logging.log4j', module: 'log4j-slf4j2-impl'
			all*.exclude group: 'xerces', module: 'xercesImpl'
			compileClasspath.exclude group: 'org.apache.tomcat.embed', module: 'tomcat-embed-core'
			runtimeClasspath.exclude group: 'jakarta.servlet', module: 'jakarta.servlet-api'

			implementation.exclude group: 'org.slf4j', module: 'slf4j-simple'
			api.exclude group: 'org.slf4j', module: 'slf4j-simple'
			all*.resolutionStrategy {
				componentSelection {
					all {
						candidate.group == 'commons-io' && candidate.module == 'commons-io' && candidate.version =~ /20\d{6}\.\d+/ && reject("Wrong commons-io version")
					}
				}

				force 'javax.servlet:javax.servlet-api:3.0.1'
			}
		}

		project.configurations.configureEach {
			it.resolutionStrategy.eachDependency { DependencyResolveDetails details ->
				if (details.requested.group == 'io.zonky.test.postgres') {
					details.useVersion "${embeddedPostgresServer.get().version}"
				}
			}
		}
	}

	static void configureDependencyManagementTask(Project project, DependencyExtension dependencyExtension) {
		def dependencyManagementTask = project.tasks.register(DEPENDENCY_MANAGEMENT_TASK_NAME) {
			group = LifecycleBasePlugin.VERIFICATION_GROUP
			description = "Dependency Version Evaluation"
			doLast {
				evaluateDependenciesVersion(project, dependencyExtension)
			}
		}

		project.getTasksByName(LifecycleBasePlugin.CHECK_TASK_NAME, false)*.dependsOn dependencyManagementTask
	}

	static void evaluateDependenciesVersion(Project project, DependencyExtension extension) {
		project.configurations.named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME).get().resolvedConfiguration.resolvedArtifacts.each { art ->
			def dependency = art.getModuleVersion().id
			extension.versionEvaluations.each { evalVersion ->
				if ((evalVersion.get(0) == "below" && !VersionUtils.belowVersion(dependency, evalVersion.get(1), evalVersion.get(2), evalVersion.get(3)))
						|| (evalVersion.get(0) == "equal" && !VersionUtils.equalVersion(dependency, evalVersion.get(1), evalVersion.get(2), evalVersion.get(3)))
						|| (evalVersion.get(0) == "above" && !VersionUtils.aboveVersion(dependency, evalVersion.get(1), evalVersion.get(2), evalVersion.get(3)))) {
					throw new GradleException("Keep ${evalVersion.get(1)}:${evalVersion.get(2)} ${evalVersion.get(0)} ${evalVersion.get(3)}, current using version is ${dependency.getVersion()}")
				}
			}
		}
	}
}
