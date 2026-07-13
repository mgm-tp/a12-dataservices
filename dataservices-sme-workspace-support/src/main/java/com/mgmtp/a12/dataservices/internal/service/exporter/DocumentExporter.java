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
package com.mgmtp.a12.dataservices.internal.service.exporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.FULL_DOCUMENT_PATH;
import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.JSON_EXTENSION;

/**
 * Exports documents to TAR archive.
 */
@Slf4j @RequiredArgsConstructor
@Component public class DocumentExporter extends AbstractTarExporter<Void> {

	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final DefaultDocumentRepository defaultDocumentRepository;
	private final ObjectMapper objectMapper;

	@Override protected void exportLogic(TarArchiveOutputStream tarStream, Void unused) {
		AtomicInteger docCounter = new AtomicInteger(0);
		AtomicInteger failCounter = new AtomicInteger(0);
		Map<String, AtomicInteger> modelCounter = new HashMap<>();

		modelHeaderJpaRepository.findAll().stream()
			.map(ModelHeaderEntity::getId)
			.forEach(modelName -> defaultDocumentRepository.findDocumentsByDocRefs(defaultDocumentRepository.findAllDocRefsForModel(modelName))
				.forEach(dataServicesDocument -> {
						try {
							writeFileToTar(tarStream, objectMapper.writeValueAsBytes(dataServicesDocument.getKernelDocument()),
								FULL_DOCUMENT_PATH.resolve(dataServicesDocument.getMetadata().getDocRef() + JSON_EXTENSION));
							docCounter.incrementAndGet();
							modelCounter.computeIfAbsent(modelName, k -> new AtomicInteger(0)).incrementAndGet();
						} catch (JacksonException e) {
							failCounter.incrementAndGet();
							throw new UnexpectedException("Error parsing document", e);
						}
					}
				));

		log.debug("{} documents exported. {} failed.", docCounter.get(), failCounter.get());
		log.debug("Documents per model:\n\t{}", modelCounter.entrySet().stream()
			.map(e -> e.getKey() + " " + e.getValue())
			.collect(Collectors.joining("\n\t")));
	}
}
