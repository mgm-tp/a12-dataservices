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
package com.mgmtp.a12.dataservices.client;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestPutConnector;
import com.mgmtp.a12.dataservices.client.configuration.ClientConfiguration;
import com.mgmtp.a12.dataservices.client.enumeration.rest.RestEnumerationClient;
import com.mgmtp.a12.dataservices.client.exception.DataServicesErrorHandler;
import com.mgmtp.a12.dataservices.client.model.rest.RestModelsClient;
import com.mgmtp.a12.dataservices.client.relationship.rest.RestRelationshipClient;
import com.mgmtp.a12.dataservices.client.rpc.RestRpcOperationsClient;
import com.mgmtp.a12.uaa.client.rest.AuthenticationRestClient;
import com.mgmtp.a12.uaa.client.rest.AuthorizationRestClient;
import com.mgmtp.a12.uaa.client.rest.config.UAARestClientFactory;
import com.mgmtp.a12.uaa.client.rest.config.UAARestClientFactoryBuilder;
import com.mgmtp.a12.uaa.client.rest.config.properties.UAARestClientProperties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The factory is used for NON spring app to initialize the Data Services REST client.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientFactory {

	private final ClientConfiguration clientConfiguration;
	private final RestGetConnector getConnector;
	private final RestPostConnector postConnector;
	private final RestPutConnector putConnector;
	private final RestDeleteConnector deleteConnector;
	private final AuthenticationRestClient authenticationRestClient;
	private final AuthorizationRestClient authorizationRestClient;
	private final RestEnumerationClient restEnumerationClient;
	private final RestRelationshipClient restRelationshipClient;
	private final RestModelsClient restModelsClient;
	private final RestRpcOperationsClient rpcOperationsClient;

	/**
	 * Creates a new {@link ClientFactoryBuilder} configured with UAA REST client properties and Data Services client configuration.
	 *
	 * @param uaaRestClientProperties UAA REST client configuration properties; must not be `null`.
	 * @param clientConfiguration Data Services client configuration including base URL and query settings; must not be `null`.
	 * @return a builder to configure interceptors and build a {@link ClientFactory}.
	 */
	public static ClientFactoryBuilder builder(UAARestClientProperties uaaRestClientProperties, ClientConfiguration clientConfiguration) {
		return new ClientFactoryBuilder(uaaRestClientProperties, clientConfiguration);
	}

	/**
	 * Builder for {@link ClientFactory} instances.
	 *
	 * Allows configuration of HTTP interceptors and builds REST connectors and clients for Data Services using UAA authentication.
	 */
	public static class ClientFactoryBuilder {
		private final ClientConfiguration clientConfiguration;
		private final UAARestClientFactoryBuilder uaaRestClientFactoryBuilder;
		private ClientFactoryBuilder(UAARestClientProperties uaaRestClientProperties, ClientConfiguration clientConfiguration) {
			this.clientConfiguration = clientConfiguration;
			this.uaaRestClientFactoryBuilder = UAARestClientFactoryBuilder
				.withConfiguration(uaaRestClientProperties)
				.withErrorHandlers(new DataServicesErrorHandler());
		}

		/**
		 * Registers additional HTTP request interceptors for all underlying REST clients.
		 *
		 * @param interceptors interceptors applied in order to outgoing requests; may be empty but not `null`.
		 * @return this builder for fluent configuration.
		 */
		public ClientFactoryBuilder withInterceptors(ClientHttpRequestInterceptor... interceptors) {
			this.uaaRestClientFactoryBuilder.withInterceptors(interceptors);
			return this;
		}

		/**
		 * Builds a {@link ClientFactory} including UAA-authenticated REST connectors and Data Services clients.
		 *
		 * On startup issues, wraps {@link GeneralSecurityException} and {@link IOException} into a {@link RuntimeException}.
		 *
		 * @return a fully initialized {@link ClientFactory}.
		 * @throws RuntimeException if REST client factory creation fails due to security or I/O problems.
		 */
		public ClientFactory build() {
			try {
				UAARestClientFactory uaaRestClientFactory = this.uaaRestClientFactoryBuilder.build();

				RestGetConnector getConnector = uaaRestClientFactory.getGetConnector();
				RestPostConnector postConnector = uaaRestClientFactory.getPostConnector();
				RestPutConnector putConnector = uaaRestClientFactory.getPutConnector();
				RestDeleteConnector deleteConnector = uaaRestClientFactory.getDeleteConnector();

				return new ClientFactory(
					clientConfiguration,
					getConnector,
					postConnector,
					putConnector,
					deleteConnector,
					uaaRestClientFactory.getAuthenticationRestClient(),
					uaaRestClientFactory.getAuthorizationRestClient(),
					new RestEnumerationClient(clientConfiguration.getBaseUrl(), getConnector),
					new RestRelationshipClient(clientConfiguration.getBaseUrl(), getConnector),
					new RestModelsClient(clientConfiguration.getBaseUrl(), getConnector, postConnector, putConnector, deleteConnector),
					new RestRpcOperationsClient(clientConfiguration.getBaseUrl(), postConnector)
				);
			} catch (GeneralSecurityException | IOException e) {
				throw new RuntimeException("Failed to create UAARestClientFactory", e);
			}
		}
	}
}
