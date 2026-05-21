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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.GenericModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;

import static org.testng.Assert.assertTrue;

@Test public class ListValidationCodesOperationTest extends AbstractSpringContextIT {

	@Autowired ListValidationCodesOperation listValidationCodesOperation;

	@Override protected void initializeWithSecurityBypass() throws Exception {
		for (Resource r : resourcePatternResolver.getResources(PathConstants.DOCUMENT_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.RELATIONSHIP_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.CDM_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.OTHER_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
	}

	@Test public void testListValidationsOperation() {
		Map<String, String> results = listValidationCodesOperation.rpc(
			List.of(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, DocumentModelConstants.CONTRACT_CDM_MODEL,
				DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)).getDocumentValidationCodes();
		assertValidationCode(results.get(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL));
		assertValidationCode(results.get(DocumentModelConstants.CONTRACT_CDM_MODEL));
		assertValidationCode(results.get(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL));
	}

	private static void assertValidationCode(String modelName) {
		assertTrue(modelName.startsWith("/**"));
		assertTrue(modelName.length() > 20000);
	}

	@Test(expectedExceptions = NotFoundException.class) public void testListValidationsOperationOverview() {
		listValidationCodesOperation.rpc(List.of(GenericModelConstants.CONTRACT_OVERVIEW_MODEL));
	}

	@Test(expectedExceptions = NotFoundException.class) public void testListValidationsForm() {
		listValidationCodesOperation.rpc(List.of(GenericModelConstants.CONTRACT_FORM_MODEL));
	}

	@Test(expectedExceptions = NotFoundException.class) public void testListValidationsRelationship() {
		listValidationCodesOperation.rpc(List.of(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL));
	}
}
