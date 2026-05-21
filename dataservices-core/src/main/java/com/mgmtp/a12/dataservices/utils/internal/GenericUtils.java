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
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import com.mgmtp.a12.dataservices.DataServicesApplication;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.MATCH_ALL;

@Slf4j
@UtilityClass public class GenericUtils {

	public static boolean isSingleAsterisk(Collection<String> inputList) {
		return Objects.equals(inputList, List.of(MATCH_ALL));
	}

	public static boolean matchOrAll(String value, Collection<String> inputList) {
		if (CollectionUtils.isEmpty(inputList)) {
			return false;
		} else if (isSingleAsterisk(inputList)) {
			return true;
		} else if (inputList.contains(MATCH_ALL)) {
			log.warn(
				"There is '{}' in the list of the supported values, but it is considered to be exact value instead of wildcard because there are more values in the list.",
				MATCH_ALL);
		}
		return inputList.contains(value);
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
