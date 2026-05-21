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
package com.mgmtp.a12.dataservices.query.generator.sql;

/**
 * Constants used in SQL query generation.
 */
public class QueryGeneratorConstants {
	public static final String AS_KEYWORD = " AS ";
	public static final String IS_OPERATOR = " IS ";
	public static final String IN_OPERATOR = " IN ";
	public static final String CAST_OPERATOR = " :: ";
	public static final String TEXT_TYPE = "TEXT";
	public static final String BOOLEAN_TYPE = "BOOLEAN";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String DECIMAL_TYPE = "DECIMAL";
	public static final String JSON_TYPE = "JSON";
	public static final String JSONB_TYPE = "JSONB";
	public static final String NUMERIC_TYPE = "NUMERIC";
	public static final String AND_OPERATOR = " AND ";
	public static final String OR_OPERATOR = " OR ";
	public static final String NULL_VALUE = "NULL";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String NULLIF = "NULLIF";
	public static final String EMPTY_VALUE = "''";
	public static final String EMPTY_STRING = "";
	public static final String SPACE = " ";
	public static final String NOT_OPERATOR = " NOT ";
	public static final String EXISTS_OPERATOR = "EXISTS ";
	public static final String SELECT_KEYWORD = "SELECT ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String ROW_KEYWORD = " ROW";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String CASE_KEYWORD = " CASE ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String WHEN_KEYWORD = "WHEN ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String THEN_KEYWORD = " THEN ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String END_KEYWORD = " END ";
	public static final String NULLS_FIRST_KEYWORD = " NULLS FIRST ";
	public static final String NULLS_LAST_KEYWORD = " NULLS LAST ";
	public static final String FROM_KEYWORD = " FROM ";
	public static final String WHERE_KEYWORD = " WHERE ";
	public static final String WITH_KEYWORD = "WITH ";
	public static final String RECURSIVE_KEYWORD = "RECURSIVE ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String UNION_KEYWORD = " UNION ";
	public static final String UNION_ALL_KEYWORD = " UNION ALL ";
	public static final String JOIN_KEYWORD = " JOIN ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String LEFT_KEYWORD = " LEFT ";
	public static final String ON_KEYWORD = " ON ";
	public static final String OFFSET_KEYWORD = " OFFSET ";
	public static final String LIMIT_KEYWORD = " LIMIT ";
	public static final String COUNT_ALL_OVER_OPERATOR = " COUNT(*) OVER()";
	public static final String GROUP_BY_KEYWORD = " GROUP BY ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String SPLIT_PART_KEYWORD = "SPLIT_PART";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String LIKE_REGEX_KEYWORD = " LIKE_REGEX ";
	public static final String REGEX_CASE_SENSITIVE_SEARCH_OPERATOR = " ~ ";
	public static final String REGEX_CASE_INSENSITIVE_SEARCH_OPERATOR = " ~* ";
	public static final String TIMESTAMP_TYPE = "timestamp";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSON_PATH_KEYWORD = "jsonpath";
	public static final String TS_RANGE_FUNCTION = "tsrange";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String TERNARY_FUNCTION = " ? ";
	public static final String ROW_NUMBER_FUNCTION = " ROW_NUMBER () OVER () ";
	public static final String LOWER_FUNCTION = " LOWER";
	public static final String ROUND_FUNCTION = " ROUND";
	public static final String COALESCE_FUNCTION = " COALESCE";
	public static final String ILIKE_OPERATOR = " ILIKE ";
	public static final String LESS_THAN_OPERATOR = " < ";
	public static final String LESS_THAN_OR_EQUAL_OPERATOR = " <= ";
	public static final String EQUALS_OPERATOR = " = ";
	public static final String GREATER_THAN_OR_EQUAL_OPERATOR = " >= ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String NOT_EQUALS_OPERATOR = " != ";
	public static final String FORWARD_SLASH = "/";
	public static final String DOT_JOINER = ".";
	public static final String OPENING_BRACKET = "(";
	public static final String CLOSING_BRACKET = ")";
	public static final String OPENING_SQUARE_BRACKET = "[";
	public static final String CLOSING_SQUARE_BRACKET = "]";
	public static final String CLOSED_RANGE = OPENING_SQUARE_BRACKET + CLOSING_SQUARE_BRACKET;
	public static final String OPENING_CURLED_BRACKET = "{";
	public static final String CLOSING_CURLED_BRACKET = "}";
	public static final String CONTAINS_OPERATOR = " @> ";
	public static final String IS_CONTAINED_OPERATOR = " <@ ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String NUMRANGE_TYPE = "NUMRANGE";
	public static final String TEXT_QUOTE = "'";
	public static final String TEXT_DOUBLE_QUOTE = "\"";
	public static final String ZERO_VALUE = "0";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String MINUS_ONE_VALUE = "-1";
	public static final String ONE_VALUE = "1";
	public static final String ASTERISK = "*";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String REGEX_BEGIN_WITH = "^";
	public static final String ORDER_BY_KEYWORD = " ORDER BY ";
	public static final String COMMA = ", ";
	public static final String COLON = ":";
	public static final String DESC_KEYWORD = " DESC ";
	public static final String CONCAT_OPERATOR = " || ";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String BYTEA_TYPE = "BYTEA";
	public static final String JSON_FIELD_TEXT_VALUE_SELECTION_OPERATOR = " ->> ";
	public static final String JSON_FIELD_PATH_OPERATOR = " -> ";
	public static final String JSONB_EXTRACT_OBJECT_OPERATOR = " #> ";
	public static final String JSONB_AGGREGATE_FUNCTION = "JSONB_AGG";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSON_OBJECT_AGGREGATE_FUNCTION = "JSON_OBJECT_AGG";
	public static final String JSONB_ARRAY_ELEMENTS_FUNCTION = "JSONB_ARRAY_ELEMENTS";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSONB_LEFT_CONTAINS_OPERATOR = " @> ";
	public static final String JSONB_EXTRACT_TEXT_FOR_PATH_OPERATOR = " #>> ";
	public static final String JSONB_BUILD_ARRAY_FUNCTION = "JSONB_BUILD_ARRAY";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSONB_BUILD_OBJECT_FUNCTION = "JSONB_BUILD_OBJECT";
	public static final String JSON_BUILD_ARRAY_FUNCTION = "JSON_BUILD_ARRAY";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSON_ROOT_KEYWORD = "$";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSON_FIELD_KEYWORD = "@";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSON_FIELD_TYPE_NAME = "fieldType";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public static final String JSONB_PATH_QUERY_FIRST_FUNCTION = "JSONB_PATH_QUERY_FIRST";
	public static final String FALSE_VALUE = "FALSE";
	public static final String TRUE_VALUE = "TRUE";
	public static final String COLLATE_CASE_SENSIITVE = " COLLATE \"C\" ";
	/**
	 * The delimiter used in {@link ColumnNames#SEARCH_DATA_COLUMN_NAME} to
	 * delimit the value from the field paths.
	 */
	public static final String SEARCH_DATA_VALUE_DELIMITER = "~";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	public interface Model {

		/**
		 * @deprecated Not used anymore, will be removed in the next breaking release.
		 */
		@Deprecated(since = "38.1.0", forRemoval = true)
		String PATH_ELEMENT_LABEL = "label";
	}

