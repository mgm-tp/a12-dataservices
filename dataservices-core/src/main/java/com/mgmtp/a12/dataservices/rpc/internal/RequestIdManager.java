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
package com.mgmtp.a12.dataservices.rpc.internal;

import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.RequestIdState;
import com.mgmtp.a12.dataservices.exception.RequestIdConflictException;
import com.mgmtp.a12.dataservices.rpc.internal.jpa.entity.RequestIdEntity;
import com.mgmtp.a12.dataservices.rpc.internal.jpa.repository.RequestIdRepository;

import static com.mgmtp.a12.dataservices.RequestIdState.FAILED;
import static com.mgmtp.a12.dataservices.RequestIdState.PENDING;
import static com.mgmtp.a12.dataservices.RequestIdState.SUCCESS;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Manager of unique request state with capabilities to mark the request as succeeded or failed.
 */
public class RequestIdManager {

	public static final String IS_ALREADY_PROCESSED = "Request with ID %s is already processed.";
	public static final String IS_ALREADY_PROCESSED_WITH_STATE_S = "Request with ID %s is already processed with state %s.";
	private final String requestId;
	private final RequestIdRepository requestIdRepository;

	/**
	 * Use {@link RequestIdService#newRequestIdManager(String)} to create new request.
	 */
	RequestIdManager(RequestIdRepository requestIdRepository, String requestId) {
		this.requestIdRepository = requestIdRepository;
		this.requestId = requestId;
		if (requestIdRepository.existsById(requestId)) {
			throw new RequestIdConflictException(IS_ALREADY_PROCESSED.formatted(requestId), requestId,
				requestIdRepository.getReferenceById(requestId).getState());
		} else {
			try {
				requestIdRepository.insert(requestId);
				requestIdRepository.flush();
			} catch (Exception e) {
				throw new RequestIdConflictException(IS_ALREADY_PROCESSED.formatted(requestId), requestId, PENDING);
			}
		}
	}

	@Transactional(propagation = REQUIRES_NEW)
	public void finalizeRequestSuccess() {
		finalizeRequestTransaction(SUCCESS);
	}

	@Transactional(propagation = REQUIRES_NEW)
	public void finalizeRequestError() {
		finalizeRequestTransaction(FAILED);
	}

	private void finalizeRequestTransaction(RequestIdState state) {
		RequestIdEntity entity = requestIdRepository.findById(requestId)
			.orElseThrow(() -> new RequestIdConflictException(IS_ALREADY_PROCESSED.formatted(requestId), requestId, PENDING));
		if (entity.getState() != PENDING) {
			throw new RequestIdConflictException(IS_ALREADY_PROCESSED_WITH_STATE_S.formatted(entity.getId(), entity.getState()),
				entity.getId(), entity.getState());
		}
		entity.setState(state);
		requestIdRepository.saveAndFlush(entity);

	}

}
