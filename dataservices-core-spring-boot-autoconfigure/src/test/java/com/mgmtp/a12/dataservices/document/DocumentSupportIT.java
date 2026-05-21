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
package com.mgmtp.a12.dataservices.document;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

public class DocumentSupportIT extends AbstractSpringContextIT {

	@BeforeMethod
	public void init() throws Exception {
		super.cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@Test public void testConvertingJSONToDocument() throws Exception {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-1.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));

		Assert.assertNotNull(document);
		Assert.assertEquals(document.fieldValue("/BusinessPartnerRoot/Name"), "Microoogle");
		Assert.assertEquals(document.fieldValue("/BusinessPartnerRoot/Attachment/original_filename"), "originalFileName");
	}

	@Test public void testConvertingDocumentToJson() throws Exception {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-1.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));

		StringWriter writer = new StringWriter();
		documentSupport.convertDocumentToJSON(document, writer);
		String convertedJsonDocument = writer.toString();

		JSONAssert.assertEquals(jsonDocument, convertedJsonDocument, false);
	}

	@Test public void testConvertingToDocumentSpec() throws Exception {
		DataServicesDocument dataServicesDocument =
				documentFunctions.createDocumentFromFile(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-1.json");

		DocumentSpec documentSpec = documentSupport.convertToDocumentSpec(dataServicesDocument);

		Assert.assertNotNull(documentSpec.getDocRef().getDocumentId());
		Assert.assertEquals(documentSpec.getDocRef().getDocumentModelName(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		JSONAssert.assertEquals(
				documentFunctions.convertDocumentToJson(dataServicesDocument),
				documentSpec.getDocument(),
				false
		);
	}

	@Test public void testResolvingLocaleInDocument() throws Exception {
		Locale firstLocale = Locale.ENGLISH;
		String jsonDocument = resourceFunctions.loadResource(PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-1.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));

		Locale locale = documentSupport.resolveLocale(document, null, false);

		// should return the first found locale
		Assert.assertEquals(locale, firstLocale);
	}

	@Test public void testResolvingLocaleInDocumentWithPreferredLocale() throws Exception {
		Locale firstLocale = Locale.ENGLISH;
		Locale nonExistingLocale = Locale.CHINESE;
		Locale existingLocale = Locale.GERMAN;
		String jsonDocument = resourceFunctions.loadResource(PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-1.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));

		Locale expectedLocale = documentSupport.resolveLocale(document, existingLocale, false);
		// should return the preferred local if it exists.
		Assert.assertEquals(expectedLocale, existingLocale);

		expectedLocale = documentSupport.resolveLocale(document, nonExistingLocale, true);
		// should return the first locale if the preferred locale does not exist and skipNonExisting flag is set.
		Assert.assertEquals(expectedLocale, firstLocale);

		expectedLocale = documentSupport.resolveLocale(document, nonExistingLocale, false);
		// should return the nonExistingLocale if the preferred locale does not exist and skipNonExisting flag is not set.
		Assert.assertEquals(expectedLocale, nonExistingLocale);
	}

	@Test(expectedExceptions = DataServicesDocumentProblemReporterException.class)
	public void testInvalidDocument() throws IOException {
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_VALIDATION_PATH + "ValidationModel.json");
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_KERNEL_VALIDATION_PATH + "ValidationModelDocument.json");

		documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));
	}
}
