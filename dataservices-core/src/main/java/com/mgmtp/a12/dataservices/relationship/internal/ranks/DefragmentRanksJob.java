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
package com.mgmtp.a12.dataservices.relationship.internal.ranks;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefragmentRanksJob implements Job {

	public static final String RM_TO_REORDER = "rmToReorder";
	public static final String ENABLED = "enabled";

	private final RelationshipRankService relationshipRankService;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final BackendAuthenticationService backendAuthenticationService;

	@Override public void execute(JobExecutionContext context) throws JobExecutionException {
		backendAuthenticationService.executeWithBackendAuthentication(
			dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
			() -> {
				JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
				if (mergedJobDataMap.getBoolean(ENABLED)) {
					String rmToReorder = mergedJobDataMap.getString(RM_TO_REORDER);
					for (String rm : rmToReorder.split("\\s*,\\s*")) {
						try {
							log.info("Recalculated {} ranks for {}.", relationshipRankService.refreshRanks(rm), rm);
						} catch (Throwable t) {
							log.error(String.format("Something went wrong recalculating the %s ranks", rm), t);
						}
					}
				} else {
					log.warn("recalculation or ranks triggered, but it is disabled by configuration => No action performed.");
				}

				return null;
			});
	}
}
