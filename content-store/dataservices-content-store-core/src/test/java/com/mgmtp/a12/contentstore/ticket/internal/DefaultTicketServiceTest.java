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
import java.util.Random;
import java.util.UUID;

import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.repository.TicketInfoJpaRepository;


public class DefaultTicketServiceTest extends AbstractContentStoreTest {

	@Mock
	private TicketInfoJpaRepository ticketInfoJpaRepository;

	@InjectMocks
	private DefaultTicketService ticketService;

	@Test
	public void createTicket_shouldSuccessAndReturnTicketInfoWithUuid() {
		Mockito.when(ticketInfoJpaRepository.save(ArgumentMatchers.any(TicketInfoEntity.class))).then(AdditionalAnswers.returnsFirstArg());
		long duration = new Random().nextLong(100, 1000);
		UUID ticketUuidId = UUID.randomUUID();
		String contentId = UUID.randomUUID().toString();

		try (MockedStatic<Instant> instant = Mockito.mockStatic(Instant.class);
			MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {

			instant.when(Instant::now).thenReturn(fixedClockInstant);
			uuid.when(UUID::randomUUID).thenReturn(ticketUuidId);
			TicketInfoEntity actualTicket = ticketService.createTicket(contentId, duration);
			Assert.assertEquals(actualTicket.getContentId(), contentId);
			Assert.assertEquals(actualTicket.getTicketId(), ticketUuidId.toString());
			Assert.assertEquals(actualTicket.getExpiredAt(), fixedClock.instant().plus(duration, ChronoUnit.SECONDS));
		}
	}
}
