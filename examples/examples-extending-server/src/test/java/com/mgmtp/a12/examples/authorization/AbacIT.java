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
package com.mgmtp.a12.examples.authorization;

import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.VariadicOperator;
import com.mgmtp.a12.dataservices.utils.QueryAssertions;
import com.mgmtp.a12.dataservices.utils.QueryUtils;
import java.util.List;

import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryRepository;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.examples.AbstractITBase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.ADDRESS_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;

/**
 * Integration tests for ABAC (Attribute-Based Access Control) authorization in Data Services.
 *
 * These tests verify that ABAC rules correctly enrich queries with authorization constraints
 * based on user attributes. The test suite covers:
 * - Contract filtering by type and value
 * - Business partner filtering by income
 * - Address filtering by location
 * - Complex queries with links and nested HasOperators
 *
 * Test Strategy: Each test verifies both the enriched query structure (ABAC constraint injection)
 * and the actual query results to ensure authorization rules are correctly applied end-to-end.
 *
 * Test Data Structure (created via application properties):
 * Contract:HealthHighValue (value 50.000)
 * Contract:Health (value 10.000)
 *   ├─ BusinessPartner:Peter
 *   ├─ CoInsuredPartner:Fritz
 *   └─ CoInsuredPartner:Nicole
 * Contract:Travel
 *   └─ BusinessPartner:Peter
 * 
 * BusinessPartner:Peter (income 4.000)
 *   ├─ Address:Berlin
 *   └─ Address:Hamburg
 * BusinessPartner:Olivia (income 8.000)
 *   └─ Address:Prague
 * BusinessPartner:Nicole (income 12.000)
 *   └─ Address:Paris
 * BusinessPartner:Fritz (income 1.000)
 * 
 * ABAC Rules (from abacAuthorizationDefinition.json):
 * - Contracts: Exclude type "Travel", filter by max value based on user
 * - BusinessPartners: Filter by minimum income based on user
 * - Addresses: Exclude certain cities based on user
 */
@ActiveProfiles({ InsuranceUserConfiguration.DATASERVICES_EXAMPLE_AUTHORIZATION_UAA_ENV })
public class AbacIT extends AbstractITBase {

	// ==================== Constants ====================
	
	/**
	 * Field paths used in query constraints.
	 */
	private static final class FieldPaths {
		static final String CONTRACT_TYPE = "/ContractRoot/Type";
		static final String CONTRACT_VALUE = "/ContractRoot/ContractValue";
		static final String BP_INCOME = "/BusinessPartnerRoot/Employment/income";
		static final String BP_NAME = "/BusinessPartnerRoot/Name";
		static final String ADDRESS_CITY = "/AddressRoot/City";
		static final String ADDRESS_COUNTRY = "/AddressRoot/Country";
	}
	
	/**
	 * Relationship model names used in links and HasOperators.
	 */
	private static final class RelationshipModels {
		static final String CONTRACT_BUSINESS_PARTNER = "ContractBusinessPartner";
		static final String CONTRACT_COINSURED_PARTNER = "ContractCoInsuredPartner";
		static final String PARTNER_ADDRESSES = "PartnerAddresses";
	}
	
	/**
	 * Role names used in query links.
	 */
	private static final class Roles {
		static final String PARTNER = "Partner";
		static final String ADDRESS = "Address";
	}

	// ==================== Test Infrastructure ====================

	@Autowired
	private QueryService queryService;

	@MockitoSpyBean
	private QueryRepository queryRepository;

	private final ArgumentCaptor<QueryRoot> queryRootCaptor = ArgumentCaptor.forClass(QueryRoot.class);

	@Override
	@BeforeClass
	public void prepareTestEnvironment() {
		// Test data is prepared via application properties
		setUserTo(UserConstants.ADMIN_USER);
	}

