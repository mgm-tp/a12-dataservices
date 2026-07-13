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
package com.mgmtp.a12.dataservices.internal.service.exporter;

import java.lang.reflect.Method;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AttachmentExporter}, focusing on URL normalization logic.
 */
public class AttachmentExporterTest {

	private final Optional<IAttachmentRepository> attachmentRepository = Optional.of(mock(IAttachmentRepository.class));
	private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

	@DataProvider
	public Object[][] standaloneContentStoreDataProvider() {
		return new Object[][] {
			// { remoteUrl, relativePath, expectedAbsoluteUrl }
			{ "http://localhost:9090/cs", "./cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11",
				"http://localhost:9090/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11" },
			{ "http://localhost:9090/cs", "/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11",
				"http://localhost:9090/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11" },
			{ "http://contentstore:9090/cs", "cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11",
				"http://contentstore:9090/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11" },
		};
	}

	@Test(dataProvider = "standaloneContentStoreDataProvider",
		description = "Should resolve relative URLs against content store remote URL in standalone mode")
	public void shouldResolveAgainstContentStoreRemoteUrl(String remoteUrl, String relativePath, String expectedUrl) throws Exception {
		ContentStoreClientProperties properties = createProperties(remoteUrl);
		AttachmentExporter exporter = new AttachmentExporter(attachmentRepository, httpServletRequest, properties);

		Method getAbsoluteUrl = AttachmentExporter.class.getDeclaredMethod("getAbsoluteUrl", String.class);
		getAbsoluteUrl.setAccessible(true);

		String result = (String) getAbsoluteUrl.invoke(exporter, relativePath);

		Assert.assertEquals(result, expectedUrl);
	}

	@DataProvider
	public Object[][] fallbackToRequestUrlDataProvider() {
		return new Object[][] {
			// { requestUrl, relativePath, expectedAbsoluteUrl }
			{ "http://localhost:8082/api/sme/workspace/export", "./cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11",
				"http://localhost:8082/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11" },
			{ "http://localhost:8082/api/sme/workspace/export", "/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11",
				"http://localhost:8082/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11" },
			{ "http://localhost:8082/api/sme/workspace/export", "cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11",
				"http://localhost:8082/cs/download/fb59b520-6154-4b46-bb4c-3e1509735d11" },
		};
	}

	@Test(dataProvider = "fallbackToRequestUrlDataProvider",
		description = "Should fall back to request URL when content store remote URL is not configured")
	public void shouldFallBackToRequestUrlWhenRemoteUrlNotConfigured(String requestUrl, String relativePath, String expectedUrl) throws Exception {
		when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
		ContentStoreClientProperties properties = createProperties("");
		AttachmentExporter exporter = new AttachmentExporter(attachmentRepository, httpServletRequest, properties);

		Method getAbsoluteUrl = AttachmentExporter.class.getDeclaredMethod("getAbsoluteUrl", String.class);
		getAbsoluteUrl.setAccessible(true);

		String result = (String) getAbsoluteUrl.invoke(exporter, relativePath);

		Assert.assertEquals(result, expectedUrl);
	}

	private ContentStoreClientProperties createProperties(String remoteUrl) {
		ContentStoreClientProperties properties = new ContentStoreClientProperties();
		ContentStoreClientProperties.ClientConfiguration config = new ContentStoreClientProperties.ClientConfiguration();
		config.setRemoteUrl(remoteUrl);
		properties.setConfiguration(config);
		return properties;
	}
}
