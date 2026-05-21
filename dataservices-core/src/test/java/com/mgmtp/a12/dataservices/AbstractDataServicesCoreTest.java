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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.Listeners;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocument;
import com.mgmtp.a12.dataservices.document.internal.DefaultDocumentMetadata;
import com.mgmtp.a12.dataservices.document.support.internal.DefaultDocumentSupport;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelContent;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.AllArgsConstructor;
import lombok.Data;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOC_REF_METADATA_NAME;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.MODEL_REFERENCE_METADATA_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Listeners(MockitoTestNGListener.class)
public abstract class AbstractDataServicesCoreTest extends AbstractKernelAwareTest {

	@Spy protected final DataServicesCoreProperties dataServicesCoreProperties = spy(new DataServicesCoreProperties());
	protected final ObjectMapper objectMapper = spy(JsonMapper.builder()
		.addModule(new JavaTimeModule())
		.addModule(new Jdk8Module())
		.addModule(new SimpleModule().addSerializer(DocumentV2.class, kernelTestSupport.getKernelDocumentSerializer()))
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.build());
	protected final DefaultRelationshipModelSerializer relationshipModelSerializer = spy(new DefaultRelationshipModelSerializer(objectMapper));

	@MockitoSpyBean("relationshipModelLoader")
	@Spy protected RelationshipModelLoader relationshipModelLoader = new TestResourceRelationshipModelLoader(relationshipModelSerializer);

	protected MetadataTestSupport metadataTestSupport = MetadataTestSupport.getInstance();
	protected final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

	protected final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();

	public DocumentV2 loadDocumentV2(String documentModelName, String documentName) {
		try (Reader r = new StringReader(loadDocumentAsString(documentName))) {
			ListIProblemReporter pr = new ListIProblemReporter();
			return kernelTestSupport.getDocumentV2Serializer().deserializeV2(r, documentModelName, kernelTestSupport.getDocumentDeserializationConfig(), pr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@MockitoSpyBean("documentSupport")
	@Spy protected DefaultDocumentSupport documentSupport =
		new DefaultDocumentSupport(documentJsonDeserializationConfig, documentJsonSerializationConfig, documentModelResolver, documentV2Serializer);

	public String loadDocumentAsString(String documentName) throws IOException {
		Resource resource = defaultResourceLoader.getResource("/document/" + documentName);
		return resource.getContentAsString(StandardCharsets.UTF_8);
	}

	protected IDocumentModel mockModel(String modelName) {
		IDocumentModel model = mock(IDocumentModel.class);
		IDocumentModelContent content = mock(IDocumentModelContent.class);
		IGroup root = mock(IGroup.class);
		Mockito.lenient().when(content.getDocumentModelRoot()).thenReturn(root);
		Mockito.lenient().when(model.getContent()).thenReturn(content);
		Mockito.lenient().when(model.getHeader()).thenReturn(makeTestModelHeader());
		Mockito.lenient().doReturn(model).when(documentModelResolver).getDocumentModelById(modelName);
		Mockito.lenient().doReturn(Optional.empty()).when(documentModelResolver).getDocumentModelSearchService(modelName);

		return model;
	}

	@Data @AllArgsConstructor
	public static class TestAnnotation implements Annotation {
		private String name;
		private String value;
	}

	public Header makeTestModelHeader() {
		return makeTestModelHeader(RandomStringUtils.randomAlphabetic(10));
	}

	public Header makeTestModelHeader(String modelType) {
		return makeTestModelHeader(modelType,
			new ArrayList<>(List.of(new TestAnnotation("roles", "admin")))
		);
	}

	public Header makeTestModelHeader(String modelType, List<Annotation> annotations) {
		TestHeader header = new TestHeader();
		header.setId(RandomStringUtils.randomAlphabetic(6));
		header.setModelType(modelType != null ? modelType : RandomStringUtils.randomAlphabetic(10));
		header.setAnnotations(annotations);
		header.setLabels(Collections.emptyList());
		return header;
	}

	public void setCurrentUser(String username) {
		UaaTestHelper.setCurrentUserName(User.builder().username(username).password("").build());
	}

	public DataServicesDocument makeTestBusinessPartnerDsDocument() {
		return makeTestDsDocument(BUSINESS_PARTNER_DOCUMENT_MODEL);
	}

	protected DefaultDataServicesDocument makeTestDsDocument() {
		return makeTestDsDocument(BUSINESS_PARTNER_DOCUMENT_MODEL);
	}

	protected DefaultDataServicesDocument makeTestDsDocument(String documentModel) {
		DocumentV2 emptyDoc = DocumentV2.empty(documentModel);
		return dataServicesDocumentFactory.newDataServicesDocument(
			metadataUtils.createDocumentMetadata(
				emptyDoc,
				documentUtils.generateDocRef(emptyDoc),
				UserConstants.ADMIN_USER, Instant.now(),
				null
			)
		);
	}

	protected DefaultDataServicesDocument createDataServicesDocument(DocumentReference documentReference, DocumentV2 documentV2) {
		DocumentV2 kernelDocument = documentV2;
		if (kernelDocument == null) {
			kernelDocument = DocumentV2.empty(documentReference.getDocumentModelName(), documentReference.getDocumentId())
				.withFieldValue(KernelUtils.of(List.of(DOCUMENT_METADATA_GROUP_NAME, DOC_REF_METADATA_NAME), List.of(1, 1)),
					documentReference.toString())
				.withFieldValue(
					KernelUtils.of(List.of(DOCUMENT_METADATA_GROUP_NAME, MODEL_REFERENCE_METADATA_NAME), List.of(1, 1)),
					documentReference.getDocumentModelName());
		}
		DataServicesDocumentMetadata metadata = new DefaultDocumentMetadata(kernelDocument);
		return new DefaultDataServicesDocument(kernelDocument, metadata);
	}
}
