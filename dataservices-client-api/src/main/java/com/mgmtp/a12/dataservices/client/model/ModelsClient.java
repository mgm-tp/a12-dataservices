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
package com.mgmtp.a12.dataservices.client.model;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import lombok.NonNull;

/**
 * ModelsClient interface provides all Models based functionality
 */
public interface ModelsClient {

	/**
	 * Upload bulk of models.
	 *
	 * @param modelBulk InputStream of zip/jar file containing all the models to import
	 * @return List of imported models names
	 */
	List<String> importModelBulk(@NonNull InputStream modelBulk);

	/**
	 * Create model from JSON string.
	 *
	 * @param modelContent Model content as JSON
	 * @return Model content as JSON
	 */
	String createModel(@NonNull Reader modelContent);

	/**
	 * Get model as JSON string.
	 *
	 * @param modelId Model name to obtain
	 * @return Model content as JSON
	 */
	String loadModel(@NonNull String modelId);

	/**
	 * Update model from JSON string.
	 *
	 * @param modelContent Model content as JSON
	 * @return Model content as JSON
	 */
	String updateModel(@NonNull String modelContent);

	/**
	 * Delete model by ModelName.
	 *
	 * @param modelId ModelName of model to delete.
	 */
	void deleteModel(@NonNull String modelId);

	/**
	 * Generate model validation code.
	 * @param modelId ModelName of model to generate validation code.
	 * @return the validation code
	 */
	String generateValidationCode(@NonNull final String modelId);
}