	public interface FieldTypes {
		String NUMBER_FIELD_TYPE = "INumberType";
		String ENUMERATION_FIELD_TYPE = "IEnumerationType";
		String DATE_FIELD_TYPE = "IDateType";
		String DATE_TIME_FIELD_TYPE = "IDateTimeType";
		String DATE_FRAGMENT_FIELD_TYPE = "IDateFragmentType";
		String DATE_RANGE_FIELD_TYPE = "IDateRangeType";
		String STRING_FIELD_TYPE = "IStringType";
		String CONFIRM_FIELD_TYPE = "IConfirmType";
		String BOOLEN_FIELD_TYPE = "IBooleanType";
	}

	public interface ColumnNames {
		String BACKREFERENCE_COLUMN_ALIAS = "back_reference";
		String INTERNAL_ID_COLUMN_ALIAS = "internal_id";
		String CONTENT_COLUMN_ALIAS = "content";
		String DEPTH_COLUMN_ALIAS = "depth";
		String DOC_REF_COLUMN_ALIAS = "doc_ref";
		String RESULT_DOC_REF_COLUMN_ALIAS = "result_doc_ref";
		String DOC_REF_COLUMN_NAME = "doc_ref";
		String FIELD_NAME_COLUMN_NAME = "field_name";
		String ID_COLUMN_NAME = "id";
		String JSON_COLUMN_ALIAS = "content";
		String ENUMERATION_ORIGINAL_VALUE_KEY = "original_value";
		String LINK_DOCUMENT_COLUMN_ALIAS = "link_document";
		String LINK_DOCUMENT_DOCREF_COLUMN_NAME = "link_document_docref";
		String LINK_ID_COLUMN_ALIAS = "link_id";
		String LINK_ID_COLUMN_NAME = "id";
		String MODEL_NAME_COLUMN_ALIAS = "model_name";
		String MODEL_NAME_COLUMN_NAME = "model_name";
		String RELATIONSHIP_ID_COLUMN_ALIAS = "relationship_id";
		String RELATIONSHIP_MODEL_COLUMN_ALIAS = "relationship_model";
		String RELATIONSHIP_MODEL_COLUMN_NAME = "relationship_model";
		String ROLE_DOCREF_COLUMN_NAME = "role_docref";
		String ROLE_NAME_COLUMN_NAME = "role_name";
		String SOURCE_DOCREF_COLUMN_ALIAS = "source_docref";
		String SOURCE_ROLE_COLUMN_ALIAS = "source_role";
		String TARGET_DOCREF_COLUMN_ALIAS = "target_docref";
		String TARGET_ROLE_COLUMN_ALIAS = "target_role";
		String TOTAL_COUNT_COLUMN_ALIAS = "total_count";
		String TYPE_COLUMN_ALIAS = "type";
		String FIELD_TYPE_COLUMN_NAME = "field_type";
		String FIELD_TYPE_ID_COLUMN_NAME = "field_type_id";
		String LOCALE_COLUMN_NAME = "locale";
		String VALUE_COLUMN_NAME = "value";
		String SEARCH_DATA_COLUMN_NAME = "search_data";
		String TYPED_VALUE_COLUMN_NAME = "typed_value";
		String NUMBER_VALUE_COLUMN_NAME = "number_value";
		String TIMESTAMP_VALUE_COLUMN_NAME = "timestamp_value";
		String TS_RANGE_VALUE_COLUMN_NAME = "ts_range_value";
		String SOURCE_COLUMN_NAME = "source";

