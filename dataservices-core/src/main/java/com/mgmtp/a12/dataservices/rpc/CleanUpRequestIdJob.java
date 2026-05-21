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
package com.mgmtp.a12.dataservices.rpc;

import java.time.Instant;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.rpc.internal.jpa.repository.RequestIdRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * A Quartz job that deletes old request IDs from the database. This is to prevent the request ID table from growing too large.
 *
 */
@Slf4j
public class CleanUpRequestIdJob implements Job {

	/**
	 * Job data map key specifying the expiration threshold in hours for request IDs.
	 * Entries older than this number of hours are deleted.
	 */
	public static final String REQUEST_ID_EXPIRE_HOURS = "requestIdExpireHours";

	@Autowired private RequestIdRepository requestIdRepository;

	/**
	 * Deletes request IDs older than the configured threshold.
	 * Reads {@link #REQUEST_ID_EXPIRE_HOURS} from the merged job data map and removes entries older than that number of hours.
	 *
	 * @param context The Quartz job execution context; never `null`.
	 * @throws JobExecutionException If job execution fails unexpectedly.
	 */
	@Transactional
	@Override public void execute(JobExecutionContext context) throws JobExecutionException {
		int tmpRequestIdExpireHours = context.getMergedJobDataMap().getInt(REQUEST_ID_EXPIRE_HOURS);
		log.debug("Delete requestId job triggered with parameter requestIdExpireHours = {}.", tmpRequestIdExpireHours);
		// Delete all entries in table request_id that are older than tmpRequestIdExpireHours
		requestIdRepository.deleteAllInBatch(requestIdRepository.findByTimestampBefore(Instant.now().minusSeconds(tmpRequestIdExpireHours * 3600L)));
	}
}
