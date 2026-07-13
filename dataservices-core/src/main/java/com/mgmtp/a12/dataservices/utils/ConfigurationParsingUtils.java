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
package com.mgmtp.a12.dataservices.utils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.MATCH_ALL;

/**
 * Utility methods for parsing configuration values, including wildcard matching.
 *
 * These utilities are intended for use in Data Services configuration and extension code.
 * This class is not intended for external implementation.
 */
@OnlyForUsage
@Slf4j
@UtilityClass
public class ConfigurationParsingUtils {

	/**
	 * Checks whether the input collection contains only a single asterisk wildcard.
	 *
	 * @param inputList the collection to check; may be `null`
	 * @return `true` if the collection equals `["*"]`, `false` otherwise
	 */
	public static boolean isSingleAsterisk(Collection<String> inputList) {
		return Objects.equals(inputList, List.of(MATCH_ALL));
	}

	/**
	 * Checks whether a value is contained in the input list, treating a single asterisk as a
	 * match-all wildcard.
	 *
	 * If the list is empty or `null`, returns `false`.
	 * If the list contains only `"*"`, returns `true` for any value.
	 * If `"*"` appears alongside other entries, it is treated as a literal value and a warning
	 * is logged.
	 *
	 * @param value     the value to look up; must not be `null`
	 * @param inputList the list of allowed values; may be empty or `null`
	 * @return `true` if `value` matches an entry or the list is a single-asterisk wildcard
	 */
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
}
