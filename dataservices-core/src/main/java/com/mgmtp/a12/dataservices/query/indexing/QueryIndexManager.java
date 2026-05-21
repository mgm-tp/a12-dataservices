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
package com.mgmtp.a12.dataservices.query.indexing;

import java.util.Collection;

/**
 * Provides a functionality to explicitly manage the query index. This includes indexing of all or specific document models
 * as well as cleaning the index.
 *
 * {@link com.mgmtp.a12.dataservices.document.DocumentService} automatically keeps the index up to date when documents
 * are created, updated or deleted in the same transaction. Therefore, it is usually not necessary to explicitly call
 * the methods of this interface. During migration, it also recommended to use {@link com.mgmtp.a12.dataservices.document.DocumentService}
 * and not call the methods of this interface directly for performance and consistency reasons.
 */
public interface QueryIndexManager {
	/**
	 * Indexes the query data. All document models are indexed.
	 */
	void indexQuery();

	/**
	 * Indexes the query data for the given document models. If `ignoreErrors` is `true`, errors during
	 * indexing of a document model are logged but do not interrupt the indexing of other document models. Documents in
	 * the failed batch are not indexed because the transaction is rolled back.
	 *
	 * @param documentModelNames the names of the document models to index
	 * @param ignoreErrors whether errors during indexing of a document model should be ignored
	 */
	void indexNew(Collection<String> documentModelNames, boolean ignoreErrors);

	/**
	 * Indexes the query data for the given document models. If `ignoreErrors` is `true`, errors during
	 * indexing of a document model are logged but do not interrupt the indexing of other document models. Documents in
	 * 	 * the failed batch are not indexed because the transaction is rolled back.
	 *
	 * @param documentModelNames the names of the document models to index
	 * @param ignoreErrors whether errors during indexing of a document model should be ignored
	 */
	void index(Collection<String> documentModelNames, boolean ignoreErrors);

	/**
	 * Removes all data from the index
	 */
	void cleanIndex();
}
