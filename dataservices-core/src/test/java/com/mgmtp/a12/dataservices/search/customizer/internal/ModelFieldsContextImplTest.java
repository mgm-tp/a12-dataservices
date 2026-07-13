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
package com.mgmtp.a12.dataservices.search.customizer.internal;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.search.customizer.ModelFieldsContext;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

public class ModelFieldsContextImplTest {

	private IField field;
	private IFieldType effectiveFieldType;
	private ModelFieldEntity.ModelFieldEntityBuilder modelFieldEntityBuilder;
	private Map<String, Map<String, String>> localizedFieldEntities;
	private ModelFieldsContextImpl context;
	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		field = mock(IField.class);
		effectiveFieldType = mock(IFieldType.class);
		modelFieldEntityBuilder = ModelFieldEntity.builder();
		localizedFieldEntities = new HashMap<>();
		objectMapper = new ObjectMapper();

		context = new ModelFieldsContextImpl("TestModel", "testField", field, effectiveFieldType, modelFieldEntityBuilder, localizedFieldEntities);
	}

	@Test public void testGetModelName() {
		assertEquals(context.getModelName(), "TestModel");
	}

	@Test public void testGetPath() {
		assertEquals(context.getPath(), "testField");
	}

	@Test public void testGetField() {
		assertNotNull(context.getField());
		assertEquals(context.getField(), field);
	}

	@Test public void testGetEffectiveFieldType() {
		assertNotNull(context.getEffectiveFieldType());
		assertEquals(context.getEffectiveFieldType(), effectiveFieldType);
	}

	@Test public void testGetOriginalLocalizedValueWhenExists() {
		localizedFieldEntities.put("cs", new HashMap<>(Map.of("label", "Testovací hodnota")));
		assertEquals(context.getOriginalLocalizedValue("cs", "label"), "Testovací hodnota");
	}

	@Test public void testGetOriginalLocalizedValueWhenLocaleNotExists() {
		assertNull(context.getOriginalLocalizedValue("en", "label"));
	}

	@Test public void testGetOriginalLocalizedValueWhenKeyNotExists() {
		localizedFieldEntities.put("cs", new HashMap<>());
		assertNull(context.getOriginalLocalizedValue("cs", "label"));
	}

	@Test public void testPutLocalizedValue() {

		localizedFieldEntities.put("en", new HashMap<>(Map.of("label", "Original Value")));

		ModelFieldsContext result = context.putLocalizedValue("en", "label", "New Value");

		assertNotNull(result);
		assertSame(result, context);
		assertEquals(context.getOriginalLocalizedValue("en", "label"), "New Value");
	}

	@Test public void testPutLocalizedValueForNonExistingLocale() {

		ModelFieldsContext result = context.putLocalizedValue("fr", "label", "Valeur");

		assertNotNull(result);
		assertEquals(result, context);
	}

	@Test public void testSetFieldValueWithBothParameters() throws Exception {

		String fieldType = "TEXT";
		JsonNode data = objectMapper.readTree("{\"key\":\"value\"}");

		ModelFieldsContext result = context.setFieldValue(fieldType, data);

		assertNotNull(result);
		assertSame(result, context);

		ModelFieldEntity entity = modelFieldEntityBuilder.build();
		assertEquals(entity.getFieldType(), fieldType);
		assertEquals(entity.getData(), data);
	}

	@Test public void testSetFieldValueWithFieldTypeOnly() {

		String fieldType = "NUMBER";

		ModelFieldsContext result = context.setFieldValue(fieldType, null);

		assertNotNull(result);
		assertSame(result, context);

		ModelFieldEntity entity = modelFieldEntityBuilder.build();
		assertEquals(entity.getFieldType(), fieldType);
		assertNull(entity.getData());
	}

	@Test public void testSetFieldValueWithDataOnly() throws Exception {

		JsonNode data = objectMapper.readTree("{\"test\":123}");

		ModelFieldsContext result = context.setFieldValue(null, data);

		assertNotNull(result);
		assertSame(result, context);

		ModelFieldEntity entity = modelFieldEntityBuilder.build();
		assertNull(entity.getFieldType());
		assertEquals(entity.getData(), data);
	}

	@Test public void testSetFieldValueWithNullParameters() {

		ModelFieldsContext result = context.setFieldValue(null, null);

		assertNotNull(result);
		assertSame(result, context);

		ModelFieldEntity entity = modelFieldEntityBuilder.build();
		assertNull(entity.getFieldType());
		assertNull(entity.getData());
	}

	@Test public void testMultipleOperations() throws Exception {

		localizedFieldEntities.put("en", new HashMap<>(Map.of("label", "Original")));

		context.putLocalizedValue("en", "label", "Updated");
		context.setFieldValue("TEXT", objectMapper.readTree("{\"value\":\"test\"}"));

		assertEquals(context.getOriginalLocalizedValue("en", "label"), "Updated");
		ModelFieldEntity modelFieldEntity = modelFieldEntityBuilder.build();
		assertEquals(modelFieldEntity.getFieldType(), "TEXT");
		assertNotNull(modelFieldEntity.getData());
	}
}
