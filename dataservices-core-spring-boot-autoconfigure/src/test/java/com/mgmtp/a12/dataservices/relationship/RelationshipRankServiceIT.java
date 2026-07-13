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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.DefragmentRanksJob;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.links.RpcOrderedLinkIT;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

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
	}

	@Test
	public void testRefreshRanks() throws SchedulerException, IOException {
		addLinkWithPositionAndPredecessor(contract1DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract3DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "");
		addLinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", "");

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

	@Test
	public void addLinksWithoutSpec_shouldSortTargetEntitiesLastOnTop() throws IOException {
		String link1 = addLinkWithoutPositionNorPredecessor(contract1DocRef);
		String link2 = addLinkWithoutPositionNorPredecessor(contract2DocRef);
		String link3 = addLinkWithoutPositionNorPredecessor(contract3DocRef);
		String link4 = addLinkWithoutPositionNorPredecessor(contract4DocRef);
		String link5 = addLinkWithoutPositionNorPredecessor(contract5DocRef);

		assertQueryIsOrdered(link5, link4, link3, link2, link1);
	}

	@Test
	public void addLinksWithoutSpec_shouldSortSourceEntitiesLastOnBottom() throws IOException {
		addLinkWithoutPositionNorPredecessor(partner1DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner2DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner3DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner4DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner5DocRef, contract1DocRef);

		assertSourcesOrderedForContract(contract1DocRef, partner1DocRef, partner2DocRef, partner3DocRef, partner4DocRef, partner5DocRef);
	}

	@Test
	@Transactional
	public void addLinksWithoutSpec_shouldProduceUniqueComplementaryRanks() throws IOException {
		addLinkWithoutPositionNorPredecessor(partner1DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner2DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner3DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner4DocRef, contract1DocRef);
		addLinkWithoutPositionNorPredecessor(partner5DocRef, contract1DocRef);

		List<String> complementaryRanks = relationshipLinkJpaRepository.findAll().stream()
			.map(link -> link.getRoles().get(RoleConstants.CONTRACT_ROLE))
			.map(RelationshipRole::getOrder)
			.toList();

		Assert.assertEquals(complementaryRanks.size(), 5, "Expected one complementary rank per link");
		Assert.assertEquals(Set.copyOf(complementaryRanks).size(), complementaryRanks.size(),
			"All complementary rank values must be unique but were: " + complementaryRanks);
	}

	private void assertSourcesOrderedForContract(DocumentReference contractDocRef, DocumentReference... expectedSources) {
		QueryRoot queryLink = constructQueryLink(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, contractDocRef, RoleConstants.PARTNER_ROLE);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		Assert.assertEquals(result.getFullSize(), expectedSources.length);
		List<DocumentTreeResult> children = result.getLinks().stream()
			.filter(link -> link.getType() == DocumentTreeNodeType.CHILD)
			.toList();

		for (int i = 0; i < expectedSources.length; i++) {
			DocumentReference expected = expectedSources[i];
			DocumentReference actual = children.get(i).getTargetDocRef();
			Assert.assertEquals(actual, expected, "On position #%d there is".formatted(i));
		}
	}

	private String addLinkWithoutPositionNorPredecessor(DocumentReference sourceDocRef, DocumentReference targetDocRef) throws IOException {
		String template = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "add/add_link_head.json");
		String request = template.formatted(RoleConstants.PARTNER_ROLE, sourceDocRef, RoleConstants.CONTRACT_ROLE, targetDocRef, "");
		JsonRpc2Response rpcResponse = sendRpcRequest(request).getFirst();
		return convertResponse(rpcResponse.getResult().toString(), RelationshipLinkSpec.class).getId();
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
