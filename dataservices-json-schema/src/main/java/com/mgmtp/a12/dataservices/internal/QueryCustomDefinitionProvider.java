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

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.JsonNodeFactory;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.internal.UnknownOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.UnknownFunction;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.node.StringNode;

/**
 * Query Custom Definition Provider class. Should not be used by customer projects.
 *
 */
@Slf4j @RequiredArgsConstructor
class QueryCustomDefinitionProvider implements CustomDefinitionProviderV2 {
	private final Set<StringNode> availableProjections;
	private final ThreadLocal<Set<Class<?>>> generationGuard;

	/**
	 * Provides a custom JSON Schema definition for known query-related types.
	 * Adds fixed-property constraints for {@link ILogicOperator} and {@link IAggregationFunction} implementations,
	 * an enum for {@link QueryRoot} projections, and mutual exclusivity constraints for {@link Order} and {@link RelationshipOrder}.
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
			return getQueryRootDefinition(declaredType, ctx);
		} else if (Order.class.equals(erased)) {
			return getOrderDefinition(declaredType, ctx);
		} else if (RelationshipOrder.class.equals(erased)) {
			return getRelationshipOrderDefinition(declaredType, ctx);
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

	private CustomDefinition getOrderDefinition(ResolvedType declaredType, SchemaGenerationContext ctx) {
		Set<Class<?>> guard = generationGuard.get();
		if (guard.contains(Order.class)) {
			return null;
		}
		try {
			guard.add(Order.class);
			ObjectNode def = ctx.createStandardDefinition(declaredType, this);

			// Trigger deferred schema generation for RelationshipOrder so that $defs/RelationshipOrder is populated
			// with our custom definition. Using createDefinitionReference ensures victools calls
			// getRelationshipOrderDefinition with all providers active.
			ctx.createDefinitionReference(ctx.getTypeContext().resolve(RelationshipOrder.class));

			def.put("description",
				"Defines the sorting specification for query results. " +
				"An order can sort by either a direct field ('field' property required) or by traversing a relationship " +
				"('relationshipModel' property required). These two modes are mutually exclusive.");

			// Add oneOf constraint: DirectFieldOrder (field required) XOR RelationshipOrder (relationshipModel required)
			ArrayNode oneOf = def.withArray("oneOf");

			// Option 1: Direct field sorting — 'field' is required
			ObjectNode fieldOption = JsonNodeFactory.instance.objectNode();
			fieldOption.withArray("required").add("field");
			oneOf.add(fieldOption);

			// Option 2: Relationship-based sorting — 'relationshipModel' is the discriminator
			ObjectNode relationshipOption = JsonNodeFactory.instance.objectNode();
			relationshipOption.withArray("required").add("relationshipModel");
			oneOf.add(relationshipOption);

			log.debug("Added mutual exclusivity constraints for Order: field (DirectFieldOrder) XOR relationshipModel (RelationshipOrder)");
			return new CustomDefinition(def);
		} finally {
			guard.remove(Order.class);
		}
	}

	private CustomDefinition getRelationshipOrderDefinition(ResolvedType declaredType, SchemaGenerationContext ctx) {
		Set<Class<?>> guard = generationGuard.get();
		if (guard.contains(RelationshipOrder.class)) {
			return null;
		}
		try {
			guard.add(RelationshipOrder.class);
			ObjectNode def = JsonNodeFactory.instance.objectNode();

			def.put("type", "object");

			ObjectNode properties = def.withObject("properties");
			properties.withObject("relationshipModel").put("type", "string");
			properties.withObject("targetRole").put("type", "string");
			properties.withObject("sortBy").put("$ref", "#/$defs/Order");

			def.put("description",
				"Defines sorting by fields of related documents through to-1 relationships. " +
				"Represents a hop in the traversal chain with a nested sort specification (either another RelationshipOrder " +
				"for multi-level traversal or a DirectFieldOrder for the terminal field).");

			def.withArray("required")
				.add("relationshipModel")
				.add("targetRole")
				.add("sortBy");

			log.debug("Added required properties for RelationshipOrder: relationshipModel, targetRole, sortBy");
			return new CustomDefinition(def, CustomDefinition.DefinitionType.ALWAYS_REF, CustomDefinition.AttributeInclusion.NO);
		} finally {
			guard.remove(RelationshipOrder.class);
		}
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
