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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for `QueryAPIOperatorWalker`.
 */
public class QueryAPIOperatorWalkerTest extends AbstractQueryContextAwareTest {

	private QueryAPIOperatorWalker walker;

	@BeforeMethod
	public void setUpWalker() {
		// Construct the walker with no registered enrichers; individual tests add enrichers as needed.
		walker = new QueryAPIOperatorWalker(List.of(), documentModelUtils, modelTypeService);
	}

	/**
	 * Verifies that `walkConstraint` returns immediately without throwing when the constraint
	 * argument is `null`.
	 */
	@Test(enabled = true, description = "Should not throw when constraint is null")
	public void shouldNotThrowWhenConstraintIsNull() {
		QueryContext context = newQueryContext();
		// Must not throw NullPointerException or any other exception.
		walker.walkConstraint(null, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);
	}

	/**
	 * Verifies that when a `FieldAwareOperator` is walked, the walker enriches the repeatability
	 * flag on the field descriptor and populates the field type.
	 */
	@Test(enabled = true, description = "Should enrich repeatability and field type when a FieldAwareOperator is walked")
	public void shouldEnrichRepeatabilityAndFieldTypeWhenFieldAwareOperatorIsWalked() {
		ExactMatchOperator<Object> operator = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor("/ContractRoot/ContractName");
		assertNotNull(fieldDescriptor.getRepeatable(), "Repeatable flag must be set after walking a FieldAwareOperator");
		assertFalse(fieldDescriptor.getRepeatable(), "ContractRoot/ContractName is not inside a repeatable group");
		assertEquals(fieldDescriptor.getFieldType(), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"Field type must be resolved to STRING for a string field");
	}

	/**
	 * Verifies that when a `SimpleSearchOperator` is walked, the walker resolves the field type
	 * directly from the document model and populates the fields-to-type map on the operator.
	 */
	@Test(enabled = true, description = "Should populate the fields-types map when a SimpleSearchOperator is walked")
	public void shouldPopulateFieldTypesMapWhenSimpleSearchOperatorIsWalked() {
		String field = "/ContractRoot/ContractName";
		SimpleSearchOperator operator = SimpleSearchOperator.builder()
			.fields(List.of(field))
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		assertNotNull(operator.getFieldsTypes(), "Fields-types map must not be null after walking");
		assertEquals(operator.getFieldsTypes().get(field), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"The fields-types map must contain the resolved type for the field");
	}

	/**
	 * Verifies that when a `SimpleSearchOperator` is nested inside a link constraint (the constraint
	 * of a `HasOperator`), the walker descends into the child constraint using the link target
	 * document model and resolves the field type against that target model rather than the outer
	 * model in scope at the root.
	 */
	@Test(enabled = true, description = "Should resolve field type against link target model when SimpleSearchOperator is in a link constraint")
	public void shouldResolveFieldTypeAgainstLinkTargetModelWhenSimpleSearchIsInLinkConstraint() {
		// The field exists in the link target model (BusinessPartner), not in the outer model (Contract).
		String field = "/BusinessPartnerRoot/Name";
		SimpleSearchOperator simpleSearch = SimpleSearchOperator.builder()
			.fields(List.of(field))
			.value("test")
			.build();
		// Nest the SimpleSearchOperator inside the constraint of a HasOperator (the link).
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.constraint(simpleSearch)
			.build();

		QueryContext context = newQueryContext();
		// Pre-register the link target document model for the HasOperator, simulating HasOperatorEnricher.
		context.getEnrichments().setTargetDocumentModel(hasOperator, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		when(modelTypeService.findAllSubtypes(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL))
			.thenReturn(Set.of());

		// Walk from the outer Contract model; the walker must switch to the link target model for the child.
		walker.walkConstraint(hasOperator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		assertNotNull(simpleSearch.getFieldsTypes(), "Fields-types map must not be null after walking");
		assertEquals(simpleSearch.getFieldsTypes().get(field), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"Field type must be resolved to STRING against the link target model, not the outer model");
	}

	/**
	 * Verifies that when a `SimpleSearchOperator` is inside a link document constraint, the walker
	 * resolves the field type against the link document model.
	 */
	@Test(enabled = true, description = "Should resolve field type against link document model when SimpleSearchOperator is in a link document constraint")
	public void shouldResolveFieldTypeAgainstLinkDocumentModelWhenSimpleSearchIsInLinkDocumentConstraint() {
		String field = "/CoInsuredRoot/Name";
		SimpleSearchOperator operator = SimpleSearchOperator.builder()
			.fields(List.of(field))
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL, context);

		assertNotNull(operator.getFieldsTypes(), "Fields-types map must not be null after walking");
		assertEquals(operator.getFieldsTypes().get(field), QueryGeneratorConstants.FieldTypes.STRING_FIELD_TYPE,
			"Field type must be resolved to STRING for a string field in the link document model");
	}

	/**
	 * Verifies that when a `SimpleSearchOperator` references a field that does not exist in the
	 * document model, the walker omits that field from the fields-types map without throwing.
	 */
	@Test(enabled = true, description = "Should omit field from fields-types map when field is not found in the model")
	public void shouldOmitFieldFromFieldsTypesWhenFieldIsNotFoundInModel() {
		String field = "/NonExistent/Field";
		SimpleSearchOperator operator = SimpleSearchOperator.builder()
			.fields(List.of(field))
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		assertNotNull(operator.getFieldsTypes(), "Fields-types map must not be null even when field is not found");
		assertEquals(operator.getFieldsTypes().size(), 0,
			"Fields-types map must be empty when the field does not exist in the model");
	}

	/**
	 * Verifies that when a `SimpleSearchOperator` references an enumeration field in the link
	 * document model, the walker resolves the field type to `IEnumerationType`.
	 */
	@Test(enabled = true, description = "Should resolve enumeration type when simple search field is an enumeration in the link document model")
	public void shouldResolveEnumerationTypeWhenSimpleSearchFieldIsEnumerationInLinkDocumentModel() {
		String field = "/CoInsuredRoot/Role";
		SimpleSearchOperator operator = SimpleSearchOperator.builder()
			.fields(List.of(field))
			.value("EXP")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL, context);

		assertNotNull(operator.getFieldsTypes(), "Fields-types map must not be null after walking");
		assertEquals(operator.getFieldsTypes().get(field), QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE,
			"Field type must be resolved to ENUMERATION for an enumeration field in the link document model");
	}

