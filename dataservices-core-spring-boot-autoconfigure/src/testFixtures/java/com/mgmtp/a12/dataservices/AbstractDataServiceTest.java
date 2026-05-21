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
package com.mgmtp.a12.dataservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

public abstract class AbstractDataServiceTest extends AbstractKernelAwareTest {

	public AutoCloseable mocks;

	@SneakyThrows
	@BeforeClass
	public void init() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@SneakyThrows
	@AfterClass
	public void cleanup() {
		mocks.close();
	}

	public Header makeTestModelHeader() {
		return makeTestModelHeader(RandomStringUtils.randomAlphabetic(10));
	}

	public Header makeTestModelHeader(String modelType) {
		return makeTestModelHeader(modelType,
			new ArrayList<>(List.of(new TestAnnotation("roles", "admin")))
		);
	}

	public Header makeTestModelHeader(String modelType, List<Annotation> annotations) {
		TestHeader header = new TestHeader();
		header.setId(RandomStringUtils.randomAlphabetic(6));
		header.setModelType(modelType != null ? modelType : RandomStringUtils.randomAlphabetic(10));
		header.setAnnotations(annotations);
		header.setLabels(Collections.emptyList());
		return header;
	}

	@Data @AllArgsConstructor
	public static class TestAnnotation implements Annotation {
		private String name;
		private String value;
	}

}
