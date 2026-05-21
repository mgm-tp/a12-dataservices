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
package com.mgmtp.a12.examples.custom.operator;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.search.customizer.DocumentFieldContext;
import com.mgmtp.a12.dataservices.search.customizer.SearchCustomizer;
import com.mgmtp.a12.dataservices.search.customizer.SearchDataContext;
import com.mgmtp.a12.examples.custom.type.RenamedTaxIDCustomFieldTypeFactory;
import com.mgmtp.a12.examples.custom.type.TaxIDCustomFieldType;

import lombok.extern.slf4j.Slf4j;

/**
 * SearchCustomizer implementation for TaxID custom field type.
 *
 * Extracts the numeric component (8 digits) from TaxID values (format: US12345678)
 * and indexes it in the `number_value` column to enable range queries via the
 * `tax_number_range` operator.
 *
 * This customizer is only active when the property
 * `com.mgmtp.a12.examples.custom-operator.enabled` is set to `true`.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "com.mgmtp.a12.examples.custom-operator.enabled", havingValue = "true")
public class TaxIDSearchCustomizer implements SearchCustomizer {

	private static final String PERSON_WITH_CUSTOM_TYPE_MODEL_NAME = "PersonWithCustomType";
	private static final String TAX_ID_FIELD_PATH = "/Person/TaxIDCustomFieldType";
	private static final Pattern TAX_ID_PATTERN = Pattern.compile(TaxIDCustomFieldType.INTERNAL_PATTERN);
	public static final String TAX_ID_CUSTOM_SOURCE = "tax_id_customizer";

	@Override
	public void customizeSearchData(SearchDataContext context) {
	}

	@Override
	public void customizeDocumentFields(DocumentFieldContext context) {
		if (!PERSON_WITH_CUSTOM_TYPE_MODEL_NAME.equals(context.getModelName())) {
			return;
		}

		String fieldPath = context.getFieldPath();
		if (!TAX_ID_FIELD_PATH.equals(fieldPath)) {
			return;
		}

		Object valueObj = context.getFieldInstance().value();
		if (valueObj == null) {
			log.debug("TaxID value is null for field path: {}", fieldPath);
			return;
		}

		String originalValue = valueObj.toString();
		if (originalValue.isEmpty()) {
			log.debug("TaxID value is empty for field path: {}", fieldPath);
			return;
		}

		Long longVal = extractTaxIdAsLong(originalValue);
		if (longVal == null) {
			log.warn("TaxID value '{}' does not match expected pattern for field path: {}", originalValue, fieldPath);
		} else {
			context.addField(fieldPath, originalValue, originalValue, BigDecimal.valueOf(longVal), null, null,
				RenamedTaxIDCustomFieldTypeFactory.TAX_ID_CUSTOM_FIELD_TYPE, TAX_ID_CUSTOM_SOURCE);
		}
	}

	@Nullable public static Long extractTaxIdAsLong(String originalValue) {
		String numericPart = extractTaxId(originalValue);
		return StringUtils.isBlank(numericPart) ? null : Long.parseLong(numericPart);
	}

	@Nullable public static String extractTaxId(String originalValue) {
		if (originalValue == null) {
			return null;
		}
		Matcher matcher = TAX_ID_PATTERN.matcher(originalValue);
		if (!matcher.matches()) {
			log.warn("TaxID value '{}' does not match expected pattern.", originalValue);
			return null;
		}
		return matcher.group(2);
	}
}
