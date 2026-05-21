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
package com.mgmtp.a12.dataservices.initialization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration;
import com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImporter;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.model.header.Header;

import lombok.extern.slf4j.Slf4j;

/**
 * Imports and cleans up business models during application initialization.
 * This component deletes persisted models according to configuration and then imports models from configured locations.
 */
@Slf4j
@Component public class BusinessModelInitializer {

	@Autowired private BulkImporterConfiguration configuration;
	@Autowired private ModelBulkImporter modelBulkImporter;
	@Autowired private ModelHeaderJpaRepository modelHeaderRepository;
	@Autowired private ModelService modelService;

	/**
	 * Imports business models from configured paths and logs progress and timing.
	 * If no import paths are configured, only persisted models are cleaned up.
	 */
	@Transactional
	public void importBusinessModels() {
		//Models delete should be done even when no path is provided
		cleanupPersistedModels();
		if (configuration.getPaths() == null || configuration.getPaths().length < 1) {
			return;
		}

		StopWatch timer = StopWatch.createStarted();

		Arrays.stream(configuration.getPaths()).forEach(p -> {
			log.info("Importing business models: {}", p);
			try {
				List<String> result = modelBulkImporter.doImport(p, configuration);
				log.info("Imported business models: {}", String.join(", ", result));
			} catch (URISyntaxException | FileNotFoundException e) {
				log.warn("Skipping invalid business model location: {}", p);
			} catch (IOException e) {
				log.error("Skipping {} due to unexpected error", p, e);
			}
		});

		timer.stop();
		log.info("Business models imported in {} ms.", timer.getTime());
	}

	private void cleanupPersistedModels() {
		if (CollectionUtils.isEmpty(configuration.getModelTypes())) {
			return;
		}

		List<? extends Header> entities = GenericUtils.isSingleAsterisk(configuration.getModelTypes())
			? modelHeaderRepository.findAll()
			: modelHeaderRepository.findAllByModelTypeIn(configuration.getModelTypes());
		entities.stream()
			.map(Header::getId)
			.forEach(e -> modelService.delete(e));
	}
}

