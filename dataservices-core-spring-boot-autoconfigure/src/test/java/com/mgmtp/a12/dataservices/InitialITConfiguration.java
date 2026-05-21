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
package com.mgmtp.a12.dataservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for test
 */
@Slf4j
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@TestPropertySource(properties = {
	"spring.datasources.dataservices.embedded-postgres.enabled=true",
	"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
	"spring.datasources.contentstore.embedded-postgres.enabled=true"
})
@DataServicesApplication public class InitialITConfiguration {

	private final YAMLFactory yamlFactory = new YAMLFactory();
	private final ObjectMapper yamlObjectMapper = new ObjectMapper(yamlFactory)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Bean public Map<String, TestGrantedAuthority> grantedAuthorities() throws IOException {
		LinkedHashMap<String, Collection<TestGrantedAuthority>> roleDefinitions =
			yamlObjectMapper.readValue(this.getClass().getResourceAsStream("/testUserRoles.yaml"), new TypeReference<>() {
			});
		return roleDefinitions
			.get("roles").stream()
			.collect(Collectors.toMap(TestGrantedAuthority::getAuthority, e -> e));
	}

	@Bean public UserDetailsService userDetailsService(Map<String, TestGrantedAuthority> grantedAuthorities) throws IOException {
		List<TestUserDetails> testUserDetails = createUsers();
		Map<String, TestUserDetails> users = testUserDetails.stream()
			.peek(user -> user.setAuthorities(user.getRoles().stream()
				.map((String key) -> grantedAuthorities.computeIfAbsent(key, k -> {
					throw new IllegalStateException(String.format("missing role %s", k));
				}))
				.collect(Collectors.toList())))
			.collect(Collectors.toMap(TestUserDetails::getUsername, e -> e));
		return users::get;
	}

	@Bean("customTaskExecutor") TaskExecutor customTaskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	private List<TestUserDetails> createUsers() throws IOException {
		List<TestUserDetails> testUserDetails =
			new ArrayList<>();
		testUserDetails.add(createUser("Admin.yaml"));
		testUserDetails.add(createUser("Guest.yaml"));
		testUserDetails.add(createUser("Test.yaml"));
		testUserDetails.add(createUser("TestUserWithSpaces.yaml"));
		testUserDetails.add(createUser("NotTest.yaml"));
		testUserDetails.add(createUser("ModelManagerUser.yaml"));
		testUserDetails.add(createUser("NoAccessRightsUser.yaml"));

		return testUserDetails;
	}

	private TestUserDetails createUser(String userFile) throws IOException {
		Map<String, Object> values = yamlObjectMapper.readValue(yamlFactory.createParser(this.getClass().getResourceAsStream("/local_users/" + userFile)),
			new TypeReference<>() {});
		TestUserDetails user = new TestUserDetails();
		user.setUsername(values.get("username").toString());
		user.setPassword(values.get("password").toString());
		user.setPersonFirstName(String.valueOf(values.get("firstname")));
		user.setRoles((ArrayList) values.get("authorities"));
		return user;
	}

	@Data
	public static class TestUserDetails implements UserDetails {
		private String password;
		private String username;
		private String campaignName;
		private String personFirstName;
		private boolean accountNonExpired;
		private boolean isAccountNonLocked;
		private boolean isCredentialsNonExpired;
		private boolean isEnabled = true;
		private Collection<String> roles;
		private Collection<TestGrantedAuthority> authorities;
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
