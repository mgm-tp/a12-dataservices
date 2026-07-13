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

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.ModelSerializationException;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * Validates uniqueness constraint definitions within a Document Model at model-save time.
 *
 * Checks that field paths referenced by uniqueness criteria exist in the model and that
 * constraint names are unique. Only processes models of type `DOCUMENT_MODEL_TYPE`; all
 * other model types are silently ignored.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniqueConstraintModelValidator {

	private final DocumentModelService documentModelService;
	private final DocumentModelUtils documentModelUtils;

	/**
	 * Validates uniqueness constraint definitions within the given Document Model.
	 *
	 * Deserializes the model from raw content and runs a consistency check via
	 * `DocumentModelService`. Only processes models of type `DOCUMENT_MODEL_TYPE`; all
	 * other model types are silently ignored.
	 *
	 * @param genericModel the generic model whose content is checked.
	 * @throws ModelSerializationException (code -32061) if any uniqueness constraint
	 * definition in the model is invalid.
	 */
	public void validateModel(GenericModel genericModel) {
		if (DOCUMENT_MODEL_TYPE.equals(genericModel.getHeader().getModelType())) {
			DocumentModel documentModel = documentModelService.convertFromExternal(documentModelUtils.deserializeDocumentModel(
				genericModel.getHeader().getId(), genericModel.getContent().getRawContent())
			);

			ListIProblemReporter pr = new ListIProblemReporter();
			documentModel.getContent().getDocumentUniquenessCriteria().forEach(criterion -> {
				documentModelService.checkDocumentUniquenessCriterionConsistency(
					documentModel,
					criterion,
					pr
				);
			});

			try {
				pr.validate(ExceptionCodes.MODEL_UNIQUE_CONSTRAINT_VALIDATION_EXCEPTION_CODE,
					ExceptionKeys.MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_KEY,
					"Error while validating unique constraints for Document Model");
			} catch (DataServicesDocumentProblemReporterException e) {
				throw new ModelSerializationException(
					ExceptionCodes.MODEL_UNIQUE_CONSTRAINT_VALIDATION_EXCEPTION_CODE,
					ExceptionKeys.MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_KEY,
					e.getMessage(),
					e
				);
			}
		}
	}
}

