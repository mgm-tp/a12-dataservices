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
package com.mgmtp.a12.dataservices.cdd.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

import static org.testng.Assert.assertEquals;

public abstract class AbstractCdmTest {
	protected static final String CONTRACTSKELETON_FILE = "/model/ContractSkeleton.json";
	protected static final String CONTRACTCDM_FILE = "/model/ContractCDM.json";
	protected void withCdm(String resourceName, Consumer<ComposeDocumentModel> consumer) {
		withDocumentModel(resourceName, dm -> consumer.accept(new ComposeDocumentModel(dm)));
	}

	@SneakyThrows
	private void withDocumentModel(String resourceName, Consumer<IDocumentModel> consumer) {
		try (InputStream r = getClass().getResourceAsStream(resourceName)) {
			IDocumentModel documentModel = deserializeModel(r);
			consumer.accept(documentModel);
		}
	}

	private IDocumentModel deserializeModel(InputStream r) throws IOException {
		return new DocumentModelServiceFactory().createDocumentModelSerializer()
			.deserialize(new InputStreamReader(r));
	}

	protected static CddSkeletonGroup makeCddSkeletonGroup(String relationshipModelName, String sourceRole, String targetRole,
		String targetDocumentModel) {
		return TestCddSkeletonGroup.testBuilder()
			.relationshipModelName(relationshipModelName)
			.sourceRole(sourceRole)
			.targetRole(targetRole)
			.targetDocumentModel(targetDocumentModel)
			.build();
	}

	protected void assertGroups(List<CddSkeletonGroup> groups, List<CddSkeletonGroup> expectations) {
		assertEquals(groups.size(), expectations.size());
		for (int i = 0; i < groups.size(); i++) {
			assertGroups(groups.get(i), expectations.get(i));
		}
	}

	private void assertGroups(CddSkeletonGroup actual, CddSkeletonGroup expected) {
		assertEquals(actual.getSourceRole(), expected.getSourceRole());
		assertEquals(actual.getRelationshipModelName(), expected.getRelationshipModelName());
		assertEquals(actual.getTargetRole(), expected.getTargetRole());
		assertEquals(actual.getTargetDocumentModel(), expected.getTargetDocumentModel());
	}

	@Data @EqualsAndHashCode(callSuper = true) @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(builderMethodName = "testBuilder")
	private static class TestCddSkeletonGroup extends CddSkeletonGroup {
		private String relationshipModelName;
		private String sourceRole;
		private String targetRole;
		private String targetDocumentModel;
	}
}
