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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component public class JsonUtils {

	private final ObjectMapper objectMapper;

	public JsonUtils(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public JsonNode copyNode(JsonNode sourceNode, Predicate<? super JsonNode> condition, Function<? super JsonNode, ? extends JsonNode> convertor) {
		if (condition.test(sourceNode)) {
			return convertor.apply(sourceNode);
		} else if (sourceNode.isContainerNode()) {
			if (sourceNode.isObject()) {
				return cloneObjectNode(sourceNode, condition, convertor);
			} else if (sourceNode.isArray()) {
				return cloneArrayNode(sourceNode, condition, convertor);
			} else {
				throw new IllegalStateException(String.format("Unexpected container node type %s", sourceNode.getNodeType()));
			}
		} else if (sourceNode.isValueNode()) {
			return sourceNode.deepCopy();
		} else {
			throw new IllegalStateException(String.format("Unexpected node type %s", sourceNode.getNodeType()));
		}
	}

	private ArrayNode cloneArrayNode(JsonNode sourceNode, Predicate<? super JsonNode> condition, Function<? super JsonNode, ? extends JsonNode> convertor) {
		ArrayNode targetNode = objectMapper.createArrayNode();
		sourceNode.elements().forEachRemaining(e -> targetNode.add(copyNode(e, condition, convertor)));
		return targetNode;
	}

	private ObjectNode cloneObjectNode(JsonNode sourceNode, Predicate<? super JsonNode> condition, Function<? super JsonNode, ? extends JsonNode> convertor) {
		ObjectNode targetNode = objectMapper.createObjectNode();
		sourceNode.fields().forEachRemaining(e -> targetNode.set(e.getKey(), copyNode(e.getValue(), condition, convertor)));
		return targetNode;
	}

}
