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
package com.mgmtp.a12.dataservices.query.generator.sql.internal;

import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.DefaultQueryGeneratorContext.QueryGeneratorContextFactory;
import com.mgmtp.a12.dataservices.query.internal.marshalling.QuerySubtypeProvider;
import com.mgmtp.a12.dataservices.rpc.internal.marshalling.DataServicesJacksonModule;

import lombok.SneakyThrows;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class QueryGeneratorContextFactorySubtypeWiringTest {

	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		QuerySubtypeProvider provider = new QuerySubtypeProvider(DataServicesCoreProperties.DS_PACKAGE_PREFIX);
		JsonMapper mapper = JsonMapper.builder().addModule(new DataServicesJacksonModule(provider.getSubtypes())).build();
		QueryGeneratorContextFactory factory = new QueryGeneratorContextFactory(mapper, mock(ApplicationContext.class), provider);
		factory.init();
		objectMapper = factory.getObjectMapper();
	}

	@SneakyThrows
	@Test(description = "Should deserialize ExactMatchOperator by its JSON type name after init()")
	public void shouldDeserializeExactMatchOperatorByJsonTypeNameAfterInit() {
		String json = "{\"operator\":\"exact_match\",\"field\":\"/Root/Field\",\"value\":\"test\",\"caseSensitive\":true}";

		ILogicOperator result = objectMapper.readValue(json, ILogicOperator.class);

		assertNotNull(result, "Deserialized operator must not be null");
		assertTrue(result instanceof ExactMatchOperator,
			"Expected ExactMatchOperator but got " + result.getClass().getSimpleName());
	}

	@SneakyThrows
	@Test(description = "Should produce identical ObjectMapper when module is already registered on injected mapper")
	public void shouldSkipRedundantScanWhenModuleAlreadyRegistered() {
		QuerySubtypeProvider provider = new QuerySubtypeProvider(DataServicesCoreProperties.DS_PACKAGE_PREFIX);
		JsonMapper mapperWithModule = JsonMapper.builder()
			.addModule(new DataServicesJacksonModule(provider.getSubtypes()))
			.build();
		QueryGeneratorContextFactory factory = new QueryGeneratorContextFactory(mapperWithModule, mock(ApplicationContext.class), provider);
		factory.init();

		boolean modulePresent = factory.getObjectMapper().registeredModules().stream()
			.anyMatch(m -> DataServicesJacksonModule.MODULE_NAME.equals(m.getModuleName()));

		assertTrue(modulePresent, "DataServicesJacksonModule must be present on the rebuilt ObjectMapper");
	}
}
