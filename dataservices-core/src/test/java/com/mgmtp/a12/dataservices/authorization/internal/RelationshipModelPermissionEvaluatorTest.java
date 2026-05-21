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

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.AbstractCorePermissionTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelContent;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

public class RelationshipModelPermissionEvaluatorTest extends AbstractCorePermissionTest {

	@Mock private ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
	@Mock private IModelReadRepository<RelationshipModel> relationshipModelReadRepository;
	@Mock private CachedPermissionEvaluator cachedPermissionEvaluator;
	@InjectMocks private RelationshipModelPermissionEvaluator relationshipModelPermissionEvaluator;

	private String linkDocumentModelName;
	private EntityCharacteristics characteristics1;
	private EntityCharacteristics characteristics2;
	private RelationshipModel relationshipModel;
	private String modelId;

	@BeforeMethod
	void beforeMethod() {
		super.initData();

	}

	@Test(expectedExceptions = AccessDeniedException.class, expectedExceptionsMessageRegExp = AuthConstants.ACCESS_DENIED)
	void testCheckModelReadPermission_dontHavePermission() {
		RelationshipModelContent content = new RelationshipModelContent();
		RelationshipModel relationshipModel = new RelationshipModel(header, content);
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(header)).thenReturn(false);

		relationshipModelPermissionEvaluator.checkModelReadPermission(relationshipModel);

		Mockito.verify(authorizationService, Mockito.times(1)).checkPermissions(header, AuthConstants.MODEL_READ_PERMISSION);
		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(header);
	}

	@Test void testCheckModelReadPermission_success() {
		initCheckPermissionData();

		Mockito.lenient().when(authorizationService.checkPermissions(header, AuthConstants.MODEL_READ_PERMISSION))
			.thenReturn(createPermissionCheckResult(true));
		Mockito.doNothing().when(documentModelPermissionEvaluator).checkModelReadPermission(ArgumentMatchers.any(String.class));
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(header)).thenReturn(true);

		relationshipModelPermissionEvaluator.checkModelReadPermission(relationshipModel);

		Mockito.verify(documentModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(linkDocumentModelName);
		Mockito.verify(documentModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(characteristics1.getDocumentModel());
		Mockito.verify(documentModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(characteristics2.getDocumentModel());
		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(header);
	}

	@Test void testCheckModelReadPermissionWithModelId_success() {
		initCheckPermissionData();

		Mockito.lenient().when(authorizationService.checkPermissions(header, AuthConstants.MODEL_READ_PERMISSION))
			.thenReturn(createPermissionCheckResult(true));
		Mockito.lenient().when(relationshipModelReadRepository.readModel(modelId))
			.thenReturn(relationshipModel);
		Mockito.doNothing().when(documentModelPermissionEvaluator).checkModelReadPermission(ArgumentMatchers.any(String.class));
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(header)).thenReturn(true);

		relationshipModelPermissionEvaluator.checkModelReadPermission(modelId);

		Mockito.verify(documentModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(linkDocumentModelName);
		Mockito.verify(documentModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(characteristics1.getDocumentModel());
		Mockito.verify(documentModelPermissionEvaluator, Mockito.times(1)).checkModelReadPermission(characteristics2.getDocumentModel());
		Mockito.verify(relationshipModelReadRepository, Mockito.times(1)).readModel(modelId);
		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(header);
	}

	private void initCheckPermissionData() {
		linkDocumentModelName = RandomStringUtils.randomAlphabetic(10);
		characteristics1 = new EntityCharacteristics();
		characteristics1.setDocumentModel(RandomStringUtils.randomAlphabetic(7));
		characteristics2 = new EntityCharacteristics();
		characteristics2.setDocumentModel(RandomStringUtils.randomAlphabetic(7));

		RelationshipModelContent content = new RelationshipModelContent();
		content.setLinkDocumentModel(linkDocumentModelName);
		content.setEntityCharacteristics(List.of(characteristics1, characteristics2));

		relationshipModel = new RelationshipModel(header, content);
		modelId = relationshipModel.getHeader().getId();
	}


}
