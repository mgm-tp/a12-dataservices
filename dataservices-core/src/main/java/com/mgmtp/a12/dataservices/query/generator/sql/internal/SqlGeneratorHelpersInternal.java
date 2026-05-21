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
package com.mgmtp.a12.dataservices.query.generator.sql.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.fields.ProjectionField;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.DocumentTreeEntity;
import com.mgmtp.a12.dataservices.query.internal.aggregation.RepeatableAggField;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.EQUALS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.JSON_FIELD_PATH_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.JSON_FIELD_TEXT_VALUE_SELECTION_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TEXT_QUOTE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.AGGREGATION_DOCUMENT;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.TARGET_DOCUMENT_TABLE_ALIAS;

@Slf4j @NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlGeneratorHelpersInternal {

	/**
	 * Renders a search table constraint for the given logic operator and appends it to the provided StringBuilder.
	 * Handles aggregation and connection to previous constraints as needed.
	 *
	 * Example SQL output (without aggregation, connectToPrevious=false):
	 * [source,sql]
	 * ----
	 * (value #>> '{field,path}' = :p1)
	 * ----
	 *
	 * Example SQL output (with aggregation, connectToPrevious=true):
	 * [source,sql]
	 * ----
	 * AND target_doc.doc_ref = agg_doc.doc_ref AND (value #>> '{field,path}' = :p1)
	 * ----
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param operator The logic operator representing the constraint.
	 * @param queryGeneratorContext The context for query generation.
	 * @param connectToPrevious Whether to connect this constraint to the previous one with an AND operator.
	 * @param <T> The type of logic operator.
	 */
	public static <T extends ILogicOperator> void renderSearchTableConstraint(StringBuilder sb, T operator, QueryGeneratorContext queryGeneratorContext,
		boolean connectToPrevious) {

		if (queryGeneratorContext.isAggregated()) {
			if (connectToPrevious) {
				sb.append(QueryGeneratorConstants.AND_OPERATOR);
			}
			appendAggregationTableJoinCondition(sb);
		}
		if (operator == null) {
			return;
		}
		if (connectToPrevious || queryGeneratorContext.isAggregated()) {
			sb.append(QueryGeneratorConstants.AND_OPERATOR);
		}

		ILogicOperatorGenerator<T> operandSqlGenerator = queryGeneratorContext.getConstraintGenerator((Class<T>) operator.getClass());
		sb.append(OPENING_BRACKET);
		operandSqlGenerator.renderCondition(sb, operator, queryGeneratorContext);
		sb.append(CLOSING_BRACKET);
	}

	/**
	 * Renders a constraint for the given logic operator and appends it to the provided StringBuilder.
	 *
	 * Example SQL output (connectToPrevious=false):
	 * [source,sql]
	 * ----
	 * (value #>> '{field,path}' = :p1)
	 * ----
	 *
	 * Example SQL output (connectToPrevious=true):
	 * [source,sql]
	 * ----
	 * AND (value #>> '{field,path}' = :p1)
	 * ----
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param operator The logic operator representing the constraint.
	 * @param queryGeneratorContext The context for query generation.
	 * @param connectToPrevious Whether to connect this constraint to the previous one with an AND operator.
	 * @param <T> The type of logic operator.
	 */
	public static <T extends ILogicOperator> void renderConstraint(StringBuilder sb, T operator, QueryGeneratorContext queryGeneratorContext,
		boolean connectToPrevious) {

		if (operator == null) {
			return;
		}
		if (connectToPrevious) {
			sb.append(QueryGeneratorConstants.AND_OPERATOR);
		}

		ILogicOperatorGenerator<T> operandSqlGenerator = queryGeneratorContext.getConstraintGenerator((Class<T>) operator.getClass());
		sb.append(OPENING_BRACKET);
		operandSqlGenerator.renderCondition(sb, operator, queryGeneratorContext);
		sb.append(CLOSING_BRACKET);
	}

	/**
	 * Renders an EXISTS constraint for the given logic operator and appends it to the provided StringBuilder.
	 *
	 * Example SQL output (with parentDocref, connectToPrevious=false):
	 * [source,sql]
	 * ----
	 * EXISTS (
	 *   SELECT 1
	 *   FROM schema.document_table AS doc_1
	 *   WHERE target_doc.doc_ref = doc_1.doc_ref
	 *   AND (value #>> '{field,path}' = :p1)
	 * )
	 * ----
	 *
	 * Example SQL output (without parentDocref, connectToPrevious=true):
	 * [source,sql]
	 * ----
	 * AND EXISTS (
	 *   SELECT 1
	 *   FROM schema.document_table AS doc_1
	 *   WHERE (value #>> '{field,path}' = :p1)
	 * )
	 * ----
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param operator The logic operator representing the constraint.
	 * @param parentDocref The parent document reference, may be null.
	 * @param queryGeneratorContext The context for query generation.
	 * @param connectToPrevious Whether to connect this constraint to the previous one with an AND operator.
	 * @param <T> The type of logic operator.
	 */
	public static <T extends ILogicOperator> void renderExistsConstraint(StringBuilder sb, T operator, CharSequence parentDocref,
		QueryGeneratorContext queryGeneratorContext, boolean connectToPrevious) {
		renderExistsConstraint(sb, operator, parentDocref, queryGeneratorContext, connectToPrevious, false);
	}

	private static void appendAggregationTableJoinCondition(StringBuilder sb) {
		sb.append(TARGET_DOCUMENT_TABLE_ALIAS).append(DOT_JOINER).append(DOC_REF_COLUMN_ALIAS)
			.append(EQUALS_OPERATOR)
			.append(AGGREGATION_DOCUMENT).append(DOT_JOINER).append(DOC_REF_COLUMN_ALIAS);
	}

	private static <T extends ILogicOperator> void renderExistsConstraint(StringBuilder sb, T operator, CharSequence parentDocref,
		QueryGeneratorContext queryGeneratorContext, boolean connectToPrevious, boolean negation) {
		if (operator == null) {
			return;
		}
		if (connectToPrevious) {
			sb.append(QueryGeneratorConstants.AND_OPERATOR);
		}

		sb
			.append(negation ? QueryGeneratorConstants.NOT_OPERATOR + OPENING_BRACKET : QueryGeneratorConstants.EMPTY_STRING)
			.append(QueryGeneratorConstants.EXISTS_OPERATOR)
			.append(OPENING_BRACKET);

		queryGeneratorContext.registerNewDocumentTableAlias();

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD)
			.append(QueryGeneratorConstants.ONE_VALUE)
			.append(QueryGeneratorConstants.FROM_KEYWORD);
		renderSchema(sb, queryGeneratorContext.getSchema());
		sb.append(queryGeneratorContext.getDocumentTableName())
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(queryGeneratorContext.getCurrentDocumentTableAlias());

		sb.append(QueryGeneratorConstants.WHERE_KEYWORD);

		if (parentDocref != null) {
			sb.append(parentDocref)
				.append(QueryGeneratorConstants.EQUALS_OPERATOR);

			sb.append(queryGeneratorContext.getCurrentDocumentTableAlias())
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.DOC_REF_COLUMN_NAME);

		}

		renderSearchTableConstraint(sb, operator, queryGeneratorContext, parentDocref != null);

		queryGeneratorContext.unregisterDocumentTableAlias();

		sb.append(CLOSING_BRACKET)
			.append(negation ? CLOSING_BRACKET : QueryGeneratorConstants.EMPTY_STRING);
	}

	/**
	 * Appends the necessary SQL conditions for linking roles and relationships in a query.
	 *
	 * Example SQL output:
	 * [source,sql]
	 * ----
	 * AND link.relationship_model = :p1
	 * AND src_role.relationship_id = link.id
	 * AND link.id = tgt_role.relationship_id
	 * AND src_role.role_name = :p2
	 * AND tgt_role.role_name = :p3
	 * AND link_tgt_ord.id = tgt_role.id
	 * AND link_src_ord.id = src_role.id
	 * ----
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param query The LinkAware query object containing relationship and role information.
	 * @param generatorContext The context for query generation.
	 */
	public static void linkWhere(StringBuilder sb, LinkAware query, QueryGeneratorContext generatorContext) {
		String input = query.getTargetRole();
		String input1 = generatorContext.getEnrichments().getSourceRole(query);
		String input2 = query.getRelationshipModel();
		sb.append(QueryGeneratorConstants.AND_OPERATOR)
			.append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.RELATIONSHIP_MODEL_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(SqlGeneratorHelpers.addParam(input2, generatorContext))

			.append(QueryGeneratorConstants.AND_OPERATOR)

			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.RELATIONSHIP_ID_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.LINK_ID_COLUMN_NAME)

			.append(QueryGeneratorConstants.AND_OPERATOR)

			.append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.LINK_ID_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(TableNames.TARGET_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.RELATIONSHIP_ID_COLUMN_ALIAS)

			.append(QueryGeneratorConstants.AND_OPERATOR)

			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_NAME_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(SqlGeneratorHelpers.addParam(input1, generatorContext))

			.append(QueryGeneratorConstants.AND_OPERATOR)

			.append(TableNames.TARGET_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_NAME_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(SqlGeneratorHelpers.addParam(input, generatorContext))
			.append(QueryGeneratorConstants.AND_OPERATOR)

			.append(TableNames.LINK_TARGET_ORDER_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ID_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(TableNames.TARGET_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ID_COLUMN_NAME)

			.append(QueryGeneratorConstants.AND_OPERATOR)

			.append(TableNames.LINK_SOURCE_ORDER_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ID_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ID_COLUMN_NAME);
	}

	/**
	 * Appends a WHERE clause for filtering by model names using an IN operator.
	 *
	 * Example SQL output:
	 * [source,sql]
	 * ----
	 * target_doc.model_name IN ('ModelA', 'ModelB', 'ModelC')
	 * ----
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param targetDocumentTableAlias The alias of the target document table.
	 * @param stream A stream of model names to include in the filter.
	 */
	public static void modelWhere(StringBuilder sb, String targetDocumentTableAlias, Stream<String> stream) {
		sb.append(targetDocumentTableAlias).append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.MODEL_NAME_COLUMN_NAME)
			.append(QueryGeneratorConstants.IN_OPERATOR).append(OPENING_BRACKET)
			.append(stream
				.map("'%s'"::formatted)
				.collect(Collectors.joining(QueryGeneratorConstants.COMMA)))
			.append(CLOSING_BRACKET);
	}

	/**
	 * Appends the schema name to the StringBuilder if it is not blank, followed by a dot.
	 *
	 * Example SQL output (with schema):
	 * [source,sql]
	 * ----
	 * my_schema.
	 * ----
	 *
	 * Example SQL output (without schema):
	 * [source,sql]
	 * ----
	 * (empty string)
	 * ----
	 *
	 * @param sb The StringBuilder to append the schema to.
	 * @param schema The schema name to append.
	 * @return The same StringBuilder instance for chaining.
	 */
	public static StringBuilder renderSchema(StringBuilder sb, CharSequence schema) {
		if (StringUtils.isNotBlank(schema)) {
			sb.append(schema).append(QueryGeneratorConstants.DOT_JOINER);
		}
		return sb;
	}

	/**
	 * Processes an SQL query string by replacing parameter placeholders with actual values,
	 * handling escape sequences, and ensuring proper formatting for Hibernate.
	 *
	 * Example SQL input:
	 * [source,sql]
	 * ----
	 * SELECT * FROM table WHERE name = :p1 AND age > :p2
	 * ----
	 *
	 * Example SQL output (with p1='John', p2='25'):
	 * [source,sql]
	 * ----
	 * SELECT * FROM table WHERE name = 'John' AND age > '25'
	 * ----
	 *
	 * @param sql The SQL query as a `CharSequence` that may contain parameter placeholders (e.g., ":p1").
	 * @param queryGeneratorContext The `QueryGeneratorContext` containing the parameter values and context
	 * for replacing placeholders in the query.
	 * @return A `String` representing the processed SQL query with parameter values substituted and
	 * necessary escape sequences handled appropriately.
	 */
	public static String getQueryWithValues(CharSequence sql, QueryGeneratorContext queryGeneratorContext) {
		return Pattern.compile(":(p\\d+)").matcher(sql)
			.replaceAll(m -> replaceMatchPattern(queryGeneratorContext, m))
			.replace("\\\\", "") // remove backslash escapes.
			.replace("\\?\\?", "?"); // replace escapes question-mark by real one.
		// For hibernate, question-mark must be escaped by doubling it, otherwise it's considered to be a placeholder.
		// also it must be escaped by additional backslashes.
	}

	private static @NonNull String replaceMatchPattern(QueryGeneratorContext queryGeneratorContext, MatchResult m) {
		log.trace("Group: {}", m);
		String replacement = Optional.of(m)
			.map(matchResult -> matchResult.group(1))
			.map(queryGeneratorContext.getParamHolder()::get)
			.map("'%s'"::formatted)
			.map(s -> s.replace("\\$", "\\\\\\$"))
			.orElse(QueryGeneratorConstants.NULL_VALUE);
		log.trace("Replacement: {}", replacement);
		return replacement;
	}

	/**
	 * Determines if the given field is a document field (starts with a forward slash).
	 *
	 * @param field The field name to check.
	 * @return True if the field is a document field, false otherwise.
	 */
	public static boolean isDocumentField(String field) {
		return field.startsWith(QueryGeneratorConstants.FORWARD_SLASH);
	}

	/**
	 * Creates a JSON path reference using the #> operator that returns the value of the field as json.
	 *
	 * The returned expression is intended to be used after a column reference to a JSONB or JSON column.
	 *
	 * Example SQL output:
	 * [source,sql]
	 * ----
	 * #> '{"field","nested","path"}'
	 * ----
	 *
	 * @param field The field for which the value should be returned.
	 * @param generatorContext The context for query generation (currently not used but kept for future extensions).
	 * @return The SQL expression that returns the value of this field as a json value.
	 */
	@NonNull public static String makeJsonFieldValueReferenceAndGetAsJson(String field, QueryGeneratorContext generatorContext) {
		return makeJsonFieldValueReferenceByPath(field, QueryGeneratorConstants.JSONB_EXTRACT_OBJECT_OPERATOR, generatorContext);
	}

	/**
	 * Creates a JSON path reference using the ##> operator that returns the value of the field as text.
	 *
	 * The returned expression is intended to be used after a column reference to a JSONB or JSON column.
	 *
	 * Example SQL output:
	 * [source,sql]
	 * ----
	 * #>> '{"field","nested","path"}'
	 * ----
	 *
	 * @param field The field for which the value should be returned.
	 * @param generatorContext The context for query generation (currently not used but kept for future extensions).
	 * @return The SQL expression that returns the value of this field as a text value.
	 */
	@NonNull public static String makeJsonFieldValueReferenceAndGetAsText(String field, QueryGeneratorContext generatorContext) {
		return makeJsonFieldValueReferenceByPath(field, QueryGeneratorConstants.JSONB_EXTRACT_TEXT_FOR_PATH_OPERATOR, generatorContext);
	}

	@NonNull private static String makeJsonFieldValueReferenceByPath(String field, String operator, QueryGeneratorContext generatorContext) {

		List<String> pathMembers = Arrays.asList(field.replaceFirst("^[./]", "").split("[./]"));

		if (CollectionUtils.isEmpty(pathMembers)) {
			return "";
		}

		return operator +
			QueryGeneratorConstants.TEXT_QUOTE +
			QueryGeneratorConstants.OPENING_CURLED_BRACKET +
			pathMembers.stream().map(SqlGeneratorHelpersInternal::escapeArrayMemberQuotes).collect(Collectors.joining(",")) +
			QueryGeneratorConstants.CLOSING_CURLED_BRACKET +
			QueryGeneratorConstants.TEXT_QUOTE;
	}

	/**
	 * Escapes single and double quotes in a JSON array member for use in SQL expressions.
	 *
	 * @param input The input string to escape.
	 * @return The escaped string suitable for use in a JSON path array.
	 */
	public static String escapeArrayMemberQuotes(String input) {
		return "\"%s\"".formatted(StringUtils.replace(StringUtils.replace(input, "'", "''"), "\"", "\\\""));
	}

	/**
	 * Prepares a JPA Query from a RootCteGenerator by rendering its SQL and using its context.
	 *
	 * @param entityManager The EntityManager to create the query with.
	 * @param gen The RootCteGenerator providing SQL and context.
	 * @return The prepared JPA Query instance.
	 */
	public static Query prepareQuery(EntityManager entityManager, RootCteGenerator gen) {
		return prepareQuery(entityManager, gen.render(new StringBuilder()).toString(), gen.getGeneratorContext());
	}

	/**
	 * Prepares a JPA Query from a SQL string and query context, setting all parameters as needed.
	 *
	 * @param entityManager The EntityManager to create the query with.
	 * @param sql The SQL string to use for the query.
	 * @param queryGeneratorContext The context holding parameters and configuration.
	 * @return The prepared JPA Query instance.
	 */
	public static Query prepareQuery(EntityManager entityManager, String sql, QueryGeneratorContext queryGeneratorContext) {
		if (queryGeneratorContext.getParamHolder().isEmpty()) {
			log.atDebug().setMessage("""
				Query SQL:
				----
				{}
				----""").addArgument(sql).log();
		} else {
			log.atDebug().setMessage("""
					Query SQL:
					----
					{}
					----
					
					Query SQL params:
					----
					{}
					----
					
					Query SQL with values:
					----
					{}
					----""")
				.addArgument(sql)
				.addArgument(queryGeneratorContext::getParamHolder)
				.addArgument(() -> getQueryWithValues(sql, queryGeneratorContext))
				.log();
		}
		Query query = entityManager.createNativeQuery(sql, DocumentTreeEntity.class);
		queryGeneratorContext.getParamHolder().forEach(query::setParameter);
		return query;
	}

	/**
	 * Renders all aggregation functions for the given query topology and appends them to the StringBuilder.
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param queryRoot The query topology containing aggregations.
	 * @param isSearchTable Whether the context is a search table.
	 * @param queryGeneratorContext The context for query generation.
	 */
	public static void renderAggregations(StringBuilder sb, QueryTopology queryRoot, boolean isSearchTable, QueryGeneratorContext queryGeneratorContext) {
		Iterator<IAggregationFunction> aggregations = queryRoot.getAggregation().getAggregations().iterator();
		while (aggregations.hasNext()) {
			renderAggregation(sb, aggregations.next(), isSearchTable, queryGeneratorContext);
			if (aggregations.hasNext()) {
				sb.append(QueryGeneratorConstants.COMMA);
			}
		}
	}

	/**
	 * Renders a single aggregation function and appends it to the StringBuilder.
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param aggregationFunction The aggregation function to render.
	 * @param isSearchTable Whether the context is a search table.
	 * @param queryGeneratorContext The context for query generation.
	 * @param <T> The type of aggregation function.
	 */
	public static <T extends IAggregationFunction> void renderAggregation(StringBuilder sb, T aggregationFunction, boolean isSearchTable,
		QueryGeneratorContext queryGeneratorContext) {
		queryGeneratorContext.getFunctionGenerator((Class<T>) aggregationFunction.getClass()).renderFunction(sb, aggregationFunction,
			isSearchTable, queryGeneratorContext);
	}

	/**
	 * Constructs a JSON object for group columns in the aggregation and appends it to the StringBuilder.
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param queryRoot The query topology containing group columns.
	 * @param queryGeneratorContext The context for query generation.
	 */
	public static void constructJsonForGroupColumns(StringBuilder sb, QueryTopology queryRoot, QueryGeneratorContext queryGeneratorContext) {
		Iterator<ProjectionField> groupFields = queryRoot.getAggregation().getGroup().iterator();
		while (groupFields.hasNext()) {
			ProjectionField groupField = groupFields.next();
			if (queryGeneratorContext.getEnrichments().getFieldDescriptor(groupField.getField()).getRepeatable()) {
				Deque<RepeatableAggField> repeatableAggFields = queryGeneratorContext.getEnrichments().getRepeatableAggFields(groupField.getField());
				Optional<RepeatableAggField> repeatableAggField = Optional.ofNullable(repeatableAggFields.peekLast());
				if (repeatableAggField.isPresent()) {
					renderAggRepeatableField(sb, repeatableAggField);
				}
			} else {
				renderJsonbField(sb, groupField.getField(), queryGeneratorContext);
			}
			if (groupFields.hasNext()) {
				sb.append(QueryGeneratorConstants.COMMA);
			}
		}
	}

	/**
	 * Renders a JSONB field reference for the given field and appends it to the StringBuilder.
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param field The field name to reference.
	 * @param generatorContext The context for query generation.
	 */
	public static void renderJsonbField(StringBuilder sb, String field, QueryGeneratorContext generatorContext) {
		// value #>> '{ContractRoot,ContractName}'
		if (SqlGeneratorHelpersInternal.isDocumentField(field)) {
			sb
				.append(QueryGeneratorConstants.TableNames.CTE_ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(generatorContext.getJsonColumnName())
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(field, generatorContext));
		} else {
			sb.append(SqlGeneratorHelpers.addParam(field, generatorContext));
		}
	}

	/**
	 * Renders a repeatable aggregation field for use in SQL and appends it to the StringBuilder.
	 *
	 * @param sb The StringBuilder to append the SQL fragment to.
	 * @param repeatableAggField The optional repeatable aggregation field to render.
	 */
	public static void renderAggRepeatableField(StringBuilder sb, Optional<RepeatableAggField> repeatableAggField) {
		sb
			.append(repeatableAggField.map(RepeatableAggField::tempAggName).orElse(""));
		Iterator<String> fields = repeatableAggField.map(field -> field.fieldPaths().iterator()).orElse(Collections.emptyIterator());
		while (fields.hasNext()) {
			String field = fields.next();
			sb.append(fields.hasNext()? JSON_FIELD_PATH_OPERATOR : JSON_FIELD_TEXT_VALUE_SELECTION_OPERATOR)
				.append(TEXT_QUOTE)
				.append(field)
				.append(TEXT_QUOTE);
		}
	}
}
