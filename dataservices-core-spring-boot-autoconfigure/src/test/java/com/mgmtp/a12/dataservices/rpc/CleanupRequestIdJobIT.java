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
import java.util.Date;
import java.util.Map;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.RequestIdState;
import com.mgmtp.a12.dataservices.rpc.internal.jpa.entity.RequestIdEntity;

import static com.mgmtp.a12.dataservices.rpc.CleanUpRequestIdJob.REQUEST_ID_EXPIRE_HOURS;
import static org.testng.Assert.assertEquals;

public class CleanupRequestIdJobIT extends AbstractSpringContextIT {
	
	private final String ID_TO_BE_DELETED = "toBeDeleted";
	private final String ID_NOT_TO_BE_DELETED = "notToBeDeleted";
	
	private RequestIdEntity requestIdToBeDeleted;
	private RequestIdEntity requestIdNotToBeDeleted;
	
	@Autowired CleanUpRequestIdJob cleanUpRequestIdJob;
	
	@BeforeMethod
	public void setUp() throws Exception {
		requestIdToBeDeleted = new RequestIdEntity(ID_TO_BE_DELETED, RequestIdState.PENDING, Instant.now().minusSeconds(365 * 24 * 3600));
		requestIdRepository.save(requestIdToBeDeleted);
		requestIdNotToBeDeleted = new RequestIdEntity(ID_NOT_TO_BE_DELETED, RequestIdState.FAILED, Instant.now());
		requestIdRepository.save(requestIdNotToBeDeleted);
	}
	
	@AfterMethod
	public void cleanUp() throws Exception {
		requestIdRepository.delete(requestIdToBeDeleted);
		requestIdRepository.delete(requestIdNotToBeDeleted);
	}

	@Test
	public void correctDelete() throws Exception {
		assertRequestId(ID_TO_BE_DELETED, true);
		JobExecutionContext context = new TestCleanUpRequestIdJobExecutionContext();
		cleanUpRequestIdJob.execute(context);
		assertRequestId(ID_TO_BE_DELETED, false);
	}
	
	@Test
	public void correctNotDelete() throws Exception {
		assertRequestId(ID_NOT_TO_BE_DELETED, true);
		JobExecutionContext context = new TestCleanUpRequestIdJobExecutionContext();
		cleanUpRequestIdJob.execute(context);
		assertRequestId(ID_NOT_TO_BE_DELETED, true);
	}
	
	private void assertRequestId(String id, boolean exists) {
		assertEquals(requestIdRepository.findById(id).isPresent(), exists);
	}

	private class TestCleanUpRequestIdJobExecutionContext implements JobExecutionContext {

		@Override public Scheduler getScheduler() {
			return null;
		}

		@Override public Trigger getTrigger() {
			return null;
		}

		@Override public Calendar getCalendar() {
			return null;
		}

		@Override public boolean isRecovering() {
			return false;
		}

		@Override public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
			return null;
		}

		@Override public int getRefireCount() {
			return 0;
		}

		@Override public JobDataMap getMergedJobDataMap() {

			return new JobDataMap(Map.of(REQUEST_ID_EXPIRE_HOURS, 24));
		}

		@Override public JobDetail getJobDetail() {
			return null;
		}

		@Override public Job getJobInstance() {
			return null;
		}

		@Override public Date getFireTime() {
			return null;
		}

		@Override public Date getScheduledFireTime() {
			return null;
		}

		@Override public Date getPreviousFireTime() {
			return null;
		}

		@Override public Date getNextFireTime() {
			return null;
		}

		@Override public String getFireInstanceId() {
			return null;
		}

		@Override public Object getResult() {
			return null;
		}

		@Override public void setResult(Object result) {
			//Needed to satisfy the interface, but not used in the test
		}

		@Override public long getJobRunTime() {
			return 0;
		}

		@Override public void put(Object key, Object value) {
			//Needed to satisfy the interface, but not used in the test
		}

		@Override public Object get(Object key) {
			return null;
		}
	}
}
