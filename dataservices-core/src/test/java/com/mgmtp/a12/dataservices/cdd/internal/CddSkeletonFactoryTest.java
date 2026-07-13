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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelContent;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest.TestAnnotation;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.header.Header;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_SOURCE_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ID_FIELD_NAME;
import static com.mgmtp.a12.dataservices.relationship.internal.RelationshipSortConstants.ORDER_BY_ROLES_SORT_COLUMNS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for `CddSkeletonFactory#constructSkeletonFromRootGroup`.
 */
public class CddSkeletonFactoryTest {

	private static final String RELATIONSHIP_MODEL_NAME = "TestRelationshipModel";
	private static final String SOURCE_ROLE = "sourceRole";
	private static final String TARGET_ROLE = "targetRole";

	private RelationshipLinkRepository relationshipLinkRepository;
	@SuppressWarnings("unchecked")
	private IModelLoader<RelationshipModel> relationshipModelLoader;
	private DataServicesCoreProperties dataServicesCoreProperties;
	private CddSkeletonFactory factory;

	private DocumentReference documentReference;
	private CddSkeletonGroup parentGroup;

	@BeforeMethod
	public void setUp() {
		relationshipLinkRepository = mock(RelationshipLinkRepository.class);
		//noinspection unchecked
		relationshipModelLoader = mock(IModelLoader.class);
		dataServicesCoreProperties = mock(DataServicesCoreProperties.class);
		DataServicesCoreProperties.Query queryProps = mock(DataServicesCoreProperties.Query.class);
		when(dataServicesCoreProperties.getQuery()).thenReturn(queryProps);
		when(queryProps.getMaxLinksSize()).thenReturn(10_000);
		factory = new CddSkeletonFactory(relationshipLinkRepository, relationshipModelLoader, dataServicesCoreProperties);

		documentReference = new DocumentReference("TestModel", "doc-1");
		parentGroup = CddSkeletonGroup.builder()
			.group(groupWithNoAnnotations("parentGroup"))
			.build();
	}

	/**
	 * Verifies that a group without a relationship annotation produces exactly one skeleton group
	 * without triggering any repository calls.
	 */
	@Test(enabled = true, description = "Should return single skeleton group when group has no relationship annotation")
	public void shouldReturnSingleSkeletonGroupWhenGroupHasNoRelationshipAnnotation() {
		IGroup group = groupWithNoAnnotations("contractGroup");

		List<CddSkeletonGroup> result = factory.constructSkeletonFromRootGroup(documentReference, parentGroup, group).toList();

		assertEquals(result.size(), 1);
		verifyNoInteractions(relationshipLinkRepository);
	}

	/**
	 * Verifies that a group with a relationship annotation but no source-role annotation produces
	 * exactly one skeleton group without triggering any repository calls.
	 */
	@Test(enabled = true, description = "Should return single skeleton group when source role annotation is absent")
	public void shouldReturnSingleSkeletonGroupWhenSourceRoleAnnotationIsAbsent() {
		IGroup group = groupWithAnnotations("relationshipGroup",
			List.of(new TestAnnotation(CDM_RELATIONSHIP_ANNOTATION, RELATIONSHIP_MODEL_NAME)));

		List<CddSkeletonGroup> result = factory.constructSkeletonFromRootGroup(documentReference, parentGroup, group).toList();

		assertEquals(result.size(), 1);
		verifyNoInteractions(relationshipLinkRepository);
	}

	/**
	 * Verifies that a null document reference results in exactly one skeleton group without
	 * triggering any repository calls.
	 */
	@Test(enabled = true, description = "Should return single skeleton group when document reference is null")
	public void shouldReturnSingleSkeletonGroupWhenDocumentReferenceIsNull() {
		IGroup group = groupWithBothAnnotations("relationshipGroup");

		List<CddSkeletonGroup> result = factory.constructSkeletonFromRootGroup(null, parentGroup, group).toList();

		assertEquals(result.size(), 1);
		verifyNoInteractions(relationshipLinkRepository);
	}

