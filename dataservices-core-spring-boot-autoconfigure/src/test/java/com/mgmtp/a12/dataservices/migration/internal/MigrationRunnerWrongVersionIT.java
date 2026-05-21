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
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.state.VersionInfo;
import com.vdurmont.semver4j.SemverException;

@Test
@ActiveProfiles({ "dataservices-test-bad_version", "dataservices-uaa", "dataservices-embedded_postgres", "dataservices-rpc",
	"dataservices-cdd_sync", "dataservices-core-test-qa" })
public class MigrationRunnerWrongVersionIT extends AbstractMigrationIT {

	@Autowired private VersionInfo versionInfo;

	@BeforeMethod
	void setUp() {
		migrationStepRepository.deleteAll();
	}

	@Test(expectedExceptions = SemverException.class, expectedExceptionsMessageRegExp = "Invalid version \\(no major version\\): BadVersion")
	public void checkStepClassWithEmptyRepository() {

		migrationRunner.migrate();

		List<MigrationStepEntity> allMigrations = migrationStepRepository.findAllByOrderByExecutionDateDescVersionDescClassNameDescTaskDesc();
		MatcherAssert.assertThat("Task #", allMigrations, Matchers.hasSize(9));

		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateDataTx"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("customName"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("annotated name always"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData2"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData3"))));
		MatcherAssert.assertThat("Task name", allMigrations,
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("link documents migration"))));

		//method ordering is not guaranteed so we can check only single method steps

		MigrationStepEntity migrationStepEntity = allMigrations.get(0);
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getTask(), Matchers.equalTo("migrateData3"));
		MatcherAssert.assertThat("Version", migrationStepEntity.getVersion(), Matchers.equalTo("BadVersion"));
		MatcherAssert.assertThat("Author", migrationStepEntity.getAuthor(), Matchers.equalTo("SuperAuthor"));
		MatcherAssert.assertThat("Description", migrationStepEntity.getDescription(), Matchers.equalTo("description 3"));
		MatcherAssert.assertThat("Executed version", migrationStepEntity.getExecutedVersion(), Matchers.equalTo(versionInfo.getA12ServicesVersion()));

		migrationStepEntity = allMigrations.get(6);
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getTask(), Matchers.equalTo("migrateData2"));
		MatcherAssert.assertThat("Version", migrationStepEntity.getVersion(), Matchers.equalTo("23.3.0"));
		MatcherAssert.assertThat("Author", migrationStepEntity.getAuthor(), Matchers.nullValue());
		MatcherAssert.assertThat("Description", migrationStepEntity.getDescription(), Matchers.equalTo("description 2"));
		MatcherAssert.assertThat("Executed version", migrationStepEntity.getExecutedVersion(), Matchers.equalTo(versionInfo.getA12ServicesVersion()));
	}

}
