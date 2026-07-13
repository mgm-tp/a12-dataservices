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
package com.mgmtp.a12.dataservices.marshalling.internal;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import com.mgmtp.a12.dataservices.document.DocumentReference;

/**
 * Canonical Jackson deserializer for {@link DocumentReference}.
 *
 * Accepts JSON input in either of the following forms:
 *
 * * `"model/documentId"` (plain string)
 * * `{"docRef":"model/documentId"}` (object with `docRef` field)
 *
 * Any value that does not conform to one of these forms causes a {@link StreamReadException}.
 */
public class DocumentReferenceDeserializer extends StdDeserializer<DocumentReference> {

	public DocumentReferenceDeserializer() {
		super(DocumentReference.class);
	}

	/**
	 * Deserializes the current JSON token into a {@link DocumentReference}.
	 *
	 * @param p the Jackson parser positioned at the value to read; never null.
	 * @param ctxt the deserialization context; never null.
	 * @return the parsed {@link DocumentReference}.
	 * @throws StreamReadException if the input is not a recognized string or object form.
	 */
	@Override
	public DocumentReference deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		if (p.hasToken(JsonToken.VALUE_STRING)) {
			return parseDocRef(p, p.getString());
		}
		if (p.hasToken(JsonToken.START_OBJECT)) {
			String docRefValue = null;
			while (p.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = p.currentName();
				p.nextToken();
				if ("docRef".equals(fieldName)) {
					docRefValue = p.getText();
				}
			}
			if (docRefValue != null) {
				return parseDocRef(p, docRefValue);
			}
			throw new StreamReadException(p, "Unable to parse Document Reference - missing 'docRef' field.");
		}
		throw new StreamReadException(p, "Unable to parse Document Reference - expected string or object.");
	}

	private static DocumentReference parseDocRef(JsonParser p, String value) throws JacksonException {
		int slashIndex = value.indexOf('/');
		if (slashIndex <= 0 || slashIndex >= value.length() - 1) {
			throw new StreamReadException(p, "Unable to parse Document Reference: " + value);
		}
		String modelName = value.substring(0, slashIndex);
		String documentId = value.substring(slashIndex + 1);
		return new DocumentReference(modelName, documentId);
	}
}
