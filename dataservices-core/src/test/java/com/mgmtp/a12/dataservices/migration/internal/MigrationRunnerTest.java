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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.migration.ErrorHandling;
import com.mgmtp.a12.dataservices.migration.MigrationStep;
import com.mgmtp.a12.dataservices.state.VersionInfo;
import com.mgmtp.a12.uaa.authorization.UAASecurityBypass;

import lombok.NonNull;
import lombok.SneakyThrows;

import static org.mockito.Mockito.mock;

public class MigrationRunnerTest {

	@Mock private PlatformTransactionManager dsTransactionManager;
	@Mock private UAASecurityBypass securityBypass;

	//unable to mock it since we relay public methods and mockito will add some unnecessary ones
	private final DataMigrationTask migrationTask = new DataMigrationTask();
	private final DataMigrationTask2 migrationTask2 = new DataMigrationTask2();
	private final ErrorContinueMigrationTask errorContinueMigrationTask = new ErrorContinueMigrationTask();
	private AutoCloseable mocks;

	@SneakyThrows
	@BeforeMethod public void init() {
		mocks = MockitoAnnotations.openMocks(this);

		prepareDefaultBeans();
	}

	@SneakyThrows
	@AfterMethod public void tearDown() {
		mocks.close();
	}

	@Test public void checkStepClassWithEmptyRepository() {

		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of());
		ArgumentCaptor<MigrationStepEntity> captor = ArgumentCaptor.forClass(MigrationStepEntity.class);
		prepareMigrationRunner(migrationStepRepository, mockAppContext(prepareDefaultBeans())).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(5)).save(captor.capture());
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateDataTx"))));
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData"))));
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("customName"))));
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("annotated name always"))));
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData2"))));

		MigrationStepEntity migrationStepEntity = captor.getAllValues().get(4);
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getTask(), Matchers.equalTo("migrateData2"));
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getVersion(), Matchers.equalTo("23.3.0"));
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getAuthor(), Matchers.equalTo("SuperAuthor"));
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getDescription(), Matchers.equalTo("description 2"));
		MatcherAssert.assertThat("Task Name", migrationStepEntity.getExecutedVersion(), Matchers.equalTo("23.3.0"));
	}

	@Test public void checkStepClassWithFullRepository() {
		List<MigrationStepEntity> entities = new ArrayList<>();
		entities.add(prepareEntity("migrateDataTx"));
		entities.add(prepareEntity("customName"));
		entities.add(prepareEntity("annotated name always"));
		entities.add(prepareEntity("migrateData"));
		entities.add(prepareEntity("migrateData2", migrationTask2.getClass()));
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(entities);
		ArgumentCaptor<MigrationStepEntity> captor = ArgumentCaptor.forClass(MigrationStepEntity.class);
		prepareMigrationRunner(migrationStepRepository, mockAppContext(prepareDefaultBeans())).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(1)).save(captor.capture());
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("annotated name always"))));
	}

	@Test public void checkStepClassWithNotEmptyRepository() {
		List<MigrationStepEntity> entities = new ArrayList<>();
		entities.add(prepareEntity("migrateDataTx"));
		entities.add(prepareEntity("customName"));
		entities.add(prepareEntity("annotated name always"));

		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(entities);
		ArgumentCaptor<MigrationStepEntity> captor = ArgumentCaptor.forClass(MigrationStepEntity.class);
		prepareMigrationRunner(migrationStepRepository, mockAppContext(prepareDefaultBeans())).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(3)).save(captor.capture());
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("annotated name always"))));
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData"))));
		MatcherAssert.assertThat("Task name", captor.getAllValues(),
			Matchers.hasItem(Matchers.<MigrationStepEntity>hasProperty("task", Matchers.equalTo("migrateData2"))));
	}

	/**
	 * == Purpose
	 * Verify that {@link MigrationRunner} skips already executed migration tasks.
	 *
	 * == Scenario
	 * 1. First run: no migrations are recorded → task executes and is saved.
	 * 2. Second run: repository is preloaded with the executed task → task is skipped.
	 *
	 * == Expected
	 * The migration step is persisted only once across both runs.
	 */
	@Test public void shouldSkipAllExecutedTasks() {
		// given: empty repo and one migration step
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of());
		HashMap<String, Object> appContextBeans = new HashMap<>();
		appContextBeans.put("tes3", new DataMigrationTask3());
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		Mockito.when(applicationContext.getBeansWithAnnotation(MigrationStep.class)).thenReturn(appContextBeans);

		MigrationRunner migrationRunner = prepareMigrationRunner(migrationStepRepository, applicationContext);

		// when: first migration run — executes and saves once
		migrationRunner.migrate();

		// capture the saved migration entity
		ArgumentCaptor<MigrationStepEntity> captor = ArgumentCaptor.forClass(MigrationStepEntity.class);
		Mockito.verify(migrationStepRepository, Mockito.times(1)).save(captor.capture());
		MigrationStepEntity executedEntity = captor.getValue();

		// simulate already executed migration for next run
		Mockito.when(migrationStepRepository.findAllByOrderByExecutionDateDescVersionDescClassNameDescTaskDesc())
			.thenReturn(List.of(executedEntity));

		// when: second migration run
		migrationRunner.migrate();

		// then: no new tasks are persisted
		Mockito.verify(migrationStepRepository, Mockito.times(1)).save(Mockito.any());
	}


	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Migration task \\[ErrorHaltMigrationTask#migrateData] execution failed and error handling is set to HALT\\. Stopping the migration\\.")
	public void checkHaltOption() {
		Map<String, Object> appContextBeans = prepareDefaultBeans();
		appContextBeans.put("errorHaltMigrationTask", new ErrorHaltMigrationTask());
		prepareMigrationRunner(mockMigrationStepRepo(List.of()), mockAppContext(appContextBeans)).migrate();
	}

	@Test(expectedExceptions = RuntimeException.class) public void testConfigurableErrorHandlingMigrationStep() {
		prepareMigrationRunner(
			mockMigrationStepRepo(List.of()),
			mockAppContext(Map.of("configurableErrorHandlingMigrationTask", new ConfigurableErrorHandlingMigrationTask(ErrorHandling.HALT)))
		).migrate();
	}

	@Test public void checkContinueErrorHandlingPersistence() {
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of());
		prepareMigrationRunner(migrationStepRepository, mockAppContext(Map.of("errorContinueMigrationTask", errorContinueMigrationTask))).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(0)).save(Mockito.any());
	}

	@Test public void checkMarkRanOptionPersistence() {
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of());
		prepareMigrationRunner(migrationStepRepository, mockAppContext(Map.of("errorMarkRanMigrationTask",
			new ErrorMarkRanMigrationTask()))).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(1)).save(Mockito.any());
	}

	@Test public void checkMetadata() {
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of(createTestMetadataEntity()));
		Mockito.when(migrationStepRepository.findLastExecutedMigrationStep("com.mgmtp.a12.dataservices.migration.internal.MetadataMigrationTask", "test"))
			.thenReturn("true");
		prepareMigrationRunner(migrationStepRepository, mockAppContext(Map.of("metadataMigrationTask", new MetadataMigrationTask()))).migrate();
	}

	@Test
	public void checkNonStringMethodParameter() {
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of());
		prepareMigrationRunner(migrationStepRepository, mockAppContext(Map.of("errorMethodParameterMigrationTask",
			new ErrorMethodParameterMigrationTask()))).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(0))
			.findLastExecutedMigrationStep(Mockito.any(), Mockito.any());
		Mockito.verify(migrationStepRepository, Mockito.times(0)).save(Mockito.any());
	}

	@Test
	public void checkTooManyMethodParameter() {
		MigrationStepRepository migrationStepRepository = mockMigrationStepRepo(List.of());
		prepareMigrationRunner(migrationStepRepository, mockAppContext(Map.of("errorTooManyMethodParametersMigrationTask",
			new ErrorTooManyMethodParametersMigrationTask()))).migrate();
		Mockito.verify(migrationStepRepository, Mockito.times(0))
			.findLastExecutedMigrationStep(Mockito.any(), Mockito.any());
		Mockito.verify(migrationStepRepository, Mockito.times(0)).save(Mockito.any());
	}

	private MigrationStepEntity prepareEntity(String task) {
		return prepareEntity(task, migrationTask.getClass());
	}

	private MigrationStepEntity prepareEntity(String task, Class<?> clazz) {
		MigrationStepEntity entity = new MigrationStepEntity();
		entity.setAuthor("tkupka");
		entity.setClassName(clazz.getCanonicalName());
		entity.setTask(task);
		return entity;
	}

	private Map<String, Object> prepareDefaultBeans() {
		HashMap<String, Object> appContextBeans = new HashMap<>();
		appContextBeans.put("test", migrationTask);
		appContextBeans.put("test2", migrationTask2);
		appContextBeans.put("errorContinueMigrationTask", errorContinueMigrationTask);
		return appContextBeans;
	}

	@SneakyThrows
	private @NonNull MigrationRunner prepareMigrationRunner(MigrationStepRepository migrationStepRepository, ApplicationContext applicationContext) {

		VersionInfo versionInfo = mock(VersionInfo.class);
		Mockito.when(versionInfo.getA12ServicesVersion()).thenReturn("23.3.0");

		ObjectMapper objectMapper = mock(ObjectMapper.class);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("");

		return new MigrationRunner(versionInfo, applicationContext, migrationStepRepository, objectMapper, new TransactionHandler());
	}

	private @NonNull ApplicationContext mockAppContext(Map<String, Object> appContextBeans1) {
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		Mockito.when(applicationContext.getBeansWithAnnotation(MigrationStep.class)).thenReturn(appContextBeans1);
		return applicationContext;
	}

	private static @NonNull MigrationStepRepository mockMigrationStepRepo(List<MigrationStepEntity> entities) {
		MigrationStepRepository migrationStepRepository = mock(MigrationStepRepository.class);
		Mockito.when(migrationStepRepository.findAll()).thenReturn(entities);
		Mockito.when(migrationStepRepository.findAllByOrderByExecutionDateDescVersionDescClassNameDescTaskDesc()).thenReturn(entities);
		return migrationStepRepository;
	}

	private MigrationStepEntity createTestMetadataEntity() {
		MigrationStepEntity migrationStep = new MigrationStepEntity();
		migrationStep.setClassName("com.mgmtp.a12.dataservices.migration.internal.MetadataMigrationTask");
		migrationStep.setTask("test");
		migrationStep.setExecutionDate(Instant.now());
		migrationStep.setName("test metadata");
		migrationStep.setAuthor("SuperAuthor");
		migrationStep.setDescription("test metadata");
		migrationStep.setVersion("36.2.0");
		migrationStep.setExecutedVersion("36.2.0");
		migrationStep.setMetadata("true");

		return migrationStep;
	}
}
