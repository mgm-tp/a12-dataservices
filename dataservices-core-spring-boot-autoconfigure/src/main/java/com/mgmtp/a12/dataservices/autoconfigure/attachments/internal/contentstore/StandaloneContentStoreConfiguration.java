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
package com.mgmtp.a12.dataservices.autoconfigure.attachments.internal.contentstore;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestServerConnectorFactory;
import com.mgmtp.a12.connector.rest.RestServerConnectorFactoryBuilder;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientConfiguration;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.contentstore.client.content.ContentStorePrivateClient;
import com.mgmtp.a12.contentstore.client.content.ContentStorePublicClient;
import com.mgmtp.a12.contentstore.client.content.ContentStoreTicketClient;
import com.mgmtp.a12.contentstore.client.exception.ContentStoreErrorHandler;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.ContentStoreMapper;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.StandaloneContentStoreAttachmentRepository;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.common.content.internal.TikaContentTypeDetector;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.contentstore.StandaloneContentStoreModeCondition;

import lombok.RequiredArgsConstructor;

@Conditional(value = StandaloneContentStoreModeCondition.class)
@Import(value = ContentStoreClientConfiguration.class)
@RequiredArgsConstructor
public class StandaloneContentStoreConfiguration {

	private RestPostConnector postConnector;
	private RestGetConnector getConnector;
	private RestDeleteConnector deleteConnector;

	private final ContentStoreClientProperties properties;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	@ConditionalOnMissingBean(IAttachmentRepository.class)
	@Bean public IAttachmentRepository standaloneContentStoreAttachmentRepository(
		ContentStorePrivateClient privateClient,
		ContentStoreTicketClient ticketClient,
		ContentStoreMapper contentStoreMapper
	) {
		return new StandaloneContentStoreAttachmentRepository(ticketClient, privateClient, contentStoreMapper);
	}

	@ConditionalOnMissingBean(ContentTypeDetector.class)
	@Bean public ContentTypeDetector contentTypeDetector(ApplicationEventPublisher eventPublisher) {
		return new TikaContentTypeDetector(eventPublisher, dataServicesCoreProperties.getAttachments().getMimeType().getInMemoryTemp().isEnabled());
	}

	@Bean public ContentStorePrivateClient privateClient(ObjectMapper objectMapper, Optional<List<ClientHttpRequestInterceptor>> interceptors,
		Optional<List<HttpMessageConverter<?>>> messageConvertes) {
		createServerConnector(objectMapper, interceptors, messageConvertes);
		return new ContentStorePrivateClient(this.properties, this.postConnector, this.getConnector, this.deleteConnector);
	}

	@Bean public ContentStoreTicketClient ticketClient(ObjectMapper objectMapper, Optional<List<ClientHttpRequestInterceptor>> interceptors,
		Optional<List<HttpMessageConverter<?>>> messageConvertes) {
		createServerConnector(objectMapper, interceptors, messageConvertes);
		return new ContentStoreTicketClient(this.properties, this.getConnector);
	}

	@Bean public ContentStorePublicClient publicClient(ObjectMapper objectMapper, Optional<List<ClientHttpRequestInterceptor>> interceptors,
		Optional<List<HttpMessageConverter<?>>> messageConvertes) {
		createServerConnector(objectMapper, interceptors, messageConvertes);
		return new ContentStorePublicClient(this.properties, this.getConnector);
	}

	private void createServerConnector(ObjectMapper objectMapper, Optional<List<ClientHttpRequestInterceptor>> interceptors,
		Optional<List<HttpMessageConverter<?>>> messageConvertes) {
		if (this.getConnector == null || this.postConnector == null || this.deleteConnector == null) {
			RestServerConnectorFactory restServerConnectorFactory = createServerConnectorFactory(objectMapper, interceptors, messageConvertes);
			this.getConnector = restServerConnectorFactory.createRestGetConnector();
			this.postConnector = restServerConnectorFactory.createRestPostConnector();
			this.deleteConnector = restServerConnectorFactory.createRestDeleteConnector();
		}
	}

	private static RestServerConnectorFactory createServerConnectorFactory(ObjectMapper objectMapper, Optional<List<ClientHttpRequestInterceptor>> interceptors,
		Optional<List<HttpMessageConverter<?>>> messageConvertes) {
		return RestServerConnectorFactoryBuilder
			.create()
			.withInterceptors(interceptors.orElseGet(Collections::emptyList).toArray(new ClientHttpRequestInterceptor[0]))
			.withErrorHandlers(new ContentStoreErrorHandler(objectMapper))
			.withMessageConverters(messageConvertes.orElse(null))
			.build();
	}
}
