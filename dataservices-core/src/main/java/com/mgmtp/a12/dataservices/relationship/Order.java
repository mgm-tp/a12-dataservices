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
package com.mgmtp.a12.dataservices.relationship;

import org.springframework.data.domain.Sort;

import lombok.Getter;

/**
 * Sorting order wrapper mapping to Spring Data's {@link Sort.Direction}.
 */
public enum Order {
	ASC(Sort.Direction.ASC), DESC(Sort.Direction.DESC);

	@Getter private final Sort.Direction direction;

	Order(Sort.Direction direction) {
		this.direction = direction;
	}

	/**
	 * Creates a {@link Sort.Order} for the given property using this enum's direction.
	 *
	 * @param property Property name to sort by; must not be null.
	 * @return A Spring {@link Sort.Order} configured with the mapped direction.
	 */
	public Sort.Order getOrder(String property) {
		return new Sort.Order(getDirection(), property);
	}
}
