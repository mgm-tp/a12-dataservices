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
package com.mgmtp.a12.dataservices.authorization.internal;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a helper class to evaluate the model permissions and cache them per user and model name.
 */
@Slf4j @RequiredArgsConstructor
@Component public abstract class AbstractModelPermissionEvaluator<T extends Model> implements ModelPermissionEvaluator<T> {

	private final AuthorizationService authorizationService;
	private final CachedPermissionEvaluator cachedPermissionEvaluator;
	private final ModelHeaderJpaRepository modelHeaderRepository;

	public void checkModelCreatePermission(Header header) {
		log.debug("Check model create permission for model [{}]", header.getId());
		checkPermission(header, AuthConstants.MODEL_CREATE_PERMISSION);
	}

	public void checkModelUpdatePermission(Header header) {
		log.debug("Check model update permission for model [{}]", header.getId());
		checkPermission(header, AuthConstants.MODEL_UPDATE_PERMISSION);
	}

	public void checkModelDeletePermission(Header header) {
		log.debug("Check model delete permission for model [{}]", header.getId());
		checkPermission(header, AuthConstants.MODEL_DELETE_PERMISSION);
	}

	public void checkModelReadPermission(Header header) {
		log.debug("Check model read permission for model [{}]", header.getId());
		if (!hasModelReadPermission(header)) {
			throw new AccessDeniedException(AuthConstants.ACCESS_DENIED);
		}
	}

	/**
	 * Check the Model Read permission for the model with id = modelId.
	 * For this, first the model header is loaded by model id.
	 *
	 * @param modelId The model id.
	 * @throws NotFoundException if the model cannot be found for this modelId.
	 * @throws AccessDeniedException if the access was denied.
	 */
	public void checkModelReadPermission(String modelId) {
		Header header = Optional.ofNullable(modelId)
			.flatMap(modelHeaderRepository::findById)
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY, "Model [%s] not found".formatted(modelId)));
		if (!hasModelReadPermission(header)) {
			throw new AccessDeniedException(AuthConstants.ACCESS_DENIED);
		}
	}

	public void checkModelReadPermission(T model) {
		checkModelReadPermission(model.getHeader());
	}

	public boolean hasModelReadPermission(Header header) {
		log.debug("Check model read permission for model [{}]", header.getId());
		return cachedPermissionEvaluator.hasModelReadPermission(header);
	}

	/**
	 * Check the Model Read permission for the model with id = modelId.
	 * For this, first the model header is loaded by model id.
	 *
	 * @param modelId The model id.
	 * @return true if the model header could be loaded and the permission was granted, otherwise false.
	 */
	public boolean hasModelReadPermission(String modelId) {
		return Optional.ofNullable(modelId)
					.flatMap(modelHeaderRepository::findById)
					.map(this::hasModelReadPermission)
					.orElse(false);
	}

	public boolean hasModelReadPermission(T model) {
		return hasModelReadPermission(model.getHeader());
	}

	private void checkPermission(Object resource, String scopeName) {
		if (authorizationService.checkPermissions(resource, scopeName).isNotPassed()) {
			throw new AccessDeniedException(AuthConstants.ACCESS_DENIED);
		}
	}
}
