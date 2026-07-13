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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.initialization.internal.ModelImportConfiguration;
import com.mgmtp.a12.dataservices.initialization.internal.RuntimeModelImporter;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.model.header.Header;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class ModelImporter {

	private final RuntimeModelImporter runtimeModelImporter;
	private final ModelImportConfiguration configuration;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Imports models from in-memory workspace data by invoking
	 * {@link RuntimeModelImporter#importRuntimeModel(String, ModelImportConfiguration)} for each model.
	 * Per-model failures are logged and skipped. A {@link ModelsAfterImportEvent} is published
	 * after all models have been processed.
	 *
	 * @param models map of model ID to `ModelTuple` containing header and JSON content
	 */
	public void importModels(Map<String, ModelTuple> models) {
		if (models.isEmpty()) {
			return;
		}
		Set<Header> importedHeaders = new HashSet<>();
		for (ModelTuple modelTuple : models.values()) {
			try {
				Header header = runtimeModelImporter.importRuntimeModel(modelTuple.getContent(), configuration);
				if (header != null) {
					importedHeaders.add(header);
				}
			} catch (Exception e) {
				log.warn("Failed to import model {}: {}", modelTuple.getHeader().getId(), e.getMessage(), e);
			}
		}
		if (!importedHeaders.isEmpty()) {
			eventPublisher.publishEvent(new ModelsAfterImportEvent(importedHeaders));
		}
	}
}
