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
package com.mgmtp.a12.dataservices.model;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.InitialITConfiguration;
import com.mgmtp.a12.dataservices.model.cdm.persistence.ComposeDocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.GenericModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelReadRepository;

import lombok.AllArgsConstructor;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.NATURAL_PERSON_CDM;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CDM_TEMPLATE_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL;
import static com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.PARTNER_ADDRESSES_MODEL;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(classes = { InitialITConfiguration.class })
public class ModelReadCachesIT extends AbstractSpringContextIT {

	private static final String NATURAL_PERSON_2_CDM = "NaturalPerson2CDM";

	@MockitoSpyBean private ModelJpaRepository jpaSpy;
	@Autowired private GenericModelReadRepository genericModelReadRepository;
	@Autowired private DocumentModelReadRepository documentModelReadRepository;
	@Autowired private RelationshipModelReadRepository relationshipModelReadRepository;
	@Autowired private ComposeDocumentModelReadRepository composeDocumentModelReadRepository;

	@BeforeMethod
	public void setUp() {
		cleanUpTestEnvironment();
		modelsFunctions.createModels(
			BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
			ADDRESS_DOCUMENT_MODEL_PATH,
			PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH,
			CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH);
		modelsFunctions.saveCdm(CDM_TEMPLATE_PATH, NATURAL_PERSON_CDM);
		modelsFunctions.saveCdm(CDM_TEMPLATE_PATH, NATURAL_PERSON_2_CDM);
		modelCacheManager.invalidateModelReadCaches(BUSINESS_PARTNER_DOCUMENT_MODEL);
		modelCacheManager.invalidateModelReadCaches(ADDRESS_DOCUMENT_MODEL);
		modelCacheManager.invalidateModelReadCaches(PARTNER_ADDRESSES_MODEL);
		modelCacheManager.invalidateModelReadCaches(CONTRACT_BUSINESS_PARTNER_MODEL);
		Mockito.reset(jpaSpy);
	}

	@DataProvider
	public Object[][] modelReadData() throws IOException {
		return new Object[][]{
			{"GenericModelWithDM", new TestData(genericModelReadRepository, BUSINESS_PARTNER_DOCUMENT_MODEL, resourceFunctions.loadResource(
				BUSINESS_PARTNER_DOCUMENT_MODEL_PATH), ADDRESS_DOCUMENT_MODEL)},
			{"GenericModelWithRM", new TestData(genericModelReadRepository, PARTNER_ADDRESSES_MODEL, resourceFunctions.loadResource(
				PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH), CONTRACT_BUSINESS_PARTNER_MODEL)},
			{"DocumentModel", new TestData(documentModelReadRepository, BUSINESS_PARTNER_DOCUMENT_MODEL, resourceFunctions.loadResource(
				BUSINESS_PARTNER_DOCUMENT_MODEL_PATH), ADDRESS_DOCUMENT_MODEL)},
			{"RelationshipModel", new TestData(relationshipModelReadRepository, PARTNER_ADDRESSES_MODEL, resourceFunctions.loadResource(
				PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH), CONTRACT_BUSINESS_PARTNER_MODEL)},
			{"ComposeDocumentModel", new TestData(composeDocumentModelReadRepository, NATURAL_PERSON_CDM, resourceFunctions.loadResource(
				CDM_TEMPLATE_PATH).formatted(NATURAL_PERSON_CDM), NATURAL_PERSON_2_CDM)}
		};
	}

	@Test(dataProvider = "modelReadData")
	public void assertCacheHitAfterFirstCall(String description, TestData testData) {
		testData.modelReadRepository.readModel(testData.model1Name);
		testData.modelReadRepository.readModel(testData.model1Name);
		verifySpyTimesCalled(1);
	}

	@Test(dataProvider = "modelReadData")
	public void assertCacheHappensPerModel(String description, TestData testData) {
		testData.modelReadRepository.readModel(testData.model1Name);
		testData.modelReadRepository.readModel(testData.model2Name);
		verifySpyTimesCalled(2);
	}

	@Test(dataProvider = "modelReadData")
	public void assertCacheMissAfterModelUpdate(String description, TestData testData) {
		testData.modelReadRepository.readModel(testData.model1Name);
		verifySpyTimesCalled(1);
		modelsFunctions.updateModelContent(testData.model1Content);
		Mockito.reset(jpaSpy);
		testData.modelReadRepository.readModel(testData.model1Name);
		verifySpyTimesCalled(1);
	}

	private void verifySpyTimesCalled(int numberOfCalls) {
		Mockito.verify(jpaSpy, Mockito.times(numberOfCalls))
			.findById(anyString());
	}

	@AllArgsConstructor
	public static class TestData {
		IModelReadRepository modelReadRepository;
		String model1Name;
		String model1Content;
		String model2Name;
	}

}
