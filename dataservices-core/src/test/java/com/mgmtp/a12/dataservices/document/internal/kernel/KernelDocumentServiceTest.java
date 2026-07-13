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
package com.mgmtp.a12.dataservices.document.internal.kernel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.LocaleUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.exception.DocumentComputationException;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IComputedFieldInstance;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentComputationResult;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;

import static org.testng.AssertJUnit.assertTrue;

public class KernelDocumentServiceTest extends AbstractDataServicesCoreTest {

	private IDocumentRtService rtService;
	private KernelDocumentService service;
	private DocumentV2 document;
	private Locale locale;

	@BeforeMethod
	public void setUp() {
		rtService = Mockito.mock(IDocumentRtService.class);
		service = new KernelDocumentService(
			true,
			List.of(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL),
			Collections.emptyList(),
			Collections.emptyList(),
			List.of("model2"),
			List.of(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL),
			rtService,
			documentModelResolver,
			true
		);
		document = Mockito.mock(DocumentV2.class);
		locale = LocaleUtils.toLocale("en");
	}

	@Test(expectedExceptions = InvalidInputException.class)
	public void testConstructorThrowsOnDuplicateModel() {
		new KernelDocumentService(
			true,
			Arrays.asList(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, "model2"),
			Collections.emptyList(),
			Collections.emptyList(),
			List.of("model2"),
			Collections.emptyList(),
			rtService,
			documentModelResolver,
			true
		);
	}

	@Test
	public void testComputeDocument_ComputationEnabled() {
		DocumentV2 computedDoc = Mockito.mock(DocumentV2.class);
		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(computedDoc).when(spyService).compute(document, locale);
		Mockito.when(document.getDocumentModelId()).thenReturn(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);

		DocumentV2 result = spyService.computeDocument(document, locale);

		Assert.assertEquals(result, computedDoc);
	}

	@Test
	public void testComputeDocument_ComputationNotEnabled() {
		DocumentV2 doc = Mockito.mock(DocumentV2.class);
		Mockito.when(doc.getDocumentModelId()).thenReturn("not-enabled-model");
		KernelDocumentService spyService = Mockito.spy(service);

		DocumentV2 result = spyService.computeDocument(doc, locale);

		Assert.assertEquals(result, doc);
	}

	@Test
	public void testValidateDocument_Partial() {
		Mockito.when(document.getDocumentModelId()).thenReturn(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
		IDocumentValidationResult validationResult = Mockito.mock(IDocumentValidationResult.class);
		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(validationResult).when(spyService).validatePartially(document, locale);

		Optional<IDocumentValidationResult> result = spyService.validateDocument(document, locale);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), validationResult);
	}

	@Test
	public void testValidateDocument_Skip() {
		Mockito.when(document.getDocumentModelId()).thenReturn("model2");
		Optional<IDocumentValidationResult> result = service.validateDocument(document, locale);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void testValidateDocument_Full() {
		Mockito.when(document.getDocumentModelId()).thenReturn("other-model");
		IDocumentValidationResult validationResult = Mockito.mock(IDocumentValidationResult.class);
		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(validationResult).when(spyService).validateFull(document, locale);

		Optional<IDocumentValidationResult> result = spyService.validateDocument(document, locale);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(result.get(), validationResult);
	}

	@Test
	public void testCompute_NoErrors() {
		IDocumentComputationResult computationResult = Mockito.mock(IDocumentComputationResult.class);
		Mockito.when(rtService.compute(Mockito.eq(document), Mockito.any(DocumentProcessingConfig.class))).thenReturn(computationResult);
		Mockito.when(computationResult.getComputedFieldInstancesWithErrors()).thenReturn(Collections.emptyList());
		Mockito.when(computationResult.applyTo(document)).thenReturn(document);

		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(locale).when(spyService).resolveLocale(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

		DocumentV2 result = spyService.compute(document, locale);

		Assert.assertEquals(result, document);
	}

	@Test(expectedExceptions = DocumentComputationException.class)
	public void testCompute_WithErrors() {
		Mockito.when(document.getDocumentModelId()).thenReturn(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
		IDocumentComputationResult computationResult = Mockito.mock(IDocumentComputationResult.class);
		IComputedFieldInstance errorField = Mockito.mock(IComputedFieldInstance.class);
		Mockito.when(errorField.pointer()).thenReturn(DocumentPointer.of("/Some/Field"));
		Mockito.when(errorField.getErrorMessage()).thenReturn(null);
		Mockito.when(rtService.compute(Mockito.eq(document), Mockito.any(DocumentProcessingConfig.class))).thenReturn(computationResult);
		Mockito.when(computationResult.getComputedFieldInstancesWithErrors()).thenReturn(Collections.singletonList(errorField));
		Mockito.when(computationResult.applyTo(document)).thenReturn(document);

		service.compute(document, locale);
	}

	@Test
	public void testValidateFull() {
		IDocumentValidationResult validationResult = Mockito.mock(IDocumentValidationResult.class);
		Mockito.when(rtService.validateFull(Mockito.eq(document), Mockito.any(DocumentProcessingConfig.class))).thenReturn(validationResult);

		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(locale).when(spyService).resolveLocale(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

		IDocumentValidationResult result = spyService.validateFull(document, locale);

		Assert.assertEquals(result, validationResult);
	}

	@Test
	public void testValidatePartially() {
		IDocumentValidationResult validationResult = Mockito.mock(IDocumentValidationResult.class);
		Mockito.when(rtService.validatePart(Mockito.eq(document), Mockito.anySet(), Mockito.any(DocumentProcessingConfig.class))).thenReturn(validationResult);

		Mockito.doAnswer(invocation -> {
			IDocumentV2Visitor visitor = invocation.getArgument(0);
			DocumentPointer pointer = Mockito.mock(DocumentPointer.class);
			FieldInstanceV2 field = Mockito.mock(FieldInstanceV2.class);
			visitor.visitField(pointer, field);
			return null;
		}).when(document).traverse(Mockito.any());

		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(locale).when(spyService).resolveLocale(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

		IDocumentValidationResult result = spyService.validatePartially(document, locale);

		Assert.assertEquals(result, validationResult);
	}

	@Test
	public void computeShouldCollectAndThrowErrorsIfCleanupIsSetToTrue() {
		IDocumentComputationResult compResult = Mockito.mock(IDocumentComputationResult.class);
		IComputedFieldInstance errorField = Mockito.mock(IComputedFieldInstance.class);

		Mockito.when(document.getId()).thenReturn(Optional.of("docId"));
		Mockito.when(errorField.pointer()).thenReturn(DocumentPointer.of("/Some/Field"));
		Mockito.when(errorField.getErrorMessage()).thenReturn(null);
		Mockito.when(compResult.getComputedFieldInstancesWithErrors()).thenReturn(List.of(errorField));
		Mockito.when(compResult.applyTo((DocumentV2) Mockito.any())).thenReturn(document);
		Mockito.when(rtService.compute(Mockito.any(DocumentV2.class), Mockito.any(DocumentProcessingConfig.class))).thenReturn(compResult);

		KernelDocumentService spyService = Mockito.spy(service);
		Mockito.doReturn(locale).when(spyService).resolveLocale(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

		try {
			spyService.compute(document, locale);
			Assert.fail("Expected DocumentComputationException");
		} catch (DocumentComputationException ex) {
			assertTrue(
				ex.getMessage().contains("Computation for field /Some/Field failed"));
		}
	}
}
