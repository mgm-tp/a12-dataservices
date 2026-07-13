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
package com.mgmtp.a12.dataservices.internal.query;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.springframework.test.context.TestPropertySource;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.SimpleSearchOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

@TestPropertySource(properties = "mgmtp.a12.dataservices.query.validation.enabled=false")
public class LeadingSlashAndLocaleQueryIT extends AbstractSpringContextIT {

	// "draft" is the enum key for the "blueprint" label in the Contract model StatusType.
	private static final String STATUS_KEY = "draft";
	// "blueprint" is the EN localized label for the "draft" enum key in the Contract model StatusType.
	private static final String STATUS_LABEL_EN = "blueprint";

	@BeforeMethod
	public void init() throws Exception {
		super.cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		documentFunctions.createDocumentFromFileAndGetDocRef("Contract", "document/ContractWithChangeLog.json");
	}

	@Test(description = "ExactMatch must throw QueryInvalidInputException during enrichment when the field path has no leading slash")
	public void shouldThrowExceptionForExactMatchWhenFieldPathHasNoLeadingSlash() {
		QueryInvalidInputException exception = expectThrows(QueryInvalidInputException.class,
			() -> runExactMatchQuery(DocumentModelConstants.STATUS_FIELD_PATH));
		assertTrue(exception.getMessage().contains("Field path must start with a leading slash: " + DocumentModelConstants.STATUS_FIELD_PATH));
	}

	@Test(description = "ExactMatch should return documents when the field path has a leading slash")
	public void shouldReturnResultsForExactMatchWhenFieldPathHasLeadingSlash() {
		QueryPage<DocumentTreeResult> result = runExactMatchQuery("/" + DocumentModelConstants.STATUS_FIELD_PATH);

		assertFalse(result.getContent().isEmpty(),
			"ExactMatch query with leading slash in field path should return documents");
	}

	@Test(description = "SimpleSearch must throw QueryInvalidInputException during enrichment when the field path has no leading slash")
	public void shouldThrowExceptionForSimpleSearchWhenFieldPathHasNoLeadingSlash() {
		QueryInvalidInputException exception = expectThrows(QueryInvalidInputException.class,
			() -> runSimpleSearchQuery(DocumentModelConstants.STATUS_FIELD_PATH));
		assertTrue(exception.getMessage().contains("Field path must start with a leading slash: " + DocumentModelConstants.STATUS_FIELD_PATH));
	}

	@Test(description = "SimpleSearch with EN locale should return documents when the field path has a leading slash")
	public void shouldReturnResultsForSimpleSearchWhenFieldPathHasLeadingSlash() {
		QueryPage<DocumentTreeResult> result = runSimpleSearchQuery("/" + DocumentModelConstants.STATUS_FIELD_PATH);

		assertFalse(result.getContent().isEmpty(),
			"SimpleSearch query with leading slash must return at least one document when searching by the EN localized label");
	}

	private QueryPage<DocumentTreeResult> runExactMatchQuery(String fieldPath) {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(ExactMatchOperator.builder()
				.field(fieldPath)
				.value(STATUS_KEY)
				.build())
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.projectionName("document")
			.build();
		return queryService.query(query, null);
	}

	private QueryPage<DocumentTreeResult> runSimpleSearchQuery(String fieldPath) {
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.constraint(SimpleSearchOperator.builder()
				.value(STATUS_LABEL_EN)
				.fields(List.of(fieldPath))
				.build())
			.paging(Paging.builder().pageNumber(0).pageSize(10).build())
			.projectionName("document")
			.build();
		return queryService.query(query, DocumentModelConstants.SearchConstants.EN_LOCALE);
	}
}
