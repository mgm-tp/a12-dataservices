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
package com.mgmtp.a12.dataservices.query.internal.marshalling;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.jsontype.NamedType;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link QuerySubtypeProvider} verifying that classpath scanning discovers
 * all concrete {@link com.mgmtp.a12.dataservices.query.annotation.QueryOperator} and
 * {@link com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction} implementations.
 */
public class QuerySubtypeProviderTest {

	private QuerySubtypeProvider provider;

	@BeforeMethod
	public void setUp() {
		provider = new QuerySubtypeProvider("com.mgmtp.a12.dataservices");
	}

	/**
	 * Tests that the provider returns a non-null, non-empty collection of subtypes
	 * after scanning the default Data Services package prefix.
	 */
	@Test(description = "Should return non-empty subtype list when scanning Data Services package")
	public void shouldReturnNonEmptySubtypeListWhenScanningDataServicesPackage() {
		// When
		Collection<NamedType> subtypes = provider.getSubtypes();

		// Then
		assertNotNull(subtypes, "Subtypes must not be null");
		assertFalse(subtypes.isEmpty(), "At least one query operator or aggregation function must be discovered");
	}

	/**
	 * Tests that every discovered {@link com.mgmtp.a12.dataservices.query.annotation.QueryOperator}
	 * subtype carries a non-blank type name derived from the annotation value.
	 */
	@Test(description = "Should assign non-blank names to all QueryOperator subtypes")
	public void shouldAssignNonBlankNamesToAllQueryOperatorSubtypes() {
		// When
		Collection<NamedType> subtypes = provider.getSubtypes();

		// Then
		subtypes.forEach(nt ->
			assertTrue(nt.getName() != null && !nt.getName().isBlank(),
				"NamedType for " + nt.getType().getSimpleName() + " must carry a non-blank name"));
	}

	/**
	 * Tests that no abstract class appears in the discovered subtype list.
	 */
	@Test(description = "Should exclude abstract classes from discovered subtypes")
	public void shouldExcludeAbstractClassesFromDiscoveredSubtypes() {
		// When
		Collection<NamedType> subtypes = provider.getSubtypes();

		// Then
		subtypes.forEach(nt -> {
			int modifiers = nt.getType().getModifiers();
			assertFalse(java.lang.reflect.Modifier.isAbstract(modifiers),
				nt.getType().getSimpleName() + " must not be abstract");
		});
	}

	/**
	 * Tests that a second call to `getSubtypes()` returns the same collection instance
	 * (result is cached or otherwise idempotent) so repeated invocations do not trigger
	 * an additional classpath scan.
	 */
	@Test(description = "Should return identical collection on repeated calls to getSubtypes")
	public void shouldReturnIdenticalCollectionOnRepeatedCallsToGetSubtypes() {
		// When
		Collection<NamedType> first = provider.getSubtypes();
		Collection<NamedType> second = provider.getSubtypes();

		// Then
		assertTrue(first == second || first.equals(second),
			"Repeated getSubtypes() calls must return the same result");
	}

	/**
	 * Tests that when a package prefix is given that contains no annotated types,
	 * the provider returns an empty (not null) collection.
	 */
	@Test(description = "Should return empty collection when no annotated types exist in the given package")
	public void shouldReturnEmptyCollectionWhenNoAnnotatedTypesExistInGivenPackage() {
		// Given
		QuerySubtypeProvider emptyProvider = new QuerySubtypeProvider("com.example.nonexistent");

		// When
		Collection<NamedType> subtypes = emptyProvider.getSubtypes();

		// Then
		assertNotNull(subtypes, "Collection must not be null even when empty");
		assertTrue(subtypes.isEmpty(), "No subtypes expected for a package with no annotated types");
	}
}
