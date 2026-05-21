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

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

/**
 * Context for managing search data associated with a specific model.
 * Provides methods to retrieve, append, and replace search data during search operations.
 */
public interface SearchDataContext {

	/**
	 * Returns the document model search service.
	 *
	 * @return the document model search service
	 */
	IDocumentModelSearchService getDocumentModelSearchService();

	/**
	 * Returns the indexable document.
	 *
	 * @return the indexable document
	 */
	DocumentV2 getIndexableDocument();

	/**
	 * Returns the name of the model associated with this search context.
	 *
	 * @return the model name
	 */
	String getModelName();

	/**
	 * Returns the current search data.
	 *
	 * @return the current search data as a string
	 */
	String getCurrentSearchData();

	/**
	 * Appends additional data to the existing search data.
	 *
	 * @param additionalData the data to append to the current search data
	 */
	void appendToSearchData(String additionalData);

	/**
	 * Replaces the current search data with new data.
	 *
	 * @param newSearchData the new search data to replace the current data
	 */
	void replaceSearchData(String newSearchData);
}
