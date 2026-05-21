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
package com.mgmtp.a12.dataservices.state;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsistencyServiceIT extends AbstractSpringContextIT {

	@AfterClass public void cleanUp() {
		try {
			modelRepository.deleteAll();
			modelHeaderJpaRepository.deleteAll();

			modelService.delete(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
			modelService.delete(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		} catch (final Exception ex) {
			log.info("Some model cannot be deleted but it is ok because it might not have existed", ex);
		}
	}

	@SneakyThrows
	@BeforeClass private void setUp() {
		setUserTo(UserConstants.ADMIN_USER);
		super.cleanUpTestEnvironment();

		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
	}

	@Test public void modelConsistencyCheckTest() {
		ListIProblemReporter pr = new ListIProblemReporter();
		List<ModelHeaderEntity> all = modelHeaderJpaRepository.findAll();
		Assert.assertFalse(all.isEmpty());
		IDocumentModelService modelService = documentModelServiceFactory.createDocumentModelService();
		all.stream()
			.map(ModelHeaderEntity::getId)
			.map(documentModelResolver::getDocumentModelById)
			.forEach(d -> modelService.checkConsistency(d, pr));

		Assert.assertFalse(pr.hasProblems());
		Assert.assertEquals(pr.getProblems().size(), 0);
	}
}
