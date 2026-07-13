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
package com.mgmtp.a12.contentstore.server.actuator;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.configuration.internal.validation.WrongConfigurationMessage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Endpoint to display configuration changes for Content Store server.
 */
@Slf4j
@Endpoint(id = ContentStoreConfigurationEndpoint.CONFIGURATION_ENDPOINT) public class ContentStoreConfigurationEndpoint {

	/** Map key for changed configuration properties compared to defaults. */
	public static final String CHANGES = "changes";
	/** Map key for collected configuration warnings detected during validation. */
	public static final String WARNINGS = "warnings";
	/** Actuator endpoint identifier for Content Store configuration introspection. */
	public static final String CONFIGURATION_ENDPOINT = "contentStoreConfiguration";
	private final Map<String, Object> configData;

	/**
	 * Creates the configuration endpoint backed by a snapshot of differences and warnings.
	 *
	 * @param coreProperties the current {@link ContentStoreProperties} to inspect; never null.
	 * @throws IntrospectionException if JavaBeans introspection fails when reading properties.
	 */
	public ContentStoreConfigurationEndpoint(ContentStoreProperties coreProperties) throws IntrospectionException {
		configData = Map.of(CHANGES,
			processBean(ContentStoreProperties.PROPERTIES_PREFIX.concat("."), ContentStoreProperties.class, new ContentStoreProperties(), coreProperties),
			WARNINGS, WrongConfigurationMessage.EXISTING_WARNINGS);
	}

	/**
	 * Returns a snapshot of configuration differences and warnings.
	 *
	 * @return a map containing {@link #CHANGES} and {@link #WARNINGS} sections.
	 */
	@ReadOperation
	public Map<String, Object> all() {
		return configData;
	}

	/**
	 * Returns a single section of the configuration endpoint.
	 *
	 * @param name the section name; valid values are {@link #CHANGES} or {@link #WARNINGS}.
	 * @return the section payload, or `null` if the section is absent.
	 */
	@ReadOperation
	public Object sub(@Selector String name) {
		return configData.get(name);
	}

	private static <T> Map<String, Map<String, ?>> processBean(String prefix, Class<T> clazz, T defaultConfig, T coreProperties)
		throws IntrospectionException {
		return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
			.map(propertyDescriptor -> handleProperty(prefix, defaultConfig, coreProperties, propertyDescriptor))
			.filter(Objects::nonNull)
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@SneakyThrows
	private static <T, U> Map<String, Map<String, ?>> handleProperty(String prefix, T defaultConfig, T currentConfig, PropertyDescriptor propertyDescriptor) {
		if (!Objects.equals(propertyDescriptor.getName(), "class") && !Objects.equals(propertyDescriptor.getName(), "annotatedInterfaces")) {
			Method method = propertyDescriptor.getReadMethod();
			if (method != null) {
				Class<U> propertyType = (Class<U>) propertyDescriptor.getPropertyType();
				U defaultValue = getValue(defaultConfig, method);
				U currentValue = getValue(currentConfig, method);

				if (isContainer(propertyType)) {
					return processBean(prefix.concat(propertyDescriptor.getName()).concat("."), propertyType, defaultValue, currentValue);
				} else if (!Objects.equals(defaultValue, currentValue)) {
					return Map.of(prefix.concat(propertyDescriptor.getName()),
						Map.of("default", Objects.toString(defaultValue), "current", Objects.toString(currentValue)));
				}
			}
		}
		return Map.of();
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

	private static boolean isContainer(Class<?> propertyType) {
		return !propertyType.isPrimitive() && !propertyType.isEnum() && propertyType
			.getTypeName().startsWith(ContentStoreProperties.class.getName());
	}
}

