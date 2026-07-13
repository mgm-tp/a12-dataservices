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
package com.mgmtp.a12.dataservices.model;

import java.util.Set;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Model graph calculation is an expensive operation to be executed during run time, therefore, the model graph caches have been introduced.
 * In production environments, change of models is not needed but during modeling time, the model graph needs not to be cached because models
 * need to be changed during runtime. ModelTypeService works as a switch for model graph caching.
 */
@OnlyForUsage public interface ModelTypeService {

	/**
	 * Return deeply nested Document Model names, which are subtypes of the queried Document Model.
	 * Document Model from the parameter is not included in output.
	 *
	 * @param documentModelName The model to load subtypes of.
	 * @return Set of nested Document Model names, which are subtypes of the queried type.
	 */
	Set<String> findAllSubtypes(String documentModelName);

	/**
	 * Return Document Model names, which are direct subtypes of the queried Document Model.
	 * Document Model from the parameter is not included in output.
	 *
	 * @param documentModelName The model to load subtypes of.
	 * @return Set of Document Model names, which are direct subtypes of the queried type.
	 */
	Set<String> findDirectSubtypes(String documentModelName);

	/**
	 * Returns the given Document Model name together with all its deeply nested subtypes.
	 * Unlike {@link #findAllSubtypes(String)}, the queried model itself is included in the result.
	 *
	 * @param documentModelName The model whose name and subtypes are to be returned.
	 * @return Set containing `documentModelName` and all its (deeply nested) subtypes with read permission.
	 */
	Set<String> findModelNameAndAllSubtypes(String documentModelName);

	/**
	 * Checks whether `testedModelName` is equal to `parentModelName` or is one of its subtypes
	 * (at any depth in the inheritance hierarchy).
	 *
	 * @param parentModelName  The parent model to check against.
	 * @param testedModelName  The model to test.
	 * @return `true` if `testedModelName` equals `parentModelName` or is a subtype of it; `false` otherwise.
	 */
	boolean isSubtype(String parentModelName, String testedModelName);

}
