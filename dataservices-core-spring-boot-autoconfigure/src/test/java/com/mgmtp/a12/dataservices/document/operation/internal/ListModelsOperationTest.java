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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.GenericModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.operation.internal.ListModelsOperation;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.ModelReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ListModelsOperationTest extends AbstractSpringContextIT {

	public static final String RECURSIVE_OVERVIEW_MODEL = "RecursiveOverview";
	@Autowired ListModelsOperation listModelsOperation;

	private final Map<String, GenericModel> modelMap = new HashMap<>();

	@Override protected void initializeWithSecurityBypass() throws Exception {
		modelMap.clear();
		for (Resource r : resourcePatternResolver.getResources(PathConstants.DOCUMENT_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.RELATIONSHIP_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.CDM_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
		for (Resource r : resourcePatternResolver.getResources(PathConstants.OTHER_MODELS_PATH_PATTERN)) {
			createModel(r.getContentAsString(StandardCharsets.UTF_8));
		}
	}

	@DataProvider public static Object[][] modelReferencesDataProvider() {
		return new Object[][] {
			{ List.of(GenericModelConstants.CONTRACT_OVERVIEW_MODEL, DocumentModelConstants.CONTRACT_CDM_MODEL,
				DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL, DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
				DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL,
				DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			},
			{ List.of() },
		};
	}

	@Test(dataProvider = "modelReferencesDataProvider") public void testListModelsOperation(Collection<String> modelNames) {
		assertListModels(listModelsOperation.rpc(modelNames), modelNames);
	}

	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "modelNames is marked non-null but is null"
	) public void testListModelsOperationWithNull() {
		listModelsOperation.rpc(null);
	}

	private void assertListModels(ListModelsResponse listModelsOperationResult, Collection<String> models) {
		Map<String, GenericModel> expectations = new HashMap<>(modelMap);
		expectations.keySet().retainAll(models);
		Map<String, GenericModel> actualModels = listModelsOperationResult.getModels();

		assertEquals(actualModels.keySet(), expectations.keySet());
		expectations.forEach((key, expectedModel) -> assertModel(key, expectedModel, actualModels));
	}

	private void assertModel(String key, GenericModel expectedModel, Map<String, GenericModel> actualModels) {
		GenericModel actualModel = actualModels.get(key);
		assertModelHeader(actualModel.getHeader(), expectedModel.getHeader());
		assertEquals(actualModel.getContent(), expectedModel.getContent());
	}

	private void assertModelHeader(Header actualHeader, Header expectedHeader) {
		ModelHeaderMapper modelHeaderMapper = Mappers.getMapper(ModelHeaderMapper.class);
		assertNotNull(actualHeader);
		assertEquals(actualHeader.getId(), expectedHeader.getId());
		assertList(actualHeader.getAnnotations(), expectedHeader.getAnnotations(),
			c -> c.stream().map(a -> Pair.of(a.getName(), a.getValue())).toList());
		assertList(actualHeader.getLabels(), expectedHeader.getLabels(), c -> c.stream().map(l -> Pair.of(l.getLocale(), l.getText())).toList());
		assertList(actualHeader.getLocales(), expectedHeader.getLocales(), Function.identity());
		assertList(actualHeader.getModelReferences(), expectedHeader.getModelReferences(),
			c -> c.stream().map(modelHeaderMapper::toModelReference).toList());
		assertEquals(actualHeader.getModelType(), expectedHeader.getModelType());
		assertEquals(actualHeader.getModelVersion(), expectedHeader.getModelVersion());
	}

	private static <T, U> void assertList(Collection<T> actualList, Collection<T> expectedList, Function<Collection<T>, Collection<U>> transformer) {
		assertThat(transformer.apply(actualList)).hasSameElementsAs(transformer.apply(expectedList));
	}

	@Override protected GenericModel createModel(String modelContent) {
		GenericModel m = super.createModel(modelContent);
		return modelMap.put(m.getHeader().getId(), m);
	}

	@Mapper public abstract static class ModelHeaderMapper {

		public abstract ModelHeaderEntity.ModelReference toModelReference(ModelReference modelReference);
	}
}
