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
package com.mgmtp.a12.dataservices.model.bulkload;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.bulkload.internal.CollapsingDocumentModelReferenceResolver;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class ModelBulkImportTest extends AbstractDataServicesCoreTest {

	@Mock
	private ResourcePatternResolver resourcePatternResolver;
	@Mock private ModelService modelService;
	@Mock private HeaderParser headerParser;
	@Mock private DocumentModelUtils documentModelUtils;
	@Spy private BulkImporterConfiguration bulkImporterConfiguration = Mockito.spy(new BulkImporterConfiguration());

	@Spy private DsResourceUtils dsResourceUtils = Mockito.spy(new DsResourceUtils(resourcePatternResolver));

	@Mock private CollapsingDocumentModelReferenceResolver.CollapsingDocumentModelReferenceResolverFactory collapsingDocumentModelReferenceResolverFactory;

	@Mock private CollapsingDocumentModelReferenceResolver collapsingDocumentModelReferenceResolver;

	@InjectMocks private ModelBulkImporter modelBulkImporter;

	@Test
	public void testDoImport_successfully() throws IOException, URISyntaxException, HeaderParseException {
		String bulkLocation = RandomStringUtils.randomAlphabetic(20);
		String content = RandomStringUtils.randomAlphabetic(20);
		Header header = makeTestModelHeader();
		GenericModel model = new GenericModel();
		model.setHeader(header);
		Resource resource = mockResource(content);
		Resource[] resources = { resource };

		doReturn(Arrays.stream(resources)).when(dsResourceUtils).getJsonResources(any(String.class));
		when(headerParser.parseJson(anyString())).thenReturn(header);
		when(modelService.create(content)).thenReturn(model);
		when(collapsingDocumentModelReferenceResolverFactory.getInstance(documentModelResolver)).thenReturn(collapsingDocumentModelReferenceResolver);

		modelBulkImporter.doImport(bulkLocation, bulkImporterConfiguration);

		ArgumentCaptor<ModelsAfterImportEvent> modelsAfterImportEventArgumentCaptor = ArgumentCaptor.forClass(ModelsAfterImportEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(modelsAfterImportEventArgumentCaptor.capture());
		Assert.assertEquals(modelsAfterImportEventArgumentCaptor.getValue().getImportedModels().size(), 1);
		Assert.assertTrue(modelsAfterImportEventArgumentCaptor.getValue().getImportedModels().stream().findFirst().isPresent());
		Assert.assertEquals(modelsAfterImportEventArgumentCaptor.getValue().getImportedModels().stream().findFirst().get(), header);

		Mockito.verify(modelService, Mockito.times(1)).exists(header);

	}

	private Resource mockResource(String content) {
		return new Resource() {
			@Override
			public boolean exists() {
				return false;
			}

			@Override
			public URL getURL() throws IOException {
				return null;
			}

			@Override
			public URI getURI() throws IOException {
				URI uri = null;
				try {
					uri = Mockito.spy(new URI("foo://demo.com:8042/over"));
					when(uri.isAbsolute()).thenReturn(true);
				} catch (URISyntaxException e) {}

				return uri;
			}

			@Override
			public File getFile() throws IOException {
				return new File(content);
			}

			@Override
			public long contentLength() throws IOException {
				return 0;
			}

			@Override
			public long lastModified() throws IOException {
				return 0;
			}

			@Override
			public Resource createRelative(String relativePath) throws IOException {
				return null;
			}

			@Override
			public String getFilename() {
				return null;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(content.getBytes());
			}
		};

	}
}
