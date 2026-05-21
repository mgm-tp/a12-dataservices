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
package com.mgmtp.a12.examples.authorization;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.security.IQueryResultAuthorization;

import lombok.extern.slf4j.Slf4j;

/**
 * Example shows how to provide only authorized subset of Query result. The decision must be taken based on the model name
 * of DocumentTreeEntity. In case of the cdd projection the further parsing of the CDD might be necessary to find proper fields
 * if needed for authorization.
 */
@Slf4j
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "black-box-authorization-example.enabled", havingValue = "true")
@Component public class NoFirstElementAuthorization implements IQueryResultAuthorization {

	/**
	 * Filters out the first element if the list contains more than one element; otherwise returns an empty list.
	 *
	 * @param documentTreeResults an unmodifiable list of {@link DocumentTreeResult}; never null.
	 * @return a list containing only authorized results; never null.
	 */
	@Override public List<DocumentTreeResult> authorizeQueryResult(List<DocumentTreeResult> documentTreeResults) {
		return provideOnlyAuthorizedResults(documentTreeResults);
	}

	private List<DocumentTreeResult> provideOnlyAuthorizedResults(List<DocumentTreeResult> documentTreeResults) {
		if (documentTreeResults.isEmpty() || documentTreeResults.size() == 1) {
			return List.of();
		}
		DocumentTreeResult removedResult = documentTreeResults.getFirst();
		log.info("Result {} was removed from result set due to Black box authorization",removedResult);
		//documentTreeResults is an unmodifiable list, so we need to create a new one
		return documentTreeResults.subList(1, documentTreeResults.size());
	}
}
