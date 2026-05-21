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
package com.mgmtp.a12.dataservices.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;

@SpringBootTest(properties = {
	"mgmtp.a12.dataservices.initialization.imports.documents.validation.enadled=false",
	"mgmtp.a12.dataservices.initialization.import.documents.import.enabled=false",
	"mgmtp.a12.dataservices.initialization.imports.models.enabled=false",
	"mgmtp.a12.dataservices.initialization.import.models.override.enabled=false",

})
public class ConfigurationPropertiesTest extends AbstractSpringContextIT {

	@Autowired DataServicesCoreProperties dataServicesCoreProperties;

	@Test
	public void testImportsKey() {
		// default value is true
		// key `mgmtp.a12.dataservices.initialization.imports.documents.validation.enadled=false` is not working.
		Assert.assertTrue(dataServicesCoreProperties.getInitialization().getImport().getDocuments().getValidation().isEnabled());

		// default value is true
		// key `mgmtp.a12.dataservices.initialization.imports.models.enabled=false` is not working.
		Assert.assertTrue(dataServicesCoreProperties.getInitialization().getImport().getModels().isEnabled());

		// default value is true
		// key `mgmtp.a12.dataservices.initialization.import.models.override.enabled=false` is working.
		Assert.assertFalse(dataServicesCoreProperties.getInitialization().getImport().getModels().getOverwrite().isEnabled());


	}
}

