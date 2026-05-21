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
package com.mgmtp.a12.dataservices.query.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class QueryContextHelper {

	private final ApplicationContext applicationContext;
	private final Map<String, IQueryOperatorValidator> validators;
	@Getter private Map<Class<? extends ILogicOperator>, Set<IQueryOperatorValidator>> validatorMappings = new HashMap<>();
	@Getter private Map<Class<? extends ILogicOperator>, String> operators;

	@PostConstruct void init() {

		Reflections reflections = GenericUtils.getApplicationReflections(applicationContext);

		operators = reflections.getTypesAnnotatedWith(QueryOperator.class).stream()
			.filter(ILogicOperator.class::isAssignableFrom)
			.map(clazz -> Pair.of((Class<? extends ILogicOperator>) clazz, clazz.getAnnotation(QueryOperator.class)))
			.filter(p -> !Objects.isNull(p.getValue()))
			.collect(Collectors.toMap(Pair::getKey, it -> it.getValue().value()));

		validatorMappings = validators.entrySet().stream()
			.flatMap(e -> Optional.ofNullable(applicationContext.findAnnotationOnBean(e.getKey(), QueryOperatorValidator.class)).stream()
				.flatMap(v -> Arrays.stream(v.value())
					.flatMap(o -> operators.keySet().stream().filter(o::isAssignableFrom))
					.map(o -> Pair.of(o, e.getValue()))))
			.filter(p -> operators.containsKey(p.getLeft()))
			.collect(Collectors.groupingBy(Pair::getKey,
				Collectors.mapping(Pair::getValue,
					Collectors.toSet())));
	}
}
