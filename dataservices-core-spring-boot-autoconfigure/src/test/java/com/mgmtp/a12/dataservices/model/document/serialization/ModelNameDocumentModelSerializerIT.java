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
package com.mgmtp.a12.dataservices.model.document.serialization;

import java.io.IOException;
import java.io.Reader;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IIdNamed;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;

import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_MODEL_RESOLVER_PATH;
import static com.mgmtp.a12.dataservices.constants.UserConstants.ADMIN_USER;

public class ModelNameDocumentModelSerializerIT extends AbstractSpringContextIT {

	private static final String INCLUDED_MODEL = DOCUMENT_MODEL_RESOLVER_PATH + "IncludedModel.json";
	private static final String BASE_MODEL = DOCUMENT_MODEL_RESOLVER_PATH + "BaseModel.json";
	private static final String BASE_MODEL_WITH_WRONG_INCLUDE = DOCUMENT_MODEL_RESOLVER_PATH + "BaseModelWithWrongInclude.json";

	@Autowired private IDocumentModelSerializer includeAwareDocumentModelXmlSerializer;

	@BeforeClass public void init() {
		setUserTo(ADMIN_USER);
		modelsFunctions.createModel(INCLUDED_MODEL);
	}

	@Test public void testIncludeResolveDeserializationOK() throws IOException {
		IDocumentModel documentModel;
		try (Reader baseModelContent = loadResourceAsReader(BASE_MODEL)) {
			documentModel = includeAwareDocumentModelXmlSerializer.deserialize(baseModelContent);
		}
		documentModelService.expand(documentModel, collapsingDocumentModelReferenceResolverFactory.getInstance(documentModelResolver));
		Assert.assertNotNull(documentModel);
		Assert.assertNotNull(documentModel.getContent().getTypeDefinitions());
		Assert.assertEquals(documentModel.getContent().getTypeDefinitions().size(), 1);
		Assert.assertTrue(documentModel.getContent().getTypeDefinitions().stream()
			.anyMatch(fieldTypeDefinition -> fieldTypeDefinition.getName().equals("IncludedType")));
		IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
		checkFieldExistence("/base/aField", "F3", documentModelSearchService);
		checkFieldExistence("/base/aGroup/anotherField", "F5", documentModelSearchService);
		checkFieldExistence("/base/base/IncludedField1", "I6_F3", documentModelSearchService);
		checkFieldExistence("/base/base/IncludedField2", "I6_F4", documentModelSearchService);
		checkFieldExistence("/base/base/sub/IncludedField3", "I6_F6", documentModelSearchService);
		checkFieldExistence("/base/base/sub/IncludedField4", "I6_F8", documentModelSearchService);
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Document model \\[NonExistingInclude] not found")
	public void testIncludeResolveDeserializationNOK() throws IOException {
		try (Reader baseModelContent = loadResourceAsReader(BASE_MODEL_WITH_WRONG_INCLUDE)) {
			IDocumentModel model = includeAwareDocumentModelXmlSerializer.deserialize(baseModelContent);
			documentModelService.expand(model, collapsingDocumentModelReferenceResolverFactory.getInstance(documentModelResolver));
		}
	}

	private void checkFieldExistence(final String path, final String id, final IDocumentModelSearchService documentModelSearchService) {
		Assert.assertTrue(documentModelSearchService.getByPath(path)
			.map(IIdNamed::getId)
			.filter(id::equals)
			.isPresent());
	}
}
