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
package com.mgmtp.a12.dataservices.authorization.internal;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.AbstractCorePermissionTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.cdd.CddConstants;
import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

public class ComposeDocumentModelPermissionEvaluatorTest extends AbstractCorePermissionTest {
	@Mock private CachedPermissionEvaluator cachedPermissionEvaluator;
	@Mock private ModelPermissionEvaluator<RelationshipModel> relationshipModelPermissionEvaluator;
	@Mock private ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	@InjectMocks private ComposeDocumentModelPermissionEvaluator composeDocumentModelPermissionEvaluator;

	@BeforeMethod
	void beforeMethod() throws IllegalAccessException {
		super.initData();
		FieldUtils.writeField(composeDocumentModelPermissionEvaluator, "modelPermissionEvaluator", modelPermissionEvaluator, true);
		FieldUtils.writeField(composeDocumentModelPermissionEvaluator, "relationshipModelPermissionEvaluator", relationshipModelPermissionEvaluator, true);
	}

	@Test(expectedExceptions = AccessDeniedException.class, expectedExceptionsMessageRegExp = AuthConstants.ACCESS_DENIED)
	void testCheckModelReadPermission_dontHavePermission() {
		ComposeDocumentModel composeDocumentModel = new ComposeDocumentModel(iDocumentModel);
		Mockito.lenient().when(authorizationService.checkPermissions(iDocumentModel.getHeader(), AuthConstants.MODEL_READ_PERMISSION))
			.thenReturn(createPermissionCheckResult(false));
		composeDocumentModelPermissionEvaluator.checkModelReadPermission(composeDocumentModel);

		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(iDocumentModel.getHeader(), AuthConstants.MODEL_READ_PERMISSION);
	}

	@Test void testCheckModelReadPermission_success() {
		String rootModelName = "rootModelName";
		String nestedModelName = "nestedModelName";
		iDocumentModel.getHeader().getAnnotations().add(new TestAnnotation(CddConstants.CDM_QUERY_ROOT_ANNOTATION, rootModelName));
		ComposeDocumentModel composeDocumentModel = new ComposeDocumentModel(iDocumentModel);

		IGroup iGroup = Mockito.mock(IGroup.class);

		doReturn(List.of(new TestAnnotation(CddConstants.CDM_RELATIONSHIP_ANNOTATION, nestedModelName))).when(iGroup).getAnnotations();

		Mockito.lenient().when(authorizationService.checkPermissions(composeDocumentModel.getHeader(), AuthConstants.MODEL_READ_PERMISSION))
			.thenReturn(createPermissionCheckResult(true));

		Mockito.doNothing().when(modelPermissionEvaluator).checkModelReadPermission(rootModelName);
		Mockito.doNothing().when(relationshipModelPermissionEvaluator).checkModelReadPermission(nestedModelName);
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(composeDocumentModel.getHeader())).thenReturn(true);

		try (MockedStatic<ComposeDocumentModelUtils> composeDocumentModelUtilsMockedStatic = Mockito.mockStatic(ComposeDocumentModelUtils.class)) {
			composeDocumentModelUtilsMockedStatic.when(() -> ComposeDocumentModelUtils.getCrdModelName(eq(iDocumentModel.getHeader())))
				.thenReturn(rootModelName);
			composeDocumentModelUtilsMockedStatic.when(() -> ComposeDocumentModelUtils.getCdmSkeletonGroups(eq(composeDocumentModel)))
				.thenReturn(Stream.of(new CddSkeletonGroup(iGroup)));

			composeDocumentModelPermissionEvaluator.checkModelReadPermission(composeDocumentModel);

			Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(rootModelName);
			Mockito.verify(relationshipModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(nestedModelName);
			Mockito.verify(cachedPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(composeDocumentModel.getHeader());
		}
	}
}
