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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.utils.internal.DateFieldFormatter;
import com.mgmtp.a12.dataservices.utils.internal.DateFragmentFieldFormatter;
import com.mgmtp.a12.dataservices.utils.internal.DateRangeFieldFormatter;
import com.mgmtp.a12.dataservices.utils.internal.DefaultFieldFormatter;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.FieldFormatter;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import io.hypersistence.utils.hibernate.type.range.Range;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.node.ObjectNode;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_INDEXING;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentSearchIndexHelper {

	/**
	 * Return the proper FieldFormatter depending on the IFieldType.
	 *
	 * @param fieldType The IFieldType for which the formatter is requested.
	 * @return The proper FieldFormatter for the passed IFieldType.
	 */
	public static FieldFormatter getFieldFormatter(IFieldType fieldType) {
		return switch (fieldType) {
			case IDateType dateType -> new DateFieldFormatter(dateType);
			case IDateRangeType ignored -> new DateRangeFieldFormatter();
			case IDateFragmentType ignored -> new DateFragmentFieldFormatter();
			case null, default -> new DefaultFieldFormatter();
		};
	}

	public static List<String> getColumnNames(String target) {
		return target.equals(DocumentSearchIndexBehaviour.DOCUMENTS_TARGET) ?
			List.of(
				QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.ORIGINAL_VALUE_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.VALUE_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.SEARCH_DATA_COLUMN_NAME)
			:
			List.of(
				QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.FIELD_NAME_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.REPETITIONS_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.FIELD_TYPE_ID_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.FIELD_TYPE_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.VALUE_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.TYPED_VALUE_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.NUMBER_VALUE_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.TIMESTAMP_VALUE_COLUMN_NAME,
				QueryGeneratorConstants.ColumnNames.TS_RANGE_VALUE_COLUMN_NAME, QueryGeneratorConstants.ColumnNames.SOURCE_COLUMN_NAME);
	}

	public static boolean isRepeatableGroup(IDocumentModelSearchService documentModelSearchService, String currentPath) {
		return documentModelSearchService.getByPath(currentPath)
			.filter(IGroup.class::isInstance)
			.map(IGroup.class::cast)
			.map(IGroup::getRepeatability)
			.filter(r -> r > 1)
			.isPresent();
	}

	public @NotNull static Optional<IFieldType> getIFieldTypeIfRangeAware(IDocumentModelSearchService documentModelSearchService, String path) {
		return DocumentModelUtils.findField(documentModelSearchService, path)
			.map(field -> getEffectiveFieldType(field, QUERY_INDEXING))
			.filter(DocumentSearchIndexHelper::isRangeAwareFieldType);
	}

	public static Range<LocalDateTime> getRangeValue(String typedValue, IFieldType fieldType) {
		if (fieldType instanceof IDateRangeType && StringUtils.isNotBlank(typedValue)) {
			LocalDateTime from = LocalDateTime.parse(typedValue.split("/")[0]);
			LocalDateTime to = LocalDateTime.parse(typedValue.split("/")[1]);
			return Range.closed(from, to);
		}
		return null;
	}

	public static LocalDateTime getDateTimeValue(String typedValue, IFieldType fieldType) {
		if ((fieldType instanceof IDateType || fieldType instanceof IDateFragmentType) && StringUtils.isNotBlank(typedValue)) {
			if (typedValue.length() == 10) {
				return LocalDate.parse(typedValue).atTime(0, 0, 0);
			}
			return LocalDateTime.parse(typedValue);
		}
		return null;
	}

	public static BigDecimal getNumberValue(String value, IFieldType fieldType) {
		if (fieldType instanceof INumberType && StringUtils.isNotBlank(value)) {
			return new BigDecimal(value);
		}
		return null;
	}

	public static String prepareSearchableJsonOfEnums(IDocumentModelSearchService documentModelSearchService, DocumentV2 indexableDocument) {
		ObjectNode rootNode = DocumentSearchIndexBehaviour.OBJECT_MAPPER.createObjectNode();
		indexableDocument.traverse(new IDocumentV2Visitor() {
			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field);
				findEnumValue(field, documentModelSearchService, pointerRelativeToBase)
					.ifPresent(enumValue -> addEnumField(DocumentSearchIndexRecursionHelper.prepareParentNode(
						documentModelSearchService, rootNode, pointerRelativeToBase), enumValue));
			}
		});
		return DocumentSearchIndexBehaviour.OBJECT_MAPPER.writeValueAsString(rootNode);
	}

	private static boolean isRangeAwareFieldType(IFieldType fieldType) {
		return fieldType instanceof IDateType
			|| fieldType instanceof INumberType
			|| fieldType instanceof IDateRangeType
			|| fieldType instanceof IDateFragmentType;
	}

	@NotNull private static Optional<IEnumerationType.IEnumValue> findEnumValue(FieldInstanceV2 fieldInstance,
		IDocumentModelSearchService documentModelSearchService, DocumentPointer pointerRelativeToBase) {
		Object value = fieldInstance.value();
		return DocumentModelUtils.getFieldType(documentModelSearchService, pointerRelativeToBase.fullName())
			.flatMap(ft -> ft instanceof IEnumerationType et
				? et.getValues().stream()
				.filter(ev -> ev.getValue().equals(String.valueOf(value)))
				.findAny()
				: Optional.empty());
	}

	private static void addEnumField(ObjectNode parent, IEnumerationType.IEnumValue enumValue) {
		enumValue.getLabel().forEach((key, value) -> parent.put(key.toString(), value));
		parent.put(QueryGeneratorConstants.ColumnNames.ENUMERATION_ORIGINAL_VALUE_KEY, enumValue.getValue());
	}
}
