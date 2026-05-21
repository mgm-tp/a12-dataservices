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
package com.mgmtp.a12.dataservices.client.config;

import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Value;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.ClientFactory;
import com.mgmtp.a12.dataservices.client.ClientFactory.ClientFactoryBuilder;
import com.mgmtp.a12.dataservices.client.configuration.ClientConfiguration;
import com.mgmtp.a12.dataservices.client.exception.MissingAccessRightException;
import com.mgmtp.a12.dataservices.client.exception.MissingDataException;
import com.mgmtp.a12.dataservices.client.model.rest.RestModelsClient;
import com.mgmtp.a12.uaa.client.rest.AuthenticationRestClient;
import com.mgmtp.a12.uaa.client.rest.CurrentUser;
import com.mgmtp.a12.uaa.client.rest.config.AuthenticationType;
import com.mgmtp.a12.uaa.client.rest.config.common.UrlProperty;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientAuthenticationProperties;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Test
public class RestMultiConfigurationIT extends AbstractSpringContextIT {

	public static final String RELATIVE_LOGIN_URL = "user/local/login";
	@Value("http://localhost:${config.port:8080}")
	private String host;

	@BeforeClass
	public void init() {
		createModelFromFile(CONTRACT_MODEL_FILE);
	}

	@AfterClass
	public void cleanUp() {
		cleanUpByDocumentModel(CONTRACT_MODEL_NAME);
	}

	@Test(expectedExceptions = MissingDataException.class)
	public void checkManualProperConfigurationWithException() {
		ClientFactory clientFactory = createProperlyConfiguedfactory().build();
		RestModelsClient modelsClient = clientFactory.getRestModelsClient();
		modelsClient.loadModel("unknown");
	}

	@Test
	public void getUserInfoGuest() {
		ClientFactory clientFactory = createFactory(getAPIUrl(), RELATIVE_LOGIN_URL, getAPIUrl(), "guest", "guest").build();
		AuthenticationRestClient authClient = clientFactory.getAuthenticationRestClient();
		CurrentUser currentUser = authClient.currentUser();
		Assert.assertEquals(currentUser.getUsername(), "guest");
	}

	@Test
	public void checkCustomHeadersIsSet() {
		Map<String, String> headers = Map.of(
			"Header1", "Value1",
			"Header2", "Value2"
		);

		MutableInt count = new MutableInt(0);

		ClientFactory clientFactory = createFactory(getAPIUrl(), RELATIVE_LOGIN_URL, getAPIUrl(), "guest", "guest")
			.withInterceptors((request, body, execution) -> {
				count.increment();
				headers.forEach((k, v) -> request.getHeaders().add(k, v));
				return execution.execute(request, body);
			}, (request, body, execution) -> {
				count.increment();
				headers.forEach((k, v) -> {
					Assert.assertTrue(request.getHeaders().containsKey(k), "Request must have key " + k);
					Assert.assertEquals(request.getHeaders().get(k).get(0), v, "Header " + k + " must have value " + v);
				});
				return execution.execute(request, body);
			})
			.build();
		clientFactory.getAuthenticationRestClient().currentUser();
		Assert.assertEquals(count.intValue(), 2, "Two interceptors must be executed");
	}

	@Test
	public void checkManualProperConfiguration() {
		ClientFactory clientFactory = createProperlyConfiguedfactory().build();

		AuthenticationRestClient authClient = clientFactory.getAuthenticationRestClient();
		CurrentUser currentUser = authClient.currentUser();
		Assert.assertNotNull(currentUser);
	}

	@Test
	public void checkSelfConfiguration() {
		ClientFactory clientFactory =
			createFactory(getAPIUrl(), RELATIVE_LOGIN_URL, getAPIUrl(),  "admin", "admin").build();

		AuthenticationRestClient authClient = clientFactory.getAuthenticationRestClient();
		CurrentUser currentUser = authClient.currentUser();
		Assert.assertNotNull(currentUser);
		String model = modelsClient.loadModel(CONTRACT_MODEL_NAME);
		Assert.assertNotNull(model);
	}

	@Test(expectedExceptions = MissingAccessRightException.class)
	public void checkManualInvalidConfigurationWrongUser() {
		ClientFactory clientFactory = createFactory(getAPIUrl(), RELATIVE_LOGIN_URL, getAPIUrl(), "adminX", "admin").build();
		RestModelsClient modelsClient = clientFactory.getRestModelsClient();
		modelsClient.loadModel(CONTRACT_MODEL_NAME);
	}

	@Test(expectedExceptions = MissingAccessRightException.class, expectedExceptionsMessageRegExp = "Unauthorized")
	public void checkManualInvalidConfigurationWrongUaaUrl() {
		ClientFactory clientFactory = createFactory("http://nowhere:8080", "wrong/url", getAPIUrl(), "admin", "admin").build();
		RestModelsClient modelsClient = clientFactory.getRestModelsClient();
		modelsClient.loadModel(CONTRACT_MODEL_NAME);
	}

	@Test(expectedExceptions = MissingDataException.class, expectedExceptionsMessageRegExp = "Data not found")
	public void checkManualInvalidConfigurationWrongDataServicesUrl() {
		ClientFactory clientFactory = createFactory(getAPIUrl(), RELATIVE_LOGIN_URL, host + "/nowhere", "admin", "admin").build();
		RestModelsClient modelsClient = clientFactory.getRestModelsClient();
		modelsClient.loadModel(CONTRACT_MODEL_NAME);
	}

	private String getAPIUrl() {
		return host + "/api";
	}

	private ClientFactoryBuilder createProperlyConfiguedfactory() {
		return createFactory(getAPIUrl(), RELATIVE_LOGIN_URL, getAPIUrl(), "admin", "admin");
	}

	private ClientFactoryBuilder createFactory(String uaaBaseUrl, String relativeLoginUrl, String dataServicesBaseUrl, String user,
		String password) {
		//init UAA REST connector

		UAARestClientAuthenticationProperties authProperties = new UAARestClientAuthenticationProperties();
		authProperties.setUsername(user);
		authProperties.setPassword(password);
		authProperties.setLoginRelative(new UrlProperty(relativeLoginUrl));

		UAARestClientProperties uaaProperties = new UAARestClientProperties(new UrlProperty(uaaBaseUrl), AuthenticationType.LOCAL);
		uaaProperties.setUaaBase(new UrlProperty(getAPIUrl()));
		uaaProperties.setAuthorizationHeaderName("Authorization");
		uaaProperties.setGeneratedTokenHeaderName("access_token");
		uaaProperties.setGeneratedTokenExpirationHeaderName("access_token_expiration");
		uaaProperties.setAuthenticationConfiguration(authProperties);

		return ClientFactory.builder(uaaProperties, new ClientConfiguration(dataServicesBaseUrl));
	}
}
