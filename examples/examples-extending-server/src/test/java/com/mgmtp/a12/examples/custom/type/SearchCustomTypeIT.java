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
package com.mgmtp.a12.examples.custom.type;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.examples.AbstractITBase;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SearchConstants.EN_LOCALE;

@Test
@ActiveProfiles({ "dataservices-example-custom-type_env" })
public class SearchCustomTypeIT extends AbstractITBase {

	private static final String FIELD_NAME = "/Person/TaxIDCustomFieldType";

	public static final String RESOURCE_ROOT_DIR = "profile_specific/example_custom_type_profile/";
	private static final String PERSON_A = "document/PersonCustomType_A.json";
	private static final String PERSON_B = "document/PersonCustomType_B.json";
	private static final String PERSON_C = "document/PersonCustomType_C.json";
	private static final String PERSON_R = "document/PersonCustomType_R.json";

	private DocumentReference personAId;
	private DocumentReference personBId;
	private DocumentReference personCId;
	private DocumentReference personRId;

	@Autowired
	private QueryService queryService;

	@BeforeClass
	public void init() throws IOException {
		// Be aware: AbstractITBase cleans up the database in its @BeforeClass method, so we have to create the model and documents here.
		modelsFunctions.createModel(MODEL_PATH + DOCUMENT_PATH + PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME + ".json");
		personAId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_A);
		personBId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_B);
		personCId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_C);
		personRId = documentFunctions.createDocumentFromFileAndGetDocRef(PERSON_WITH_CUSTOM_TYPE_DOCUMENT_MODEL_NAME, RESOURCE_ROOT_DIR + PERSON_R);
	}

	@DataProvider public Object[][] sortDataProvider() {
		return new Object[][] {
			new Object[] { "ASC", personAId.toString(), personBId.toString(), personCId.toString() },
			new Object[] { "DESC", personCId.toString(), personBId.toString(), personAId.toString() }
		};
	}

	@Test
	public void testFilterExactMatch() {
		QueryRoot queryRoot = createQuery(ExactMatchOperator.builder()
				.field(FIELD_NAME)
				.value("RR12345678")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personRId.toString()));
	}

	@Test
	public void testFilterApproximateMatch() {
		QueryRoot queryRoot = createQuery(SimpleSearchOperator.builder()
				.fields(List.of(FIELD_NAME))
				.value("rr12345")
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personRId.toString()));
	}

	@Test
	public void testFilterCaseInsensitive() {
		QueryRoot queryRoot = createQuery(ExactMatchOperator.builder()
				.field(FIELD_NAME)
				.value("rr12345678")
				.caseSensitive(false)
				.build(),
			Collections.emptyList());
		filterAndExpect(queryRoot, Lists.newArrayList(personRId.toString()));
	}

	@Test(dataProvider = "sortDataProvider")
	public void testSort(String order, String... expectedOrder) {
		QueryRoot queryRoot = createQuery(
			NotOperator.builder()
				.operand(
					ExactMatchOperator.builder()
						.field(FIELD_NAME)
						.value("rr12345678")
						.caseSensitive(false)
						.build())
				.build(),
			List.of(new DirectFieldOrder(FIELD_NAME, DirectFieldOrder.Direction.valueOf(order))));
		filterAndExpect(queryRoot, Lists.newArrayList(expectedOrder));
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
