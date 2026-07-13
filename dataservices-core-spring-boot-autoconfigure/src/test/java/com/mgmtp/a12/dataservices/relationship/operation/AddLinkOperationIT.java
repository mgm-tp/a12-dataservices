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
package com.mgmtp.a12.dataservices.relationship.operation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants.FieldConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants.RoleConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.Order;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipRole;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.NoAvailableRanksException;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import static com.mgmtp.a12.dataservices.relationship.Order.ASC;
import static com.mgmtp.a12.dataservices.relationship.Order.DESC;

@Slf4j
public class AddLinkOperationIT extends AbstractListITBase {

	public static final int PARTNER_COUNT = 20;

	@Autowired protected RelationshipLinkRepository relationshipLinkRepository;
	@Autowired private RelationshipRankService relationshipRankService;

	// These scenarios represent situation in case DM is missing, then RM is not available
	@DataProvider(name = "ProvideDataForInvalidRelationshipModelTest")
	public Object[][] provideDataForInvalidRelationshipModelTest() {
		return new Object[][] {
			{ INVALID_LINK_DM_RM },
			{ INVALID_FIRST_ROLE_RM },
			{ INVALID_SECOND_ROLE_RM }
		};
	}

	@Test(dataProvider = "ProvideDataForInvalidRelationshipModelTest")
	public void invalidRelationshipModelTest(String relationship) {
		try {
			LinkDescriptor
				linkDescriptor = createLinkDescriptor(relationship, RoleConstants.CONTRACT_ROLE, contractDocRef1, RoleConstants.PARTNER_ROLE, partnerDocRef1);
			addLinkOperation.rpc(linkDescriptor, createLinkDocument());
			Assert.fail("Exception was expected to be thrown");
		} catch (RpcException e) {
			LocalizedEntry shortMessage = e.getOperationError().getShortMessage();
			LocalizedEntry longMessage = e.getOperationError().getLongMessage();

			Assert.assertEquals(shortMessage.getKey(), "error.link.document.validation.title");
			Assert.assertEquals(shortMessage.getDefaultMessage(), "Invalid document references in relationship model");
			Assert.assertEquals(longMessage.getKey(), "error.link.document.validation.description");
			Assert.assertEquals(longMessage.getDefaultMessage(), "Relationship model [%s] contains invalid document references".formatted(relationship));
		}
	}

