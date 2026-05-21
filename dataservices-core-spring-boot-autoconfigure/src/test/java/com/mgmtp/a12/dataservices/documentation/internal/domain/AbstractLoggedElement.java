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
package com.mgmtp.a12.dataservices.documentation.internal.domain;

import java.beans.IntrospectionException;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Data @RequiredArgsConstructor
public abstract class AbstractLoggedElement {
	@NonNull protected static String tidyUpId(String rule) {
		return rule.replaceAll("\\W", "_");
	}

	public abstract AbstractLoggedElement createReturn() throws IntrospectionException;

	public enum LoggedEventType {OPERATION, SECURITY, EVENT}

	public enum LoggedEventDirection {CALL, RETURN}

	private final LoggedEventType type;
	private final String id;
	private final String name;
	private final Instant started;
	private final LoggedEventDirection direction;
	private final List<StackTraceElement> callHistory;

	private Map<String, ?> properties = new HashMap<>();
	private Instant finished;

	@Override public String toString() {
		if (getDirection() == LoggedEventDirection.CALL) {
			return String.format("%s \"%s\" as %s", getType(), getName(), getId());
		} else {
			return String.format("'return %s \"%s\" as %s\nreturn", getType(), getName(), getId());
		}
	}

	@SneakyThrows public Caller getCaller() {
		StackTraceElement element = callHistory.get(0);
		return new Caller(element.getClassName(), element.getMethodName(), getModifier(element.getClassName(), element.getMethodName()));
	}

	public Caller getInterceptor() {
		return getCallerByType(Caller.CallerModifier.INTERCEPTOR);
	}

	public Caller getPublicCaller() {
		return getCallerByType(Caller.CallerModifier.PUBLIC);
	}

	public Stream<Caller> getCallingMethods() {
		return Stream.of(getPublicCaller(), getInterceptor(), getCaller())
			.filter(Objects::nonNull)
			.distinct();
	}

	@SneakyThrows protected Caller.CallerModifier getModifier(String className, String methodName) {
		return getModifier(Class.forName(className), methodName);
	}

	protected static Caller.CallerModifier getModifier(Class<?> aClass, String methodName) {
		if (Arrays.stream(aClass.getMethods())
			.noneMatch(m -> Objects.equals(methodName, m.getName()) && (Modifier.isProtected(m.getModifiers()) || Modifier.isPublic(m.getModifiers())))) {
			return Caller.CallerModifier.PRIVATE;
		} else if (aClass.getName().contains(".internal")) {
			return Caller.CallerModifier.INTERNAL;
		} else if (isPermissionEvaluator(aClass.getName(), methodName)) {
			return Caller.CallerModifier.INTERCEPTOR;
		} else {
			return Caller.CallerModifier.PUBLIC;
		}
	}

	@Nullable private Caller getCallerByType(Caller.CallerModifier type) {
		return callHistory.stream()
			.map(st -> new Caller(st.getClassName(), st.getMethodName(), getModifier(st.getClassName(), st.getMethodName())))
			.filter(caller -> caller.getModifier() == type)
			.findFirst()
			.orElse(null);
	}

	@SneakyThrows private static boolean isPermissionEvaluator(String className, String methodName) {
		return className.startsWith("com.mgmtp.a12.dataservices.common.events.internal.CommonDataServicesListenerMethodAdapter")
			|| className.startsWith("com.mgmtp.a12.dataservices.common.events.internal.CommonDataServicesEventPublisher")
			|| ModelPermissionEvaluator.class.isAssignableFrom(Class.forName(className));
	}
}
