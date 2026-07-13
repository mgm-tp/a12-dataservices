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
package com.mgmtp.a12.dataservices.query.validation;

import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Validation Item record. Represents a single validation result item, including the path to the validated element,
 * whether it is valid, and an optional message.
 *
 */
public record ValidationItem(String[] path, boolean valid, String message) {

	public static ValidationItem valid(String[] path, String message) {
		return new ValidationItem(path, true, message);
	}

	public static ValidationItem invalid(String[] path, String message) {
		return new ValidationItem(path, false, message);
	}

	@Override public String toString() {
		return "ValidationItem{" +
			"path=" + Arrays.toString(path) +
			", valid=" + valid +
			", message='" + message + '\'' +
			'}';
	}

	@Override public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof ValidationItem that) {
			return new EqualsBuilder()
				.append(valid(), that.valid())
				.append(path(), that.path())
				.append(message(), that.message())
				.isEquals();
		} else {
			return false;
		}
	}

	@Override public int hashCode() {
		return new HashCodeBuilder(17, 37).append(path()).append(valid()).append(message()).toHashCode();
	}
}
