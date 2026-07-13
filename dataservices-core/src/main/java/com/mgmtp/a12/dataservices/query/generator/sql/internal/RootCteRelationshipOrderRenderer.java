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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.AND_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.AS_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.CLOSING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.COMMA;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.FIELD_NAME_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.NUMBER_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.ROLE_DOCREF_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.TARGET_DOCREF_COLUMN_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.TIMESTAMP_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.TS_RANGE_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.TYPED_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.DOT_JOINER;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.EQUALS_OPERATOR;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FROM_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FRAGMENT_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_RANGE_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_TIME_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.NUMBER_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.LEFT_JOIN_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ON_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.OPENING_BRACKET;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SELECT_KEYWORD;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.SPACE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.DOCUMENT_FIELDS_TABLE_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.DOCUMENT_SEARCH_TABLE_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.LINK_TABLE_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.RELATIONSHIP_LINK_TABLE_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.RELATIONSHIP_ROLE_TABLE_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.SOURCE_ROLE_TABLE_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames.TARGET_ROLE_TABLE_ALIAS;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.WHERE_KEYWORD;

/**
 * Renders LEFT JOIN clauses and ORDER BY expressions for relationship-based sorting
 * ({@link RelationshipOrder}) within a root CTE query.
 *
 * Used when the sort specification contains at least one relationship-based order.
 * Mixed sort lists (combining relationship and direct field orders) are supported —
 * direct field sort expressions delegate to inherited helpers from `RootCteOrderRenderer`.
 * Nested (multi-hop) relationship traversal is handled recursively.
 *
 * === Mechanism
 *
 * A LEFT JOIN to a related document may produce multiple rows per source document.
 * To handle this, each source document is assigned a deterministic rank via
 * `ROW_NUMBER() OVER (ORDER BY ...)` inside the root CTE.
 * The outer SELECT then orders by this pre-computed rank column (`_sort_rank`).
 *
 * Rendering proceeds in two phases:
 *
 * . `computeTerminalJoinLevels()` — dry-runs the hop traversal to determine which
 * alias suffix (`d1`, `d2`, …) each sort field will reference. This is needed because
 * the `ROW_NUMBER()` expression is emitted in the SELECT clause, before the JOIN clauses.
 * . `renderJoins()` — generates the actual LEFT JOIN chain and the inner `ORDER BY _sort_rank`.
 *
 * === Join alias scheme
 *
 * For each relationship hop at level `n`, two external table aliases are generated, plus an
 * optional terminal document-fields alias:
 *
 * [source,text]
 * ----
 * rr{n}   – derived table    – encapsulates source_role/link/target_role traversal for one hop
 * d{n}    – document_search  – the target document data, joined on rr{n}.target_docref
 * df{n}   – document_fields  – typed column values (date, number, enum…); terminal hop only
 * ----
 *
 * Inside each derived table the fixed aliases `source_role`, `link` and
 * `target_role` are used (locally scoped, safe to reuse across multiple derived tables).
 *
 * The hop counter `n` increments globally across all sort orders. The first hop of every
 * independent sort order always anchors on `cte_root.doc_ref`; subsequent nested hops
 * anchor on the preceding hop's `d{n-1`.doc_ref}.
 *
 * === Traversal diagram
 *
 * Single-hop (e.g. Contract → BusinessPartner via ContractBusinessPartner):
 *
 * [source,text]
 * ----
 * cte_root ──► rr1 (derived table) ──► d1 ──► df1 (terminal)
 * ----
 *
 * Two-hop nested (e.g. Contract → BusinessPartner → Address):
 *
 * [source,text]
 * ----
 * cte_root ──► rr1 ──► d1 ──► rr2 ──► d2 ──► df2 (terminal)
 * ----
 *
 * Two independent sort orders (e.g. sort by Partner.name AND Insurer.companyName):
 *
 * [source,text]
 * ----
 * cte_root ──► rr1 ──► d1 ──► df1   (sort order 1: ContractBusinessPartner)
 * cte_root ──► rr2 ──► d2 ──► df2   (sort order 2: ContractInsurer, also anchored on cte_root)
 * ----
 *
 * === Example — generated SQL structure
 *
 * Query: sort `Contract` by field `/BusinessPartnerRoot/Name` of related
 * `BusinessPartner` through relationship `ContractBusinessPartner`, target role `Partner`.
 *
 * [source,sql]
 * ----
 * WITH "Contract_0001" AS (
 * SELECT cte_root.*,
 * ROW_NUMBER() OVER (ORDER BY
 * d1.original_value #>> '{"BusinessPartnerRoot","Name"}' COLLATE "C" NULLS LAST
 * ) AS _sort_rank
 * FROM (...) AS cte_root
 * LEFT JOIN (
 * SELECT source_role.role_docref AS source_docref,
 * target_role.role_docref AS target_docref
 * FROM relationship_role AS source_role,
 * relationship_link AS link,
 * relationship_role AS target_role
 * WHERE link.relationship_model = :p0
 * AND source_role.relationship_id = link.id
 * AND link.id = target_role.relationship_id
 * AND source_role.role_name = :p1
 * AND target_role.role_name = :p2
 * ) AS rr1 ON rr1.source_docref = cte_root.doc_ref
 * LEFT JOIN document_search AS d1 ON d1.doc_ref = rr1.target_docref
 * LEFT JOIN document_fields AS df1
 * ON df1.doc_ref = d1.doc_ref AND df1.field_name = :p3
 * ORDER BY _sort_rank ...)
 * SELECT ...
 * FROM "Contract_0001" AS roots
 * ORDER BY roots._sort_rank
 * ----
 */
