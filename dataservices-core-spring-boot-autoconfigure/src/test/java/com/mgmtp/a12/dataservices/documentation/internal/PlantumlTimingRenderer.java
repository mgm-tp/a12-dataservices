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
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.dataservices.documentation.internal.domain.AbstractLoggedElement;

public class PlantumlTimingRenderer extends AbstractRenderer {

	public PlantumlTimingRenderer(String title, Map<AbstractLoggedElement, List<AbstractLoggedElement>> threads) {
		super(title, threads);
	}

	public void render(PrintStream output) {
		output.println("@startuml");
		output.println("!procedure event(name, id)");
		output.println("concise \"name\" as id");
		output.println("!endprocedure");
		output.println("!procedure operation(name, id)");
		output.println("concise \"name\" as id");
		output.println("!endprocedure");
		output.println("!procedure security(name, id)");
		output.println("concise \"name\" as id");
		output.println("!endprocedure");

		renderTitle(output);

		output.println("scale 1000000 as 20 pixels");
		output.println("hide time-axis");
		output.println();

		renderDefinitions(output);

		getThreads().forEach((key, value) -> {
			renderCall(output, key);
			value.forEach(e -> {
				renderCall(output, e);
			});
		});

		output.println("@enduml");
	}

	protected void renderCall(PrintStream output, AbstractLoggedElement e) {
		if (e.getDirection() == AbstractLoggedElement.LoggedEventDirection.CALL) {

			output.println();
			output.print("@");
			output.println(e.getId());

			output.print(getNanos(e.getStarted()));
			output.print(" is \"");
			output.print(e.getName());
			output.print("\" #");
			switch (e.getType()) {
			case OPERATION:
				output.println("APPLICATION");
				break;
			case SECURITY:
				output.println("IMPLEMENTATION");
				break;
			case EVENT:
				output.println("TECHNOLOGY");
				break;
			}

			output.print(getNanos(e.getFinished()));
			output.println(" is {hidden}");
		}
	}

	private static String getNanos(Instant timestamp) {
		return String.format("%.0f",timestamp.getEpochSecond() * 1_000_000_000D + timestamp.getNano());
	}
}
