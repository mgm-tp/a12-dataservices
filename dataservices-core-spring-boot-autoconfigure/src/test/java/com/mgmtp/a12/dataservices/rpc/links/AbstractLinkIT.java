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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.testng.annotations.BeforeMethod;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.rpc.OperationError;

import static org.testng.Assert.assertEquals;

public abstract class AbstractLinkIT extends AbstractSpringContextIT {

	@Autowired protected RelationshipLinkRepository relationshipLinkRepository;

	protected DocumentReference partner1DocRef;
	protected DocumentReference partner2DocRef;
	protected DocumentReference partner3DocRef;
	protected DocumentReference partner4DocRef;
	protected DocumentReference partner5DocRef;
	protected DocumentReference partner6DocRef;
	protected DocumentReference partner7DocRef;
	protected DocumentReference partner8DocRef;
	protected DocumentReference partner9DocRef;

	protected DocumentReference contract1DocRef;
	protected DocumentReference contract2DocRef;
	protected DocumentReference contract3DocRef;
	protected DocumentReference contract4DocRef;
	protected DocumentReference contract5DocRef;
	protected DocumentReference contract6DocRef;

	@BeforeMethod
	public void setUp() throws Exception {
		cleanUpTestEnvironment();

		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.CONTRACT_DOCUMENT_MODEL_PATH);
		modelsFunctions.createModel(PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH);
		createModel(resourceFunctions.loadResource(PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH));

		partner1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner4DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner5DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner6DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner7DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner8DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");
		partner9DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartnerSuper-1.json");

		contract1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		contract2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		contract3DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		contract4DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		contract5DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
		contract6DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Contract-1.json");
	}

	protected OperationError createErrorTemplate(String keyPrefix, String shortMessage, String longMessage, String operationId) {
		LocalizedEntry shortMessageObject = new LocalizedEntry(keyPrefix + ".title", shortMessage);
		LocalizedEntry longMessageObject = new LocalizedEntry(keyPrefix + ".description", longMessage);

		OperationError.OperationErrorBuilder builder = OperationError.builder()
			.operationId(operationId)
			.level(ErrorLevel.ERROR)
			.shortMessage(shortMessageObject)
			.longMessage(longMessageObject)
			.errorDetail(ErrorDetail.createGenericError());
		return builder.build();
	}

	protected void checkEntityRelation(DocumentReference sourceEntityDocRef, String sourceRole, int count) {
		assertEquals(
			relationshipLinkRepository.findByRelationshipModelNameAndSource(
				RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
				sourceRole,
				sourceEntityDocRef,
				OffsetBasedPageRequest.unpaged(Sort.by(RelationshipSortConstants.ID_FIELD_NAME))
			).getContent().size(),
			count
		);
	}

	protected void assertExceptions(OperationError expectedError, OperationError actualError) {
		assertEquals(actualError.getShortMessage().getKey(), expectedError.getShortMessage().getKey());
		assertEquals(actualError.getShortMessage().getDefaultMessage(), expectedError.getShortMessage().getDefaultMessage());
		assertEquals(actualError.getLongMessage().getKey(), expectedError.getLongMessage().getKey());
		assertEquals(actualError.getLongMessage().getDefaultMessage(), expectedError.getLongMessage().getDefaultMessage());
	}

	public static QueryRoot constructQueryLink(String sourceModel, DocumentReference sourceDocRef, String targetRole) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(sourceModel)
			.constraint(ExactMatchOperator.builder()
				.field(DocumentMetadataConstants.DOCREF_METADATA_PATH)
				.value(sourceDocRef)
				.build())
			.exclude(true)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(100)
				.build())
			.links(List.of(QueryLink.builder()
				.relationshipModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL)
				.targetRole(targetRole)
				.build()))
			.build();
	}
}
