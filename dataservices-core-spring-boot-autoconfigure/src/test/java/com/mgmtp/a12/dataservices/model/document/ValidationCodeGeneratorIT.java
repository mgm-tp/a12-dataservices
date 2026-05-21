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
package com.mgmtp.a12.dataservices.model.document;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.model.document.internal.ValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_MODEL_PATH;

public class ValidationCodeGeneratorIT extends AbstractSpringContextIT {

	private static final String CORRUPTED_MODEL = "CorruptedModel";
	@Autowired private ValidationCodeGenerator validationCodeGenerator;

	@BeforeClass public void setUp() throws IllegalAccessException, InstantiationException, IOException, InvocationTargetException, NoSuchMethodException {
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(DOCUMENT_MODEL_PATH + "DomainCorrupted.json");
	}

	@Test public void checkCodeGeneration() {
		final ModelEntity documentModelEntity = modelRepository.findById(BUSINESS_PARTNER_DOCUMENT_MODEL).orElseThrow(null);
		ListIProblemReporter pr = new ListIProblemReporter();
		final String validatingCode = validationCodeGenerator.generateValidationCode(documentModelUtils.deserializeDocumentModel(documentModelEntity), pr);
		MatcherAssert.assertThat(validatingCode, Matchers.not(Matchers.emptyOrNullString()));
	}

	@Test public void checkCaching() {
		final StopWatch watch = new StopWatch();
		watch.start("initial generation");
		ListIProblemReporter pr = new ListIProblemReporter();
		validationCodeGenerator.getValidationCode(BUSINESS_PARTNER_DOCUMENT_MODEL, pr);
		pr.validate(0, "");
		watch.stop();
		watch.start("repeated generation");
		validationCodeGenerator.getValidationCode(BUSINESS_PARTNER_DOCUMENT_MODEL, pr);
		pr.validate(0, "");
		watch.stop();
		watch.start("update Model");
		String modelContent = genericModelLoader.loadModel(BUSINESS_PARTNER_DOCUMENT_MODEL).getContent().getRawContent();
		modelService.update(modelContent);
		watch.stop();
		watch.start("repeated generation after update");
		validationCodeGenerator.getValidationCode(BUSINESS_PARTNER_DOCUMENT_MODEL, pr);
		pr.validate(0, "");
		watch.stop();

		watch.start("repeated generation after update with cache");
		validationCodeGenerator.getValidationCode(BUSINESS_PARTNER_DOCUMENT_MODEL, pr);
		pr.validate(0, "");
		watch.stop();
		logger.info(watch.prettyPrint());
		// There is no assert because it's hard to assert something. Even 2 repetitive calls have totally different times.

	}

	@Test public void testValidationExceptionCreation() {
		try {
			ListIProblemReporter pr = new ListIProblemReporter();
			validationCodeGenerator.getValidationCode(CORRUPTED_MODEL, pr);
			Assert.fail("Service should have thrown ValidationException");
		} catch (final DataServicesDocumentProblemReporterException validationException) {
			List<IProblem> errors = validationException.getProblems();
			Assert.assertNotNull(errors);
			Assert.assertEquals(errors.size(), 4);
			Assert.assertEquals(errors.get(0).getMessage(),
				"The maximum value of the number type in field [id: NumberField1] is specified with '1E+15'. It may not exceed '9999999999999.99'.");
			Assert.assertEquals(errors.get(1).getMessage(),
				"The maximum value of the number type in field [id: NumberField2] is specified with '1E+15'. It may not exceed '9999999999999.99'.");
			Assert.assertEquals(errors.get(2).getMessage(),
				"Field with path '/root/NumberField2': Only values with up to 15 digits are allowed.");
			Assert.assertEquals(errors.get(3).getMessage(),
				"Field with path '/root/NumberField1': Only values with up to 15 digits are allowed.");
		}
	}
}