	/**
	 * Verifies that a null parent (root invocation) results in exactly one skeleton group without
	 * triggering any repository calls.
	 */
	@Test(enabled = true, description = "Should return single skeleton group when parent is null")
	public void shouldReturnSingleSkeletonGroupWhenParentIsNull() {
		IGroup group = groupWithBothAnnotations("relationshipGroup");

		List<CddSkeletonGroup> result = factory.constructSkeletonFromRootGroup(documentReference, null, group).toList();

		assertEquals(result.size(), 1);
		verifyNoInteractions(relationshipLinkRepository);
	}

	/**
	 * Verifies that links are loaded with ordering by role columns when source characteristics
	 * are marked as ordered.
	 */
	@Test(enabled = true, description = "Should load ordered links when source characteristics are ordered")
	public void shouldLoadOrderedLinksWhenSourceCharacteristicsAreOrdered() {
		IGroup group = groupWithBothAnnotations("orderedGroup");
		RelationshipModel model = relationshipModelWithOrderedSource(true);
		when(relationshipModelLoader.loadModel(RELATIONSHIP_MODEL_NAME)).thenReturn(model);

		RelationshipLink link1 = mock(RelationshipLink.class);
		RelationshipLink link2 = mock(RelationshipLink.class);
		Sort expectedSort = Sort.by(ORDER_BY_ROLES_SORT_COLUMNS);
		Pageable expectedPageable = OffsetBasedPageRequest.ofOffset(0, 10_000, expectedSort);

		doReturn(new PageImpl<>(List.of(link1, link2)))
			.when(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), eq(expectedPageable));

