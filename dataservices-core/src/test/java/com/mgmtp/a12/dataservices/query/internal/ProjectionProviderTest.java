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
package com.mgmtp.a12.dataservices.query.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

/**
 * Unit tests for {@link ProjectionProvider} verifying
 *  - lookup by name
 *  - exception when missing
 *  - selection of highest precedence (lowest @Order value) among duplicates
 *  - behavior with multiple permutations (DataProvider driven)
 */
public class ProjectionProviderTest {

	@DataProvider
	public Object[][] projectionNames() {
		return new Object[][] {{"p1"}, {"p2"}};
	}

	@DataProvider
	public Object[][] duplicateOrderingScenarios() {
		// Provide different insertion orders – Spring would sort by @Order before injection.
		return new Object[][] {
			{Arrays.asList(new LowPrecedenceDupProjection(), new HighPrecedenceDupProjection(), new MediumPrecedenceDupProjection()), HighPrecedenceDupProjection.class},
			{Arrays.asList(new MediumPrecedenceDupProjection(), new LowPrecedenceDupProjection(), new HighPrecedenceDupProjection()), HighPrecedenceDupProjection.class},
			{Arrays.asList(new HighPrecedenceDupProjection(), new MediumPrecedenceDupProjection(), new LowPrecedenceDupProjection()), HighPrecedenceDupProjection.class},
			{Arrays.asList(new MediumPrecedenceDupProjection(), new LowPrecedenceDupProjection()), MediumPrecedenceDupProjection.class},
		};
	}

	@Test(dataProvider = "projectionNames", description = "Returns projection when name exists (using Mockito mocks)")
	public void shouldReturnProjectionWhenNameExists(String projectionName) {
		IQueryProjection<?> p1 = mock(IQueryProjection.class);
		when(p1.getId()).thenReturn("p1");
		IQueryProjection<?> p2 = mock(IQueryProjection.class);
		when(p2.getId()).thenReturn("p2");
		ProjectionProvider provider = new ProjectionProvider(List.of(p1, p2));

		IQueryProjection<?> result = provider.getMatchingProjection(projectionName);
		assertThat(result.getId()).isEqualTo(projectionName);
	}

	@Test(expectedExceptions = QueryInvalidInputException.class, description = "Throws when projection not found")
	public void shouldThrowExceptionWhenProjectionNotFound() {
		IQueryProjection<?> p1 = mock(IQueryProjection.class);
		when(p1.getId()).thenReturn("p1");
		ProjectionProvider provider = new ProjectionProvider(List.of(p1));
		provider.getMatchingProjection("missing");
	}

	@Test(dataProvider = "duplicateOrderingScenarios", description = "Selects highest precedence (lowest @Order) among multiple projections with same id")
	public void shouldPickHighestPrecedenceWhenDuplicatesPresent(List<IQueryProjection<?>> unsorted, Class<?> expectedClass) {
		List<IQueryProjection<?>> copy = new ArrayList<>(unsorted);
		// Simulate Spring's ordering of injected List<IQueryProjection<?>>
		AnnotationAwareOrderComparator.sort(copy);
		ProjectionProvider provider = new ProjectionProvider(copy);

		IQueryProjection<?> selected = provider.getMatchingProjection("dup");
		assertThat(selected).isInstanceOf(expectedClass);
	}

	// Concrete projection stubs with different precedence
	@QueryProjection("dup") @Order(10)
	static class LowPrecedenceDupProjection implements IQueryProjection<String> {
		@Override public QueryRoot preprocess(QueryRoot originalQuery, QueryContext context) { throw new UnsupportedOperationException(); }
		@Override public QueryPage<String> postprocess(QueryRoot originalQuery, Page<DocumentTreeResult> queryResult, QueryContext context) { throw new UnsupportedOperationException(); }
	}

	@QueryProjection("dup") @Order(5)
	static class MediumPrecedenceDupProjection implements IQueryProjection<String> {
		@Override public QueryRoot preprocess(QueryRoot originalQuery, QueryContext context) { throw new UnsupportedOperationException(); }
		@Override public QueryPage<String> postprocess(QueryRoot originalQuery, Page<DocumentTreeResult> queryResult, QueryContext context) { throw new UnsupportedOperationException(); }
	}

	@QueryProjection("dup") @Order(0) // highest precedence
	static class HighPrecedenceDupProjection implements IQueryProjection<String> {
		@Override public QueryRoot preprocess(QueryRoot originalQuery, QueryContext context) { throw new UnsupportedOperationException(); }
		@Override public QueryPage<String> postprocess(QueryRoot originalQuery, Page<DocumentTreeResult> queryResult, QueryContext context) { throw new UnsupportedOperationException(); }
	}
}
