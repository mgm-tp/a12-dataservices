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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkSpecification;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.factory.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ORDER_BY_ROLES_SORT_COLUMNS;
import static org.mockito.Mockito.doReturn;

public class DefaultRelationshipLinkServiceTest extends AbstractDataServicesCoreTest {
	private @Mock RelationshipLinkRepository repository;
	private @Mock RelationshipLinkFactory relationshipLinkFactory;
	private DataServicesCoreProperties dataServicesCoreProperties = Mockito.spy(DataServicesCoreProperties.class);

	private @Mock DocumentService documentService;

	private @InjectMocks DefaultRelationshipLinkService defaultRelationshipLinkService;

	@BeforeMethod
	public void clearData() throws IllegalAccessException {
		FieldUtils.writeField(defaultRelationshipLinkService, "properties", dataServicesCoreProperties, true);
		Mockito.reset(repository, eventPublisher, relationshipModelLoader);
	}

	@Test void testCreate_successfully() {
		LinkDescriptor mockedLinkDescriptor = mockLinkDescriptor();
		RelationshipLink mockedRelationshipLink1 = mockRelationshipLink(null);
		RelationshipLink mockedRelationshipLink2 = mockRelationshipLink(RandomStringUtils.secure().nextAlphanumeric(5));
		RelationshipModel relationshipModel = mockRelationshipModel(makeTestModelHeader());

		Mockito.when(relationshipLinkFactory.createLink(mockedLinkDescriptor, null)).thenReturn(mockedRelationshipLink1);
		Mockito.when(repository.create(mockedRelationshipLink1)).thenReturn(mockedRelationshipLink2);
		doReturn(relationshipModel).when(relationshipModelLoader).loadModel(mockedRelationshipLink2.getRelationshipModel());

		RelationshipLink result = defaultRelationshipLinkService.create(mockedLinkDescriptor);

		ArgumentCaptor<RelationshipLinkAfterCreateEvent> afterCreateEventArgumentCaptor =
			ArgumentCaptor.forClass(RelationshipLinkAfterCreateEvent.class);
		Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(afterCreateEventArgumentCaptor.capture());
		Assert.assertEquals(afterCreateEventArgumentCaptor.getValue().getLink(), mockedRelationshipLink2);
		Assert.assertEquals(afterCreateEventArgumentCaptor.getValue().getLink().getId(), mockedRelationshipLink2.getId());
		Assert.assertEquals(afterCreateEventArgumentCaptor.getValue().getLink().getLinkDocumentDocRef(), mockedRelationshipLink2.getLinkDocumentDocRef());
		Assert.assertEquals(afterCreateEventArgumentCaptor.getValue().getRelationshipModel(), relationshipModel);

		Assert.assertEquals(result, mockedRelationshipLink2);
		Assert.assertEquals(result.getId(), mockedRelationshipLink2.getId());
		Assert.assertEquals(result.getLinkDocumentDocRef(), mockedRelationshipLink2.getLinkDocumentDocRef());
	}

