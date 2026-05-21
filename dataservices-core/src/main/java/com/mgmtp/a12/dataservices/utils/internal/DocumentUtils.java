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
package com.mgmtp.a12.dataservices.utils.internal;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.IDocumentIdGenerator;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.internal.TimeFormatUtils;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDmAwareDocService;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IBooleanType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IConfirmType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ICustomFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateTimeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITimeType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCREF_METADATA_PATH;

/**
 * Internal tools to automatize common tasks with documents.
 */
@RequiredArgsConstructor @Component public class DocumentUtils {

	public static final String ERROR_WHILE_CONVERTING_FIELD_INSTANCE_TO_JAVA_TYPE =
		"Error while converting field instance to java type";
	private final List<IDocumentIdGenerator> documentIdGenerators;
	private final IDocumentModelResolver documentModelResolver;

	/**
	 * Gets value from unique field determined just by path. No repetitions are allowed.
	 *
	 * @param document document to get the instance from.
	 * @param path path determining the field instance.
	 * @param <T> Type of the value to get. Currently kernel supports just String, Date, BigDecimal and Boolean.
	 * @return value of the field.
	 */
	public static <T> Optional<T> findSingleValue(DocumentV2 document, String path) {
		return Optional.ofNullable(document.fieldValue(path)).map(v -> (T) v);
	}

	/**
	 * @param path path determining the field instance.
	 * @param repetitions position of field in case of repeating groups.
	 * @param value updated value.
	 * @return UpdateAction function.
	 */
	public static UpdateAction createFieldUpdateAction(String path, int[] repetitions, Object value) {
		DocumentPointer documentPointer = KernelUtils.fromPathAndRepetitions(path, repetitions);
		return UpdateAction.putFieldValue(documentPointer, value);
	}

	/**
	 * Transform to documentV2 value
	 *
	 * @param modelName model name is used to find model to get field type
	 * @param path path determining the field instance.
	 * @param value value to set to field.
	 * @return DocumentV2 value
	 */
	public Object transformToV2Value(String modelName, String path, Object value) {
		ListIProblemReporter pr = new ListIProblemReporter();
		IDmAwareDocService dmAwareDocService = DmAwareDocServiceImpl.ofResolver(modelName, documentModelResolver);
		Object typedValue =  dmAwareDocService.convertToJavaTypeV2(path, value.toString(), pr);
		pr.validate(ExceptionCodes.FIELD_INSTANCE_TO_JAVA_TYPE_EXCEPTION_CODE,
			ExceptionKeys.FIELD_INSTANCE_TO_JAVA_TYPE_ERROR_KEY, ERROR_WHILE_CONVERTING_FIELD_INSTANCE_TO_JAVA_TYPE);
		return typedValue;
	}

	/**
	 * Format value stores in fieldInstance by type.
	 *
	 * @param type The type of the field to format by.
	 * @param value The value to format.
	 * @param timeZone The time zone to be used for date formatting.
	 * @param <T> Java type of the object to format.
	 * @return value formatted as String by type of the field.
	 */
	public static <T> String format(IFieldType type, T value, TimeZone timeZone) {
		if (value == null) {
			return null;
		}
		Optional<SupportedDataTypes> optionalType = SupportedDataTypes.fromFieldType(type);
		return optionalType.map(t -> switch (t) {
			case TIME, DATE, DATETIME -> {
				if (value instanceof String stringValue) {
					yield stringValue;
				}
				DateTimeFormatter f = DateTimeFormatter.ofPattern(TimeFormatUtils.getDateTimeFormat(type));
				yield TimeFormatUtils.format(f, TimeFormatUtils.getTemporalAccessor(value, timeZone));
			}
			default -> value.toString();
		}).orElse(value.toString());
	}

	public @NonNull DocumentReference generateDocRef(@NonNull DocumentV2 document) {
		return new DocumentReference(
			document.getDocumentModelId(),
			documentIdGenerators.stream()
				.map(documentIdGenerator -> documentIdGenerator.generateId(document))
				.flatMap(Optional::stream)
				.findFirst()
				.orElseThrow(() -> new NotFoundException("Couldn't generate document id as no matching generator for: " +
					document.getDocumentModelId() + " was found"))
		);
	}

	/**
	 * Uses `QueryRoot.builder` to construct the query that loads a document by its docRef.
	 *
	 * @param documentReference The `DocumentReference`
	 * @return The query which can be used in `QueryService`
	 */
	public static QueryRoot buildQueryLoadDocumentByDocRef(DocumentReference documentReference) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(documentReference.getDocumentModelName())
			.constraint(ExactMatchOperator.builder()
				.field(DOCREF_METADATA_PATH)
				.value(documentReference.toString())
				.build()
			)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(1)
				.build())
			.build();
	}

	/**
	 * Uses `QueryRoot.builder` to construct the query that loads the docRef of a document by its docRef.
	 * This may be used to prove that the document exists, and that the current user has access to it.
	 *
	 * @param documentReference The `DocumentReference`
	 * @return The query which can be used in `QueryService`
	 */
	public static QueryRoot buildQueryLoadDocRefOnly(DocumentReference documentReference) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(documentReference.getDocumentModelName())
			.field(DOCREF_METADATA_PATH)
			.constraint(ExactMatchOperator.builder()
				.field(DOCREF_METADATA_PATH)
				.value(documentReference.toString())
				.build()
			)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(1)
				.build())
			.build();
	}

	public enum SupportedDataTypes {
		TEXT(IStringType.class),
		BOOLEAN(IBooleanType.class, IConfirmType.class),
		ENUM(IEnumerationType.class),
		LOCALIZED_ENUM(IEnumerationType.class),
		NUMBER(INumberType.class),
		DATETIME(IDateTimeType.class),
		TIME(ITimeType.class),
		DATE(IDateType.class, IDateFragmentType.class),
		DATE_RANGE(IDateRangeType.class),
		CUSTOM_FIELD_TYPE(ICustomFieldType.class);
		private final Class<? extends IFieldType>[] acceptedTypes;

		SupportedDataTypes(Class<? extends IFieldType>... type) {
			acceptedTypes = type;
		}

		public static Optional<SupportedDataTypes> fromFieldType(IFieldType effectiveDataType) {
			return Arrays.stream(values())
				.filter(a -> a.matches(effectiveDataType))
				.findAny();
		}

		public boolean matches(IFieldType effectiveDataType) {
			return effectiveDataType != null && Arrays.stream(acceptedTypes)
				.anyMatch(t -> t.isAssignableFrom(effectiveDataType.getClass()));
		}
	}
}
