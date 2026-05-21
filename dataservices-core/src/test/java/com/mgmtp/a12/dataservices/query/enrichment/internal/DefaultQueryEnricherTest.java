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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class DefaultQueryEnricherTest extends AbstractQueryContextAwareTest {
	@Mock DataServicesCoreProperties.Query queryProperties;

	@Test public void testEnrichSimpleQuery() {
		QueryRoot query = QueryRoot.builder().targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(AndOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field("/ContractRoot/ContractName")
					.value("Project Manhattan")
					.build()
				).build()).build();

		QueryContext queryContext = newQueryContext();
		queryEnricher.enrichQuery(query, queryContext);
		assertEquals(queryContext.getEnrichments().getFieldDescriptor("/ContractRoot/ContractName").getFieldType(),
			QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE);
	}

	@Test public void testEnrichQueryLink() {

		final int maxLinksSize = 10;
		when(dataServicesCoreProperties.getQuery()).thenReturn(queryProperties);
		when(queryProperties.getMaxLinksSize()).thenReturn(maxLinksSize);

		ExactMatchOperator<Object> operator1 = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("Project Manhattan")
			.build();
		ExactMatchOperator<Object> operator2 = ExactMatchOperator.builder()
			.field("/ContractRoot/ChangeLog/ChangeTimestamp")
			.value("2025-03-04")
			.build();
		AndOperator andOperator = AndOperator.builder()
			.operand(operator1)
			.operand(operator2)
			.build();

		QueryLink input = QueryLink.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.CONTRACT_ROLE)
			.constraint(andOperator)
			.backReference("backReference2")
			.ordered(true)
			.build();

		doReturn(null).when(queryAuthorizationService).addAbacRules(Mockito.isNull(), Mockito.eq("Contract"));
		doReturn(andOperator).when(queryAuthorizationService).addAbacRules(Mockito.eq(andOperator), Mockito.eq("Contract"));

		QueryContext queryContext = newQueryContext();
		queryEnricher.enrichQuery(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.link(input).build(), queryContext);
		assertEquals(queryContext.getEnrichments().getSourceRole(input), RoleConstants.PARTNER_ROLE);
		assertEquals(queryContext.getEnrichments().getTargetDocumentModel(input), DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		assertEquals(queryContext.getEnrichments().getFieldDescriptor("/ContractRoot/ContractName").getRepeatable(), false);
		assertEquals(queryContext.getEnrichments().getFieldDescriptor("/ContractRoot/ChangeLog/ChangeTimestamp").getRepeatable(), true);
		assertEquals(input.getMaxLinksSize(), maxLinksSize + 1);
	}

	@Test(description = "Populates field types for all discovered field paths when missing")
	public void shouldPopulateFieldTypesWhenMissing() {
		QueryContext ctx = newQueryContext();
		String path = "/ContractRoot/ContractName";
		FieldDescriptor descriptor = ctx.getEnrichments().getFieldDescriptor(path);
		assertNull(descriptor.getFieldType(), "Precondition: field type should be null before enrichment");

		queryEnricher.enrichFieldTypes(ctx, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);

		assertEquals(ctx.getEnrichments().getFieldDescriptor(path).getFieldType(), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE);
	}

	@Test(description = "Fields from sub types of the target document model are considered for enrichment")
	public void shouldEnrichFromSubType() {
		QueryContext ctx = newQueryContext();
		String pathMake = "/ContractRoot/Car/Make";
		String pathModel = "/ContractRoot/Car/Model";
		FieldDescriptor descriptorModel = ctx.getEnrichments().getFieldDescriptor(pathModel);
		FieldDescriptor descriptorMake = ctx.getEnrichments().getFieldDescriptor(pathMake);
		// Preconditions
		assertNull(descriptorModel.getFieldType(), "Precondition: field type should be null before enrichment");
		assertNull(descriptorMake.getFieldType(), "Precondition: field type should be null before enrichment");

		// Mock model-graph
		when(modelTypeService.findAllSubtypes(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)).thenReturn(Set.of(DocumentModelConstants.CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL));

		queryEnricher.enrichFieldTypes(ctx, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);

		assertEquals(ctx.getEnrichments().getFieldDescriptor(pathModel).getFieldType(), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE);
		assertEquals(ctx.getEnrichments().getFieldDescriptor(pathMake).getFieldType(), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE);
	}

	@Test(description = "Does not override an already set field type")
	public void shouldNotOverrideExistingFieldType() {
		QueryContext ctx = newQueryContext();
		String path = "/ContractRoot/ContractName";
		FieldDescriptor descriptor = ctx.getEnrichments().getFieldDescriptor(path);
		descriptor.setFieldType("PRESET_TYPE");

		queryEnricher.enrichFieldTypes(ctx, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);

		assertEquals(ctx.getEnrichments().getFieldDescriptor(path).getFieldType(), "PRESET_TYPE");
	}
}
