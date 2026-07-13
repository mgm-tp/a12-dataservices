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
package com.mgmtp.a12.dataservices.rpc.internal;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.aop.TargetClassAware;
import org.springframework.core.annotation.AnnotationUtils;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RpcUtils {

	public static final String REPLICA_URL_PROPERTY = "spring.datasources.dataservices-read-replica.url";

	private static final Pattern METHOD_WITH_VERSION = Pattern.compile("^(.*):(\\d+)$");

	private RpcUtils() {
	}

	/**
	 * Returns `true` if every RPC call in `json` (single or batch) is non-mutating.
	 * An operation is non-mutating when its `@RemoteOperation(isMutation = false)`.
	 * Unknown operations or operations missing the annotation are treated as mutating.
	 */
	public static boolean isAllOperationsNonMutating(JsonNode json, Map<String, Object> operations) {
		if (json.isArray()) {
			for (JsonNode rpc : json) {
				String methodName = rpc.path("method").asText("");
				if (isMutatingOperation(methodName, operations)) {
					return false;
				}
			}
			return true;
		} else {
			String methodName = json.path("method").asText("");
			return !isMutatingOperation(methodName, operations);
		}
	}

	private static boolean isMutatingOperation(String methodName, Map<String, Object> operations) {
		String serviceName = getServiceName(methodName);
		Object operationBean = operations != null ? operations.get(serviceName) : null;

		if (operationBean == null) {
			log.warn("Operation not found for method: {}, treating as mutating", methodName);
			return true;
		}

		Class<?> operationClass = operationBean instanceof TargetClassAware targetClassAwareBean
			? targetClassAwareBean.getTargetClass()
			: operationBean.getClass();

		RemoteOperation annotation = AnnotationUtils.findAnnotation(operationClass, RemoteOperation.class);

		if (annotation == null) {
			log.warn("@RemoteOperation annotation missing on {}, treating as mutating", operationClass.getSimpleName());
			return true;
		}

		return annotation.isMutation();
	}

	private static String getServiceName(String methodName) {
		Matcher matcher = METHOD_WITH_VERSION.matcher(methodName);
		return matcher.matches() ? matcher.group(1) : methodName;
	}
}
