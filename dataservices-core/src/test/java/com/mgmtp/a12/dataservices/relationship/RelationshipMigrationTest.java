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
package com.mgmtp.a12.dataservices.relationship;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.model.migration.MigrationResult;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.RELATIONSHIP_MODEL_VALIDATION_ERROR_KEY;

public class RelationshipMigrationTest {

	protected static final String RELATIONSHIP_MODEL_MIGRATION_PATH = "model/migration/";
	protected static final String RELATIONSHIP_MODEL_V1_SOURCE_PATH = RELATIONSHIP_MODEL_MIGRATION_PATH + "v1/";
	protected static final String RELATIONSHIP_MODEL_V2_SOURCE_PATH = RELATIONSHIP_MODEL_MIGRATION_PATH + "v2/";
	protected static final String RELATIONSHIP_MODEL_V3_SOURCE_PATH = RELATIONSHIP_MODEL_MIGRATION_PATH + "v3/";
	protected static final String RELATIONSHIP_MODEL_V4_SOURCE_PATH = RELATIONSHIP_MODEL_MIGRATION_PATH + "v4/";

	// v1 models
	public static final String PRODUCT_BRAND_V1_MODEL_INPUT_JSON = RELATIONSHIP_MODEL_V1_SOURCE_PATH + "ContractCoInsuredPartner_V1.json";
	public static final String PRODUCT_BRAND_NO_MODEL_ID_V1_MODEL_INPUT_JSON = RELATIONSHIP_MODEL_V1_SOURCE_PATH + "ContractCoInsuredPartnerNoModelId.json";
	public static final String GROUP_ELEMENT_V1_MODEL_ORIGINAL = RELATIONSHIP_MODEL_V1_SOURCE_PATH + "GroupElement.json";

	// v2 models
	public static final String PRODUCT_BRAND_V2_MODEL_INPUT_JSON = RELATIONSHIP_MODEL_V2_SOURCE_PATH + "ContractCoInsuredPartner_V2.json";

	// v3 models
	public static final String PRODUCT_BRAND_MODEL_MIGRATED_JSON = RELATIONSHIP_MODEL_V3_SOURCE_PATH + "ContractCoInsuredPartner_V3.json";
	public static final String PRODUCT_BRAND_MODEL_MIGRATED_WITH_ROLES_JSON = RELATIONSHIP_MODEL_V3_SOURCE_PATH + "ContractCoInsuredPartner_CustomUsers.json";
	public static final String GROUP_ELEMENT_MODEL_MIGRATED = RELATIONSHIP_MODEL_V3_SOURCE_PATH + "GroupElement_CustomLocale.json";

	// v4 models
	public static final String PRODUCT_BRAND_NOT_SUPPORTED_V4_MODEL_INPUT_JSON = RELATIONSHIP_MODEL_V4_SOURCE_PATH + "ContractCoInsuredPartnerNotSupported.json";

	private final RelationshipMigration relationshipMigration = new RelationshipMigration(new ObjectMapper());

	@DataProvider
	public static Object[][] models() {
		return new Object[][] {
			{ "", PRODUCT_BRAND_V1_MODEL_INPUT_JSON, PRODUCT_BRAND_MODEL_MIGRATED_JSON },
			{ "", PRODUCT_BRAND_V2_MODEL_INPUT_JSON, PRODUCT_BRAND_MODEL_MIGRATED_JSON },
			{ "customTest,user", PRODUCT_BRAND_V1_MODEL_INPUT_JSON, PRODUCT_BRAND_MODEL_MIGRATED_WITH_ROLES_JSON }
		};
	}

	@DataProvider
	public static Object[][] modelsForSmeApi() {
		return new Object[][] {
			{ PRODUCT_BRAND_V1_MODEL_INPUT_JSON, PRODUCT_BRAND_MODEL_MIGRATED_JSON,
				PRODUCT_BRAND_V2_MODEL_INPUT_JSON, PRODUCT_BRAND_MODEL_MIGRATED_JSON ,
				PRODUCT_BRAND_NO_MODEL_ID_V1_MODEL_INPUT_JSON, PRODUCT_BRAND_NO_MODEL_ID_V1_MODEL_INPUT_JSON,
				PRODUCT_BRAND_NOT_SUPPORTED_V4_MODEL_INPUT_JSON, PRODUCT_BRAND_NOT_SUPPORTED_V4_MODEL_INPUT_JSON,
				PRODUCT_BRAND_MODEL_MIGRATED_JSON, PRODUCT_BRAND_MODEL_MIGRATED_JSON}
		};
	}

