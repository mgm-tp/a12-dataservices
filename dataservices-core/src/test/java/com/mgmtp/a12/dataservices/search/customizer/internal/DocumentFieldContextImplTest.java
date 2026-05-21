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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;
import com.mgmtp.a12.dataservices.search.customizer.DocumentFieldContext;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import io.hypersistence.utils.hibernate.type.range.Range;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class DocumentFieldContextImplTest {

	private IDocumentModelSearchService documentModelSearchService;
	private DocumentReference documentReference;
	private DocumentPointer documentPointer;
	private FieldInstanceV2 fieldInstance;
	private IField field;
	private BiFunction<String, String, Long> fieldTypeIdProvider;
	private DocumentFieldContextImpl context;

	@BeforeMethod public void setUp() {
		documentModelSearchService = mock(IDocumentModelSearchService.class);
		documentReference = mock(DocumentReference.class);
		documentPointer = mock(DocumentPointer.class);
		fieldInstance = mock(FieldInstanceV2.class);
		field = mock(IField.class);
		fieldTypeIdProvider = (modelName, fieldPath) -> 123L;

		when(documentReference.getDocumentModelName()).thenReturn("TestModel");
		when(documentReference.toString()).thenReturn("TestModel/123");
		when(documentPointer.fullName()).thenReturn("testField");
		when(documentPointer.repetitionIndexes()).thenReturn(List.of());

		context = new DocumentFieldContextImpl(documentModelSearchService, documentReference, documentPointer, fieldInstance, field, fieldTypeIdProvider);
	}

	@Test public void testGetModelName() {
		assertEquals(context.getModelName(), "TestModel");
	}

	@Test public void testGetFieldPath() {
		assertEquals(context.getFieldPath(), "testField");
	}

	@Test public void testAddFieldWithAllParameters() {
		String fieldPath = "customField";
		String value = "testValue";
		String typedValue = "typedTestValue";
		BigDecimal numberValue = BigDecimal.valueOf(42.5);
		LocalDateTime timestampValue = LocalDateTime.of(2026, 2, 4, 12, 0);
		Range<LocalDateTime> rangeValue = Range.closed(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59));
		String type = "TEXT";
		String source = "customizer";

		DocumentFieldContext result = context.addField(fieldPath, value, typedValue, numberValue, timestampValue, rangeValue, type, source);

		assertNotNull(result);
		assertEquals(result, context);
		assertEquals(context.getAdditionalFields().size(), 1);

		DocumentFieldEntity entity = context.getAdditionalFields().getFirst();
		assertEquals(entity.getDocRef(), "TestModel/123");
		assertEquals(entity.getModelName(), "TestModel");
		assertEquals(entity.getFieldName(), fieldPath);
		assertEquals(entity.getValue(), value);
		assertEquals(entity.getTypedValue(), typedValue);
		assertEquals(entity.getNumberValue(), numberValue);
		assertEquals(entity.getTimestampValue(), timestampValue);
		assertEquals(entity.getTsRangeValue(), rangeValue);
		assertEquals(entity.getFieldType(), type);
		assertEquals(entity.getSource(), source);
		assertEquals(entity.getFieldTypeId(), Long.valueOf(123L));
	}

	@Test public void testAddFieldWithMinimalParameters() {
		String fieldPath = "customField";
		String value = "testValue";
		String source = "customizer";

		DocumentFieldContext result = context.addField(fieldPath, value, null, null, null, null, null, source);

		assertNotNull(result);
		assertEquals(context.getAdditionalFields().size(), 1);

		DocumentFieldEntity entity = context.getAdditionalFields().getFirst();
		assertEquals(entity.getFieldName(), fieldPath);
		assertEquals(entity.getValue(), value);
		assertEquals(entity.getSource(), source);
	}

	@Test public void testAddMultipleFields() {
		context.addField("field1", "value1", null, null, null, null, null, "source1");
		context.addField("field2", "value2", null, null, null, null, null, "source2");
		context.addField("field3", "value3", null, null, null, null, null, "source3");

		assertEquals(context.getAdditionalFields().size(), 3);
		assertEquals(context.getAdditionalFields().get(0).getFieldName(), "field1");
		assertEquals(context.getAdditionalFields().get(1).getFieldName(), "field2");
		assertEquals(context.getAdditionalFields().get(2).getFieldName(), "field3");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAddFieldWithNullFieldPath() {
		context.addField(null, "value", null, null, null, null, null, "source");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAddFieldWithNullValue() {
		context.addField("fieldPath", null, null, null, null, null, null, "source");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAddFieldWithEmptySource() {
		context.addField("fieldPath", "value", null, null, null, null, null, "");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAddFieldWithNullSource() {
		context.addField("fieldPath", "value", null, null, null, null, null, null);
	}

	@Test public void testSkipCoreFieldIndexing() {
		assertFalse(context.isCoreFieldIndexingSkipped());

		DocumentFieldContext result = context.skipCoreFieldIndexing();

		assertNotNull(result);
		assertSame(result, context);
		assertTrue(context.isCoreFieldIndexingSkipped());
	}

	@Test public void testInitialStateOfAdditionalFields() {
		assertNotNull(context.getAdditionalFields());
		assertTrue(context.getAdditionalFields().isEmpty());
	}

	@Test public void testFieldPathWithRepetitions() {
		when(documentPointer.fullName()).thenReturn("testField[0].nestedField[1]");
		when(documentPointer.repetitionIndexes()).thenReturn(List.of(0, 1));

		context = new DocumentFieldContextImpl(documentModelSearchService, documentReference, documentPointer, fieldInstance, field, fieldTypeIdProvider);
		context.addField("customField", "value", null, null, null, null, null, "source");

		assertEquals(context.getFieldPath(), "testField[0].nestedField[1]");
		DocumentFieldEntity entity = context.getAdditionalFields().getFirst();
		assertNotNull(entity.getRepetitions());
		assertEquals(entity.getRepetitions().length, 2);
		assertEquals(entity.getRepetitions()[0], 0);
		assertEquals(entity.getRepetitions()[1], 1);
	}
}
