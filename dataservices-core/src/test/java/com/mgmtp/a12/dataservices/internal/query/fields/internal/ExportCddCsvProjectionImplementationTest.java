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
package com.mgmtp.a12.dataservices.internal.query.fields.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentHeaderRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.export.IDocumentExporter;
import com.mgmtp.a12.dataservices.export.internal.csv.CsvDocumentExporter;
import com.mgmtp.a12.dataservices.internal.query.fields.AbstractProjectionTest;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.document.persistence.internal.DocumentModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.internal.RootBasedPageImpl;
import com.mgmtp.a12.dataservices.query.projection.internal.ExportCddCsvProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ExportCddCsvProjectionImplementationTest extends AbstractProjectionTest {

	@Mock private IDocumentModelService documentModelService;
	@Mock private CsvDocumentExporter csvDocumentExporter;
	@Mock private ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	@Mock private DocumentModelReadRepository documentModelReadRepository;
	@Mock private AttachmentHeaderRepository attachmentHeaderRepository;
	@Mock private IAttachmentRepository attachmentRepository;
	@Mock private DocumentPermissionEvaluator documentPermissionEvaluator;
	@Mock private IModelLoader<ComposeDocumentModel> composeDocumentModelLoader;
	@Mock private IModelLoader<RelationshipModel> relationshipModelLoader;
	@Mock private DefaultQueryContext.InternalQueryAction queryMethod;
	private List<IDocumentExporter> documentExports;
	private DocumentModelLoader documentModelLoader;

	private DefaultQueryContext queryContext;
	private ExportCddCsvProjectionImplementation exportCddProjectionImplementation;

	@BeforeMethod
	void setup() {
		Mockito.reset(documentPermissionEvaluator, documentModelReadRepository);
		documentExports = List.of(csvDocumentExporter);
		documentModelLoader = new DocumentModelLoader(documentModelPermissionEvaluator, eventPublisher, documentModelReadRepository);
		queryContext = new DefaultQueryContext(documentModelLoader, relationshipModelLoader,
			queryMethod, documentModelServiceFactory, queryContextHelper, indexedModelFieldCache, null, null);
		exportCddProjectionImplementation = new ExportCddCsvProjectionImplementation(
			documentTreeHelper, documentModelService, documentExports, Optional.of(attachmentHeaderRepository),
			Optional.of(attachmentRepository),
			documentPermissionEvaluator, composeDocumentModelLoader,
			dataServicesCoreProperties
		);
		Mockito.lenient().doAnswer(invocation -> kernelTestSupport.getDocumentModelResolver().getDocumentModelById(invocation.getArgument(0, String.class)))
			.when(documentModelReadRepository).readModel(anyString());
	}

	@Test
	public void testPreProcess_success() {
		QueryRoot result = exportCddProjectionImplementation.preprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build(), queryContext);

		assertEquals(result.getTargetDocumentModel(), DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		Mockito.verify(documentPermissionEvaluator, Mockito.times(1)).checkExportListCDDPermission();
	}

	@Test(expectedExceptions = AccessDeniedException.class, expectedExceptionsMessageRegExp = "No permission")
	public void testPreProcess_shouldThrowException_whenHaveNoPermission() {

		Mockito.doThrow(new AccessDeniedException("No permission")).when(documentPermissionEvaluator).checkExportListCDDPermission();

		exportCddProjectionImplementation.preprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build(), queryContext);
	}

	@Test public void testPostprocess_success() throws JsonProcessingException {
		setCurrentUser("admin");
		Page<DocumentTreeResult> rootDocuments = getCddRootDocuments();
		IDocumentModel documentModel = documentModelResolver.getDocumentModelById(DocumentModelConstants.CONTRACT_CDM_MODEL);
		ComposeDocumentModel composeDocumentModel = new ComposeDocumentModel(documentModel);

		String modelName = documentModel.getHeader().getId();

		InputStream inputStream = new ByteArrayInputStream(RandomStringUtils.randomAlphabetic(10).getBytes());
		String fileName = String.format("export_%s.csv", modelName);

		AttachmentPersistenceResult attachmentPersistenceResult = mockAttachmentPersistenceResult();
		AttachmentUrl attachmentUrl = mockAttachmentUrl();

		when(composeDocumentModelLoader.loadModel(DocumentModelConstants.CONTRACT_CDM_MODEL)).thenReturn(composeDocumentModel);

		when(csvDocumentExporter.supports("csv")).thenReturn(true);
		when(csvDocumentExporter.export(any(), any())).thenReturn(inputStream);

		when(attachmentRepository.create(any(), eq(inputStream), eq(fileName), eq(TypeOfTheContent.ATTACHMENT_SECURED), any())).thenReturn(
			attachmentPersistenceResult);
		when(attachmentRepository.findUrl(any(), eq(fileName), eq(TypeOfTheContent.ATTACHMENT_SECURED))).thenReturn(Optional.of(attachmentUrl));

		RootBasedPageImpl<Void> result = (RootBasedPageImpl<Void>) exportCddProjectionImplementation.postprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build(), rootDocuments, queryContext);

		verify(csvDocumentExporter, times(1)).export(eq(composeDocumentModel), argThat((List<JsonNode> list) -> {
			Assert.assertEquals(list.size(), 1);
			Assert.assertEquals(list.getFirst(), rootDocuments.getContent().getFirst().getDocument());
			return true;
		}));

		verify(attachmentHeaderRepository, times(1)).create(argThat(attachmentHeader -> {
			Assert.assertEquals(attachmentHeader.getSize(), attachmentPersistenceResult.getSize());
			Assert.assertEquals(attachmentHeader.getMimeType(), attachmentPersistenceResult.getMimeType());
			Assert.assertEquals(attachmentHeader.getFilename(), fileName);
			return true;
		}));

		Assert.assertEquals(result.getOtherResults().get(ExportCddCsvProjectionImplementation.DOWNLOADED_URL_PARAM), attachmentUrl.getLocation());
	}

	private AttachmentPersistenceResult mockAttachmentPersistenceResult() {
		return AttachmentPersistenceResult.builder()
			.attachmentId(UUID.randomUUID().toString())
			.mimeType(RandomStringUtils.randomAlphabetic(10))
			.size(100)
			.build();
	}

	private AttachmentUrl mockAttachmentUrl() {
		return new AttachmentUrl(RandomStringUtils.randomAlphabetic(50));
	}

}
