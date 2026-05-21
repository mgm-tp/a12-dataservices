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
package com.mgmtp.a12.dataservices.relationship;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.mockito.Mockito;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.DefragmentRanksJob;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.rpc.links.RpcOrderedLinkIT;

import lombok.extern.slf4j.Slf4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Slf4j
public class RelationshipRankServiceIT extends RpcOrderedLinkIT {

	@MockitoSpyBean private RelationshipRankService relationshipRankService;

	@Autowired protected DefragmentRanksJob defragmentRanksJob;

	@Override
	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		addLinkWithPositionAndPredecessor(contract1DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract3DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", "");
	}

	@Test
	public void testRefreshRanks() throws SchedulerException {

		doAnswer(a -> {
			Object r = a.callRealMethod();
			assertEquals(r, 5);
			return r;
		})
			.when(relationshipRankService)
			.refreshRanks(eq(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL), eq(RoleConstants.PARTNER_ROLE),
				any(DocumentReference.class));

		doAnswer(a -> {
			Object r = a.callRealMethod();
			assertEquals(r, 1);
			return r;
		})
			.when(relationshipRankService)
			.refreshRanks(eq(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL), eq(RoleConstants.CONTRACT_ROLE),
				any(DocumentReference.class));

		JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
		when(context.getMergedJobDataMap()).thenReturn(makeJobData());
		defragmentRanksJob.execute(context);

		List<? extends RelationshipLink> links = relationshipLinkJpaRepository.findAll();
		assertEquals(links.size(), 5);
		assertExecutions(1, 1, 5);

		long count = relationshipRankService.refreshRanks(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		assertEquals(count, 10);
		assertExecutions(2, 2, 10);
	}

	private void assertExecutions(int mainMethod, int product, int campaign) {
		verify(relationshipRankService, times(mainMethod)).refreshRanks(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);

		verify(relationshipRankService, times(product))
			.refreshRanks(eq(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL), eq(RoleConstants.PARTNER_ROLE),
				any(DocumentReference.class));
		verify(relationshipRankService, times(campaign))
			.refreshRanks(eq(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL), eq(RoleConstants.CONTRACT_ROLE),
				any(DocumentReference.class));
	}

	private JobDataMap makeJobData() {
		JobDataMap data = new JobDataMap();
		data.put(DefragmentRanksJob.RM_TO_REORDER, RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		data.put(DefragmentRanksJob.ENABLED, true);
		return data;
	}

	private JobDetail makeJobDetail() {
		return JobBuilder.newJob()
			.ofType(DefragmentRanksJob.class)
			.withIdentity("defragmentRanksJob", "links")
			.withDescription("Defragment ranks job.")
			.usingJobData(DefragmentRanksJob.RM_TO_REORDER, RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL)
			.usingJobData(DefragmentRanksJob.ENABLED, true)
			.build();
	}

	private static class JL extends JobListenerSupport {

		private final AtomicLong triggered = new AtomicLong(0);

		@Override
		public String getName() {
			return "defragmentRanksJobListener";
		}

		public boolean isTriggered() {
			return triggered.get() > 0;
		}

		@Override
		public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
			if ("defragmentRanksJob".equals(context.getJobDetail().getKey().getName()) && "links".equals(context.getJobDetail().getKey().getGroup())) {
				triggered.incrementAndGet();
			}
		}
	}
}
