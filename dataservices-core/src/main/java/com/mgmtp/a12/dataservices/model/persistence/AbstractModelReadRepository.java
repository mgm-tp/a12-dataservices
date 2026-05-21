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
package com.mgmtp.a12.dataservices.model.persistence;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.model.events.ModelAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base implementation of {@link IModelReadRepository}.
 *
 */
@Slf4j
public abstract class AbstractModelReadRepository<T extends Model> implements IModelReadRepository<T> {

	@Autowired private ModelJpaRepository modelJpaRepository;
	@Autowired private ModelHeaderJpaRepository modelHeaderJpaRepository;
	@Autowired private ApplicationEventPublisher eventPublisher;

	/**
	 * Reads a model by its identifier from the persistent store.
	 *
	 * @param modelId The unique identifier of the model; must not be blank.
	 * @return The fully constructed model instance.
	 * @throws InvalidInputException if the modelId is blank.
	 * @throws NotFoundException if no model with the given id exists or does not match the repository's type filter.
	 */
	public T readModel(@NonNull String modelId) {
		if (StringUtils.isBlank(modelId)) {
			throw new InvalidInputException(ExceptionKeys.MODEL_ID_NOT_VALID_ERROR_KEY,
				String.format("%s shouldn't be blank or null", getModelTypeForMessage()));
		}
		StopWatch stopWatch = StopWatch.createStarted();
		return modelHeaderJpaRepository
			.findById(modelId)
			.filter(filterModelsToMatchModelType())
			.map(h -> {
				String content = getContent(h);
				stopWatch.stop();
				log.debug("{} [{}] has been loaded from persistent store in [{} ms]", getModelTypeForMessage(), modelId, stopWatch.getTime());
				String modelEntityContent = publishModelAfterRepositoryLoadEvent(h.getModelType(), modelId, content).getModelEntityContent();
				return buildModelFromHeaderAndContent(h, modelEntityContent);

			})
			.orElseThrow(() -> new NotFoundException(getModelNotFoundErrorKey(),
				String.format("%s [%s] not found", getModelTypeForMessage(), modelId)));
	}

	/**
	 * Retrieves the serialized content for the given header from the repository.
	 *
	 * @param h The model header referencing the stored entity; must not be null.
	 * @return The serialized model content as stored in the repository.
	 * @throws IllegalStateException if the content entity cannot be found for the given header.
	 */
	protected String getContent(Header h) {
		ModelEntity modelEntity = modelJpaRepository.findById(h.getId())
			.orElseThrow(() -> new IllegalStateException(String.format("Model not found for existing header %s", h.getId())));
		return modelEntity.getContent();
	}

	/**
	 * Returns the error key used when a model cannot be found.
	 *
	 * @return The error key representing the not-found condition.
	 */
	protected abstract String getModelNotFoundErrorKey();

	/**
	 * Returns a human-readable type name used in messages and logs.
	 *
	 * @return The type name for the model managed by this repository.
	 */
	protected abstract String getModelTypeForMessage();

	/**
	 * Constructs a model instance from header and serialized content.
	 *
	 * @param header The model header; must not be null.
	 * @param modelContent The serialized content; must not be null.
	 * @return A fully constructed model instance.
	 */
	protected abstract T buildModelFromHeaderAndContent(@NonNull Header header, @NonNull String modelContent);

	/**
	 * Provides a predicate that filters headers to match the model type handled by this repository.
	 *
	 * @return A predicate to filter model headers to the expected type.
	 */
	protected abstract Predicate<? super ModelHeaderEntity> filterModelsToMatchModelType();

	private ModelAfterRepositoryLoadEvent publishModelAfterRepositoryLoadEvent(String modelType, String modelName, String modelEntityContent) {
		ModelAfterRepositoryLoadEvent modelAfterRepositoryLoadEvent = new ModelAfterRepositoryLoadEvent(modelType, modelName, modelEntityContent);
		eventPublisher.publishEvent(modelAfterRepositoryLoadEvent);
		return modelAfterRepositoryLoadEvent;
	}

}
