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
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.NUMBER_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE;

/**
 * Base class for rendering SELECT clause expansion, JOIN clauses, and ORDER BY
 * expressions within the root CTE query.
 *
 * Use the `of` factory method to obtain the appropriate subclass:
 *
 * - `RootCteRelationshipOrderRenderer` — when the sort specification contains relationship-based orders
 * - `RootCteDirectOrderRenderer` — when all orders are direct field orders
 *
 * Protected helper methods `appendDirectFieldOrderColumns`, `appendOrderDirection`, and
 * `appendNullHandling` are available to subclasses for rendering direct field sort expressions.
 */
abstract class RootCteOrderRenderer {

	protected final List<Order> sort;
	protected final QueryGeneratorContext context;

	RootCteOrderRenderer(List<Order> sort, QueryGeneratorContext context) {
		this.sort = sort;
		this.context = context;
	}

	/**
	 * Appends the SELECT column expression for the root CTE.
	 *
	 * @param sb the StringBuilder to append to
	 */
	abstract void appendSelectClause(StringBuilder sb);

	/**
	 * Renders JOIN clauses (if applicable) and the `ORDER BY` clause.
	 *
	 * @param sb the StringBuilder to append to
	 */
	abstract void renderJoinsAndOrderBy(StringBuilder sb);

	/**
	 * Appends the `ORDER BY` clause to the outer final SELECT (if applicable).
	 *
	 * @param sb the StringBuilder to append to
	 */
	abstract void appendFinalSelectOrderBy(StringBuilder sb);

	/**
	 * Returns whether the outer final SELECT must be enclosed in parentheses when followed by
	 * a `UNION ALL` of link sub-selects.
	 *
	 * PostgreSQL rejects an unparenthesized `SELECT ... ORDER BY ... UNION ALL ...` form, so
	 * any renderer that emits an outer `ORDER BY` (i.e. relationship-based sort) must opt in.
	 *
	 * @return `true` when {@link #appendFinalSelectOrderBy(StringBuilder)} produces output that
	 *         needs parenthesization in a `UNION ALL` context, `false` otherwise
	 */
	boolean requiresRootSelectParenthesization() {
		return false;
	}

	/**
	 * Returns the appropriate `RootCteOrderRenderer` subclass for the given sort specification.
	 *
	 * Returns `RootCteRelationshipOrderRenderer` when any order in `sort` is relationship-based,
	 * otherwise returns `RootCteDirectOrderRenderer`.
	 *
	 * @param sort the sort specification from the query
	 * @param context the query generator context
	 * @return the renderer instance suited for the sort specification
	 */
	static RootCteOrderRenderer of(List<Order> sort, QueryGeneratorContext context) {
		boolean hasRelationshipOrders = CollectionUtils.isNotEmpty(sort)
			&& sort.stream().anyMatch(RelationshipOrder.class::isInstance);
		return hasRelationshipOrders
			? new RootCteRelationshipOrderRenderer(sort, context)
			: new RootCteDirectOrderRenderer(sort, context);
	}

	/**
	 * Appends the sort column expression for a single direct field order.
	 *
	 * Handles special metadata fields (`doc_ref`, model name), string collation,
	 * enumeration locale sorting, and numeric casting.
	 *
	 * @param sb the StringBuilder to append to
	 * @param order the direct field order specification
	 */
	protected void appendDirectFieldOrderColumns(StringBuilder sb, DirectFieldOrder order) {
		// safe: only called for direct field orders
		boolean isStringField = STRING_FIELD_TYPE.equals(context.getEnrichments().getFieldDescriptor(order.field()).getFieldType());
		boolean isCaseSensitive = !Boolean.TRUE.equals(order.ignoreCase());
		if (DocumentMetadataConstants.DOCREF_METADATA_PATH.equalsIgnoreCase(order.field())) {
			sb.append(TableNames.CTE_ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.DOC_REF_COLUMN_ALIAS);
		} else if (DocumentMetadataConstants.MODEL_REFERENCE_PATH.equalsIgnoreCase(order.field())) {
			sb.append(TableNames.CTE_ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.MODEL_NAME_COLUMN_NAME);
		} else if (isStringField) {
			sb.append(TableNames.CTE_ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(order.field(), context));
			if (isCaseSensitive) {
				sb.append(QueryGeneratorConstants.COLLATE_CASE_SENSIITVE);
			}
		} else if (context.getEnrichments().getFieldDescriptor(order.field()).isEnumerationType()
			&& StringUtils.isNotBlank(context.getLocale())) {
			sb.append(TableNames.CTE_ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsJson(
					order.field() + QueryGeneratorConstants.FORWARD_SLASH + context.getLocale(), context));
		} else {
			if (NUMBER_FIELD_TYPE.equals(context.getEnrichments().getFieldDescriptor(order.field()).getFieldType())) {
				sb.append(QueryGeneratorConstants.OPENING_BRACKET)
					.append(TableNames.CTE_ROOT_ALIAS)
					.append(QueryGeneratorConstants.DOT_JOINER)
					.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
					.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(order.field(), context))
					.append(QueryGeneratorConstants.CLOSING_BRACKET)
					.append(QueryGeneratorConstants.CAST_OPERATOR)
					.append(QueryGeneratorConstants.NUMERIC_TYPE);
			} else {
				sb.append(TableNames.CTE_ROOT_ALIAS)
					.append(QueryGeneratorConstants.DOT_JOINER)
					.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
					.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsJson(order.field(), context));
			}
		}
	}

	/**
	 * Appends the sort direction keyword (`DESC`) for the given order.
	 * Appends nothing for ascending direction.
	 *
	 * @param sb the StringBuilder to append to
	 * @param order the order specification
	 */
	protected static void appendOrderDirection(StringBuilder sb, Order order) {
		if (order instanceof DirectFieldOrder dfo && dfo.direction() == DirectFieldOrder.Direction.DESC) {
			sb.append(QueryGeneratorConstants.DESC_KEYWORD);
		}
	}

	/**
	 * Appends the NULL handling clause (`NULLS FIRST` or `NULLS LAST`) for the given order.
	 * Appends nothing for `NATIVE` null handling.
	 *
	 * @param sb the StringBuilder to append to
	 * @param order the order specification
	 */
	protected static void appendNullHandling(StringBuilder sb, Order order) {
		Optional.ofNullable(order)
			.filter(DirectFieldOrder.class::isInstance)
			.map(DirectFieldOrder.class::cast)
			.map(DirectFieldOrder::nullHandling)
			.filter(nullHandling -> nullHandling != NullHandling.NATIVE)
			.ifPresent(nullHandling -> sb.append(nullHandling == NullHandling.NULLS_FIRST ?
				QueryGeneratorConstants.NULLS_FIRST_KEYWORD :
				QueryGeneratorConstants.NULLS_LAST_KEYWORD));
	}
}
