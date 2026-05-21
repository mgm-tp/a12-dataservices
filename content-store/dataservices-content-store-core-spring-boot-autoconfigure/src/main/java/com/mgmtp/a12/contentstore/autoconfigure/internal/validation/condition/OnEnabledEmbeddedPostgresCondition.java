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
package com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.mgmtp.a12.contentstore.autoconfigure.internal.CSEmbeddedPostgresDatasourceConfiguration;
import com.mgmtp.a12.contentstore.configuration.internal.validation.ConfigurationMessage;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OnEnabledEmbeddedPostgresCondition extends SpringBootCondition {

	private static final String EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_NAME =
		CSEmbeddedPostgresDatasourceConfiguration.CONTENT_STORE_EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_BASE + ".enabled";

	@NonNull private static Optional<Boolean> embeddedPostgres = Optional.empty();

	@Override public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Boolean isEmbeddedPostgres = findBoundProperties(context.getEnvironment())
			.orElse(null);
		return new ConditionOutcome(evaluateCondition(),
			ConditionMessage.forCondition(ConditionalOnExpression.class, validate())
				.resultedIn(isEmbeddedPostgres));
	}

	protected boolean evaluateCondition() {
		return embeddedPostgres.isPresent() && embeddedPostgres.get();
	}

	private static @NonNull Optional<Boolean> findBoundProperties(Environment environment) {
		embeddedPostgres = Optional.of(Binder.get(environment))
				.map(b -> b.bind(EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_NAME, Boolean.class))
				.filter(BindResult::isBound)
				.map(BindResult::get);
		return embeddedPostgres;
	}

	private static ConfigurationMessage validate() {
		return ConfigurationMessage.builder()
			.message(embeddedPostgres
				.map(isEmbedded -> "Enabled embedded postgres.")
				.orElse("Disabled embedded postgres."))
			.relatedProperties(List.of(EMBEDDED_POSTGRES_DATASOURCE_PROPERTY_NAME))
			.build();
	}
}
