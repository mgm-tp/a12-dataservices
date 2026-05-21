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
package com.mgmtp.a12.dataservices.internal.query.security;

import java.util.HashSet;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.query.security.internal.DefaultQueryAuthorizationService;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.UndefinedMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

public class QueryAuthorizationServiceTest extends AbstractQueryContextAwareTest {

	@Mock AuthorizationService authorizationService;
	@InjectMocks private DefaultQueryAuthorizationService queryAuthorizationService;

	@BeforeMethod
	public void setUp() {
		queryAuthorizationService = new DefaultQueryAuthorizationService(authorizationService, objectMapper);
	}

	@Test
	public void testQueryAuthorizationServiceWithoutTemplate() {
		Set<String> permissionTemplates = new HashSet<>();
		Mockito.when(authorizationService.generateRepositoryPermissions(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(permissionTemplates);
		ILogicOperator constraint = UndefinedMatchOperator.builder()
			.field("some_field")
			.build();

		ILogicOperator operator = queryAuthorizationService.addAbacRules(constraint, "some_model");

		Assert.assertTrue(operator instanceof UndefinedMatchOperator);
		Assert.assertEquals(((UndefinedMatchOperator)operator).getField(), "some_field");
	}

	@Test
	public void testQueryAuthorizationServiceWithSingleTemplate() {
		Set<String> permissionTemplates = new HashSet<>();
		permissionTemplates.add(
			"""
			{
				"operator": "exact_match",
				"field": "/ContractRoot/ContractValue",
				"value": "5000"
			}
			"""
		);

		Mockito.when(authorizationService.generateRepositoryPermissions(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(permissionTemplates);
		ILogicOperator constraint = UndefinedMatchOperator.builder()
			.field("some_field")
			.build();

		ILogicOperator operator = queryAuthorizationService.addAbacRules(constraint, "some_model");

		Assert.assertTrue(operator instanceof AndOperator);
		Assert.assertEquals(((AndOperator) operator).getOperands().size(), 2);
	}

	@Test
	public void testQueryAuthorizationServiceWithMultipleTemplates() {
		Set<String> permissionTemplates = new HashSet<>();
		permissionTemplates.add(
			"""
			{
				"operator": "double_range",
				"field": "/ContractRoot/ContractValue",
				"to": "5000"
			}
			"""
		);
		permissionTemplates.add(
			"""
			{
				"operator": "exact_match",
				"field": "/ContractRoot/ContractName",
				"value": "Ina"
			}
			"""
		);

		Mockito.when(authorizationService.generateRepositoryPermissions(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(permissionTemplates);
		ILogicOperator constraint = UndefinedMatchOperator.builder()
			.field("some_field")
			.build();

		ILogicOperator operator = queryAuthorizationService.addAbacRules(constraint, "some_model");

		Assert.assertTrue(operator instanceof AndOperator);
		Assert.assertEquals(((AndOperator) operator).getOperands().size(), 3);
	}

	@Test
	public void testQueryAuthorizationServiceWithEmptyConstraint() {
		Set<String> permissionTemplates = new HashSet<>();
		permissionTemplates.add(
			"""
			{
				"operator": "double_range",
				"field": "/ContractRoot/ContractValue",
				"to": "5000"
			}
			"""
		);

		Mockito.when(authorizationService.generateRepositoryPermissions(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(permissionTemplates);

		ILogicOperator operator = queryAuthorizationService.addAbacRules(null, "some_model");

		Assert.assertTrue(operator instanceof DoubleRangeOperator);
		Assert.assertEquals(((DoubleRangeOperator) operator).getField(), "/ContractRoot/ContractValue");
	}

	@Test
	public void testQueryAuthorizationServiceWithMissingDocumentModel() {
		ILogicOperator constraint = UndefinedMatchOperator.builder()
			.field("some_field")
			.build();

		ILogicOperator operator = queryAuthorizationService.addAbacRules(constraint, null);

		Assert.assertTrue(operator instanceof UndefinedMatchOperator);
		Assert.assertEquals(((UndefinedMatchOperator)operator).getField(), "some_field");
	}
}
