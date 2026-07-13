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
package com.mgmtp.a12.dataservices;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;

public class JsonFunctions {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * @param jsonString  the JSON string to modify
	 * @param jsonPointer the JSON Pointer path (e.g., "/user/name" or "/users/0/name")
	 * @param newValue    the new value to set
	 * @param prettyPrint whether to use pretty printing for the output
	 * @return the modified JSON string
	 * @throws IOException              if JSON parsing or generation fails
	 * @throws IllegalArgumentException if the path is invalid or doesn't exist
	 */
	public static String replaceValue(
			@NotNull String jsonString, @NotNull String jsonPointer, String newValue, boolean prettyPrint
	) throws IOException {
		JsonNode rootNode = OBJECT_MAPPER.readTree(jsonString);
		replaceValueInNode(rootNode, jsonPointer, newValue);
		return prettyPrint
				? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode)
				: OBJECT_MAPPER.writeValueAsString(rootNode);
	}

	private static void replaceValueInNode(JsonNode root, String path, String newValue) {
		int lastSlash = path.lastIndexOf('/');
		String parentPath = path.substring(0, lastSlash);
		String fieldName = path.substring(lastSlash + 1);

		JsonNode parentNode = root.at(parentPath);

		if (parentNode instanceof ObjectNode node) {
			node.put(fieldName, newValue);
		}
	}
}
