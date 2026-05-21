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
package com.mgmtp.a12.dataservices.model;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.relationship.ModelGraphDocumentModelElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;

import static com.mgmtp.a12.dataservices.constants.PathConstants.CDM_TEMPLATE_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_MODEL_SUBTYPES_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.SUPER_TYPE_MODEL_PATH;
import static com.mgmtp.a12.dataservices.constants.UserConstants.ADMIN_USER;

public abstract class AbstractModelServiceIT extends AbstractSpringContextIT {

	@BeforeClass
	public void init() {
		setUserTo(ADMIN_USER);
		modelsFunctions.createModel(SUPER_TYPE_MODEL_PATH);
		modelsFunctions.createModel(DOCUMENT_MODEL_SUBTYPES_PATH + "DateTestSubType1.json");
		modelsFunctions.createModel(DOCUMENT_MODEL_SUBTYPES_PATH + "DateTestSubType2.json");
		modelsFunctions.createModel(DOCUMENT_MODEL_SUBTYPES_PATH + "DateTestSubType3.json");
		modelsFunctions.saveCdm(CDM_TEMPLATE_PATH, "cdm1");
		modelsFunctions.saveCdm(CDM_TEMPLATE_PATH, "cdm2");
		modelsFunctions.saveCdm(CDM_TEMPLATE_PATH, "cdm3");
	}

	protected Set<String> getSubTypes(ModelGraphRoot modelGraphRoot, String documentModelName) {
		return modelGraphRoot.getDocumentModels().stream()
			.filter(e -> documentModelName.equals(e.getModelId()))
			.findAny()
			.map(ModelGraphDocumentModelElement::getSubTypes)
			.orElseThrow(() -> new InvalidInputException("No Document Model found!"));
	}

	protected void assertContains(Set<String> subTypes, boolean shouldContain, String modelName) {
		Assert.assertEquals(subTypes.contains(modelName), shouldContain,
			String.format("Model [%s] should %s included as Sub-Type!", modelName, shouldContain ? "be" : "not be"));
	}

}
