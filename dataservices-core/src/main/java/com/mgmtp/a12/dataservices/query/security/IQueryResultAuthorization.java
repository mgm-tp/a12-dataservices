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
package com.mgmtp.a12.dataservices.query.security;

import java.util.List;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;

/**
 * The implementation of this interface provided a place where client projects can write their own logic to authorize document read on all documents.
 * The execution result might reduce set of {@link DocumentTreeResult}, result contains only loadable documents.
 * Query processing is continued without interruption if some documents are not present in the result.
 * Broken paging is inevitable.
 */
public interface IQueryResultAuthorization {

	/**
	 * This method executes authorization logic on set of DocumentTreeResult, the only loadable results are returned.
	 *
	 * @param documentTreeResults The List of DocumentTreeResult from the query that need authorization evaluation.
	 * @return List of loadable DocumentTreeResult.
	 */
	List<DocumentTreeResult> authorizeQueryResult(final List<DocumentTreeResult> documentTreeResults);
}
