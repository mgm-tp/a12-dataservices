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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for `HasOperatorEnricher`.
 */
public class HasOperatorEnricherTest extends AbstractQueryContextAwareTest {

	private ModelTypeService mockModelTypeService;
	private QueryAuthorizationService mockQueryAuthorizationService;
	private DocumentModelUtils mockDocumentModelUtils;
	private HasOperatorEnricher enricher;

	@BeforeMethod
	public void setUpEnricher() {
		mockModelTypeService = mock(ModelTypeService.class);
		mockQueryAuthorizationService = mock(QueryAuthorizationService.class);
		mockDocumentModelUtils = mock(DocumentModelUtils.class);
		enricher = new HasOperatorEnricher(mockModelTypeService, mockDocumentModelUtils, documentModelServiceFactory, mockQueryAuthorizationService);
	}

	/**
	 * Verifies that `enrich` returns `false` immediately when the operator is not a `HasOperator`,
	 * and that it does not modify any enrichment data.
	 */
	@Test(enabled = true, description = "Should return false for a non-HasOperator")
	public void shouldReturnFalseForNonHasOperator() {
		ExactMatchOperator<Object> nonHasOperator = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		boolean result = enricher.enrich(nonHasOperator, context);

		assertFalse(result, "enrich must return false for a non-HasOperator");
		// No model-type service calls must have been made.
		verify(mockModelTypeService, never()).findAllSubtypes(any());
	}

	/**
	 * Verifies that `enrich` returns `true` when the operator is a `HasOperator`.
	 */
	@Test(enabled = true, description = "Should return true for a HasOperator")
	public void shouldReturnTrueForHasOperator() {
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		// Stub ABAC service to return the constraint unchanged (null input returns null).
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		QueryContext context = newQueryContext();

		boolean result = enricher.enrich(hasOperator, context);

		assertTrue(result, "enrich must return true for a HasOperator");
	}

	/**
	 * Verifies that `enrich` stores the resolved target document model in the enrichments under
	 * the `HasOperator` key.
	 */
	@Test(enabled = true, description = "Should set the target document model in enrichments for a HasOperator")
	public void shouldSetTargetDocumentModelInEnrichmentsForHasOperator() {
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		QueryContext context = newQueryContext();

		enricher.enrich(hasOperator, context);

		// The Partner role in ContractBusinessPartner maps to BusinessPartnerSuper.
		assertEquals(
			context.getEnrichments().getTargetDocumentModel(hasOperator),
			DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL,
			"Target document model must be resolved from the relationship model and stored in enrichments");
	}

	/**
	 * Verifies that `enrich` stores the source role in the enrichments under the `HasOperator` key.
	 */
	@Test(enabled = true, description = "Should set the source role in enrichments for a HasOperator")
	public void shouldSetSourceRoleInEnrichmentsForHasOperator() {
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		QueryContext context = newQueryContext();

		enricher.enrich(hasOperator, context);

		// The source role (opposite side of Partner) in ContractBusinessPartner is Contract.
		assertEquals(
			context.getEnrichments().getSourceRole(hasOperator),
			RoleConstants.CONTRACT_ROLE,
			"Source role must be resolved from the relationship model and stored in enrichments");
	}

	/**
	 * Verifies that `enrich` triggers computation of model subtypes for the resolved target
	 * document model.
	 */
	@Test(enabled = true, description = "Should compute model subtypes for the target document model")
	public void shouldComputeModelSubtypesForTargetDocumentModel() {
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		Set<String> subtypes = Set.of(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL);
		when(mockModelTypeService.findAllSubtypes(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL))
			.thenReturn(subtypes);
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		QueryContext context = newQueryContext();

		enricher.enrich(hasOperator, context);

		assertNotNull(
			context.getEnrichments().getModelSubtypes(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL),
			"Model subtypes must be computed for the target document model");
		assertTrue(
			context.getEnrichments().getModelSubtypes(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL)
				.containsAll(subtypes),
			"Computed subtypes must match the value returned by ModelTypeService");
	}

