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
package com.mgmtp.a12.dataservices.model.document.persistence;

import java.util.function.Predicate;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.dataservices.model.persistence.AbstractModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * Base repository for reading document models.
 * Applies caching and filtering by model type and delegates model construction to subclasses.
 *
 * @param <T> the concrete document model type
 */
@Slf4j
@Component public abstract class AbstractDocumentModelReadRepository<T extends IDocumentModel> extends AbstractModelReadRepository<T> {

	@Cacheable(value = ModelCacheManager.DOCUMENT_MODEL_READ_CACHE)
	@Override public T readModel(@NonNull String modelId) {
		return super.readModel(modelId);
	}

	/**
	 * Returns the error key used when a model cannot be found.
	 *
	 * @return the error key string
	 */
	@Override protected String getModelNotFoundErrorKey() {
		return ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY;
	}

	/**
	 * Provides a human-readable model type name used in messages.
	 *
	 * @return the model type label
	 */
	@Override protected String getModelTypeForMessage() {
		return "Document model";
	}

	/**
	 * Builds a model instance from its header and serialized content.
	 *
	 * @param header the model header; must not be null
	 * @param modelContent the serialized model content; must not be null
	 * @return the constructed model instance
	 */
	@Override protected abstract T buildModelFromHeaderAndContent(@NonNull Header header, @NonNull String modelContent);

	/**
	 * Filters models to include only entries matching the document model type.
	 *
	 * @return a predicate selecting document models
	 */
	@Override protected Predicate<ModelHeaderEntity> filterModelsToMatchModelType() {
		return m -> DOCUMENT_MODEL_TYPE.equals(m.getModelType());
	}
}
