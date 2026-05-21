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

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

/**
 * Interface for model CRUD operations.
 * Default implementation is provided by DataServices.
 */
public interface IModelRepository {

	/**
	 * Depending on model header information returns if an implementation of this interface may be used to
	 * perform CRUD operations for this model.
	 *
	 * @param header The model header
	 * @return true if the model is supported
	 */
	boolean supports(@NonNull Header header);

	/**
	 * Saves the model content to the model specified by the passed header or creates new model if not existent.
	 *
	 * @param header       The model header
	 * @param modelContent The model content
	 * @return The model after the save operation as a {@link GenericModel}
	 */
	@Transactional
	GenericModel save(@NonNull Header header, @NonNull String modelContent);

	/**
	 * Updates the model header and the model content to the model specified by the passed header.
	 *
	 * @param newHeader       The model header
	 * @param newModelContent The model content
	 * @return The model after the save operation as a {@link GenericModel}
	 */
	@Transactional
	GenericModel update(@NonNull Header newHeader, @NonNull String newModelContent);

	/**
	 * Deletes the model specified by the passed header.
	 *
	 * @param header The model header
	 * @return true if model could be deleted ot if model didn't exist
	 */
	@Transactional
	boolean delete(@NonNull Header header);

	/**
	 * Checks if the model specified by the passed header exists.
	 *
	 * @param header The model header
	 * @return true if the model exists
	 */
	@Transactional(readOnly = true)
	boolean exists(@NonNull Header header);

	/**
	 * Loads the model specified by the passed header.
	 *
	 * @param header The model header
	 * @return The model specified by the passed header as a {@link GenericModel}
	 */
	@Transactional(readOnly = true)
	Optional<GenericModel> load(@NonNull Header header);

}
