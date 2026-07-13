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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.internal.AbstractCdmTest;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;

import static org.testng.Assert.assertEquals;

public class ComposeDocumentModelUtilsTest extends AbstractCdmTest {

	@Test public void testGetCdmSkeletonGroups() {
		withCdm(CONTRACTCDM_FILE, cdm -> {
			Stream<CddSkeletonGroup> groups = ComposeDocumentModelUtils.getCdmSkeletonGroups(cdm);
			List<CddSkeletonGroup> expectations =
				List.of(makeCddSkeletonGroup("PolicyHolder", "contract", "businessPartner", "BusinessPartner-document"),
					makeCddSkeletonGroup("Location", "businessPartner", "address", "Address-document"),
					makeCddSkeletonGroup("PostAddress", "businessPartner", "address", "Address-document"),
					makeCddSkeletonGroup("CoInsurer", "contract", "businessPartner", "BusinessPartner-document"),
					makeCddSkeletonGroup("Location", "businessPartner", "address", "Address-document"),
					makeCddSkeletonGroup("PostAddress", "businessPartner", "address", "Address-document"));
			assertGroups(groups.toList(), expectations);
		});
	}

	@Test public void testGetAllGroups() {
		List<? extends Pair<String, String>> expected = List.of(new ImmutablePair<>("RootGroup", "RootGroup"),
			new ImmutablePair<>("group_8f4b1", DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME),
			new ImmutablePair<>("group_4c9a1", DocumentMetadataConstants.EXTENSIONS_METADATA_NAME),
			new ImmutablePair<>("group_f8c0d", "r_policyHolder"),
			new ImmutablePair<>("group_05abe", "r_location"),
			new ImmutablePair<>("group_60e44", "address"),
			new ImmutablePair<>("group_6406a", "r_postAddress"),
			new ImmutablePair<>("group_60e44", "address"),
			new ImmutablePair<>("group_39e86", "businessPartner"),
			new ImmutablePair<>("group_d1772", "logo"),
			new ImmutablePair<>("group_f46c7", "notes"),
			new ImmutablePair<>("group_051b0", "r_coInsurer"),
			new ImmutablePair<>("group_b71d2", "additionalFields"),
			new ImmutablePair<>("group_8f138", "r_location"),
			new ImmutablePair<>("group_60e44", "address"),
			new ImmutablePair<>("group_9be25", "r_postAddress"),
			new ImmutablePair<>("group_60e44", "address"),
			new ImmutablePair<>("group_39e86", "businessPartner"),
			new ImmutablePair<>("group_d1772", "logo"),
			new ImmutablePair<>("group_f46c7", "notes"),
			new ImmutablePair<>("group_9ee41", "cdm"),
			new ImmutablePair<>("group_41512", "contract"),
			new ImmutablePair<>("group_b8a72", "contractAdditionalFields"));
		withCdm(CONTRACTCDM_FILE, cdm -> {
			List<ImmutablePair<String, String>> groups = ComposeDocumentModelUtils.getAllGroups(cdm.getContent().getDocumentModelRoot())
				.map(g -> new ImmutablePair<>(g.getId(), g.getName()))
				.toList();
			assertEquals(groups, expected);
		});
	}
}