class RootCteRelationshipOrderRenderer extends RootCteOrderRenderer {

	RootCteRelationshipOrderRenderer(List<Order> sort, QueryGeneratorContext context) {
		super(sort, context);
	}

	/**
	 * Appends the SELECT column expression for the root CTE query.
	 *
	 * Produces:
	 * `cte_root.*, ROW_NUMBER() OVER (ORDER BY <sort expressions>) AS sort_rank`
	 * to allow deterministic ranking before the outer SELECT.
	 *
	 * @param sb the StringBuilder to append to
	 */
	@Override
	void appendSelectClause(StringBuilder sb) {
		List<Integer> terminalJoinLevels = computeTerminalJoinLevels();
		String sortExpression = buildSortExpressionsString(terminalJoinLevels);
		sb.append(TableNames.CTE_ROOT_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(QueryGeneratorConstants.ASTERISK)
			.append(QueryGeneratorConstants.COMMA)
			.append(QueryGeneratorConstants.ROW_NUMBER_FUNCTION_PREFIX)
			.append(sortExpression)
			.append(QueryGeneratorConstants.CLOSING_BRACKET)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(ColumnNames.SORT_RANK_COLUMN);
	}

	/**
	 * Renders LEFT JOIN clauses for all relationship orders and appends `ORDER BY sort_rank`,
	 * referencing the `ROW_NUMBER()` column added by `appendSelectClause`.
	 *
	 * @param sb the StringBuilder to append to
	 */
	@Override
	void renderJoinsAndOrderBy(StringBuilder sb) {
		renderJoins(sb);
		if (CollectionUtils.isNotEmpty(sort)) {
			sb.append(QueryGeneratorConstants.ORDER_BY_KEYWORD)
				.append(ColumnNames.SORT_RANK_COLUMN);
		}
	}

	/**
	 * Appends the `ORDER BY` clause to the outer final SELECT.
	 *
	 * Produces `ORDER BY root_alias.sort_rank`, referencing the pre-computed rank column
	 * added to the inner CTE.
	 *
	 * @param sb the StringBuilder to append to
	 */
	@Override
	void appendFinalSelectOrderBy(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.ORDER_BY_KEYWORD)
			.append(TableNames.ROOT_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.SORT_RANK_COLUMN);
	}

	/**
	 * Relationship-based sorting emits an outer `ORDER BY roots._sort_rank` on the final SELECT,
	 * which PostgreSQL rejects when followed by `UNION ALL` unless the SELECT is parenthesized.
	 */
	@Override
	boolean requiresRootSelectParenthesization() {
		return true;
	}

	/**
	 * Pre-computes the terminal join level for each relationship order without generating SQL output.
	 *
	 * This mirrors `renderJoins` logic but appends nothing to any `StringBuilder`.
	 * Used to determine join level numbers before the SELECT clause is built, so that the sort
	 * expressions can be embedded in `ROW_NUMBER() OVER (ORDER BY ...)`.
	 *
	 * @return list of terminal join counter values, one per relationship order (in order)
	 */
	List<Integer> computeTerminalJoinLevels() {
		if (CollectionUtils.isEmpty(sort)) {
			return List.of();
		}
		AtomicReference<Integer> joinCounter = new AtomicReference<>(0);
		return sort.stream()
			.filter(RelationshipOrder.class::isInstance)
			.map(RelationshipOrder.class::cast)
			.map(order -> computeJoinLevel(order, joinCounter))
			.toList();
	}

