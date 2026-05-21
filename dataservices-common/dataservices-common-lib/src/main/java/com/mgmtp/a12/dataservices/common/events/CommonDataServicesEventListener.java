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
package com.mgmtp.a12.dataservices.common.events;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;

/**
 * An {@link EventListener} that is listened to only events which come from the same application context.
 * If the event's source does not match the application context, the event is discarded.
 **/
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener
public @interface CommonDataServicesEventListener {
	/**
	 * Alias for {@link #classes}.
	 */
	@AliasFor(annotation = EventListener.class, attribute = "classes")
	Class<?>[] value() default {};

	/**
	 * The event classes that this listener handles.
	 * If this attribute is specified with a single value, the annotated
	 * method may optionally accept a single parameter. However, if this
	 * attribute is specified with multiple values, the annotated method
	 * must _not_ declare any parameters.
	 */
	@AliasFor(annotation = EventListener.class, attribute = "classes")
	Class<?>[] classes() default {};

	/**
	 * Spring Expression Language (SpEL) attribute used for making the event
	 * handling conditional.
	 * The default is `""`, meaning the event is always handled.
	 *
	 * @see EventListener#condition
	 */
	@AliasFor(annotation = EventListener.class, attribute = "condition")
	String condition() default "";

	/**
	 * An optional identifier for the listener, defaulting to the fully-qualified
	 * signature of the declaring method (e.g. "mypackage.MyClass.myMethod()").
	 *
	 * @see EventListener#id
	 */
	@AliasFor(annotation = EventListener.class, attribute = "id")
	String id() default "";
}
