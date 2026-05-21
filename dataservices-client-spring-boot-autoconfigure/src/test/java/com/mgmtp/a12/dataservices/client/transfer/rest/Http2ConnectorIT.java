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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.autoconfigure.ClientProperties;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;

@Slf4j
public class Http2ConnectorIT extends AbstractSpringContextIT {

	private static final String HEALTH_ENDPOINT = "/actuator/health";

	@Autowired private ClientProperties properties;
	private RestTemplate restTemplate;
	private OkHttpClient okHttpClient;

	@BeforeClass
	public void setUp() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(1, TimeUnit.MINUTES)
			.writeTimeout(1, TimeUnit.MINUTES)
			.addInterceptor(new Http2AssertionInterceptor());
		builder.setProtocols$okhttp(List.of(Protocol.H2_PRIOR_KNOWLEDGE));
		okHttpClient = builder.build();
		OkHttp3ClientHttpRequestFactory httpRequestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
		restTemplate = new RestTemplate(httpRequestFactory);
	}

	@Test
	public void checkH2RequestResponse() {
		restTemplate.getForEntity(
			UrlBuilderSupport.withBaseUrl(properties.getConfiguration().getBaseUrl().replace("/api", "") + HEALTH_ENDPOINT)
				.createBuilder().toUriString(),
			String.class);
	}

	private static class Http2AssertionInterceptor implements Interceptor {

		@NotNull @Override public Response intercept(@NotNull Chain chain) throws IOException {
			Response response = chain.proceed(chain.request());
			String responseGenerated = response.protocol().name()
				+ ' ' + response.code();
			String expectedResponse = "H2_PRIOR_KNOWLEDGE 200";
			Assert.assertEquals(responseGenerated, expectedResponse);
			return response;
		}
	}
}
