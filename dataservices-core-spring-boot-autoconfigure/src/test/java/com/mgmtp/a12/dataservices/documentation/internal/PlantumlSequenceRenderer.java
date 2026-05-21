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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.documentation.internal.domain.AbstractLoggedElement;
import com.mgmtp.a12.dataservices.documentation.internal.domain.Caller;

import lombok.NonNull;

public class PlantumlSequenceRenderer extends AbstractRenderer {

	public PlantumlSequenceRenderer(String title, Map<AbstractLoggedElement, List<AbstractLoggedElement>> threads) {
		super(title, threads);
	}

	@Override public void render(PrintStream output) {
		output.println("@startuml");
		output.println("!procedure event(name, id)");
		output.println("boundary \"name\" as id <<Event>> #TECHNOLOGY");
		output.println("!endprocedure");
		output.println("!procedure operation(name, id)");
		output.println("actor \"name\" as id <<Operation>> #APPLICATION");
		output.println("!endprocedure");
		output.println("!procedure security(name, id)");
		output.println("participant \"name\" as id <<Security>> #IMPLEMENTATION");
		output.println("!endprocedure");

		renderTitle(output);

		renderDefinitions(output);

		getThreads().forEach((key, value) -> {
			final Deque<AbstractLoggedElement> stack = new ArrayDeque<>();
			stack.push(key);
			output.print("activate ");
			output.println(key.getId());
			value.forEach(e -> {
				renderCall(output, stack, e);
			});
			output.print("deactivate ");
			output.println(key.getId());
		});

		output.println("@enduml");
	}

	protected void renderCall(PrintStream output, Deque<AbstractLoggedElement> stack, AbstractLoggedElement e) {
		if (e.getDirection() == AbstractLoggedElement.LoggedEventDirection.CALL) {
			output.print(stack.peek().getId());
			output.print(" -> ");
			output.print(e.getId());
			output.print("++");
			renderRelationship(output, e);
			output.println();
			stack.push(e);
		} else {
			output.print("'return ");
			output.println(e.getId());
			output.println("return");
			stack.remove();
		}
	}

	private void renderRelationship(PrintStream output, AbstractLoggedElement element) {
		String r = element.getCallingMethods()
			.map(e -> String.format(getFormatByModifier(e.getModifier()), e.getClassName().replaceFirst("^com\\.mgmtp\\.a12\\.dataservices", ""),
				e.getMethodName()))
			.collect(Collectors.joining("\\n↪ "));
		if (StringUtils.isNotBlank(r)) {
			output.print(": ");
			output.println(r);
		}
	}

	@NonNull private static String getFormatByModifier(Caller.CallerModifier modifier) {
		switch (modifier) {
		case PUBLIC:
			return "**%s#%s**";
		case PRIVATE:
			return "--%s#%s--";
		case INTERCEPTOR:
			return "//%s#%s//";
		case INTERNAL:
			return "~~%s#%s~~";
		default:
			return "%s#%s";
		}
	}
}
