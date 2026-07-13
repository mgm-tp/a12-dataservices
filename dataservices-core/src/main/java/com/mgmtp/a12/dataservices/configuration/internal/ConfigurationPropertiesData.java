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
package com.mgmtp.a12.dataservices.configuration.internal;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;

import com.mgmtp.a12.dataservices.configuration.ExposePropertiesToActuator;
import com.mgmtp.a12.dataservices.configuration.validation.internal.WrongConfigurationMessage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationPropertiesData {

	public static final String CHANGES = "changes";
	public static final String WARNINGS = "warnings";
	private final Map<String, Object> configurationData;

	public ConfigurationPropertiesData(ApplicationContext applicationContext) {
		Map<String, Object> changedConfiguration = findChangedConfiguration(applicationContext);
		configurationData = Map.of(CHANGES, changedConfiguration,
			WARNINGS, WrongConfigurationMessage.EXISTING_WARNINGS);
		log.info("configurationData: {}", configurationData);
	}

	public Map<String, Object> getAll() {
		return configurationData;
	}

	public Object getByKey(String name) {
		return configurationData.get(name);
	}

	private Map<String, Object> findChangedConfiguration(ApplicationContext applicationContext) {
		Map<String, Object> changedConfiguration = new HashMap<>();
		Map<String, Object> configurationBeans = applicationContext.getBeansWithAnnotation(ExposePropertiesToActuator.class);

		for (Map.Entry<String, Object> entry : configurationBeans.entrySet()) {
			Class<?> targetClass = AopUtils.getTargetClass(entry.getValue());
			ConfigurationProperties configurationProperties = applicationContext.findAnnotationOnBean(entry.getKey(), ConfigurationProperties.class);

			try {
				if (configurationProperties != null) {
					String configurationPrefix = configurationProperties.prefix().concat(".");
					Object newBeanInstance = targetClass.getConstructors()[0].newInstance();

					Map<String, Map<String, String>> result =
						processBean(configurationPrefix, targetClass, newBeanInstance, entry.getValue(), targetClass.getName());
					changedConfiguration.putAll(result);
				} else {
					changedConfiguration.put(targetClass.getName(), "Unable to resolve configuration. Class is not annotated with @ConfigurationProperties");
				}
			} catch (IllegalAccessException e) {
				changedConfiguration.put(targetClass.getName(), "Unable to resolve configuration. Class doesn't have public default constructor");
			} catch (InstantiationException e) {
				changedConfiguration.put(targetClass.getName(), "Unable to resolve configuration. Cannot create instance of abstract class");
			} catch (InvocationTargetException e) {
				changedConfiguration.put(targetClass.getName(), "Unable to resolve configuration. Exception occurred during instantiation");
			} catch (IntrospectionException e) {
				changedConfiguration.put(targetClass.getName(), "Unable to resolve configuration. Exception occurred during bean introspection");
			}
		}

		return changedConfiguration;
	}

	private static <T> Map<String, Map<String, String>> processBean(String prefix, Class<?> clazz, T defaultConfig, T coreProperties, String configurationClassName) throws IntrospectionException {
		return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
			.map(propertyDescriptor -> handleProperty(prefix, defaultConfig, coreProperties, propertyDescriptor, configurationClassName))
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@SneakyThrows private static <T, U> Map<String, Map<String, String>> handleProperty(String prefix, T defaultConfig, T currentConfig, PropertyDescriptor propertyDescriptor, String configurationClassName) {
		if (!Objects.equals(propertyDescriptor.getName(), "class") && !Objects.equals(propertyDescriptor.getName(), "annotatedInterfaces")) {
			Method method = propertyDescriptor.getReadMethod();
			if (method == null) {
				return Collections.emptyMap();
			}
			Class<U> propertyType = (Class<U>) propertyDescriptor.getPropertyType();
			U defaultValue = getValue(defaultConfig, method);
			U currentValue = getValue(currentConfig, method);

			if (isContainer(propertyType, configurationClassName)) {
				return processBean(prefix.concat(propertyDescriptor.getName()).concat("."), propertyType, defaultValue, currentValue, configurationClassName);
			} else if (!Objects.equals(defaultValue, currentValue)) {
				return Map.of(prefix.concat(propertyDescriptor.getName()),
					Map.of("default", Objects.toString(defaultValue), "current", Objects.toString(currentValue)));
			}
		}
		return Collections.emptyMap();
	}

	private static <T, U> U getValue(T defaultConfig, Method method) {
		return (U) Optional.ofNullable(defaultConfig)
			.map(obj -> {
				try {
					return method.invoke(obj);
				} catch (IllegalAccessException | InvocationTargetException e) {
					log.error(e.getMessage(), e);
					return null;
				}
			})
			.orElse(null);
	}

	private static boolean isContainer(Class<?> propertyType, String configurationClassName) {
		return !propertyType.isPrimitive() && !propertyType.isEnum() && propertyType
			.getTypeName().startsWith(configurationClassName);
	}
}
