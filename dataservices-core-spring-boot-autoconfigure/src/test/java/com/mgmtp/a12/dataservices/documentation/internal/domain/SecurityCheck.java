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

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.NonNull;
import lombok.SneakyThrows;

public class SecurityCheck extends AbstractLoggedElement {
	private final Method target;
	private final Collection<String> preRules;
	private final Collection<String> postRules;
	private final Object resource;

	public SecurityCheck(Method target, Collection<String> preRules, Collection<String> postRules, Object resource, List<StackTraceElement> callHistory,
		LoggedEventDirection direction) {
		super(LoggedEventType.SECURITY,
			tidyUpId("security_" + joinString(List.of("pre_" + joinString(preRules, "_"), "post_" + joinString(postRules, "_")), "_")),
			joinString(List.of(formatRules("Pre", preRules), formatRules("Post", postRules)), "\\n"),
			Instant.now(), direction, callHistory
		);
		this.target = target;
		this.preRules = preRules;
		this.postRules = postRules;
		this.resource = resource;
		this.setProperties(Map.of("rule", String.valueOf(preRules), "resource", String.valueOf(resource)));
	}

	@NonNull private static String joinString(Collection<String> rules, String delimiter) {
		return CollectionUtils.isEmpty(rules) ? "" : String.join(delimiter, rules);
	}

	@NonNull private static String formatRules(String post, Collection<String> rules) {
		return CollectionUtils.isEmpty(rules) ? "" : String.format("--//%s checks//--\\n**%s**", post, joinString(rules, "**\\n**"));
	}

	@Override public AbstractLoggedElement createReturn() {
		return new SecurityCheck(target, preRules, postRules, resource, getCallHistory(), LoggedEventDirection.RETURN);
	}

	@SneakyThrows
	@Override public Caller getCaller() {
		if (target.getDeclaringClass().getName().startsWith(DataServicesCoreProperties.DS_PACKAGE_PREFIX)) {
			return new Caller(target.getDeclaringClass().getName(), target.getName(),
				getModifier(target.getDeclaringClass().getName(), target.getName()));
		} else {
			StackTraceElement stackTraceElement = getCallHistory().get(0);
			return new Caller(stackTraceElement.getClassName(), stackTraceElement.getMethodName(),
				getModifier(stackTraceElement.getClassName(), stackTraceElement.getMethodName()));
		}
	}

	@Override public Caller getPublicCaller() {
		Caller caller = getCaller();
		return caller.getModifier() == Caller.CallerModifier.PUBLIC ? caller : super.getPublicCaller();
	}
}
