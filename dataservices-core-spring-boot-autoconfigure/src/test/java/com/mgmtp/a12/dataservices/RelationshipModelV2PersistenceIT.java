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
package com.mgmtp.a12.dataservices;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsIterableContaining;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelReadRepository;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;


public class RelationshipModelV2PersistenceIT extends AbstractSpringContextIT {

	@Autowired private IModelLoader<RelationshipModel> relationshipModelLoader;
	@Autowired private RelationshipModelReadRepository relationshipModelRepository;

	@BeforeMethod public void setUp() throws Exception {
		cleanUpTestEnvironment();
		String relationshipModelContent = loadResourceFromClasspathAsString(PathConstants.PRODUCT_BUNDLE_RM_PATH);
		modelService.create(relationshipModelContent);
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Relationship model \\[NonExisting] not found")
	public void loadNonExistingRelationshipModel() {
		relationshipModelLoader.loadModel("NonExisting");
	}

	@Test public void saveRelationshipModel() {
		String relationshipModelContent = loadResourceFromClasspathAsString(PathConstants.PRODUCT_BRAND_RM_PATH);
		modelService.create(relationshipModelContent);

		RelationshipModel relationshipModel = relationshipModelRepository.readModel(RelationshipModelConstants.PRODUCT_BRAND_RM);
		Assert.assertNotNull(relationshipModel);
		List<EntityCharacteristics> entityCharacteristics = relationshipModel.getContent().getEntityCharacteristics();
		MatcherAssert.assertThat(entityCharacteristics,
			IsIterableContaining.hasItem(Matchers.<EntityCharacteristics>hasProperty("role", Matchers.is(RelationshipModelConstants.RoleConstants.PRODUCT_ROLE))));
		MatcherAssert.assertThat(entityCharacteristics,
			IsIterableContaining.hasItem(Matchers.<EntityCharacteristics>hasProperty("role", Matchers.is(RelationshipModelConstants.RoleConstants.BRAND_ROLE))));
		MatcherAssert.assertThat(entityCharacteristics,
			IsIterableContaining.hasItem(Matchers.<EntityCharacteristics>hasProperty("documentModel", Matchers.is(DocumentModelConstants.PRODUCT_MODEL_NAME))));
		MatcherAssert.assertThat(entityCharacteristics,
			IsIterableContaining.hasItem(Matchers.<EntityCharacteristics>hasProperty("documentModel", Matchers.is(DocumentModelConstants.BRAND_MODEL_NAME))));
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = ".*Type: VALUE_ERROR Message: Only values with up to 85 characters are allowed.*")
	public void saveRelationshipModelWithLongRoleName_ShouldThrowException() {
		String relationshipModelContent = loadResourceFromClasspathAsString(PathConstants.LONG_ROLE_NAME_PRODUCT_BRAND_PATH);
		modelService.create(relationshipModelContent);
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void addDuplicateRelationshipModel() {
		String relationshipModelContent = loadResourceFromClasspathAsString(PathConstants.PRODUCT_BUNDLE_RM_PATH);

		modelService.create(relationshipModelContent);
	}

	@Test public void updateRelationshipModel() {
		RelationshipModel relationshipModel = relationshipModelRepository.readModel(RelationshipModelConstants.PRODUCT_BUNDLE_RM);
		Assert.assertNotNull(relationshipModel.getContent().getLinkDocumentModel());
		MatcherAssert.assertThat(relationshipModel.getContent().getLinkDocumentModel(), Matchers.is("AdditionalFieldsModel"));

		String relationshipModelContent = loadResourceFromClasspathAsString(PathConstants.RELATIONSHIP_MODEL_PATH + "ProductBundleUpdated.json");
		modelService.update(relationshipModelContent);

		relationshipModel = relationshipModelRepository.readModel(RelationshipModelConstants.PRODUCT_BUNDLE_RM);
		MatcherAssert.assertThat(relationshipModel.getContent().getLinkDocumentModel(), Matchers.is("AdditionalFieldsModel"));
	}

	@Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Model header \\[ProductBrand] not found in the persistent store")
	public void updateNonExistingRelationshipModel() {
		String relationshipModelContent = loadResourceFromClasspathAsString(PathConstants.PRODUCT_BRAND_RM_PATH);
		modelService.update(relationshipModelContent);
	}
}
