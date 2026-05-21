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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.migration.ErrorHandling;
import com.mgmtp.a12.dataservices.migration.MigrationStep;
import com.mgmtp.a12.dataservices.migration.MigrationTask;
import com.mgmtp.a12.dataservices.migration.exception.MigrationException;
import com.mgmtp.a12.dataservices.state.VersionInfo;
import com.vdurmont.semver4j.Semver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class MigrationRunner {

	public static final String VERSION_ATTRIBUTE_NAME = "version";
	private static final String EXECUTED_CLASS_ATTRIBUTE_NAME = "executedClassName";
	private static final String RUN_ALWAYS_ATTRIBUTE_NAME = "runAlways";

	private final VersionInfo versionInfo;
	private final ApplicationContext applicationContext;
	private final MigrationStepRepository migrationStepRepository;
	private final ObjectMapper objectMapper;
	private final TransactionHandler transactionHandler;
	private Map<MigrationStepId, MigrationStepEntity> executedMigrations = new HashMap<>();

	@Transactional
	public void migrate() {
		preloadExecutedMigrations();
		resolveMigrationStepsWithVersions().entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.flatMap(List::stream)
			.forEach(migrationStep -> transactionHandler.runMethodInNewTransaction(() -> processMigrationStep(migrationStep)));
	}

	private Map<Semver, List<Object>> resolveMigrationStepsWithVersions() {
		return resolveUniqueMigrationSteps().values().stream()
			.sorted(new AnnotationAwareOrderComparator())
			.collect(Collectors.groupingBy(MigrationRunner::getSemverVersion, Collectors.toList()));
	}

	private Map<Class<?>, Object> resolveUniqueMigrationSteps() {
		Map<String, Object> migrationStepBeans = applicationContext.getBeansWithAnnotation(MigrationStep.class);
		Map<Class<?>, List<Map.Entry<String, Object>>> groupedBeans = migrationStepBeans.entrySet().stream()
			.collect(Collectors.groupingBy(entry -> entry.getValue().getClass()));

		return extractUniqueBeans(groupedBeans);
	}

	private Map<Class<?>, Object> extractUniqueBeans(Map<Class<?>, List<Map.Entry<String, Object>>> groupedBeans) {
		Map<Class<?>, Object> uniqueBeans = new HashMap<>();
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

		for (Map.Entry<Class<?>, List<Map.Entry<String, Object>>> entry : groupedBeans.entrySet()) {
			Class<?> beanClass = entry.getKey();
			List<Map.Entry<String, Object>> beanInstances = entry.getValue();

			uniqueBeans.put(beanClass, resolveUniqueBean(beanInstances, beanClass, beanFactory));
		}

		return uniqueBeans;
	}

	private Object resolveUniqueBean(List<Map.Entry<String, Object>> beanInstances, Class<?> beanClass, DefaultListableBeanFactory beanFactory) {
		if (beanInstances.size() == 1) {
			return beanInstances.get(0).getValue(); // Only one bean available
		}

		// Look for the @Primary bean in multiple instances
		Optional<Object> primaryBean = beanInstances.stream()
			.filter(beanEntry -> isPrimaryBean(beanEntry.getKey(), beanFactory))
			.map(Map.Entry::getValue)
			.findFirst();

		return primaryBean.orElseThrow(() ->
			new InvalidInputException("Multiple implementations of migration step for class '"
				+ beanClass.getName() + "' are present, but none are annotated with @Primary."));
	}

	private static boolean isPrimaryBean(String beanName, DefaultListableBeanFactory beanFactory) {
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
		return beanDefinition.isPrimary();
	}

	@NonNull private static Semver getSemverVersion(Object migrationStepBean) {
		String version = MergedAnnotations.from(migrationStepBean.getClass(), SearchStrategy.SUPERCLASS)
			.get(MigrationStep.class)
			.getString(VERSION_ATTRIBUTE_NAME);

		try {
			return new Semver(version);
		} catch (Exception e) {
			log.warn(String.format("Semantic version defined in the migration step [%s] has wrong format.", migrationStepBean.getClass().getName()));
			throw e;
		}
	}

	private void processMigrationStep(Object migrationStepBean) {

		MergedAnnotation<MigrationStep> migrationStepAnnotation = MergedAnnotations.from(migrationStepBean.getClass(), SearchStrategy.SUPERCLASS)
			.get(MigrationStep.class);

		if (migrationStepAnnotation.isPresent()) {
			Class<?> declaringClass = (Class<?>) migrationStepAnnotation.getSource();
			Arrays.stream(Objects.requireNonNull(declaringClass).getDeclaredMethods())
				.filter(this::migrationTaskFilter)
				.sorted(new AnnotationAwareOrderComparator())
				.filter(method -> shouldExecute(declaringClass, migrationStepAnnotation, method))
				.forEach(method -> executeTask(method, declaringClass, migrationStepBean));
		}
	}

	private boolean migrationTaskFilter(Method stepMethod) {
		if (AnnotationUtils.findAnnotation(stepMethod, MigrationTask.class) == null) {
			return false;
		} else if (Modifier.isPrivate(stepMethod.getModifiers())) {
			log.error("Annotated TASK method [{}] on class [{}] can't be executed since it's private!", stepMethod.getName(),
				stepMethod.getDeclaringClass().getCanonicalName());
			return false;
		} else if (stepMethod.getParameterCount() > 1) {
			log.error("Annotated TASK method [{}] on class [{}] can't be executed since it has more method parameters than one allowed!", stepMethod.getName(),
				stepMethod.getDeclaringClass().getCanonicalName());
			return false;
		} else if (stepMethod.getParameterCount() == 1 && !stepMethod.getParameterTypes()[0].getTypeName().equals(String.class.getName())) {
			log.error("Annotated TASK method [{}] on class [{}] can't be executed since it has method parameter not of allowed String type",
				stepMethod.getName(),
				stepMethod.getDeclaringClass().getCanonicalName());
			return false;
		}

		return true;
	}

	private boolean shouldExecute(Class<?> stepClass, MergedAnnotation<MigrationStep> migrationStepAnnotation, Method taskMethod) {
		MigrationStepEntity alreadyExecuted = getAlreadyExecuted(stepClass, migrationStepAnnotation.getString(EXECUTED_CLASS_ATTRIBUTE_NAME), taskMethod);
		boolean runStepAlways = migrationStepAnnotation.getBoolean(RUN_ALWAYS_ATTRIBUTE_NAME);
		boolean runTaskAlways = Optional.ofNullable(AnnotationUtils.findAnnotation(taskMethod, MigrationTask.class))
			.map(MigrationTask::runAlways)
			.orElse(false);

		if (runTaskAlways || runStepAlways || alreadyExecuted == null) {
			return true;
		} else {
			log.info(String.format("TASK method [%s] on class [%s] was already executed at [%tc].", taskMethod.getName(),
				taskMethod.getDeclaringClass().getCanonicalName(), alreadyExecuted.getExecutionDate().atOffset(ZoneOffset.UTC)));
			return false;
		}
	}

	private void preloadExecutedMigrations() {
		executedMigrations = migrationStepRepository.findAllByOrderByExecutionDateDescVersionDescClassNameDescTaskDesc().stream()
			.collect(Collectors.toMap(entity -> new MigrationStepId(entity.getClassName(), entity.getTask()), entity -> entity, (o1, o2) -> o1));
	}

	private String getTaskName(Method taskMethod) {
		return Optional.ofNullable(AnnotationUtils.findAnnotation(taskMethod, MigrationTask.class))
			.map(MigrationTask::name)
			.filter(n -> !MigrationTask.UNASSIGNED.equals(n))
			.orElse(taskMethod.getName());
	}

	private void executeTask(Method method, Class<?> stepClass, Object stepInstance) {
		MigrationStep migrationStep = Objects.requireNonNull(AnnotatedElementUtils.findMergedAnnotation(stepClass, MigrationStep.class));
		MigrationTask migrationTask = AnnotationUtils.findAnnotation(method, MigrationTask.class);
		ErrorHandling errorHandling;

		if (stepInstance instanceof AbstractDataServicesMigrationStep abstractMigrationStep && abstractMigrationStep.getErrorHandling() != null) {
			errorHandling = abstractMigrationStep.getErrorHandling();
		} else {
			errorHandling = migrationTask == null ? migrationStep.onFailure() : migrationTask.onFailure();
		}

		try {
			// we are executing method only if migration task annotation is present
			if (migrationTask != null) {
				Object object = executeMigrationTask(method, stepInstance, migrationTask.name());
				persistMigrationStep(method, stepClass, migrationStep, object);
			}
		} catch (Exception e) {
			log.error(String.format("TASK method [%s] on class [%s] can't be executed!",
				method.getName(), method.getDeclaringClass().getCanonicalName()), e);

			if (errorHandling == ErrorHandling.HALT) {
				throw new MigrationException(
					String.format("Migration task [%s#%s] execution failed and error handling is set to HALT. Stopping the migration.",
						stepClass.getSimpleName(),
						method.getName()));
			}

			if (errorHandling == ErrorHandling.MARK_RUN) {
				persistMigrationStep(method, stepClass, migrationStep, null);
			}

			// if error handling is equal to CONTINUE, we silently ignore the error
		}
	}

	private Object executeMigrationTask(Method method, Object stepInstance, String migrationTaskName) throws InvocationTargetException, IllegalAccessException {
		StopWatch watch = new StopWatch();
		watch.start();
		method.setAccessible(true); //for protected methods
		Object executionResult = null;

		if (method.getParameterCount() == 0) {
			executionResult = method.invoke(stepInstance);
		} else if (method.getParameterCount() == 1) {
			String metadata = migrationStepRepository.findLastExecutedMigrationStep(stepInstance.getClass().getName(), migrationTaskName);
			executionResult = method.invoke(stepInstance, metadata);
		}

		watch.stop();
		log.info(String.format("TASK method [%s] on class [%s] has been executed in [%sms]..", method.getName(), method.getDeclaringClass().getCanonicalName(),
			watch.getTotalTimeMillis()));
		return executionResult;
	}

	private void persistMigrationStep(Method method, Class<?> stepClass, MigrationStep migrationStep, Object metadata) {
		MigrationStepEntity step = new MigrationStepEntity();
		step.setClassName(StringUtils.isBlank(migrationStep.executedClassName()) ? stepClass.getCanonicalName() : migrationStep.executedClassName());
		step.setTask(getTaskName(method));
		step.setName(StringUtils.trimToNull(migrationStep.name()));
		step.setDescription(StringUtils.trimToNull(migrationStep.description()));
		step.setVersion(migrationStep.version());
		step.setAuthor(StringUtils.trimToNull(migrationStep.author()));
		step.setExecutedVersion(versionInfo.getA12ServicesVersion());

		try {
			step.setMetadata(metadata == null ? null : objectMapper.writeValueAsString(metadata));
		} catch (JsonProcessingException e) {
			log.error("Unable to serialize metadata to json for migration step name: {}, version {} and method: {}", migrationStep.name(),
				migrationStep.version(), method.getName());
		}

		migrationStepRepository.save(step);
		putAlreadyExecuted(step);
	}

	private void putAlreadyExecuted(MigrationStepEntity step) {
		executedMigrations.put(new MigrationStepId(step.getClassName(), step.getTask()), step);
	}

	private MigrationStepEntity getAlreadyExecuted(Class<?> stepClass, String executedClassName, Method taskMethod) {
		return executedMigrations.get(new MigrationStepId(StringUtils.isBlank(executedClassName) ? stepClass.getCanonicalName() : executedClassName,
			getTaskName(taskMethod)));
	}
}
