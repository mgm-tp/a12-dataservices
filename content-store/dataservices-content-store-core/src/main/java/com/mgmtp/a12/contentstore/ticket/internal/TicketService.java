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

import java.util.Optional;

import com.mgmtp.a12.contentstore.ticket.internal.jpa.entity.TicketInfoEntity;

public interface TicketService {

	/**
	 * Implementation registers ticket for downloading a specific content by id
	 *
	 * @param contentId unique UUID to identify content for downloading
	 * @param duration Time to live in seconds for created ticket
	 * @return information of registered ticket
	 */
	TicketInfoEntity createTicket(String contentId, long duration);

	/**
	 * Implementation gets ticket information by ticket UUID
	 *
	 * @param ticketId unique UUID to retrieve ticket information
	 * @return information of registered ticket by ticket UUID
	 */
	Optional<TicketInfoEntity> findTicket(String ticketId);

	/**
	 * Implementation deletes a specific ticket by id
	 *
	 * @param ticketId unique UUID to delete
	 */
	void delete(String ticketId);

	/**
	 * Implementation updates ticket information like isDownloaded, invalidate or extends expire time of ticket
	 * @param ticketInfoEntity entity of ticket to be updated
	 */
	void update(TicketInfoEntity ticketInfoEntity);
}
