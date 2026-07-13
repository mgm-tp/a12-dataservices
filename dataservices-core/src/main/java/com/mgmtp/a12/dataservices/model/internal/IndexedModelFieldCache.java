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

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelLoader;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class IndexedModelFieldCache {

	private final DocumentModelLoader documentModelLoader;
	private final DocumentModelFieldsIndexer documentModelFieldsIndexer;
	private final IDocumentModelService modelService;

	/**
	 * Retrieves all indexed field paths for a given document model.
	 *
	 * This method is cached to avoid expensive model loading and traversal operations.
	 * The result is computed only once per model and then stored in the cache.
	 *
	 * @param model the document model name to get indexed fields for
	 * @return an immutable set of indexed field paths for the given model
	 * @throws NullPointerException if any field path is null
	 */
	@Cacheable(value = ModelCacheManager.MODEL_IS_INDEXED_FIELD_CACHE, sync = true)
	public Set<String> getIndexedFields(String model) {
		final Set<String> results = new HashSet<>();

		new DocumentModelWalker().acceptDocumentModel(
			documentModelLoader.loadModel(model), new DocumentModelVisitor() {
				@Override
				public DocumentModelWalker.VisitProcess visitField(@NotNull IField field) {
					if (documentModelFieldsIndexer.isIndexable(field)) {
						results.add(modelService.getPath(field));
					}
					return super.visitField(field);
				}
			});

		// Set.copyOf() returns an immutable set to:
		// - Prevent callers from modifying the cached data
		// - Ensure thread-safety for concurrent access
		// - Fail fast if nulls are present (prevents serialization errors)
		return Set.copyOf(results);
	}
}
