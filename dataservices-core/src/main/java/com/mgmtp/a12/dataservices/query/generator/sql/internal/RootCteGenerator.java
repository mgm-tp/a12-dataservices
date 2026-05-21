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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.Order;
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

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.NUMBER_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE;

@SuperBuilder
public class RootCteGenerator extends AbstractCteGenerator<QueryRoot> {

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
			sb.append(QueryGeneratorConstants.ASTERISK);
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
			renderOrderBy(sb);
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
			boolean isRepeatable = generatorContext.getEnrichments().getFieldDescriptor(aggregation.getField()).getRepeatable();
			if (isRepeatable) {
				handleRepeatableAggField(sb, aggregation.getField(), handledAlias);
			}
		});
		getQuery().getAggregation().getGroup().forEach(projectionField -> {
			boolean isRepeatable = generatorContext.getEnrichments().getFieldDescriptor(projectionField.getField()).getRepeatable();
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
			.renderColumns(sb, generatorContext)
			.append(QueryGeneratorConstants.FROM_KEYWORD)
			.append(tableAlias)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(TableNames.ROOT_ALIAS);
	}

	@Override protected void renderLinksCte(StringBuilder sb) {
		Optional.ofNullable(query.getLinks()).stream()
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

	private void renderOrderBy(StringBuilder sb) {
		List<Order> sort = getQuery().getSort();
		if (CollectionUtils.isNotEmpty(sort)) {
			sb.append(QueryGeneratorConstants.ORDER_BY_KEYWORD)
				.append(sort.stream()
					.map(this::renderSortField)
					.collect(Collectors.joining(QueryGeneratorConstants.COMMA)));
		}
	}

	private String renderSortField(Order order) {
		StringBuilder sb = new StringBuilder();

		appendOrderColumns(sb, order);
		appendOrderDirection(sb, order);
		appendNullHandling(sb, order);

		return sb.toString();
	}

	private void appendOrderColumns(StringBuilder sb, Order order) {
		boolean isStringField = STRING_FIELD_TYPE.equals(getGeneratorContext().getEnrichments().getFieldDescriptor(order.field()).getFieldType());
		boolean isCaseSensitive = !order.ignoreCase();
		if (DocumentMetadataConstants.DOCREF_METADATA_PATH.equalsIgnoreCase(order.field())) {
			sb.append(ColumnNames.DOC_REF_COLUMN_ALIAS);
		} else if (DocumentMetadataConstants.MODEL_REFERENCE_PATH.equalsIgnoreCase(order.field())) {
			sb.append(ColumnNames.MODEL_NAME_COLUMN_NAME);
		} else if (isStringField && isCaseSensitive) {
			sb.append(TableNames.CTE_ROOT_ALIAS)
				.append(QueryGeneratorConstants.DOT_JOINER)
				.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(order.field(), getGeneratorContext()))
				.append(QueryGeneratorConstants.COLLATE_CASE_SENSIITVE);
		} else if (getGeneratorContext().getEnrichments().getFieldDescriptor(order.field()).isEnumerationType()
			&& StringUtils.isNotBlank(getGeneratorContext().getLocale())) {
			sb.append(ColumnNames.VALUE_COLUMN_NAME)
				.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsJson(
					order.field() + QueryGeneratorConstants.FORWARD_SLASH + getGeneratorContext().getLocale(), getGeneratorContext()));
		} else {
			if (NUMBER_FIELD_TYPE.equals(getGeneratorContext().getEnrichments().getFieldDescriptor(order.field()).getFieldType())) {
				sb.append(QueryGeneratorConstants.OPENING_BRACKET)
					.append(TableNames.CTE_ROOT_ALIAS)
					.append(QueryGeneratorConstants.DOT_JOINER)
					.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
					.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsText(order.field(), getGeneratorContext()))
					.append(QueryGeneratorConstants.CLOSING_BRACKET)
					.append(QueryGeneratorConstants.CAST_OPERATOR)
					.append(QueryGeneratorConstants.NUMERIC_TYPE);
			} else {
				sb.append(TableNames.CTE_ROOT_ALIAS)
					.append(QueryGeneratorConstants.DOT_JOINER)
					.append(ColumnNames.ORIGINAL_VALUE_COLUMN_NAME)
					.append(SqlGeneratorHelpersInternal.makeJsonFieldValueReferenceAndGetAsJson(order.field(), getGeneratorContext()));
			}
		}
	}

	private static void appendNullHandling(StringBuilder sb, Order order) {
		if (order.nullHandling() == Order.NullHandling.NULLS_FIRST) {
			sb.append(QueryGeneratorConstants.NULLS_FIRST_KEYWORD);
		} else if (order.nullHandling() == Order.NullHandling.NULLS_LAST) {
			sb.append(QueryGeneratorConstants.NULLS_LAST_KEYWORD);
		}
	}

	private static void appendOrderDirection(StringBuilder sb, Order order) {
		if (order.direction() == Order.Direction.DESC) {
			sb.append(QueryGeneratorConstants.DESC_KEYWORD);
		}
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
			renderFinalSelect(sb, false);
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
		Deque<RepeatableAggField> repeatableAggFields = generatorContext.getEnrichments().getRepeatableAggFields(projectionField);
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
