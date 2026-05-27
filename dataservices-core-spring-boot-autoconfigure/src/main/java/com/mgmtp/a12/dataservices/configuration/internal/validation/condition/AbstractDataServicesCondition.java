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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.validation.internal.ConfigurationMessage;
import com.mgmtp.a12.dataservices.configuration.validation.internal.WrongConfigurationMessage;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.configuration.validation.internal.WrongConfigurationMessage.EXISTING_WARNINGS;

/**
 * Parent for all {@link DataServicesCoreProperties} based {@link SpringBootCondition}s.
 */
@Slf4j
public abstract class AbstractDataServicesCondition extends SpringBootCondition {
	private static final DataServicesCoreProperties DEFAULT_PROPERTIES = new DataServicesCoreProperties();
	@NonNull private static Optional<DataServicesCoreProperties> boundProperties = Optional.empty();

	@Override public final ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		DataServicesCoreProperties dataServicesCoreProperties = AbstractDataServicesCondition.findBoundProperties(context.getEnvironment());
		return new ConditionOutcome(evaluateCondition(dataServicesCoreProperties),
			ConditionMessage.forCondition(ConditionalOnExpression.class, validate(dataServicesCoreProperties))
				.resultedIn(getStringRepresentation(dataServicesCoreProperties)));
	}

	/**
	 * Get condition state string representation to be used in messages.
	 *
	 * @param dataServicesCoreProperties {@link DataServicesCoreProperties} with actual values
	 * @return String representation of the condition state
	 */
	protected abstract String getStringRepresentation(DataServicesCoreProperties dataServicesCoreProperties);

	/**
	 * Evaluates the condition state.
	 *
	 * @param dataServicesCoreProperties {@link DataServicesCoreProperties} with actual values
	 * @return whether the condition is true or false
	 */
	protected abstract boolean evaluateCondition(DataServicesCoreProperties dataServicesCoreProperties);

	/**
	 * Validate property values and report nonsense configuration.
	 *
	 * @param dataServicesCoreProperties {@link DataServicesCoreProperties} with actual values
	 * @return {@link  ConfigurationMessage} for {@link SpringBootCondition}
	 */
	protected abstract ConfigurationMessage validate(DataServicesCoreProperties dataServicesCoreProperties);

	protected ConfigurationMessage makeWarnMessage(String message, String... relatedProperties) {
		return makeMessageInternal(message, Arrays.asList(relatedProperties), true);
	}

	protected ConfigurationMessage makeValidMessage(String message, String... relatedProperties) {
		return makeMessageInternal(message, Arrays.asList(relatedProperties), false);
	}

	private ConfigurationMessage makeMessageInternal(String message, List<String> relatedProperties, boolean warn) {
		ConfigurationMessage msg = (warn ? WrongConfigurationMessage.builder() : ConfigurationMessage.builder())
			.message(message)
			.relatedProperties(relatedProperties)
			.build();
		if (warn && !EXISTING_WARNINGS.contains(msg)) {
			log.warn(msg.toString());
			EXISTING_WARNINGS.add(msg);
		}
		return msg;
	}

	/**
	 * Detects whether queried value is on its default value or if it is overwritten by properties.
	 *
	 * @param props  {@link DataServicesCoreProperties} with actual values
	 * @param getter {@link Function} to get the tested value from {@link DataServicesCoreProperties}
	 * @param <T>    type of the property value
	 * @return whether the actual value equals to the default one
	 */
	protected <T> boolean isDefault(DataServicesCoreProperties props, Function<DataServicesCoreProperties, T> getter) {
		return Objects.equals(getter.apply(DEFAULT_PROPERTIES), getter.apply(props));
	}

	/**
	 * Function to get actual values of {@link DataServicesCoreProperties}.
	 * Falls back to a default instance when no {@code mgmtp.a12.dataservices.*} property is present.
	 *
	 * @param environment spring {@link Environment}
	 * @return actual {@link DataServicesCoreProperties}, never {@code null}
	 */
	public static @NonNull DataServicesCoreProperties findBoundProperties(Environment environment) {
		if (boundProperties.isEmpty()) {
			boundProperties = Optional.of(Binder.get(environment)
				.bindOrCreate(DataServicesCoreProperties.PROPERTIES_PREFIX, DataServicesCoreProperties.class));
		}
		return boundProperties.get();
	}
}
