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
package com.mgmtp.a12.dataservices.search.customizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

/**
 * Context providing information about model fields during search customization.
 * This interface provides access to field metadata and path information
 * used for customizing search indices.
 */
public interface ModelFieldsContext {

	/**
	 * Returns the name of the model.
	 *
	 * @return the model name
	 */
	String getModelName();

	/**
	 * Returns the path to the field within the model.
	 *
	 * @return the field path
	 */
	String getPath();

	/**
	 * Returns the field metadata.
	 *
	 * @return the field instance
	 */
	IField getField();

	/**
	 * Returns the effective field type after resolution.
	 *
	 * @return the effective field type
	 */
	IFieldType getEffectiveFieldType();

	/**
	 * Returns the original localized value for the specified locale and key.
	 *
	 * @param locale the locale identifier
	 * @param key the localization key
	 * @return the original localized value, or null if not found
	 */
	String getOriginalLocalizedValue(String locale, String key);

	/**
	 * Adds or updates a localized value for the specified locale and key.
	 *
	 * @param locale the locale identifier
	 * @param key the localization key
	 * @param value the localized value to set
	 * @return this context instance for method chaining
	 */
	ModelFieldsContext putLocalizedValue(String locale, String key, String value);

	/**
	 * Sets the field value with the specified type and data.
	 *
	 * @param fieldType the field type identifier
	 * @param data the field data as JSON node
	 * @return this context instance for method chaining
	 */
	ModelFieldsContext setFieldValue(String fieldType, JsonNode data);
}
