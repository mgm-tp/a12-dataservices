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
package com.mgmtp.a12.dataservices.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;

@SpringBootTest
public class CleanupJobsDefaultScheduleIT extends AbstractSpringContextIT {

	private static final String EXPECTED_DIRTY_ATTACHMENTS_SCHEDULE = "0 0 1 * * ?";
	private static final String EXPECTED_STALE_ATTACHMENTS_SCHEDULE = "0 15 1 * * ?";
	private static final String EXPECTED_REQUEST_ID_SCHEDULE = "0 30 1 * * ?";
	private static final String EXPECTED_RANK_RECALCULATION_SCHEDULE = "0 45 1 ? * SUN";

	@Autowired DataServicesCoreProperties dataServicesCoreProperties;

	@Test(description ="Should use daily schedule as default for cleanupRequestId schedule property")
	public void shouldUseDailyScheduleAsDefaultForCleanupRequestIdSchedule() {
		Assert.assertEquals(
			dataServicesCoreProperties.getJobs().getRequests().getCleanupRequestId().getSchedule(),
			EXPECTED_REQUEST_ID_SCHEDULE);
	}

	@Test(description ="Should use daily schedule as default for cleanUpDirtyAttachments schedule property")
	public void shouldUseDailyScheduleAsDefaultForCleanUpDirtyAttachmentsSchedule() {
		Assert.assertEquals(
			dataServicesCoreProperties.getJobs().getAttachments().getCleanUpDirtyAttachments().getSchedule(),
			EXPECTED_DIRTY_ATTACHMENTS_SCHEDULE);
	}

	@Test(description ="Should use daily schedule as default for cleanUpStaleAttachments schedule property")
	public void shouldUseDailyScheduleAsDefaultForCleanUpStaleAttachmentsSchedule() {
		Assert.assertEquals(
			dataServicesCoreProperties.getJobs().getAttachments().getCleanUpStaleAttachments().getSchedule(),
			EXPECTED_STALE_ATTACHMENTS_SCHEDULE);
	}

	@Test(description ="Should use weekly schedule as default for rankRecalculation schedule property")
	public void shouldUseWeeklyScheduleAsDefaultForRankRecalculationSchedule() {
		Assert.assertEquals(
			dataServicesCoreProperties.getJobs().getRelationships().getRankRecalculation().getSchedule(),
			EXPECTED_RANK_RECALCULATION_SCHEDULE);
	}
}
