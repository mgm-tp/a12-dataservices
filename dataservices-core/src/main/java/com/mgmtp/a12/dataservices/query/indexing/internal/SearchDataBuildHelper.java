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
package com.mgmtp.a12.dataservices.query.indexing.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.utils.internal.DateTimeUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.EnumerationTypeWrapper;
import com.rometools.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_INDEXING;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_SQUARE_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_SQUARE_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SEARCH_DATA_VALUE_DELIMITER;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

/**
 * A class to create a "flat map" of a DocumentV2 to be used for a full text search.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchDataBuildHelper {

	private static final int GUESSTIMATED_AVG_FIELD_LENGTH = 50;

	/**
	 * This method will create a flat key/value map of all field values of the document.
	 *
	 * If not all fields should be included, this must be done before passing the DocumentV2 instance.
	 *
	 * Basic de-duplication is applied when building the search string.
	 * This is mainly helpful if there are many repeating groups that might contain identical values
	 * (e.g. enum fields inside repeating groups).
	 */
	public static String buildSearchData(IDocumentModelSearchService documentModelSearchService, DocumentV2 document) {

		// Storing the values per path in a set does a basic de-duplication which helps if a lot of repeatable groups are present in the document.
		// And even more so if they contain enumerations with a low number of possible values.
		// The Query API does not support searching inside a specific position of a repeatable group, so there is no sense in keeping duplicates.
		Map<String, Set<String>> values = new HashMap<>();
		Map<String, Set<ILocalizedTextMap>> localizedEnumValues = new HashMap<>();

		AtomicInteger fieldVisitCount = new AtomicInteger(); // counts all visited fields

		document.traverse(new IDocumentV2Visitor() {
			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 fieldInstance) {
				fieldVisitCount.incrementAndGet();
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, fieldInstance);
				Optional<IField> field = DocumentModelUtils.findField(documentModelSearchService, pointerRelativeToBase.fullName());
				if (field.isPresent()) {
					String value = getFormattedFieldValue(field.get(), fieldInstance.value());
					if (Strings.isNotEmpty(value)) {
						String path = pointerRelativeToBase.fullName();
						values.computeIfAbsent(path, k -> new HashSet<>()).add(value);
						if (getEffectiveFieldType(field.get(), QUERY_INDEXING) instanceof EnumerationTypeWrapper enumType) {
							addLocalizedEnumValues(enumType, path, value, localizedEnumValues);
						}
					}
				}
			}
		});

		StringBuilder result = new StringBuilder(fieldVisitCount.get() * GUESSTIMATED_AVG_FIELD_LENGTH);
		appendFieldValues(result, values);
		appendEnumValues(result, localizedEnumValues);
		result.append(ModelConstants.FIELD_SEPARATOR);

		return result.toString();
	}

	private static void appendFieldValues(StringBuilder result, Map<String, Set<String>> values) {
		for (Map.Entry<String, Set<String>> entry : values.entrySet()) {
			for (String value : entry.getValue()) {
				result.append(entry.getKey()).append(SEARCH_DATA_VALUE_DELIMITER).append(value).append(SEARCH_DATA_VALUE_DELIMITER);
			}
		}
	}

	private static void appendEnumValues(StringBuilder result, Map<String, Set<ILocalizedTextMap>> localizedEnumValues) {
		for (Map.Entry<String, Set<ILocalizedTextMap>> entry : localizedEnumValues.entrySet()) {
			for (ILocalizedTextMap map : entry.getValue()) {
				for (Map.Entry<Locale, String> label : map.entrySet()) {
					result.append(entry.getKey());
					result.append(OPENING_SQUARE_BRACKET).append(getLanguage(label.getKey())).append(CLOSING_SQUARE_BRACKET);
					result.append(SEARCH_DATA_VALUE_DELIMITER).append(label.getValue()).append(SEARCH_DATA_VALUE_DELIMITER);
				}
			}
		}
	}

	private static void addLocalizedEnumValues(IEnumerationType enumType, String path, String fieldValue,
		Map<String, Set<ILocalizedTextMap>> localizedValues) {
		enumType.getValues().stream()
			.filter(v -> v.getValue().equals(fieldValue))
			.findAny()
			.ifPresent(iEnumValue -> localizedValues.computeIfAbsent(path, k -> new HashSet<>()).add(iEnumValue.getLabel()));
	}

	/**
	 * Creates a string that includes the language and the variant if present.
	 * We can't use Locale.getLanguage() as that doesn't return the variant
	 */
	private static String getLanguage(Locale l) {
		if (StringUtils.isEmpty(l.getCountry())) {
			return l.getLanguage();
		}
		return l.getLanguage() + "_" + l.getCountry();
	}

	private static String getFormattedFieldValue(IField field, Object fieldValue) {
		if (fieldValue == null) {
			return null;
		}
		String value;
		IFieldType effectiveFieldType = getEffectiveFieldType(field, QUERY_INDEXING);
		if (shouldUseFormatter(effectiveFieldType, fieldValue)) {
			String format = DateTimeUtils.getFieldFormat(effectiveFieldType).orElse(null);
			value = DocumentSearchIndexHelper.getFieldFormatter(effectiveFieldType)
				.format(fieldValue, format, TimeZone.getTimeZone(DateTimeUtils.STANDARD_TIME_ZONE));
		} else {
			value = fieldValue.toString();
		}
		return cleanupFieldValue(value);
	}

	private static boolean shouldUseFormatter(IFieldType type, Object value) {
		// For partial dates we always store the original value.
		// Sending a partial date to the formatter would return a date value that is "close" to the partial one
		// which means that with an exact macht wouldn't work any more.
		if (type instanceof IDateType idt && idt.arePartiallyKnownDatesAllowed() && value instanceof String) {
			return false;
		}
		return type instanceof IDateRangeType
			|| type instanceof IDateFragmentType
			|| type instanceof IDateType;
	}

	private static String cleanupFieldValue(String input) {
		return StringUtils.trimToNull(StringUtils.replace(input, SEARCH_DATA_VALUE_DELIMITER, ""));
	}
}
