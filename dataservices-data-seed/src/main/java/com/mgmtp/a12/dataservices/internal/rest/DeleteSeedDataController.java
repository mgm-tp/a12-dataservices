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
package com.mgmtp.a12.dataservices.internal.rest;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.dataservices.internal.condition.DeleteSeedDataEnabledCondition;
import com.mgmtp.a12.dataservices.internal.service.SeedDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This API provides the ability to delete models, documents, relationship links, attachments.
 *
 * @title Internal Seed Data Delete REST API
 * @topic Seed Data (Internal API): For testing and demo purposes only. Not intended for production use.
 */

@Slf4j
@RequiredArgsConstructor
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/internal/seed-data")
@Conditional(DeleteSeedDataEnabledCondition.class)
@RestController public class DeleteSeedDataController {

	private final SeedDataService seedDataService;

	/**
	 * Endpoint allowing delete seed data.
	 *
	 * @title Delete Seed Data
	 * @headers Accept:: application/json
	 * @responseSuccess 204 No Content:: Delete Seed Data successfully.
	 */
	@DeleteMapping(path = { "", "/" })
	public ResponseEntity<Void> deleteSeedData() {
		log.debug("Deleting seed-data");
		StopWatch sw = StopWatch.createStarted();
		seedDataService.deleteAllData();
		log.info("Seed-data deleted in {}", sw.formatTime());
		return ResponseEntity.noContent().build();
	}
}
