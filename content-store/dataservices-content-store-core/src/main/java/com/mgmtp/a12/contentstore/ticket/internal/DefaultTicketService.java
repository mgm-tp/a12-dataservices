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
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;
import com.mgmtp.a12.contentstore.ticket.internal.jpa.repository.TicketInfoJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service public class DefaultTicketService implements TicketService {
	private final TicketInfoJpaRepository ticketInfoJpaRepository;

	@Override public TicketInfoEntity createTicket(String contentId, long duration) {
		return ticketInfoJpaRepository.save(TicketInfoEntity.builder()
			.contentId(contentId)
			.ticketId(UUID.randomUUID().toString())
			.expiredAt(Instant.now().plus(duration, ChronoUnit.SECONDS))
			.build());
	}

	@Override public Optional<TicketInfoEntity> findTicket(String ticketId) {
		return ticketInfoJpaRepository.findById(ticketId);
	}

	@Override public void delete(String ticketId) {
		ticketInfoJpaRepository.deleteById(ticketId);
	}

	@Override public void update(TicketInfoEntity ticketInfoEntity) {
		ticketInfoJpaRepository.save(ticketInfoEntity);
	}
}
