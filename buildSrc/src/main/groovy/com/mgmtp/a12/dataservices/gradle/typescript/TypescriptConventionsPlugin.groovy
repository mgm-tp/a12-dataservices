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
package com.mgmtp.a12.dataservices.gradle.typescript

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider

import static org.gradle.api.plugins.BasePlugin.BUILD_GROUP
import static org.gradle.language.base.plugins.LifecycleBasePlugin.*

/**
 * Gradle plugin that applies TypeScript conventions: Node setup, lint/test/build tasks,
 * license checks, Typedoc generation, and publishing configuration.
 */
class TypescriptConventionsPlugin implements Plugin<Project> {

	/**
	 * {@inheritDoc}
	 * Configures Node plugin and registers tasks for linting, compiling, testing, documentation,
	 * SBOM integration, and publishing for TypeScript modules.
	 *
	 * @param project the target Gradle project.
	 */
	@Override
	void apply(final Project project) {

		VersionCatalog libs = project.extensions.getByType(VersionCatalogsExtension).named('libs')
		project.pluginManager.apply(libs.findPlugin('nodePlugin').map { it.get() }.map { it.pluginId }.orElseThrow())

		TypescriptConventionsExtension ext = project.extensions.create(TypescriptConventionsExtension.NAME, TypescriptConventionsExtension)

		NodeExtension nodeExt = project.extensions.getByType(NodeExtension)

		if (!(System.getenv("NODE_HOME") && new File("${System.getenv("NODE_HOME")}/bin/npm").exists())) {
			nodeExt.download.set(true)
			nodeExt.version.set(project.property("node.version") as String)
			nodeExt.npmVersion.set(project.property("npm.version") as String)
		}

		project.configurations { compileNode.transitive = true }

		TaskCollection<NpmInstallTask> npmInstall = project.tasks.withType(NpmInstallTask)
		npmInstall.configureEach { NpmInstallTask npmInstallTask ->
			npmInstallTask.args.set(['--legacy-peer-deps'])
		}

		if (!(project.tasks.find { it.name == CLEAN_TASK_NAME })) project.tasks.register(CLEAN_TASK_NAME, Delete) {
			it.group = BUILD_GROUP
			it.delete project.buildDir
		}
		if (!(project.tasks.find { it.name == ASSEMBLE_TASK_NAME })) project.tasks.register(ASSEMBLE_TASK_NAME) { group = BUILD_GROUP }
		if (!(project.tasks.find { it.name == CHECK_TASK_NAME })) project.tasks.register(CHECK_TASK_NAME) { group = VERIFICATION_GROUP }
		if (!(project.tasks.find { it.name == 'test' })) project.tasks.register('test') { group = VERIFICATION_GROUP }
		if (!(project.tasks.find { it.name == BUILD_TASK_NAME })) project.tasks.register(BUILD_TASK_NAME) { group = BUILD_GROUP }
		project.tasks.build.dependsOn project.tasks.assemble, project.tasks.check, project.tasks.test

		if (!System.getenv('CI') && (Os.isFamily(Os.FAMILY_UNIX))) {
			// Setting incremental build for npmInstall takes a long time for the first time, but dramatically speeds up other tasks.
			// So it is recommended to set up on developer workstation, where you are repeating clean and build tasks,
			// but slows down CI build where all is run only once.
			npmInstall.configureEach {
				it.inputs.file 'package.json'
				it.outputs.dir 'node_modules'
			}
		}

		project.clean {
			project.delete project.buildDir, 'lib', project.fileTree(dir: '.', include: ['LINT-*.xml', 'TEST-*.xml']), 'doc'
		}

		TaskProvider<NpmTask> lintTypescriptTask = project.tasks.register('lintTypescript', NpmTask) {
			it.args.set(['run', 'initialize'])
			it.dependsOn npmInstall
		}

		project.tasks.register("versionNpm", NpmTask) {
			it.args.set(['version'])
			it.dependsOn npmInstall
		}

		project.tasks.register('versionNode', NpmTask) {
			it.args.set(['-version'])
			it.dependsOn npmInstall
		}

		TaskProvider<NpmTask> compileTypescript = project.tasks.register('compileTypescript', NpmTask) { NpmTask compileTypescript ->
			compileTypescript.args.set(['run', 'compile'])
			compileTypescript.inputs.dir 'src'
			compileTypescript.inputs.file 'package.json'
			compileTypescript.outputs.dirs 'lib', 'build'
			compileTypescript.dependsOn npmInstall
		}

		project.tasks.check.dependsOn project.tasks.register('testTypescript', NpmTask) {
			it.args.set(['run', 'test'])
			it.inputs.files project.fileTree('src'), project.fileTree('lib')
			it.inputs.file 'package.json'
			it.outputs.files project.fileTree(dir: "${project.buildDir}/tests", include: 'TEST*.xml')
			it.dependsOn project.tasks.compileTypescript
			it.mustRunAfter project.tasks.compileTypescript
		}

		project.tasks.register('distClean', Delete) {
			it.followSymlinks = false
			it.delete 'node_modules', 'package-lock.json'
			it.dependsOn project.tasks.clean
		}

		project.tasks.assemble.dependsOn compileTypescript
	}
}