	/**
	 * Verifies that when a `SimpleSearchOperator` is walked with a `null` target document model,
	 * the walker produces an empty fields-types map without throwing.
	 */
	@Test(enabled = true, description = "Should produce empty fields-types map when target document model is null")
	public void shouldProduceEmptyFieldsTypesWhenTargetDocumentModelIsNull() {
		String field = "/CoInsuredRoot/Name";
		SimpleSearchOperator operator = SimpleSearchOperator.builder()
			.fields(List.of(field))
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, null, context);

		assertNotNull(operator.getFieldsTypes(), "Fields-types map must not be null even when target document model is null");
		assertEquals(operator.getFieldsTypes().size(), 0,
			"Fields-types map must be empty when target document model is null");
	}

	/**
	 * Verifies that when a `VariadicOperator` is walked, the walker recurses into each of its
	 * operands and enriches them.
	 */
	@Test(enabled = true, description = "Should walk all operands when a VariadicOperator is walked")
	public void shouldWalkAllOperandsWhenVariadicOperatorIsWalked() {
		ExactMatchOperator<Object> op1 = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("first")
			.build();
		ExactMatchOperator<Object> op2 = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("second")
			.build();
		AndOperator and = AndOperator.builder().operand(op1).operand(op2).build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(and, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		// Both operands are FieldAwareOperators; their repeatability must have been set.
		assertNotNull(context.getEnrichments().getFieldDescriptor("/ContractRoot/ContractName").getRepeatable(),
			"Walker must have recursed into operands of the VariadicOperator");
	}

	/**
	 * Verifies that when a `NestingOperator` is walked, the walker recurses into its single
	 * operand and enriches it.
	 */
	@Test(enabled = true, description = "Should walk the operand when a NestingOperator is walked")
	public void shouldWalkOperandWhenNestingOperatorIsWalked() {
		ExactMatchOperator<Object> inner = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("test")
			.build();
		NotOperator not = NotOperator.builder().operand(inner).build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(not, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		assertNotNull(context.getEnrichments().getFieldDescriptor("/ContractRoot/ContractName").getRepeatable(),
			"Walker must have recursed into the operand of the NestingOperator");
	}

	/**
	 * Verifies that all registered enrichers are called for each operator, even if one of them
	 * returns `false` (not applicable).
	 */
	@Test(enabled = true, description = "Should call all registered enrichers for each operator")
	public void shouldCallAllRegisteredEnrichersForEachOperator() {
		IQueryAPIOperatorEnricher enricher1 = mock(IQueryAPIOperatorEnricher.class);
		IQueryAPIOperatorEnricher enricher2 = mock(IQueryAPIOperatorEnricher.class);
		when(enricher1.enrich(any(), any())).thenReturn(true);
		when(enricher2.enrich(any(), any())).thenReturn(true);

		walker = new QueryAPIOperatorWalker(List.of(enricher1, enricher2), documentModelUtils, modelTypeService);

		ExactMatchOperator<Object> operator = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		verify(enricher1, atLeastOnce()).enrich(eq(operator), eq(context));
		verify(enricher2, atLeastOnce()).enrich(eq(operator), eq(context));
	}

	/**
	 * Verifies that when one enricher returns `false`, the walker still calls all subsequent
	 * enrichers in the list.
	 */
	@Test(enabled = true, description = "Should continue calling other enrichers when one returns false")
	public void shouldContinueCallingOtherEnrichersWhenOneReturnsFalse() {
		IQueryAPIOperatorEnricher enricher1 = mock(IQueryAPIOperatorEnricher.class);
		IQueryAPIOperatorEnricher enricher2 = mock(IQueryAPIOperatorEnricher.class);
		// First enricher declines to handle this operator.
		when(enricher1.enrich(any(), any())).thenReturn(false);
		when(enricher2.enrich(any(), any())).thenReturn(true);

		walker = new QueryAPIOperatorWalker(List.of(enricher1, enricher2), documentModelUtils, modelTypeService);

		ExactMatchOperator<Object> operator = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("test")
			.build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(operator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		// Both enrichers must be called regardless of the return value.
		verify(enricher1).enrich(eq(operator), eq(context));
		verify(enricher2).enrich(eq(operator), eq(context));
	}

	/**
	 * Verifies that the walker continues its descent into child nodes even after an enricher
	 * returns `true` for a parent operator.
	 */
	@Test(enabled = true, description = "Should continue descent after enricher returns true")
	public void shouldContinueDescentAfterEnricherReturnsTrue() {
		IQueryAPIOperatorEnricher enricher = mock(IQueryAPIOperatorEnricher.class);
		when(enricher.enrich(any(), any())).thenReturn(true);

		walker = new QueryAPIOperatorWalker(List.of(enricher), documentModelUtils, modelTypeService);

		ExactMatchOperator<Object> child = ExactMatchOperator.builder()
			.field("/ContractRoot/ContractName")
			.value("test")
			.build();
		AndOperator parent = AndOperator.builder().operand(child).build();
		QueryContext context = newQueryContext();

		walker.walkConstraint(parent, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		// Enricher must be called for both the parent (AndOperator) and the child (ExactMatchOperator).
		verify(enricher, atLeastOnce()).enrich(eq(parent), eq(context));
		verify(enricher, atLeastOnce()).enrich(eq(child), eq(context));
	}

	/**
	 * Verifies that when a `ConstraintAware` operator (i.e., `HasOperator`) is walked, the walker
	 * recurses into the child constraint using the child target document model resolved from the
	 * enrichments. The child constraint operators must be enriched with the resolved model.
	 */
	@Test(enabled = true, description = "Should walk child constraint with child target document model when a ConstraintAware operator is walked")
	public void shouldWalkChildConstraintWithChildTargetDocumentModelWhenConstraintAwareOperatorIsWalked() {
		// A HasOperator that has a child ExactMatchOperator inside its constraint.
		ExactMatchOperator<Object> childOp = ExactMatchOperator.builder()
			.field("/BusinessPartnerRoot/Name")
			.value("Smith")
			.build();
		HasOperator hasOperator = HasOperator.builder()
			.relationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL)
			.targetRole(RoleConstants.PARTNER_ROLE)
			.constraint(childOp)
			.build();

		QueryContext context = newQueryContext();
		// Pre-register the target document model for the HasOperator in the enrichments
		// to simulate what HasOperatorEnricher sets.
		context.getEnrichments().setTargetDocumentModel(hasOperator, DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		when(modelTypeService.findAllSubtypes(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL))
			.thenReturn(Set.of());

		walker.walkConstraint(hasOperator, DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, context);

		// The child ExactMatchOperator is a FieldAwareOperator; its repeatability must have been set,
		// proving the walker descended into the child constraint.
		assertNotNull(
			context.getEnrichments().getFieldDescriptor("/BusinessPartnerRoot/Name").getRepeatable(),
			"Walker must recurse into the child constraint of a ConstraintAware operator");
	}
}