	@Test(dataProvider = "modelsForSmeApi")
	public void convertRelationshipModelViaSmeApi(String input1, String output1,
		String input2, String output2,
		String input3, String output3,
		String input4, String output4,
		String input5, String output5) throws Exception {

		List<String> models = List.of(loadResourceAsString(input1), loadResourceAsString(input2), loadResourceAsString(input3), loadResourceAsString(input4),
			loadResourceAsString(input5));
		List<MigrationResult> migratedModels = relationshipMigration.migrate(models);

		// successful migration
		JSONAssert.assertEquals(loadResourceAsString(output1), new JSONObject(migratedModels.get(0).model).toString(4), JSONCompareMode.LENIENT);
		JSONAssert.assertEquals(loadResourceAsString(output2), new JSONObject(migratedModels.get(1).model).toString(4), JSONCompareMode.LENIENT);

		// error cases
		JSONAssert.assertEquals(loadResourceAsString(output3), new JSONObject(migratedModels.get(2).model).toString(4), JSONCompareMode.LENIENT);
		Assert.assertTrue(migratedModels.get(2).getErrorMessage().isPresent());
		Assert.assertEquals(migratedModels.get(2).getStatus(), MigrationResult.Status.ERROR);
		Assert.assertEquals(migratedModels.get(2).getErrorMessage().get(), "Could not migrate model because model id could not be detected");

		JSONAssert.assertEquals(loadResourceAsString(output4), new JSONObject(migratedModels.get(3).model).toString(4), JSONCompareMode.LENIENT);
		Assert.assertTrue(migratedModels.get(3).getErrorMessage().isPresent());
		Assert.assertEquals(migratedModels.get(3).getStatus(), MigrationResult.Status.SKIPPED);
		Assert.assertEquals(migratedModels.get(3).getErrorMessage().get(), "Model not migrated, not supported");

		// up-to-date case
		JSONAssert.assertEquals(loadResourceAsString(output5), new JSONObject(migratedModels.get(4).model).toString(4), JSONCompareMode.LENIENT);
		Assert.assertTrue(migratedModels.get(4).getErrorMessage().isPresent());
		Assert.assertEquals(migratedModels.get(4).getStatus(), MigrationResult.Status.SKIPPED);
		Assert.assertEquals(migratedModels.get(4).getErrorMessage().get(), "Model not migrated, already up-to-date");
	}

	@Test(dataProvider = "models")
	public void convertRelationshipModel(String roles, String input, String output) throws Exception {
		Reader inputModel = loadResourceAsReader(input);
		Writer outputModel = new StringWriter();

		relationshipMigration.convertModel(inputModel, outputModel, roles, Collections.singleton("en,de"), false);
		JSONAssert.assertEquals(loadResourceAsString(output), new JSONObject(outputModel.toString()).toString(4), JSONCompareMode.LENIENT);
	}

	@Test
	public void convertWithoutMainLabels() throws Exception {
		Writer outputModel = new StringWriter();

		JSONObject jsonObject = new JSONObject(loadResourceAsString(PRODUCT_BRAND_V1_MODEL_INPUT_JSON));
		jsonObject.getJSONObject("relationshipModel").remove("displayLabel");

		relationshipMigration.convertModel(new StringReader(jsonObject.toString()), outputModel, "guest,admin,systemAdmin", Collections.singleton("en,de"),
			false);

		jsonObject = new JSONObject(loadResourceAsString(PRODUCT_BRAND_MODEL_MIGRATED_JSON));
		jsonObject.getJSONObject("header").remove("labels");
		jsonObject.getJSONObject("header").put("labels", new JSONArray());

		jsonObject.getJSONObject("content").remove("labels");
		jsonObject.getJSONObject("content").put("labels", new JSONArray());

		JSONAssert.assertEquals(jsonObject.toString(), outputModel.toString(), JSONCompareMode.LENIENT);
	}

	@Test
	public void convertRelationshipModelWithoutLabels() throws Exception {
		Reader inputModel = new StringReader(loadResourceAsString(GROUP_ELEMENT_V1_MODEL_ORIGINAL));
		Writer outputModel = new StringWriter();

		JSONObject jsonObject = new JSONObject(loadResourceAsString(GROUP_ELEMENT_MODEL_MIGRATED));
		jsonObject.getJSONObject("header").remove("labels");
		jsonObject.getJSONObject("content").remove("labels");

		relationshipMigration.convertModel(inputModel, outputModel, "guest,admin,systemAdmin", Collections.singleton("test"), false);
		JSONAssert.assertEquals(jsonObject.toString(), new JSONObject(outputModel.toString()).toString(4), JSONCompareMode.LENIENT);
	}

	@Test
	public void convertWithoutProvidingLocales() throws Exception {
		try {
			Reader inputModel = new StringReader(loadResourceAsString(GROUP_ELEMENT_V1_MODEL_ORIGINAL));
			Writer outputModel = new StringWriter();
			relationshipMigration.convertModel(inputModel, outputModel, "guest,admin,systemAdmin", Collections.emptySet(), false);
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getShortMessage().getKey(), RELATIONSHIP_MODEL_VALIDATION_ERROR_KEY);
			Assert.assertEquals(e.getShortMessage().getDefaultMessage(), "No locale to be set");
			Assert.assertEquals(e.getLongMessage().getKey(), RELATIONSHIP_MODEL_VALIDATION_ERROR_KEY);
			Assert.assertEquals(e.getLongMessage().getDefaultMessage(), "No locale to be set");
		}
	}

	protected InputStream loadResourceAsInputStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

	protected Reader loadResourceAsReader(String path) {
		return new InputStreamReader(loadResourceAsInputStream(path));
	}

	protected String loadResourceAsString(String path) throws IOException {
		return IOUtils.toString(loadResourceAsInputStream(path), StandardCharsets.UTF_8);
	}
}
