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
package com.mgmtp.a12.dataservices.relationship.model;

import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import lombok.SneakyThrows;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Verifies that the v4 Relationship Model domain classes can deserialize v3-format JSON
 * that contains the properties removed in v4 without throwing errors.
 */
public class RelationshipModelV4BackwardCompatibilityTest {

	private static final String FIXTURE = "relationship-model-v3-with-removed-properties.json";

	private ObjectMapper objectMapper;

	@BeforeMethod
	public void setUp() {
		objectMapper = JsonMapper.builder().build();
	}

	@Test(description ="Should deserialize v3 model with removed properties without errors")
	public void shouldDeserializeV3ModelWithRemovedPropertiesWithoutErrors() {
		// Given a v3-format JSON with associationType, storage, embeddedGroupPath, navigable,
		// candidateConstraints, lowerLimit fields present
		// When deserialized into v4 RelationshipModel
		// Then deserialization succeeds without exception
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		Assert.assertNotNull(model);
		Assert.assertNotNull(model.getContent());
	}

	@Test(description ="Should ignore associationType when deserializing v3 model")
	public void shouldIgnoreAssociationTypeWhenDeserializingV3Model() {
		// Given v3 JSON with associationType = "OWNED"
		// When deserialized into RelationshipModelContent
		// Then content.associationType field does not exist / is not populated
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		Assert.assertNotNull(model.getContent());
		// associationType field no longer exists on RelationshipModelContent — compilation confirms removal
	}

	@Test(description ="Should ignore storage when deserializing v3 model")
	public void shouldIgnoreStorageWhenDeserializingV3Model() {
		// Given v3 JSON with storage = "EXTERNAL"
		// When deserialized into RelationshipModelContent
		// Then no error occurs
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		Assert.assertNotNull(model.getContent());
	}

	@Test(description ="Should ignore embeddedGroupPath when deserializing v3 model")
	public void shouldIgnoreEmbeddedGroupPathWhenDeserializingV3Model() {
		// Given v3 JSON with embeddedGroupPath present
		// When deserialized into RelationshipModelContent
		// Then no error occurs
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		Assert.assertNotNull(model.getContent());
	}

	@Test(description ="Should ignore navigable when deserializing v3 entity characteristics")
	public void shouldIgnoreNavigableWhenDeserializingEntityCharacteristicsV3() {
		// Given v3 JSON with navigable = true in entityCharacteristics
		// When deserialized into EntityCharacteristics
		// Then no error occurs
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		Assert.assertNotNull(model.getContent().getEntityCharacteristics());
		Assert.assertFalse(model.getContent().getEntityCharacteristics().isEmpty());
	}

	@Test(description ="Should ignore candidateConstraints when deserializing v3 entity characteristics")
	public void shouldIgnoreCandidateConstraintsWhenDeserializingEntityCharacteristicsV3() {
		// Given v3 JSON with candidateConstraints present in entityCharacteristics
		// When deserialized into EntityCharacteristics
		// Then no error occurs
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		Assert.assertNotNull(model.getContent().getEntityCharacteristics());
		Assert.assertFalse(model.getContent().getEntityCharacteristics().isEmpty());
	}

	@Test(description ="Should ignore lowerLimit when deserializing v3 multiplicity")
	public void shouldIgnoreLowerLimitWhenDeserializingMultiplicityV3() {
		// Given v3 JSON with lowerLimit = 0 in multiplicity
		// When deserialized into Multiplicity
		// Then Multiplicity is populated with only retained fields (unbounded, upperLimit, duplicatesAllowed)
		RelationshipModel model = deserializeFixture(FIXTURE, RelationshipModel.class);
		EntityCharacteristics first = model.getContent().getEntityCharacteristics().getFirst();
		Multiplicity multiplicity = first.getLinkConstraints().getMultiplicity();
		Assert.assertNotNull(multiplicity);
		Assert.assertEquals(multiplicity.getUnbounded(), Boolean.TRUE);
	}

	@SneakyThrows
	private <T> T deserializeFixture(String resourceName, Class<T> type) {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			return objectMapper.readValue(is, type);
		}
	}
}
