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
package com.mgmtp.a12.dataservices.internal.query.fields;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mgmtp.a12.dataservices.AbstractQueryContextAwareTest;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;

import lombok.SneakyThrows;

import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_MODEL_ROOT_DIR;
import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_TREE_RESULT_PATH;
import static com.mgmtp.a12.dataservices.constants.PathConstants.JSON_EXT;

public abstract class AbstractProjectionTest extends AbstractQueryContextAwareTest {
	protected final DocumentModelServiceFactory documentModelServiceFactory = new DocumentModelServiceFactory();
	protected final IDocumentModelSerializer documentModelSerializer = documentModelServiceFactory.createDocumentModelSerializer();

	protected final DocumentModelUtils documentModelUtils = new DocumentModelUtils(documentModelServiceFactory, documentModelSerializer, headerParser);
	protected final DocumentTreeHelper documentTreeHelper =
		new DocumentTreeHelper(documentFactory, jsonMapper, documentModelUtils, documentModelServiceFactory);

	protected QueryRoot constructQueryRootWithFields(List<String> fields, ILogicOperator operator) {
		return QueryRoot.builder()
			.fields(fields)
			.targetDocumentModel(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL)
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.constraint(operator)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.build();
	}

	@SneakyThrows public DocumentTreeResult loadDocumentTreeResult(final String documentName) {
		String path = DOCUMENT_TREE_RESULT_PATH + documentName + JSON_EXT;
		try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
			return jsonMapper.readValue(resourceAsStream, DocumentTreeResult.class);
		}
	}

	@SneakyThrows public IDocumentModel loadDocumentModel(final String modelName) {
		String path = DOCUMENT_MODEL_ROOT_DIR + modelName + JSON_EXT;
		try (InputStream resourceAsStream = this.getClass().getResourceAsStream(path)) {
			return documentModelSerializer.deserialize(new InputStreamReader(resourceAsStream));
		}
	}

	public Page<DocumentTreeResult> getCddRootDocuments() throws JsonProcessingException {
		return new PageImpl<>(List.of(DocumentTreeResult.builder()
			.docRef(new DocumentReference("Contract/1"))
			.type(DocumentTreeNodeType.ROOT)
			.document(JsonMapper.builder().build().readTree("""
				[
					["/ContractRoot/ContractName", [1, 1], "Insurance"],
					["/ContractRoot/LengthOfContract", [1, 1], "1"],
					["/ContractRoot/ContractValue", [1, 1], "10"],
					["/ContractRoot/CostToCustomer", [1, 1], "1"],
					["/ContractRoot/MaxDiscount", [1, 1], "1%"],
					["/__meta/creator", [1, 1], "admin"],
					["/__meta/modifier", [1, 1], "admin"],
					["/__meta/createdAt", [1, 1], "2024-10-24T14:43:05.197"],
					["/__meta/modifiedAt", [1, 1], "2024-10-24T14:43:05.197"],
					["/__meta/modelVersion", [1, 1], null]
				]
				"""))
			.linkId("-1")
			.build()));
	}
}