	@AfterClass
	public void cleanUpTestEnvironment() {
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager);
	}

	// ==================== Data Providers ====================

	/**
	 * Provides test data for contract value limit scenarios.
	 * Each row contains: user, max contract value, expected result count.
	 */
	@DataProvider(name = "contractValueLimits")
	public Object[][] contractValueLimits() {
		return new Object[][] {
			{ "insurance100-5-B", 100000.0, 5000.0 },
			{ "insurance15-5-B", 15000.0, 5000.0 },
			{ "insurance15-3-B", 15000.0, 3000.0 }
		};
	}

	// ==================== Test Methods ====================

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should add NOT Travel AND value constraints based on user contract value limits"
	)
	public void shouldEnforceContractValueLimitsForUser(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A basic contract query
		QueryRoot queryRoot = QueryUtils.createBasicQueryRoot(CONTRACT_DOCUMENT_MODEL);

		// When: Query is executed with ABAC enforcement
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: ABAC adds both authorization constraints
		QueryAssertions.assertThat(enrichedRoot)
			.hasTargetModel(CONTRACT_DOCUMENT_MODEL)
			.hasAndConstraint()
			.withOperandCount(2)
			.containsNotOperator(FieldPaths.CONTRACT_TYPE, "Travel")
			.containsRangeOperator(FieldPaths.CONTRACT_VALUE, null, maxContractValue);
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should filter business partners by minimum income threshold"
	)
	public void shouldFilterBusinessPartnersByMinimumIncome(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A basic business partner query
		QueryRoot queryRoot = QueryUtils.createBasicQueryRoot(BUSINESS_PARTNER_DOCUMENT_MODEL);

		// When: Query is executed with ABAC enforcement for user with income threshold
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: ABAC adds income constraint (>= 5000)
		assertNotNull(enrichedRoot.getConstraint(), "ABAC should add income constraint");
		assertDoubleRangeOperator(enrichedRoot.getConstraint(), FieldPaths.BP_INCOME, minPartnerIncome, null);
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should create Contract query with both included and excluded Type value"
	)
	public void shouldFilterContractByRestrictedValue(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A Contract query with queried Type
		QueryRoot queryRoot = QueryRoot.builder()
				.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
				.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
				.paging(Paging.builder().pageNumber(0).pageSize(10).build())
				.constraint(ExactMatchOperator.builder()
						.field("/ContractRoot/Type")
						.value("Travel")
						.build())
				.build();

		// When: Query is executed with ABAC enforcement for Type
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: Query contains both exact match for same enumeration value at the same field
		assertNotNull(enrichedRoot.getConstraint());
		AndOperator andConstraint = (AndOperator) enrichedRoot.getConstraint();
		assertEquals(andConstraint.getOperands().size(), 3);

		assertContainsNotOperatorWithExactMatch(andConstraint, FieldPaths.CONTRACT_TYPE, "Travel");
		assertContainsExactMatch(andConstraint, FieldPaths.CONTRACT_TYPE, "Travel");
		assertContainsDoubleRangeOperator(andConstraint, FieldPaths.CONTRACT_VALUE, null, maxContractValue);
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should exclude addresses in restricted cities when querying with links"
	)
	public void shouldExcludeRestrictedCitiesInAddressLinks(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A query for Peter's business partner with address links
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(BUSINESS_PARTNER_DOCUMENT_MODEL)
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.constraint(ExactMatchOperator.builder()
				.field(FieldPaths.BP_NAME)
				.value("Peter")
				.build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModels.PARTNER_ADDRESSES)
				.targetRole(Roles.ADDRESS)
				.build())
			.build();

		// When: Query is executed with city restrictions
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: Root constraint includes Peter filter and income constraint
		assertNotNull(enrichedRoot.getConstraint(), "ABAC should add constraints");
		assertEquals(enrichedRoot.getConstraint().getClass(), AndOperator.class);
		
		AndOperator andConstraint = (AndOperator) enrichedRoot.getConstraint();
		assertEquals(andConstraint.getOperands().size(), 2, "Expected 2 constraints");
		
		assertContainsExactMatch(andConstraint, FieldPaths.BP_NAME, "Peter");
		assertContainsDoubleRangeOperator(andConstraint, FieldPaths.BP_INCOME, minPartnerIncome, null);

		// And: Link constraint excludes Berlin addresses
		List<QueryLink> queryLinks = enrichedRoot.getLinks().stream().toList();
		assertEquals(queryLinks.size(), 1, "Expected exactly 1 link definition");

		QueryLink firstLevelLink = queryLinks.getFirst();
		assertEquals(firstLevelLink.getConstraint().getClass(), NotOperator.class);
		NotOperator notOperator = (NotOperator) firstLevelLink.getConstraint();

		assertEquals(notOperator.getOperand().getClass(), ExactMatchOperator.class);
		ExactMatchOperator<?> addressConstraint = (ExactMatchOperator<?>) notOperator.getOperand();
		assertEquals(addressConstraint.getField(), FieldPaths.ADDRESS_CITY);
		assertEquals(addressConstraint.getValue(), "Berlin");
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should apply city restrictions in combination with OR operator"
	)
	public void shouldApplyCityRestrictionsWithOrOperator(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A query for addresses in Germany OR France
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(ADDRESS_DOCUMENT_MODEL)
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.constraint(OrOperator.builder()
				.operands(List.of(
					ExactMatchOperator.builder()
						.field(FieldPaths.ADDRESS_COUNTRY)
						.value("Germany")
						.build(),
					ExactMatchOperator.builder()
						.field(FieldPaths.ADDRESS_COUNTRY)
						.value("France")
						.build()
				))
				.build())
			.build();

		// When: Query is executed with city restrictions
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: ABAC adds AND constraint combining OR clause with city exclusion
		AndOperator andConstraint = (AndOperator) enrichedRoot.getConstraint();

		// Verify OR constraint for countries
		OrOperator orConstraint = findOperatorOfType(andConstraint, OrOperator.class);
		assertEquals(orConstraint.getOperands().size(), 2, "Expected 2 operands in OR constraint");
		assertOperatorInVariadicOperator(orConstraint, 
			ExactMatchOperator.builder().field(FieldPaths.ADDRESS_COUNTRY).value("Germany").build(), 
			"OR should contain Germany constraint");
		assertOperatorInVariadicOperator(orConstraint, 
			ExactMatchOperator.builder().field(FieldPaths.ADDRESS_COUNTRY).value("France").build(), 
			"OR should contain France constraint");

		// Verify NOT Berlin constraint
		assertContainsNotOperatorWithExactMatch(andConstraint, FieldPaths.ADDRESS_CITY, "Berlin");
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should apply authorization constraints in nested HasOperator for addresses"
	)
	public void shouldApplyAuthorizationInNestedHasOperatorForAddresses(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A query for contracts with business partners having addresses in Germany
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.constraint(HasOperator.builder()
				.relationshipModel(RelationshipModels.CONTRACT_BUSINESS_PARTNER)
				.targetRole(Roles.PARTNER)
				.constraint(HasOperator.builder()
					.relationshipModel(RelationshipModels.PARTNER_ADDRESSES)
					.targetRole(Roles.ADDRESS)
					.constraint(ExactMatchOperator.builder()
						.field(FieldPaths.ADDRESS_COUNTRY)
						.value("Germany")
						.build())
					.build())
				.build())
			.build();

		// When: Query is executed with ABAC enforcement
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: Root constraint includes contract restrictions
		assertNotNull(enrichedRoot.getConstraint(), "ABAC should add constraints");
		assertEquals(enrichedRoot.getConstraint().getClass(), AndOperator.class);

		AndOperator rootAndConstraint = (AndOperator) enrichedRoot.getConstraint();
		assertEquals(rootAndConstraint.getOperands().size(), 3, "Expected 3 constraints at root level");

		assertContainsNotOperatorWithExactMatch(rootAndConstraint, FieldPaths.CONTRACT_TYPE, "Travel");
		assertContainsDoubleRangeOperator(rootAndConstraint, FieldPaths.CONTRACT_VALUE, null, maxContractValue);

		// And: HasOperator constraint includes business partner income restriction
		HasOperator hasOperator = findOperatorOfType(rootAndConstraint, HasOperator.class);
		assertEquals(hasOperator.getRelationshipModel(), RelationshipModels.CONTRACT_BUSINESS_PARTNER);
		assertEquals(hasOperator.getTargetRole(), Roles.PARTNER);
		assertTrue(hasOperator.getConstraint() instanceof AndOperator, 
			"Expected AndOperator as constraint of root HasOperator");

		// And: Nested constraint includes income and address restrictions
		AndOperator partnerAndConstraint = (AndOperator) hasOperator.getConstraint();
		assertEquals(partnerAndConstraint.getOperands().size(), 2, 
			"Expected 2 constraints in nested AndOperator");

		assertContainsDoubleRangeOperator(partnerAndConstraint, FieldPaths.BP_INCOME, minPartnerIncome, null);

		HasOperator addressHasOperator = findOperatorOfType(partnerAndConstraint, HasOperator.class);
		assertEquals(addressHasOperator.getRelationshipModel(), RelationshipModels.PARTNER_ADDRESSES);
		assertEquals(addressHasOperator.getTargetRole(), Roles.ADDRESS);

		// And: Address constraint includes Germany filter and Berlin exclusion
		AndOperator addressAndConstraint = (AndOperator) addressHasOperator.getConstraint();
		assertEquals(addressAndConstraint.getOperands().size(), 2);

		assertContainsExactMatch(addressAndConstraint, FieldPaths.ADDRESS_COUNTRY, "Germany");
		assertContainsNotOperatorWithExactMatch(addressAndConstraint, FieldPaths.ADDRESS_CITY, "Berlin");
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should return no results when HasOperator explicitly queries for excluded city"
	)
	public void shouldReturnNoResultsForExcludedCityInHasOperator(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A query for contracts with business partners having addresses in Berlin
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(CONTRACT_DOCUMENT_MODEL)
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.constraint(HasOperator.builder()
				.relationshipModel(RelationshipModels.CONTRACT_BUSINESS_PARTNER)
				.targetRole(Roles.PARTNER)
				.constraint(HasOperator.builder()
					.relationshipModel(RelationshipModels.PARTNER_ADDRESSES)
					.targetRole(Roles.ADDRESS)
					.constraint(ExactMatchOperator.builder()
						.field(FieldPaths.ADDRESS_CITY)
						.value("Berlin")
						.build())
					.build())
				.build())
			.build();

		// When: Query is executed with city restrictions
		setUserTo(user);
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Then: No contracts are returned (Berlin is excluded by ABAC)
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 0, 
			"Expected no contracts as Berlin addresses are excluded by ABAC");
	}

	@Test(
			dataProvider = "contractValueLimits",
			description = "Should apply authorization constraints across multi-level nested links"
	)
	public void shouldApplyAuthorizationInMultiLevelNestedLinks(String user, double maxContractValue, double minPartnerIncome) {
		// Given: A query for business partners with their contracts and co-insured partners
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(BUSINESS_PARTNER_DOCUMENT_MODEL)
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.link(QueryLink.builder()
				.relationshipModel(RelationshipModels.CONTRACT_BUSINESS_PARTNER)
				.targetRole(CONTRACT_DOCUMENT_MODEL)
				.link(QueryLink.builder()
					.relationshipModel(RelationshipModels.CONTRACT_COINSURED_PARTNER)
					.targetRole(Roles.PARTNER)
					.build())
				.build())
			.build();

		// When: Query is executed with ABAC enforcement
		QueryRoot enrichedRoot = executeQueryAndCaptureEnrichedRoot(queryRoot, user);

		// Then: Root constraint filters business partners by income
		assertNotNull(enrichedRoot.getConstraint());
		assertEquals(enrichedRoot.getConstraint().getClass(), DoubleRangeOperator.class);
		assertDoubleRangeOperator(enrichedRoot.getConstraint(), FieldPaths.BP_INCOME, minPartnerIncome, null);

		// And: First level link filters contracts
		List<QueryLink> queryLinkCollection = enrichedRoot.getLinks().stream().toList();
		assertEquals(queryLinkCollection.size(), 1);

		QueryLink firstLevelLink = queryLinkCollection.getFirst();
		assertNotNull(firstLevelLink.getConstraint(), "ABAC should add constraints to first level link");
		assertEquals(firstLevelLink.getConstraint().getClass(), AndOperator.class);

		AndOperator contractConstraint = (AndOperator) firstLevelLink.getConstraint();
		assertEquals(contractConstraint.getOperands().size(), 2, "Expected 2 ABAC constraints on contracts");

		assertContainsNotOperatorWithExactMatch(contractConstraint, FieldPaths.CONTRACT_TYPE, "Travel");
		assertContainsDoubleRangeOperator(contractConstraint, FieldPaths.CONTRACT_VALUE, null, maxContractValue);

		// And: Second level link filters co-insured partners by income
		assertNotNull(firstLevelLink.getLinks());
		QueryLink secondLevelLink = firstLevelLink.getLinks().stream().toList().getFirst();
		assertEquals(secondLevelLink.getConstraint().getClass(), DoubleRangeOperator.class);
		assertDoubleRangeOperator(secondLevelLink.getConstraint(), FieldPaths.BP_INCOME, minPartnerIncome, null);
	}

	// ==================== Helper Methods ====================

	/**
	 * Executes a query and captures the ABAC-enriched QueryRoot.
	 *
	 * @param queryRoot the original query root
	 * @param user the user to execute the query as
	 * @return the enriched QueryRoot with ABAC constraints applied
	 */
	private QueryRoot executeQueryAndCaptureEnrichedRoot(QueryRoot queryRoot, String user) {
		setUserTo(user);
		queryService.query(queryRoot, null);
		verify(queryRepository).query(queryRootCaptor.capture(), anyCollection(), any());
		return queryRootCaptor.getValue();
	}

	/**
	 * Searches for an operator of a specific type within a variadic operator.
	 *
	 * @param variadicOperator the parent operator to search in
	 * @param operatorClass the class of operator to find
	 * @param <T> the operator type
	 * @return the found operator
	 * @throws AssertionError if operator not found
	 */
	private <T extends ILogicOperator> T findOperatorOfType(VariadicOperator variadicOperator, Class<T> operatorClass) {
		return variadicOperator.getOperands().stream()
				.filter(operatorClass::isInstance)
				.map(operatorClass::cast)
				.findFirst()
				.orElseThrow(() -> new AssertionError(
						"Expected " + operatorClass.getSimpleName() + " in " + variadicOperator.getClass().getSimpleName()));
	}

	/**
	 * Verifies that a variadic operator contains a specific operator.
	 *
	 * @param variadicOperator the parent operator to check
	 * @param operator the operator to find
	 * @param message assertion failure message
	 */
	private void assertOperatorInVariadicOperator(VariadicOperator variadicOperator, ILogicOperator operator, String message) {
		assertTrue(variadicOperator.getOperands().stream().anyMatch(op -> op.equals(operator)), message);
	}

	/**
	 * Asserts that a variadic operator contains a NotOperator wrapping an ExactMatchOperator
	 * with the specified field and value.
	 *
	 * @param variadicOperator the parent operator containing the NotOperator
	 * @param expectedField the expected field path in the nested ExactMatchOperator
	 * @param expectedValue the expected value in the nested ExactMatchOperator
	 */
	private void assertContainsNotOperatorWithExactMatch(VariadicOperator variadicOperator, String expectedField, String expectedValue) {
		NotOperator notOperator = findOperatorOfType(variadicOperator, NotOperator.class);
		assertExactMatch(notOperator.getOperand(), expectedField, expectedValue);
	}

	/**
	 * Asserts that a variadic operator contains an ExactMatchOperator with the specified field and value.
	 *
	 * @param variadicOperator the parent operator containing the ExactMatchOperator
	 * @param expectedField the expected field path
	 * @param expectedValue the expected value
	 */
	private void assertContainsExactMatch(VariadicOperator variadicOperator, String expectedField, String expectedValue) {
		ExactMatchOperator<?> exactMatch = findOperatorOfType(variadicOperator, ExactMatchOperator.class);
		assertEquals(exactMatch.getField(), expectedField,
				"Expected ExactMatchOperator with field [%s] but got [%s]".formatted(expectedField, exactMatch.getField()));
		assertEquals(exactMatch.getValue(), expectedValue,
				"Expected ExactMatchOperator with value [%s] but got [%s]".formatted(expectedValue, exactMatch.getValue()));
	}

	/**
	 * Asserts that a variadic operator contains a DoubleRangeOperator with the specified range.
	 *
	 * @param variadicOperator the parent operator containing the DoubleRangeOperator
	 * @param expectedField the expected field path
	 * @param expectedFrom the expected lower bound (null if unbounded)
	 * @param expectedTo the expected upper bound (null if unbounded)
	 */
	private void assertContainsDoubleRangeOperator(VariadicOperator variadicOperator, String expectedField, Double expectedFrom, Double expectedTo) {
		DoubleRangeOperator rangeOperator = findOperatorOfType(variadicOperator, DoubleRangeOperator.class);
		assertDoubleRangeOperator(rangeOperator, expectedField, expectedFrom, expectedTo);
	}

	/**
	 * Asserts that an operator is an ExactMatchOperator with specific field and value.
	 *
	 * @param operator the operator to verify
	 * @param expectedField the expected field path
	 * @param expectedValue the expected value
	 */
	private void assertExactMatch(ILogicOperator operator, String expectedField, String expectedValue) {
		assertEquals(operator.getClass(), ExactMatchOperator.class);
		ExactMatchOperator<?> exactMatchOperator = (ExactMatchOperator<?>) operator;

		assertEquals(exactMatchOperator.getField(), expectedField,
				"Expected ExactMatchOperator with field [%s] but got [%s]".formatted(expectedField, exactMatchOperator.getField()));
		assertEquals(exactMatchOperator.getValue(), expectedValue,
				"Expected ExactMatchOperator with value [%s] but got [%s]".formatted(expectedValue, exactMatchOperator.getValue()));
	}

	/**
	 * Asserts that an operator is a DoubleRangeOperator with specific range bounds.
	 *
	 * @param operator the operator to verify
	 * @param expectedField the expected field path
	 * @param expectedFrom the expected lower bound (null if unbounded)
	 * @param expectedTo the expected upper bound (null if unbounded)
	 */
	private void assertDoubleRangeOperator(ILogicOperator operator, String expectedField, Double expectedFrom, Double expectedTo) {
		assertEquals(operator.getClass(), DoubleRangeOperator.class);
		DoubleRangeOperator rangeOperator = (DoubleRangeOperator) operator;

		assertEquals(rangeOperator.getField(), expectedField,
				"Expected DoubleRangeOperator with field [%s] but got [%s]".formatted(expectedField, rangeOperator.getField()));

		if (expectedTo != null) {
			assertEquals(rangeOperator.getTo(), expectedTo,
					"Expected DoubleRangeOperator with to [%s] but got [%s]".formatted(expectedTo, rangeOperator.getTo()));
		}

		if (expectedFrom != null) {
			assertEquals(rangeOperator.getFrom(), expectedFrom,
					"Expected DoubleRangeOperator with from [%s] but got [%s]".formatted(expectedFrom, rangeOperator.getFrom()));
		}
	}
}
