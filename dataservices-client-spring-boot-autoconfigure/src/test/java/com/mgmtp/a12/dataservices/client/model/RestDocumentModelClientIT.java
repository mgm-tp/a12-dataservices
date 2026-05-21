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
package com.mgmtp.a12.dataservices.client.model;

import java.io.StringReader;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.client.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.client.exception.A12ClientException;

public class RestDocumentModelClientIT extends AbstractSpringContextIT {

	private String contractModelContent;

	@BeforeMethod
	public void setUp() {
		contractModelContent = readFile(CONTRACT_MODEL_FILE);
		createDocumentModelIfNotExist(contractModelContent);
	}

	@AfterMethod
	@BeforeClass
	public void cleanup() {
		modelsClient.deleteModel(CONTRACT_MODEL_NAME);
		modelsClient.deleteModel(ADDRESS_MODEL_NAME);
	}

	@Test(expectedExceptions = A12ClientException.class)
	public void deleteModel() {
		try {
			modelsClient.deleteModel(CONTRACT_MODEL_NAME);
		} catch (Exception ex) {
			Assert.fail("this operation should not fail", ex);
		}
		// This one should throw exception because model is missing after deletion
		modelsClient.loadModel(CONTRACT_MODEL_NAME);
		Assert.fail("Model should be missing");
	}

	@Test
	public void updateModel() {
		String updatedModelContent = modelsClient.updateModel(contractModelContent);
		Assert.assertNotNull(updatedModelContent);
	}

	@Test
	public void createModel() {
		try {
			String brandContent = readFile(ADDRESS_MODEL_FILE);
			String model = modelsClient.createModel(new StringReader(brandContent));
			Assert.assertNotNull(model);
		} catch (Exception ex) {
			Assert.fail("model creation failed", ex);
		}
		String loadedModel = modelsClient.loadModel(ADDRESS_MODEL_NAME);
		Assert.assertNotNull(loadedModel);
	}

	@Test
	public void generateJavascriptCode() {
		String generateValidationCode = modelsClient.generateValidationCode(CONTRACT_MODEL_NAME);
		Assert.assertNotNull(generateValidationCode);
		//This is taken from the validation code to test the encoding issues with Ü
		Assert.assertTrue(generateValidationCode.contains("Übergebenes Ergebnis darf nicht null sein"));
	}
}
