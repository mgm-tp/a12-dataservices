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
package com.mgmtp.a12.examples.document.extension.document;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.operation.internal.QueryOperation;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;
import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

@Import({ AddDocumentIT.TestEventListenerConfig.class })
public class AddDocumentIT extends AbstractITBase {

	@Autowired private AddDocumentOperation addDocumentOperation;
	@Autowired private QueryOperation queryOperation;

	@Test void addDocument_testDocumentBeforeIndexEvent() throws IOException {
		modelsFunctions.createModels("/model/document/" + BUSINESS_PARTNER_DOCUMENT_MODEL_NAME + ".json");

		DocumentV2 document = documentFunctions.getKernelDocumentFromFile(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, "link/BusinessPartnerSuper.json");
		document = document.withFieldValue("/businessPartner/name", "NeedUpdateValue");
		addDocumentOperation.rpc(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME, objectMapper.valueToTree(document), null);

		PagedResultSet<Object> result = queryOperation.rpc(
			QueryRoot.builder()
				.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
				.targetDocumentModel(BUSINESS_PARTNER_DOCUMENT_MODEL_NAME)
				.constraint(ExactMatchOperator.builder()
					.field("/businessPartner/name")
					.value("NewUpdatedValue")
					.build())
				.paging(Paging.builder().pageSize(10).pageNumber(0).build())
				.build()
		);

		Assert.assertEquals(result.getFullSize(), 1);

	}

	@TestConfiguration
	static class TestEventListenerConfig {

		@Component static class DocumentEventListener {
			@Autowired private DataServicesDocumentFactory dataServicesDocumentFactory;

			@CommonDataServicesEventListener
			public void changeDocumentContent(DocumentBeforeIndexEvent documentBeforeIndexEvent) {
				DocumentV2 document = documentBeforeIndexEvent.getDataServicesDocument().getKernelDocument();
				if (BUSINESS_PARTNER_DOCUMENT_MODEL_NAME.equals(documentBeforeIndexEvent.getDataServicesDocument().getMetadata().getDocumentModelReference())
					&& document.fieldValue("/businessPartner/name").equals("NeedUpdateValue")) {
					documentBeforeIndexEvent.setDataServicesDocument(
						dataServicesDocumentFactory.newDataServicesDocument(
							document.withFieldValue("/businessPartner/name", "NewUpdatedValue")
						)
					);
				}
			}
		}
	}

}
