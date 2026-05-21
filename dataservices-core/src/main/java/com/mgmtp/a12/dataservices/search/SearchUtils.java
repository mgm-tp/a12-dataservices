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
package com.mgmtp.a12.dataservices.search;

import java.util.Optional;
import com.mgmtp.a12.dataservices.rpc.query.PageSpec;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated the class was used in Solr-specific implementations that have been removed in 38.0.0
 *
 * SearchUtils provides definition of index names and naming patterns. There are also some small util methods that are
 * being used from the various parts of index manipulation.
 */
@Slf4j
@Deprecated(since = "38.1.0", forRemoval = true)
@UtilityClass public class SearchUtils {

	/**
	 * Resolve pagespec defaults if some values are missing and validate values against hard-limits from configuration. In case of crossed limit the value is replaced by the limit.
	 *
	 * CAUTION: This method was copied to `dataservices-core/src/main/java/com/mgmtp/a12/dataservices/rpc/internal/QueryPagingHelper` (and modified there),
	 * make sure to apply changes to both places if appropriate.
	 *
	 * @param pageSpec {@link PageSpec} to validate
	 * @param offsetLimit Hard limit for offset which can not be crossed
	 * @param pageLimit Hard limit for page size which can not be crossed
	 * @return {@link PageSpec} with missing values replaced by default ones and not overlapping the limits
	 */
	public static PageSpec validatePageSpec(PageSpec pageSpec, int offsetLimit, int pageLimit) {
		Optional<PageSpec> pageSpecOpt = Optional.ofNullable(pageSpec);
		return PageSpec.builder()
			.offset(validatePageSpecProperty(offsetLimit, pageSpecOpt.map(PageSpec::getOffset), "offset", PageSpec.NO_OFFSET))
			.limit(validatePageSpecProperty(pageLimit, pageSpecOpt.map(PageSpec::getLimit), "limit", PageSpec.DEFAULT_MAX_RESULTS))
			.build();
	}

	private static int validatePageSpecProperty(int limit, Optional<Integer> requested, String subject, int defaultValue) {
		return requested
			.filter(v -> v >= 0)
			.or(() -> Optional.of(defaultValue))
			.filter(v -> limit >= v)
			.orElseGet(() -> {
				log.warn("The requested {} is bigger than the allowed one. Reducing {} to {}.", subject, requested.orElse(null), limit);
				return limit;
			});
	}
}
