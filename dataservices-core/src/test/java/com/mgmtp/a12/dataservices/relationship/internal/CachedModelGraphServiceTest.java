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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.lang.reflect.Method;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CachedModelGraphServiceTest {

	@Test(enabled = true, description = "Should extend ModelGraphService")
	public void shouldExtendModelGraphService() {
		Assert.assertTrue(ModelGraphService.class.isAssignableFrom(CachedModelGraphService.class),
			"CachedModelGraphService must extend ModelGraphService");
	}

	@Test(enabled = true, description = "Should have @Service annotation")
	public void shouldHaveServiceAnnotation() {
		Assert.assertNotNull(CachedModelGraphService.class.getAnnotation(Service.class),
			"CachedModelGraphService must be annotated with @Service");
	}

	@Test(enabled = true, description = "Should have @Cacheable annotation on constructModelGraph(String username) parameter")
	public void shouldHaveCacheableAnnotationOnConstructModelGraphWithUsernameParameter() throws NoSuchMethodException {
		Method method = CachedModelGraphService.class.getMethod("constructModelGraph", String.class);
		Cacheable cacheable = method.getAnnotation(Cacheable.class);
		Assert.assertNotNull(cacheable, "constructModelGraph(String) must be annotated with @Cacheable");
	}

	@Test(enabled = true, description = "Should have @Transactional annotation with dsTransactionManager on constructModelGraph")
	public void shouldHaveTransactionalAnnotationWithDsTransactionManagerOnConstructModelGraph() throws NoSuchMethodException {
		Method method = CachedModelGraphService.class.getMethod("constructModelGraph");
		Transactional transactional = method.getAnnotation(Transactional.class);
		Assert.assertNotNull(transactional, "constructModelGraph() must be annotated with @Transactional");
	}
}
