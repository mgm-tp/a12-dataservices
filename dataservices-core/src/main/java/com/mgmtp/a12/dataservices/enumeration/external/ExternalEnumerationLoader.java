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
package com.mgmtp.a12.dataservices.enumeration.external;

import com.mgmtp.a12.dataservices.document.DocumentReference;

import lombok.NonNull;

/**
 * Interface that provides the ability to load specific external enumerations.
 *
 * You can load enumerations via {@link ExternalEnumerationService}, which uses implementations of this loader to filter documents and create enumerations.
 */
public interface ExternalEnumerationLoader {

	/**
	 * Indicates whether this loader supports the given model.
	 *
	 * @param modelName The document model name; must not be `null`.
	 * @return `true if the model is supported, otherwise `false`.
	 */
	Boolean isModelSupported(@NonNull String modelName);

	/**
	 * Converts a document reference to an {@link ExternalEnumeration}.
	 *
	 * @param documentReference The document reference to convert; must not be `null`.
	 * @return The converted enumeration instance; may be `null` if conversion is not possible.
	 */
	ExternalEnumeration convertToEnumeration(@NonNull DocumentReference documentReference);
}