	/**
	 * Verifies that `enrich` populates field types for the target document model so that
	 * subsequent SQL generation can resolve field types of the linked document.
	 */
	@Test(enabled = true, description = "Should enrich field types for the target document model")
	public void shouldEnrichFieldTypesForTargetDocumentModel() {
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		// Use the real document model utils because enrichFieldTypes delegates to it.
		enricher = new HasOperatorEnricher(mockModelTypeService, documentModelUtils, documentModelServiceFactory, mockQueryAuthorizationService);
		QueryContext context = newQueryContext();

		enricher.enrich(hasOperator, context);

		// After enrichment, at least one field type for the BusinessPartnerSuper model must be populated.
		// BusinessPartnerSuper has a Name field which is a StringType.
		assertNotNull(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getFieldType(),
			"Field types for the target document model must be populated after enrichment");
	}

	/**
	 * Verifies that `enrich` also computes model subtypes for the link document model when the
	 * relationship model specifies one.
	 */
	@Test(enabled = true, description = "Should compute model subtypes for the link document model when present")
	public void shouldComputeModelSubtypesForLinkDocumentModelWhenPresent() {
		// ContractCoInsuredPartner has a linkDocumentModel: CoInsuredAdditionalFields.
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		QueryContext context = newQueryContext();

		enricher.enrich(hasOperator, context);

		// Subtypes computation must have been triggered for the link document model.
		verify(mockModelTypeService).findAllSubtypes(DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL);
	}

	/**
	 * Verifies that `enrich` injects ABAC rules into the `HasOperator`'s constraint by calling
	 * `QueryAuthorizationService.addAbacRules()` and replacing the original constraint.
	 */
	@Test(enabled = true, description = "Should inject ABAC rules into the HasOperator constraint")
	public void shouldInjectAbacRulesIntoHasOperatorConstraint() {
		ILogicOperator originalConstraint = ExactMatchOperator.builder()
			.field("/BusinessPartnerRoot/Name")
			.value("Smith")
			.build();
		ILogicOperator abacEnrichedConstraint = AndOperator.builder()
			.operand(originalConstraint)
			.build();

		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.constraint(originalConstraint)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		// Stub ABAC service to return a different constraint (simulates injection of ABAC rule).
		when(mockQueryAuthorizationService.addAbacRules(eq(originalConstraint), eq(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL)))
			.thenReturn(abacEnrichedConstraint);
		QueryContext context = newQueryContext();

		enricher.enrich(hasOperator, context);

		assertEquals(
			hasOperator.getConstraint(),
			abacEnrichedConstraint,
			"HasOperator constraint must be replaced with the ABAC-enriched constraint");
	}

	/**
	 * Verifies that `HasOperatorEnricher` does not recurse into nested operators inside the
	 * `HasOperator`. Recursion is the responsibility of `QueryAPIOperatorWalker`, not of this enricher.
	 */
	@Test(enabled = true, description = "Should not recurse into nested operators inside the HasOperator")
	public void shouldNotRecurseIntoNestedOperators() {
		// A nested HasOperator inside the main HasOperator's constraint.
		HasOperator innerHasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.build();
		HasOperator outerHasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.constraint(innerHasOperator)
			.build();
		when(mockModelTypeService.findAllSubtypes(any())).thenReturn(Set.of());
		when(mockQueryAuthorizationService.addAbacRules(any(), any())).thenAnswer(inv -> inv.getArgument(0));
		QueryContext context = newQueryContext();

		enricher.enrich(outerHasOperator, context);

		// The inner HasOperator should NOT have a target document model set, because the enricher
		// must not recurse. Only the outer HasOperator is processed by this enricher.
		assertNull(
			context.getEnrichments().getTargetDocumentModel(innerHasOperator),
			"HasOperatorEnricher must not recurse into nested operators; that is the walker's responsibility");
	}
}