	@Test
	@Transactional
	public void testAddLink() {
		RelationshipLinkSpec linkSpec = executeAddLinkOperation(contractDocRef5, partnerDocRef4, LinkPosition.TOP);
		String linkID = linkSpec.getId();

		// Verify that linkDocumentDocRef is set in the response
		Assert.assertNotNull(linkSpec.getLinkDescriptor(), "LinkDescriptor should not be null");
		Assert.assertNotNull(linkSpec.getLinkDescriptor().getLinkDocumentDocRef(), "linkDocumentDocRef should be set in the response");

		QueryRoot queryLink = constructQueryLink(contractDocRef5);
		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);
		JsonNode link = result.getLinks().stream().filter(e -> e.getType() == DocumentTreeNodeType.LINK && linkID.equals(e.getLinkId()))
			.map(documentTreeResult -> {
				return objectMapper.readTree(documentTreeResult.getDocument().toString());
			})
			.findFirst().orElse(null);
		Assert.assertNotNull(link);
		JsonNode coInsuredRoot = link.get(FieldConstants.CO_INSURED_ADDITIONAL_FIELDS_ROOT);
		Assert.assertEquals(coInsuredRoot.get("Name").asText(), "Karel");
		Assert.assertEquals(coInsuredRoot.get("ID").asText(), "0000-0000-0000-0000");
	}

	@Test(description = "Should return linkDocumentDocRef in the response after creating a link")
	@Transactional("dsTransactionManager")
	public void shouldReturnLinkDocumentDocRefWhenLinkCreated() {
		// Given - prepare link with link document
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec(RoleConstants.PARTNER_ROLE, partnerDocRef1);
		RelationshipRoleSpec contractRole = new RelationshipRoleSpec(RoleConstants.CONTRACT_ROLE, contractDocRef1);
		LinkDescriptor linkDescriptor = new LinkDescriptor(
			RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
			Arrays.asList(contractRole, partnerRole),
			LinkPosition.TOP
		);
		JsonNode linkDocument = createLinkDocument("TestPartner");

		// When - create link
		RelationshipLinkSpec result = addLinkOperation.rpc(linkDescriptor, linkDocument);

		// Then - verify linkDocumentDocRef is present
		Assert.assertNotNull(result, "RelationshipLinkSpec should not be null");
		Assert.assertNotNull(result.getId(), "Link ID should not be null");
		Assert.assertNotNull(result.getLinkDescriptor(), "LinkDescriptor should not be null");
		Assert.assertNotNull(result.getLinkDescriptor().getLinkDocumentDocRef(),
			"linkDocumentDocRef should be populated in the response");

		// Verify the linkDocumentDocRef is valid
		DocumentReference linkDocRef = result.getLinkDescriptor().getLinkDocumentDocRef();
		Assert.assertNotNull(linkDocRef.getDocumentModelName(), "Document model name should be set");
		Assert.assertNotNull(linkDocRef.getDocumentId(), "Document ID should be set");
	}

	@Test(description = "Should populate modelName on each RelationshipRoleSpec returned from ADD_LINK")
	@Transactional("dsTransactionManager")
	public void shouldPopulateModelNameOnReturnedRolesWhenLinkCreated() {
		// Given - role specs constructed without modelName (mimicking JSON-RPC deserialization via no-arg constructor + setters)
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec();
		partnerRole.setRole(RoleConstants.PARTNER_ROLE);
		partnerRole.setDocRef(partnerDocRef1);
		RelationshipRoleSpec contractRole = new RelationshipRoleSpec();
		contractRole.setRole(RoleConstants.CONTRACT_ROLE);
		contractRole.setDocRef(contractDocRef1);
		LinkDescriptor linkDescriptor = new LinkDescriptor(
			RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
			Arrays.asList(contractRole, partnerRole),
			LinkPosition.TOP
		);

		// When - create link
		RelationshipLinkSpec result = addLinkOperation.rpc(linkDescriptor, createLinkDocument("TestPartner"));

		// Then - modelName should be populated on every returned role spec
		Assert.assertNotNull(result.getLinkDescriptor(), "LinkDescriptor should not be null");
		for (RelationshipRoleSpec entity : result.getLinkDescriptor().getEntities()) {
			Assert.assertNotNull(entity.getModelName(),
				"modelName should be set on returned RelationshipRoleSpec for role=" + entity.getRole());
			Assert.assertEquals(entity.getModelName(), entity.getDocRef().getDocumentModelName(),
				"modelName should match the docRef's document model name for role=" + entity.getRole());
		}
	}

	@Transactional
	@Test(expectedExceptions = NoAvailableRanksException.class)
	public void testAddLinkOutOfRanksTop() throws Exception {
		DocumentReference contractDocRef = documentService.create(
				documentFunctions.getKernelDocumentFromFile(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
					PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-alan.json"), Locale.getDefault())
			.getMetadata().getDocRef();
		List<DocumentReference> businessPartnerDocRefs = prepareLinks(contractDocRef, i -> StringUtils.repeat("!", i + 1));
		String lowerBound = "!";
		String upperBound = StringUtils.repeat("!", PARTNER_COUNT);
		assertEntitiesOrdered(contractDocRef, lowerBound, true, upperBound, businessPartnerDocRefs);
		executeAddLinkOperation(contractDocRef, partnerDocRef1, LinkPosition.TOP);
	}

	@Transactional
	@Test(expectedExceptions = NoAvailableRanksException.class)
	public void testAddLinkOutOfRanksBottom() throws Exception {
		DocumentReference contractDocRef = documentService.create(
				documentFunctions.getKernelDocumentFromFile(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
					PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-alan.json"), Locale.getDefault())
			.getMetadata().getDocRef();
		List<DocumentReference> businessPainterDocRefs = prepareLinks(contractDocRef, i -> StringUtils.repeat("~", 255 - i));
		Collections.reverse(businessPainterDocRefs);

		String lowerBound = StringUtils.repeat("~", 255 - PARTNER_COUNT + 1);
		assertTerminatingOrder(contractDocRef, ASC, lowerBound, true);
		String upperBound = StringUtils.repeat("~", 255);
		assertTerminatingOrder(contractDocRef, DESC, upperBound, true);
		assertLinksOrder(contractDocRef, businessPainterDocRefs);
		executeAddLinkOperation(contractDocRef, partnerDocRef1, LinkPosition.BOTTOM);
	}

	@Transactional
	@Test public void testRefreshRanks() throws IOException {
		ImmutablePair<DocumentReference, List<DocumentReference>> cp = prepareTestDateForRefreshRanks(i -> i + 1);
		assertEntitiesOrdered(cp.left, "!", true, StringUtils.repeat("!", PARTNER_COUNT), cp.right);
		relationshipRankService.refreshRanks(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RoleConstants.CONTRACT_ROLE, cp.left);
		assertEntitiesOrdered(cp.left, "!", false, StringUtils.repeat("!", PARTNER_COUNT), cp.right);
	}

	@Transactional
	@Test public void testRefreshRanksReversed() throws IOException {
		ImmutablePair<DocumentReference, List<DocumentReference>> cp = prepareTestDateForRefreshRanks(i -> PARTNER_COUNT - i);
		Collections.reverse(cp.right);
		assertEntitiesOrdered(cp.left, "!", true, StringUtils.repeat("!", PARTNER_COUNT), cp.right);
		relationshipRankService.refreshRanks(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, RoleConstants.CONTRACT_ROLE, cp.left);
		assertEntitiesOrdered(cp.left, "!", false, StringUtils.repeat("!", PARTNER_COUNT), cp.right);
	}

	private ImmutablePair<DocumentReference, List<DocumentReference>> prepareTestDateForRefreshRanks(UnaryOperator<Integer> count) throws IOException {
		DocumentReference contractDocRef = documentService.create(
				documentFunctions.getKernelDocumentFromFile(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
					PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-alan.json"), Locale.getDefault())
			.getMetadata().getDocRef();
		List<DocumentReference> partnerDocRefs = prepareLinks(contractDocRef, i -> StringUtils.repeat("!", count.apply(i)));
		return new ImmutablePair<>(contractDocRef, partnerDocRefs);
	}

	private void assertEntitiesOrdered(DocumentReference contractDocRef, String expectedOrder, boolean equals, String roleOrderValue,
		List<DocumentReference> partnerDocRefs) {
		assertTerminatingOrder(contractDocRef, ASC, expectedOrder, equals);
		assertTerminatingOrder(contractDocRef, DESC, roleOrderValue, equals);
		assertLinksOrder(contractDocRef, partnerDocRefs);
	}

	private void assertTerminatingOrder(DocumentReference docRef, Order direction, String expectedOrder, boolean equals) {
		Optional<? extends RelationshipLink> r =
			relationshipLinkRepository.findByRelationshipModelNameAndSource(
					RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL
					, RoleConstants.CONTRACT_ROLE,
					docRef,
					OffsetBasedPageRequest.ofOffset(0, 1,
						Sort.by(Arrays.stream(RelationshipSortConstants.ORDER_BY_ROLES_SORT_COLUMNS).map(direction::getOrder).toList()))
				).stream()
				.findFirst();

		Assert.assertTrue(r.isPresent());
		String order = r
			.map(RelationshipLink::getRoles)
			.map(e -> e.get(RoleConstants.CONTRACT_ROLE))
			.map(RelationshipRole::getOrder)
			.orElse(null);
		Assert.assertEquals(Objects.equals(order, expectedOrder), equals,
			"%s should %s to %s but it is not true.".formatted(order, equals ? "equal" : "not equal", expectedOrder));
	}

	private void assertLinksOrder(DocumentReference contractDocRef, List<DocumentReference> businessPartnerDocRefs) {
		QueryRoot queryLink = constructQueryLink(contractDocRef);

		PagedResultSet<DocumentTreeResult> result = queryOperation.rpc(queryLink);

		Assert.assertEquals(result.getFullSize(), PARTNER_COUNT);
		Assert.assertEquals(result.getLinks().stream()
			.filter(l -> l.getType() == DocumentTreeNodeType.CHILD)
			.count(), PARTNER_COUNT);

		List<DocumentReference> actual = result.getLinks().stream()
			.filter(l -> l.getType() == DocumentTreeNodeType.CHILD)
			.map(DocumentTreeResult::getDocRef)
			.toList();

		Assert.assertEquals(actual, businessPartnerDocRefs);
	}

	private List<DocumentReference> prepareLinks(DocumentReference contractDocRef, Function<Integer, String> orderFunction) {
		return IntStream.range(0, PARTNER_COUNT)
			.mapToObj(i -> createPartnerLinkedToContract(orderFunction.apply(i), contractDocRef))
			.map(RelationshipLink::getRoles)
			.map(e -> e.get(RoleConstants.PARTNER_ROLE))
			.map(RelationshipRole::getDocRef)
			.collect(Collectors.toList());
	}

	@SneakyThrows
	RelationshipLink createPartnerLinkedToContract(String order, DocumentReference contractDocRef) {
		String partnerDocumentPath = PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-1.json";
		DocumentReference businessPartnerDocRef = documentService.create(
				documentFunctions.getKernelDocumentFromFile(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, partnerDocumentPath), Locale.getDefault())
			.getMetadata().getDocRef();
		RelationshipLink r = new DataServicesRelationshipLink(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, null);
		r.addRole(new DataServicesRelationshipRole(RoleConstants.PARTNER_ROLE, businessPartnerDocRef, order));
		r.addRole(new DataServicesRelationshipRole(RoleConstants.CONTRACT_ROLE, contractDocRef, order));
		return relationshipLinkRepository.create(r);
	}

	private static QueryRoot constructQueryLink(DocumentReference sourceDocRef) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
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
				.targetRole(RoleConstants.PARTNER_ROLE)
				.build()))
			.build();
	}

}
