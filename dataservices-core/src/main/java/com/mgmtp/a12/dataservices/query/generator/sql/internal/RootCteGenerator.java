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
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.TargetDocumentModelAware;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.AbstractTopologyColumnGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.RootColumnGenerator;
import com.mgmtp.a12.dataservices.query.internal.aggregation.RepeatableAggField;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class RootCteGenerator extends AbstractCteGenerator<QueryRoot> {

	private RootCteOrderRenderer orderRenderer;

	private RootCteOrderRenderer getOrderRenderer() {
		if (orderRenderer == null) {
			orderRenderer = RootCteOrderRenderer.of(getQuery().getSort(), getGeneratorContext());
		}
		return orderRenderer;
	}

	@Override protected AbstractTopologyColumnGenerator.AbstractTopologyColumnGeneratorBuilder<?, ?> getFinalSelectColumnBuilder() {
		return RootColumnGenerator.builder();
	}

	@Override protected StringBuilder renderMainCteQuery(StringBuilder sb) {
		getGeneratorContext().registerRecursion(false);
		getGeneratorContext().setIsAggregated(getQuery().isAggregated());

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD);

		if (getQuery().isAggregated()) {
			appendAggregationSelect(sb);
		} else {
			getOrderRenderer().appendSelectClause(sb);
		}

		sb.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(QueryGeneratorConstants.OPENING_BRACKET);

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD)
			.append(QueryGeneratorConstants.ASTERISK)
			.append(QueryGeneratorConstants.COMMA)
			.append(ColumnNames.DOC_REF_COLUMN_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(ColumnNames.RESULT_DOC_REF_COLUMN_ALIAS);
		if (!getQuery().isAggregated()) {
			sb.append(QueryGeneratorConstants.COMMA)
				.append(QueryGeneratorConstants.COUNT_ALL_OVER_OPERATOR)
				.append(QueryGeneratorConstants.AS_KEYWORD)
				.append(ColumnNames.TOTAL_COUNT_COLUMN_ALIAS);
		}
		sb.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(TableNames.DOCUMENT_SEARCH_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(TableNames.TARGET_DOCUMENT_TABLE_ALIAS)
			.append(QueryGeneratorConstants.WHERE_KEYWORD);

		TargetDocumentModelAware query = getQuery();
		SqlGeneratorHelpersInternal.modelWhere(sb, TableNames.TARGET_DOCUMENT_TABLE_ALIAS,
			Stream.concat(Stream.of(query.getTargetDocumentModel()),
				Optional.ofNullable(getGeneratorContext().getEnrichments().getModelSubtypes(query.getTargetDocumentModel())).stream()
					.flatMap(Collection::stream)));
		SqlGeneratorHelpersInternal.renderConstraint(sb, getQuery().getConstraint(), getGeneratorContext(), true);

		sb.append(QueryGeneratorConstants.CLOSING_BRACKET);

		sb.append(QueryGeneratorConstants.AS_KEYWORD).append(TableNames.CTE_ROOT_ALIAS);

		if (getQuery().isAggregated()) {
			handleAggregationsOnRepeatableFields(sb);
			addGroupingForAggregations(sb);
		} else {
			getOrderRenderer().renderJoinsAndOrderBy(sb);
		}

		// Only append pagination for ROOT in case `exclude` is `false`
		if (!this.getGeneratorContext().isExclude()) {
			sb.append(QueryGeneratorConstants.OFFSET_KEYWORD)
				.append(getGeneratorContext().getPageOffset())
				.append(QueryGeneratorConstants.LIMIT_KEYWORD)
				.append(getGeneratorContext().getPageLimit());
		}

		getGeneratorContext().unregisterRecursion();
		getGeneratorContext().setIsAggregated(false);

		return sb;
	}

	private void addGroupingForAggregations(StringBuilder sb) {
		if (!getQuery().getAggregation().getGroup().isEmpty()) {
			// TODO A12S-6246: isGroupingAgg must be specified in every query level,
			//  the flag should not impact the whole query context but on proper level wher aggregation function is called.
			getGeneratorContext().setIsGroupingAgg(true);
			sb.append(QueryGeneratorConstants.GROUP_BY_KEYWORD);
			SqlGeneratorHelpersInternal.constructJsonForGroupColumns(sb, getQuery(), getGeneratorContext());
			if (isWhispererRequest(getQuery())) {
				sb.append(QueryGeneratorConstants.ORDER_BY_KEYWORD);
				SqlGeneratorHelpersInternal.renderAggregations(sb, getQuery(), false, getGeneratorContext());
				sb.append(QueryGeneratorConstants.DESC_KEYWORD)
					.append(QueryGeneratorConstants.COMMA);
				SqlGeneratorHelpersInternal.constructJsonForGroupColumns(sb, getQuery(), getGeneratorContext());
			}
		}
	}

	private void handleAggregationsOnRepeatableFields(StringBuilder sb) {
		Set<String> handledAlias = new HashSet<>();
		getQuery().getAggregation().getAggregations().forEach(aggregation -> {
			boolean isRepeatable = getGeneratorContext().getEnrichments().getFieldDescriptor(aggregation.getField()).getRepeatable();
			if (isRepeatable) {
				handleRepeatableAggField(sb, aggregation.getField(), handledAlias);
			}
		});
		getQuery().getAggregation().getGroup().forEach(projectionField -> {
			boolean isRepeatable = getGeneratorContext().getEnrichments().getFieldDescriptor(projectionField.getField()).getRepeatable();
			if (isRepeatable) {
				handleRepeatableAggField(sb, projectionField.getField(), handledAlias);
			}
		});
	}

	private void appendAggregationSelect(StringBuilder sb) {
		getGeneratorContext().setIsGroupingAgg(!getQuery().getAggregation().getGroup().isEmpty());
		sb.append(QueryGeneratorConstants.COUNT_ALL_OVER_OPERATOR)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(ColumnNames.TOTAL_COUNT_COLUMN_ALIAS)
			.append(QueryGeneratorConstants.COMMA)
			.append(QueryGeneratorConstants.JSON_BUILD_ARRAY_FUNCTION)
			.append(QueryGeneratorConstants.OPENING_BRACKET);
		SqlGeneratorHelpersInternal.constructJsonForGroupColumns(sb, getQuery(), this.getGeneratorContext());
		sb.append(getQuery().getAggregation().getGroup().isEmpty() ? "" : QueryGeneratorConstants.COMMA);
		SqlGeneratorHelpersInternal.renderAggregations(sb, getQuery(), false, this.getGeneratorContext());
		sb.append(QueryGeneratorConstants.CLOSING_BRACKET)
			.append(QueryGeneratorConstants.CAST_OPERATOR)
			.append(QueryGeneratorConstants.JSONB_TYPE)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(ColumnNames.CONTENT_COLUMN_ALIAS);
	}

	@Override public void renderFinalSelect(StringBuilder sb, boolean exclude) {
		String tableAlias = getAlias();

		sb.append(QueryGeneratorConstants.SELECT_KEYWORD);
		getFinalSelectColumnGenerator()
			.renderColumns(sb, getGeneratorContext())
			.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(tableAlias)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(TableNames.ROOT_ALIAS);

		getOrderRenderer().appendFinalSelectOrderBy(sb);
	}

	@Override protected void renderLinksCte(StringBuilder sb) {
		Optional.ofNullable(getQuery().getLinks()).stream()
			.flatMap(Collection::stream)
			.map(l -> LinkCteGenerator.builder().query(l))
			.map(b -> b.generatorContext(getGeneratorContext()))
			.map(b -> b.parent(this))
			.map(LinkCteGenerator.LinkCteGeneratorBuilder::build)
			.forEach(c -> {
				links.add(c);
				sb.append(QueryGeneratorConstants.COMMA);
				c.render(sb);
			});
	}

	private boolean isWhispererRequest(@NonNull QueryRoot query) {
		/*
			We assume the query to be a whisperer request if there is
		    a) only one aggregation functions, and this is the count function
		    b) only one group field (for which the whisperer suggestion should be returned.
		 */
		boolean onlyTheCountFunction = query.getAggregation().getAggregations() != null && query.getAggregation().getAggregations().size() == 1 &&
			query.getAggregation().getAggregations().getFirst().getFunction().equals("count");
		boolean onlyOneGroupField =
			query.getAggregation() != null && query.getAggregation().getGroup() != null && query.getAggregation().getGroup().size() == 1;
		return onlyTheCountFunction && onlyOneGroupField;
	}

	/**
	 * Main method for rendering the structure of a Common Table Expression (CTE).
	 *
	 * This method follows a three-stage process:
	 * 1. Render the main CTE structure.
	 * 2. Render CTEs for associated links.
	 * 3. Render resulting union select, combining results from CTEs.
	 */
	@Override public StringBuilder render(@NotNull StringBuilder sb) {
		sb.append(QueryGeneratorConstants.WITH_KEYWORD);
		if (hasRecursion(getQuery().getTargetDocumentModel(), getQuery().getLinks())) {
			sb.append(QueryGeneratorConstants.RECURSIVE_KEYWORD);
		}

		super.render(sb);
		renderUnionSelect(sb);

		return sb;
	}

	private void renderUnionSelect(StringBuilder sb) {
		AtomicBoolean putUnion = new AtomicBoolean(false);
		AtomicBoolean exclude = new AtomicBoolean(false);
		// render resulting select statement for main CTE
		AtomicReference<Stream<LinkCteGenerator>> allLinksFlattened = new AtomicReference<>();
		if (getQuery().isExclude()) {
			// Currently paging is only applied for single link query
			// TODO A12S-6000: Support for pagination on multiple links
			if (links.size() == 1) {
				links.stream().findFirst()
					.ifPresent(linkCteGenerator -> {
							putUnion.set(true);
							linkCteGenerator.setType(DocumentTreeNodeType.CHILD);
							beginCte(sb, getGeneratorContext().registerCteAlias("%s_%04d".formatted(
								TableNames.LINK_TABLE_ALIAS, getGeneratorContext().getTableCounter().addAndGet(1))));
							linkCteGenerator.renderFinalSelect(sb, false);
							endCte(sb, linkCteGenerator.getGeneratorContext().getCurrentCteAlias());
							linkCteGenerator.setType(DocumentTreeNodeType.LINK);
							linkCteGenerator.renderFinalSelect(sb.append(QueryGeneratorConstants.UNION_ALL_KEYWORD), true);
							allLinksFlattened.set(linkCteGenerator.getAllLinksFlattened());
							exclude.set(true);
						}
					);
			} else {
				allLinksFlattened.set(getAllLinksFlattened());
			}
		} else {
			// When non-excluded links follow, the root SELECT may carry an outer
			// `ORDER BY` (relationship-based sort) which PostgreSQL rejects directly before
			// `UNION ALL`. Wrap the root SELECT in parentheses in that case.
			boolean parenthesize = getOrderRenderer().requiresRootSelectParenthesization()
				&& getAllLinksFlattened().anyMatch(g -> !g.getQuery().isExclude());
			if (parenthesize) {
				sb.append(QueryGeneratorConstants.OPENING_BRACKET);
			}
			renderFinalSelect(sb, false);
			if (parenthesize) {
				sb.append(QueryGeneratorConstants.CLOSING_BRACKET);
			}
			putUnion.set(true);
			allLinksFlattened.set(getAllLinksFlattened());
		}

		// render resulting select statements for link CTEs
		allLinksFlattened.get()
			.filter(generator -> !generator.getQuery().isExclude())
			.forEach(linkCteGenerator -> renderLink(sb, linkCteGenerator, putUnion, exclude.get()));
	}

	private static void renderLink(StringBuilder sb, LinkCteGenerator linkCteGenerator, AtomicBoolean putUnion, boolean exclude) {
		// Add CHILD entries (aka links)
		linkCteGenerator.setType(DocumentTreeNodeType.CHILD);
		if (putUnion.getAndSet(true)) {
			sb.append(QueryGeneratorConstants.UNION_ALL_KEYWORD);
		}
		linkCteGenerator.renderFinalSelect(sb, false);
		// Add LINK entries  (aka link documents)
		linkCteGenerator.setType(DocumentTreeNodeType.LINK);
		linkCteGenerator.renderFinalSelect(sb.append(putUnion.getAndSet(true) ? QueryGeneratorConstants.UNION_ALL_KEYWORD : ""), exclude);
	}

	private static void endCte(StringBuilder sb, CharSequence currentCteAlias) {
		sb.append(QueryGeneratorConstants.CLOSING_BRACKET)
			.append(QueryGeneratorConstants.SELECT_KEYWORD)
			.append(QueryGeneratorConstants.ASTERISK)
			.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(currentCteAlias);
	}

	private static void beginCte(StringBuilder sb, CharSequence currentCteAlias) {
		sb.append(QueryGeneratorConstants.COMMA)
			.append(currentCteAlias)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(QueryGeneratorConstants.OPENING_BRACKET);
	}

	private void handleRepeatableAggField(StringBuilder sb, String projectionField, Set<String> handledAlias) {
		Deque<RepeatableAggField> repeatableAggFields = getGeneratorContext().getEnrichments().getRepeatableAggFields(projectionField);
		while (repeatableAggFields.size() > 1) {
			RepeatableAggField field = repeatableAggFields.pop();
			RepeatableAggField nextField = repeatableAggFields.peek();
			if (nextField != null && !handledAlias.contains(nextField.tempAggName())) {
				constructRepeatableAggFieldStr(sb, field, repeatableAggFields);
				handledAlias.add(nextField.tempAggName());
			}
		}
	}

	private static void constructRepeatableAggFieldStr(StringBuilder sb, RepeatableAggField field, Deque<RepeatableAggField> repeatableAggFields) {
		sb
			.append(QueryGeneratorConstants.COMMA)
			.append(QueryGeneratorConstants.JSONB_ARRAY_ELEMENTS_FUNCTION)
			.append(QueryGeneratorConstants.OPENING_BRACKET)
			.append(StringUtils.isNotBlank(field.tempAggName())? field.tempAggName() : (TableNames.CTE_ROOT_ALIAS + QueryGeneratorConstants.DOT_JOINER + ColumnNames.ORIGINAL_VALUE_COLUMN_NAME))
			.append(QueryGeneratorConstants.JSON_FIELD_PATH_OPERATOR)
			.append(QueryGeneratorConstants.TEXT_QUOTE)
			.append(StringUtils.join(field.fieldPaths(), QueryGeneratorConstants.TEXT_QUOTE + QueryGeneratorConstants.JSON_FIELD_PATH_OPERATOR + QueryGeneratorConstants.TEXT_QUOTE))
			.append(QueryGeneratorConstants.TEXT_QUOTE)
			.append(QueryGeneratorConstants.CLOSING_BRACKET)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append((repeatableAggFields.peek() != null && StringUtils.isNotBlank(repeatableAggFields.peek().tempAggName()))? Objects.requireNonNull(
				repeatableAggFields.peek()).tempAggName() : "");
	}
}
