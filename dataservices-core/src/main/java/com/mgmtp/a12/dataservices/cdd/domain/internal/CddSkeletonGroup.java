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
package com.mgmtp.a12.dataservices.cdd.domain.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceToStringConverter;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_SOURCE_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_TARGET_DOCUMENT_MODEL_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_TARGET_ROLE_ANNOTATION;
import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;

@JsonPropertyOrder({
	"name", "path",
	"relationshipModelName",
	"sourceRole", "sourceDocRef",
	"targetRole", "targetDocRef",
	"targetDocumentModel", "children"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true) @ToString(exclude = { "group", "parent", "children" })
public class CddSkeletonGroup implements ICddSkeletonNode {

	@JsonIgnore private IGroup group;
	@JsonIgnore private RelationshipLink link;
	@JsonIgnore @Builder.Default private int position = 1;
	@JsonIgnore private CddSkeletonGroup parent;
	@Builder.Default private List<CddSkeletonGroup> children = new ArrayList<>();

	public CddSkeletonGroup(IGroup group) {
		this.group = group;
	}

	public String getName() {
		return group.getName();
	}

	@JsonIgnore public boolean isRelationship() {
		return getRelationshipModelName() != null;
	}

	@JsonSerialize(converter = DocumentReferenceToStringConverter.class)
	public DocumentReference getSourceDocRef() {
		return getRoleDocRef(getSourceRole());
	}

	@Override public DocumentReference getTargetDocRef() {
		return getRoleDocRef(getTargetRole());
	}

	public String getRelationshipModelName() {
		return ModelUtils.getAnnotationValue(getGroup(), CDM_RELATIONSHIP_ANNOTATION)
			.orElse(null);
	}

	public String getSourceRole() {
		return ModelUtils.getAnnotationValue(getGroup(), CDM_SOURCE_ROLE_ANNOTATION)
			.orElse(null);
	}

	public String getTargetRole() {
		return ModelUtils.getAnnotationValue(getGroup(), CDM_TARGET_ROLE_ANNOTATION)
			.orElse(null);
	}

	@Override public String getTargetDocumentModel() {
		return ModelUtils.getAnnotationValue(getGroup(), CDM_TARGET_DOCUMENT_MODEL_ANNOTATION)
			.orElse(null);
	}

	@JsonSerialize(converter = DocumentReferenceToStringConverter.class)
	public DocumentReference getRoleDocRef(String role) {
		return Optional.ofNullable(getLink())
			.map(RelationshipLink::getRoles)
			.map(r -> r.get(role))
			.map(RelationshipRole::getDocRef)
			.orElse(null);
	}

	public Stream<CddSkeletonGroup> getByPath(@NonNull String path) {
		String[] ps = path.split(FIELD_SEPARATOR, 2);
		if (ps.length == 0) {
			return Stream.of();
		}
		Stream<CddSkeletonGroup> matchingChildren = getChildren().stream()
			.filter(g -> Objects.equals(ps[0], g.getName()));
		if (ps.length == 1) {
			return matchingChildren;
		} else {
			return matchingChildren
				.flatMap(g -> getByPath(ps[1]));
		}
	}

	public String getPath() {
		return getPath(getGroup(), FIELD_SEPARATOR);
	}

	private String getPath(IGroup group, String separator) {
		if (group.getParent() == null) {
			return separator.concat(group.getName());
		} else {
			return getPath(group.getParent(), separator).concat(separator).concat(group.getName());
		}
	}

	public Optional<DocumentReference> getLinkDocRef() {
		return Optional.ofNullable(getLink())
			.map(RelationshipLink::getLinkDocumentDocRef);
	}
}
