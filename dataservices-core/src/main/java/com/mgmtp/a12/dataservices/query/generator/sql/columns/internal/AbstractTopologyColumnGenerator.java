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
package com.mgmtp.a12.dataservices.query.generator.sql.columns.internal;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
public abstract class AbstractTopologyColumnGenerator extends SelectColumns implements GeneratedColumns {

	private final QueryTopology query;
	private boolean initialized;

	private void init(QueryGeneratorContext generatorContext) {
		if (initialized) {
			return;
		}

		column(QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_ALIAS, getDocRefValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_ALIAS, getModelNameValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.CONTENT_COLUMN_ALIAS, getContentValue(generatorContext), QueryGeneratorConstants.JSONB_TYPE);
		column(QueryGeneratorConstants.ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS, getRelationshipModelValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.SOURCE_ROLE_COLUMN_ALIAS, getSourceRoleValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS, getSourceDocRefValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.TARGET_ROLE_COLUMN_ALIAS, getTargetRoleValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.TARGET_DOCREF_COLUMN_ALIAS, getTargetDocRefValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS, getLinkDocumentValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.LINK_ID_COLUMN_ALIAS, getLinkIdValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.TYPE_COLUMN_ALIAS, getTypeValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.BACKREFERENCE_COLUMN_ALIAS, getBackReferenceValue(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.INTERNAL_ID_COLUMN_ALIAS, getInternalId(), QueryGeneratorConstants.TEXT_TYPE);
		column(QueryGeneratorConstants.ColumnNames.FIELDS_PROJECTION, getFieldsProjection(), QueryGeneratorConstants.BOOLEAN_TYPE);
		column(QueryGeneratorConstants.ColumnNames.DEPTH_COLUMN_ALIAS, getDepthValue(), QueryGeneratorConstants.NUMERIC_TYPE);
		column(QueryGeneratorConstants.ColumnNames.ROW_NUM_COLUMN_ALIAS, QueryGeneratorConstants.ROW_NUMBER_FUNCTION, QueryGeneratorConstants.EMPTY_STRING);
		column(QueryGeneratorConstants.ColumnNames.TOTAL_COUNT_COLUMN_ALIAS, getCountValue(), QueryGeneratorConstants.NUMERIC_TYPE);
		initialized = true;
	}

	@Override public StringBuilder renderColumns(StringBuilder sb, QueryGeneratorContext generatorContext) {
		init(generatorContext);
		return super.renderColumns(sb, generatorContext);
	}

	@Override public StringBuilder renderTables(StringBuilder sb, QueryGeneratorContext generatorContext) {
		init(generatorContext);
		return super.renderTables(sb, generatorContext);
	}

	@Override public CharSequence getContentValue(QueryGeneratorContext generatorContext) {
		CharSequence charContent =
			QueryGeneratorConstants.OPENING_BRACKET + QueryGeneratorConstants.JSONB_AGGREGATE_FUNCTION + QueryGeneratorConstants.OPENING_BRACKET
				+ QueryGeneratorConstants.JSON_BUILD_ARRAY_FUNCTION + QueryGeneratorConstants.OPENING_BRACKET
				+ QueryGeneratorConstants.ColumnNames.FIELD_NAME_COLUMN_NAME
				+ QueryGeneratorConstants.COMMA + QueryGeneratorConstants.ColumnNames.REPETITIONS_COLUMN_NAME + QueryGeneratorConstants.COMMA
				+ QueryGeneratorConstants.ColumnNames.ORIGINAL_VALUE_COLUMN_NAME + QueryGeneratorConstants.CLOSING_BRACKET
				+ QueryGeneratorConstants.CLOSING_BRACKET;
		/*
		Also append `/__meta/doc_ref` and `/__meta/model_name` from `doc_ref` and `model_name` columns to content if needed in projection fields.
		|| JSONB_BUILD_ARRAY(JSON_BUILD_ARRAY('/__meta/docRef',JSON_BUILD_ARRAY(1,1),doc_ref))
		|| JSONB_BUILD_ARRAY(JSON_BUILD_ARRAY('/__meta/modelReference',JSON_BUILD_ARRAY(1,1),model_name))
		 */
		if (CollectionUtils.isNotEmpty(getQuery().getFields()) && getQuery().getFields().contains(DocumentMetadataConstants.DOCREF_METADATA_PATH)) {
			charContent = appendMetaFieldProjection(charContent, DocumentMetadataConstants.DOCREF_METADATA_PATH,
				QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_ALIAS, getQueryGeneratorContext());
		}
		if (CollectionUtils.isNotEmpty(getQuery().getFields()) && getQuery().getFields().contains(DocumentMetadataConstants.MODEL_REFERENCE_PATH)) {
			charContent = appendMetaFieldProjection(charContent, DocumentMetadataConstants.MODEL_REFERENCE_PATH,
				QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_ALIAS, getQueryGeneratorContext());
		}
		charContent += QueryGeneratorConstants.CLOSING_BRACKET + QueryGeneratorConstants.CAST_OPERATOR + QueryGeneratorConstants.JSON_TYPE;
		return charContent;
	}

	private static CharSequence appendMetaFieldProjection(CharSequence charContent, String metaPath, String metaColumn, QueryGeneratorContext generatorContext) {
		return charContent + (QueryGeneratorConstants.CONCAT_OPERATOR + QueryGeneratorConstants.JSONB_BUILD_ARRAY_FUNCTION
			+ QueryGeneratorConstants.OPENING_BRACKET + QueryGeneratorConstants.JSON_BUILD_ARRAY_FUNCTION + QueryGeneratorConstants.OPENING_BRACKET + SqlGeneratorHelpers.addParam(
			metaPath, generatorContext) + QueryGeneratorConstants.COMMA + QueryGeneratorConstants.JSON_BUILD_ARRAY_FUNCTION + QueryGeneratorConstants.OPENING_BRACKET
			+ DocumentMetadataConstants.META_REPETITION_VALUE + QueryGeneratorConstants.CLOSING_BRACKET + QueryGeneratorConstants.COMMA + metaColumn
			+ QueryGeneratorConstants.CLOSING_BRACKET + QueryGeneratorConstants.CLOSING_BRACKET);
	}

	@Override public CharSequence getBackReferenceValue() {
		if (getQuery().getBackReference() == null) {
			return QueryGeneratorConstants.EMPTY_VALUE;
		} else {
			CharSequence sb = query.getBackReference();
			return SqlGeneratorHelpers.addParam(sb, getQueryGeneratorContext());
		}
	}

	@Override public CharSequence getInternalId() {
		return SqlGeneratorHelpers.addParam(query.getInternalId().toString(), getQueryGeneratorContext());
	}

	@Override public CharSequence getLinkIdValue() {
		return QueryGeneratorConstants.EMPTY_VALUE;
	}

	@Override public CharSequence getLinkDocumentValue() {
		return QueryGeneratorConstants.NULL_VALUE;
	}

	@Override public CharSequence getTargetDocRefValue() {
		return QueryGeneratorConstants.EMPTY_VALUE;
	}

	@Override public CharSequence getTargetRoleValue() {
		return QueryGeneratorConstants.EMPTY_VALUE;
	}

	@Override public CharSequence getSourceDocRefValue() {
		return QueryGeneratorConstants.EMPTY_VALUE;
	}

	@Override public CharSequence getSourceRoleValue() {
		return QueryGeneratorConstants.EMPTY_VALUE;
	}

	@Override public CharSequence getRelationshipModelValue() {
		return QueryGeneratorConstants.EMPTY_VALUE;
	}

	@Override public CharSequence getCountValue() {
		return QueryGeneratorConstants.NULL_VALUE;
	}

	@Override public CharSequence getModelNameValue() {
		return QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_ALIAS;
	}

	@Override public CharSequence getDocRefValue() {
		return QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_ALIAS;
	}

	@Override public CharSequence getDepthValue() {
		return QueryGeneratorConstants.ZERO_VALUE;
	}

	public abstract static class AbstractTopologyColumnGeneratorBuilder<C extends AbstractTopologyColumnGenerator, B extends AbstractTopologyColumnGeneratorBuilder<C, B>>
		extends SelectColumnsBuilder<C, B> {
	}

	@Override public CharSequence getFieldsProjection() {
		return CollectionUtils.isEmpty(getQuery().getFields()) ?
			QueryGeneratorConstants.FALSE_VALUE : QueryGeneratorConstants.TRUE_VALUE;
	}
}
