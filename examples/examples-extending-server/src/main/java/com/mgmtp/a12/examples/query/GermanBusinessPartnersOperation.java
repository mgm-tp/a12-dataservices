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
package com.mgmtp.a12.examples.query;

import java.util.List;

import org.springframework.stereotype.Component;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Example of a custom German Business Partners Operation. An example is based on {@link QueryRoot} extension with a constraint
 *
 */
@RemoteOperation(name = "GERMAN_BUSINESS_PARTNERS", group = "EXAMPLES")
@Component
@AllArgsConstructor
public class GermanBusinessPartnersOperation {

	private final QueryService queryService;

	/**
	 * Executes a query constrained to German business partners by augmenting the given {@link QueryRoot}.
	 *
	 * @param <T> unused type parameter (present for demonstration only).
	 * @param query the original query root to be executed; must not be null.
	 * @return the projected results containing only German business partners.
	 */
	public <T> List<DocumentTreeResult> rpc(@NonNull @JsonRpcParam("query") QueryRoot query) {
		AndOperator newConstraint = AndOperator.builder()
			.operands(List.of(germanBusinessPartnersOnly()))
			.build();
		query.setConstraint(newConstraint);
		QueryPage<DocumentTreeResult> resolvedQuery = queryService.query(query, null);

		return resolvedQuery.getContent().stream().toList();
	}

	private HasOperator germanBusinessPartnersOnly() {
		return HasOperator.builder()
			.relationshipModel("PartnerAddresses")
			.targetRole("Address")
			.constraint(ExactMatchOperator.builder()
				.field("/AddressRoot/Country")
				.value("Germany")
				.build())
			.build();
	}
}
