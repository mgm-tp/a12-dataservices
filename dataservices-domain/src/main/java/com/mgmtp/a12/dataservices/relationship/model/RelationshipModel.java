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
package com.mgmtp.a12.dataservices.relationship.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleNameNotFoundException;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Relationship Model class. Java representation of the relationship model JSON containing header and the content.
 *
 */
@Data @AllArgsConstructor @NoArgsConstructor
public class RelationshipModel implements Model, Serializable {

	/**
	 * Semantic version of the relationship meta-model schema.
	 */
	public static final String VERSION = "3.0.0";
	/**
	 * Meta-model name used to identify relationship meta definitions.
	 */
	public static final String META_MODEL_NAME = "RelationshipMetaModel";
	/**
	 * Classpath location of the JSON meta-model resource.
	 */
	public static final String META_MODEL_JSON_LOCATION = "/com/mgmtp/a12/platform/relationship/RelationshipMetaModel.json";
	/**
	 * Header annotation key for sub-type model identifiers.
	 */
	public static final String SUB_TYPES_ANNOTATION_KEY = "subTypes";
	/**
	 * Header annotation key for super-type model identifiers.
	 */
	public static final String SUPER_TYPES_ANNOTATION_KEY = "superTypes";
	/**
	 * Model type discriminator for relationship models.
	 */
	public static final String RELATIONSHIP_MODEL_TYPE = "relationship";

	@JsonIgnore private Header header;

	private RelationshipModelContent content;

	/**
	 * Finds the target entity characteristics given the source role name.
	 *
	 * @param sourceRole role name considered as source; case-insensitive; must not be null.
	 * @return the {@link EntityCharacteristics} for the opposite (target) role.
	 */
	public EntityCharacteristics getTargetEntityCharacteristicsFromSourceRole(String sourceRole) {
		boolean firstEntity = getContent().getEntityCharacteristics().get(0).getRole().equalsIgnoreCase(sourceRole);
		if (firstEntity) {
			return getContent().getEntityCharacteristics().get(1);
		} else {
			return getContent().getEntityCharacteristics().get(0);
		}
	}

	/**
	 * Resolves the entity characteristics for a role name.
	 *
	 * @param roleName role name to resolve; case-insensitive; must not be null.
	 * @return the matching {@link EntityCharacteristics}.
	 * @throws com.mgmtp.a12.dataservices.relationship.exception.RelationshipRoleNameNotFoundException if no role exists for the given name.
	 */
	public EntityCharacteristics getEntityCharacteristicsFromRole(String roleName) {
		return getContent().getEntityCharacteristics().stream()
			.filter(e -> roleName.equalsIgnoreCase(e.getRole()))
			.findAny()
			.orElseThrow(() -> new RelationshipRoleNameNotFoundException(getHeader().getId(), roleName).withAnonymityMessage("Get enrichment from role failed."));
	}
}
