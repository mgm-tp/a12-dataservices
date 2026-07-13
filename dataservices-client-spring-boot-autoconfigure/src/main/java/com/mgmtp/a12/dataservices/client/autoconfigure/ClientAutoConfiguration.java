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
package com.mgmtp.a12.dataservices.client.autoconfigure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestPutConnector;
import com.mgmtp.a12.dataservices.client.attachment.AttachmentClientV2;
import com.mgmtp.a12.dataservices.client.attachment.RestAttachmentV2Client;
import com.mgmtp.a12.dataservices.client.enumeration.EnumerationClient;
import com.mgmtp.a12.dataservices.client.enumeration.rest.RestEnumerationClient;
import com.mgmtp.a12.dataservices.client.model.ModelsClient;
import com.mgmtp.a12.dataservices.client.model.rest.RestModelsClient;
import com.mgmtp.a12.dataservices.client.relationship.RelationshipClient;
import com.mgmtp.a12.dataservices.client.relationship.rest.RestRelationshipClient;
import com.mgmtp.a12.dataservices.rpc.internal.marshalling.DataServicesJacksonModule;
import com.mgmtp.a12.dataservices.client.rpc.RequestBuilderFactory;
import com.mgmtp.a12.dataservices.client.rpc.RestRpcOperationsClient;
import com.mgmtp.a12.dataservices.client.rpc.RpcOperationsClient;
import com.mgmtp.a12.dataservices.query.internal.marshalling.QuerySubtypeProvider;

import tools.jackson.databind.ObjectMapper;

/**
 * Spring Boot auto-configuration for the Data Services client.
 *
 * Registers client beans for RPC, enumeration, relationship, models, and attachments.
 */
@AutoConfigureAfter(name = "com.mgmtp.a12.dataservices.autoconfigure.DataServicesCoreAutoconfiguration")
@EnableConfigurationProperties(ClientProperties.class)
@PropertySource("classpath:dataservices-client-default.properties")
@Configuration public class ClientAutoConfiguration implements InitializingBean {

	private final ClientProperties restProperties;
	private final RestPostConnector postConnector;
	@Deprecated(since = "39.0.0", forRemoval = true)
	private final ObjectMapper objectMapper;

	/**
	 * Creates the auto-configuration with explicit {@link ClientProperties} and {@link com.mgmtp.a12.connector.rest.RestPostConnector}.
	 *
	 * @param restProperties Data Services client properties; must not be null.
	 * @param postConnector REST connector used for POST operations; must not be null.
	 */
	@Autowired public ClientAutoConfiguration(ClientProperties restProperties, RestPostConnector postConnector) {
		this.restProperties = restProperties;
		this.postConnector = postConnector;
		this.objectMapper = null;
	}

	/**
	 * Creates the auto-configuration with explicit {@link ClientProperties}, REST connector, and {@link ObjectMapper}.
	 *
	 * @param restProperties Data Services client properties; must not be null.
	 * @param postConnector REST connector used for POST operations; must not be null.
	 * @param objectMapper Jackson mapper used for registering query subtypes; must not be null.
	 * @deprecated Use {@link #ClientAutoConfiguration(ClientProperties, RestPostConnector)} instead.
	 *             Subtype registration is now handled by the `clientQueryTypesModule` bean.
	 */
	@Deprecated(since = "39.0.0", forRemoval = true)
	public ClientAutoConfiguration(ClientProperties restProperties, RestPostConnector postConnector, ObjectMapper objectMapper) {
		this.restProperties = restProperties;
		this.postConnector = postConnector;
		this.objectMapper = objectMapper;
	}

	/**
	 * No-op implementation of {@link InitializingBean#afterPropertiesSet()}.
	 *
	 * Reserved for future post-property-set initialization; no action is taken at this time.
	 */
	@Override public void afterPropertiesSet() {
		// No-op: intentionally kept empty to avoid breaking changes; reserved for future post-property-set initialization.
	}

	@Bean RequestBuilderFactory requestBuilderFactory(ObjectMapper objectMapper) {
		return new RequestBuilderFactory(objectMapper);
	}

	/**
	 * Creates the {@link com.mgmtp.a12.dataservices.client.rpc.RpcOperationsClient} using the configured base URL and POST connector.
	 *
	 * @param restProperties Client properties providing the base URL; must not be null.
	 * @param postConnector REST connector for POST requests; must not be null.
	 * @return A configured {@link com.mgmtp.a12.dataservices.client.rpc.RpcOperationsClient} instance.
	 */
	@Bean public RpcOperationsClient createRestRpcOperationsClient(ClientProperties restProperties, RestPostConnector postConnector) {
		return new RestRpcOperationsClient(restProperties.getConfiguration().getBaseUrl(), postConnector);
	}

