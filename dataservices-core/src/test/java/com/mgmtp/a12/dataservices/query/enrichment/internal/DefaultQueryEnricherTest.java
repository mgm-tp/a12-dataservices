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

import java.util.List;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

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

		FieldTypeEnrichmentHelper.enrichFieldTypes(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			ctx, modelTypeService, documentModelUtils, documentModelServiceFactory);

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
		when(modelTypeService.findAllSubtypes(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)).thenReturn(
			Set.of(DocumentModelConstants.CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL));

		FieldTypeEnrichmentHelper.enrichFieldTypes(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			ctx, modelTypeService, documentModelUtils, documentModelServiceFactory);

		assertEquals(ctx.getEnrichments().getFieldDescriptor(pathModel).getFieldType(), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE);
		assertEquals(ctx.getEnrichments().getFieldDescriptor(pathMake).getFieldType(), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE);
	}

	@Test(description = "Does not override an already set field type")
	public void shouldNotOverrideExistingFieldType() {
		QueryContext ctx = newQueryContext();
		String path = "/ContractRoot/ContractName";
		FieldDescriptor descriptor = ctx.getEnrichments().getFieldDescriptor(path);
		descriptor.setFieldType("PRESET_TYPE");

		FieldTypeEnrichmentHelper.enrichFieldTypes(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			ctx, modelTypeService, documentModelUtils, documentModelServiceFactory);

		assertEquals(ctx.getEnrichments().getFieldDescriptor(path).getFieldType(), "PRESET_TYPE");
	}

	/**
	 * Verifies that `DefaultQueryEnricher` delegates to `QueryAPIOperatorWalker` and
	 * `HasOperatorEnricher` so that an end-to-end `enrichQuery` call for a query containing a
	 * `HasOperator` produces the expected enrichment results:
	 *
	 * - The target document model for the `HasOperator` is resolved and stored in the enrichments.
	 * - The source role for the `HasOperator` is resolved and stored in the enrichments.
	 * - Field types for the linked document model are populated.
	 */
	@Test(description = "Should enrich query containing a HasOperator using the new architecture")
	public void shouldEnrichQueryWithHasOperatorUsingNewArchitecture() {
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		QueryRoot queryRoot = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(hasOperator)
			.build();

		QueryContext context = newQueryContext();
		queryEnricher.enrichQuery(queryRoot, context);

		// Target document model for the HasOperator must be set by HasOperatorEnricher via the walker.
		assertEquals(
			context.getEnrichments().getTargetDocumentModel(hasOperator),
			DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL,
			"Target document model must be resolved and stored for the HasOperator");

		// Source role for the HasOperator must be set.
		assertEquals(
			context.getEnrichments().getSourceRole(hasOperator),
			RoleConstants.CONTRACT_ROLE,
			"Source role must be resolved and stored for the HasOperator");

		// Field types for the linked BusinessPartnerSuper model must be populated.
		assertNotNull(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			"Field types for the linked document model must be populated");
	}

	@Test(description = "Should enrich field type correctly when field path has a leading slash")
	public void shouldEnrichFieldTypeWhenFieldPathHasLeadingSlash() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(ModelConstants.FIELD_SEPARATOR + DocumentModelConstants.STATUS_FIELD_PATH)
				.value("draft")
				.build())
			.build();
		QueryContext context = newQueryContext();

		queryEnricher.enrichQuery(query, context);

		FieldDescriptor descriptor = context.getEnrichments().getFieldDescriptor(ModelConstants.FIELD_SEPARATOR + DocumentModelConstants.STATUS_FIELD_PATH);
		assertNotNull(descriptor.getFieldType());
	}

	@Test(description = "Should enrich repeatability correctly when field path has a leading slash")
	public void shouldEnrichRepeatabilityWhenFieldPathHasLeadingSlash() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(ModelConstants.FIELD_SEPARATOR + DocumentModelConstants.STATUS_FIELD_PATH)
				.value("draft")
				.build())
			.build();
		QueryContext context = newQueryContext();

		queryEnricher.enrichQuery(query, context);

		FieldDescriptor descriptor = context.getEnrichments().getFieldDescriptor(ModelConstants.FIELD_SEPARATOR + DocumentModelConstants.STATUS_FIELD_PATH);
		assertTrue(descriptor.getRepeatable());
	}

	@Test(description = "Should throw QueryInvalidInputException during enrichment when the field path has no leading slash")
	public void shouldThrowWhenFieldPathHasNoLeadingSlash() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(DocumentModelConstants.STATUS_FIELD_PATH)
				.value("draft")
				.build())
			.build();
		QueryContext context = newQueryContext();

		assertThrows(QueryInvalidInputException.class, () -> queryEnricher.enrichQuery(query, context));
	}

	@Test(description = "Should throw QueryInvalidInputException when root projection field has no leading slash")
	public void shouldThrowWhenRootProjectionFieldHasNoLeadingSlash() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.field(DocumentModelConstants.STATUS_FIELD_PATH)
			.build();
		QueryContext context = newQueryContext();

		assertThrows(QueryInvalidInputException.class, () -> queryEnricher.enrichQuery(query, context));
	}

	@Test(description = "Should throw QueryInvalidInputException when link projection field has no leading slash")
	public void shouldThrowWhenLinkProjectionFieldHasNoLeadingSlash() {
		QueryLink link = QueryLink.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.field(DocumentModelConstants.STATUS_FIELD_PATH)
			.build();
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.link(link)
			.build();
		QueryContext context = newQueryContext();

		assertThrows(QueryInvalidInputException.class, () -> queryEnricher.enrichQuery(query, context));
	}

	@Test(description = "Should throw QueryInvalidInputException when link linkDocumentFields has no leading slash")
	public void shouldThrowWhenLinkDocumentFieldHasNoLeadingSlash() {
		QueryLink link = QueryLink.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.linkDocumentFields(List.of(DocumentModelConstants.STATUS_FIELD_PATH))
			.build();
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.link(link)
			.build();
		QueryContext context = newQueryContext();

		assertThrows(QueryInvalidInputException.class, () -> queryEnricher.enrichQuery(query, context));
	}

	@Test(description = "Should accept valid locale when enriching simple search query")
	public void shouldAcceptValidLocaleWhenEnrichingSimpleSearch() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(SimpleSearchOperator.builder().value("foo").build())
			.build();
		QueryContext context = newQueryContextWithLocale(DocumentModelConstants.SearchConstants.EN_LOCALE);

		queryEnricher.enrichQuery(query, context);

		assertEquals(
			context.getEnrichments().getModelLocale(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL),
			DocumentModelConstants.SearchConstants.EN_LOCALE);
	}

	@Test(description = "Should accept blank locale when enriching simple search query")
	public void shouldAcceptBlankLocaleWhenEnrichingSimpleSearch() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(SimpleSearchOperator.builder().value("foo").build())
			.build();
		QueryContext context = newQueryContextWithLocale(null);

		queryEnricher.enrichQuery(query, context);

		assertNull(context.getEnrichments().getModelLocale(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL));
	}

	@Test(description = "Should throw QueryInvalidInputException when locale is completely unsupported")
	public void shouldThrowWhenLocaleIsUnsupportedWhenEnrichingSimpleSearch() {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(SimpleSearchOperator.builder().value("foo").build())
			.build();
		QueryContext context = newQueryContextWithLocale("fr");

		assertThrows(QueryInvalidInputException.class, () -> queryEnricher.enrichQuery(query, context));
	}
}
