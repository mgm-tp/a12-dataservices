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
package com.mgmtp.a12.dataservices.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.mgmtp.a12.dataservices.internal.DataSourceContextHolder.DataSourceType;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class DataSourceContextHolderTest {

	@AfterMethod
	public void cleanUp() {
		DataSourceContextHolder.forceClear();
	}

	@Test
	public void shouldDefaultToPrimaryWhenNoContextSet() {
		assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.PRIMARY);
	}

	@Test
	public void shouldReturnSetDatasourceType() {
		DataSourceContextHolder.setDataSourceType(DataSourceType.REPLICA);

		assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.REPLICA);
	}

	@Test
	public void shouldReturnFalseForIsSetWhenNoContextSet() {
		assertThat(DataSourceContextHolder.isDataSourceTypeSet()).isFalse();
	}

	@Test
	public void shouldReturnTrueForIsSetWhenContextIsSet() {
		DataSourceContextHolder.setDataSourceType(DataSourceType.REPLICA);

		assertThat(DataSourceContextHolder.isDataSourceTypeSet()).isTrue();
	}

	@Test
	public void shouldDefaultToPrimaryAfterClear() {
		DataSourceContextHolder.setDataSourceType(DataSourceType.REPLICA);

		DataSourceContextHolder.clearDataSourceType();

		assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.PRIMARY);
		assertThat(DataSourceContextHolder.isDataSourceTypeSet()).isFalse();
	}

	@Test
	public void shouldDefaultToPrimaryAfterForceClear() {
		DataSourceContextHolder.setDataSourceType(DataSourceType.REPLICA);

		DataSourceContextHolder.forceClear();

		assertThat(DataSourceContextHolder.isDataSourceTypeSet()).isFalse();
	}
}
