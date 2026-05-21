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
package com.mgmtp.a12.dataservices.document;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.DocumentFunctions;
import com.mgmtp.a12.dataservices.ModelsFunctions;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipRole;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentGraphServiceIT extends AbstractSpringContextIT {

	@Autowired ModelsFunctions modelsFunctions;
	@Autowired protected DocumentFunctions documentFunctions;
	@Autowired DocumentGraphService documentGraphService;
	@Autowired ObjectMapper objectMapper;

	private DocumentReference contractDocRef;
	private DocumentReference businessPartnerDocRef;
	private DocumentReference coInsuredDocRef;
	private DocumentReference address1DocRef;
	private DocumentReference address2DocRef;

	private RelationshipLink partnerLink;
	private RelationshipLink coInsuredLink;
	private RelationshipLink addressLink1;
	private RelationshipLink addressLink2;
	private RelationshipLink postalAddressLink1;
	private DocumentReference coInsuredAdditionalFieldDocRef;

	@SneakyThrows
	@Override protected void initializeWithSecurityBypass() {
		// Models of insurance domain
		modelsFunctions.createModels(
			PathConstants.ADDRESS_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH,
			PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH,
			PathConstants.PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH
		);
		modelsFunctions.saveCdms(
			PathConstants.CONTRACT_CDM_MODEL_PATH,
			PathConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL_PATH
		);

		// create documents needed for making relationships 
		contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, PathConstants.CDD_PATH + "Contract-1.json");
		businessPartnerDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartner-1.json");
		coInsuredDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "BusinessPartner-2.json");
		address1DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Address-1.json");
		address2DocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Address-2.json");

		coInsuredAdditionalFieldDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.COINSURED_ADDITIONAL_FIELDS_MODEL,PathConstants.DOCUMENTS_PATH + "CoInsuredAdditionalFields-1.json" );

		// Make relationships for contract, business partners, and addresses
		partnerLink = makeRelationshipLink(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", businessPartnerDocRef, null),
			new DataServicesRelationshipRole("Contract", contractDocRef, null)), null);
		coInsuredLink = makeRelationshipLink(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", coInsuredDocRef, null),
			new DataServicesRelationshipRole("Contract", contractDocRef, null)), coInsuredAdditionalFieldDocRef);
		addressLink1 = makeRelationshipLink(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", businessPartnerDocRef, null),
			new DataServicesRelationshipRole("Address", address1DocRef, null)), null);
		addressLink2 = makeRelationshipLink(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", businessPartnerDocRef, null),
			new DataServicesRelationshipRole("Address", address2DocRef, null)), null);
		makeRelationshipLink(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", coInsuredDocRef, null),
			new DataServicesRelationshipRole("Address", address2DocRef, null)), null);
		postalAddressLink1 = makeRelationshipLink(RelationshipModelConstants.PARTNER_POSTAL_ADDRESS_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", businessPartnerDocRef, null),
			new DataServicesRelationshipRole("Address", address1DocRef, null)), null);
		makeRelationshipLink(RelationshipModelConstants.PARTNER_POSTAL_ADDRESS_MODEL, List.of(
			new DataServicesRelationshipRole("Partner", coInsuredDocRef, null),
			new DataServicesRelationshipRole("Address", address2DocRef, null)), null);
	}

	@Test public void testDocumentGraph() throws IOException {
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(partnerLink, coInsuredLink, addressLink1, addressLink2, postalAddressLink1),
				List.of(contractDocRef, businessPartnerDocRef, coInsuredDocRef, address1DocRef, address2DocRef, coInsuredAdditionalFieldDocRef)),
			objectMapper.writeValueAsString(documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, contractDocRef, null)),
			false);
	}

	@Test public void testDocumentSubGraph() throws IOException {
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(addressLink1, addressLink2, postalAddressLink1),
				List.of(businessPartnerDocRef, address1DocRef, address2DocRef)),
			objectMapper.writeValueAsString(documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, businessPartnerDocRef, "/ContractBusinessPartner")),
			false);
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(),
				List.of(address1DocRef)),
			objectMapper.writeValueAsString(
				documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, address1DocRef, "/ContractBusinessPartner/PartnerAddresses")),
			false);
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(),
				List.of(address2DocRef)),
			objectMapper.writeValueAsString(
				documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, address2DocRef, "/ContractBusinessPartner/PartnerAddresses")),
			false);
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(),
				List.of(address1DocRef)),
			objectMapper.writeValueAsString(
				documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, address1DocRef, "/ContractBusinessPartner/PartnerPostalAddress")),
			false);
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(),
				List.of(coInsuredDocRef)),
			objectMapper.writeValueAsString(documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, coInsuredDocRef, "/ContractCoInsuredPartner")),
			false);
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(partnerLink, coInsuredLink, addressLink1, addressLink2, postalAddressLink1),
				List.of(contractDocRef, businessPartnerDocRef, coInsuredDocRef, address1DocRef, address2DocRef, coInsuredAdditionalFieldDocRef)),
			objectMapper.writeValueAsString(documentGraphService.constructDocumentGraph(DocumentModelConstants.CONTRACT_CDM_MODEL, contractDocRef, "/")),
			false);
	}

	@Test public void testDocumentGraphAnonymized() throws IOException {
		JSONAssert.assertEquals(makeDocumentGraphExpectation(List.of(), List.of(businessPartnerDocRef)),
			objectMapper.writeValueAsString(documentGraphService.constructDocumentGraph(DocumentModelConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL, businessPartnerDocRef, null)),
			false);
	}

	private String makeDocumentGraphExpectation(Collection<RelationshipLink> relationshipLinks, Collection<DocumentReference> documentReferences) {
		return String.format("{"
				+ "  \"links\": [%s],"
				+ "  \"documents\": [%s]"
				+ "}",
			// ContractCDM does not include addresses of co-insured business partners
			relationshipLinks.stream()
				.map(this::makeLinkPart)
				.collect(Collectors.joining(",")),
			documentReferences.stream()
				.map(d -> documentService.load(d)
					.orElseThrow(() -> new NotFoundException(ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY, String.format("Document [%s] not found", d.getDocumentId()))))
				.map(documentSupport::convertToDocumentSpec)
				.map(this::makeDocumentPart)
				.collect(Collectors.joining(",")));
	}

	private String makeDocumentPart(DocumentSpec d) {
		return String.format("{"
			+ "            \"docRef\": \"%s\","
			+ "            \"documentModelName\": \"%s\","
			+ "            \"document\": %s"
			+ "        }", d.getDocRef(), d.getDocumentModelName(), d.getDocument());
	}

	private String makeLinkPart(RelationshipLink link) {
		return String.format("{"
			+ "      \"linkId\": \"%s\","
			+ "      \"linkDescriptor\": {"
			+ "        \"relationshipModelName\": \"%s\","
			+ "        \"entities\": [%s]"
			+ "      },"
			+ makeLinkDocRef(link.getLinkDocumentDocRef())
			+ "    }", link.getId(), link.getRelationshipModel(), makeLinkRoleParts(link));
	}

	private String makeLinkDocRef(DocumentReference linkDocRef) {
		return Objects.isNull(linkDocRef) ? "\"linkDocRef\": null" :
			String.format(
				"\"linkDocRef\": \"%s\""
				, linkDocRef);
	}

	private String makeLinkRoleParts(RelationshipLink link) {
		return link.getRoles().values().stream()
			.map(this::makeLinkRolePart)
			.collect(Collectors.joining(","));
	}

	private String makeLinkRolePart(RelationshipRole r) {
		return String.format("{"
			+ "            \"role\": \"%s\","
			+ "            \"modelName\": \"%s\","
			+ "            \"docRef\": \"%s\""
			+ "          }", r.getName(), r.getDocRef().getDocumentModelName(), r.getDocRef());
	}
}
