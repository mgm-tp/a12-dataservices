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
package com.mgmtp.a12.dataservices.server;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.mgmtp.a12.dataservices.TestHeader;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

public abstract class AbstractDataServiceServerTest {

	public AutoCloseable mocks;

	@BeforeClass
	public void init() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@SneakyThrows
	@AfterClass
	public void tearDown() {
		mocks.close();
	}

	@Data
	@AllArgsConstructor
	public class AnnotationTest implements Annotation {
		private String name;
		private String value;
	}

	public Header mockModelHeader() {
		return mockModelHeader(null);
	}

	public Header mockModelHeader(String modelType) {
		TestHeader header = new TestHeader();
		header.setId(RandomStringUtils.randomAlphabetic(6));
		header.setModelType(modelType != null ? modelType : RandomStringUtils.randomAlphabetic(10));
		header.setAnnotations(new ArrayList<>(Collections.singletonList(new AnnotationTest("roles", "admin"))));
		return header;
	}

	public void mockAuthorizationContext(String username) {
		UaaTestHelper.setCurrentUserName(User.builder().username(username).password("").build());
	}

}
