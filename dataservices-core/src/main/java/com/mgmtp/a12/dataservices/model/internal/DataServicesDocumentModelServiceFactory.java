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
package com.mgmtp.a12.dataservices.model.internal;

import org.springframework.cache.annotation.Cacheable;

import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.NonNull;

/**
 * DataServices extension of `DocumentModelServiceFactory` that adds Spring `@Cacheable`
 * behavior to `createDocumentModelSearchService`.
 *
 * *NOTE:* {@link DocumentModelServiceFactory} is annotated with `@OnlyForUsage`,
 * meaning it is not intended for subclassing
 * and new methods may be added to it without being considered a breaking change.
 * This extension is safe because it only adds a caching layer without changing behavior,
 * and `createDocumentModelSearchService` is called elsewhere in DataServices
 * so any upstream change would require updating all callers regardless of this subclass.
 *
 * @see DocumentModelServiceFactory
 */
public class DataServicesDocumentModelServiceFactory extends DocumentModelServiceFactory {

	@Cacheable(key = "#documentModel.header.id", cacheResolver = ModelCacheManager.DOCUMENT_MODEL_SEARCH_SERVICE_CACHE_RESOLVER)
	@Override public @NonNull IDocumentModelSearchService createDocumentModelSearchService(IDocumentModel documentModel) {
		return super.createDocumentModelSearchService(documentModel);
	}
}
