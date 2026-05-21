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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.AbstractTopologyColumnGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.AbstractTopologyColumnGenerator.AbstractTopologyColumnGeneratorBuilder;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.LinkColumnsGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.LinkDocumentColumnsGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.SelectColumns;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder(toBuilder = true)
public class LinkCteGenerator extends AbstractCteGenerator<QueryLink> {

	@Setter protected DocumentTreeNodeType type;
	@NonNull private AbstractCteGenerator<? extends QueryTopology> parent;

	@Override public void renderFinalSelect(StringBuilder sb, boolean exclude) {
		String tableAlias = getAlias();

		getGeneratorContext().registerNewDocumentTableAlias(tableAlias);

		sb.append(QueryGeneratorConstants.OPENING_BRACKET)
			.append(QueryGeneratorConstants.SELECT_KEYWORD);

		getFinalSelectColumnGenerator()
			.renderColumns(sb, generatorContext)
			.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(tableAlias)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(TableNames.ROOT_ALIAS)
			.append(QueryGeneratorConstants.JOIN_KEYWORD)
			.append(TableNames.DOCUMENT_SEARCH_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(TableNames.RESULT_JOIN_TABLE_ALIAS)
			.append(QueryGeneratorConstants.ON_KEYWORD)
			.append(TableNames.ROOT_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(DocumentTreeNodeType.LINK.equals(this.type) ?
				ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS :
				ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(TableNames.RESULT_JOIN_TABLE_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.DOC_REF_COLUMN_ALIAS);

		renderWhere(sb, exclude);

		renderGroupByAggregated(sb);

		renderOrderBy(sb);

		// apply QueryRoot pagination onto links in case `exclude` is `true`.
		// TODO A12S-6000: Support for pagination on multiple links
		if (this.generatorContext.isExclude() && DocumentTreeNodeType.CHILD == this.type) {
			sb.append(QueryGeneratorConstants.OFFSET_KEYWORD)
				.append(this.generatorContext.getPageOffset())
				.append(QueryGeneratorConstants.LIMIT_KEYWORD)
				.append(this.generatorContext.getPageLimit());
		}

		sb.append(QueryGeneratorConstants.CLOSING_BRACKET);

		getGeneratorContext().unregisterDocumentTableAlias();
	}

	private void renderWhere(StringBuilder sb, boolean exclude) {
		// in case of `exclude` is `true`, we will append condition to current statement with prepared cte alias for including only matching `LINK` documents for `CHILD` in current page.
		// TODO A12S-6000: Support for pagination on multiple links
		if (exclude) {
			sb.append(QueryGeneratorConstants.WHERE_KEYWORD)
				.append(TableNames.ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.LINK_ID_COLUMN_ALIAS)
				.append(QueryGeneratorConstants.IN_OPERATOR)
				.append(QueryGeneratorConstants.OPENING_BRACKET)
				.append(QueryGeneratorConstants.SELECT_KEYWORD)
				.append(getGeneratorContext().getCurrentCteAlias())
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.LINK_ID_COLUMN_ALIAS)
				.append(QueryGeneratorConstants.FROM_KEYWORD)
				.append(getGeneratorContext().getCurrentCteAlias())
				.append(QueryGeneratorConstants.CLOSING_BRACKET);
		}
	}

	protected void renderGroupByAggregated(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.GROUP_BY_KEYWORD)
			.append(TableNames.RESULT_JOIN_TABLE_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.DOC_REF_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.RESULT_JOIN_TABLE_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.MODEL_NAME_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.RESULT_JOIN_TABLE_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.SOURCE_ROLE_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.TARGET_ROLE_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.LINK_SOURCE_ORDER_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.LINK_TARGET_ORDER_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.LINK_ID_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(TableNames.ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.LINK_COUNT_COLUMN_ALIS)
			.append(QueryGeneratorConstants.COMMA)
			.append(ColumnNames.DEPTH_COLUMN_ALIAS);
	}

	@Override protected StringBuilder renderMainCteQuery(StringBuilder sb) {

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD);

		SelectColumns outerCols = addOuterSelectColumns();
		outerCols.renderColumns(sb, getGeneratorContext());

		sb.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(QueryGeneratorConstants.OPENING_BRACKET);

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD);
		SelectColumns innerCols = addInnerSelectColumns();
		innerCols.renderColumns(sb, generatorContext);

		sb.append(QueryGeneratorConstants.FROM_KEYWORD);
		innerCols.renderTables(sb, generatorContext);

		renderWhere(sb);

		renderGroupByLinks(sb);

		if (Boolean.FALSE.equals(getGeneratorContext().getCurrentRecursionState())) {
			sb.append(QueryGeneratorConstants.LIMIT_KEYWORD)
				.append(getQuery().getMaxLinksSize());
		}

		sb.append(QueryGeneratorConstants.CLOSING_BRACKET)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(getAlias());

		if (isRecursive()) {
			renderRecursion(sb);
		}
		return sb;
	}

