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
package com.mgmtp.a12.dataservices.query;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for {@link Order} that dispatches on the presence of
 * `"relationshipModel"`: JSON containing that property is deserialized as
 * {@link RelationshipOrder}; any other JSON is deserialized as {@link DirectFieldOrder}.
 */
class OrderDeserializer extends StdDeserializer<Order> {

	OrderDeserializer() {
		super(Order.class);
	}

	@Override
	public Order deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		JsonNode node = p.readValueAsTree();
		if (node.get("relationshipModel") != null) {
			if (node.get("field") != null) {
				if (node.get("sortBy") != null) {
					throw new QueryValidationException(ExceptionKeys.ExecutionPhase.QUERY_VALIDATION,
						ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE, ExceptionKeys.INVALID_QUERY_ERROR_KEY,
						"RelationshipOrder must not specify 'field'; use 'sortBy' with a DirectFieldOrder to specify the field to sort by");
				} else {
					throw new QueryValidationException(ExceptionKeys.ExecutionPhase.QUERY_VALIDATION,
						ExceptionCodes.QUERY_INVALID_INPUT_ERROR_CODE, ExceptionKeys.INVALID_QUERY_ERROR_KEY,
						"sortBy is required on RelationshipOrder");
				}
			}
			return ctxt.readTreeAsValue(node, RelationshipOrder.class);
		} else {
			return ctxt.readTreeAsValue(node, DirectFieldOrder.class);
		}
	}
}
