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
package com.mgmtp.a12.dataservices.model.document;

import org.springframework.test.context.TestPropertySource;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.model.GenericModel;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH;
import static org.testng.AssertJUnit.assertTrue;

@TestPropertySource(properties = {
	"mgmtp.a12.dataservices.authorization.roleBased.enabled=false"
})
public class DocumentModelRoleBaseDisabledIT extends AbstractSpringContextIT {

	@Test
	public void CRUDOnModelSuccessWithoutRole() {
		final String withoutRoleDocumentModelString = removeRoleAnnotationFromBusinessPartnerModel();

		GenericModel model = modelService.create(withoutRoleDocumentModelString);

		String updateContent = "__Updated_Industry or business sector_Updated__";
		String updateModelStr = withoutRoleDocumentModelString.replace("Industry or business sector", updateContent);
		modelService.update(updateModelStr);
		String storedModel = genericModelLoader.loadModel(BUSINESS_PARTNER_DOCUMENT_MODEL).getContent().getRawContent();
		assertTrue(storedModel.contains(updateContent));

		assertTrue(modelPermissionEvaluator.hasModelReadPermission(model.getHeader()));

		modelService.delete(model.getHeader().getId());
	}

	private String removeRoleAnnotationFromBusinessPartnerModel() {
		return loadResourceFromClasspathAsString(BUSINESS_PARTNER_DOCUMENT_MODEL_PATH)
			.replace("""
					      {
					        "name": "roles",
					        "value": "%s"
					      },
					""".formatted("admin,guest,ModelRead"),
				"");
	}
}
