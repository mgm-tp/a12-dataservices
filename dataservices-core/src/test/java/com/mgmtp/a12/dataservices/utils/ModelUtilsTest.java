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
package com.mgmtp.a12.dataservices.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.model.internal.ModelSubtypes;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUB_TYPES_ANNOTATION_KEY;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUPER_TYPES_ANNOTATION_KEY;
import static org.testng.Assert.assertEquals;

public class ModelUtilsTest {

	private static final String TOP_MODEL_NAME = "topModel";
	private static final String MODEL_1_NAME = "model1";
	private static final String MODEL_2_NAME = "model2";
	private static final String MODEL_3_NAME = "model3";
	private static final String MODEL_4_NAME = "model4";
	private static final String MODEL_5_NAME = "model5";
	private static final String MODEL_6_NAME = "model6";

	public static final List<Header> MODEL_HEADERS = List.of(
		makeHeader(TOP_MODEL_NAME, Set.of(MODEL_1_NAME), Set.of()),
		makeHeader(MODEL_1_NAME, Set.of(MODEL_2_NAME), Set.of()),
		makeHeader(MODEL_2_NAME, Set.of(), Set.of(TOP_MODEL_NAME)),
		makeHeader(MODEL_3_NAME, Set.of(MODEL_4_NAME, MODEL_5_NAME), Set.of(MODEL_1_NAME)),
		makeHeader(MODEL_4_NAME, Set.of(MODEL_6_NAME), Set.of(MODEL_2_NAME)),
		makeHeader(MODEL_5_NAME, Set.of(), Set.of()),
		makeHeader(MODEL_6_NAME, Set.of(), Set.of(MODEL_4_NAME))
	);

	private static final ModelSubtypes MODEL_6 = new ModelSubtypes(MODEL_6_NAME, Set.of());
	private static final ModelSubtypes MODEL_5 = new ModelSubtypes(MODEL_5_NAME, Set.of());
	private static final ModelSubtypes MODEL_4 = new ModelSubtypes(MODEL_4_NAME, Set.of(MODEL_6));
	private static final ModelSubtypes MODEL_2 = new ModelSubtypes(MODEL_2_NAME, Set.of(MODEL_4));
	private static final ModelSubtypes MODEL_3 = new ModelSubtypes(MODEL_3_NAME, Set.of(MODEL_4, MODEL_5));
	private static final ModelSubtypes MODEL_1 = new ModelSubtypes(MODEL_1_NAME, Set.of(MODEL_2, MODEL_3));
	private static final ModelSubtypes TOP_MODEL = new ModelSubtypes(TOP_MODEL_NAME, Set.of(MODEL_1, MODEL_2));

	/**
	 * ----
	 * topModel
	 *  +- model1
	 *  |  +- model2
	 *  |      +- model4
	 *  |      +- model6
	 *  |  +- model3
	 *  |      +- model4
	 *  |      |  +- model6
	 *  |      +- model5
	 *  +- model2
	 *      +- model4
	 *          +- model6
	 * ----
	 */
	@Test public void testModelInheritanceIterator() {
		List<String> result = StreamSupport.stream(TOP_MODEL.spliterator(), false)
			.map(ModelSubtypes::getModelName)
			.toList();
		assertEquals(result, List.of(MODEL_1_NAME, MODEL_3_NAME, MODEL_5_NAME, MODEL_4_NAME, MODEL_6_NAME,
			MODEL_2_NAME, MODEL_4_NAME, MODEL_6_NAME, MODEL_2_NAME, MODEL_4_NAME, MODEL_6_NAME));
	}

	@Test public void testUpdateModelHeterogeneity() {
		Map<String, ModelSubtypes> expected = Map.of(
			TOP_MODEL_NAME, TOP_MODEL,
			MODEL_1_NAME, MODEL_1,
			MODEL_2_NAME, MODEL_2,
			MODEL_3_NAME, MODEL_3,
			MODEL_4_NAME, MODEL_4,
			MODEL_5_NAME, MODEL_5,
			MODEL_6_NAME, MODEL_6
		);

		assertEquals(ModelUtils.computeModelHeterogeneity(MODEL_HEADERS), expected);

	}

	@Test public void testValidateHeterogeneityOk() {
		ModelUtils.validateHeterogeneity(MODEL_HEADERS);
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Heterogeneity inheritance loop in model's model2 chain: Nested model model1 has subtype model2 which is already in the chain of its supertypes.")
	public void testValidateHeterogeneityDirectLoop() {
		ModelUtils.validateHeterogeneity(List.of(
			makeHeader(MODEL_1_NAME, List.of(), List.of()),
			makeHeader(MODEL_2_NAME, List.of(MODEL_1_NAME), List.of(MODEL_1_NAME))));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Heterogeneity inheritance loop in model's model3 chain: Nested model model2 has subtype model3 which is already in the chain of its supertypes.")
	public void testValidateHeterogeneityNestedLoop() {
		ModelUtils.validateHeterogeneity(List.of(
			makeHeader(MODEL_1_NAME, List.of(MODEL_2_NAME), List.of(MODEL_3_NAME)),
			makeHeader(MODEL_2_NAME, List.of(MODEL_3_NAME), List.of()),
			makeHeader(MODEL_3_NAME, List.of(), List.of())));
	}

	private static Header makeHeader(String name, Collection<String> subtypes, Collection<String> supertypes) {
		ModelHeaderEntity modelHeaderEntity = new ModelHeaderEntity();
		modelHeaderEntity.setId(name);
		modelHeaderEntity.setModelType(DOCUMENT_MODEL_TYPE);
		modelHeaderEntity.setAnnotations(Map.of(
			SUB_TYPES_ANNOTATION_KEY, String.join(",", subtypes),
			SUPER_TYPES_ANNOTATION_KEY, String.join(",", supertypes)));
		return modelHeaderEntity;
	}
}
