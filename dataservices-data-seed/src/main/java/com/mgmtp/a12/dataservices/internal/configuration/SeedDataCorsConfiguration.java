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
package com.mgmtp.a12.dataservices.internal.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.mgmtp.a12.uaa.authentication.AuthenticationProperties;
import com.mgmtp.a12.uaa.authentication.security.UAASecurityConfigurer;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SeedDataCorsConfiguration extends UAASecurityConfigurer<SeedDataCorsConfiguration> {

	private AuthenticationProperties authenticationProperties;

	@Override
	public void configureHttpSecurity(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorizedHttpRequest ->
			authorizedHttpRequest.requestMatchers(PathPatternRequestMatcher.withDefaults()
				.matcher("%s/internal/seed-data".formatted(authenticationProperties.getContextPath()))).permitAll());
		http.addFilterBefore(createCorsFilterWithCustomProcessor(), CsrfFilter.class);
	}

	private CorsFilter createCorsFilterWithCustomProcessor() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(authenticationProperties.getCors().getAllowCredentials());
		configuration.setAllowedMethods(authenticationProperties.getCors().getAllowedMethods());
		configuration.setAllowedHeaders(authenticationProperties.getCors().getAllowedHeaders());
		configuration.setExposedHeaders(authenticationProperties.getCors().getExposedHeaders());
		configuration.setAllowedOrigins(authenticationProperties.getCors().getAllowedOrigins());
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return new CorsFilter(source);
	}
}
