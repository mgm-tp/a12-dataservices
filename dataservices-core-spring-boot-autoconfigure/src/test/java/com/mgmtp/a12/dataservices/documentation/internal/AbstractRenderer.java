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
package com.mgmtp.a12.dataservices.documentation.internal;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.documentation.internal.domain.AbstractLoggedElement;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public abstract class AbstractRenderer {
	private final String title;
	private final Map<AbstractLoggedElement, List<AbstractLoggedElement>> threads;
	private boolean titleEnabled = false;

	public abstract void render(PrintStream output);

	protected void renderTitle(PrintStream output) {
		if (titleEnabled) {
			if (StringUtils.isNotBlank(getTitle())) {
				output.print("title ");
				output.println(getTitle());
			}
			output.println();
		}
	}

	protected void renderDefinitions(PrintStream output) {
		Set<String> renderedIds = new HashSet<>();
		Stream.concat(getThreads().keySet().stream(), getThreads().values().stream().flatMap(Collection::stream))
			.filter(e -> renderedIds.add(e.getId()))
			.forEach(e -> renderObject(output, e));
	}

	protected void renderObject(PrintStream output, AbstractLoggedElement e) {
		switch (e.getType()) {
		case EVENT:
			renderEvent(output, e);
			break;
		case SECURITY:
			renderSecurity(output, e);
			break;
		case OPERATION:
			renderOperation(output, e);
			break;
		}
	}

	protected void renderOperation(PrintStream output, AbstractLoggedElement e) {
		renderEntity(output, "operation", e);
	}

	protected void renderSecurity(PrintStream output, AbstractLoggedElement e) {
		renderEntity(output, "security", e);
	}

	protected void renderEvent(PrintStream output, AbstractLoggedElement e) {
		renderEntity(output, "event", e);
	}

	private static void renderEntity(PrintStream output, String type, AbstractLoggedElement e) {
		output.print(type);
		output.print("(\"");
		output.print(e.getName());
		output.print("\", \"");
		output.print(e.getId());
		output.println("\")");
		output.println();
	}
}
