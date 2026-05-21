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

import java.util.List;

import com.fasterxml.classmate.ResolvedType;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SubtypeResolver;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;

import lombok.RequiredArgsConstructor;

/**
 * Query Subtype Resolver class for JSON schema generation. Should not be used by customer projects.
 *
 */
@RequiredArgsConstructor
class QuerySubtypeResolver implements SubtypeResolver {
	private final List<Class<? extends ILogicOperator>> operatorImpls;
	private final List<Class<? extends IAggregationFunction>> functionImpls;

	/**
	 * Resolves concrete subtypes for the declared base type to include in the generated schema.
	 * Supports {@link ILogicOperator} and {@link IAggregationFunction} by returning discovered implementations.
	 *
	 * @param declaredType the base type being examined; never null.
	 * @param ctx the generation context providing type resolution; never null.
	 * @return a list of resolved subtypes to be used in schema generation, or `null` if no special handling applies.
	 */
	@Override public List<ResolvedType> findSubtypes(ResolvedType declaredType, SchemaGenerationContext ctx) {
		Class<?> erased = declaredType.getErasedType();
		if (ILogicOperator.class.equals(erased)) {
			return operatorImpls.stream()
				.map(ctx.getTypeContext()::resolve)
				.toList();
		} else if (IAggregationFunction.class.equals(erased)) {
			return functionImpls.stream()
				.map(ctx.getTypeContext()::resolve)
				.toList();
		} else {
			return null;
		}
	}
}
