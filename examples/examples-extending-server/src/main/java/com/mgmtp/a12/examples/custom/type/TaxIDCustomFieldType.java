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
package com.mgmtp.a12.examples.custom.type;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Example of ICustomFieldType implementation.
 *
 */
public class TaxIDCustomFieldType implements ICustomFieldValidator {
	private static final String OUTPUT_FORMAT_INTERNAL = "%s%s";
	private static final String OUTPUT_FORMAT_DISPLAY = "%s-%s";
	public static final String INTERNAL_PATTERN = "([A-Z]{2})(\\d{8})";
	private static final String DISPLAY_PATTERN = "([A-Z]{2})\\-(\\d{8})";
	public static final String ERROR_MSG_DISPLAY_VALUE = "Tax ID must have country code (2 upper letters), a dash (-) and 8 digits";
	public static final String ERROR_MSG_INTERNAL_VALUE = "Tax ID must have country code (2 upper letters) followed by 8 digits";
	public static final String ERROR_KEY = "error.taxId.invalid";

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(@NonNull String value, @NonNull ICustomFieldTypeValidationParam valParam,
		boolean isDisplayValue) {
		return value.matches(INTERNAL_PATTERN)
			? Optional.empty()
			: Optional.of(new ICustomFieldTypeCheckError() {
			@Override public String getErrorMessage() {
				return ERROR_MSG_INTERNAL_VALUE;
			}

			@Override public String getErrorKey() {
				return ERROR_KEY;
			}
		});
	}

	@Override public ICustomFieldTypeConversionResult convertDisplay2Internal(String displayValue) {
		return new TaxIDCustomFieldTypeConversionResult(
			displayValue, DISPLAY_PATTERN, ERROR_MSG_DISPLAY_VALUE, OUTPUT_FORMAT_INTERNAL
		);
	}

	@Override public ICustomFieldTypeConversionResult convertInternal2Display(String internalValue) {
		return new TaxIDCustomFieldTypeConversionResult(
			internalValue, INTERNAL_PATTERN, ERROR_MSG_INTERNAL_VALUE, OUTPUT_FORMAT_DISPLAY
		);
	}

	@AllArgsConstructor
	private static class TaxIDCustomFieldTypeConversionResult implements ICustomFieldTypeConversionResult {
		private final String value;
		private final String pattern;
		private final String errorMsg;
		private final String outputFormat;

		@Override public String getConvertedValue() {
			return canConvert() ? convert() : value;
		}

		@Override public Optional<String> getErrorMessage() {
			return canConvert()
				? Optional.empty()
				: Optional.of(errorMsg);
		}

		private boolean canConvert() {
			return value.matches(pattern);
		}

		private String convert() {
			Matcher matcher = Pattern.compile(pattern).matcher(value);
			if (matcher.find()) {
				return outputFormat.formatted(matcher.group(1), matcher.group(2));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}
}
