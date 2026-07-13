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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import com.mgmtp.a12.dataservices.DataServicesApplication;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.utils.ConfigurationParsingUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass public class GenericUtils {

	public static boolean isSingleAsterisk(Collection<String> inputList) {
		return ConfigurationParsingUtils.isSingleAsterisk(inputList);
	}

	public static boolean matchOrAll(String value, Collection<String> inputList) {
		return ConfigurationParsingUtils.matchOrAll(value, inputList);
	}

	@NotNull public static Reflections getApplicationReflections(ApplicationContext applicationContext) {
		String[] basePackages = applicationContext.getBeansWithAnnotation(DataServicesApplication.class).values().stream()
			.map(bean -> AnnotationUtils.findAnnotation(bean.getClass(), DataServicesApplication.class))
			.filter(Objects::nonNull)
			.map(DataServicesApplication::scanBasePackages)
			.findFirst()
			.orElse(new String[] { DataServicesCoreProperties.DS_PACKAGE_PREFIX });

		return new Reflections((Object[]) basePackages);
	}
}
