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
import java.io.StringReader;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IIdNamed;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.SneakyThrows;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_MODEL_RESOLVER_PATH;
import static com.mgmtp.a12.dataservices.constants.UserConstants.ADMIN_USER;

public class DocumentModelServiceIT extends AbstractSpringContextIT {

	private static final String INCLUDED_MODEL_PATH = DOCUMENT_MODEL_RESOLVER_PATH + "IncludedModel.json";
	private static final String INCLUDED_MODEL2_PATH = DOCUMENT_MODEL_RESOLVER_PATH + "IncludedModel2.json";
	private static final String BASE_MODEL2_PATH = DOCUMENT_MODEL_RESOLVER_PATH + "BaseModel2.json";
	private static final String BASE_MODEL2_INIT_PATH = DOCUMENT_MODEL_RESOLVER_PATH + "BaseModel2Init.json";
	private static final String BASE_MODEL2_NAME = "BaseModel2";

	@BeforeClass public void init() throws IOException {
		setUserTo(ADMIN_USER);
		final String includedModelContent = loadResourceFromClasspathAsString(INCLUDED_MODEL_PATH);
		modelService.create(includedModelContent);
		final String includedModel2Content = loadResourceFromClasspathAsString(INCLUDED_MODEL2_PATH);
		modelService.create(includedModel2Content);
		final String baseModel2 = loadResourceFromClasspathAsString(BASE_MODEL2_INIT_PATH);
		modelService.create(baseModel2);
		modelService.create(loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH));
	}

	@DataProvider public static Object[][] invalidModelNameProvider() {
		return new Object[][] {
			new Object[] { "0BusinessPartner" },
			new Object[] { "BusinessPartnerBusinessPartnerBusinessPartnerBusinessPartnerBusinessPartnerBusinessPartnerBusinessPar" },
			new Object[] { "_BusinessPartner-BusinessPartner+BusinessPartner" }
		};
	}

	@Test(dataProvider = "invalidModelNameProvider", expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model name .* is not valid")
	public void updateModelWithInvalidName(String invalidModelName) {
		modelService.create(loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH).replace(BUSINESS_PARTNER_DOCUMENT_MODEL, invalidModelName));
	}

	@SneakyThrows
	private IDocumentModel deserializeModel(String reference) {
		return documentModelSerializer.deserialize(
			new StringReader(modelService.load(reference).getContent().getRawContent()));
	}

	private boolean doesFieldExist(final String path, final String id, final IDocumentModelSearchService documentModelSearchService) {
		return documentModelSearchService
			.getByPath(path)
			.map(IIdNamed::getId)
			.filter(id::equals)
			.isPresent();
	}
}
