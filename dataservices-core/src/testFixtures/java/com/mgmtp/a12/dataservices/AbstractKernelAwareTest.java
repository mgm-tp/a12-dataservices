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
package com.mgmtp.a12.dataservices;

import java.util.List;

import org.mockito.Spy;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgmtp.a12.dataservices.document.IDocumentIdGenerator;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentMetadataExtractor;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentIdGenerator;
import com.mgmtp.a12.dataservices.document.internal.MetadataUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;

import static org.mockito.Mockito.spy;

public abstract class AbstractKernelAwareTest {

	protected final KernelTestSupport kernelTestSupport = KernelTestSupport.getInstance();

	@MockitoSpyBean("documentModelResolver")
	@Spy protected TestResourcesDocumentModelResolver documentModelResolver = kernelTestSupport.getDocumentModelResolver();

	@MockitoSpyBean("documentModelLoader")
	@Spy protected TestResourcesDocumentModelResolver documentModelLoader = documentModelResolver;

	@MockitoSpyBean("composeDocumentModelLoader")
	@Spy protected TestResourcesCdmResolver composeDocumentModelLoader = new TestResourcesCdmResolver(documentModelResolver);

	protected final DocumentModelServiceFactory documentModelServiceFactory = kernelTestSupport.getDocumentModelServiceFactory();
	protected final DocumentModelService documentModelService = kernelTestSupport.getDocumentModelService();
	protected final IDocumentModelService iDocumentModelService = kernelTestSupport.getIDocumentModelService();
	protected final IDocumentModelSerializer documentModelSerializer = kernelTestSupport.getDocumentModelSerializer();
	protected final IDocumentFactory documentFactory = kernelTestSupport.getDocumentFactory();
	protected final DocumentModelJoiningService documentModelJoiningService = kernelTestSupport.getDocumentModelJoiningService();
	protected final IDocumentSerializer documentSerializer = kernelTestSupport.getDocumentSerializer();
	protected final IDocumentV2Serializer documentV2Serializer = kernelTestSupport.getDocumentV2Serializer();
	protected final DocumentSerializationConfig documentJsonSerializationConfig = kernelTestSupport.getDocumentSerializationConfig();
	protected final DocumentDeserializationConfig documentJsonDeserializationConfig = kernelTestSupport.getDocumentDeserializationConfig();
	protected final JsonMapper jsonMapper = spy(JsonMapper.builder()
		.addModule(new JavaTimeModule())
		.addModule(new Jdk8Module())
		.addModule(new SimpleModule().addSerializer(DocumentV2.class, kernelTestSupport.getKernelDocumentSerializer()))
	).build();
	protected final DocumentServiceFactory documentServiceFactory = spy(new DocumentServiceFactory(documentModelResolver));
	protected final List<IDocumentIdGenerator> documentIdGenerators = List.of(new DefaultDocumentIdGenerator());
	protected final DocumentUtils documentUtils = spy(new DocumentUtils(documentIdGenerators, documentModelResolver));
	protected final DefaultDataServicesDocumentMetadataExtractor dataServicesDocumentMetadataExtractor = spy(
		new DefaultDataServicesDocumentMetadataExtractor());
	protected final HeaderParser headerParser = new DefaultHeaderParser();
	private final DocumentModelUtils docModelUtils = new DocumentModelUtils(documentModelServiceFactory, documentModelSerializer, headerParser);
	protected final TestDocumentMetadataMetaModelProvider metadataMetaModelProvider = new TestDocumentMetadataMetaModelProvider(docModelUtils);
	protected final MetadataUtils metadataUtils = new MetadataUtils(metadataMetaModelProvider, documentModelServiceFactory);
	protected final DefaultDataServicesDocumentFactory dataServicesDocumentFactory =
		spy(new DefaultDataServicesDocumentFactory(dataServicesDocumentMetadataExtractor, metadataUtils, documentUtils));
	protected final RelationshipModelResolver relationshipModelResolver = spy(new RelationshipModelResolver());
}
