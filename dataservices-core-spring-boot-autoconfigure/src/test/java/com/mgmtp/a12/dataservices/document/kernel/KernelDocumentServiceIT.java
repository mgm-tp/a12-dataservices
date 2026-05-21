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
package com.mgmtp.a12.dataservices.document.kernel;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class KernelDocumentServiceIT extends AbstractSpringContextIT {

	private static final String COMPUTATION_PREFIX = "computation/";
	private static final String VALIDATION_PREFIX = "validation/";
	private static final String TEST_COMPUTE_CLEAN_UP_DOCUMENT_MODEL = "TestComputeCleanUp";

	@Autowired private IDocumentRtService rtService;
	@Autowired private IDocumentModelResolver documentModelResolver;
	private DocumentV2 businessPartner1;
	private DocumentV2 address1;
	private DocumentV2 address2;
	private DocumentV2 testComputeCleanUp1;

	@BeforeClass
	public void init() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_LTD_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.ADDRESS_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel("/model/other/TestComputeCleanUp.json");

		String doc1 = loadResourceFromClasspathAsString(PathConstants.DOCUMENTS_KERNEL_PATH + COMPUTATION_PREFIX + "BusinessPartnerLTD-1.json");
		businessPartner1 = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL,new StringReader(doc1));

		doc1 = loadResourceFromClasspathAsString(PathConstants.DOCUMENTS_KERNEL_PATH + VALIDATION_PREFIX + "Address-1.json");
		address1 = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(doc1));
		doc1 = loadResourceFromClasspathAsString(PathConstants.DOCUMENTS_KERNEL_PATH + VALIDATION_PREFIX + "Address-2.json");
		address2 = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(doc1));
		doc1 = loadResourceFromClasspathAsString(PathConstants.DOCUMENTS_KERNEL_PATH + COMPUTATION_PREFIX + "TestComputeCleanUp-1.json");
		testComputeCleanUp1 = documentSupport.convertJSONToDocument(TEST_COMPUTE_CLEAN_UP_DOCUMENT_MODEL, new StringReader(doc1));
	}

	@DataProvider
	public static Object[][] computationList() {
		return new Object[][] {
				{ List.of("CustomModel", DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL) },
				{ List.of(DataServicesCoreProperties.MATCH_ALL) },
				{ List.of(DataServicesCoreProperties.MATCH_ALL, DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL) },
				{ null }
		};
	}
	@DataProvider
	public static Object[][] nonComputationList() {
		return new Object[][] {
			{ List.of() },
			{ List.of("CustomModel") },
			{ List.of(DataServicesCoreProperties.MATCH_ALL, "CustomModel") },
			{ null }
		};
	}

	@Test(dataProvider = "computationList")
	public void checkComputationIsEnabledExplicitly(List<String> modelList) {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true, List.of(), List.of(), modelList, false);

		businessPartner1 = kernelDocumentService.computeDocument(businessPartner1, null);
		validateComputation(businessPartner1,"/BusinessPartnerRoot/PersonOrEntity", "Legal Entity");
	}
	@Test(dataProvider = "nonComputationList")
	public void checkComputationIsDisabled(List<String> modelList) {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true, List.of(), List.of(), modelList, false);

		businessPartner1 = kernelDocumentService.computeDocument(businessPartner1, null);
		validateComputation(businessPartner1, "/BusinessPartnerRoot/PersonOrEntity", null);
	}
	@Test(dataProvider = "nonComputationList")
	public void computeTest(List<String> modelList) {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true, List.of(), List.of(), modelList, false);

		businessPartner1 = kernelDocumentService.compute(businessPartner1, null);
		validateComputation(businessPartner1, "/BusinessPartnerRoot/PersonOrEntity", "Legal Entity");
	}
	@Test
	public void checkValidationIsEnabled() {
		// Arrange: validation enabled, no skip/partial for the model
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(
			true, List.of(), List.of(), List.of(), false);

		// Act: validate a document known to have a validation error
		Optional<IDocumentValidationResult> result = kernelDocumentService.validateDocument(address1, null);

		// Assert: validation result is present and contains the expected error
		assertTrue(result.isPresent());
		IDocumentValidationResult validationResult = result.get();
		assertFalse(validationResult.noErrorOccurred());
		IMessage message = validationResult.getMessages().getFirst();
		assertEquals(message.getErrorCode(), "Error rule_7c66e");
		assertEquals(message.getErrorText(), "No country provided");
		assertEquals(message.getRulePath().orElse(""), "/AddressRoot/MustContainCountry");
	}
	@Test
	public void checkValidationPartialForModelPassing() {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true, List.of(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL), List.of(), List.of(), false);

		Optional<IDocumentValidationResult> testResults = kernelDocumentService.validateDocument(address1, null);
		assertTrue(testResults.isPresent());
		assertTrue(testResults.get().noErrorOccurred());
	}
	@Test
	public void checkValidationPartialForModelFailing() {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(
			true,
			List.of(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL), // enable partial validation for this model
			List.of(),
			List.of(),
			false
		);

		// Address2 contains an empty country attribute, which should trigger a validation error
		Optional<IDocumentValidationResult> result = kernelDocumentService.validateDocument(address2, null);

		assertTrue(result.isPresent());
		IDocumentValidationResult validationResult = result.get();
		assertFalse(validationResult.noErrorOccurred());
		IMessage message = validationResult.getMessages().getFirst();
		assertEquals(message.getErrorCode(), "Error rule_7c66e");
		assertEquals(message.getErrorText(), "No country provided");
		assertEquals(message.getRulePath().orElse(""), "/AddressRoot/MustContainCountry");
	}
	@Test
	public void checkValidationSkipForModel() {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true, List.of(), List.of(
				DocumentModelConstants.ADDRESS_DOCUMENT_MODEL),
			List.of(),
			false);

		assertFalse(kernelDocumentService.validateDocument(address1, null).isPresent());
	}

	@Test
	public void checkComputedFieldShouldBeNullIfEnableCleanUpErrorField() {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true,
			List.of(),
			List.of(),
			List.of(TEST_COMPUTE_CLEAN_UP_DOCUMENT_MODEL),
			true);

		testComputeCleanUp1 = kernelDocumentService.compute(testComputeCleanUp1, null);
		validateComputation(testComputeCleanUp1, "/TestComputeCleanUp/CompField", null);
	}

	@Test
	public void checkComputedFieldShouldBeKeptIfDisableEnableCleanUpErrorField() {
		KernelDocumentService kernelDocumentService = prepareKernelDocumentServiceInstance(true,
			List.of(),
			List.of(),
			List.of(TEST_COMPUTE_CLEAN_UP_DOCUMENT_MODEL),
			false);

		testComputeCleanUp1 = kernelDocumentService.compute(testComputeCleanUp1, null);
		validateComputation(testComputeCleanUp1, "/TestComputeCleanUp/CompField", "Z");
	}

	private KernelDocumentService prepareKernelDocumentServiceInstance(boolean validation, List<String> partialValidationForModels, List<String> skipValidationForModels,
			List<String> computationEnabledForModels, boolean enabledCleanupErrorAndNotComputedValue) {

		return new KernelDocumentService(validation, partialValidationForModels, skipValidationForModels, computationEnabledForModels, rtService, documentModelResolver, enabledCleanupErrorAndNotComputedValue);
	}

	private void validateComputation(DocumentV2 document, String path, String expectedString) {
		Object valueFound = document.fieldValue(path);
		if (expectedString == null && valueFound == null) {
			assertTrue(true, "Looking for null and found null");
		} else if (expectedString != null && valueFound == null) {
			Assert.fail(path + " not found");
		} else {
			assertEquals(valueFound.toString(), expectedString);
		}
	}
	
}
