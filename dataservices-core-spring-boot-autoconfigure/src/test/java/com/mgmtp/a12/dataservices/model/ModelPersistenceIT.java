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

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;

@Listeners(MockitoTestNGListener.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class })
@Transactional
public class ModelPersistenceIT extends AbstractSpringContextIT {

	private static final String MODEL_ID_1 = "MODEL1";
	private static final String MODEL_TYPE_1 = "DocumentModel";
	private static final String MODEL_TYPE_2 = "UiModel";
	private static final String MODEL_TYPE_VERSION_1 = "1.0.0";
	private static final String MODEL_TYPE_VERSION_2 = "2.0.0";
	private static final String MODEL_DESCRIPTION_1 = "Model description";
	private static final String MODEL_DESCRIPTION_2 = "Second model description";
	private static final Set<Locale> LOCALES_1 = Set.of(Locale.ENGLISH, Locale.GERMAN, Locale.forLanguageTag("cz"), Locale.forLanguageTag("es"));
	private static final Set<Locale> LOCALES_2 =
		Set.of(Locale.forLanguageTag("ru"), Locale.forLanguageTag("ua"), Locale.forLanguageTag("ro"), Locale.forLanguageTag("gr"));
	private static final Map<Locale, String> LABELS_1 = Map.of(Locale.ENGLISH, "Hello", Locale.GERMAN, "Gutten Tag", Locale.forLanguageTag("cz"), "Dobrý den");
	private static final Map<Locale, String> LABELS_2 =
		Map.of(Locale.forLanguageTag("ru"), "Добрый день", Locale.forLanguageTag("ro"), "Bună ziua", Locale.forLanguageTag("ua"), "Привіт");
	private static final Map<String, String> ANNOTATIONS_1 = Map.of("roles", "admin,guest", "subtypes", "Document", "parent", "Root");
	private static final Map<String, String> ANNOTATIONS_2 = Map.of("mappings", "plane,square", "subtypes", "Document2", "parent", "Root2");
	private static final Set<ModelHeaderEntity.ModelReference> MODEL_REFERENCES_1 = prepareReferences(2, 5);
	private static final Set<ModelHeaderEntity.ModelReference> MODEL_REFERENCES_2 = prepareReferences(8, 12);
	private static final String USER_1 = "User 1";
	private static final String USER_2 = "User 2";
	private static final String CONTENT_1 = "{\"name\" : \"value\", \"x\" : 1}";
	private static final String CONTENT_2 = "{\"name2\" : \"value2\", \"x2\" : 2}";

	private static Set<ModelHeaderEntity.ModelReference> prepareReferences(int from, int to) {
		return IntStream.rangeClosed(from, to).boxed()
			.map(i -> Set.of(
				ImmutablePair.of("UiModel", "Form" + i),
				ImmutablePair.of("UiModel", "Overview" + i),
				ImmutablePair.of("ApplicationModel", "App" + i)))
			.flatMap(Collection::stream)
			.map(e -> {
				ModelHeaderEntity.ModelReference modelReference = new ModelHeaderEntity.ModelReference();
				modelReference.setModelType(e.getKey());
				modelReference.setReference(e.getValue());
				modelReference.setAlias(e.getValue() + " alias");
				modelReference.setPurpose("purpose of " + e.getValue());
				return modelReference;
			})
			.collect(Collectors.toSet());
	}

	@Test
	public void testModelHeader() {
		modelHeaderJpaRepository.save(createModelHeader(MODEL_ID_1, MODEL_TYPE_1, MODEL_TYPE_VERSION_1, MODEL_DESCRIPTION_1, LOCALES_1, LABELS_1, ANNOTATIONS_1,
			MODEL_REFERENCES_1));
		ModelHeaderEntity persistedModelHeader = findHeaderByModelName(MODEL_ID_1);
		assertModelHeader(persistedModelHeader, MODEL_ID_1, MODEL_TYPE_1, MODEL_TYPE_VERSION_1, MODEL_DESCRIPTION_1, LOCALES_1, LABELS_1, ANNOTATIONS_1,
			MODEL_REFERENCES_1);

		persistedModelHeader.setModelReferences(null);
		modelHeaderJpaRepository.save(persistedModelHeader);
		persistedModelHeader = findHeaderByModelName(MODEL_ID_1);
		assertModelHeader(persistedModelHeader, MODEL_ID_1, MODEL_TYPE_1, MODEL_TYPE_VERSION_1, MODEL_DESCRIPTION_1, LOCALES_1, LABELS_1, ANNOTATIONS_1, null);
	}

