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
package com.mgmtp.a12.dataservices.query.internal;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_SQL_GENERATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.QUERY_PROJECTION_NOT_FOUND_ERROR_KEY;

/**
 * Provides query projections by name. If multiple projections with the same name are registered, the one with the highest
 * precedence (lowest {@link org.springframework.core.Ordered} value) is returned.
 */
@RequiredArgsConstructor
@Component public class ProjectionProvider {

	private final List<IQueryProjection<?>> queryProjections;

	public <T> IQueryProjection<T> getMatchingProjection(String projectionName) {
		return queryProjections.stream()
			.filter(p -> p.getId().equalsIgnoreCase(projectionName))
			.map(p -> (IQueryProjection<T>) p)
			.findFirst()
			.orElseThrow(() -> new QueryInvalidInputException(QUERY_SQL_GENERATION, QUERY_PROJECTION_NOT_FOUND_ERROR_KEY, null)
				.withAnonymityMessage("Projection %s is not registered in the system.".formatted(projectionName)));
	}
}
