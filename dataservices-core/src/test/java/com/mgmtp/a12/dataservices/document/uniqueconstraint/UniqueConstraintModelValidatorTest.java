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
package com.mgmtp.a12.dataservices.document.uniqueconstraint;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.exception.ModelSerializationException;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.internal.UniqueConstraintModelValidator;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;

/**
 * Unit tests for `UniqueConstraintModelValidator`.
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueConstraintModelValidatorTest {

	@InjectMocks
	private UniqueConstraintModelValidator validator;

	@Mock
	private DocumentModelService documentModelService;
	@Mock
	private DocumentModelUtils documentModelUtils;

	@BeforeMethod
	public void setUp() {
		Mockito.reset(documentModelService, documentModelUtils);
	}

	@Test(description = "Should skip validation for non-document model types")
	public void shouldSkipValidateModelWhenModelTypeIsNotDocument() {
		Header header = mock(Header.class);
		when(header.getModelType()).thenReturn("relationship");
		GenericModel genericModel = GenericModel.of(header, "rawContent");

		validator.validateModel(genericModel);

		Mockito.verifyNoInteractions(documentModelUtils, documentModelService);
	}

	@Test(description = "Should invoke checkConsistency for document model type when no problems are reported")
	public void shouldPassValidateModelWhenCheckConsistencyReportsNoProblems() {
		Header header = mock(Header.class);
		when(header.getModelType()).thenReturn(ModelConstants.DOCUMENT_MODEL_TYPE);
		when(header.getId()).thenReturn("MyDocModel");
		GenericModel genericModel = GenericModel.of(header, "rawContent");

		IDocumentModel iDocumentModel = mock(IDocumentModel.class);
		DocumentModel documentModel = mock(DocumentModel.class);
		DocumentModelContent documentModelContent = mock(DocumentModelContent.class);
		DocumentUniquenessCriterion documentUniquenessCriterion = mock(DocumentUniquenessCriterion.class);
		when(documentModelUtils.deserializeDocumentModel("MyDocModel", "rawContent")).thenReturn(iDocumentModel);
		when(documentModelService.convertFromExternal(iDocumentModel)).thenReturn(documentModel);
		when(documentModel.getContent()).thenReturn(documentModelContent);
		when(documentModelContent.getDocumentUniquenessCriteria()).thenReturn(List.of(documentUniquenessCriterion));

		validator.validateModel(genericModel); // no exception

		Mockito.verify(documentModelService).checkDocumentUniquenessCriterionConsistency(
			Mockito.eq(documentModel), Mockito.eq(documentUniquenessCriterion), Mockito.any(ListIProblemReporter.class));
	}

	@Test(description = "Should throw ModelSerializationException when checkConsistency reports problems")
	public void shouldThrowModelSerializationExceptionWhenCheckConsistencyReportsProblems() {
		Header header = mock(Header.class);
		when(header.getModelType()).thenReturn(ModelConstants.DOCUMENT_MODEL_TYPE);
		when(header.getId()).thenReturn("MyDocModel");
		GenericModel genericModel = GenericModel.of(header, "rawContent");

		IDocumentModel iDocumentModel = mock(IDocumentModel.class);
		DocumentModel documentModel = mock(DocumentModel.class);
		DocumentModelContent documentModelContent = mock(DocumentModelContent.class);
		DocumentUniquenessCriterion documentUniquenessCriterion = mock(DocumentUniquenessCriterion.class);
		when(documentModelUtils.deserializeDocumentModel("MyDocModel", "rawContent")).thenReturn(iDocumentModel);
		when(documentModelService.convertFromExternal(iDocumentModel)).thenReturn(documentModel);
		when(documentModel.getContent()).thenReturn(documentModelContent);
		when(documentModelContent.getDocumentUniquenessCriteria()).thenReturn(List.of(documentUniquenessCriterion));

		Mockito.doAnswer(invocation -> {
			ListIProblemReporter reporter = invocation.getArgument(2);
			reporter.reportProblem(mock(IProblem.class));
			return null;
		}).when(documentModelService).checkDocumentUniquenessCriterionConsistency(Mockito.any(), Mockito.any(), Mockito.any());

		Assert.assertThrows(ModelSerializationException.class, () -> validator.validateModel(genericModel));
	}
}
