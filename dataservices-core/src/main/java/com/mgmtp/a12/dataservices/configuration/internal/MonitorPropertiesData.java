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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorPropertiesData {

	private static final List<String> MONITORING_PROPERTIES = List.of(
		"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize",
		"mgmtp.a12.dataservices.jsonRpc.maxMethodCallsPerRequest",
		"mgmtp.a12.dataservices.query.maxQueryDepth",
		"mgmtp.a12.dataservices.query.maxLinksSize"
	);

	private final Map<String, Object> monitoringValues = new HashMap<>();

	public MonitorPropertiesData(ApplicationContext applicationContext, ConfigurableEnvironment environment) {
		Map<String, Object> defaultValues = PropertiesExtractor.extractDefaultProperties(applicationContext);

		MONITORING_PROPERTIES.forEach((monitoringKey) -> {
			monitoringValues.put(monitoringKey,
				Objects.toString(defaultValues.get(PropertiesExtractor.toKebabCase(monitoringKey)))
			);
		});

		// override default monitoring properties with actual values from environment
		MONITORING_PROPERTIES.forEach((monitoringProperty) -> {
			if (environment.getProperty(PropertiesExtractor.toKebabCase(monitoringProperty)) != null) {
				monitoringValues.put(
					monitoringProperty,
					environment.getProperty(PropertiesExtractor.toKebabCase(monitoringProperty))
				);
			}
		});
	}

	public Map<String, Object> getMonitorProperties() {
		return monitoringValues;
	}


	private static final class PropertiesExtractor {

		private static final String CLASS_PROPERTY = "class";

		private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
			String.class, Boolean.class, Character.class,
			Byte.class, Short.class, Integer.class, Long.class,
			Float.class, Double.class
		);


		static String toKebabCase(String camelCase) {
			return camelCase.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
		}

		static Map<String, Object> extractDefaultProperties(ApplicationContext applicationContext) {
			Map<String, Object> defaults = new HashMap<>();
			ConfigurableListableBeanFactory beanFactory =
				((ConfigurableApplicationContext) applicationContext).getBeanFactory();

			for (String beanName : beanFactory.getBeanNamesForAnnotation(ConfigurationProperties.class)) {
				Object bean = applicationContext.getBean(beanName);
				findAnnotation(beanFactory, beanName, bean, applicationContext)
					.ifPresent(annotation -> {
						Class<?> beanClass = resolveTargetClass(bean.getClass());
						extractProperties(bean, beanClass, getPrefix(annotation), new HashSet<>(), defaults);
					});
			}
			return defaults;
		}

		private static Optional<ConfigurationProperties> findAnnotation(
			ConfigurableListableBeanFactory beanFactory,
			String beanName,
			Object bean,
			ApplicationContext applicationContext) {

			Class<?> beanClass = resolveTargetClass(bean.getClass());
			ConfigurationProperties annotation = AnnotationUtils.findAnnotation(beanClass, ConfigurationProperties.class);
			if (annotation != null) {
				return Optional.of(annotation);
			}
			return findAnnotationOnFactoryMethod(beanFactory, beanName, applicationContext);
		}

		private static Optional<ConfigurationProperties> findAnnotationOnFactoryMethod(
			ConfigurableListableBeanFactory beanFactory,
			String beanName,
			ApplicationContext applicationContext) {
			try {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				String factoryMethodName = beanDefinition.getFactoryMethodName();
				String factoryBeanName = beanDefinition.getFactoryBeanName();

				if (factoryMethodName == null || factoryBeanName == null) {
					return Optional.empty();
				}

				Object factoryBean = applicationContext.getBean(factoryBeanName);
				Class<?> factoryClass = resolveTargetClass(factoryBean.getClass());

				for (Method method : factoryClass.getDeclaredMethods()) {
					if (method.getName().equals(factoryMethodName)) {
						ConfigurationProperties annotation = AnnotationUtils.findAnnotation(method, ConfigurationProperties.class);
						if (annotation != null) {
							return Optional.of(annotation);
						}
					}
				}
			} catch (Exception e) {
				log.debug("Could not inspect factory method for bean: {}", beanName, e);
			}
			return Optional.empty();
		}

		private static void extractProperties(Object bean, Class<?> beanClass, String prefix,
			Set<Object> visited, Map<String, Object> target) {
			if (bean == null || !visited.add(bean)) {
				return;
			}

			// Create a fresh instance to get true defaults
			Object defaultInstance = createDefaultInstance(beanClass);
			if (defaultInstance == null) {
				return;
			}

			for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(beanClass)) {
				if (CLASS_PROPERTY.equals(descriptor.getName()) || descriptor.getReadMethod() == null) {
					continue;
				}
				extractProperty(defaultInstance, descriptor, prefix, visited, target);
			}
		}

		private static Object createDefaultInstance(Class<?> beanClass) {
			try {
				return beanClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				log.debug("Could not create default instance for: {}", beanClass.getName(), e);
				return null;
			}
		}

		private static void extractProperty(Object bean, PropertyDescriptor descriptor, String prefix,
			Set<Object> visited, Map<String, Object> target) {
			if (bean == null) {
				return;
			}
			try {
				Object value = descriptor.getReadMethod().invoke(bean);
				String fullKey = toKebabCase(prefix + "." + descriptor.getName());

				if (value == null || isSimpleType(value.getClass())) {
					target.put(fullKey, value);
				} else if (value instanceof Map<?, ?> map) {
					extractMapProperties(map, fullKey, target);
				} else if (value instanceof Collection<?> collection) {
					extractCollectionProperties(collection, fullKey, target);
				} else if (isConfigurationClass(value.getClass())) {
					extractPropertiesFromInstance(value, value.getClass(), fullKey, visited, target);
				}
			} catch (Exception e) {
				// ignore error
			}
		}

		private static void extractPropertiesFromInstance(Object instance, Class<?> beanClass, String prefix,
			Set<Object> visited, Map<String, Object> target) {
			if (instance == null || !visited.add(instance)) {
				return;
			}
			for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(beanClass)) {
				if (CLASS_PROPERTY.equals(descriptor.getName()) || descriptor.getReadMethod() == null) {
					continue;
				}
				extractProperty(instance, descriptor, prefix, visited, target);
			}
		}

		private static void extractMapProperties(Map<?, ?> map, String prefix, Map<String, Object> target) {
			map.forEach((k, v) -> {
				String key = prefix + "." + toKebabCase(String.valueOf(k));
				if (v == null || isSimpleType(v.getClass())) {
					target.put(key, v);
				}
			});
		}

		private static void extractCollectionProperties(Collection<?> collection, String key, Map<String, Object> target) {
			target.put(key, collection.toString());
		}

		private static String getPrefix(ConfigurationProperties annotation) {
			return annotation.prefix().isEmpty() ? annotation.value() : annotation.prefix();
		}

		private static Class<?> resolveTargetClass(Class<?> clazz) {
			return clazz.getName().contains("$$") ? clazz.getSuperclass() : clazz;
		}

		private static boolean isSimpleType(Class<?> type) {
			return type.isPrimitive() || type.isEnum() || SIMPLE_TYPES.contains(type);
		}

		private static boolean isConfigurationClass(Class<?> type) {
			return !type.isArray()
				&& !type.isInterface()
				&& !type.getName().contains("$$");
		}

	}
}
