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

import java.util.HashMap;
import java.util.Optional;

import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.authorization.AbstractCorePermissionTest;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.model.header.Header;


public class DefaultDocumentModelPermissionEvaluatorTest extends AbstractCorePermissionTest {

	@Mock private CachedPermissionEvaluator cachedPermissionEvaluator;
	@Mock private ModelHeaderJpaRepository modelHeaderRepository;

	@InjectMocks DefaultDocumentModelPermissionEvaluator defaultDocumentModelPermissionEvaluator;

	@BeforeMethod
	void beforeMethod() {
		super.initData();
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckModelCreatePermission_success(boolean expectedResult) {
		callCheckPermission(() -> {
				defaultDocumentModelPermissionEvaluator.checkModelCreatePermission(header);
				return null;
			},
			expectedResult,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1))
			.checkPermissions(header, AuthConstants.MODEL_CREATE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckModelUpdatePermission_success(boolean expectedResult) {
		callCheckPermission(() -> {
				defaultDocumentModelPermissionEvaluator.checkModelUpdatePermission(header);
				return null;
			},
			expectedResult,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1))
			.checkPermissions(header, AuthConstants.MODEL_UPDATE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckModelDeletePermission_success(boolean expectedResult) {
		callCheckPermission(() -> {
				defaultDocumentModelPermissionEvaluator.checkModelDeletePermission(header);
				return null;
			},
			expectedResult,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1))
			.checkPermissions(header, AuthConstants.MODEL_DELETE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckModelReadPermission_success(boolean expectedResult) {
		callCheckPermission(() -> {
				defaultDocumentModelPermissionEvaluator.checkModelDeletePermission(header);
				return null;
			},
			expectedResult,
			AuthConstants.ACCESS_DENIED
		);
		Mockito.verify(authorizationService, Mockito.times(1))
			.checkPermissions(header, AuthConstants.MODEL_DELETE_PERMISSION);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckModelReadPermissionWithModelId_success(boolean expectedResult) {
		String modelId = header.getId();
		ModelHeaderEntity headerEntity = makeHeaderEntity(header);

		Mockito.when(modelHeaderRepository.findById(modelId)).thenReturn(Optional.of(headerEntity));
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(headerEntity)).thenReturn(expectedResult);

		callCheckPermission(() -> {
				defaultDocumentModelPermissionEvaluator.checkModelReadPermission(modelId);
				return null;
			},
			expectedResult,
			AuthConstants.ACCESS_DENIED
		);

		Mockito.verify(authorizationService, Mockito.times(0))
			.checkPermissions(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(modelHeaderRepository, Mockito.times(1))
			.findById(modelId);
		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1))
			.hasModelReadPermission(headerEntity);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testCheckModelReadPermissionWithModel_success(boolean expectedResult) {
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(iDocumentModel.getHeader())).thenReturn(expectedResult);
		callCheckPermission(() -> {
				defaultDocumentModelPermissionEvaluator.checkModelReadPermission(iDocumentModel);
				return null;
			},
			expectedResult,
			AuthConstants.ACCESS_DENIED
		);

		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(iDocumentModel.getHeader());
	}


	@Test(dataProvider = "permissionResultCheck")
	void testHasModelReadPermissionWithModelHeader_success(boolean expectedResult) {

		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(header)).thenReturn(expectedResult);
		callHasPermission(
			() -> defaultDocumentModelPermissionEvaluator.hasModelReadPermission(header),
			expectedResult
		);

		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1))
			.hasModelReadPermission(header);
		Mockito.verify(authorizationService, Mockito.times(0))
			.checkPermissions(ArgumentMatchers.any(), ArgumentMatchers.any());
	}

	@Test(dataProvider = "permissionResultCheck")
	void testHasModelReadPermissionWithModelId_success(boolean expectedResult) {
		String modelId = header.getId();
		ModelHeaderEntity headerEntity = makeHeaderEntity(header);

		Mockito.when(modelHeaderRepository.findById(modelId)).thenReturn(Optional.of(headerEntity));
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(headerEntity)).thenReturn(expectedResult);

		callHasPermission(() -> defaultDocumentModelPermissionEvaluator.hasModelReadPermission(modelId),
			expectedResult
		);

		Mockito.verify(authorizationService, Mockito.times(0))
			.checkPermissions(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(modelHeaderRepository, Mockito.times(1))
			.findById(modelId);
		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1))
			.hasModelReadPermission(headerEntity);
	}

	@Test(dataProvider = "permissionResultCheck")
	void testHasModelReadPermissionWithModel_success(boolean expectedResult) {
		Mockito.when(cachedPermissionEvaluator.hasModelReadPermission(iDocumentModel.getHeader())).thenReturn(expectedResult);
		callHasPermission(
			() -> defaultDocumentModelPermissionEvaluator.hasModelReadPermission(iDocumentModel),
			expectedResult
		);

		Mockito.verify(cachedPermissionEvaluator, Mockito.times(1))
			.hasModelReadPermission(iDocumentModel.getHeader());
	}

	private ModelHeaderEntity makeHeaderEntity(Header header) {
		ModelHeaderEntity headerEntity = new ModelHeaderEntity();
		headerEntity.setId(header.getId());
		headerEntity.setModelType(header.getModelType());
		headerEntity.setAnnotations(new HashMap<>());
		headerEntity.setLabels(new HashMap<>());
		return headerEntity;
	}
}
