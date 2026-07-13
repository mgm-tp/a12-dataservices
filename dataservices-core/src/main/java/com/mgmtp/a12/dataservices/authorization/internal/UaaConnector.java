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
package com.mgmtp.a12.dataservices.authorization.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mgmtp.a12.uaa.authorization.internal.UserUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Temporary solution to bypass UAA non-standard user handling.
 * Username and authorities must be fetched from principal which is guaranteed to be of {@link UserDetails} type.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public class UaaConnector {

	public static String getCurrentUserName() {
		return getCurrentPrincipal()
			.map(UserDetails::getUsername)
			.orElse(null);
	}

	public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
		return getCurrentPrincipal()
			.map(UserDetails::getAuthorities)
			.orElse(List.of());
	}

	public static String getCurrentUserAuthoritiesAsString() {
		return getCurrentUserAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));
	}

	public static Optional<UserDetails> getCurrentPrincipal() {
		return Optional.ofNullable(UserUtils.resolveCurrentUser())
			.map(UserDetails.class::cast);
	}
}
