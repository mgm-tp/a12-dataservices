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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.security.core.context.SecurityContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelLoader;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.relationship.internal.DefaultRelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.model.Model;

@Listeners(MockitoTestNGListener.class)
public abstract class AbstractDefaultDocumentServiceTest extends AbstractDataServicesCoreTest {

	public static final String DOCUMENT_FILENAME = DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json";
	public static final String DOCUMENT_FIELD_NAME = "/BusinessPartnerRoot/Name";

	@InjectMocks protected DefaultDocumentService defaultDocumentService;

	@Mock protected DocumentModelLoader documentModelLoader;
	@Mock protected DocumentModelServiceFactory documentModelServiceFactory;
	@Mock protected AttachmentHandler attachmentHandler;
	@Mock protected AttachmentSupport attachmentSupport;
	@Mock protected IDocumentRepository documentRepository;
	@Spy protected ArrayList<IDocumentRepository> documentRepositories;
	@Mock protected DocumentPermissionEvaluator documentPermissionEvaluator;
	@Mock protected ModelPermissionEvaluator<Model> modelPermissionEvaluator;
	@Mock protected DefaultRelationshipLinkService defaultRelationshipLinkService;
	@Mock protected RelationshipLinkRepository relationshipLinkRepository;
	@Mock protected DocumentModelUtils documentModelUtils;
	@Mock protected KernelDocumentService kernelDocumentService;
	@Mock protected DocumentUtils documentUtils;
	@Mock protected DocumentServiceFactory documentServiceFactory;
	@Mock protected ModelHeaderJpaRepository modelHeaderRepository;
	@Spy protected DataServicesCoreProperties dataServicesCoreProperties = Mockito.spy(new DataServicesCoreProperties());

	@Mock protected SecurityContext securityContext;
	@Mock protected UniqueConstraintValidator uniqueConstraintValidator;
	@Mock protected DocumentSearchIndexBehaviour documentSearchIndexBehaviour;
	protected Optional<DocumentSearchIndexBehaviour> indexBehavior;
	@Mock protected QueryService queryService;

	protected final List<DocumentReference> documentReferences =
		List.of(
			new DocumentReference(RandomStringUtils.randomAlphabetic(15), "1"),
			new DocumentReference(RandomStringUtils.randomAlphabetic(10), "2")
		);

	protected String testModelName = DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
	protected final String userName = "userName";

	protected ModelHeaderEntity headerEntity;

	@BeforeMethod public void before() throws IllegalAccessException {
		super.setCurrentUser(userName);
		documentRepositories.add(documentRepository);
		indexBehavior = Optional.of(documentSearchIndexBehaviour);
		FieldUtils.writeField(defaultDocumentService, "dataServicesDocumentFactory", dataServicesDocumentFactory, true);
		FieldUtils.writeField(defaultDocumentService, "documentUtils", documentUtils, true);
		FieldUtils.writeField(defaultDocumentService, "attachmentHandler", Optional.of(attachmentHandler), true);
		FieldUtils.writeField(defaultDocumentService, "attachmentSupport", Optional.of(attachmentSupport), true);
		FieldUtils.writeField(defaultDocumentService, "dataServicesCoreProperties", dataServicesCoreProperties, true);
		FieldUtils.writeField(defaultDocumentService, "metadataUtils", metadataUtils, true);
		FieldUtils.writeField(defaultDocumentService, "indexBehavior", indexBehavior, true);
		FieldUtils.writeField(defaultDocumentService, "documentModelServiceFactory", documentModelServiceFactory, true);
		FieldUtils.writeField(defaultDocumentService, "documentModelLoader", documentModelResolver, true);
		FieldUtils.writeField(defaultDocumentService, "documentV2Serializer", documentV2Serializer, true);
		headerEntity = new ModelHeaderEntity(kernelTestSupport.getDocumentModelResolver().getDocumentModelById(testModelName).getHeader());
	}

	@AfterMethod public void after() {
		documentRepositories.clear();
		Mockito.reset(documentRepository, eventPublisher,
			modelPermissionEvaluator, documentUtils, documentModelUtils, modelHeaderRepository, dataServicesDocumentFactory, documentSearchIndexBehaviour,
			documentModelServiceFactory, uniqueConstraintValidator);
	}

	protected void compareDocumentV2(DocumentV2 oldDoc, DocumentV2 updatedDoc) {
		List<String> paths = List.of(
			DocumentMetadataConstants.DOCREF_METADATA_PATH,
			DocumentMetadataConstants.CREATED_AT_METADATA_NAME,
			DocumentMetadataConstants.CREATOR_PATH,
			DOCUMENT_FIELD_NAME
		);
		compareDocumentV2(oldDoc, updatedDoc, paths);
	}

	protected void compareDocumentV2(DocumentV2 oldDoc, DocumentV2 updatedDoc, List<String> paths) {
		paths.forEach(path -> Assert.assertEquals(oldDoc.fieldValue(path), updatedDoc.fieldValue(path)));
	}

}
