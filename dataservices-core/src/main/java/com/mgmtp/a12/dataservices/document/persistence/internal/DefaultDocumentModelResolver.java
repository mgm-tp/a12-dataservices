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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.internal.IInternalModelProvider;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class DefaultDocumentModelResolver implements IDocumentModelResolver {

	private final DocumentModelReadRepository documentModelReadRepository;
	private final Set<IInternalModelProvider> internalModelProviders;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	@Override public IDocumentModel getDocumentModelById(@NonNull String documentModelName) {
		StopWatch stopWatch = StopWatch.createStarted();
		IDocumentModel documentModel = getInternalModel(documentModelName)
			.orElseGet(() -> getUserModel(documentModelName));
		log.trace("Document model [{}] has been loaded in [{}] ms", documentModelName, stopWatch.getTime());
		return documentModel;
	}

	private IDocumentModel getUserModel(@NonNull String documentModelName) {
		return Optional.of(documentModelName)
			.map(documentModelReadRepository::readModel)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY,
				String.format("DocumentModel %s is not available.", documentModelName)));
	}

	private Optional<IDocumentModel> getInternalModel(String documentModelName) {
		return Stream.ofNullable(internalModelProviders)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.filter(p -> p.supports(documentModelName))
			.map(IInternalModelProvider::getModel)
			.filter(Objects::nonNull)
			.findAny();
	}

	@Cacheable(cacheResolver = ModelCacheManager.INSECURE_MODEL_CACHE_RESOLVER)
	@Override public Optional<IDocumentModelSearchService> getDocumentModelSearchService(String documentModelId) {
		return Optional.of(documentModelServiceFactory.createDocumentModelSearchService(getDocumentModelById(documentModelId)));
	}
}
