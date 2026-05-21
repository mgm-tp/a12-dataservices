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
package com.mgmtp.a12.dataservices.marshalling;

import java.io.IOException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;

/**
 * Deserializes a JSON array into a set of raw JSON element strings.
 * Each array element is converted to its `toString()` JSON representation and collected into a {@link Set}.
 */
public class JsonRawValuesSetDeserializer extends JsonDeserializer<Set<String>> {

	/**
	 * Deserializes the current JSON token as a set of raw JSON strings.
	 *
	 * @param parser the Jackson parser positioned at the value to read; never null.
	 * @param deserializationContext the context for deserialization; never null.
	 * @return a set containing the raw string representation of each array element.
	 * @throws IOException if reading from the parser fails.
	 * @throws com.mgmtp.a12.dataservices.common.exception.InvalidInputException if the current token is not an array.
	 */
	@Override public Set<String> deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
		TreeNode treeNode = parser.getCodec().readTree(parser);
		if (treeNode.isArray()) {
			return stream((ArrayNode) treeNode)
				.map(JsonNode::toString)
				.collect(Collectors.toSet());
		} else {
			throw new InvalidInputException(ExceptionKeys.DOCUMENT_INTEGRITY_ERROR_KEY, String.format("Not a JSON array: %s", treeNode.toString()))
				.withAnonymityMessage("Deserialization of JSON array failed.");
		}
	}

	private static Stream<JsonNode> stream(ArrayNode treeNode) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(treeNode.elements(), Spliterator.ORDERED), false);
	}

}

