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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.ADDRESS_WITH_METADATA_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_AUTOMOTIVE_WITH_METADATA_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.MODELS_WITH_METADATA_ROOT_DIR;
import static com.mgmtp.a12.dataservices.constants.PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class })
public class ModelServiceIT extends AbstractSpringContextIT {

	private static final String TEST_ROLES = "admin,guest,tester";

	@Override
	protected void initializeWithSecurityBypass() throws Exception {
		cleanUpTestEnvironment();
		modelsFunctions.createModel(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(CONTRACT_AUTOMOTIVE_WITH_METADATA_MODEL_PATH);
	}

	@Test
	public void testModelLifecycle() {
		String modelContent = modelService.load(BUSINESS_PARTNER_DOCUMENT_MODEL).getContent().getRawContent();
		Assert.assertTrue(modelContent.contains("GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)"));

		modelContent = modelService.load(CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL).getContent().getRawContent();
		JSONAssert.assertEquals(loadResourceFromClasspathAsString(CONTRACT_AUTOMOTIVE_WITH_METADATA_MODEL_PATH), modelContent,
			JSONCompareMode.LENIENT);

		modelService.delete(BUSINESS_PARTNER_DOCUMENT_MODEL);
		modelService.delete(CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL);
		assertModelDeleted(BUSINESS_PARTNER_DOCUMENT_MODEL);
		assertModelDeleted(CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL);
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model cannot be created without id")
	public void createModelWithoutId() {
		String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
		modelService.create(removeFromHeader(model, "id"));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model \\[.*] does not have modelType defined")
	public void createModelWithoutModelType() {
		String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
		modelService.create(removeFromHeader(model, "modelType"));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model \\[.*] does not have roles defined")
	public void createModelWithoutRolesTest() {
		String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
		model = removeRoles(model);
		modelService.create(model);
	}

	private void assertModelDeleted(String modelName) {
		try {
			modelService.load(modelName);
			Assert.fail();
		} catch (NotFoundException e) {
			// pass
		}
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Relationship model \\[PartnerAddresses] is not valid:\n" +
	                                                                                          "\\[Entity: /content\\[1]/associationType\\[1] Type: OMISSION_ERROR Message: This field is required. ErrorCode: mandatoryField Rule: formalePruefung]")
	public void createInvalidRelationshipModel() throws HeaderParseException {
		String relationshipModel =
			loadResourceFromClasspathAsString(PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH).replace("\"associationType\": \"SHARED\",", "");
		modelService.create(relationshipModel);
		Assert.assertFalse(modelService.exists(headerParser.parseJson(relationshipModel)));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Relationship model \\[PartnerAddresses] is not valid:\n" +
	                                                                                          "\\[Entity: /content\\[1]/entityCharacteristics\\[1]/linkConstraints\\[1]/multiplicity\\[1]/upperLimit\\[1] Type: OMISSION_ERROR Message: When not unbounded, upperLimit must have a value .*]")
	public void createRelationshipModelBoundedWithoutUpperLimit() throws HeaderParseException {
		String relationshipModel = loadResourceFromClasspathAsString(PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH)
			.replace("\"unbounded\": true", "\"unbounded\": false");
		modelService.create(relationshipModel);
		Assert.assertFalse(modelService.exists(headerParser.parseJson(relationshipModel)));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model cannot be created without id")
	public void updateModelWithoutId() {
		String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
		modelService.update(removeFromHeader(model, "id"));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model \\[.*] does not have modelType defined")
	public void updateModelWithoutModelType() {
		String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
		modelService.update(removeFromHeader(model, "modelType"));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Model \\[.*] does not have roles defined")
	public void updateModelWithoutRolesTest() {
		try {
			modelService.delete(ADDRESS_DOCUMENT_MODEL);
			String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
			modelService.create(model);
			model = removeRoles(model);
			modelService.update(model);
		} finally {
			modelService.delete(ADDRESS_DOCUMENT_MODEL);
		}
	}

	@Test
	public void updateModel() throws HeaderParseException {
		modelService.delete(ADDRESS_DOCUMENT_MODEL);
		String model = loadResourceFromClasspathAsString(ADDRESS_DOCUMENT_MODEL_PATH);
		modelService.create(model);
		model = replaceRoles(model);
		modelService.update(model);
		String storedModel = modelService.load(ADDRESS_DOCUMENT_MODEL).getContent().getRawContent();
		JSONAssert.assertEquals(loadResourceFromClasspathAsString(MODELS_WITH_METADATA_ROOT_DIR + "Address_with_metadata-ModelServiceIT.json"),
			storedModel, false);
		Header header = headerParser.parseJson(storedModel);
		assertHeaderRoles(header, TEST_ROLES);
	}

	@Test public void findAllHeaders() {
		List<Header> documentModels = modelService.findAllHeadersByType("document");
		int documentModelCount = documentModels.size();
		modelService.create(loadResourceFromClasspathAsString(COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH));
		modelService.create(loadResourceFromClasspathAsString(CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH));
		documentModels = modelService.findAllHeadersByType("document");
		Assert.assertEquals(documentModels.size(), documentModelCount + 1);
	}

	@Test
	public void manageModelWithModelManageRight() throws HeaderParseException {
		setUserTo("model_manager_user");

		String model = loadResourceFromClasspathAsString(ADDRESS_WITH_METADATA_MODEL_PATH);
		modelService.create(model);
		String storedModel = modelService.load(ADDRESS_DOCUMENT_MODEL).getContent().getRawContent();
		JSONAssert.assertEquals(model, storedModel, JSONCompareMode.LENIENT);

		model = replaceRoles(model);
		modelService.update(model);
		storedModel = modelService.load(ADDRESS_DOCUMENT_MODEL).getContent().getRawContent();
		JSONAssert.assertEquals(model, storedModel, JSONCompareMode.LENIENT);

		Header header = headerParser.parseJson(storedModel);
		assertHeaderRoles(header, TEST_ROLES);
		modelService.delete(ADDRESS_DOCUMENT_MODEL);
		assertModelDeleted(ADDRESS_DOCUMENT_MODEL);
	}

	private void assertHeaderRoles(Header header, String searchedRoles) {
		Assert.assertEquals(Optional.ofNullable(header.getAnnotations()).stream()
			.flatMap(Collection::stream)
			.filter(e -> "roles".equalsIgnoreCase(e.getName()))
			.findFirst()
			.map(Annotation::getValue)
			.orElseThrow(IllegalStateException::new), searchedRoles);
	}

	private String removeFromHeader(String model, String headerField) {
		JSONObject jsonModel = new JSONObject(model);
		jsonModel.getJSONObject("header").remove(headerField);
		return jsonModel.toString();
	}

	private String replaceRoles(String model) {
		JSONObject jsonModel = new JSONObject(model);
		JSONArray annotations = jsonModel.getJSONObject("header").getJSONArray("annotations");
		for (int i = 0; i < annotations.length(); i++) {
			JSONObject obj = annotations.getJSONObject(i);
			String nameField = obj.getString("name");
			if ("roles".equals(nameField)) {
				obj.put("value", TEST_ROLES);
				break;
			}
		}
		return jsonModel.toString();
	}

	private String removeRoles(String model) {
		JSONObject jsonModel = new JSONObject(model);
		JSONArray annotations = jsonModel.getJSONObject("header").getJSONArray("annotations");
		for (int i = 0; i < annotations.length(); i++) {
			String nameField = annotations.getJSONObject(i).getString("name");
			if ("roles".equals(nameField)) {
				annotations.remove(i);
				break;
			}
		}
		return jsonModel.toString();
	}
}
