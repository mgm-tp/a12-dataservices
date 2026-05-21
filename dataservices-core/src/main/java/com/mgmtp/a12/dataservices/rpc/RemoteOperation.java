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
package com.mgmtp.a12.dataservices.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Identifies a RPC operation and define its metadata.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteOperation {

	/**
	 * Unique operation name. required parameter. Used to look-up proper implementation of the Operation
	 */
	String name();

	/**
	 * The group of operation, optional parameter.
	 * This field serves as a convenient way to enable/disable operations in batch.
	 */
	String group() default "";

	/**
	 * Helper for working with {@link RemoteOperation}-annotated types.
	 */
	@NoArgsConstructor(access = AccessLevel.NONE)
	class RemoteOperationHelper {
		/**
		 * Returns the operation identifier declared by {@link RemoteOperation#name()} on the given class.
		 *
		 * @param cl The class to inspect; must not be `null`.
		 * @return The operation id, or an empty string if the annotation is missing.
		 */
		public static String getOperationId(Class<?> cl) {
			return Optional.ofNullable(AnnotationUtils.findAnnotation(cl, RemoteOperation.class))
				.map(RemoteOperation::name)
				.orElse("");
		}
	}
}
