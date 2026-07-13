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
package com.mgmtp.a12.examples.custom.operator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.TaxNumberRangeOperator;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.examples.AbstractITBase;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SearchConstants.EN_LOCALE;

@Test
@TestPropertySource(properties = {
	"com.mgmtp.a12.examples.custom-operator.enabled=true",
	"com.mgmtp.a12.examples.custom.type.enabled=true"
})
public class TaxNumberRangeOperatorIT extends AbstractITBase {

	private static final String FIELD_NAME = "/Person/TaxIDCustomFieldType";

	public static final String RESOURCE_ROOT_DIR = "profile_specific/example_custom_type_profile/";
	private static final String PERSON_A = "document/PersonTaxNumber_A.json";
	private static final String PERSON_B = "document/PersonTaxNumber_B.json";
	private static final String PERSON_C = "document/PersonTaxNumber_C.json";

	private DocumentReference personAId;
	private DocumentReference personBId;
	private DocumentReference personCId;

	@Autowired
	private QueryService queryService;

	@Autowired
	private DocumentFieldsJpaRepository documentFieldsRepository;

	@BeforeClass
	public void init() throws IOException {
		modelsFunctions.createModel(MODEL_PATH + DOCUMENT_PATH + PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME + ".json");
		personAId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_A);
		personBId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_B);
		personCId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_C);
	}

	@Test(description = "Should find documents with tax numbers in range 10000000-50000000")
	public void shouldFindDocumentsInRange() {
		QueryRoot queryRoot = createQuery(TaxNumberRangeOperator.builder()
				.field(FIELD_NAME)
				.from("US10000000")
				.to("BR50000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personAId.toString(), personBId.toString()));
	}

	@Test(description = "Should find documents with tax numbers greater than 50000000")
	public void shouldFindDocumentsAboveThreshold() {
		QueryRoot queryRoot = createQuery(TaxNumberRangeOperator.builder()
				.field(FIELD_NAME)
				.from("BR50000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personBId.toString(), personCId.toString()));
	}

	@Test(description = "Should find documents with tax numbers less than 50000000")
	public void shouldFindDocumentsBelowThreshold() {
		QueryRoot queryRoot = createQuery(TaxNumberRangeOperator.builder()
				.field(FIELD_NAME)
				.to("BR50000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personAId.toString(), personBId.toString()));
	}

	@Test(description = "Should find exact match with single value range")
	public void shouldFindExactMatch() {
		QueryRoot queryRoot = createQuery(TaxNumberRangeOperator.builder()
				.field(FIELD_NAME)
				.from("BR50000000")
				.to("BR50000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personBId.toString()));
	}

	@Test(description = "Should verify custom indexing of TaxID numeric values")
	public void shouldVerifyCustomIndexing() {
		verifyIndexedValue(personAId, "US10000000", BigDecimal.valueOf(10000000));
		verifyIndexedValue(personBId, "BR50000000", BigDecimal.valueOf(50000000));
		verifyIndexedValue(personCId, "DE90000000", BigDecimal.valueOf(90000000));
	}

	@Test(description = "Should find exact match using exact_match operator")
	public void shouldFindExactMatchWithExactMatchOperator() {
		QueryRoot queryRoot = createQuery(ExactMatchOperator.builder()
				.field(FIELD_NAME)
				.value("BR50000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personBId.toString()));
	}

	@Test(description = "Should find exact match case-insensitive using exact_match operator")
	public void shouldFindExactMatchCaseInsensitive() {
		QueryRoot queryRoot = createQuery(ExactMatchOperator.builder()
				.field(FIELD_NAME)
				.value("US10000000")
				.caseSensitive(false)
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personAId.toString()));
	}

	@Test(description = "Should not find match when case-sensitive is enabled")
	public void shouldNotFindMatchWhenCaseSensitive() {
		QueryRoot queryRoot = createQuery(ExactMatchOperator.builder()
				.field(FIELD_NAME)
				.value("us10000000")
				.caseSensitive(true)
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Collections.emptyList());
	}

	@Test(description = "Should find documents using simple_search with partial match")
	public void shouldFindDocumentsWithSimpleSearch() {
		QueryRoot queryRoot = createQuery(SimpleSearchOperator.builder()
				.fields(List.of(FIELD_NAME))
				.value("50000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personBId.toString()));
	}

	@Test(description = "Should find documents using simple_search with prefix match")
	public void shouldFindDocumentsWithSimpleSearchPrefix() {
		QueryRoot queryRoot = createQuery(SimpleSearchOperator.builder()
				.fields(List.of(FIELD_NAME))
				.value("DE9")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personCId.toString()));
	}

	@Test(description = "Should find multiple documents using simple_search with common prefix")
	public void shouldFindMultipleDocumentsWithSimpleSearchCommonPrefix() {
		QueryRoot queryRoot = createQuery(SimpleSearchOperator.builder()
				.fields(List.of(FIELD_NAME))
				.value("10000000")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personAId.toString()));
	}

	private void verifyIndexedValue(DocumentReference docRef, String expectedValue, BigDecimal expectedNumberValue) {
		List<DocumentFieldEntity> fields = documentFieldsRepository.findAll().stream()
			.filter(entity -> entity.getDocRef().equals(docRef.toString()))
			.filter(entity -> entity.getFieldName().equals(FIELD_NAME))
			.filter(entity -> "tax_id_customizer".equals(entity.getSource()))
			.toList();

		Assert.assertFalse(fields.isEmpty(), "No custom indexed field found for docRef: " + docRef);
		Assert.assertEquals(fields.size(), 1, "Expected exactly one custom indexed field for docRef: " + docRef);

		DocumentFieldEntity field = fields.get(0);
		Assert.assertEquals(field.getValue(), expectedValue, "Value mismatch for docRef: " + docRef);
		Assert.assertEquals(field.getNumberValue(), expectedNumberValue, "Number value mismatch for docRef: " + docRef);
		Assert.assertEquals(field.getFieldType(), "TaxIDCustomFieldType", "Field type mismatch for docRef: " + docRef);
		Assert.assertEquals(field.getSource(), "tax_id_customizer", "Source mismatch for docRef: " + docRef);
	}

	private QueryRoot createQuery(ILogicOperator logicOperator, List<Order> sorts) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME)
			.constraint(logicOperator)
			.sort(sorts)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(10)
				.build())
			.build();
	}

	protected void filterAndExpect(QueryRoot queryRoot, List<String> expected) {
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, EN_LOCALE);
		String actual = StringUtils.join(result.getContent().stream()
			.map(documentTreeResult -> documentTreeResult.getDocRef().toString()).toList(), ",");
		String expectedString = StringUtils.join(expected, ",");
		Assert.assertEquals(actual, expectedString);
	}
}
