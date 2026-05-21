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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

/**
 * Context for customizing document field indexing.
 *
 * Provides access to the current field being indexed and methods to add custom field entries
 * to the `document_fields` table.
 */
public interface DocumentFieldContext {

	/**
	 * Returns the document model search service.
	 *
	 * @return the document model search service
	 */
	IDocumentModelSearchService getDocumentModelSearchService();

	/**
	 * Returns the document reference.
	 *
	 * @return the document reference
	 */
	DocumentReference getDocumentReference();

	/**
	 * Returns the document pointer relative to base.
	 *
	 * @return the document pointer
	 */
	DocumentPointer getPointerRelativeToBase();

	/**
	 * Returns the name of the model being indexed.
	 *
	 * @return the model name
	 */
	String getModelName();

	/**
	 * Returns the full path of the field being indexed.
	 *
	 * @return the field path (e.g., "/Person/TaxIDCustomFieldType")
	 */
	String getFieldPath();

	/**
	 * Returns the metadata field definition.
	 *
	 * @return the field metadata
	 */
	IField getField();

	/**
	 * Returns the field instance containing the actual field data.
	 *
	 * @return the field instance
	 */
	FieldInstanceV2 getFieldInstance();

	/**
	 * Returns the field type ID provider function.
	 *
	 * @return the field type ID provider
	 */
	BiFunction<String, String, Long> getFieldTypeIdProvider();

	/**
	 * Adds a custom field entry to the `document_fields` table.
	 *
	 * @param fieldPath the field path (e.g., "/Person/TaxIDCustomFieldType")
	 * @param value the formatted string value
	 * @param typedValue the typed string value (optional, can be `null`)
	 * @param numberValue the numeric value (optional, can be `null`)
	 * @param timestampValue the timestamp value (optional, can be `null`)
	 * @param tsRangeValue the timestamp range value (optional, can be `null`)
	 * @param type the field type name
	 * @param source the source identifier (MANDATORY - must not be `null` or empty)
	 * @return this context instance for method chaining
	 */
	DocumentFieldContext addField(String fieldPath, String value, String typedValue,
		BigDecimal numberValue, LocalDateTime timestampValue,
		Object tsRangeValue, String type, String source);

	/**
	 * Skips the default core field indexing for the current field.
	 *
	 * Use this method when you want to completely replace the default indexing behavior
	 * with custom field entries added via {@link #addField}.
	 *
	 * @return this context instance for method chaining
	 */
	DocumentFieldContext skipCoreFieldIndexing();

	/**
	 * Checks whether core field indexing has been skipped for the current field.
	 *
	 * @return {@code true} if core field indexing is skipped, {@code false} otherwise
	 */
	boolean isCoreFieldIndexingSkipped();
}