	/**
	 * Renders LEFT JOIN clauses for all relationship orders in the query.
	 *
	 * For each relationship order (and each nested hop within it), generates:
	 *
	 * . A derived-table LEFT JOIN (`rr{n`}) encapsulating the three structural
	 * relationship tables (`source_role`, `link`, `target_role`)
	 * filtered by the relationship model and role names via bound parameters.
	 * . `LEFT JOIN document_search AS d{n`} on the derived table's target docref.
	 * . `LEFT JOIN document_fields AS df{n`} — terminal hop only, for typed sort columns.
	 *
	 * @param sb the StringBuilder to append JOIN clauses to
	 */
	void renderJoins(StringBuilder sb) {
		if (CollectionUtils.isEmpty(sort)) {
			return;
		}
		AtomicInteger joinUniqueId = new AtomicInteger(0);
		String rootAnchor = TableNames.CTE_ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.DOC_REF_COLUMN_ALIAS;
		sort.stream()
			.filter(RelationshipOrder.class::isInstance)
			.map(RelationshipOrder.class::cast)
			.forEach(order -> renderJoin(sb, order, joinUniqueId, rootAnchor));
	}

	/**
	 * Appends ORDER BY column reference for a relationship order.
	 *
	 * Generates a reference to the terminal field of the JOINed target document.
	 * String fields use `d{n}.original_value` with optional case handling.
	 * Typed fields (date, datetime, number, enumeration, date-fragment, date-range)
	 * use the typed columns from the `df{n}` (document_fields) alias added at the terminal level.
	 *
	 * @param sb the StringBuilder to append to
	 * @param terminalField the terminal direct field order specification
	 * @param terminalJoinLevel the terminal join counter value for this relationship order
	 */
	void appendRelationshipOrderColumns(StringBuilder sb, DirectFieldOrder terminalField, int terminalJoinLevel) {
		String fieldName = terminalField.field();

		String documentAlias = "d" + terminalJoinLevel;
		String documentFieldsAlias = "df" + terminalJoinLevel;

		String fieldType = context.getEnrichments().getFieldDescriptor(fieldName).getFieldType();

		if (STRING_FIELD_TYPE.equals(fieldType) && !Boolean.TRUE.equals(terminalField.ignoreCase())) {
			sb.append(documentAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(fieldName, context))
				.append(QueryGeneratorConstants.COLLATE_CASE_SENSIITVE);
		} else if (STRING_FIELD_TYPE.equals(fieldType)) {
			sb.append(QueryGeneratorConstants.LOWER_FUNCTION)
				.append(QueryGeneratorConstants.OPENING_BRACKET)
				.append(documentAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(fieldName, context))
				.append(QueryGeneratorConstants.CLOSING_BRACKET);
		} else if (ENUMERATION_FIELD_TYPE.equals(fieldType)
			&& StringUtils.isNotBlank(context.getLocale())) {
			sb.append(documentAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsJson(
					fieldName + QueryGeneratorConstants.FORWARD_SLASH + context.getLocale(),
					context));
		} else if (NUMBER_FIELD_TYPE.equals(fieldType)) {
			sb.append(documentFieldsAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(NUMBER_VALUE_COLUMN_NAME);
		} else if (DATE_FIELD_TYPE.equals(fieldType) || DATE_TIME_FIELD_TYPE.equals(fieldType)) {
			sb.append(documentFieldsAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(TIMESTAMP_VALUE_COLUMN_NAME);
		} else if (DATE_RANGE_FIELD_TYPE.equals(fieldType)) {
			sb.append(QueryGeneratorConstants.LOWER_FUNCTION)
				.append(QueryGeneratorConstants.OPENING_BRACKET)
				.append(documentFieldsAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(TS_RANGE_VALUE_COLUMN_NAME)
				.append(QueryGeneratorConstants.CLOSING_BRACKET);
		} else if (DATE_FRAGMENT_FIELD_TYPE.equals(fieldType)) {
			sb.append(documentFieldsAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(TYPED_VALUE_COLUMN_NAME);
		} else if (ENUMERATION_FIELD_TYPE.equals(fieldType)) {
			sb.append(documentFieldsAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(VALUE_COLUMN_NAME);
		} else {
			sb.append(documentAlias)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(fieldName, context));
		}
	}

	private String buildSortExpressionsString(List<Integer> terminalJoinLevels) {
		AtomicInteger relationshipOrderIndex = new AtomicInteger(0);
		return sort.stream()
			.map(order -> renderSortField(order, relationshipOrderIndex, terminalJoinLevels))
			.collect(Collectors.joining(QueryGeneratorConstants.COMMA));
	}

	private CharSequence renderSortField(Order order, AtomicInteger relationshipOrderIndex, List<Integer> terminalJoinLevels) {
		StringBuilder sb = new StringBuilder();
		if (order instanceof RelationshipOrder relationshipOrder) {
			DirectFieldOrder terminalOrder = findTerminalOrder(relationshipOrder);
			appendRelationshipOrderColumns(sb, terminalOrder, terminalJoinLevels.get(relationshipOrderIndex.getAndIncrement()));
			order = terminalOrder;
		} else if (order instanceof DirectFieldOrder dfo) {
			appendDirectFieldOrderColumns(sb, dfo);
		} else {
			throw new IllegalStateException();
		}
		appendOrderDirection(sb, order);
		appendNullHandling(sb, order);
		return sb;
	}

	private static DirectFieldOrder findTerminalOrder(Order order) {
		if (order instanceof DirectFieldOrder dfo) {
			return dfo;
		} else if (order instanceof RelationshipOrder ro) {
			return findTerminalOrder(ro.sortBy());
		} else {
			throw new IllegalStateException("No terminal field found in relationship order chain");
		}
	}

	/**
	 * Recursively computes the terminal hop level for a single relationship order
	 * without generating SQL output.
	 *
	 * @param relationshipOrder the relationship order specification
	 * @param joinCounter counter tracking the current hop depth (shared across all sort orders)
	 * @return the hop counter value at the innermost (terminal) level
	 */
	private int computeJoinLevel(Order relationshipOrder, AtomicReference<Integer> joinCounter) {
		if (relationshipOrder instanceof DirectFieldOrder) {
			return joinCounter.get();
		}
		if (relationshipOrder instanceof RelationshipOrder ro) {
			int currentLevel = joinCounter.get() + 1;
			joinCounter.set(currentLevel);
			return computeJoinLevel(ro.sortBy(), joinCounter);
		}
		throw new IllegalStateException("Unexpected order type in relationship chain");
	}

	/**
	 * Renders JOIN clauses for one relationship-order hop and recurses for nested hops.
	 *
	 * Each hop produces a derived-table LEFT JOIN that encapsulates the three structural
	 * relationship tables (`source_role`, `link`, `target_role`), followed
	 * by a `document_search` LEFT JOIN on the derived table's `target_docref`.
	 * At the terminal hop a `document_fields` LEFT JOIN is added for typed sort columns.
	 *
	 * [source,sql]
	 * ----
	 * LEFT JOIN (
	 * SELECT source_role.role_docref AS source_docref,
	 * target_role.role_docref AS target_docref
	 * FROM relationship_role AS source_role,
	 * relationship_link AS link,
	 * relationship_role AS target_role
	 * WHERE link.relationship_model = :p0
	 * AND source_role.relationship_id = link.id
	 * AND link.id = target_role.relationship_id
	 * AND source_role.role_name = :p1
	 * AND target_role.role_name = :p2
	 * ) AS rr1 ON rr1.source_docref = cte_root.doc_ref
	 * LEFT JOIN document_search AS d1 ON d1.doc_ref = rr1.target_docref
	 * LEFT JOIN document_fields AS df1 ON df1.doc_ref = d1.doc_ref AND df1.field_name = :p3
	 * ----
	 *
	 * @param sb the StringBuilder to append JOIN clauses to
	 * @param relationshipOrder the relationship order specification for this hop
	 * @param joinUniqueId shared hop counter for generating unique outer aliases
	 * @param anchorDocRef the docref expression this hop's derived table is joined on
	 * @return the hop counter value at the innermost (terminal) level
	 */
	private int renderJoin(StringBuilder sb, Order relationshipOrder, AtomicInteger joinUniqueId, String anchorDocRef) {
		if (relationshipOrder instanceof DirectFieldOrder dfo) {
			return renderDirectFieldJoin(sb, dfo, joinUniqueId.get());
		} else if (relationshipOrder instanceof RelationshipOrder ro)
			return renderRelationshipJoin(sb, joinUniqueId, anchorDocRef, ro);
		else {
			throw new IllegalStateException("Unexpected order type in relationship chain");
		}
	}

	private int renderRelationshipJoin(StringBuilder sb, AtomicInteger joinUniqueId, String anchorDocRef, RelationshipOrder relationshipOrder) {

		int currentLevel = joinUniqueId.addAndGet(1);
		String rrAlias = "rr%d".formatted(currentLevel);
		String documentAlias = "d%d".formatted(currentLevel);

		String sourceRole = context.getEnrichments().getSourceRoleForRelationshipOrder(relationshipOrder);
		StringBuilder derivedTableWhere = new StringBuilder();
		SqlGeneratorHelpersInternal.linkWhere(derivedTableWhere, relationshipOrder.relationshipModel(), sourceRole, relationshipOrder.targetRole(),
			context);

		sb.append(LEFT_JOIN_KEYWORD)
			.append(OPENING_BRACKET)
			.append(SELECT_KEYWORD)
			.append(SOURCE_ROLE_TABLE_ALIAS).append(DOT_JOINER).append(ROLE_DOCREF_COLUMN_NAME)
			.append(AS_KEYWORD).append(SOURCE_DOCREF_COLUMN_ALIAS)
			.append(COMMA)
			.append(TARGET_ROLE_TABLE_ALIAS).append(DOT_JOINER).append(ROLE_DOCREF_COLUMN_NAME)
			.append(AS_KEYWORD).append(TARGET_DOCREF_COLUMN_ALIAS)
			.append(FROM_KEYWORD)
			.append(RELATIONSHIP_ROLE_TABLE_NAME).append(AS_KEYWORD).append(SOURCE_ROLE_TABLE_ALIAS)
			.append(COMMA)
			.append(RELATIONSHIP_LINK_TABLE_NAME).append(AS_KEYWORD).append(LINK_TABLE_ALIAS)
			.append(COMMA)
			.append(RELATIONSHIP_ROLE_TABLE_NAME).append(AS_KEYWORD).append(TARGET_ROLE_TABLE_ALIAS)
			.append(WHERE_KEYWORD)
			.append(derivedTableWhere)
			.append(SPACE)
			.append(CLOSING_BRACKET)
			.append(AS_KEYWORD).append(rrAlias)
			.append(ON_KEYWORD)
			.append(rrAlias).append(DOT_JOINER).append(SOURCE_DOCREF_COLUMN_ALIAS)
			.append(EQUALS_OPERATOR)
			.append(anchorDocRef);

		appendTargetDocumentJoin(sb, documentAlias, rrAlias);

		return renderJoin(sb, relationshipOrder.sortBy(), joinUniqueId, documentAlias + DOT_JOINER + DOC_REF_COLUMN_NAME);
	}

	private int renderDirectFieldJoin(StringBuilder sb, DirectFieldOrder dfo, int joinUniqueId) {
		String dfAlias = "df%d".formatted(joinUniqueId);
		String documentAlias = "d%d".formatted(joinUniqueId);
		sb.append(LEFT_JOIN_KEYWORD)
			.append(DOCUMENT_FIELDS_TABLE_NAME)
			.append(AS_KEYWORD).append(dfAlias)
			.append(ON_KEYWORD).append(dfAlias).append(DOT_JOINER).append(DOC_REF_COLUMN_NAME)
			.append(EQUALS_OPERATOR).append(documentAlias).append(DOT_JOINER).append(DOC_REF_COLUMN_NAME)
			.append(AND_OPERATOR).append(dfAlias).append(DOT_JOINER).append(FIELD_NAME_COLUMN_NAME)
			.append(EQUALS_OPERATOR)
			.append(SqlGeneratorHelpersInternal.addParam(dfo.field(), context));
		return joinUniqueId;
	}

	/**
	 * Appends the `document_search` LEFT JOIN for the target document.
	 *
	 * Resolves the derived table's `target_docref` into full document data
	 * accessible for sort expressions.
	 *
	 * [source,sql]
	 * ----
	 * LEFT JOIN document_search AS d1
	 * ON d1.doc_ref = rr1.target_docref
	 * ----
	 */
	private void appendTargetDocumentJoin(StringBuilder sb, String documentAlias, String rrAlias) {
		sb.append(LEFT_JOIN_KEYWORD)
			.append(DOCUMENT_SEARCH_TABLE_NAME).append(AS_KEYWORD).append(documentAlias)
			.append(ON_KEYWORD)
			.append(documentAlias).append(DOT_JOINER).append(DOC_REF_COLUMN_NAME)
			.append(EQUALS_OPERATOR)
			.append(rrAlias).append(DOT_JOINER).append(TARGET_DOCREF_COLUMN_ALIAS);
	}

}