	protected StringBuilder renderWhere(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.WHERE_KEYWORD)
			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.ROLE_DOCREF_COLUMN_NAME)
			.append(QueryGeneratorConstants.IN_OPERATOR)
			.append(QueryGeneratorConstants.OPENING_BRACKET)
			.append(QueryGeneratorConstants.SELECT_KEYWORD)
			.append(ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(getParent().getAlias())
			.append(QueryGeneratorConstants.CLOSING_BRACKET);
		renderWhereInternal(sb);
		return sb;
	}

	@Override protected void renderLinksCte(StringBuilder sb) {
		Optional.ofNullable(query.getLinks()).stream()
			.flatMap(Collection::stream)
			.map(l -> getBuilder().query(l))
			.map(b -> b.generatorContext(generatorContext))
			.map(b -> b.parent(this))
			.map(LinkCteGenerator.LinkCteGeneratorBuilder::build)
			.forEach(c -> {
				links.add(c);
				sb.append(QueryGeneratorConstants.COMMA);
				c.render(sb);
			});
	}

	private static LinkCteGeneratorBuilder<?, ?> getBuilder() {
		return LinkCteGenerator.builder();
	}

	private void renderRecursion(StringBuilder sb) {

		getGeneratorContext().registerRecursion(true);

		sb.append(QueryGeneratorConstants.UNION_ALL_KEYWORD);

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD);

		SelectColumns cols = addInnerRecursionSelectColumns();
		cols.withTable(getAlias(), false)
			.end();
		cols
			.renderColumns(sb, generatorContext);

		sb.append(QueryGeneratorConstants.FROM_KEYWORD);

		cols.renderTables(sb, generatorContext);

		sb.append(QueryGeneratorConstants.WHERE_KEYWORD)

			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(
				ColumnNames.ROLE_DOCREF_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(getAlias())
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS);

		if (getQuery().getMaxDepth() != null) {
			sb.append(QueryGeneratorConstants.AND_OPERATOR)
				.append(ColumnNames.DEPTH_COLUMN_ALIAS)
				.append(QueryGeneratorConstants.LESS_THAN_OPERATOR)
				.append(getQuery().getMaxDepth());
		}

		renderWhereInternal(sb);

		getGeneratorContext().unregisterRecursion();
	}

	private void renderWhereInternal(StringBuilder sb) {
		SqlGeneratorHelpersInternal.linkWhere(sb, getQuery(), generatorContext);

		SqlGeneratorHelpersInternal.renderExistsConstraint(sb, getQuery().getConstraint(),
			new StringBuilder().append(TableNames.TARGET_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.ROLE_DOCREF_COLUMN_NAME), getGeneratorContext(),
			true);

		generatorContext.registerNewTargetDocRef
			(TableNames.LINK_TABLE_ALIAS + QueryGeneratorConstants.DOT_JOINER
				+ ColumnNames.LINK_DOCUMENT_DOCREF_COLUMN_NAME);
		SqlGeneratorHelpersInternal.renderExistsConstraint(sb, getQuery().getLinkDocumentConstraint(),
			new StringBuilder().append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.LINK_DOCUMENT_DOCREF_COLUMN_NAME),
			getGeneratorContext(),
			true);
		generatorContext.unregisterTargetDocRef();
	}

	protected SelectColumns addInnerSelectColumns() {
		SelectColumns cols = SelectColumns.builder().queryGeneratorContext(getGeneratorContext()).build();

		cols.withTable(TableNames.SOURCE_ROLE_TABLE_ALIAS, TableNames.RELATIONSHIP_ROLE_TABLE_NAME)
			.column(ColumnNames.SOURCE_ROLE_COLUMN_ALIAS, ColumnNames.ROLE_NAME_COLUMN_NAME)
			.column(ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS, ColumnNames.ROLE_DOCREF_COLUMN_NAME)
			.end();
		cols.withTable(TableNames.TARGET_ROLE_TABLE_ALIAS, TableNames.RELATIONSHIP_ROLE_TABLE_NAME)
			.column(ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS, ColumnNames.ROLE_DOCREF_COLUMN_NAME)
			.column(ColumnNames.TARGET_ROLE_COLUMN_ALIAS, ColumnNames.ROLE_NAME_COLUMN_NAME)
			.end();
		cols.withTable(TableNames.LINK_SOURCE_ORDER_TABLE_ALIAS, TableNames.LINK_ORDER_TABLE_NAME)
			.column(ColumnNames.LINK_SOURCE_ORDER_ALIAS, ColumnNames.ROLE_ORDER_COLUMN_NAME)
			.end();
		cols.withTable(TableNames.LINK_TARGET_ORDER_TABLE_ALIAS, TableNames.LINK_ORDER_TABLE_NAME)
			.column(ColumnNames.LINK_TARGET_ORDER_ALIAS, ColumnNames.ROLE_ORDER_COLUMN_NAME)
			.end();
		cols.withTable(TableNames.LINK_TABLE_ALIAS, TableNames.RELATIONSHIP_LINK_TABLE_NAME)
			.column(ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS, ColumnNames.RELATIONSHIP_MODEL_COLUMN_NAME)
			.column(ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS, ColumnNames.LINK_DOCUMENT_DOCREF_COLUMN_NAME)
			.column(ColumnNames.LINK_ID_COLUMN_ALIAS, ColumnNames.LINK_ID_COLUMN_NAME)
			.end();

		cols.column(ColumnNames.LINK_COUNT_COLUMN_ALIS, QueryGeneratorConstants.COUNT_ALL_OVER_OPERATOR);

		return cols;
	}

	private SelectColumns addInnerRecursionSelectColumns() {
		SelectColumns cols = addInnerSelectColumns();
		cols.column(ColumnNames.DEPTH_COLUMN_ALIAS, ColumnNames.DEPTH_COLUMN_ALIAS + " + 1");

		return cols;
	}

	@Override public boolean isRecursive() {
		return Objects.equals(generatorContext.getEnrichments().getTargetDocumentModel(getParent().getQuery()),
			generatorContext.getEnrichments().getTargetDocumentModel(getQuery()));
	}

	protected void renderOrderBy(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.ORDER_BY_KEYWORD);
		if (getQuery().getOrdered() != null && getQuery().getOrdered()) {
			sb.append(ColumnNames.LINK_SOURCE_ORDER_ALIAS).append(QueryGeneratorConstants.COMMA)
				.append(ColumnNames.LINK_TARGET_ORDER_ALIAS).append(QueryGeneratorConstants.COMMA);
		}
		sb.append(ColumnNames.LINK_ID_COLUMN_ALIAS);
	}

	protected SelectColumns addOuterSelectColumns() {
		SelectColumns cols = SelectColumns.builder().queryGeneratorContext(getGeneratorContext()).build();
		cols.column(ColumnNames.SOURCE_ROLE_COLUMN_ALIAS, ColumnNames.SOURCE_ROLE_COLUMN_ALIAS)
			.column(ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS, ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS)
			.column(ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS, ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS)
			.column(ColumnNames.TARGET_ROLE_COLUMN_ALIAS, ColumnNames.TARGET_ROLE_COLUMN_ALIAS)
			.column(ColumnNames.LINK_SOURCE_ORDER_ALIAS, ColumnNames.LINK_SOURCE_ORDER_ALIAS)
			.column(ColumnNames.LINK_TARGET_ORDER_ALIAS, ColumnNames.LINK_TARGET_ORDER_ALIAS)
			.column(ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS, ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS)
			.column(ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS, ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS)
			.column(ColumnNames.LINK_ID_COLUMN_ALIAS, ColumnNames.LINK_ID_COLUMN_ALIAS)
			.column(ColumnNames.LINK_COUNT_COLUMN_ALIS, ColumnNames.LINK_COUNT_COLUMN_ALIS)
			.column(ColumnNames.DEPTH_COLUMN_ALIAS, "1");

		return cols;
	}

	private static void renderGroupByLinks(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.GROUP_BY_KEYWORD)
			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_NAME_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.SOURCE_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_DOCREF_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.TARGET_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_DOCREF_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.TARGET_ROLE_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_NAME_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.LINK_SOURCE_ORDER_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_ORDER_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.LINK_TARGET_ORDER_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ROLE_ORDER_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.RELATIONSHIP_MODEL_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.LINK_DOCUMENT_DOCREF_COLUMN_NAME).append(
				QueryGeneratorConstants.COMMA)
			.append(TableNames.LINK_TABLE_ALIAS).append(QueryGeneratorConstants.DOT_JOINER).append(ColumnNames.ID_COLUMN_NAME);
	}

	@Override protected <C1 extends AbstractTopologyColumnGenerator, B extends AbstractTopologyColumnGeneratorBuilder<C1, B>> B getFinalSelectColumnBuilder() {
		return (B) (DocumentTreeNodeType.LINK.equals(type) ? LinkDocumentColumnsGenerator.builder() : LinkColumnsGenerator.builder());
	}

	public abstract static class LinkCteGeneratorBuilder<C extends LinkCteGenerator, B extends LinkCteGeneratorBuilder<C, B>> extends AbstractCteGenerator.AbstractCteGeneratorBuilder<QueryLink, C, B> {}
}