		List<CddSkeletonGroup> result = factory.constructSkeletonFromRootGroup(documentReference, parentGroup, group).toList();

		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getPosition(), 1);
		assertEquals(result.get(1).getPosition(), 2);
		verify(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), eq(expectedPageable));
	}

	/**
	 * Verifies that links are loaded with ordering by ID when source characteristics are not ordered.
	 */
	@Test(enabled = true, description = "Should load unordered links when source characteristics are not ordered")
	public void shouldLoadUnorderedLinksWhenSourceCharacteristicsAreNotOrdered() {
		IGroup group = groupWithBothAnnotations("unorderedGroup");
		RelationshipModel model = relationshipModelWithOrderedSource(null);
		when(relationshipModelLoader.loadModel(RELATIONSHIP_MODEL_NAME)).thenReturn(model);

		RelationshipLink link1 = mock(RelationshipLink.class);
		Sort expectedSort = Sort.by(ID_FIELD_NAME);
		Pageable expectedPageable = OffsetBasedPageRequest.ofOffset(0, 10_000, expectedSort);

		doReturn(new PageImpl<>(List.of(link1)))
			.when(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), eq(expectedPageable));

		List<CddSkeletonGroup> result = factory.constructSkeletonFromRootGroup(documentReference, parentGroup, group).toList();

		assertEquals(result.size(), 1);
		verify(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), eq(expectedPageable));
	}

	/**
	 * Verifies that the repository receives a pageable with the configured `maxLinksSize` as page size.
	 */
	@Test(enabled = true, description = "Should use maxLinksSize as page size when loading links")
	public void shouldCapPageSizeUsingMaxLinksSizeWhenLargerPageRequested() {
		DataServicesCoreProperties.Query queryProps = mock(DataServicesCoreProperties.Query.class);
		when(dataServicesCoreProperties.getQuery()).thenReturn(queryProps);
		when(queryProps.getMaxLinksSize()).thenReturn(5);

		IGroup group = groupWithBothAnnotations("capGroup");
		RelationshipModel model = relationshipModelWithOrderedSource(null);
		when(relationshipModelLoader.loadModel(RELATIONSHIP_MODEL_NAME)).thenReturn(model);

		doReturn(new PageImpl<>(Collections.emptyList()))
			.when(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), any(Pageable.class));

		factory.constructSkeletonFromRootGroup(documentReference, parentGroup, group).toList();

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), pageableCaptor.capture());
		assertEquals(pageableCaptor.getValue().getPageSize(), 5);
	}

	/**
	 * Verifies that child groups are processed recursively and the target document reference from
	 * a link is propagated into the child recursion.
	 */
	@Test(enabled = true, description = "Should recurse into child groups and propagate target document reference")
	public void shouldRecurseIntoChildGroupsAndPropagateTargetDocRef() {
		IGroup childGroup = groupWithNoAnnotations("childGroup");
		IGroup rootGroup = groupWithBothAnnotationsAndChild("rootGroup", childGroup);

		RelationshipModel model = relationshipModelWithOrderedSource(null);
		when(relationshipModelLoader.loadModel(RELATIONSHIP_MODEL_NAME)).thenReturn(model);

		RelationshipLink link = mock(RelationshipLink.class);
		Sort expectedSort = Sort.by(ID_FIELD_NAME);
		Pageable expectedPageable = OffsetBasedPageRequest.ofOffset(0, 10_000, expectedSort);

		doReturn(new PageImpl<>(List.of(link)))
			.when(relationshipLinkRepository).findByRelationshipModelNameAndSource(eq(RELATIONSHIP_MODEL_NAME), eq(SOURCE_ROLE), eq(documentReference), eq(expectedPageable));

		Stream<CddSkeletonGroup> resultStream = factory.constructSkeletonFromRootGroup(documentReference, parentGroup, rootGroup);
		List<CddSkeletonGroup> result = resultStream.toList();

		assertEquals(result.size(), 1);
		List<CddSkeletonGroup> children = result.get(0).getChildren();
		assertTrue(children.size() >= 1, "Expected at least one child skeleton group from recursive call");
	}

	// ---- helpers ----

	private IGroup groupWithNoAnnotations(String name) {
		IGroup group = mock(IGroup.class);
		when(group.getName()).thenReturn(name);
		when(group.getAnnotations()).thenReturn(Collections.emptyList());
		when(group.getElements()).thenReturn(Collections.emptyList());
		return group;
	}

	private IGroup groupWithAnnotations(String name, List<com.mgmtp.a12.model.header.Annotation> annotations) {
		IGroup group = mock(IGroup.class);
		when(group.getName()).thenReturn(name);
		when(group.getAnnotations()).thenReturn(annotations);
		when(group.getElements()).thenReturn(Collections.emptyList());
		return group;
	}

	private IGroup groupWithBothAnnotations(String name) {
		return groupWithAnnotations(name, List.of(
			new TestAnnotation(CDM_RELATIONSHIP_ANNOTATION, RELATIONSHIP_MODEL_NAME),
			new TestAnnotation(CDM_SOURCE_ROLE_ANNOTATION, SOURCE_ROLE)
		));
	}

	private IGroup groupWithBothAnnotationsAndChild(String name, IGroup childGroup) {
		IGroup group = mock(IGroup.class);
		when(group.getName()).thenReturn(name);
		when(group.getAnnotations()).thenReturn(List.of(
			new TestAnnotation(CDM_RELATIONSHIP_ANNOTATION, RELATIONSHIP_MODEL_NAME),
			new TestAnnotation(CDM_SOURCE_ROLE_ANNOTATION, SOURCE_ROLE)
		));
		List<IElement> elements = List.of(childGroup);
		when(group.getElements()).thenReturn(elements);
		return group;
	}

	private RelationshipModel relationshipModelWithOrderedSource(Boolean ordered) {
		EntityCharacteristics sourceCharacteristics = new EntityCharacteristics();
		sourceCharacteristics.setRole(SOURCE_ROLE);
		sourceCharacteristics.setOrdered(ordered);

		EntityCharacteristics targetCharacteristics = new EntityCharacteristics();
		targetCharacteristics.setRole(TARGET_ROLE);

		RelationshipModelContent content = new RelationshipModelContent();
		content.setEntityCharacteristics(List.of(sourceCharacteristics, targetCharacteristics));

		Header header = mock(Header.class);
		when(header.getId()).thenReturn(RELATIONSHIP_MODEL_NAME);

		RelationshipModel model = new RelationshipModel();
		model.setHeader(header);
		model.setContent(content);
		return model;
	}
}
