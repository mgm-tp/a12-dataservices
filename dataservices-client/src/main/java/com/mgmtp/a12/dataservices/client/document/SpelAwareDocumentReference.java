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
package com.mgmtp.a12.dataservices.client.document;

import java.util.regex.Pattern;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Document reference that is aware of SpEL when JSON-RPC request is created using Java Client for example.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpelAwareDocumentReference extends DocumentReference {

	private String content;

	/**
	 * Creates a new document reference that preserves SpEL expressions or parses a standard {@link DocumentReference}.
	 *
	 * @param docRef either a SpEL expression (e.g., `#{#...}`) or a canonical document reference string.
	 */
	public SpelAwareDocumentReference(String docRef) {
		if (Pattern.compile("#\\{#.*}").matcher(docRef).matches()) {
			this.content = docRef;
		} else {
			setIfValid(docRef);
		}
	}

	/**
	 * {@inheritDoc}
	 * Returns the canonical document reference if available; otherwise returns the raw SpEL content, or an empty string if neither is present.
	 */
	@Override public String toString() {
		if (StringUtils.isNotEmpty(getDocumentModelName()) && StringUtils.isNotEmpty(getDocumentId())) {
			return super.toString();
		} else if (StringUtils.isNotEmpty(getContent())) {
			return getContent();
		} else {
			return "";
		}
	}
}
