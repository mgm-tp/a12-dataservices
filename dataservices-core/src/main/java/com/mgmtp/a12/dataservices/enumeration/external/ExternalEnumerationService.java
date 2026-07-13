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
package com.mgmtp.a12.dataservices.enumeration.external;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that loads and converts external enumerations from persisted documents.
 * Applies authorization checks and respects configured limits on the number of loaded documents.
 */
@RequiredArgsConstructor
@Slf4j
// TODO A12S-4114: Hide this implementation behind a public interface and move it to internal package
@Service public class ExternalEnumerationService {

	private final ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	private final DocumentService documentService;
	private final Optional<List<ExternalEnumerationLoader>> externalEnumerationLoaders;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final DefaultDocumentService defaultDocumentService;

	/**
	 * Returns external enumerations for particular model.
	 * Please note: The number of documents loaded to fill the enumeration is restricted by configuration.
	 *
	 * @param modelName model to retrieve enums for
	 * @return List of external enums
	 */
	public List<ExternalEnumeration> loadExternalEnumerationForModel(String modelName) {
		modelPermissionEvaluator.checkModelReadPermission(modelName);

		List<ExternalEnumerationLoader> loaders = externalEnumerationLoaders
			.filter(CollectionUtils::isNotEmpty)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.EXTERNAL_ENUM_NOT_FOUND_ERROR_KEY,
			"No external enumeration implementation found for model %s".formatted(modelName)));

		List<DataServicesDocument> documents = loadDocuments(modelName);
		return loaders.stream()
			.filter(e -> e.isModelSupported(modelName))
			.flatMap(loader -> convert(documents, loader))
			.toList();
	}

	private List<DataServicesDocument> loadDocuments(String modelName) {
		int maxPageSize = dataServicesCoreProperties.getEnumeration().getPageLimit();
		log.debug("Number of documents loaded is restricted to [{}] by configuration", maxPageSize);
		return defaultDocumentService.loadForModel(modelName, PageRequest.of(0, maxPageSize, Sort.unsorted())).stream()
			.map(docRef -> documentService.load(docRef)
				.orElseThrow(() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY, "Document [%s] not found".formatted(docRef))))
			.toList();
	}

	private Stream<ExternalEnumeration> convert(List<DataServicesDocument> documentList, ExternalEnumerationLoader enumerationLoader) {
		return documentList.stream()
			.map(DataServicesDocument::getMetadata)
			.map(DataServicesDocumentMetadata::getDocRef)
			.map(enumerationLoader::convertToEnumeration);
	}
}