		/**
		 * @deprecated Not used anymore, will be removed in the next breaking release.
		 */
		@Deprecated(since = "38.1.0", forRemoval = true)
		String VALUES_COLUMN_NAME = "values";
		String ORIGINAL_VALUE_COLUMN_NAME = "original_value";
		String LOCALIZED_VALUE_COLUMN_NAME = "localized_value";
		String DATA_COLUMN_NAME = "data";
		String REPETITIONS_COLUMN_NAME = "repetitions";

		/**
		 * @deprecated Not used anymore, will be removed in the next breaking release.
		 */
		@Deprecated(since = "38.1.0", forRemoval = true)
		String DOC_COUNT_COLUMN_ALIAS = "doc_count";
		String LINK_COUNT_COLUMN_ALIS = "link_count";

		/**
		 * @deprecated Not used anymore, will be removed in the next breaking release.
		 */
		@Deprecated(since = "38.1.0", forRemoval = true)
		String FULLTEXT_STRING_COLUMN_NAME = "fulltext_string";
		String LOCALIZED_FULLTEXT_STRING_COLUMN_NAME = "localized_fulltext_string";
		String ROW_NUM_COLUMN_ALIAS = "row_num";
		String FIELDS_PROJECTION = "fields_projection";
		String LINK_SOURCE_ORDER_ALIAS = "link_source_order";
		String LINK_TARGET_ORDER_ALIAS = "link_target_order";
		String ROLE_ORDER_COLUMN_NAME = "role_order";
		String INDEX_NAME_COLUMN_NAME = "index_name";
		String DESCRIPTION_COLUMN_NAME = "description";
		String SCHEMA_NAME_COLUMN_NAME = "schema_name";
	}

	public interface TableNames {
		String TARGET_DOCUMENT_TABLE_ALIAS = "target_document";
		String DOCUMENT_FIELDS_TABLE_NAME = "document_fields";
		String DOCUMENT_FIELDS_TABLE_ALIAS = "document_fields";
		String DOCUMENT_SEARCH_TABLE_NAME = "document_search";
		String LOCALIZED_FIELDS_TABLE_NAME = "localized_fields";
		String MODEL_FIELDS_TABLE_NAME = "model_fields";

		/**
		 * @deprecated Not used anymore, will be removed in the next breaking release.
		 */
		@Deprecated(since = "38.1.0", forRemoval = true)
		String MODEL_FIELDS_TABLE_ALIAS = "model_fields";
		String LINK_TABLE_ALIAS = "link";
		String TARGET_ROLE_TABLE_ALIAS = "target_role";
		String SOURCE_ROLE_TABLE_ALIAS = "source_role";
		String RELATIONSHIP_ROLE_TABLE_NAME = "relationship_role";
		String RELATIONSHIP_LINK_TABLE_NAME = "relationship_link";
		String ROOT_ALIAS = "roots";
		String CTE_ROOT_ALIAS = "cte_root";
		String RESULT_JOIN_TABLE_ALIAS = "result_doc_fields";
		String AGGREGATION_DOCUMENT = "aggregation_document";
		String LINK_ORDER_TABLE_NAME = "relationship_order";
		String LINK_SOURCE_ORDER_TABLE_ALIAS = "relationship_source_order";
		String LINK_TARGET_ORDER_TABLE_ALIAS = "relationship_target_order";
	}

	public interface NodeType {
		String ROOT_TYPE = "ROOT";
		String CHILD_TYPE = "CHILD";
		String LINK_TYPE = "LINK";
	}
}
