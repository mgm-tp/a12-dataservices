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
package com.mgmtp.a12.dataservices.cdd.internal;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeleton;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderFactory;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_QUERY_ROOT_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.RELATIONSHIP_MODEL_TYPE;
import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;

public class CddSupportTest extends AbstractCdmTest {
	@DataProvider public static Object[][] cdmProvider() {
		return new Object[][] {
			new Object[] { "CdmWithEmptyRoot", false,
				DOCUMENT_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_QUERY_ROOT_ANNOTATION, "") },
			new Object[] { "CdmWithFilledRoot", true,
				DOCUMENT_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_QUERY_ROOT_ANNOTATION, "RootModel") },
			new Object[] { "NotDocumentModel", false,
				RELATIONSHIP_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_QUERY_ROOT_ANNOTATION, "RootModel") },
			new Object[] { "NoCdm", false,
				DOCUMENT_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_RELATIONSHIP_ANNOTATION, "AnyRelationship") },
		};
	}

	@Test(dataProvider = "cdmProvider")
	public void testIsComposeDocumentModel(String id, boolean isCdm, String type, Annotation... annotations) {
		Header header = new HeaderFactory.Builder()
			.withId(id)
			.withModelVersion("28.0.0")
			.withModelType(type)
			.withAnnotations(annotations)
			.build();
		assertEquals(ComposeDocumentModelUtils.isComposeDocumentModel(header), isCdm);
	}

	@DataProvider public static Object[][] dmProvider() {
		return new Object[][] {
			new Object[] { "CdmWithEmptyRoot", true,
				DOCUMENT_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_QUERY_ROOT_ANNOTATION, "") },
			new Object[] { "CdmWithFilledRoot", false,
				DOCUMENT_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_QUERY_ROOT_ANNOTATION, "RootModel") },
			new Object[] { "NotDocumentModel", false,
				RELATIONSHIP_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_QUERY_ROOT_ANNOTATION, "RootModel") },
			new Object[] { "NoCdm", true,
				DOCUMENT_MODEL_TYPE, HeaderFactory.createAnnotation(CDM_RELATIONSHIP_ANNOTATION, "AnyRelationship") },
		};
	}

	@Test(dataProvider = "dmProvider")
	public void testIsDocumentModel(String id, boolean isCdm, String type, Annotation... annotations) {
		Header header = new HeaderFactory.Builder()
			.withId(id)
			.withModelVersion("28.0.0")
			.withModelType(type)
			.withAnnotations(annotations)
			.build();
		assertEquals(ComposeDocumentModelUtils.isDocumentModel(header), isCdm);
	}

	@Test public void testTraverseGroups() {
		withCdm(CONTRACTCDM_FILE, cdm -> {
			try {
				CddSkeleton cdmSkeleton = CddSkeletonFactory.constructSkeletonFromCdm(cdm);
				JSONAssert.assertEquals(
					IOUtils.toString(requireNonNull(getClass().getResourceAsStream(CONTRACTSKELETON_FILE)), Charset.defaultCharset()),
					new ObjectMapper().registerModules(new Jdk8Module()).writeValueAsString(cdmSkeleton), false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
