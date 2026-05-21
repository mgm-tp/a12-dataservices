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
package com.mgmtp.a12.dataservices.server.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgmtp.a12.dataservices.configuration.internal.ConfigurationPropertiesData;
import com.mgmtp.a12.dataservices.document.internal.KernelDocumentSerializer;
import com.mgmtp.a12.dataservices.server.actuator.internal.ConfigurationEndpoint;
import com.mgmtp.a12.dataservices.server.internal.NoCacheControlFilter;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

/**
 * Auto-configuration support for REST server library.
 */
@PropertySource("classpath:dataservices-server-default.properties")
@Configuration public class ServerAutoConfiguration implements WebMvcConfigurer {

	/**
	 * Configures the {@link MultipartResolver} for handling multipart/form-data requests.
	 *
	 * @return the {@link StandardServletMultipartResolver} used by Spring MVC.
	 */
	@Bean(name = "multipartResolver") public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	/**
	 * Registers a {@link NoCacheControlFilter} to prevent client-side caching of responses.
	 *
	 * @param registry the {@link InterceptorRegistry} to configure; never null.
	 */
	@Override public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new NoCacheControlFilter());
	}

	/**
	 * Configures the primary {@link Jackson2ObjectMapperBuilder} with modules used by DataServices.
	 * Includes Java Time, JDK8 support, and a custom serializer for {@link DocumentV2}.
	 *
	 * @param kernelDocumentSerializer serializer for kernel {@link DocumentV2}; must not be null.
	 * @return the configured {@link Jackson2ObjectMapperBuilder}.
	 */
	@Bean @Primary public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(KernelDocumentSerializer kernelDocumentSerializer) {
		return new Jackson2ObjectMapperBuilder().modules(
			new JavaTimeModule(),
			new Jdk8Module(),
			new SimpleModule().addSerializer(DocumentV2.class, kernelDocumentSerializer)
		);
	}

	/**
	 * Exposes an actuator-like endpoint providing configuration properties for diagnostics.
	 *
	 * @param configurationPropertiesData aggregated configuration properties; must not be null.
	 * @return the {@link ConfigurationEndpoint} bean.
	 */
	@Bean public ConfigurationEndpoint getConfigurationEndpoint(ConfigurationPropertiesData configurationPropertiesData) {
		return new ConfigurationEndpoint(configurationPropertiesData);
	}
}
