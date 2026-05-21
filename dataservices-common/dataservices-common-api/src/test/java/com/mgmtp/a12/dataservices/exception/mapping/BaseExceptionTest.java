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
package com.mgmtp.a12.dataservices.exception.mapping;

import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.BaseException;

import static org.testng.Assert.assertEquals;

import static com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority;
import static com.mgmtp.a12.dataservices.common.exception.BaseException.LocalizedMessageWithPriority;

public class BaseExceptionTest {

	public static final String INITIAL_MESSAGE = "Initial message";
	public static final String FIRST_CHANGE = "First change";
	public static final String SECOND_CHANGE = "second change";
	public static final String THIRD_CHANGE = "third change";

	@Test
	public void testOverwritingMessage() {
		BaseException e = new BaseException(-1, INITIAL_MESSAGE, MessagePriority.MEDIUM) {
		};
		assertMessage(e.getShortMessage(), INITIAL_MESSAGE, MessagePriority.MEDIUM);

		e.setShortMessage(new LocalizedEntry(null, FIRST_CHANGE), MessagePriority.LOW);
		assertMessage(e.getShortMessage(), INITIAL_MESSAGE, MessagePriority.MEDIUM);

		e.setShortMessage(new LocalizedEntry(null, FIRST_CHANGE), MessagePriority.MEDIUM);
		assertMessage(e.getShortMessage(), FIRST_CHANGE, MessagePriority.MEDIUM);

		e.setShortMessage(new LocalizedEntry(null, SECOND_CHANGE), MessagePriority.HIGH);
		assertMessage(e.getShortMessage(), SECOND_CHANGE, MessagePriority.HIGH);
	}

	@Test
	public void testOverwritingMessageWithoutPriority() {
		BaseException e = new BaseException(-1, INITIAL_MESSAGE) {
		};
		assertMessage(e.getShortMessage(), INITIAL_MESSAGE, null);

		e.setShortMessage(new LocalizedEntry(null, FIRST_CHANGE));
		assertMessage(e.getShortMessage(), FIRST_CHANGE, null);

		e.setShortMessage(new LocalizedEntry(null, SECOND_CHANGE), MessagePriority.LOW);
		assertMessage(e.getShortMessage(), SECOND_CHANGE, MessagePriority.LOW);

		e.setShortMessage(new LocalizedEntry(null, THIRD_CHANGE), null);
		assertMessage(e.getShortMessage(), SECOND_CHANGE, MessagePriority.LOW);
	}

	@Test
	public void testOverwritingPriorityMessageWithoutPriority() {
		BaseException e = new BaseException(-1, INITIAL_MESSAGE) {
		};
		assertMessage(e.getShortMessage(), INITIAL_MESSAGE, null);

		e.setShortMessage(new LocalizedMessageWithPriority(null, FIRST_CHANGE));
		assertMessage(e.getShortMessage(), FIRST_CHANGE, null);

		e.setShortMessage(new LocalizedMessageWithPriority(null, SECOND_CHANGE, MessagePriority.LOW));
		assertMessage(e.getShortMessage(), SECOND_CHANGE, MessagePriority.LOW);

		e.setShortMessage(new LocalizedMessageWithPriority(null, THIRD_CHANGE));
		assertMessage(e.getShortMessage(), SECOND_CHANGE, MessagePriority.LOW);
	}

	@Test
	public void testOverwritingPriorityMessage() {
		BaseException e = new BaseException(-1, INITIAL_MESSAGE, MessagePriority.MEDIUM) {
		};
		assertMessage(e.getShortMessage(), INITIAL_MESSAGE, MessagePriority.MEDIUM);

		e.setShortMessage(new LocalizedMessageWithPriority(null, FIRST_CHANGE, MessagePriority.LOW));
		assertMessage(e.getShortMessage(), INITIAL_MESSAGE, MessagePriority.MEDIUM);

		e.setShortMessage(new LocalizedMessageWithPriority(null, FIRST_CHANGE, MessagePriority.MEDIUM));
		assertMessage(e.getShortMessage(), FIRST_CHANGE, MessagePriority.MEDIUM);

		e.setShortMessage(new LocalizedMessageWithPriority(null, SECOND_CHANGE, MessagePriority.HIGH));
		assertMessage(e.getShortMessage(), SECOND_CHANGE, MessagePriority.HIGH);
	}

	private void assertMessage(BaseException.LocalizedMessageWithPriority shortMessage, String message, MessagePriority priority) {
		assertEquals(shortMessage.getDefaultMessage(), message);
		assertEquals(shortMessage.getPriority(), priority);
	}
}
