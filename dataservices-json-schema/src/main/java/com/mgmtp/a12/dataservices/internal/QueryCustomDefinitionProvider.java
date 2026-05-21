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
package com.mgmtp.a12.dataservices.internal;

import java.util.Set;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.internal.UnknownOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.UnknownFunction;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Query Custom Definition Provider class. Should not be used by customer projects.
 *
 */
@Slf4j @RequiredArgsConstructor
class QueryCustomDefinitionProvider implements CustomDefinitionProviderV2 {
	private final Set<TextNode> availableProjections;
	private final ThreadLocal<Set<Class<?>>> generationGuard;

	/**
	 * Provides a custom JSON Schema definition for known query-related types.
	 * Adds fixed-property constraints for {@link ILogicOperator} and {@link IAggregationFunction} implementations and an enum for {@link QueryRoot} projections.
	 *
	 * @param declaredType the declared type being processed; never null.
	 * @param ctx the schema generation context providing helpers and type resolution; never null.
	 * @return a {@link CustomDefinition} to use for the given type, or `null` to fall back to the standard definition.
	 */
	@Override public CustomDefinition provideCustomSchemaDefinition(ResolvedType declaredType, SchemaGenerationContext ctx) {

		Class<?> erased = declaredType.getErasedType();

		if (ILogicOperator.class.isAssignableFrom(erased)
			&& !ILogicOperator.class.equals(erased)
			&& !UnknownOperator.class.isAssignableFrom(erased)) {
			return getCustomDefinition(declaredType, ctx, erased, "operator", erased.getDeclaredAnnotation(QueryOperator.class).value());
		} else if (IAggregationFunction.class.isAssignableFrom(erased)
			&& !IAggregationFunction.class.equals(erased)
			&& !UnknownFunction.class.isAssignableFrom(erased)) {
			return getCustomDefinition(declaredType, ctx, erased, "function",
				erased.getDeclaredAnnotation(QueryAggregationFunction.class).value());
		} else if (QueryRoot.class.equals(erased)) {
			return getQueryRootDefinition(declaredType, ctx); // root type, no special handling
		} else {
			return null;
		}
	}

	private CustomDefinition getQueryRootDefinition(ResolvedType declaredType, SchemaGenerationContext ctx) {
		ObjectNode def = ctx.createStandardDefinition(declaredType, this);
		def.withObject("properties")
			.withObject("projectionName")
			.withArray("enum")
			.addAll(availableProjections);
		log.debug("Added enum for projectionName: {}", availableProjections);
		return new CustomDefinition(def);
	}

	private CustomDefinition getCustomDefinition(ResolvedType declaredType, SchemaGenerationContext ctx, Class<?> erased, String propertyName,
		String constValue) {

		Set<Class<?>> guard = generationGuard.get();
		if (guard.contains(erased)) {
			return null;
		}
		try {
			guard.add(erased);
			ObjectNode def = ctx.createStandardDefinition(declaredType, this);
			def.withObject("properties")
				.withObject(propertyName)
				.put("const", constValue);
			def.withArray("required").add(propertyName);
			return new CustomDefinition(def, false);
		} finally {
			guard.remove(erased);
		}
	}
}
