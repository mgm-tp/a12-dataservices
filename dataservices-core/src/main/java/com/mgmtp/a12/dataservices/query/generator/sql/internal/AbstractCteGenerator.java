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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.AbstractTopologyColumnGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.AbstractTopologyColumnGenerator.AbstractTopologyColumnGeneratorBuilder;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.AggregatedColumnGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.columns.internal.RecursiveColumnGenerator;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public abstract class AbstractCteGenerator<Q extends QueryTopology> {

	@Getter protected final @NonNull Q query;

	@Getter protected final QueryGeneratorContext generatorContext;

	@Getter protected final Collection<DocumentTreeNodeType> types;

	protected String alias;

	protected final Collection<LinkCteGenerator> links = new ArrayList<>();

	/**
	 * Main method for rendering the structure of a Common Table Expression (CTE).
	 *
	 * This method follows a two-stage process:
	 * 1. Render the main CTE structure.
	 * 2. Render CTEs for associated links.
	 */
	public StringBuilder render(StringBuilder sb) {
		renderMainCte(sb);
		renderLinksCte(sb);

		return sb;
	}

	public Stream<LinkCteGenerator> getAllLinksFlattened() {
		return links.stream()
			.flatMap(s -> Stream.concat(Stream.of(s), s.getAllLinksFlattened()));
	}

	public String getAlias() {
		if (alias == null) {
			alias = "\"%s_%04d\"".formatted(generatorContext.getEnrichments().getTargetDocumentModel(getQuery()),
				generatorContext.getTableCounter().addAndGet(1));
		}
		return alias;
	}

	/**
	 * Abstract method responsible for generating the main CTE query.
	 *
	 * This method must be implemented by subclasses to define the logic
	 * for rendering the primary Common Table Expression (CTE).
	 */
	protected abstract StringBuilder renderMainCteQuery(StringBuilder sb);

	/**
	 * Abstract method responsible for generating CTEs for links.
	 *
	 * Subclasses must implement this method to define the logic for rendering
	 * the Common Table Expressions (CTEs) associated with links.
	 */
	protected abstract void renderLinksCte(StringBuilder sb);

	/**
	 * Abstract method responsible for generating the final SELECT statement.
	 *
	 * This statement retrieves results from the main CTE and converts them into the proper format.
	 *
	 * Subclasses must implement this method to define the logic for rendering
	 * the final output SELECT query.
	 */
	public abstract void renderFinalSelect(StringBuilder sb, boolean exclude);

	/**
	 * Abstract method responsible for providing the appropriate column builder
	 * to be used for selecting columns in the final SELECT statement.
	 *
	 * Subclasses must implement this method to supply a column builder
	 * tailored to their specific requirements.
	 */
	protected abstract <C1 extends AbstractTopologyColumnGenerator, B extends AbstractTopologyColumnGeneratorBuilder<C1, B>> B getFinalSelectColumnBuilder();

	protected boolean isAggregated() {
		return getQuery().getAggregation() != null;
	}

	protected boolean isRecursive() {
		return false;
	}

	protected AbstractTopologyColumnGenerator getFinalSelectColumnGenerator() {
		AbstractTopologyColumnGenerator columnGenerator = getFinalSelectColumnBuilder()
			.queryGeneratorContext(getGeneratorContext())
			.query(getQuery())
			.build();

		if (isAggregated()) {
			return AggregatedColumnGenerator.builder()
				.delegate(columnGenerator)
				.build();
		} else if (isRecursive()) {
			return RecursiveColumnGenerator.builder()
				.delegate(columnGenerator)
				.build();
		} else {
			return columnGenerator;
		}
	}

	protected boolean hasRecursion(String modelName, Collection<QueryLink> links) {
		return Optional.ofNullable(links).stream()
			.flatMap(Collection::stream)
			.anyMatch(ql -> Objects.equals(modelName, getGeneratorContext().getEnrichments().getTargetDocumentModel(ql))
				|| links.stream()
				.anyMatch(l -> hasRecursion(getGeneratorContext().getEnrichments().getTargetDocumentModel(l), l.getLinks())));
	}

	private void renderMainCte(StringBuilder sb) {
		generatorContext.registerCteAlias(getAlias());
		sb.append(getAlias()).append(QueryGeneratorConstants.AS_KEYWORD).append(QueryGeneratorConstants.OPENING_BRACKET);
		renderMainCteQuery(sb).append(QueryGeneratorConstants.CLOSING_BRACKET);
		generatorContext.unregisterCteAlias();
	}

	public abstract static class AbstractCteGeneratorBuilder<Q extends QueryTopology, C extends AbstractCteGenerator<Q>, B extends AbstractCteGeneratorBuilder<Q, C, B>> {}

}
