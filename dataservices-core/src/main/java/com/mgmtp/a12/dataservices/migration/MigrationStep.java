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
package com.mgmtp.a12.dataservices.migration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class which is responsible for Document or Model migration. In fact it can be any kind of migration. It
 * automatically publish this class to a spring
 * container and process all injects.
 * 
 * Migration is executed when a spring application is completely bootstrapped and you are free to use any kind of an
 * API/Beans which are provided by the Data Services.
 * 
 * A migration class doesn't have any specific constraints. All non-arguments public/protected methods will be executed.
 * 
 * Base unit of the migration is a {@link MigrationStep} it has bunch of properties and just {@link #name()} and
 * {@link #version()} is
 * required. The step is represented by an annotated class. Each step has 1 or more tasks.
 * The task is a method in the step class which has no arguments and is annotated by {@link MigrationTask}
 *
 * The task is defined for marking the method as task and for providing some parameters to fine-tune the behavior.
 *
 * - {@link MigrationStep#runAlways()}
 * - {@link MigrationStep#onFailure()}
 * - Ability to redefine task name
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MigrationStep {

	/**
	 * Migration step name.
	 */
	String name();

	/**
	 * Services version for which the step is needed. Format is XX.XX.XX
	 */
	String version();

	/**
	 * Optional author name.
	 */
	String author() default "";

	String description() default "";

	/**
	 * Define if the step should run with each migration or only once.
	 */
	boolean runAlways() default false;

	/**
	 * Define error handling in case of migration failure.
	 */
	ErrorHandling onFailure() default ErrorHandling.HALT;

	/**
	 * Annotated class name is a part of migration step identifier. Thus, moving class or changing its name will change the id which will cause migration step to be executed again.
	 * To avoid above-mentioned problem you may use this optional parameter to use previous class name to match already executed migration steps.
	 */
	String executedClassName() default "";
}
