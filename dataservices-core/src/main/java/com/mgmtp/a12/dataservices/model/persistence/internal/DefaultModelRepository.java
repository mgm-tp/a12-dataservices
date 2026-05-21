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
package com.mgmtp.a12.dataservices.model.persistence.internal;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.GenericModelContent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class DefaultModelRepository implements IModelRepository {

	private final ModelJpaRepository modelJpaRepository;
	private final IModelReadRepository<GenericModel> genericModelReadRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Override public boolean supports(Header header) {
		return true;
	}

	@Transactional
	@Override public GenericModel save(@NonNull Header header, @NonNull String modelContent) {
		ModelEntity modelEntity = new ModelEntity(header, modelContent);
		modelEntity.setCreatedBy(UaaConnector.getCurrentUserName());
		String savedContent = modelJpaRepository.save(getModelEntityFromBeforeRepositorySaveEvent(modelEntity, header)).getContent();
		log.debug("Model [{}] has been successfully created", header.getId());
		return GenericModel.of(header, savedContent);
	}

	@Transactional
	@Override public GenericModel update(@NonNull Header header, @NonNull String modelContent) {
		GenericModel newModel = GenericModel.of(header, modelContent);
		ModelEntity updatedModelEntity = new ModelEntity(header, modelContent);
		updatedModelEntity.setUpdatedBy(UaaConnector.getCurrentUserName());
		ModelEntity savedEntity = modelJpaRepository.save(getModelEntityFromBeforeRepositorySaveEvent(updatedModelEntity, header));
		newModel.setContent(new GenericModelContent(savedEntity.getContent()));
		log.debug(String.format("Model [%s] has been successfully updated", header.getId()));
		return newModel;
	}

	@Transactional
	@Override public boolean delete(@NonNull Header header) {
		modelJpaRepository.findById(header.getId())
			.ifPresent(modelEntity -> {
				modelJpaRepository.deleteById(header.getId());
				log.debug(String.format("Model [%s] has been successfully deleted", header.getId()));
			});
		return true;
	}

	@Transactional(readOnly = true)
	@Override public boolean exists(@NonNull Header header) {
		return modelJpaRepository.existsById(header.getId());
	}

	@Transactional(readOnly = true)
	@Override public Optional<GenericModel> load(@NonNull Header header) {
		try {
			return Optional.of(genericModelReadRepository.readModel(header.getId()));
		} catch (InvalidInputException | NotFoundException e) {
			return Optional.empty();
		}
	}

	private ModelEntity getModelEntityFromBeforeRepositorySaveEvent(ModelEntity modelEntity, Header modelHeader) {
		ModelBeforeRepositorySaveEvent beforeRepositorySaveEvent =
			new ModelBeforeRepositorySaveEvent(modelHeader.getModelType(), modelHeader.getId(), modelEntity.getContent());
		eventPublisher.publishEvent(beforeRepositorySaveEvent);
		modelEntity.setContent(beforeRepositorySaveEvent.getModelEntityContent());
		return modelEntity;
	}

}
