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

/**
 * Strategy interface for customizing document search indexing behavior.
 *
 * Implementations of this interface can customize how documents are indexed for search operations.
 * This includes:
 * - Customizing the full-text search data (`search_data` column)
 * - Adding custom typed field values to `document_fields` table (e.g., extracting numeric values from custom field types)
 * - Customizing model field metadata (optional)
 *
 * Implementations must be registered as Spring beans (e.g., using `@Component`) to be automatically
 * discovered by the `SearchCustomizerRegistry`.
 *
 * Example use case: Extracting numeric component from a TaxID custom field type to enable range queries.
 *
 * @see DocumentFieldContext
 * @see SearchDataContext
 * @see ModelFieldsContext
 */
public interface SearchCustomizer {

	/**
	 * Customizes the full-text search data for a document.
	 *
	 * This method is called during document indexing to allow customization of the `search_data` column
	 * in the `document_search` table. Implementations can append additional searchable text or replace
	 * the entire search data.
	 *
	 * @param context the search data context providing access to the document and search data
	 */
	void customizeSearchData(SearchDataContext context);

	/**
	 * Customizes document field indexing by adding custom typed field values.
	 *
	 * This method is called for each indexable field during document indexing. Implementations can:
	 * - Extract typed values from custom field types (e.g., numeric component from TaxID)
	 * - Add additional field entries to the `document_fields` table
	 * - Skip core field indexing if needed
	 *
	 * All custom field values MUST include a `source` parameter to track their origin.
	 *
	 * @param context the document field context providing access to the field and methods to add custom fields
	 */
	void customizeDocumentFields(DocumentFieldContext context);

	/**
	 * Customizes model field metadata (optional).
	 *
	 * This method is called during model indexing to allow customization of model field metadata.
	 * Default implementation is a no-op.
	 *
	 * @param context the model fields context providing access to the model and methods to add custom fields
	 */
	default void customizeModelFields(ModelFieldsContext context) {
		// Optional; no-op by default
	}
}
