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
package com.mgmtp.a12.examples.migration;

import com.mgmtp.a12.dataservices.migration.MigrationTask;

import lombok.extern.slf4j.Slf4j;

/**
 * Example of migration step class. The class annotated with custom annotation  {@link ExtendedServerMigrationStep}
 *
 */
@ExtendedServerMigrationStep(version="34.0.0", name="MigrationStepThree", description="This is an example of a migration step")
@Slf4j
public class MigrationStepThree {
	/**
	 * Performs a recurring validation task for version 34.0.0.
	 * Marked with +runAlways = true+, so it executes on every application start after ordered tasks.
	 * Side effects: writes an informational log entry.
	 */
	@MigrationTask(name = "migrationThree", runAlways = true)
	public void migrationThree() {
		log.info("Running the migration check of the version 34.0.0. It will run every time the init app is started after all ordered tasks, since it has the runAlways flag as true");
	}
}

