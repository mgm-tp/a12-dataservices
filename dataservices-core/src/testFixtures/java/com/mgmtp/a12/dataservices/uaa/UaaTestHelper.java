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
package com.mgmtp.a12.dataservices.uaa;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class UaaTestHelper {

	public static void setCurrentUserName(UserDetails auth) {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(auth, auth.getPassword(), auth.getAuthorities()));
	}

	public TestUserDetails mockUserDetail(Collection<String> accessRights) {
		TestUserDetails userDetails = createUser();
		TestGrantedAuthority grantedAuthority = new TestGrantedAuthority();
		grantedAuthority.setName(RandomStringUtils.randomAlphabetic(10));
		grantedAuthority.setAccessRights(accessRights.stream()
			.map(TestAccessRight::new)
			.toList()
		);
		userDetails.setAuthorities(List.of(grantedAuthority));
		return userDetails;
	}

	public static TestUserDetails createUser() {
		TestUserDetails userDetails = new TestUserDetails();
		userDetails.setUsername(RandomStringUtils.randomAlphabetic(10));
		userDetails.setPassword(RandomStringUtils.randomAlphabetic(10));
		return userDetails;
	}

	@Data
	public static class TestUserDetails implements UserDetails {
		private String password;
		private String username;
		private Collection<String> roles;
		private Collection<GrantedAuthority> authorities;
	}

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class TestGrantedAuthority implements GrantedAuthority {
		private String name;
		private Collection<TestAccessRight> accessRights;

		@Override public String getAuthority() {
			return name;
		}
	}

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class TestAccessRight {
		@JsonValue private String name;
	}

}
