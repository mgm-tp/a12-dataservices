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
package com.mgmtp.a12.dataservices.internal.ranks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

import com.mgmtp.a12.dataservices.relationship.internal.ranks.OutOfRanksException;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankComputer;

import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;

@Slf4j
public class RelationshipRankComputerTest {

	private static final int NUMBER_OF_INSERTS = 6300;

	@Test
	public void insertAlwaysAtTheEnd() throws OutOfRanksException  {
		List<String> ranks = new ArrayList<>(List.of(RelationshipRankComputer.computeRank(new ImmutablePair<>(null, null))));
		for (int i = 2; i < NUMBER_OF_INSERTS; i++) {
			int insertAfter = ranks.size() - 1;
			String nextRank = RelationshipRankComputer.computeRank(new ImmutablePair<>(ranks.get(insertAfter), null));
			ranks.add(nextRank);
			//	logStateOfRanks(i, insertAfter, ranks);
			assertThatListIsSorted(ranks);
		}
	}

	@Test
	public void insertAlwaysAtTheHead() throws OutOfRanksException {
		List<String> ranks = new ArrayList<>(List.of(RelationshipRankComputer.computeRank(new ImmutablePair<>(null, null))));
		for (int i = 2; i < NUMBER_OF_INSERTS; i++) {
			String nextRank = RelationshipRankComputer.computeRank(new ImmutablePair<>(null, ranks.get(0)));
			ranks.add(0, nextRank);
			//logStateOfRanks(i, 0, ranks);
			assertThatListIsSorted(ranks);
		}
	}

	@Test
	public void testNewFirstOrder() throws OutOfRanksException {
		Random randomGenerator = new Random();
		List<String> ranks = new ArrayList<>(List.of(RelationshipRankComputer.computeRank(new ImmutablePair<>(null, null))));
		ranks.add(RelationshipRankComputer.computeRank(new ImmutablePair<>(ranks.get(0), null)));
		for (int i = 0; i < NUMBER_OF_INSERTS; i++) {
			int insertAfterIndex = randomGenerator.nextInt(ranks.size() - 1);
			//	LOG.info(String.format("%d insert after %d position, rank size %d", i, insertAfterIndex, ranks.size()));
			if (insertAfterIndex == 0) {
				String newHead = RelationshipRankComputer.computeRank(new ImmutablePair<>(null, ranks.get(0)));
				ranks.add(0, newHead);
			} else {
				Pair<String, String> ranksFound = getRandomRanks(ranks, insertAfterIndex);
				String nextRank = RelationshipRankComputer.computeRank(new ImmutablePair<>(ranksFound.first(), ranksFound.second()));
				ranks.add(insertAfterIndex + 1, nextRank);
			}
			//	logStateOfRanks(i, insertAfterIndex, ranks);
			assertThatListIsSorted(ranks);
		}
	}

	@DataProvider
	public static Object[][] numberSequence() {
		// this magic expression is empirically researched to have increasing spread of pseudorandom (but the same for each test) numbers.
		return LongStream.iterate(0, i -> (i + 3) * 7 - (i * 5) - (i / 3))
			.limit(27)
			.mapToObj(i -> new Object[] { i })
			.toArray(Object[][]::new);
	}

	@Test(dataProvider = "numberSequence")
	public void testSpreadEqually(long size) {
		List<String> result = RelationshipRankComputer.spreadEqually(size);
		log.trace("{}", result);
		assertEquals(result.size(), size);
	}

	private Pair<String, String> getRandomRanks(List<String> ranks, int startIndex) {
		if (ranks.size() > 2) {
			// we are picking 1 or 2 entries from this list therefore we must not consider the whole list
			List<String> subList = ranks.subList(startIndex, startIndex + 2);
			if (subList.size() > 1) {
				return new Pair<>(subList.get(0), subList.get(1));
			} else {
				return new Pair<>(subList.get(0), null);
			}
		} else {
			return new Pair<>(ranks.get(0), null);
		}

	}

	private void assertThatListIsSorted(List<String> ranks) {

		List<String> sortedList = ranks.stream().sorted().toList();
		assertEquals(ranks.size(), sortedList.size());
		for (int i = 0; i < sortedList.size(); i++) {
			if (ranks.get(i).compareTo(sortedList.get(i)) != 0) {
				log.info("Order of ranks:");
				for (String rank : ranks) {
					log.info(rank);
				}
				log.info("is different from ordered ranks:");
				for (String rank : sortedList) {
					log.info(rank);
				}
				Assert.fail("Wrong sort order created");
			}
		}
	}

	private void logStateOfRanks(int insertCount, int insertAfter, List<String> ranks) {
		log.info(String.format("Ranks after %d insert after %d:", insertCount, insertAfter));
		for (int i = 0; i < ranks.size(); i++) {
			log.info(String.format("%s [%d]", ranks.get(i), i));
		}
	}
}
