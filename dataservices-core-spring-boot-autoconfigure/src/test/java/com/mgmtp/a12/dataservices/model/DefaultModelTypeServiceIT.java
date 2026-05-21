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

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SUBTYPE_MODEL1;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SUBTYPE_MODEL2;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SUBTYPE_MODEL3;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.SUPER_TYPE_MODEL;

public class DefaultModelTypeServiceIT extends AbstractModelServiceIT {

	@Autowired private DefaultModelTypeService modelTypeService;
	@Autowired private ModelGraphService modelGraphService;

	@Test public void testOnlyDirectSubTypes() {
		ModelGraphRoot modelGraphRoot = modelGraphService.constructModelGraph();
		Set<String> subTypes = getSubTypes(modelGraphRoot, SUPER_TYPE_MODEL);
		Assert.assertEquals(subTypes.size(), 2);
		assertContains(subTypes, true, SUBTYPE_MODEL1);
		assertContains(subTypes, true, SUBTYPE_MODEL2);
		assertContains(subTypes, false, SUBTYPE_MODEL3);

		subTypes = getSubTypes(modelGraphRoot, SUBTYPE_MODEL1);
		Assert.assertEquals(subTypes.size(), 1);
		assertContains(subTypes, false, SUBTYPE_MODEL1);
		assertContains(subTypes, false, SUBTYPE_MODEL2);
		assertContains(subTypes, true, SUBTYPE_MODEL3);

		subTypes = getSubTypes(modelGraphRoot, SUBTYPE_MODEL2);
		Assert.assertEquals(subTypes.size(), 0);
		assertContains(subTypes, false, SUBTYPE_MODEL1);
		assertContains(subTypes, false, SUBTYPE_MODEL2);
		assertContains(subTypes, false, SUBTYPE_MODEL3);
	}

	@Test public void testAllSubTypes() {
		Set<String> subTypes = modelTypeService.findAllSubtypes(SUPER_TYPE_MODEL);
		Assert.assertEquals(subTypes.size(), 3);
		assertContains(subTypes, true, SUBTYPE_MODEL1);
		assertContains(subTypes, true, SUBTYPE_MODEL2);
		assertContains(subTypes, true, SUBTYPE_MODEL3);

		subTypes = modelTypeService.findAllSubtypes(SUBTYPE_MODEL1);
		Assert.assertEquals(subTypes.size(), 1);
		assertContains(subTypes, false, SUBTYPE_MODEL1);
		assertContains(subTypes, false, SUBTYPE_MODEL2);
		assertContains(subTypes, true, SUBTYPE_MODEL3);

		subTypes = modelTypeService.findAllSubtypes(SUBTYPE_MODEL2);
		Assert.assertTrue(subTypes.isEmpty(), "No models should be found as sub-types!");

		subTypes = modelTypeService.findAllSubtypes("NonExisting");
		Assert.assertTrue(subTypes.isEmpty(), "No models should be found as sub-types!");
	}
}
