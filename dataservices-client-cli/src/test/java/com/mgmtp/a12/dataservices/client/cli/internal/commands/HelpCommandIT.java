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
package com.mgmtp.a12.dataservices.client.cli.internal.commands;

import java.io.IOException;

import org.springframework.boot.DefaultApplicationArguments;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.client.cli.internal.AbstractCliIT;

import static org.assertj.core.api.Assertions.assertThat;

public class HelpCommandIT extends AbstractCliIT {

	@DataProvider(name = "application-arguments")
	public Object[][] helpVariations() {
		return new Object[][] {
				{ "help" },
				{ "-h" }
		};
	}

	@Test(dataProvider = "application-arguments")
	public void testHelpCommands(String... args) throws IOException {
		client.run(new DefaultApplicationArguments(args));
		assertThat(getCleanLineEndings(stdout.toString())).isBlank();
		assertThat(getCleanLineEndings(stderr.toString())).isEqualTo(HELP_OUTPUT);
		assertThat(client.getExitCode()).isEqualTo(2);
	}
}
