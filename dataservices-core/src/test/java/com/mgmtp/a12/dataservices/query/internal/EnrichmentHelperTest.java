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

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link EnrichmentHelper}, specifically testing ISO 8601 interval format handling
 * for date range operators on IDateRangeType fields.
 */
@Test
public class EnrichmentHelperTest extends AbstractQueryContextAwareTest {

	private static final String DATE_RANGE_FIELD_PATH = "/BusinessPartnerRoot/Offer/ValidityPeriod";
	private static final String DATE_FIELD_PATH = "/BusinessPartnerRoot/StartOfRelationship";
	private static final ExceptionKeys.ExecutionPhase EXECUTION_PHASE = ExceptionKeys.ExecutionPhase.QUERY_VALIDATION;

	private IDocumentModel documentModel;
	private QueryContext queryContext;

	@BeforeMethod
	public void setUp() {
		documentModel = documentModelResolver.getDocumentModelById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		queryContext = newQueryContext();
	}

	@Test(description = "Should split ISO 8601 interval value into from/to enrichments for IDateRangeType field")
	public void shouldSplitIntervalValueIntoFromToForDateRangeType() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_RANGE_FIELD_PATH)
			.value("2015-01-01/2020-12-31")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_RANGE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		// Dates are transformed to ISO_LOCAL_DATE_TIME format during enrichment
		assertEquals(enrichments.get(Enrichments.FROM_PROPERTY), "2015-01-01T00:00:00");
		assertEquals(enrichments.get(Enrichments.TO_PROPERTY), "2020-12-31T00:00:00");
		assertFalse(enrichments.containsKey(Enrichments.VALUE_PROPERTY));
		assertTrue(operator.isRangeType());
	}

	@Test(description = "Should handle open interval with from date only")
	public void shouldHandleOpenIntervalFromOnly() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_RANGE_FIELD_PATH)
			.value("2015-01-01/")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_RANGE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		assertEquals(enrichments.get(Enrichments.FROM_PROPERTY), "2015-01-01T00:00:00");
		assertNull(enrichments.get(Enrichments.TO_PROPERTY));
		assertFalse(enrichments.containsKey(Enrichments.VALUE_PROPERTY));
	}

	@Test(description = "Should handle open interval with to date only")
	public void shouldHandleOpenIntervalToOnly() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_RANGE_FIELD_PATH)
			.value("/2020-12-31")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_RANGE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		assertNull(enrichments.get(Enrichments.FROM_PROPERTY));
		assertEquals(enrichments.get(Enrichments.TO_PROPERTY), "2020-12-31T00:00:00");
		assertFalse(enrichments.containsKey(Enrichments.VALUE_PROPERTY));
	}

	@Test(description = "Should use explicit from/to properties when provided without interval value")
	public void shouldUseExplicitFromToProperties() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_RANGE_FIELD_PATH)
			.from("2015-01-01")
			.to("2020-12-31")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_RANGE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		assertEquals(enrichments.get(Enrichments.FROM_PROPERTY), "2015-01-01T00:00:00");
		assertEquals(enrichments.get(Enrichments.TO_PROPERTY), "2020-12-31T00:00:00");
	}

	@Test(description = "Should prioritize interval value over explicit from/to when both provided")
	public void shouldPrioritizeIntervalValueOverExplicitFromTo() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_RANGE_FIELD_PATH)
			.value("2015-01-01/2020-12-31")
			.from("2000-01-01")
			.to("2030-12-31")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_RANGE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		// Interval value should take precedence (computeIfAbsent behavior)
		assertEquals(enrichments.get(Enrichments.FROM_PROPERTY), "2015-01-01T00:00:00");
		assertEquals(enrichments.get(Enrichments.TO_PROPERTY), "2020-12-31T00:00:00");
	}

	@Test(description = "Should not split interval value for non-DateRangeType fields (IDateType)")
	public void shouldNotSplitIntervalValueForDateType() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_FIELD_PATH)
			.value("2015-01-01")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		// For IDateType, the value is enriched normally (transformed to ISO format)
		assertTrue(enrichments.containsKey(Enrichments.VALUE_PROPERTY));
		assertFalse(operator.isRangeType());
	}

	@Test(description = "Should handle single date value (no separator) on IDateRangeType field")
	public void shouldHandleSingleDateValueOnDateRangeType() {
		DateRangeOperator operator = DateRangeOperator.builder()
			.field(DATE_RANGE_FIELD_PATH)
			.value("2015-01-01")
			.build();

		IField field = documentModelUtils.findField(documentModel, DATE_RANGE_FIELD_PATH).orElseThrow();

		EnrichmentHelper.enrichDateRangeOperator(operator, field, documentModel, queryContext, EXECUTION_PHASE);

		Map<String, Object> enrichments = queryContext.getEnrichments().getOperatorEnrichment(operator);
		// Single date without separator should be treated as regular value (transformed to ISO format)
		assertEquals(enrichments.get(Enrichments.VALUE_PROPERTY), "2015-01-01T00:00:00");
		assertFalse(enrichments.containsKey(Enrichments.FROM_PROPERTY));
		assertFalse(enrichments.containsKey(Enrichments.TO_PROPERTY));
	}
}
