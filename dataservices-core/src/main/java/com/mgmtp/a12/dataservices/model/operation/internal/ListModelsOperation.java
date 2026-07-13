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
package com.mgmtp.a12.dataservices.model.operation.internal;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.operation.internal.ListModelsResponse;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * List model contents defined by filtering parameters. Currently, model names are supported.
 */
@Slf4j
@RequiredArgsConstructor
@RemoteOperation(name = CoreOperationConstants.LIST_MODELS_INTERNAL_OPERATION, group = CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP, isMutation = false)
public class ListModelsOperation {

	private final ModelService modelService;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * @param modelNames return models of these names. If the model of the requested name doesn't exist or the current user has no access to it, the model
	 * is silently omitted in the response without any warning.
	 * @return {@link Map} of all models referenced by model names on the input.
	 */
	public ListModelsResponse rpc(@NonNull @JsonRpcParam("modelNames") Collection<String> modelNames) {

		int limit = dataServicesCoreProperties.getModels().getList().getHardLimit();
		if (modelNames.size() > limit) {
			throw new InvalidInputException(ExceptionCodes.HARD_LIMIT_EXCEEDED_EXCEPTION_CODE, ExceptionKeys.HARD_LIMIT_EXCEEDED_ERROR_KEY,
				"Maximum result size limit reached. Requested [%d] but limit is [%d]".formatted(modelNames.size(), limit));
		}

		return ListModelsResponse.builder()
			.models(modelService.load(modelNames).stream()
				.collect(Collectors.toMap(m -> m.getHeader().getId(), Function.identity())))
			.build();
	}
}
