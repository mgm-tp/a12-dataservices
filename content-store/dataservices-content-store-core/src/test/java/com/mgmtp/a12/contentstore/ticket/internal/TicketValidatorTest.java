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
package com.mgmtp.a12.contentstore.ticket.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;

import static com.mgmtp.a12.contentstore.constants.Constants.THREE_MINUTES_DURATION;

public class TicketValidatorTest extends AbstractContentStoreTest {

	@InjectMocks
	private TicketValidator ticketValidator;

	@DataProvider
	public Object[][] ticketValidatorTestData() {
		return new Object[][] {
			new Object[] { true, false, 1L, true },
			new Object[] { true, true, 1L, true },
			new Object[] { true, true, 0L, false },
			new Object[] { true, false, 0L, false },
			new Object[] { true, true, -1L, false },
			new Object[] { true, false, -1L, false },
			new Object[] { false, false, 1L, true },
			new Object[] { false, true, 1L, false },
			new Object[] { false, true, 0L, false },
			new Object[] { false, false, 0L, false },
			new Object[] { false, true, -1L, false },
			new Object[] { false, false, -1L, false }
		};
	}

	@Test(dataProvider = "ticketValidatorTestData")
	public void testIsAvailableTicket(boolean isMultiDownload, boolean isDownloaded, long seconds, boolean expectedResult) {
		Instant expiredAt = fixedClockInstant.plus(seconds, ChronoUnit.SECONDS);
		String contentId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		try (MockedStatic<Instant> instant = Mockito.mockStatic(Instant.class)) {
			instant.when(Instant::now).thenReturn(fixedClockInstant);
			TicketInfoEntity ticketInfoEntity =
				TicketInfoEntity.builder().ticketId(ticketId).contentId(contentId).downloaded(isDownloaded)
					.expiredAt(expiredAt)
					.build();
			ContentStoreProperties.EnabledProperty enabledProperty = new ContentStoreProperties.EnabledProperty();
			enabledProperty.setEnabled(isMultiDownload);
			Mockito.when(contentStoreProperties.getTicketDuration()).thenReturn(THREE_MINUTES_DURATION);
			Mockito.when(contentStoreProperties.getTicketMultiDownload()).thenReturn(enabledProperty);
			Assert.assertEquals(ticketValidator.isAvailableTicket(ticketInfoEntity), expectedResult);
		}
	}
}
