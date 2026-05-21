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
package com.mgmtp.a12.contentstore;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.tika.mime.MediaType;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.content.internal.ContentRepository;
import com.mgmtp.a12.contentstore.content.internal.ContentService;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentEntity;
import com.mgmtp.a12.contentstore.content.internal.jpa.entity.ContentHeaderEntity;
import com.mgmtp.a12.contentstore.content.internal.jpa.repository.ContentHeaderJpaRepository;
import com.mgmtp.a12.contentstore.ticket.internal.TicketService;
import com.mgmtp.a12.contentstore.ticket.internal.TicketValidator;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;
import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;

import lombok.SneakyThrows;

import static com.mgmtp.a12.contentstore.constants.Constants.BASE_URL;
import static com.mgmtp.a12.contentstore.constants.Constants.CONTENT;

public abstract class AbstractContentStoreTest {

	@Mock protected ContentRepository contentRepository;
	@Mock protected ContentHeaderJpaRepository contentHeaderJpaRepository;
	@Spy protected ContentStoreProperties contentStoreProperties = new ContentStoreProperties();
	@Mock protected TicketService ticketService;
	@Mock protected ContentService contentService;
	@Mock protected TicketValidator ticketValidator;
	@Mock protected ContentTypeDetector contentTypeDetector;
	protected Clock fixedClock;
	protected Instant fixedClockInstant;
	private AutoCloseable mocks;

	@SneakyThrows
	@BeforeClass public void init() throws IOException {
		mocks = MockitoAnnotations.openMocks(this);
		Mockito.when(contentStoreProperties.getServer()).thenReturn(new ContentStoreProperties.Server());
		Mockito.when(contentStoreProperties.getBaseUrl()).thenReturn(BASE_URL);
		Mockito.when(contentStoreProperties.getContentWaitReadyTimeout()).thenReturn(10_000L);
		Mockito.when(contentTypeDetector.probeContentType(ArgumentMatchers.any(InputStream.class), ArgumentMatchers.isNull(String.class)))
			.thenReturn(MimeTypeUtils.ALL_VALUE);
		fixedClock = Clock.fixed(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
		fixedClockInstant = fixedClock.instant();
	}

	@SneakyThrows
	@AfterClass public void cleanup() {
		mocks.close();
	}

	public void assertContentEntity(ContentEntity expected, ContentEntity actual) {
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(expected.getContent(), actual.getContent());
	}

	protected void mockValidContentEntity(String contentId, String persistentType) {
		ContentHeaderEntity contentHeaderEntity = new ContentHeaderEntity(contentId, persistentType, MediaType.TEXT_PLAIN.toString());

		Mockito.when(contentRepository.findBinaryContentById(contentId)).thenReturn(Optional.of(CONTENT.getBytes()));

		Mockito.when(contentService.findByContentIdAndPersistentType(contentId, persistentType)).thenReturn(Optional.of(contentHeaderEntity));
		Mockito.when(contentService.findHeaderById(contentId)).thenReturn(Optional.of(contentHeaderEntity));

		Mockito.when(contentHeaderJpaRepository.findByIdAndPersistentTypeIgnoreCase(contentId, persistentType)).thenReturn(Optional.of(contentHeaderEntity));
		Mockito.when(contentHeaderJpaRepository.existsByIdAndPersistentType(contentId, persistentType)).thenReturn(true);
	}

	protected TicketInfoEntity mockValidTicket(String contentId, String ticketId, long duration) {
		TicketInfoEntity validTicket = TicketInfoEntity.builder()
			.contentId(contentId)
			.ticketId(ticketId)
			.expiredAt(fixedClockInstant.plus(duration, ChronoUnit.SECONDS))
			.downloaded(false)
			.build();
		Mockito.when(ticketService.createTicket(contentId, duration)).thenReturn(validTicket);
		Mockito.when(ticketService.findTicket(ticketId)).thenReturn(Optional.of(validTicket));
		return validTicket;
	}


}
