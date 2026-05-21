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
package com.mgmtp.a12.dataservices.migration.internal;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MigrationRunnerITWithPartialTaskDB extends AbstractMigrationIT {

	@Autowired private MigrationStepRepository migrationStepRepository;
	@Autowired private MigrationRunner migrationRunner;

	@BeforeClass
	void setUp() {
		migrationStepRepository.save(prepareEntity("migrateDataTx"));
		migrationStepRepository.save(prepareEntity("customName"));
		migrationStepRepository.save(prepareEntity("annotated name always"));
	}

	@Test
	public void checkStepClassWithNotEmptyRepository() {
		migrationRunner.migrate();
		List<MigrationStepEntity> allMigrations = migrationStepRepository.findAll();

		MatcherAssert.assertThat("Persisted steps count", allMigrations, Matchers.hasSize(9));

		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateDataTx"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("customName"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("annotated name always"))));
	}

	private MigrationStepEntity prepareEntity(String task) {
		MigrationStepEntity entity = new MigrationStepEntity();
		entity.setAuthor("tkupka");
		entity.setName("test");
		entity.setClassName(DataMigrationTask.class.getCanonicalName());
		entity.setTask(task);
		return entity;

	}

}
