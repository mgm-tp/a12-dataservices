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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import lombok.SneakyThrows;

import static org.testng.Assert.assertTrue;

public class MigrationRunnerIT extends AbstractMigrationIT {

	@MockitoSpyBean DataMigrationTask2 dataMigrationTask2;
	@MockitoSpyBean DataMigrationTask dataMigrationTask;

	static final List<MigrationStepEntity> EXPECTED = List.of(
		makeMigrationStepEntity("com.mgmtp.a12.dataservices.migration.internal.DataMigrationTask2", "migrateData2", "tests", null, "description 2", "23.3.0",
			"test"),
		makeMigrationStepEntity("com.mgmtp.a12.dataservices.migration.internal.DataMigrationTask", "annotated name always", "test", null, "description",
			"23.3.0",
			"test"),
		makeMigrationStepEntity("com.mgmtp.a12.dataservices.migration.internal.DataMigrationTask", "customName", "test", null, "description", "23.3.0", "test"),
		makeMigrationStepEntity("com.mgmtp.a12.dataservices.migration.internal.DataMigrationTask", "migrateDataTx", "test", null, "description", "23.3.0",
			"test"),
		makeMigrationStepEntity("com.mgmtp.a12.dataservices.migration.internal.DataMigrationTask", "migrateData", "test", null, "description", "23.3.0", "test"),
		makeMigrationStepEntity("com.mgmtp.a12.dataservices.migration.internal.ErrorMigrationMarkRunTask", "Error migration mark run task", "Error migration mark run task", "ErrorAuthor", "error migration task", "36.0.0", "test")
	);

	@BeforeMethod void setUp() {
		migrationStepRepository.deleteAll();
	}

	@SneakyThrows
	@Test public void checkStepClassWithEmptyRepository() {

		Instant timestampBefore = Instant.now();
		migrationRunner.migrate();
		Instant timestampAfter = Instant.now();

		List<MigrationStepEntity> allMigrations = migrationStepRepository.findAllByOrderByExecutionDateDescVersionDescClassNameDescTaskDesc();
		MatcherAssert.assertThat("Task #", allMigrations, Matchers.hasSize(EXPECTED.size()));

		checkOrder();
		allMigrations.forEach(m -> assertMigration(m, timestampBefore, timestampAfter));
	}

	private void checkOrder() {
		InOrder inOrder = Mockito.inOrder(dataMigrationTask2, dataMigrationTask);

		inOrder.verify(dataMigrationTask2).migrateData2();
		inOrder.verify(dataMigrationTask).migrateDataAnnotation();
		inOrder.verify(dataMigrationTask).migrateData();
		inOrder.verify(dataMigrationTask).migrateDataParamAnnotationAlways();
		inOrder.verify(dataMigrationTask).migrateDataTx();
	}

	private void assertMigration(MigrationStepEntity actual, Instant timestampBefore, Instant timestampAfter) {
		assertTrue(actual.getExecutionDate().isAfter(timestampBefore), "The time of the migration step is before the migration was executed");
		assertTrue(actual.getExecutionDate().isBefore(timestampAfter), "The time of the migration step is after the migration was finished");
		assertTrue(EXPECTED.stream().anyMatch(expected -> migrationStepEqual(actual, expected)),
			"Migration step %s was not found among the expected steps:%n  %s".formatted(actual, EXPECTED.stream()
				.map(MigrationStepEntity::toString)
				.collect(Collectors.joining("%n  "))));
	}

	private boolean migrationStepEqual(MigrationStepEntity actual, MigrationStepEntity expected) {
		Stream<BiFunction<MigrationStepEntity, MigrationStepEntity, Boolean>> equals = Stream.<Function<MigrationStepEntity, String>>of(
				MigrationStepEntity::getAuthor,
				MigrationStepEntity::getName,
				MigrationStepEntity::getTask,
				MigrationStepEntity::getDescription,
				MigrationStepEntity::getVersion,
				MigrationStepEntity::getExecutedVersion)
			.map(f -> (a, e) -> Objects.equals(f.apply(a), f.apply(e)));
		Stream<BiFunction<MigrationStepEntity, MigrationStepEntity, Boolean>> startsWith =
			Stream.<Function<MigrationStepEntity, String>>of(MigrationStepEntity::getClassName)
				.map(f -> (a, e) -> f.apply(a).startsWith(f.apply(e)));

		return Stream.concat(equals, startsWith)
			.allMatch(f -> f.apply(actual, expected));
	}

	private static MigrationStepEntity makeMigrationStepEntity(String className, String task, String name, String author, String description, String version,
		String executedVersion) {

		MigrationStepEntity e = new MigrationStepEntity();
		e.setClassName(className);
		e.setTask(task);
		e.setName(name);
		e.setAuthor(author);
		e.setDescription(description);
		e.setVersion(version);
		e.setExecutedVersion(executedVersion);
		return e;
	}
}
