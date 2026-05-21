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
package com.mgmtp.a12.dataservices.rpc.query;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paging specification for legacy RPC queries.
 * @deprecated The class is based on the Solr API which is no longer supported. Use paging mechanisms provided by the Query API.
 */
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder
@Deprecated(since = "38.1.0", forRemoval = true)
public class PageSpec implements Serializable {

	/** Convenience value representing no logical limit (uses {@link Integer#MAX_VALUE}). */
	public static final PageSpec MAX_RESULTS = PageSpec.builder().offset(0).limit(Integer.MAX_VALUE).build();
	/** Starting offset for the first page (zero-based). */
	public static final int NO_OFFSET = 0;
	/** Default maximum number of results per page. */
	public static final int DEFAULT_MAX_RESULTS = 10;

	@Builder.Default
	private Integer offset = NO_OFFSET;
	@Builder.Default
	private Integer limit = DEFAULT_MAX_RESULTS;

}
