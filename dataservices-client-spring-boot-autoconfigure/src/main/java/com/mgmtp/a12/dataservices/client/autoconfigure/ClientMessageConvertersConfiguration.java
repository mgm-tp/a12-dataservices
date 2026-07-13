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
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import com.mgmtp.a12.dataservices.client.ZipMessageConverter;
/**
 * Registers HTTP message converters for the Data Services REST connector.
 *
 * This configuration is intentionally separate from {@link ClientAutoConfiguration} to break a
 * circular dependency: `ClientAutoConfiguration` depends on `RestPostConnector` (from
 * `RestServerConnectorAutoConfiguration`), which injects `Optional<List<HttpMessageConverter<?>>>`
 * before its `@PostConstruct` runs.
 *
 * `ZipMessageConverter` is NOT a Spring bean (no `@Service`) to prevent auto-collection
 * interference: if Spring collected individual `HttpMessageConverter<?>` beans, the connector
 * would receive only those and skip `registerDefaults()`, losing all standard converters.
 *
 * The registered converters are:
 * `ByteArrayHttpMessageConverter`, `StringHttpMessageConverter` (including `application/json`),
 * `JacksonJsonHttpMessageConverter` (for JSON object responses), `ZipMessageConverter` (for ZIP downloads).
 */
@Configuration public class ClientMessageConvertersConfiguration {
	/**
	 * Creates the list of `HttpMessageConverter` instances injected into the REST connector.
	 *
	 * Includes all converters needed for connector operations because the connector uses
	 * `addCustomConverter` (which disables `registerDefaults()`), so Spring built-in defaults
	 * are not applied automatically.
	 *
	 * @return mutable list of converters for connector injection
	 */
	@Bean public List<HttpMessageConverter<?>> clientHttpMessageConverters() {
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		stringConverter.setSupportedMediaTypes(List.of(
			MediaType.TEXT_PLAIN,
			MediaType.APPLICATION_JSON,
			MediaType.ALL));
		return new ArrayList<>(List.of(
			new ByteArrayHttpMessageConverter(),
			new ResourceHttpMessageConverter(),
			stringConverter,
			new JacksonJsonHttpMessageConverter(),
			new ZipMessageConverter()));
	}
}
