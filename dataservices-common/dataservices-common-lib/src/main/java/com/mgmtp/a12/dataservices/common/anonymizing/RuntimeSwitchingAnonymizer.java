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
package com.mgmtp.a12.dataservices.common.anonymizing;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;


/**
 * This {@link Anonymizer} implementation allows toggling anonymization at runtime.
 * By default, anonymization is enabled. However, you can connect to the running JVM using JMC
 * and toggle anonymization off via the `com.mgmtp.a12.dataservices.common.anonymizing.RuntimeSwitchingAnonymizer`
 * class, using the `anonymizationEnabled` flag.
 */
@ManagedResource public class RuntimeSwitchingAnonymizer implements Anonymizer {

	private static final Anonymizer ANONYMIZER_BYPASS = s -> s;
	private final Anonymizer anonymizer = new AsterixAnonymizer();
	private boolean anonymizationEnabled;

	/**
	 * Creates a new runtime-switchable anonymizer.
	 *
	 * @param anonymizationEnabled initial switch state; when `true` anonymization is applied, when `false` input is passed through unchanged.
	 */
	public RuntimeSwitchingAnonymizer(boolean anonymizationEnabled) {
		this.anonymizationEnabled = anonymizationEnabled;
	}

	/**
	 * Toggles anonymization on or off via JMX.
	 *
	 * @param bypass when `true` anonymization is bypassed (no masking); when `false` anonymization is enabled.
	 */
	@ManagedAttribute public void setAnonymizerBypass(boolean bypass) {
		this.anonymizationEnabled = bypass;
	}

	/**
	 * Indicates whether anonymization is currently bypassed.
	 *
	 * @return `true` if anonymization is bypassed (no masking); `false` if anonymization is enabled.
	 */
	@ManagedAttribute public boolean getAnonymizerBypass() {
		return this.anonymizationEnabled;
	}

	/**
	 * Applies anonymization to the given input depending on the current switch state.
	 * Null input is converted to an empty string before processing.
	 *
	 * @param s input text; may be `null`, which is treated as an empty string.
	 * @return anonymized text when enabled; original text when bypass is active.
	 */
	@Override public String apply(String s) {
		if (s == null) {
			s = "";
		}

		return anonymizationEnabled ? anonymizer.apply(s) : ANONYMIZER_BYPASS.apply(s);
	}
}
