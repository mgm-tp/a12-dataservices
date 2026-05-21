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
package com.mgmtp.a12.examples.attachment.virus.scan;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.examples.attachment.AttachmentContentValidator;

import lombok.RequiredArgsConstructor;

/**
 * Virus Scan Validator class.
 *
 */
@RequiredArgsConstructor
public class VirusScanValidator implements AttachmentContentValidator {
	private final VirusScanner virusScanner;

	/**
	 * Validates attachment content by delegating to the configured {@link VirusScanner}.
	 * Throws a {@link VirusScanException} if the scan does not indicate {@link VirusScanStatus#PASSED}.
	 *
	 * @param bytes raw content to scan; must not be null. May be empty.
	 */
	@Override public void validate(byte[] bytes) {
		VirusScanResult virusScanResult = virusScanner.scan(bytes);

		if (!virusScanResult.getStatus().equals(VirusScanStatus.PASSED)) {
			throw new VirusScanException(
				"error.attachment.virusSan.failed",
				virusScanResult,
				BaseException.MessagePriority.HIGH
			);
		}
	}
}
