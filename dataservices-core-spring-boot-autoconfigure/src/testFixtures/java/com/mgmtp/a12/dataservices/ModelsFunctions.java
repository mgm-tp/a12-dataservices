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
package com.mgmtp.a12.dataservices;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.SneakyThrows;

@Component public class ModelsFunctions {

	@Autowired private ResourceFunctions resourceFunctions;
	@Autowired private HeaderParser headerParser;
	@Autowired private ModelJpaRepository modelRepository;
	@Autowired private ModelHeaderJpaRepository modelHeaderRepository;
	@Autowired private ModelService modelService;

	@SneakyThrows public void createModel(final String modelPath) {
		String modelContent = resourceFunctions.loadResource(modelPath);
		createModelFromJson(modelContent);
	}

	@SneakyThrows public void createModelFromJson(final String modelContent) {
		modelService.create(modelContent);
	}

	public void createModels(String... modelPath) {
		Arrays.stream(modelPath)
			.distinct()
			.filter(Objects::nonNull)
			.forEach(this::createModel);
	}

	@SneakyThrows public void saveModel(final String modelContent, final Class<? extends ModelEntity> modelClass) {
		final ModelEntity modelEntity = modelClass.getDeclaredConstructor().newInstance();
		Header header = headerParser.parseJson(modelContent);
		modelEntity.setCreatedBy("test");
		modelEntity.setId(header.getId());
		modelEntity.setContent(modelContent);
		modelHeaderRepository.save(new ModelHeaderEntity(header));
		modelRepository.save(modelEntity);
	}

	@SneakyThrows public void saveCdm(String relativePath, String modelName) {
		saveCdmInternal(resourceFunctions.loadResource(relativePath).formatted(modelName));
	}

	@SneakyThrows public void saveCdm(String relativePath) {
		saveCdmInternal(resourceFunctions.loadResource(relativePath));
	}

	public void saveCdms(String... relativePath) {
		Arrays.stream(relativePath)
			.distinct()
			.filter(Objects::nonNull)
			.forEach(this::saveCdm);
	}

	@SneakyThrows private void saveCdmInternal(String modelContent) {
		Header header = headerParser.parseJson(modelContent);
		modelService.create(modelContent);
		modelHeaderRepository.save(new ModelHeaderEntity(header));
	}

	@SneakyThrows public void updateModel(final String modelPath) {
		String modelContent = resourceFunctions.loadResource(modelPath);
		updateModelContent(modelContent);
	}

	@SneakyThrows public void updateModelContent(final String modelContent) {
		modelService.update(modelContent);
	}

}
