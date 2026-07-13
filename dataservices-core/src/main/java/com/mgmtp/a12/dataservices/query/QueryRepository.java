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
package com.mgmtp.a12.dataservices.query;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Repository for insecure executing of the Query.
 */
@OnlyForUsage public interface QueryRepository {

	/**
	 * Returns a page of document tree entries according to the passed query and page request.
	 * No validations, enrichments, or additional input processing happens inside the repository. This happens in the {@link QueryService}.
	 *
	 * @param queryRoot the query root
	 * @param types the types
	 * @param queryContext the query context
	 * @return the page of query results
	 */
	@Transactional(readOnly = true)
	@NonNull Page<DocumentTreeResult> query(QueryRoot queryRoot, Collection<DocumentTreeNodeType> types, QueryContext queryContext);
}
