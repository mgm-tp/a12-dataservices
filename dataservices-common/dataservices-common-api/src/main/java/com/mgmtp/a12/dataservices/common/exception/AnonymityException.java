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
package com.mgmtp.a12.dataservices.common.exception;

/**
 * Contract for providing an anonymity-safe message representation.
 * Use this to avoid logging user-related or sensitive content while still conveying context.
 *
 * We CAN log:
 * 1. Model names
 * 2. Document meta-data (except creator and modifier)
 * 3. Link data
 * 4. Attachment meta-data
 *
 * We CANNOT log:
 * 1. Model content (field paths are acceptable)
 * 2. Document content
 * 3. Attachment content
 *
 * @see com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer
 * @see BaseException#getAnonymityMessage()
 */
public interface AnonymityException {

	/**
	 * Returns a message with all sensitive information removed or masked.
	 *
	 * @return An anonymity-safe message; never contains personal data.
	 */
	String getAnonymityMessage();
}
