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

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.internal.configuration.SmeWorkspaceProperties;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.SME_WORKSPACE_PROPERTY_PATH;

@Slf4j
public abstract class AbstractSmeWorkspaceCondition extends SpringBootCondition {

	@Override public final ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		SmeWorkspaceProperties workspaceProperties = AbstractSmeWorkspaceCondition.findBoundProperties(context.getEnvironment());
		return new ConditionOutcome(evaluateCondition(workspaceProperties),
			ConditionMessage.forCondition(ConditionalOnExpression.class, validate(workspaceProperties))
				.resultedIn(getStringRepresentation(workspaceProperties)));
	}

	/**
	 * Get condition state string representation to be used in messages.
	 *
	 * @param workspaceProperties {@link SmeWorkspaceProperties} with actual values
	 * @return String representation of the condition state
	 */
	protected String getStringRepresentation(SmeWorkspaceProperties workspaceProperties) {
		return getConfigurationMessage(workspaceProperties).getLeft();
	}

	protected Pair<String, List<String>> getConfigurationMessage(SmeWorkspaceProperties workspaceProperties) {
		return evaluateCondition(workspaceProperties) ?
			makeValidMessage(getEnabledMessage(), getProperty()) :
			makeValidMessage(getDisabledMessage(), getProperty());
	}

	protected boolean evaluateCondition(SmeWorkspaceProperties workspaceProperties) {
		return isEnabled(workspaceProperties);
	}

	protected abstract boolean isEnabled(SmeWorkspaceProperties workspaceProperties);

	protected Pair<String, List<String>> validate(SmeWorkspaceProperties workspaceProperties) {
		return getConfigurationMessage(workspaceProperties);
	}

	/**
	 * Function to get actual values of {@link SmeWorkspaceProperties}.
	 * Falls back to a default instance when no `mgmtp.a12.dataservices.*` SME workspace property is present.
	 *
	 * @param environment spring {@link Environment}
	 * @return actual {@link SmeWorkspaceProperties}, never `null`
	 */
	public static @NonNull SmeWorkspaceProperties findBoundProperties(Environment environment) {
		return Optional.of(environment)
			.map(Binder::get)
			.map(b -> b.bind(DataServicesCoreProperties.PROPERTIES_PREFIX + SME_WORKSPACE_PROPERTY_PATH, SmeWorkspaceProperties.class))
			.filter(BindResult::isBound)
			.map(BindResult::get)
			.orElse(new SmeWorkspaceProperties());
	}

	protected Pair<String, List<String>> makeValidMessage(String message, String... relatedProperties) {
		return makeMessageInternal(message, Arrays.asList(relatedProperties));
	}

	private Pair<String, List<String>> makeMessageInternal(String message, List<String> relatedProperties) {
		return Pair.of(message, relatedProperties);
	}

	protected abstract String getProperty();

	protected abstract String getDisabledMessage();

	protected abstract String getEnabledMessage();
}
