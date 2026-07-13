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
package com.mgmtp.a12.dataservices.common.anonymizing;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;

/**
 * An {@link Anonymizer} that masks text by replacing characters with asterisks.
 * The masking length is controlled by the constructor parameter:
 *
 * - length > 0: always produce exactly `length` asterisks.
 * - length == 0: produce an empty string.
 * - length < 0: produce asterisks matching the original input length.
 */
public class AsterixAnonymizer implements Anonymizer {

	private static final int DEFAULT_LENGTH = 3;
	private static final char DEFAULT_CHAR = '*';
	private final int length;

	/**
	 * Creates an anonymizer with a fixed default mask length.
	 * The default is `3` asterisks.
	 */
	public AsterixAnonymizer() {
		this(false);
	}

	/**
	 * Creates an anonymizer with configurable mask length behavior.
	 *
	 * @param keepLength if `true`, the mask length matches the input length; if `false`, uses the default fixed length.
	 */
	public AsterixAnonymizer(boolean keepLength) {
		this(keepLength ? -1 : DEFAULT_LENGTH);
	}

	/**
	 * Creates an anonymizer with an explicit mask length setting.
	 *
	 * @param length mask length rule; `> 0` fixed length, `0` empty result, `< 0` same length as input.
	 */
	public AsterixAnonymizer(int length) {
		this.length = length;
	}

	/**
	 * Masks the input string with asterisks according to the configured rule.
	 *
	 * @param s input string; must not be `null`.
	 * @return	masked string consisting of asterisks.
	 */
	@Override public String apply(@NonNull String s) {
		return StringUtils.repeat(DEFAULT_CHAR, length >= 0 ? length : s.length());
	}
}
