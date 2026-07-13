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
package com.mgmtp.a12.dataservices.experimental;

import java.util.function.Consumer;

import com.mgmtp.a12.dataservices.utils.internal.AbstractListProblemReporter;
import com.mgmtp.a12.dataservices.utils.internal.ProblemFormatter;
import com.mgmtp.a12.dataservices.utils.internal.RankedNotification2Problem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * This class has been moved to public API just to allow custom creation of IValidationCodeLoader without usage of internal classes. The experimental package denotes that
 * this class is not part of the stable API and may change in future non-breaking releases.
 *
 *
 * A problem reporter that collects problems in a list and formats them for output.
 * It implements the {@link IProblemReporter} interface and can be used to report problems
 */
public class ListIProblemReporter extends AbstractListProblemReporter<IProblem> implements IProblemReporter, Consumer<RankedNotification> {

	/**
	 * Reports a ranked notification by converting it into an {@link IProblem}.
	 *
	 * @param rankedNotification the notification to report; must not be `null`.
	 */
	@Override public void accept(RankedNotification rankedNotification) {
		reportProblem(new RankedNotification2Problem(rankedNotification));
	}

	/**
	 * Provides a formatter that renders a problem in a concise single-line representation.
	 *
	 * @return a {@link ProblemFormatter} that formats message, severity, line, source start and end positions.
	 */
	@Override public ProblemFormatter<IProblem> getFormatter() {
		return p -> "%s [%s,L%d,s%d,e%d],".formatted(p.getMessage(), p.getSeverity(), p.getLine(), p.getSourceStart(), p.getSourceEnd());
	}
}
