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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.internal.aggregation.RepeatableAggField;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;

/**
 * Helper that enriches the repeatability flag of a field's `FieldDescriptor` and, for aggregated
 * queries, records the repeatable aggregate-field paths and aliases on the `QueryContext`.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE) final class RepeatabilityEnrichmentHelper {

	/**
	 * Enriches the `FieldDescriptor` for `field` with its repeatability flag and, when
	 * `isAggregated` is `true`, also records the repeatable aggregate-field paths and aliases.
	 */
	public static void enrichRepeatability(String field, String targetDocumentModel, QueryContext context, boolean isAggregated) {
		FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor(field);
		if (fieldDescriptor.getRepeatable() != null && !isAggregated) {
			// nothing to do, already set
			return;
		}

		boolean repeatable = false;
		IElement element = context.getDocumentModelSearchService(targetDocumentModel).getByPath(field).orElse(null);
		Deque<String> paths = new ArrayDeque<>();
		String currentField = field;
		while (element != null && element.getParent() != null) {
			currentField = currentField.substring(0, currentField.lastIndexOf(FIELD_SEPARATOR));
			// A field is repeatable if it has at least one parent group that is repeatable
			if (element instanceof IGroup group && group.getRepeatability() > 1) {
				repeatable = true;
				if (isAggregated) {
					// in case of aggregation processing, we're traversing the whole repeatable group for aggregation, therefore will not break the loop
					paths = enrichRepeatableAggField(field, context, element, currentField, paths);
				} else {
					// as soon as we find such a group we can terminate the loop
					break;
				}
			} else {
				paths.push(element.getName());
			}
			element = element.getParent();
		}
		if (isAggregated && repeatable) {
			RepeatableAggField repeatableAggField = new RepeatableAggField("", paths);
			context.getEnrichments().addRepeatableAggField(field, repeatableAggField);
		}

		fieldDescriptor.setRepeatable(repeatable);
	}

	/**
	 * Enriches a repeatable aggregate field by generating a unique alias and storing its path.
	 * Ensures that each repeatable aggregate field gets a unique alias within the `QueryContext`
	 * so that multiple occurrences of the same element name in different aggregation groups do
	 * not collide.
	 * Example: three fields share the element name `Title` but have different paths -
	 * `/EmployeeRoot/Position/Title/Name`, `/EmployeeRoot/Department/Title/Name`, and
	 * `/EmployeeRoot/Project/Title/Name`. The element name `title` is the base alias for all of
	 * them; uniqueness is achieved by appending an incrementing numeric suffix (`_1`, `_2`, ...).
	 * The resolved aliases are `title` for `/EmployeeRoot/Position/Title`, `title_1` for
	 * `/EmployeeRoot/Department/Title`, and `title_2` for `/EmployeeRoot/Project/Title`.
	 * The next free suffix is picked by a single pass over the existing alias map: it scans all
	 * keys matching `baseAlias_<n>`, remembers the highest `n` seen, and uses `n + 1`. If an
	 * existing alias already maps to `currentField` it is reused as-is.
	 *
	 * @param field The original field name for which the repeatable aggregate field is being enriched.
	 * @param context The {@link QueryContext} containing enrichment data, including repeatable aggregate group aliases.
	 * @param element The {@link IElement} representing the current element, used to derive the initial alias.
	 * @param currentField The current field name being processed, used to check for existing aliases.
	 * @param paths A {@link Deque} of strings representing the current path of the field, which will be
	 * associated with the generated alias.
	 * @return A {@link Deque} of strings containing the newly generated alias, pushed onto the deque.
	 */
	private static Deque<String> enrichRepeatableAggField(String field, QueryContext context, IElement element, String currentField, Deque<String> paths) {
		Map<String, String> aliases = context.getEnrichments().getRepeatableAggGroupAliases();
		String alias = resolveAlias(aliases, element.getName(), currentField);
		context.getEnrichments().addRepeatableAggField(field, new RepeatableAggField(alias, paths));
		Deque<String> resultPaths = new ArrayDeque<>();
		resultPaths.push(alias);
		return resultPaths;
	}

	/**
	 * Resolves the alias for `currentField`. If `elementName` is free or already points to
	 * `currentField`, returns it unchanged. Otherwise walks the alias map once: any existing
	 * `elementName_<n>` entry already mapped to `currentField` is reused; otherwise the highest
	 * matching `<n>` is incremented by one and used as the new suffix.
	 */
	private static String resolveAlias(Map<String, String> aliases, String elementName, String currentField) {
		String existing = aliases.get(elementName);
		// If there is no such alias created yet, or it already points to the current field, return the base alias.
		if (existing == null || existing.equals(currentField)) {
			return elementName;
		}
		String prefix = elementName + "_";
		int maxSuffix = 0;
		for (Map.Entry<String, String> entry : aliases.entrySet()) {
			String key = entry.getKey();
			if (!key.startsWith(prefix)) {
				continue;
			}
			Integer numericSuffix = parseNumericSuffix(key, prefix.length());
			if (numericSuffix == null) {
				continue;
			}
			if (entry.getValue().equals(currentField)) {
				return key;
			}
			maxSuffix = Math.max(maxSuffix, numericSuffix);
		}
		String alias = elementName + "_" + (maxSuffix + 1);
		aliases.put(alias, currentField);
		return alias;
	}

	/**
	 * Returns the integer formed by the digits in `value` starting at `from`, or `null` when the
	 * remaining substring is empty or contains any non-digit character. This is the regex-free
	 * equivalent of matching `^\d+$` against `value.substring(from)`.
	 */
	private static Integer parseNumericSuffix(String value, int from) {
		if (from >= value.length()) {
			return null;
		}
		try {
			return Integer.parseInt(value, from, value.length(), 10);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