	@Test void testLoad_successfully() throws IOException {
		RelationshipLinkSpecification relationshipLinkSpecification = new RelationshipLinkSpecification(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		Pageable pageable = PageRequest.of(1, 20, Sort.by(ORDER_BY_ROLES_SORT_COLUMNS));
		RelationshipModel mockRelationshipModel = relationshipModelResolver.getRelationshipModelById(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		RelationshipLinkEntity mockedRelationshipLinkEntity = Mockito.mock(RelationshipLinkEntity.class);
		Page<RelationshipLinkEntity> mockedResult = new PageImpl<>(List.of(mockedRelationshipLinkEntity), pageable, 10);

		doReturn(mockRelationshipModel).when(relationshipModelLoader).loadModel(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		doReturn(mockedResult).when(repository).findByRelationshipModelName(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL, pageable);

		Page<? extends RelationshipLink> result = defaultRelationshipLinkService.load(relationshipLinkSpecification, pageable);

		Assert.assertEquals(result, mockedResult);
		Assert.assertEquals(result.getPageable(), pageable);
		Assert.assertEquals(result.getContent().size(), 1);
		Assert.assertEquals(result.getContent().getFirst(), mockedRelationshipLinkEntity);
		Mockito.verify(relationshipModelLoader, Mockito.times(1)).loadModel(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		Mockito.verify(repository, Mockito.times(1)).findByRelationshipModelName(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL, pageable);
	}

	@Test void testLoadPageSizeIsLargerThanMaxConfig_successfully() throws IOException {
		RelationshipLinkSpecification relationshipLinkSpecification = new RelationshipLinkSpecification(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		Integer maxPageSize = dataServicesCoreProperties.getQuery().getMaxLinksSize();
		Pageable pageable = PageRequest.of(1, 10000, Sort.by(ORDER_BY_ROLES_SORT_COLUMNS));
		Pageable expectPageable = PageRequest.of(1, maxPageSize, Sort.by(ORDER_BY_ROLES_SORT_COLUMNS));

		RelationshipModel mockRelationshipModel = relationshipModelResolver.getRelationshipModelById(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);

		RelationshipLinkEntity mockedRelationshipLinkEntity = Mockito.mock(RelationshipLinkEntity.class);
		Page<RelationshipLinkEntity> mockedResult = new PageImpl<>(List.of(mockedRelationshipLinkEntity), expectPageable, 10);

		doReturn(mockRelationshipModel).when(relationshipModelLoader).loadModel(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		doReturn(mockedResult).when(repository).findByRelationshipModelName(Mockito.any(), Mockito.any());

		Page<? extends RelationshipLink> result = defaultRelationshipLinkService.load(relationshipLinkSpecification, pageable);

		Assert.assertEquals(result, mockedResult);
		Assert.assertEquals(result.getPageable().getPageSize(), maxPageSize);
		Assert.assertEquals(result.getContent().size(), 1);
		Assert.assertEquals(result.getContent().getFirst(), mockedRelationshipLinkEntity);
		Mockito.verify(relationshipModelLoader, Mockito.times(1)).loadModel(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		Mockito.verify(repository, Mockito.times(1)).findByRelationshipModelName(
			Mockito.eq(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL),
			Mockito.argThat(p -> {
				Assert.assertEquals(p.getSort(), pageable.getSort());
				Assert.assertEquals(p.getPageNumber(), pageable.getPageNumber());
				Assert.assertEquals(p.getPageSize(), maxPageSize);
				return true;
			}));
	}

	@Test void testLoadWithSourceFilter_successfully() throws IOException {
		RelationshipLinkSpecification relationshipLinkSpecification =
			new RelationshipLinkSpecification(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		DocumentReference sourceDocRef = DocumentReference.builder()
			.documentModelName(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.documentId(UUID.randomUUID().toString())
			.build();
		relationshipLinkSpecification.setSourceFilter(
			new RelationshipLinkSpecification.RelationshipRoleSpecification(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, sourceDocRef));
		Pageable pageable = PageRequest.of(2, 50, Sort.by(ORDER_BY_ROLES_SORT_COLUMNS));

		RelationshipModel mockRelationshipModel =
			relationshipModelResolver.getRelationshipModelById(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		RelationshipLinkEntity mockedRelationshipLinkEntity = Mockito.mock(RelationshipLinkEntity.class);

		Page<RelationshipLinkEntity> mockedResult = new PageImpl<>(List.of(mockedRelationshipLinkEntity), pageable, 10);

		doReturn(mockRelationshipModel).when(relationshipModelLoader).loadModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		doReturn(mockedResult).when(repository).findByRelationshipModelNameAndSource(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Page<? extends RelationshipLink> result = defaultRelationshipLinkService.load(relationshipLinkSpecification, pageable);

		Assert.assertEquals(result, mockedResult);
		Assert.assertEquals(result.getPageable(), pageable);
		Assert.assertEquals(result.getContent().size(), 1);
		Assert.assertEquals(result.getContent().getFirst(), mockedRelationshipLinkEntity);
		Mockito.verify(relationshipModelLoader, Mockito.times(1)).loadModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		Mockito.verify(repository, Mockito.times(1)).findByRelationshipModelNameAndSource(
			RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
			RelationshipModelConstants.RoleConstants.CONTRACT_ROLE,
			sourceDocRef,
			pageable
		);
	}

	@Test void testLoadWithSourceAndTargetFilter_successfully() throws IOException {
		RelationshipLinkSpecification relationshipLinkSpecification =
			new RelationshipLinkSpecification(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		DocumentReference sourceDocRef = DocumentReference.builder()
			.documentModelName(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.documentId(UUID.randomUUID().toString())
			.build();
		DocumentReference targetDocRef = DocumentReference.builder()
			.documentModelName(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL)
			.documentId(UUID.randomUUID().toString())
			.build();
		relationshipLinkSpecification.setSourceFilter(
			new RelationshipLinkSpecification.RelationshipRoleSpecification(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, sourceDocRef));
		relationshipLinkSpecification.setTargetFilter(
			new RelationshipLinkSpecification.RelationshipRoleSpecification(RelationshipModelConstants.RoleConstants.PARTNER_ROLE, targetDocRef));
		Pageable pageable = PageRequest.of(2, 50, Sort.by(ORDER_BY_ROLES_SORT_COLUMNS));

		RelationshipModel mockRelationshipModel =
			relationshipModelResolver.getRelationshipModelById(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		RelationshipLinkEntity mockedRelationshipLinkEntity = Mockito.mock(RelationshipLinkEntity.class);
		Page<RelationshipLinkEntity> mockedResult = new PageImpl<>(List.of(mockedRelationshipLinkEntity), pageable, 10);

		doReturn(mockRelationshipModel).when(relationshipModelLoader).loadModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		doReturn(mockedResult).when(repository)
			.findByRelationshipModelNameAndSourceAndTarget(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		Page<? extends RelationshipLink> result = defaultRelationshipLinkService.load(relationshipLinkSpecification, pageable);

		Assert.assertEquals(result, mockedResult);
		Assert.assertEquals(result.getPageable(), pageable);
		Assert.assertEquals(result.getContent().size(), 1);
		Assert.assertEquals(result.getContent().getFirst(), mockedRelationshipLinkEntity);
		Mockito.verify(relationshipModelLoader, Mockito.times(1)).loadModel(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		Mockito.verify(repository, Mockito.times(1)).findByRelationshipModelNameAndSourceAndTarget(
			RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL,
			RelationshipModelConstants.RoleConstants.CONTRACT_ROLE,
			sourceDocRef,
			RelationshipModelConstants.RoleConstants.PARTNER_ROLE,
			targetDocRef,
			pageable
		);
	}

	private LinkDescriptor mockLinkDescriptor() {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(RelationshipModelConstants.PRODUCT_BUNDLE_RM);

		RelationshipRoleSpec relationshipRoleSpec1 = new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.PRODUCT_ROLE,
			DocumentReference.builder()
				.documentId(RandomStringUtils.secure().nextAlphabetic(10))
				.documentModelName(DocumentModelConstants.PRODUCT_MODEL_NAME)
				.build());
		linkDescriptor.getEntities().add(relationshipRoleSpec1);

		RelationshipRoleSpec relationshipRoleSpec2 = new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.BUNDLE_ROLE,
			DocumentReference.builder()
				.documentId(RandomStringUtils.secure().nextAlphabetic(10))
				.documentModelName(DocumentModelConstants.DOMAIN_BUNDLE_MODEL_NAME)
				.build());
		linkDescriptor.getEntities().add(relationshipRoleSpec2);
		return linkDescriptor;
	}

	private RelationshipLink mockRelationshipLink(String id) {
		DataServicesRelationshipLink link = new DataServicesRelationshipLink(RelationshipModelConstants.PRODUCT_BUNDLE_RM, null);
		link.setId(id);

		DocumentReference documentReference1 = DocumentReference.builder()
			.documentId(RandomStringUtils.secure().nextAlphabetic(10))
			.documentModelName(DocumentModelConstants.PRODUCT_MODEL_NAME)
			.build();
		DocumentReference documentReference2 = DocumentReference.builder()
			.documentId(RandomStringUtils.secure().nextAlphabetic(10))
			.documentModelName(DocumentModelConstants.DOMAIN_BUNDLE_MODEL_NAME)
			.build();
		Map<String, RelationshipRole> roles = new HashMap<>();
		DataServicesRelationshipRole role1 = new DataServicesRelationshipRole(RelationshipModelConstants.RoleConstants.PRODUCT_ROLE, documentReference1, "1");
		DataServicesRelationshipRole role2 = new DataServicesRelationshipRole(RelationshipModelConstants.RoleConstants.BUNDLE_ROLE, documentReference2, "2");
		roles.put(role1.getName(), role1);
		roles.put(role2.getName(), role2);
		link.setRoles(roles);
		return link;
	}

	private RelationshipModel mockRelationshipModel(Header header) {
		RelationshipModel relationshipModel = new RelationshipModel();
		relationshipModel.setHeader(header);
		return relationshipModel;
	}

}
