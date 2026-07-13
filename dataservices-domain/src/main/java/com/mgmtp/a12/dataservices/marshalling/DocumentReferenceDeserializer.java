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


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes a document reference from either a plain string or an object with a `docRef` field.
 * Accepts JSON input in one of the following forms:
 *
 * - `"model/documentId"`
 * - `{"docRef":"model/documentId"}`
 */
public class DocumentReferenceDeserializer extends StdDeserializer<String> {
	protected DocumentReferenceDeserializer() {
		super(String.class);
	}

	/**
	 * Deserializes the current JSON token into a document reference string.
	 *
	 * @param p the Jackson parser positioned at the value to read; never null.
	 * @param ctxt the deserialization context; never null.
	 * @return the document reference string (e.g., `model/documentId`).
	 * @throws JacksonException if reading from the parser fails.
	 * @throws JacksonException if the input structure is not recognized.
	 */
	@Override public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		if (p.hasToken(JsonToken.VALUE_STRING)) {
			return p.getString();
		}
		if (p.hasToken(JsonToken.START_OBJECT) && p.nextName().equals("docRef")) {
			String value = p.nextStringValue();
			p.nextToken();
			if (p.hasToken(JsonToken.END_OBJECT)) {
				return value;

			}
		}
		throw new StreamReadException(p, "Unable to parse Document Reference.");
	}
}

