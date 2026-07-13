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
package com.mgmtp.a12.dataservices.model.cdm.persistence;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.document.persistence.AbstractDocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * Reads composed document models from the persistent store and enforces composed-type filtering.
 * Adds transparent caching via {@link ModelCacheManager} and throws {@link NotFoundException}
 * if the model is not a composed document model.
 */
@Component public class ComposeDocumentModelReadRepository extends AbstractDocumentModelReadRepository<ComposeDocumentModel> {

	public ComposeDocumentModelReadRepository(ModelJpaRepository modelJpaRepository,
		ModelHeaderJpaRepository modelHeaderJpaRepository, ApplicationEventPublisher eventPublisher, DocumentModelUtils documentModelUtils) {
		super(modelJpaRepository, modelHeaderJpaRepository, eventPublisher, documentModelUtils);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Additionally verifies the loaded model is a composed document model and throws
	 * {@link NotFoundException} if it is not.
	 */
	@Cacheable(value = ModelCacheManager.COMPOSE_DOCUMENT_MODEL_READ_CACHE)
	@Override public ComposeDocumentModel readModel(@NonNull String modelId) {
		ComposeDocumentModel model = new ComposeDocumentModel(super.readModel(modelId));
		if (!ComposeDocumentModelUtils.isComposeDocumentModel(model.getHeader())) {
			throw new NotFoundException(ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY,
				"%s %s is not available.".formatted(getModelTypeForMessage(), modelId));
		}
		return model;
	}

	@Override protected String getModelTypeForMessage() {
		return "Composed document model";
	}

	@Override protected ComposeDocumentModel buildModelFromHeaderAndContent(@NonNull Header header, @NonNull String modelContent) {
		return new ComposeDocumentModel(documentModelUtils.deserializeDocumentModel(header.getId(), modelContent));
	}

	/**
	 * Returns the headers of all composed document models available in the repository.
	 *
	 * @return A list of composed document model headers.
	 */
	public List<Header> readAllModelHeaders() {
		return modelHeaderJpaRepository.findByModelType(DOCUMENT_MODEL_TYPE).stream()
			.filter(ComposeDocumentModelUtils::isComposeDocumentModel)
			.toList();
	}
}
