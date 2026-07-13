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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServiceTest;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.AbstractModelTypeService;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.ModelGraphDocumentModelElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.Model;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

public class ModelGraphServiceTest extends AbstractDataServiceTest {

	@Mock private ModelService modelService;
	@Mock private RelationshipModelLoader relationshipModelLoader;
	@Mock private RelationshipModelSerializer relationshipModelSerializer;
	@Mock private ModelTypeService modelTypeService;
	@Mock private AbstractModelTypeService abstractModelTypeService;
	@Mock private ModelPermissionEvaluator<Model> modelPermissionEvaluator;

	private ModelGraphService modelGraphService;
	private String serializeRelationshipModel;
	private IDocumentModel addressModel;
	private IDocumentModel businessPartnerSuperModel;
	private IDocumentModel businessPartner;
	private IDocumentModel businessPartnerLTD;
	private RelationshipModel relationshipModel;

	@BeforeMethod public void before() throws IOException {
		modelGraphService = new ModelGraphService(modelService, relationshipModelLoader, relationshipModelSerializer,
			modelTypeService, Optional.of(abstractModelTypeService), modelPermissionEvaluator);
		Mockito.reset(relationshipModelLoader, modelService, relationshipModelSerializer, modelPermissionEvaluator, modelTypeService);

		serializeRelationshipModel = RandomStringUtils.insecure().nextAlphabetic(50);

		addressModel = documentModelResolver.getDocumentModelById(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
		businessPartnerSuperModel = documentModelResolver.getDocumentModelById(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL);
		businessPartner = documentModelResolver.getDocumentModelById(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		businessPartnerLTD = documentModelResolver.getDocumentModelById(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL);
		relationshipModel = relationshipModelResolver.getRelationshipModelById(RelationshipModelConstants.PARTNER_ADDRESSES_MODEL);
		Mockito.when(relationshipModelLoader.loadAllRelationshipModels()).thenReturn(Set.of(relationshipModel));
		Mockito.when(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE))
			.thenReturn(List.of(
					addressModel.getHeader(),
					businessPartner.getHeader(),
					businessPartnerLTD.getHeader(),
					businessPartnerSuperModel.getHeader()
				)
			);
		Mockito.when(relationshipModelSerializer.serialize(relationshipModel)).thenReturn(serializeRelationshipModel);
	}

	@Test
	void testConstructModelGraph_shouldHavePermissionCheck() {

		Mockito.when(modelPermissionEvaluator.hasModelReadPermission(ArgumentMatchers.anyString())).thenReturn(true);

		ModelGraphRoot modelGraphRoot = modelGraphService.constructModelGraph();

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		Mockito.verify(relationshipModelLoader, Mockito.times(1)).loadAllRelationshipModels();
		Mockito.verify(modelService, Mockito.times(1)).findAllHeadersByType(DOCUMENT_MODEL_TYPE);
		Mockito.verify(relationshipModelSerializer, Mockito.times(1)).serialize(relationshipModel);
		Mockito.verifyNoInteractions(modelTypeService);
		Assert.assertEquals(modelGraphRoot.getDocumentModels().size(), 4);
		Map<String, ModelGraphDocumentModelElement> map = modelGraphRoot.getDocumentModels()
			.stream()
			.collect(
				Collectors.toMap(ModelGraphElement::getModelId, e -> e)
			);
		Assert.assertEquals(map.get(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL).getSubTypes().size(), 2);
		Assert.assertTrue(modelGraphRoot.getRelationshipModels().contains(serializeRelationshipModel));
	}

	@Test
	void testConstructModelGraph_hasNoPermissionForSubtypes() {
		Set<String> subtypes = Set.of(RandomStringUtils.insecure().nextAlphabetic(15));

		Mockito.when(modelPermissionEvaluator.hasModelReadPermission(ArgumentMatchers.anyString())).thenReturn(false);
		Mockito.when(modelTypeService.findDirectSubtypes(Mockito.any())).thenReturn(subtypes);

		ModelGraphRoot modelGraphRoot = modelGraphService.constructModelGraph();

		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(DocumentModelConstants.BUSINESS_PARTNER_LTD_MODEL);
		Mockito.verify(modelPermissionEvaluator, Mockito.times(1)).hasModelReadPermission(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);

		Mockito.verify(relationshipModelLoader, Mockito.times(1)).loadAllRelationshipModels();
		Mockito.verify(modelService, Mockito.times(1)).findAllHeadersByType(DOCUMENT_MODEL_TYPE);
		Mockito.verify(relationshipModelSerializer, Mockito.times(1)).serialize(relationshipModel);
		Mockito.verifyNoInteractions(modelTypeService);
		Assert.assertEquals(modelGraphRoot.getDocumentModels().size(), 4);
		Map<String, ModelGraphDocumentModelElement> map = modelGraphRoot.getDocumentModels()
			.stream()
			.collect(
				Collectors.toMap(ModelGraphElement::getModelId, e -> e)
			);
		Assert.assertEquals(map.get(DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL).getSubTypes().size(), 0);
		Assert.assertTrue(modelGraphRoot.getRelationshipModels().contains(serializeRelationshipModel));
	}
}
