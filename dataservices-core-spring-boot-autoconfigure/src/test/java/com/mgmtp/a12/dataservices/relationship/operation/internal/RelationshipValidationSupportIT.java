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
package com.mgmtp.a12.dataservices.relationship.operation.internal;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipModelMismatchException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleMismatchException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipValidationException;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipRole;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipValidationSupport;
import com.mgmtp.a12.dataservices.relationship.operation.AbstractListITBase;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;

public class RelationshipValidationSupportIT extends AbstractListITBase {

	private static final String BRAND_ROLE = "brand";
	private static final String PRODUCT_ROLE = "product";
	private static final String BUNDLE_ROLE = "bundle";
	private static final DocumentReference BRAND_DOCREF = new DocumentReference("brand", "brandDocRef");
	private static final DocumentReference PRODUCT_DOCREF = new DocumentReference("product", "productDocRef");
	private static final DocumentReference BUNDLE_DOCREF = new DocumentReference("bundle", "bundleDocRef");
	private static final DocumentReference OTHER_DOCREF = new DocumentReference("other", "OtherDocRef");
	private static final String COMMON_RELATIONSHIPMODEL = "commonRM";
	private static final String OTHER_RELATIONSHIPMODEL = "otherRM";
	private static final Map<Role, Pair<String, DocumentReference>> ROLE_MAP = new EnumMap<>(Role.class) {{
		put(Role.BRAND, new ImmutablePair<>(BRAND_ROLE, BRAND_DOCREF));
		put(Role.PRODUCT, new ImmutablePair<>(PRODUCT_ROLE, PRODUCT_DOCREF));
		put(Role.BUNDLE, new ImmutablePair<>(BUNDLE_ROLE, BUNDLE_DOCREF));
		put(Role.PRODUCT_OTHER_DOCREF, new ImmutablePair<>(PRODUCT_ROLE, OTHER_DOCREF));
	}};
	private static final RelationshipLink
		BRAND_PRODUCT_ENTITY = relationshipLinkEntityBuilder(Role.BRAND, Role.PRODUCT);
	private static final RelationshipLink PRODUCT_BUNDLE_ENTITY =
		relationshipLinkEntityBuilder(Role.PRODUCT, Role.BUNDLE);
	private static final RelationshipLink BUNDLE_PRODUCT_ENTITY =
		relationshipLinkEntityBuilder(Role.BUNDLE, Role.PRODUCT);
	private static final RelationshipLink BRAND_PRODUCT_OTHER_DOCREF_ENTITY =
		relationshipLinkEntityBuilder(Role.BRAND, Role.PRODUCT_OTHER_DOCREF);
	private static final RelationshipLinkSpec
		BRAND_PRODUCT_LINK_REF = linkRefBuilder(Role.BRAND, Role.PRODUCT);
	private static final RelationshipLinkSpec PRODUCT_BRAND_LINK_REF =
		linkRefBuilder(Role.PRODUCT, Role.BRAND);
	private static final RelationshipLinkSpec PRODCUT_BUNDLE_LINK_REF =
		linkRefBuilder(Role.PRODUCT, Role.BUNDLE);
	private static final RelationshipLinkSpec PRODUCT_BUNDLE_OTHER_DOC_REF_LINK_REF =
		linkRefBuilder(Role.PRODUCT_OTHER_DOCREF, Role.BUNDLE);
	private static final RelationshipLinkSpec OTHER_RM_LINK_REF =
		linkRefBuilder(Role.BUNDLE, Role.PRODUCT, OTHER_RELATIONSHIPMODEL);
	private String invalidVersionRM;

	private static RelationshipLink relationshipLinkEntityBuilder(
		Role role1, Role role2) {
		RelationshipLink relationshipLink = new DataServicesRelationshipLink(COMMON_RELATIONSHIPMODEL, null);
		relationshipLink.addRole(new DataServicesRelationshipRole(ROLE_MAP.get(role1).getLeft(), ROLE_MAP.get(role1).getRight(), null));
		relationshipLink.addRole(new DataServicesRelationshipRole(ROLE_MAP.get(role2).getLeft(), ROLE_MAP.get(role2).getRight(), null));
		return relationshipLink;
	}

	private static RelationshipLinkSpec linkRefBuilder(Role role1, Role role2) {
		return linkRefBuilder(role1, role2, COMMON_RELATIONSHIPMODEL);
	}

