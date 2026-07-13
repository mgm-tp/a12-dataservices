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
package com.mgmtp.a12.dataservices.client.transfer.rest;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.autoconfigure.ClientProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Http2ConnectorIT extends AbstractSpringContextIT {

	private static final String HEALTH_ENDPOINT = "/actuator/health";

	@Autowired private ClientProperties properties;
	private RestTemplate restTemplate;

	@BeforeClass
	public void setUp() {
		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(10))
			.build();
		JdkClientHttpRequestFactory httpRequestFactory = new JdkClientHttpRequestFactory(httpClient);
		httpRequestFactory.setReadTimeout(Duration.ofMinutes(1));
		restTemplate = new RestTemplate(httpRequestFactory);
	}

	@Test
	public void checkH2RequestResponse() {
		String response = restTemplate.getForEntity(
			UrlBuilderSupport.withBaseUrl(properties.getConfiguration().getBaseUrl().replace("/api", "") + HEALTH_ENDPOINT)
				.createBuilder().toUriString(),
			String.class).getBody();
		Assert.assertNotNull(response, "Health endpoint should return a response");
	}
}
