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
package com.mgmtp.a12.dataservices.configuration.internal.validation.condition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.rpc.internal.RpcUtils;

public class DataSourceReplicaRoutingEnabledConditionTest {

	private DataSourceReplicaRoutingEnabledCondition condition;
	private ConditionContext conditionContext;
	private Environment environment;

	@BeforeMethod
	public void setUp() {
		condition = new DataSourceReplicaRoutingEnabledCondition();
		conditionContext = mock(ConditionContext.class);
		environment = mock(Environment.class);
		when(conditionContext.getEnvironment()).thenReturn(environment);
	}

	@Test
	public void shouldReturnTrueWhenReplicaUrlIsConfigured() {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY))
			.thenReturn("jdbc:postgresql://replica:5432/db");

		assertTrue(condition.matches(conditionContext, mock(AnnotatedTypeMetadata.class)));
	}

	@Test
	public void shouldReturnFalseWhenReplicaUrlIsAbsent() {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY))
			.thenReturn(null);

		assertFalse(condition.matches(conditionContext, mock(AnnotatedTypeMetadata.class)));
	}

	@Test
	public void shouldReturnFalseWhenReplicaUrlIsBlank() {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY))
			.thenReturn("   ");

		assertFalse(condition.matches(conditionContext, mock(AnnotatedTypeMetadata.class)));
	}

	@Test
	public void shouldReturnFalseWhenReplicaUrlIsEmpty() {
		when(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY))
			.thenReturn("");

		assertFalse(condition.matches(conditionContext, mock(AnnotatedTypeMetadata.class)));
	}
}
