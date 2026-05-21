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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.examples.AbstractITBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@ActiveProfiles({ InsuranceUserConfiguration.DATASERVICES_EXAMPLE_ABAC_ENV })
public class AbacIT extends AbstractITBase {

	@Autowired private QueryService queryService;

	@Override @BeforeClass public void prepareTestEnvironment() {
		// everything is prepared via application properties
		/*
								  Contract:HealthHighValue (value 50.000)

									Contract:Health (value 10.000)
							/               |                       \
				BusinessPartner:Peter   CoInsuredPartner:Fritz  CoInsuredPartner:Nicole

									Contract:Travel
											|
									BusinessPartner:Peter

		                    BusinessPartner:Peter (income 4.000)
			                    /               \
			         Address:Berlin         Address:Hamburg

		                    BusinessPartner:Olivia (income 8.000)
			                                |
			                            Address:Prague

		                    BusinessPartner:Nicole (income 12.000)
			                                |
			                            Address:Paris

			                BusinessPartner:Fritz (income 1.000)


			User is allowed to see (via abacAuthorizationDefinition.json)
			- Contracts that are not of type "Travel"
			- Contracts that have a value below a max value
			- BusinessPartners with income above a lower limit
			- Addresses that are not in a certain city
		 */
		setUserTo(UserConstants.ADMIN_USER);
	}

	@AfterClass public void cleanUpTestEnvironment() {
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager);
	}

	@Test
	public void checkContractTypeNotTravel() {

		setUserTo("insurance100-5-B");

		// Select the first 10 contracts
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("Contract")
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 2 contract according to the ABAC rule that the contract type must not be "Travel"
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 2);

	}

	@Test
	public void checkContractBelowMaxValue() {

		setUserTo("insurance15-5-B");

		// Select the first 10 contracts
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("Contract")
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 1 contract according to the ABAC rules that
		// a) the contract type must not be "Travel"
		// b) the contract value must be maximum 15.000
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 1);

	}

	@Test
	public void checkBusinessPartnerMinIncome() {

		setUserTo("insurance15-5-B");

		// Select the first 10 business partners
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("BusinessPartner")
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 2 business partner according to the ABAC rule that the income must be above 5.000 (it's Olivia and Nicole)
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 2);

	}

	@Test
	public void checkCityNotBerlin() {

		setUserTo("insurance15-3-B");

		// Select the business partners, which have an address in Berlin assigned (it's Peter), together with his addresses
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("BusinessPartner")
			.constraint(ExactMatchOperator.builder()
				.field("/BusinessPartnerRoot/Name")
				.value("Peter")
				.build())
			.link(QueryLink.builder()
				.relationshipModel("PartnerAddresses")
				.targetRole("Address")
				.build())
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 1 business partner and 1 address link according to the ABAC rule that the address must not be located in Berlin
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 2);
		result.getContent().forEach(r -> {
			if (r.getType().equals(DocumentTreeNodeType.ROOT)) {
				assertTrue(r.getDocument().toString().contains("Peter"));
			} else if (r.getType().equals(DocumentTreeNodeType.CHILD)) {
				assertFalse(r.getDocument().toString().contains("Berlin"));
			} else {
				Assert.fail("No other types but ROOT and CHILD expected but got [%s]".formatted(r.getType()));
			}
		});
	}

	@Test
	public void checkCityNotBerlinWithOr() {

		setUserTo("insurance15-3-B");

		// Select the addresses, which are in Germany or France
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("Address")
			.constraint(OrOperator.builder()
				.operands(List.of(
					ExactMatchOperator.builder()
						.field("/AddressRoot/Country")
						.value("Germany")
						.build(),
					ExactMatchOperator.builder()
						.field("/AddressRoot/Country")
						.value("France")
						.build()
				))
				.build()
			)
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 2 addresses because one of the German addresses is in Berlin which is excluded due to ABAC
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 2);
		result.getContent().forEach(r -> assertFalse(r.getDocument().toString().contains("Berlin")));
	}

	@Test
	public void checkCityNotBerlinWithNestedHas() {

		setUserTo("insurance15-3-B");

		// Select the first 10 contracts that have business partners that have addresses in Germany
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("Contract")
			.constraint(HasOperator.builder()
				.relationshipModel("ContractBusinessPartner")
				.targetRole("Partner")
				.constraint(HasOperator.builder()
					.relationshipModel("PartnerAddresses")
					.targetRole("Address")
					.constraint(ExactMatchOperator.builder()
						.field("/AddressRoot/Country")
						.value("Germany")
						.build())
					.build())
				.build())
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 1 contract according to the ABAC rule that the contract type must not be "Travel"
		// and by user insurance15-5-B the contract value must be below 15.000
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 1);

		// Select the first 10 contracts that have business partners that have addresses in Berlin
		queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("Contract")
			.constraint(HasOperator.builder()
				.relationshipModel("ContractBusinessPartner")
				.targetRole("Partner")
				.constraint(HasOperator.builder()
					.relationshipModel("PartnerAddresses")
					.targetRole("Address")
					.constraint(ExactMatchOperator.builder()
						.field("/AddressRoot/City")
						.value("Berlin")
						.build())
					.build())
				.build())
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		result = queryService.query(queryRoot, null);

		// Expect no contract according to the ABAC rule that the address must not be in Berlin
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 0);
	}

	@Test
	public void checkBusinessPartnerMinIncomeWithNestedLinks() {

		setUserTo("insurance15-3-B");

		// Select the first 10 business partners together with their contracts together with the contract's co-insured partners
		QueryRoot queryRoot = QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel("BusinessPartner")                                         // gives Peter, Olivia and Nicole (Fritz has income below 3000)
			.link(QueryLink.builder()
				.relationshipModel("ContractBusinessPartner")
				.targetRole("Contract")                                                     // gives ContractHealth via Peter (other contracts are Travel or above 15.000)
				.link(QueryLink.builder()
					.relationshipModel("ContractCoInsuredPartner")                          // gives Nicole (Fritz has income below 3000)
					.targetRole("Partner")
					.build())
				.build())
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.build();
		QueryPage<DocumentTreeResult> result = queryService.query(queryRoot, null);

		// Expect exactly 3 business partner according to the ABAC rule that the income must be above 3.000 which excludes Fritz
		// In addition we expect in the links section
		//    - 1 contract due to the ABAC rule that contracts must not be of type Travel and must have a value less than 15.000
		//    - 1 co-insured partner due to the ABAC rule that business partners must have an income above 3.000 which excludes Fritz
		assertNotNull(result);
		assertEquals((long) result.getContent().size(), 5);

		result.getContent().forEach(r -> {
			if (r.getType().equals(DocumentTreeNodeType.ROOT)) {
				assertFalse(r.getDocument().toString().contains("Fritz"));
			} else if (r.getType().equals(DocumentTreeNodeType.CHILD)) {
				String childDocument = r.getDocument().toString();
				assertFalse(childDocument.contains("Travel"));
				assertFalse(childDocument.contains("Fritz"));
			} else {
				Assert.fail("No other types but ROOT and CHILD expected but got [%s]".formatted(r.getType()));
			}
		});
	}
}
