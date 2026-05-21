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
package com.mgmtp.a12.contentstore.autoconfigure.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.contentstore.autoconfigure.internal.listener.DefaultContentDownloadEventListener;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.ContentStoreBaseUrlPropertyCondition;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.OnEnabledDatabaseStorageCondition;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.OnEnabledDefaultListenerCondition;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.OnEnabledFileSystemStorageCondition;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.content.internal.DatabaseContentRepository;
import com.mgmtp.a12.contentstore.content.internal.FileSystemContentRepository;
import com.mgmtp.a12.contentstore.content.internal.jpa.repository.ContentJpaRepository;
import com.mgmtp.a12.contentstore.initialization.ContentStoreInitializationListener;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.dataservices.common.content.internal.JsonContentTypeListener;
import com.mgmtp.a12.dataservices.common.content.internal.MsWordContentTypeListener;
import com.mgmtp.a12.dataservices.common.content.internal.TikaContentTypeDetector;

import lombok.RequiredArgsConstructor;

/**
 * Spring application context auto-configuration for Content Store server.
 */
@Import({ ContentStoreRepositoryConfiguration.class })
@ComponentScan(basePackages = { "com.mgmtp.a12.contentstore" })
@PropertySource({ "classpath:contentstore-common.properties" })
@RequiredArgsConstructor
public class ContentStoreAutoConfiguration {

	private final ContentJpaRepository contentJpaRepository;
	private final ApplicationContext applicationContext;
	private final ApplicationEventPublisher applicationEventPublisher;

	@ConfigurationProperties(prefix = ContentStoreProperties.PROPERTIES_PREFIX)
	@ConditionalOnMissingBean
	@Conditional(ContentStoreBaseUrlPropertyCondition.class)
	@Bean ContentStoreProperties contentStoreProperties() {
		return new ContentStoreProperties();
	}

	/**
	 * Initial implementation for ContentTypeDetector bean.
	 *
	 * @param applicationEventPublisher publisher for publishing events like {@link com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent}.
	 * @param contentStoreProperties property for setting up service.
	 * @return TikaContentTypeService.
	 */
	@ConditionalOnMissingBean
	@Bean public ContentTypeDetector contentTypeDetector(ApplicationEventPublisher applicationEventPublisher, ContentStoreProperties contentStoreProperties) {
		return new TikaContentTypeDetector(applicationEventPublisher, contentStoreProperties.getExtensions().getTika().getInMemoryTemp().isEnabled());
	}

	/**
	 * Initial DatabaseContentRepository and FileSystemContentRepository beans depend on application properties mgmtp.a12.dataservices.contentstore.storage.defaultContentStorage.
	 *
	 * @return FileSystemContentRepository.
	 */
	@Conditional(OnEnabledFileSystemStorageCondition.class)
	@Bean public FileSystemContentRepository fileSystemContentRepository(ContentStoreProperties contentStoreProperties) {
		return new FileSystemContentRepository(contentStoreProperties.getStorage().getFs().getLocation());
	}

	/**
	 * Initial DatabaseContentRepository and FileSystemContentRepository beans depend on application properties mgmtp.a12.dataservices.contentstore.storage.defaultContentStorage.
	 *
	 * @return DatabaseContentRepository.
	 */
	@Conditional(OnEnabledDatabaseStorageCondition.class)
	@Bean public DatabaseContentRepository databaseContentRepository() {
		return new DatabaseContentRepository(contentJpaRepository);
	}

	@Bean public JsonContentTypeListener jsonContentTypeListener(ObjectMapper objectMapper) {
		return new JsonContentTypeListener(objectMapper);
	}

	@Bean public MsWordContentTypeListener msWordContentTypeListener() {
		return new MsWordContentTypeListener();
	}

	@Bean public ContentStoreInitializationListener contentStoreInitializationListener() {
		return new ContentStoreInitializationListener(applicationContext, applicationEventPublisher);
	}

	@Conditional(OnEnabledDefaultListenerCondition.class)
	@Bean public DefaultContentDownloadEventListener defaultContentDownloadEventListener() {
		return new DefaultContentDownloadEventListener();
	}
}
