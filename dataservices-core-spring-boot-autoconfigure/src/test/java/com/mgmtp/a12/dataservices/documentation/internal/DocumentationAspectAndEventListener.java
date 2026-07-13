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
package com.mgmtp.a12.dataservices.documentation.internal;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.StreamUtils;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.documentation.internal.domain.AbstractLoggedElement;
import com.mgmtp.a12.dataservices.documentation.internal.domain.AbstractLoggedElement.LoggedEventDirection;
import com.mgmtp.a12.dataservices.documentation.internal.domain.EventTriggered;
import com.mgmtp.a12.dataservices.documentation.internal.domain.OperationCall;
import com.mgmtp.a12.dataservices.documentation.internal.domain.SecurityCheck;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect @Component public class DocumentationAspectAndEventListener {

	private final ThreadLocal<ArrayList<AbstractLoggedElement>> currentQueue = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<Deque<EventTriggered>> eventStack = ThreadLocal.withInitial(ArrayDeque::new);
	@Getter private final Map<AbstractLoggedElement, List<AbstractLoggedElement>> threads = new ConcurrentHashMap<>();
	@Setter private boolean active = false;

	@Pointcut("within(com.mgmtp.a12.dataservices..*) && !(within(com.mgmtp.a12.dataservices.transfer.imports..*) || within(com.mgmtp.a12.dataservices.documentation.internal.DocumentationAspectAndEventListener))")
	private void dsClassesExceptImports() {}

	@Around("execution(* com.mgmtp.a12.uaa.authorization.AuthorizationService.checkPermissions(..))")
	public Object logDirectPermissionCall(ProceedingJoinPoint joinPoint) throws Throwable {
		SecurityCheck securityCheck = logPermissionCheck(joinPoint);
		return handleCallAndFinishTimestamp(joinPoint, securityCheck);
	}

	@Around("dsClassesExceptImports() && ("
		+ "@annotation(org.springframework.security.access.prepost.PreAuthorize) || "
		+ "@annotation(org.springframework.security.access.prepost.PostAuthorize) || "
		+ "@annotation(org.springframework.security.access.prepost.PreFilter) || "
		+ "@annotation(org.springframework.security.access.prepost.PostFilter))")
	public Object logPermissionCall(ProceedingJoinPoint joinPoint) throws Throwable {
		SecurityCheck securityCheck = logPermissionCheck(joinPoint);
		return handleCallAndFinishTimestamp(joinPoint, securityCheck);
	}

	@Around("dsClassesExceptImports() && @target(com.mgmtp.a12.dataservices.rpc.RemoteOperation)")
	public Object logOperationCall(ProceedingJoinPoint joinPoint) throws Throwable {
		OperationCall o = null;
		if (active) {
			String operation = joinPoint.getTarget().getClass().getAnnotation(RemoteOperation.class).name();
			o = new OperationCall(operation, getParameters(joinPoint), getCallHistory(), LoggedEventDirection.CALL);
			startNewThread(o);
		}
		Object r = joinPoint.proceed();
		if (o != null) {
			o.setFinished(Instant.now());
		}
		return r;
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@CommonDataServicesEventListener public void logEventStart(Object event) throws IntrospectionException {
		if (active) {
			String name = event.getClass().getName();
			if (name.startsWith(DataServicesCoreProperties.DS_PACKAGE_PREFIX)) {
				EventTriggered e = new EventTriggered(event, getCallHistory(), LoggedEventDirection.CALL);
				currentQueue.get().add(e);
				eventStack.get().push(e);
			}
		}
	}

	@Order
	@CommonDataServicesEventListener public void logEventFinish(Object event) throws IntrospectionException {
		if (active) {
			String name = event.getClass().getName();
			if (name.startsWith(DataServicesCoreProperties.DS_PACKAGE_PREFIX)) {
				EventTriggered startEvent = eventStack.get().pop();
				Assert.isTrue(startEvent.getEvent().getClass().equals(event.getClass()), "Event class doesn't match.");
				EventTriggered finishEvent = new EventTriggered(event, getCallHistory(), LoggedEventDirection.RETURN);
				currentQueue.get().add(finishEvent);
				startEvent.setFinished(finishEvent.getStarted());
			}
		}
	}

	public void reset() {
		currentQueue.remove();
		eventStack.remove();
		threads.clear();
	}

	@SneakyThrows
	@NonNull private SecurityCheck getSecurityCheck(ProceedingJoinPoint jp, Method method) {
		if ("checkPermissions".equals(method.getName()) && AuthorizationService.class.isAssignableFrom(method.getDeclaringClass())) {
			return new SecurityCheck(method, List.of(String.valueOf(jp.getArgs()[1])), null, null, getCallHistory(), LoggedEventDirection.CALL);
		} else {
			return new SecurityCheck(method,
				getRules(method, Stream.of(PreAuthorize.class, PreFilter.class)),
				getRules(method, Stream.of(PostAuthorize.class, PostFilter.class)),
				null, getCallHistory(), LoggedEventDirection.CALL);
		}
	}

	private static Collection<String> getRules(Method method, Stream<Class<? extends Annotation>> ruleTypes) {
		return ruleTypes
			.map(a -> extractSecurityRole(method, a))
			.filter(Objects::nonNull)
			.map(v -> v.replaceAll("hasUAAPermission\\([\"']?(.*?)[\"']?\\)", "$1"))
			.collect(Collectors.toSet());
	}

	private static String extractSecurityRole(Method method, Class<? extends Annotation> annotationType) {
		Annotation annotation = method.getAnnotation(annotationType);
		return switch (annotation) {
			case PreAuthorize preAuthorize -> preAuthorize.value();
			case PostAuthorize postAuthorize -> postAuthorize.value();
			case PreFilter preFilter -> preFilter.value();
			case PostFilter postFilter -> postFilter.value();
			case null, default -> null;
		};
	}

	private Object handleCallAndFinishTimestamp(ProceedingJoinPoint joinPoint, AbstractLoggedElement loggedElement) throws Throwable {
		Object r = joinPoint.proceed();
		if (loggedElement != null) {
			loggedElement.setFinished(Instant.now());
			currentQueue.get().add(loggedElement.createReturn());
		}
		return r;
	}

	private SecurityCheck logPermissionCheck(ProceedingJoinPoint jp) {
		Signature signature = jp.getSignature();
		if (active) {
			if (signature instanceof MethodSignature methodSignature) {
				Method method = methodSignature.getMethod();
				SecurityCheck securityCheck = getSecurityCheck(jp, method);
				currentQueue.get().add(securityCheck);
				return securityCheck;

			} else {
				log.warn("not a method: {}", signature);
			}
		}
		return null;
	}

	private static Map<String, ?> getParameters(ProceedingJoinPoint joinPoint) {
		MethodSignature ms = (MethodSignature) joinPoint.getSignature();
		return StreamUtils.zip(Arrays.stream(ms.getMethod().getParameters()), Arrays.stream(joinPoint.getArgs()),
				(a, b) -> new AbstractMap.SimpleEntry<>(a.getName(), b))
			.collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
	}

	private List<StackTraceElement> getCallHistory() {
		return makeCallHistory(Thread.currentThread().getStackTrace());
	}

	private List<StackTraceElement> makeCallHistory(StackTraceElement[] stackTrace) {
		return Arrays.stream(stackTrace)
			.filter(s -> s.getClassName().startsWith(DataServicesCoreProperties.DS_PACKAGE_PREFIX))
			.filter(s -> !s.getClassName().startsWith("com.mgmtp.a12.dataservices.documentation.internal"))
			.toList();
	}

	private void startNewThread(AbstractLoggedElement head) {
		ArrayList<AbstractLoggedElement> thread = new ArrayList<>();
		currentQueue.set(thread);
		threads.put(head, thread);
	}
}