	/**
	 * Creates the {@link com.mgmtp.a12.dataservices.client.enumeration.EnumerationClient} backed by REST.
	 *
	 * @param restProperties Client properties providing the base URL; must not be null.
	 * @param getConnector REST connector for GET requests; must not be null.
	 * @return A configured {@link com.mgmtp.a12.dataservices.client.enumeration.EnumerationClient}.
	 */
	@Bean public EnumerationClient createRestEnumerationClient(ClientProperties restProperties, RestGetConnector getConnector) {
		return new RestEnumerationClient(restProperties.getConfiguration().getBaseUrl(), getConnector);
	}

	/**
	 * Creates the {@link com.mgmtp.a12.dataservices.client.relationship.RelationshipClient} backed by REST.
	 *
	 * @param restProperties Client properties providing the base URL; must not be null.
	 * @param getConnector REST connector for GET requests; must not be null.
	 * @return A configured {@link com.mgmtp.a12.dataservices.client.relationship.RelationshipClient}.
	 */
	@Bean public RelationshipClient createRestRelationshipClient(ClientProperties restProperties, RestGetConnector getConnector) {
		return new RestRelationshipClient(restProperties.getConfiguration().getBaseUrl(), getConnector);
	}

	/**
	 * Creates the {@link com.mgmtp.a12.dataservices.client.model.ModelsClient} backed by REST.
	 *
	 * @param restProperties Client properties providing the base URL; must not be null.
	 * @param getConnector REST connector for GET requests; must not be null.
	 * @param postConnector REST connector for POST requests; must not be null.
	 * @param putConnector REST connector for PUT requests; must not be null.
	 * @param deleteConnector REST connector for DELETE requests; must not be null.
	 * @return A configured {@link com.mgmtp.a12.dataservices.client.model.ModelsClient}.
	 */
	@Bean public ModelsClient createRestModelsClient(ClientProperties restProperties, RestGetConnector getConnector, RestPostConnector postConnector,
		RestPutConnector putConnector, RestDeleteConnector deleteConnector) {
		return new RestModelsClient(restProperties.getConfiguration().getBaseUrl(), getConnector, postConnector, putConnector, deleteConnector);
	}

	/**
	 * Creates the {@link com.mgmtp.a12.dataservices.client.attachment.AttachmentClientV2} backed by REST.
	 *
	 * @return A configured {@link com.mgmtp.a12.dataservices.client.attachment.AttachmentClientV2}.
	 */
	@Bean public AttachmentClientV2 createRestAttachmentClientV2() {
		return new RestAttachmentV2Client(restProperties.getConfiguration().getBaseUrl(), postConnector);
	}

	/**
	 * Nested configuration that exposes the `clientDataServicesJacksonModule` bean in isolation
	 * from {@link ClientAutoConfiguration}. Declared static so Spring can instantiate it
	 * before the outer class, avoiding a circular dependency between the `ObjectMapper` build
	 * pipeline (which collects `JacksonModule` beans via `StandardJsonMapperBuilderCustomizer`)
	 * and the infrastructure beans that `ClientAutoConfiguration` depends on.
	 */
	@Configuration
	static class JacksonConfiguration {

		/**
		 * Scans the packages configured in {@link ClientProperties} for classes annotated with
		 * `@QueryOperator` and `@QueryAggregationFunction` and exposes them as a
		 * {@link QuerySubtypeProvider} bean.
		 *
		 * Skipped when a `QuerySubtypeProvider` bean is already present (e.g. when the server
		 * autoconfiguration is active in the same context).
		 *
		 * @param clientProperties client configuration providing the scan packages; must not be null.
		 * @return the provider; never null.
		 */
		@Bean
		public QuerySubtypeProvider clientQuerySubtypeProvider(ClientProperties clientProperties) {
			return new QuerySubtypeProvider(
				clientProperties.getConfiguration().getQuery().getScanPackages().toArray(new String[0])
			);
		}

		/**
		 * Exposes a {@link DataServicesJacksonModule} bean so that Spring Boot's
		 * `StandardJsonMapperBuilderCustomizer` registers `@QueryOperator` and
		 * `@QueryAggregationFunction` subtypes during `ObjectMapper` construction.
		 *
		 * Skipped when a `DataServicesJacksonModule` bean is already present (e.g. when the server
		 * autoconfiguration is active in the same context).
		 *
		 * @param clientQuerySubtypeProvider provider of discovered query operator and aggregation function subtypes.
		 * @return a configured {@link DataServicesJacksonModule}; never null.
		 */
		@Bean
		public DataServicesJacksonModule clientDataServicesJacksonModule(@Qualifier("clientQuerySubtypeProvider") QuerySubtypeProvider clientQuerySubtypeProvider) {
			return new DataServicesJacksonModule(clientQuerySubtypeProvider.getSubtypes());
		}
	}

}
