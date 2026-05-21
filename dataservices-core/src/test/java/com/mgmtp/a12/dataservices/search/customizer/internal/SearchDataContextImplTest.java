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
package com.mgmtp.a12.dataservices.search.customizer.internal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class SearchDataContextImplTest {

	private IDocumentModelSearchService documentModelSearchService;
	private DocumentV2 indexableDocument;
	private SearchDataContextImpl context;
	private static final String MODEL_NAME = "TestModel";

	@BeforeMethod public void setUp() {
		documentModelSearchService = mock(IDocumentModelSearchService.class);
		indexableDocument = mock(DocumentV2.class);
		context = new SearchDataContextImpl(documentModelSearchService, indexableDocument, MODEL_NAME, "/initial/data~initial value~/");
	}

	@Test public void testGetCurrentSearchData() {
		assertEquals(context.getCurrentSearchData(), "/initial/data~initial value~/");
	}

	@Test public void testGetDocumentModelSearchService() {
		assertEquals(context.getDocumentModelSearchService(), documentModelSearchService);
	}

	@Test public void testGetIndexableDocument() {
		assertEquals(context.getIndexableDocument(), indexableDocument);
	}

	@Test public void testGetModelName() {
		assertEquals(context.getModelName(), MODEL_NAME);
	}

	@Test public void testAppendToSearchDataWithoutTrailingSlash() {
		context.appendToSearchData("/additional/data~additional value~");
		assertEquals(context.getCurrentSearchData(), "/initial/data~initial value~/additional/data~additional value~/");
	}

	@Test public void testAppendToSearchDataWithLeadingSlashRemoval() {
		context.appendToSearchData("/additional/data~additional value~/");
		assertEquals(context.getCurrentSearchData(), "/initial/data~initial value~/additional/data~additional value~/");
	}

	@Test public void testSearchDataWithoutTrailingSlashGetsSlashAdded() {
		SearchDataContextImpl context = new SearchDataContextImpl(documentModelSearchService, indexableDocument, MODEL_NAME, "/initial~initial value~");
		context.appendToSearchData("/additional~additional value~");

		assertEquals(context.getCurrentSearchData(), "/initial~initial value~/additional~additional value~/");
	}

	@Test public void testAppendToSearchDataMultipleTimes() {
		context.appendToSearchData("/first~first value~");
		context.appendToSearchData("/second~second value~");
		context.appendToSearchData("/third~third value~");

		assertEquals(context.getCurrentSearchData(), "/initial/data~initial value~/first~first value~/second~second value~/third~third value~/");
	}

	@Test public void testAppendToSearchDataWithNull() {
		String originalData = context.getCurrentSearchData();
		context.appendToSearchData(null);

		assertEquals(context.getCurrentSearchData(), originalData);
	}

	@Test public void testAppendToSearchDataWithEmptyString() {
		String originalData = context.getCurrentSearchData();
		context.appendToSearchData("");

		assertEquals(context.getCurrentSearchData(), originalData);
	}

	@Test public void testReplaceSearchData() {
		context.replaceSearchData("/completely/new/data~new value~");

		assertEquals(context.getCurrentSearchData(), "/completely/new/data~new value~/");
	}

	@Test public void testReplaceSearchDataMultipleTimes() {
		context.replaceSearchData("/first/replacement~first replacement value~");
		context.replaceSearchData("/second/replacement~second replacement value~");

		assertEquals(context.getCurrentSearchData(), "/second/replacement~second replacement value~/");
	}

	@Test public void testAppendAfterReplace() {
		context.replaceSearchData("/replaced~replaced value~");
		context.appendToSearchData("/appended~appended value~");

		assertEquals(context.getCurrentSearchData(), "/replaced~replaced value~/appended~appended value~/");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testConstructorWithNullSearchData() {
		new SearchDataContextImpl(documentModelSearchService, indexableDocument, MODEL_NAME, null);
	}

	@Test public void testComplexScenario() {
		context.appendToSearchData("/field1~field1 value~/");
		context.appendToSearchData("field2~field2 value~");
		context.appendToSearchData("/field3/value~field3 value~/");

		assertEquals(context.getCurrentSearchData(), "/initial/data~initial value~/field1~field1 value~/field2~field2 value~/field3/value~field3 value~/");

		context.replaceSearchData("/newdata~new value~");
		context.appendToSearchData("/additional~additional value~");

		assertEquals(context.getCurrentSearchData(), "/newdata~new value~/additional~additional value~/");
	}
}
