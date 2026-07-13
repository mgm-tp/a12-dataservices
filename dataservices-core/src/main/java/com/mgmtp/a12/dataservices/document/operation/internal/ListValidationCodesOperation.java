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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.document.SecuredValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Get map of document model names and its relevant validation codes.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.LIST_DOCUMENT_VALIDATION_CODES_INTERNAL_OPERATION, group = CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP, isMutation = false)
@RequiredArgsConstructor
public class ListValidationCodesOperation {

	private final SecuredValidationCodeGenerator securedValidationCodeGenerator;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final ModelHeaderJpaRepository modelHeaderJpaRepository;

	/**
	 * @param documentModelNames List of document model names to get validation codes for.  If the model of the requested name doesn't exist, the model is silently omitted in the response without any warning.
	 * @return {@link DocumentValidationCodesResponse} which consists of a map of model name and its validation code in the field `documentValidationCodes`.
	 */
	public DocumentValidationCodesResponse rpc(@JsonRpcParam("documentModelNames") @NonNull Collection<String> documentModelNames) {
		int limit = dataServicesCoreProperties.getDocuments().getValidation().getList().getHardLimit();
		if (documentModelNames.size() > limit) {
			throw new InvalidInputException(ExceptionCodes.HARD_LIMIT_EXCEEDED_EXCEPTION_CODE, ExceptionKeys.HARD_LIMIT_EXCEEDED_ERROR_KEY,
				"Maximum result size limit reached. Requested [%d] but limit is [%d]".formatted(documentModelNames.size(), limit));
		}
		ListIProblemReporter pr = new ListIProblemReporter();
		Map<String, String> validationCodes = documentModelNames.stream()
			.filter(modelHeaderJpaRepository::existsById)
			.collect(Collectors.toMap(modelName -> modelName, modelName -> securedValidationCodeGenerator.generateValidationCode(modelName, pr)));
		pr.validate(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE, ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY, "Error while validation codes generation");
		return DocumentValidationCodesResponse.builder()
			.documentValidationCodes(validationCodes)
			.build();
	}
}
