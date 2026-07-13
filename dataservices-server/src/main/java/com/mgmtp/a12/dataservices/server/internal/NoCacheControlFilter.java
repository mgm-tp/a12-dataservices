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
package com.mgmtp.a12.dataservices.server.internal;

import com.mgmtp.a12.dataservices.api.common.rest.NoCache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Provider for the {@link NoCache} annotation. The response of the methods annotated with the {@link NoCache} annotation
 * will contain the cache control headers.
 **/
@NoCache
public class NoCacheControlFilter extends AbstractAnnotationBasedControlFilter {

	private static final String NO_CACHE = "no-cache";
	private static final String NO_STORE = "no-store";
	private static final String MUST_REVALIDATE = "must-revalidate";

	private static final String PRIVATE = "private";
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String PRAGMA = "Pragma";
	private static final String EXPIRES = "Expires";
	private static final String VARY = "Vary";
	private static final String STAR = "*";
	private static final String IN_THE_PAST = "-1";

	@Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		getParameterAnnotations(handler, NoCache.class).findAny().ifPresent(a -> {
				response.addHeader(CACHE_CONTROL, "%s, %s, %s, %s".formatted(PRIVATE, NO_CACHE, NO_STORE, MUST_REVALIDATE));
				response.addHeader(PRAGMA, NO_CACHE);
				response.addHeader(VARY, STAR);
				response.addHeader(EXPIRES, IN_THE_PAST);
			}
		);
		return true;
	}

}
