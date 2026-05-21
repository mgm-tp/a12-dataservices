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
package com.mgmtp.a12.dataservices.rpc.links;

import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RpcRelinkDocumentOrderIT extends RpcOrderedLinkIT {
	private String firstLink;
	private String secondLink;
	private String thirdLink;
	private String fourthLink;
	private String fifthLink;

	@Override
	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		firstLink = addLinkWithPositionAndPredecessor(contract1DocRef, "BOTTOM", "");
		secondLink = addLinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "");
		thirdLink = addLinkWithPositionAndPredecessor(contract3DocRef, "BOTTOM", "");
		fourthLink = addLinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "");
		fifthLink = addLinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", "");
	}

	@AfterMethod
	public void tearDown() {
		relationshipLinkJpaRepository.deleteAll();
	}

	@Test
	public void relinkWithPositionBottom_shouldPutItAtTheEnd() throws IOException {
		String newFirstLink = relinkWithPositionAndPredecessor(contract1DocRef, "BOTTOM", "", firstLink);
		assertQueryIsOrdered(secondLink, thirdLink, fourthLink, fifthLink, newFirstLink);

		String newSecondLink = relinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "", secondLink);
		assertQueryIsOrdered(thirdLink, fourthLink, fifthLink, newFirstLink, newSecondLink);

		String newThirdLink = relinkWithPositionAndPredecessor(contract3DocRef, "BOTTOM", "", thirdLink);
		assertQueryIsOrdered(fourthLink, fifthLink, newFirstLink, newSecondLink, newThirdLink);

		String newFourthLink = relinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "", fourthLink);
		assertQueryIsOrdered(fifthLink, newFirstLink, newSecondLink, newThirdLink, newFourthLink);

		String newFifthLink = relinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", "", fifthLink);
		assertQueryIsOrdered(newFirstLink, newSecondLink, newThirdLink, newFourthLink, newFifthLink);
	}

	@Test
	public void relinkWithPositionTop_shouldPutItAtTheBeginning() throws IOException {
		String newFirstLink = relinkWithPositionAndPredecessor(contract1DocRef, "TOP", "", firstLink);
		assertQueryIsOrdered(newFirstLink, secondLink, thirdLink, fourthLink, fifthLink);

		String newSecondLink = relinkWithPositionAndPredecessor(contract2DocRef, "TOP", "", secondLink);
		assertQueryIsOrdered(newSecondLink, newFirstLink, thirdLink, fourthLink, fifthLink);

		String newThirdLink = relinkWithPositionAndPredecessor(contract3DocRef, "TOP", "", thirdLink);
		assertQueryIsOrdered(newThirdLink, newSecondLink, newFirstLink, fourthLink, fifthLink);

		String newFourthLink = relinkWithPositionAndPredecessor(contract4DocRef, "TOP", "", fourthLink);
		assertQueryIsOrdered(newFourthLink, newThirdLink, newSecondLink, newFirstLink, fifthLink);

		String newFifthLink = relinkWithPositionAndPredecessor(contract5DocRef, "TOP", "", fifthLink);
		assertQueryIsOrdered(newFifthLink, newFourthLink, newThirdLink, newSecondLink, newFirstLink);
	}

	@Test
	public void relinkWithoutPositionNorPredecessor_shouldPutItAtTheBeginning() throws IOException {
		String newFirstLink = relinkWithoutPositionNorPredecessor(contract1DocRef, firstLink);
		assertQueryIsOrdered(newFirstLink, secondLink, thirdLink, fourthLink, fifthLink);

		String newSecondLink = relinkWithoutPositionNorPredecessor(contract2DocRef, secondLink);
		assertQueryIsOrdered(newSecondLink, newFirstLink, thirdLink, fourthLink, fifthLink);

		String newThirdLink = relinkWithoutPositionNorPredecessor(contract3DocRef, thirdLink);
		assertQueryIsOrdered(newThirdLink, newSecondLink, newFirstLink, fourthLink, fifthLink);

		String newFourthLink = relinkWithoutPositionNorPredecessor(contract4DocRef, fourthLink);
		assertQueryIsOrdered(newFourthLink, newThirdLink, newSecondLink, newFirstLink, fifthLink);

		String newFifthLink = relinkWithoutPositionNorPredecessor(contract5DocRef, fifthLink);
		assertQueryIsOrdered(newFifthLink, newFourthLink, newThirdLink, newSecondLink, newFirstLink);
	}

	@Test
	public void relinkWithPredecessor_shouldIgnorePosition() throws IOException {
		String newFirstLink = relinkWithPositionAndPredecessor(contract1DocRef, "BOTTOM", secondLink, firstLink);
		assertQueryIsOrdered(secondLink, newFirstLink, thirdLink, fourthLink, fifthLink);

		String newSecondLink = relinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", fourthLink, secondLink);
		assertQueryIsOrdered(newFirstLink, thirdLink, fourthLink, newSecondLink, fifthLink);

		String newThirdLink = relinkWithPositionAndPredecessor(contract3DocRef, "BOTTOM", fifthLink, thirdLink);
		assertQueryIsOrdered(newFirstLink, fourthLink, newSecondLink, fifthLink, newThirdLink);

		String newFourthLink = relinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", newSecondLink, fourthLink);
		assertQueryIsOrdered(newFirstLink, newSecondLink, newFourthLink, fifthLink, newThirdLink);

		String newFifthLink = relinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", newFirstLink, fifthLink);
		assertQueryIsOrdered(newFirstLink, newFifthLink, newSecondLink, newFourthLink, newThirdLink);
	}

	@Test
	public void mixPositionsAndPredecessor_shouldOrderAccordingly() throws IOException {
		String newFirstLink = relinkWithPositionAndPredecessor(contract1DocRef, "TOP", "", firstLink);
		assertQueryIsOrdered(newFirstLink, secondLink, thirdLink, fourthLink, fifthLink);

		String newSecondLink = relinkWithPositionAndPredecessor(contract2DocRef, "BOTTOM", "", secondLink);
		assertQueryIsOrdered(newFirstLink, thirdLink, fourthLink, fifthLink, newSecondLink);

		String newThirdLink = relinkWithPositionAndPredecessor(contract3DocRef, "TOP", "", thirdLink);
		assertQueryIsOrdered(newThirdLink, newFirstLink, fourthLink, fifthLink, newSecondLink);

		String newFourthLink = relinkWithPositionAndPredecessor(contract4DocRef, "BOTTOM", "", fourthLink);
		assertQueryIsOrdered(newThirdLink, newFirstLink, fifthLink, newSecondLink, newFourthLink);

		String newFifthLink = relinkWithPositionAndPredecessor(contract5DocRef, "BOTTOM", "", fifthLink);
		assertQueryIsOrdered(newThirdLink, newFirstLink, newSecondLink, newFourthLink, newFifthLink);
	}
}
