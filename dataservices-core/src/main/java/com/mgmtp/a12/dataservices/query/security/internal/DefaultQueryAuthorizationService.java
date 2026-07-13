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
package com.mgmtp.a12.dataservices.query.security.internal;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryJsonParsingException;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.exception.MissingPermissionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for applying authorization rules to a query, e.g. ABAC constraints injection.
 */
@Slf4j
@RequiredArgsConstructor
@Service public class DefaultQueryAuthorizationService implements QueryAuthorizationService {

	public static final String QUERY_PERMISSION_SCOPE = "Query";

	private final AuthorizationService authorizationService;
	private final ObjectMapper objectMapper;

	/**
	 * Adds the constraints that are defined in an authorizationDefinition.json for this document model to the passed constraint.
	 * @param constraint The original constraint.
	 * @param documentModel The document model for which ABAC rules should be applied.
	 * @return The effective constraint that contains the ABAC constraints.
	 */
	@Override public ILogicOperator addAbacRules(ILogicOperator constraint, String documentModel) {
		if (documentModel == null) {
			return constraint;
		}
		Set<ILogicOperator> permissionOperators = new HashSet<>();
		try {
			Set<String> repositoryPermissions = authorizationService.generateRepositoryPermissions(documentModel, QUERY_PERMISSION_SCOPE, null);
			for (String repositoryPermission : repositoryPermissions) {
				try {
					ILogicOperator operator = objectMapper.readValue(repositoryPermission, ILogicOperator.class);
					permissionOperators.add(operator);
				} catch (Exception e) {
					throw new QueryJsonParsingException(ExceptionKeys.ExecutionPhase.QUERY_ENRICHMENT, ExceptionKeys.SECURITY_INVALID_ABAC_RULE_ERROR_KEY,
						"Could not parse ABAC rule [%s] to query operator".formatted(repositoryPermission), e).withAnonymityMessage("ABAC rules could not be parsed.");
				}
			}
		} catch (MissingPermissionException e) {
			log.info("Caught MissingPermissionException for scope [%s], proceeded without ABAC injection for resource [%s], original message was [%s]"
				.formatted(QUERY_PERMISSION_SCOPE, documentModel, e.getMessage()));
		}
		return concatWithAnd(constraint, permissionOperators);
	}

	private ILogicOperator concatWithAnd(ILogicOperator constraint, Set<ILogicOperator> operators) {
		if (constraint != null) {
			operators.add(constraint);
		}
		if (CollectionUtils.isNotEmpty(operators)) {
			if (operators.size() == 1) {
				return operators.iterator().next();
			} else {
				return AndOperator.builder()
					.operands(operators)
					.build();
			}
		}
		return constraint;
	}
}
