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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.configuration.internal.validation.ConfigurationMessage;
import com.mgmtp.a12.contentstore.configuration.internal.validation.WrongConfigurationMessage;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractContentStoreCondition extends SpringBootCondition {

	protected static final String CONTENT_STORAGE_PROPERTY_NAME = "mgmtp.a12.dataservices.contentstore.storage.contentStorage";
	protected static final String CONTENT_STORE_BASE_URL = "mgmtp.a12.dataservices.contentstore.baseUrl";
	protected static final String ENABLE_DEFAULT_DOWNLOAD_LISTENER_PROPERTY_NAME = "mgmtp.a12.dataservices.contentstore.enableDefaultDownloadListener";

	@NonNull private static Optional<ContentStoreProperties> boundProperties = Optional.empty();

	@Override public final ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		ContentStoreProperties contentStoreProperties = AbstractContentStoreCondition.findBoundProperties(context.getEnvironment());
		String representationStr = getStringRepresentation(contentStoreProperties);
		log.debug(representationStr);
		return new ConditionOutcome(evaluateCondition(contentStoreProperties),
			ConditionMessage.forCondition(ConditionalOnExpression.class, validate(contentStoreProperties))
				.resultedIn(representationStr));
	}

	/**
	 * Get condition state string representation to be used in messages.
	 *
	 * @param contentStoreProperties {@link ContentStoreProperties} with actual values
	 * @return String representation of the condition state
	 */
	protected abstract String getStringRepresentation(ContentStoreProperties contentStoreProperties);

	/**
	 * Evaluates the condition state.
	 *
	 * @param contentStoreProperties {@link ContentStoreProperties} with actual values
	 * @return whether the condition is true or false
	 */
	protected abstract boolean evaluateCondition(ContentStoreProperties contentStoreProperties);

	/**
	 * Validate property values and report nonsense configuration.
	 *
	 * @param contentStoreProperties {@link ContentStoreProperties} with actual values
	 * @return {@link  ConfigurationMessage} for {@link SpringBootCondition}
	 */
	protected abstract ConfigurationMessage validate(ContentStoreProperties contentStoreProperties);

	/**
	 * Function to get actual values of {@link ContentStoreProperties}.
	 * Falls back to a default instance when no {@code mgmtp.a12.dataservices.contentstore.*} property is present.
	 *
	 * @param environment spring {@link Environment}
	 * @return actual {@link ContentStoreProperties}, never {@code null}
	 */
	public static @NonNull ContentStoreProperties findBoundProperties(Environment environment) {
		if (boundProperties.isEmpty()) {
			boundProperties = Optional.of(Binder.get(environment)
				.bindOrCreate(ContentStoreProperties.PROPERTIES_PREFIX, ContentStoreProperties.class));
		}
		return boundProperties.get();
	}

	protected ConfigurationMessage makeValidMessage(String message, String... relatedProperties) {
		return makeMessageInternal(message, Arrays.asList(relatedProperties), false);
	}

	private ConfigurationMessage makeMessageInternal(String message, List<String> relatedProperties, boolean warn) {
		ConfigurationMessage msg = (warn ? WrongConfigurationMessage.builder() : ConfigurationMessage.builder())
			.message(message)
			.relatedProperties(relatedProperties)
			.build();
		if (warn && !WrongConfigurationMessage.EXISTING_WARNINGS.contains(msg)) {
			log.warn(msg.toString());
			WrongConfigurationMessage.EXISTING_WARNINGS.add(msg);
		}
		return msg;
	}

	protected ConfigurationMessage makeWarnMessage(String message, String... relatedProperties) {
		return makeMessageInternal(message, Arrays.asList(relatedProperties), true);
	}

}
