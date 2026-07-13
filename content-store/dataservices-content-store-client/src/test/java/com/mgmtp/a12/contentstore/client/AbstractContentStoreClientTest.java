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
package com.mgmtp.a12.contentstore.client;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockserver.integration.ClientAndServer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.mgmtp.a12.connector.rest.RestDeleteConnector;
import com.mgmtp.a12.connector.rest.RestGetConnector;
import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestServerConnectorFactory;
import com.mgmtp.a12.connector.rest.RestServerConnectorFactoryBuilder;
import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.contentstore.client.content.ContentStorePrivateClient;
import com.mgmtp.a12.contentstore.client.content.ContentStorePublicClient;
import com.mgmtp.a12.contentstore.client.content.ContentStoreTicketClient;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

import static com.mgmtp.a12.contentstore.client.constants.Constants.PERSISTENT_TYPE_PUBLIC;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public abstract class AbstractContentStoreClientTest {

	public String contentId;
	public String persistentType;
	public String contentUpload;

	public ObjectWriter objectWriter = JsonMapper.builder().build().writer().withDefaultPrettyPrinter();

	public ClientAndServer mockServer;
	public ContentStorePrivateClient contentStorePrivateClient;
	public ContentStorePublicClient contentStorePublicClient;
	public ContentStoreTicketClient contentStoreTicketClient;

	public RestServerConnectorFactory connectorFactory;
	public RestGetConnector getConnector;
	public RestPostConnector postConnector;
	public RestDeleteConnector deleteConnector;

	@Spy public ContentStoreClientProperties contentStoreClientProperties = Mockito.spy(new ContentStoreClientProperties());

	@BeforeClass public void init() {
		mockServer = startClientAndServer(9090);
		contentId = UUID.randomUUID().toString();
		persistentType = PERSISTENT_TYPE_PUBLIC;

		ContentStoreClientProperties.ClientConfiguration clientConfiguration = new ContentStoreClientProperties.ClientConfiguration();
		clientConfiguration.setRemoteUrl("http://localhost:9090");
		Mockito.doReturn(clientConfiguration).when(contentStoreClientProperties).getConfiguration();

		connectorFactory = createServerConnectorFactory();
		getConnector = connectorFactory.createRestGetConnector();
		postConnector = connectorFactory.createRestPostConnector();
		deleteConnector = connectorFactory.createRestDeleteConnector();

		contentStorePrivateClient = new ContentStorePrivateClient(contentStoreClientProperties, postConnector, getConnector, deleteConnector);
		contentStorePublicClient = new ContentStorePublicClient(contentStoreClientProperties, getConnector);
		contentStoreTicketClient = new ContentStoreTicketClient(contentStoreClientProperties, getConnector);

		contentUpload = RandomStringUtils.randomAlphabetic(20);
	}

	@AfterClass public void afterClass() {
		mockServer.stop();
	}

	public RestServerConnectorFactory createServerConnectorFactory() {
		return RestServerConnectorFactoryBuilder
			.create()
			.build();
	}

}