	private static RelationshipLinkSpec linkRefBuilder(Role role1, Role role2,
		String relationshipModel) {
		RelationshipRoleSpec relationshipRoleSpec1 = linkEntitySpecBuilder(role1);
		RelationshipRoleSpec relationshipRoleSpec2 = linkEntitySpecBuilder(role2);

		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setEntities(Arrays.asList(relationshipRoleSpec1, relationshipRoleSpec2));

		RelationshipLinkSpec relationshipLinkSpec = new RelationshipLinkSpec();
		relationshipLinkSpec.setLinkDescriptor(linkDescriptor);
		relationshipLinkSpec.setId("1");
		relationshipLinkSpec.getLinkDescriptor().setRelationshipModel(relationshipModel);

		return relationshipLinkSpec;
	}

	private static RelationshipRoleSpec linkEntitySpecBuilder(Role role) {
		return new RelationshipRoleSpec(ROLE_MAP.get(role).getLeft(), ROLE_MAP.get(role).getRight());
	}

	@BeforeClass @Override public void init() throws Exception {
		super.init();
		invalidVersionRM = resourceFunctions.loadResource(PathConstants.RELATIONSHIP_MODEL_INVALID_PATH + "InvalidVersionRM.json");
	}

	@Test(expectedExceptions = InvalidInputException.class)
	public void createRMWithWrongVersion() {
		createModel(invalidVersionRM);
	}

	@Test public void testValidateLinkSuccessful() {
		try {
			RelationshipValidationSupport.validateLink(BRAND_PRODUCT_ENTITY, BRAND_PRODUCT_LINK_REF.getLinkDescriptor());
			RelationshipValidationSupport.validateLink(BRAND_PRODUCT_ENTITY, PRODUCT_BRAND_LINK_REF.getLinkDescriptor());
		} catch (RelationshipValidationException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = RelationshipModelMismatchException.class, expectedExceptionsMessageRegExp = "Requested link \\[null] is for relationship model \\[commonRM], but expected is \\[otherRM]")
	public void testValidateLinkWrongRelationshipModel() {
		assertValidation(BUNDLE_PRODUCT_ENTITY, OTHER_RM_LINK_REF, WRONG_RELATIONSHIP_MODEL_MSG);
	}

	@Test(expectedExceptions = RelationshipRoleMismatchException.class, expectedExceptionsMessageRegExp = "Requested link \\[null] has role:docRef \\[bundle:bundle/bundleDocRef] and \\[product:product/productDocRef], but expected is \\[brand:brand/brandDocRef] and \\[product:product/productDocRef]")
	public void testValidateLinkWrongRole() {
		assertValidation(BRAND_PRODUCT_ENTITY, PRODCUT_BUNDLE_LINK_REF, "Wrong Entity");
	}

	@Test(expectedExceptions = RelationshipRoleMismatchException.class, expectedExceptionsMessageRegExp = "Requested link \\[null] has role:docRef \\[bundle:bundle/bundleDocRef] and \\[product:other/OtherDocRef], but expected is \\[bundle:bundle/bundleDocRef] and \\[product:product/productDocRef]")
	public void testValidateLinkWrongDocRef1() {
		assertValidation(PRODUCT_BUNDLE_ENTITY, PRODUCT_BUNDLE_OTHER_DOC_REF_LINK_REF, "Wrong Entity");
	}

	@Test(expectedExceptions = RelationshipRoleMismatchException.class, expectedExceptionsMessageRegExp = "Requested link \\[null] has role:docRef \\[brand:brand/brandDocRef] and \\[product:product/productDocRef], but expected is \\[brand:brand/brandDocRef] and \\[product:other/OtherDocRef]")
	public void testValidateLinkWrongDocRef2() {
		assertValidation(BRAND_PRODUCT_OTHER_DOCREF_ENTITY, BRAND_PRODUCT_LINK_REF, "Wrong Entity");
	}

	private void assertValidation(RelationshipLink bundleProductEntity, RelationshipLinkSpec otherRMLinkRef, String wrongRelationshipModel) {
		try {
			RelationshipValidationSupport.validateLink(bundleProductEntity, otherRMLinkRef.getLinkDescriptor());
		} catch (RelationshipValidationException e) {
			Assert.assertEquals(e.getTitle(), wrongRelationshipModel);
			throw e;
		}
	}

	private enum Role {
		BRAND, PRODUCT, BUNDLE, PRODUCT_OTHER_DOCREF
	}

}
