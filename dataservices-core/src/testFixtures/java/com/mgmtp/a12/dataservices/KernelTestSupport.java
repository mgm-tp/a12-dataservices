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

import java.util.Collections;
import java.util.List;

import org.mockito.Spy;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.internal.KernelDocumentSerializer;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.model.internal.DataServicesDocumentDynamicServiceConfig;
import com.mgmtp.a12.dataservices.model.internal.DataServicesModelCodeCache;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

import static com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig.Format.JSON;
import static org.mockito.Mockito.spy;

@Slf4j
@Getter @NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KernelTestSupport {

	private static final KernelTestSupport INSTANCE = new KernelTestSupport();

	private final DocumentModelServiceFactory documentModelServiceFactory = spy(new DocumentModelServiceFactory());
	private final DocumentModelService documentModelService = spy(new DocumentModelService());
	private final IDocumentModelService iDocumentModelService = spy(new ExternalDocumentModelService());
	private final IDocumentModelSerializer documentModelSerializer = spy(documentModelServiceFactory.createDocumentModelSerializer());

	@MockitoSpyBean("documentModelLoader")
	@Spy private TestResourcesDocumentModelResolver documentModelResolver = new TestResourcesDocumentModelResolver(this);
	private final JsonMapper jsonMapper = spy(JsonMapper.builder().build());
	private final IDocumentV2Serializer documentV2Serializer = spy(new DocumentSerializerImpl(documentModelResolver));
	private final DocumentSerializationConfig documentSerializationConfig = spy(DocumentSerializationConfig.builder().format(JSON).build());
	private final KernelDocumentSerializer kernelDocumentSerializer = new KernelDocumentSerializer(documentV2Serializer, documentSerializationConfig);
	private final DocumentDeserializationConfig documentDeserializationConfig = spy(DocumentDeserializationConfig.builder().format(JSON).build());
	private final DocumentServiceFactory documentServiceFactory = spy(new DocumentServiceFactory(documentModelResolver));
	private final IDocumentRtService rtService = spy(new DocumentRtServiceFactory(documentModelResolver)
		.createDocumentRtService(new DataServicesDocumentDynamicServiceConfig(new DataServicesModelCodeCache())));
	private final KernelDocumentService kernelDocumentService = spy(new KernelDocumentService(false, Collections.emptyList(),
		Collections.emptyList(), Collections.emptyList(),
		Collections.emptyList(), List.of(DocumentModelConstants.CONTRACT_CDM_MODEL),
		rtService, documentModelResolver, false));

	public static KernelTestSupport getInstance() {
		return INSTANCE;
	}

}
