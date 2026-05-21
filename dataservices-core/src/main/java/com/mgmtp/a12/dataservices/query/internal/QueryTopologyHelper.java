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

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryNotFoundException;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IBooleanType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IConfirmType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ICustomFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITypeDefType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IUnspecifiedType;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class QueryTopologyHelper {
	private static final List<Class<? extends IFieldType>> KNOWN_TYPES = List.of(
		IUnspecifiedType.class,
		IEnumerationType.class,
		IDateRangeType.class,
		ITypeDefType.class,
		IDateType.class,
		IDateFragmentType.class,
		ICustomFieldType.class,
		IStringType.class,
		INumberType.class,
		IBooleanType.class,
		IConfirmType.class
	);
	private static final List<String> REGISTERED_AGGREGATION_FUNCTIONS = List.of(
		"min", "max", "sum", "avg", "count"
	);

	/**
	 * This method determines the type of field based on its class:
	 * .For {@link ICustomFieldType}, it returns the name of the custom type.
	 * .For {@link ITypeDefType}, it returns the ID of the type definition.
	 * .For other types, it attempts to find a match in {@link #KNOWN_TYPES} and returns
	 * the simple name of the matching class. If no match is found in {@link #KNOWN_TYPES}, it returns the simple name of the field type's class.
	 *
	 * @param fieldType The object to find type.
	 * @return A non-null string representation of the field type.
	 */
	public static @NonNull String fieldTypeAsString(Object fieldType) {
		Class<?> fieldTypeClass = fieldType.getClass();
		if (fieldType instanceof ICustomFieldType customType) {
			return customType.getName();
		} else if (fieldType instanceof ITypeDefType typeDef) {
			return typeDef.getId();
		} else {
			return KNOWN_TYPES.stream()
				.filter(t -> t.isAssignableFrom(fieldTypeClass))
				.map(Class::getSimpleName)
				.findAny()
				.orElse(fieldTypeClass.getSimpleName());
		}
	}

	/**
	 * Returns the effective field type. It can be used e.g., if the type is defined in a separate type definition.
	 *
	 * @param field The field for which the effective type should be looked up.
	 * @param executionPhase The phase of query execution that should be used if an exception is thrown.
	 * @return The effective type of the field.
	 */
	public static IFieldType getEffectiveFieldType(IField field, ExceptionKeys.ExecutionPhase executionPhase) {
		return field.getEffectiveType().orElseThrow(() -> new QueryNotFoundException(
			executionPhase, MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY, "Effective type code not be found for field [%s]".formatted(field))
			.withAnonymityMessage("Effective type code could not be found."));
	}

	public static boolean isAggregationFunctionRegistered(String function) {
		return REGISTERED_AGGREGATION_FUNCTIONS.contains(function);
	}
}
