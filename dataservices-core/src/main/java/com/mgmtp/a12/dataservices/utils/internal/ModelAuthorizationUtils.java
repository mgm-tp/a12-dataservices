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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

/**
 * Utility methods invoked by Spring Expression Language expressions inside
 * `authorizationDefinition.json` via the `T(...)` operator.
 *
 * [WARNING]
 * Do NOT rename, move, or remove this class or any of its public methods.
 * The fully-qualified class name and method signatures are hard-coded in
 * `authorizationDefinition.json` (e.g. `dataservices-security-user-api`).
 * Renaming or deleting them will silently break every model-level authorization
 * check without any compile-time or start-up error.
 */
public class ModelAuthorizationUtils {

	private static final String ROLES_ANNOTATION = "roles";

	/**
	 * Returns the list of role names declared in the `roles` annotation of the given document header.
	 *
	 * Called from `authorizationDefinition.json
	 *
	 * @param header document header whose `roles` annotation is read; must not be `null`
	 * @return ordered list of trimmed, non-blank role names; empty list when no annotation is present
	 */
	public static List<String> getObjectRoles(@NonNull Header header) {
		return Optional.ofNullable(header.getAnnotations()).stream()
			.flatMap(Collection::stream)
			.filter(a -> ROLES_ANNOTATION.equals(a.getName()))
			.map(Annotation::getValue)
			.map(a -> a.split(","))
			.flatMap(Arrays::stream)
			.map(StringUtils::trim)
			.filter(StringUtils::isNotBlank)
			.toList();
	}

	/**
	 * Returns the subset of the current user's granted authorities whose names appear in the
	 * `roles` annotation of the given document header.
	 *
	 * Called from authorizationDefinition.json
	 *
	 * @param header document header whose `roles` annotation is compared against the current user's authorities
	 * @return authorities granted to the current user that also appear in the header's role list; empty when there is no overlap
	 */
	public static Collection<GrantedAuthority> getMatchingRoles(Header header) {
		List<String> objectRoles = getObjectRoles(header);
		return UaaConnector.getCurrentUserAuthorities()
			.stream()
			.filter(auth -> objectRoles.contains(auth.getAuthority()))
			.map(GrantedAuthority.class::cast)
			.toList();
	}

}