	@Test
	public void testModelHeaderConstraints() {
		modelHeaderJpaRepository.save(createModelHeader(MODEL_ID_1, MODEL_TYPE_1, MODEL_TYPE_VERSION_1, MODEL_DESCRIPTION_1, LOCALES_1, LABELS_1, ANNOTATIONS_1,
			MODEL_REFERENCES_1));
		modelHeaderJpaRepository.save(createModelHeader(MODEL_ID_1, MODEL_TYPE_2, MODEL_TYPE_VERSION_2, MODEL_DESCRIPTION_2, LOCALES_2, LABELS_2, ANNOTATIONS_2,
			MODEL_REFERENCES_2));
		ModelHeaderEntity persistedModelHeader = findHeaderByModelName(MODEL_ID_1);
		assertModelHeader(persistedModelHeader, MODEL_ID_1, MODEL_TYPE_2, MODEL_TYPE_VERSION_2, MODEL_DESCRIPTION_2, LOCALES_2, LABELS_2, ANNOTATIONS_2,
			MODEL_REFERENCES_2);

		persistedModelHeader.setLocales(new HashSet<>(LOCALES_1));
		persistedModelHeader.setLabels(new HashMap<>(LABELS_1));
		persistedModelHeader.setAnnotations(new HashMap<>(ANNOTATIONS_1));
		persistedModelHeader.setModelReferences(new HashSet<>(MODEL_REFERENCES_1));
		persistedModelHeader.setDescription(MODEL_DESCRIPTION_1);
		modelHeaderJpaRepository.save(persistedModelHeader);
		persistedModelHeader = findHeaderByModelName(MODEL_ID_1);
		assertModelHeader(persistedModelHeader, MODEL_ID_1, MODEL_TYPE_2, MODEL_TYPE_VERSION_2, MODEL_DESCRIPTION_1, LOCALES_1, LABELS_1, ANNOTATIONS_1,
			MODEL_REFERENCES_1);
	}

	@Test
	public void testModel() {
		Instant beforeSave = Instant.now();
		modelRepository.save(createModel(MODEL_ID_1, USER_1, CONTENT_1));
		Instant afterSave = Instant.now();
		ModelEntity persistedModel = findModelByName(MODEL_ID_1);
		Pair<Instant, Instant> createInterval = Pair.of(beforeSave, afterSave);
		Pair<Instant, Instant> updateInterval = Pair.of(beforeSave, afterSave);
		assertModel(persistedModel, MODEL_ID_1, USER_1, createInterval, USER_1, updateInterval, CONTENT_1);

		persistedModel.setUpdatedBy(USER_2);
		persistedModel.setContent(CONTENT_2);
		beforeSave = Instant.now();
		persistedModel.setUpdatedAt(Instant.now());
		modelRepository.save(persistedModel);
		afterSave = Instant.now();
		persistedModel = findModelByName(MODEL_ID_1);
		updateInterval = Pair.of(beforeSave, afterSave);
		assertModel(persistedModel, MODEL_ID_1, USER_1, createInterval, USER_2, updateInterval, CONTENT_2);
	}

	private void assertModel(ModelEntity persistedModel, String modelId, String createdBy, Pair<Instant, Instant> createInterval, String updatedBy,
		Pair<Instant, Instant> updateInterval, String content) {
		Assert.assertEquals(persistedModel.getId(), modelId);
		Assert.assertEquals(persistedModel.getCreatedBy(), createdBy);
		Assert.assertEquals(persistedModel.getUpdatedBy(), updatedBy);
		assertBetween(persistedModel.getCreatedAt(), createInterval);
		assertBetween(persistedModel.getUpdatedAt(), updateInterval);
		Assert.assertEquals(persistedModel.getContent(), content);
	}

	private void assertBetween(Instant persistedModel, Pair<Instant, Instant> createInterval) {
		Assert.assertTrue(persistedModel.isAfter(createInterval.getLeft()) && persistedModel.isBefore(createInterval.getRight()) || persistedModel.equals(
			createInterval.getLeft()) || persistedModel.equals(createInterval.getRight()));
	}

	private ModelEntity createModel(String modelId, String user, String content) {
		ModelEntity modelEntity = new ModelEntity();
		modelEntity.setId(modelId);
		modelEntity.setUpdatedBy(user);
		modelEntity.setCreatedBy(user);
		modelEntity.setContent(content);
		return modelEntity;
	}

	private ModelHeaderEntity createModelHeader(String modelId, String modelType, String modelTypeVersion, String description, Set<Locale> locales,
		Map<Locale, String> labels, Map<String, String> annotations, Set<ModelHeaderEntity.ModelReference> modelReferences) {
		ModelHeaderEntity modelHeader = new ModelHeaderEntity();
		modelHeader.setId(modelId);
		modelHeader.setModelType(modelType);
		modelHeader.setModelVersion(modelTypeVersion);
		modelHeader.setDescription(description);
		modelHeader.setLocales(locales);
		modelHeader.setLabels(labels);
		modelHeader.setAnnotations(annotations);
		modelHeader.setModelReferences(modelReferences);
		return modelHeader;
	}

	private void assertModelHeader(ModelHeaderEntity persistedModelHeader, String modelId, String modelType, String modelTypeVersion,
		String description, Set<Locale> locales, Map<Locale, String> labels, Map<String, String> annotations,
		Set<ModelHeaderEntity.ModelReference> references) {
		Assert.assertEquals(persistedModelHeader.getId(), modelId);
		Assert.assertEquals(persistedModelHeader.getModelType(), modelType);
		Assert.assertEquals(persistedModelHeader.getModelVersion(), modelTypeVersion);
		Assert.assertEquals(persistedModelHeader.getDescription(), description);
		Assert.assertEquals(persistedModelHeader.getLocalesAsSet(), locales);
		Assert.assertEquals(persistedModelHeader.getLabelsAsMap(), labels);
		Assert.assertEquals(persistedModelHeader.getAnnotationsAsMap(), annotations);
		if (references == null) {
			Assert.assertNull(persistedModelHeader.getModelReferencesAsSet());
		} else {
			Assert.assertTrue(SetUtils.isEqualSet(persistedModelHeader.getModelReferences(), references));
		}
	}

}
