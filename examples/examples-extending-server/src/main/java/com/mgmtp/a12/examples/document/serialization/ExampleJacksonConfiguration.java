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
package com.mgmtp.a12.examples.document.serialization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;

/**
 * An example for extending the Jackson `ObjectMapper` configuration in a DataServices application.
 * Exposes a {@link tools.jackson.databind.JacksonModule} bean that Spring Boot registers automatically
 * alongside DataServices internal modules.
 */
@ConditionalOnProperty(
	name = "com.mgmtp.a12.examples.documents.jackson.enabled",
	havingValue = "true"
)
@Configuration
public class ExampleJacksonConfiguration {

	/**
	 * Provides a Jackson module that registers custom serializers and deserializers for example types.
	 *
	 * @return a {@link tools.jackson.databind.JacksonModule} with example type mappings.
	 */
	@Bean("exampleJacksonModule")
	public JacksonModule exampleJacksonModule() {
		SimpleModule module = new SimpleModule("ExampleJacksonModule");
		module.addSerializer(ExampleTaxId.class, new ExampleTaxIdSerializer());
		module.addDeserializer(ExampleTaxId.class, new ExampleTaxIdDeserializer());
		return module;
	}
}
