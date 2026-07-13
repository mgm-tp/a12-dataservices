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
package com.mgmtp.a12.contentstore.configuration.internal.validation;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * This class will be used for construct message to display properties related to Content Store configuration
 */
@Data @SuperBuilder @EqualsAndHashCode(of = { "relatedProperties", "message"})
public class ConfigurationMessage {

	/**
	 * The list of properties related to specific configuration
	 */
	private List<String> relatedProperties;
	/**
	 * The message for describing configuration and properties
	 */
	private String message;

	@Override
	public String toString() {
		String heading = "==== " + getTitle() + " ==========";
		String props = "- %s".formatted(String.join("\n- ", getRelatedProperties()));
		int headingLength = heading.length();
		String row = "-".repeat(headingLength);
		String endRow = "=".repeat(headingLength);
		return "Content Store configuration\n%s\n%s\n%s\n%s\nRelated properties:\n%s\n%s".formatted(
			heading, getSubtitle(), indentString(getMessage(), 2), row, props, endRow);
	}

	protected String getTitle() {
		return "Configuration";
	}

	protected String getSubtitle() {
		return "State:";
	}

	public abstract static class ConfigurationMessageBuilder<C extends ConfigurationMessage, B extends ConfigurationMessageBuilder<C, B>> {
	}

	private static String indentString(String input, int n) {
		char[] chars = new char[n];
		Arrays.fill(chars, ' ');
		return input.replaceAll("(?m)^", new String(chars));
	}
}
