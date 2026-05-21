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
package com.mgmtp.a12.dataservices.model.cdm;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.cdm.persistence.ComposeDocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.cdm.persistence.internal.ComposeDocumentModelLoader;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipInvalidDocumentReferencesException;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.NATURAL_PERSON_CDM;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.NATURAL_PERSON_CDM_WRONG_CRD;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.NATURAL_PERSON_CDM_WRONG_NESTED_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.NATURAL_PERSON_CDM_WRONG_RM;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CDM_TEMPLATE_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CDM_TEMPLATE_WRONG_CRD_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CDM_TEMPLATE_WRONG_NESTED_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CDM_TEMPLATE_WRONG_RM_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH;

public class ComposeDataDocumentModelIT extends AbstractSpringContextIT {

	@Autowired private ComposeDocumentModelReadRepository composeDocumentModelReadRepository;
	@Autowired private ComposeDocumentModelLoader composeDocumentModelLoader;
	@Autowired ModelService modelService;

	protected void initializeWithSecurityBypass() throws Exception {
		cleanUpTestEnvironment();
		modelsFunctions.saveCdm(CDM_TEMPLATE_PATH, NATURAL_PERSON_CDM);
		modelsFunctions.saveCdm(CDM_TEMPLATE_WRONG_CRD_PATH, NATURAL_PERSON_CDM_WRONG_CRD);
		modelsFunctions.saveCdm(CDM_TEMPLATE_WRONG_RM_PATH, NATURAL_PERSON_CDM_WRONG_RM);
		modelsFunctions.saveCdm(CDM_TEMPLATE_WRONG_NESTED_MODEL_PATH, NATURAL_PERSON_CDM_WRONG_NESTED_MODEL);

		modelsFunctions.createModels(
			PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH,
			CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH);
		modelsFunctions.createModels(
			BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
			ADDRESS_DOCUMENT_MODEL_PATH,
			CONTRACT_DOCUMENT_MODEL_PATH,
			BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
	}

	@Test public void testCdmServedByCdmLoader() {
		composeDocumentModelLoader.loadModel(NATURAL_PERSON_CDM);
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Model \\[WrongCrd] not found")
	public void testCdmByCdmLoaderWrongCrd() {
		composeDocumentModelLoader.loadModel(NATURAL_PERSON_CDM_WRONG_CRD);
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Relationship model \\[WrongRM] not found")
	public void testCdmByCdmLoaderWrongRm() {
		composeDocumentModelLoader.loadModel(NATURAL_PERSON_CDM_WRONG_RM);
	}

	@Test(expectedExceptions = RelationshipInvalidDocumentReferencesException.class, expectedExceptionsMessageRegExp = "Relationship model \\[ContractCoInsuredPartner] contains invalid document references")
	public void testCdmByCdmLoaderWrongNestedModel() {
		composeDocumentModelLoader.loadModel(NATURAL_PERSON_CDM_WRONG_NESTED_MODEL);
	}

	@Test public void testCdmServedByCdmRepository() {
		composeDocumentModelReadRepository.readModel(NATURAL_PERSON_CDM);
	}

}
