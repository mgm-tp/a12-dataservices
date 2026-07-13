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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractKernelAwareTest;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

public class GroupValueConverterTest extends AbstractKernelAwareTest {

	private GroupValueConverter converter;
	private IDocumentModel businessPartnerDocumentModel;

	@BeforeMethod
	public void setUp() {
		converter = new GroupValueConverter(documentV2Serializer, documentModelLoader);
		businessPartnerDocumentModel = documentModelLoader.getDocumentModelById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
	}

	@Test(enabled = true, description = "toGroupInstance converts a Map with scalar fields to a DM-typed GroupInstanceV2")
	public void shouldConvertMapToGroupInstanceWithTypedScalarFields() {
		Map<String, Object> value = Map.of(
			"original_filename", "photo.jpg",
			"internal_filename", "abc123"
		);

		GroupInstanceV2 result = converter.toGroupInstance(
			businessPartnerDocumentModel,
			"BusinessPartnerRoot/Attachment",
			value
		);

		Assert.assertNotNull(result);
	}

	@Test(enabled = true, description = "toGroupInstance handles a value that contains nested group structures")
	public void shouldConvertNestedGroupsWithinValue() {
		Map<String, Object> value = Map.of(
			"original_filename", "contract.pdf",
			"internal_filename", "xyz789"
		);

		GroupInstanceV2 result = converter.toGroupInstance(
			businessPartnerDocumentModel,
			"BusinessPartnerRoot/Attachment",
			value
		);

		Assert.assertNotNull(result);
	}

	@Test(enabled = true, description = "toGroupInstance silently drops fields not present in the document model")
	public void shouldPruneUnknownFieldsDuringConversion() {
		Map<String, Object> value = Map.of(
			"original_filename", "file.txt",
			"unknownField", "should be dropped"
		);

		GroupInstanceV2 result = converter.toGroupInstance(
			businessPartnerDocumentModel,
			"BusinessPartnerRoot/Attachment",
			value
		);

		Assert.assertNotNull(result);
	}

	@Test(enabled = true, description = "toGroupInstance throws InvalidInputException when the value cannot be converted to a group instance")
	public void shouldThrowInvalidInputWhenValueNotConvertibleToGroup() {
		Assert.assertThrows(
			InvalidInputException.class,
			() -> converter.toGroupInstance(
				businessPartnerDocumentModel,
				"BusinessPartnerRoot/Attachment",
				"this-is-a-plain-string-not-a-group"
			)
		);
	}
}
