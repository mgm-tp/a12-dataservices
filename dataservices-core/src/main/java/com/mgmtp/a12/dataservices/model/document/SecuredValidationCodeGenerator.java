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
package com.mgmtp.a12.dataservices.model.document;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.RequiredArgsConstructor;

/**
 * TODO: A12S-6269 Rename class in a breaking release to SecuredValidationCodeProvider
 *
 * A secured wrapper around an {@link IValidationCodeProvider} that checks read permissions
 * for the document model before generating the validation code.
 */
@Component
@RequiredArgsConstructor
public class SecuredValidationCodeGenerator {
	private final IValidationCodeProvider validationCodeLoader;
	private final ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;

	/**
	 * Generates a validation code for the given document model after checking read permissions.
	 * If the caller lacks permission to read the model, a {@link java.nio.file.AccessDeniedException} is thrown.
	 *
	 * @param documentModelId the ID of the document model; must not be null
	 * @param pr a consumer receiving {@link RankedNotification} messages during generation; may be used for problem reporting
	 * @return the validation code string for the model
	 */
	public String generateValidationCode(String documentModelId, Consumer<RankedNotification> pr) {
		modelPermissionEvaluator.checkModelReadPermission(documentModelId);
		return validationCodeLoader.getValidationCode(documentModelId, pr instanceof ListIProblemReporter problemReporter ? problemReporter : new ListIProblemReporter());
	}
}
