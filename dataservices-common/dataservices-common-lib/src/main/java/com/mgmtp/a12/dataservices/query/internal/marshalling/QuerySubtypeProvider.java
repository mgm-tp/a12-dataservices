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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.reflections.Reflections;

import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;

import tools.jackson.databind.jsontype.NamedType;

/**
 * Scans the specified base packages and collects all concrete classes annotated with
 * {@link QueryOperator} or {@link QueryAggregationFunction} as Jackson {@link NamedType} entries.
 *
 * The result is cached after construction; `getSubtypes()` is safe to call from multiple threads.
 */
public class QuerySubtypeProvider {

	private final List<NamedType> subtypes;

	/**
	 * Scans the given base packages and collects all concrete subtypes annotated with
	 * {@link QueryOperator} or {@link QueryAggregationFunction}.
	 *
	 * @param basePackages one or more package prefixes to scan; must not be null or empty.
	 */
	public QuerySubtypeProvider(String... basePackages) {
		this(new Reflections((Object[]) basePackages));
	}

	/**
	 * Collects all concrete subtypes from a pre-built {@link Reflections} instance.
	 * Use this overload when the caller already holds a configured {@link Reflections}
	 * (e.g. one built from `@DataServicesApplication.scanBasePackages`).
	 *
	 * @param reflections pre-built reflections scanner; must not be null.
	 */
	public QuerySubtypeProvider(Reflections reflections) {
		subtypes = List.copyOf(collectSubtypes(reflections));
	}

	/**
	 * Returns the immutable list of discovered subtypes.
	 *
	 * @return subtypes; never null, may be empty.
	 */
	public Collection<NamedType> getSubtypes() {
		return subtypes;
	}

	private static List<NamedType> collectSubtypes(Reflections reflections) {
		List<NamedType> result = new ArrayList<>();
		reflections.getTypesAnnotatedWith(QueryOperator.class).stream()
			.filter(c -> !Modifier.isAbstract(c.getModifiers()))
			.map(c -> new NamedType(c, c.getAnnotation(QueryOperator.class).value()))
			.forEach(result::add);
		reflections.getTypesAnnotatedWith(QueryAggregationFunction.class).stream()
			.filter(c -> !Modifier.isAbstract(c.getModifiers()))
			.map(c -> new NamedType(c, c.getAnnotation(QueryAggregationFunction.class).value()))
			.forEach(result::add);
		return result;
	}
}
