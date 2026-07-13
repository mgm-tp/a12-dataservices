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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

public class NoOpModelPermissionEvaluatorTest {

	private NoOpModelPermissionEvaluator evaluator;

	@BeforeMethod
	public void setUp() {
		evaluator = new NoOpModelPermissionEvaluator();
	}

	@Test(description ="Should not throw when create permission is checked")
	public void shouldNotThrowWhenCreatePermissionIsChecked() {
		// Given
		Header header = mock(Header.class);

		// When / Then
		assertThatCode(() -> evaluator.checkModelCreatePermission(header))
			.doesNotThrowAnyException();
	}

	@Test(description ="Should not throw when update permission is checked")
	public void shouldNotThrowWhenUpdatePermissionIsChecked() {
		// Given
		Header header = mock(Header.class);

		// When / Then
		assertThatCode(() -> evaluator.checkModelUpdatePermission(header))
			.doesNotThrowAnyException();
	}

	@Test(description ="Should not throw when delete permission is checked")
	public void shouldNotThrowWhenDeletePermissionIsChecked() {
		// Given
		Header header = mock(Header.class);

		// When / Then
		assertThatCode(() -> evaluator.checkModelDeletePermission(header))
			.doesNotThrowAnyException();
	}

	@Test(description ="Should grant read permission when checking by model identifier")
	public void shouldGrantReadPermissionWhenCheckingByModelIdentifier() {
		// When
		boolean result = evaluator.hasModelReadPermission("someModelId");

		// Then
		assertThat(result).isTrue();
	}

	@Test(description ="Should grant read permission when checking by model header")
	public void shouldGrantReadPermissionWhenCheckingByModelHeader() {
		// Given
		Header header = mock(Header.class);

		// When
		boolean result = evaluator.hasModelReadPermission(header);

		// Then
		assertThat(result).isTrue();
	}

	@Test(description ="Should grant read permission when checking by model instance")
	public void shouldGrantReadPermissionWhenCheckingByModelInstance() {
		// Given
		Model model = mock(Model.class);

		// When
		boolean result = evaluator.hasModelReadPermission(model);

		// Then
		assertThat(result).isTrue();
	}
}
