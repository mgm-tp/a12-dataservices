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
package com.mgmtp.a12.dataservices.relationship.operation.heterogeneity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;

public class RpcHeterogeneousOperationsIT extends AbstractSpringContextIT {

	private static final String SOURCE_DOC_REF_PLACEHOLDER = "&sourceDocRef";
	private static final String SOURCE_ROLE_PLACEHOLDER = "&sourceRole";
	private static final String SOURCE_MODEL_PLACEHOLDER = "&sourceModel";
	private static final String ADD_GROUP_1_OPERATION = "addGroup1";
	private static final String ADD_RULE_OPERATION = "addRule";
	private static final String CHILD_ROLE = "child";
	private static final String PARENT_ROLE = "parent";
	private static final String ADD_NUMBER_FIELD_1_OPERATION = "addNumberField1";
	private static final String ADD_PREFIX = "add";
	private static final String ADD_COLORFUL_BLUE_GROUP_OPERATION = "addColorfulBlueGroup";
	private static final String DOMAIN_GROUP_MODEL = "DomainGroup";
	private static final String DOMAIN_ELEMENT_MODEL = "DomainElement";

	private Map<String, DocumentReference> prepareDataResult;

	/**
	 * --> Element (Abstract)
	 * |   /  |  \
	 * |  /   |   \
	 * | /    |    \
	 * Group   Field  Rule
	 * |
	 * |
	 * ColorfulGroup
	 *
	 * addGroup1#docRef (DomainGroup)
	 * addGroup2#docRef (DomainGroup)
	 * addColorfulBlueGroup#docRef (DomainColorfulGroup)
	 * addColorfulYellow#docRef (DomainColorfulGroup)
	 * addRule0#docRef (DomainRule)
	 * addNumberField1#docRef (DomainField)
	 * addNumberField2#docRef (DomainField)
	 * addTimeField#docRef (DomainField)
	 * addRule#docRef (DomainRule)
	 *
	 * || parent                     ||            child            ||
	 * | addGroup1#docRef            | addNumberField1#docRef       |
	 * | addGroup1#docRef            | addRule.docRef               |
	 * | addColorfulBlueGroup#docRef | addRule.docRef               |
	 */
	@BeforeClass
	void prepareData() throws Exception {
		setUserTo(UserConstants.ADMIN_USER);
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_HETEROGENEITY_PATH + "DomainElement.json");
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_HETEROGENEITY_PATH + "DomainGroup.json");
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_HETEROGENEITY_PATH + "DomainColorfulGroup.json");
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_HETEROGENEITY_PATH + "DomainField.json");
		modelsFunctions.createModel(PathConstants.DOCUMENT_MODEL_HETEROGENEITY_PATH + "DomainRule.json");

		createModel(resourceFunctions.loadResource(PathConstants.RELATIONSHIP_MODEL_HETEROGENEITY_PATH + "GroupElement.json"));

		prepareDataResult = handleErrors(sendRpcRequest(loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "heterogeneity/init_request.json"))).stream()
			// Only add document operations starts with prefix add
			.filter(response -> response.getId().startsWith(ADD_PREFIX))
			.collect(Collectors.toMap(JsonRpc2Response::getId, this::convertResponse));
	}

	@Test
	void listLinksHeterogeneous() throws Exception {
		String listLinksTemplate = loadResourceFromClasspathAsString(PathConstants.RPC_PATH + "heterogeneity/list_links.json");
		assertLink(listLinksTemplate, ADD_GROUP_1_OPERATION, DOMAIN_GROUP_MODEL, CHILD_ROLE, ADD_NUMBER_FIELD_1_OPERATION, ADD_RULE_OPERATION);
		//Other side of the link test
		assertLink(listLinksTemplate, ADD_RULE_OPERATION, DOMAIN_ELEMENT_MODEL, PARENT_ROLE, ADD_GROUP_1_OPERATION, ADD_COLORFUL_BLUE_GROUP_OPERATION);
	}

	private void assertLink(String listLinksTemplate, String addDocOperationIdForSourceDocRef, String sourceModel, String targetRole,
		String addDocOperationIdForResult1, String addDocOperationIdForResult2) throws IOException {
		JsonRpc2Response response = sendRpcRequest(prepareRequest(listLinksTemplate, addDocOperationIdForSourceDocRef, sourceModel, targetRole)).get(0);
		List<DocumentTreeResult> linkResults = new ArrayList<>();
		response.getResult().get("links").forEach(e -> linkResults.add(convertObject(e, DocumentTreeResult.class)));
		Assert.assertEquals(linkResults.size(), 2);
		assertDocRef(linkResults, addDocOperationIdForResult1);
		assertDocRef(linkResults, addDocOperationIdForResult2);
	}

	private String prepareRequest(String requestTemplate, String addDocumentOperationId, String sourceModel, String sourceRole) {
		return requestTemplate
			.replace(SOURCE_DOC_REF_PLACEHOLDER, prepareDataResult.get(addDocumentOperationId).toString())
			.replace(SOURCE_ROLE_PLACEHOLDER, sourceRole)
			.replace(SOURCE_MODEL_PLACEHOLDER, sourceModel);
	}

	private void assertDocRef(List<DocumentTreeResult> listLinksResult, String operationId) {
		Optional<DocumentTreeResult> targetDocument = listLinksResult.stream()
			.filter(l -> prepareDataResult.get(operationId).equals(l.getTargetDocRef()))
			.findFirst();
		Assert.assertTrue(targetDocument.isPresent());
	}

	private DocumentReference convertResponse(JsonRpc2Response response) {
		try {
			return objectMapper.convertValue(response.getResult().get("docRef"), DocumentReference.class);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot parse response data");
		}
	}

	private <T> T convertObject(JsonNode jsonNode, Class<T> clazz) {
		try {
			return objectMapper.treeToValue(jsonNode, clazz);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot parse response data");
		}
	}
}
