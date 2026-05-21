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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.migration.ErrorHandling;
import com.mgmtp.a12.dataservices.migration.MigrationStep;

/**
 * Composed annotation for server-side migration steps.
 * Combines {@link MigrationStep} with {@link Component} to register a Spring bean and define execution metadata.
 * Apply this to classes hosting {@link com.mgmtp.a12.dataservices.migration.MigrationTask}-annotated methods.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@MigrationStep(name = "default", version = "default")
@Component
public @interface ExtendedServerMigrationStep {

	/**
	 * The logical identifier of the migration step. Must be unique within a release.
	 * Maps to {@link MigrationStep#name()}.
	 * @return the name of the migration step.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "name") String name();

	/**
	 * Release version label (e.g., "34.0.0") this step belongs to.
	 * Maps to {@link MigrationStep#version()}.
	 * @return the version of the migration step.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "version") String version();

	/**
	 * Author or ticket reference identifying the responsible person or work item. May be empty.
	 * Maps to {@link MigrationStep#author()}.
	 * @return the author of the migration step.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "author") String author() default "";

	/**
	 * Concise English description of what this step does. May be empty.
	 * Maps to {@link MigrationStep#description()}.
	 * @return the description of the migration step.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "description") String description() default "";

	/**
	 * Controls whether the step runs on every startup regardless of previous execution.
	 * Maps to {@link MigrationStep#runAlways()}.
	 * @return true if the step should always run, false otherwise.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "runAlways") boolean runAlways() default false;

	/**
	 * Error handling strategy applied when execution of this step fails.
	 * Maps to {@link MigrationStep#onFailure()}.
	 * @return the error handling strategy.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "onFailure") ErrorHandling onFailure() default ErrorHandling.HALT;

	/**
	 * Overrides the class name recorded as the executor for audit purposes. May be empty to use the runtime type.
	 * Maps to {@link MigrationStep#executedClassName()}.
	 * @return the executed class name.
	 */
	@AliasFor(annotation = MigrationStep.class, attribute = "executedClassName") String executedClassName() default "";
}

