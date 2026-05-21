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
package com.mgmtp.a12.dataservices.internal.condition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.SeedDataProperties;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSeedDataCondition extends SpringBootCondition {

	@NonNull private static Optional<SeedDataProperties> boundProperties = Optional.empty();

	@Override public final ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		SeedDataProperties seedDataProperties = AbstractSeedDataCondition.findBoundProperties(context.getEnvironment())
			.orElseThrow(() -> new UnexpectedException("Unable to bind Seed Data properties."));
		return new ConditionOutcome(evaluateCondition(seedDataProperties),
			ConditionMessage.forCondition(ConditionalOnExpression.class, validate(seedDataProperties))
				.resultedIn(getStringRepresentation(seedDataProperties)));
	}

	/**
	 * Get condition state string representation to be used in messages.
	 *
	 * @param seedDataProperties {@link SeedDataProperties} with actual values
	 * @return String representation of the condition state
	 */
	protected String getStringRepresentation(SeedDataProperties seedDataProperties) {
		return getConfigurationMessage(seedDataProperties).getLeft();
	}

	protected Pair<String, List<String>> getConfigurationMessage(SeedDataProperties seedDataProperties) {
		return evaluateCondition(seedDataProperties) ?
			makeValidMessage(getEnabledMessage(), getProperty()) :
			makeValidMessage(getDisabledMessage(), getProperty());
	}

	protected boolean evaluateCondition(SeedDataProperties seedDataProperties) {
		return isEnabled(seedDataProperties);
	}

	protected abstract boolean isEnabled(SeedDataProperties seedDataProperties);

	protected Pair<String, List<String>>  validate(SeedDataProperties seedDataProperties) {
		return getConfigurationMessage(seedDataProperties);
	}

	/**
	 * Function to get actual values of {@link SeedDataProperties}.
	 *
	 * @param environment spring {@link Environment}
	 * @return {@link Optional} of actual {@link SeedDataProperties} of {@link Optional#empty()} if it can not be bound
	 */
	public static @NonNull Optional<SeedDataProperties> findBoundProperties(Environment environment) {
		if (boundProperties.isEmpty()) {
			boundProperties = Optional.of(Binder.get(environment))
				.map(b -> b.bind(DataServicesCoreProperties.PROPERTIES_PREFIX, SeedDataProperties.class))
				.filter(BindResult::isBound)
				.map(BindResult::get);
		}
		return boundProperties.isPresent() ? boundProperties : Optional.of(new SeedDataProperties());
	}

	protected Pair<String, List<String>>  makeValidMessage(String message, String... relatedProperties) {
		return makeMessageInternal(message, Arrays.asList(relatedProperties));
	}

	private Pair<String, List<String>>  makeMessageInternal(String message, List<String> relatedProperties) {
		return Pair.of(message, relatedProperties);
	}

	protected abstract String getProperty();

	protected abstract String getDisabledMessage();

	protected abstract String getEnabledMessage();
}
