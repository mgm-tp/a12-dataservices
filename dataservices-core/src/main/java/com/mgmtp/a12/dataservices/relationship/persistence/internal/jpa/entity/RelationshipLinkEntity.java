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
package com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.entity.DocumentReferenceConverter;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.id.CustomUuid;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @EqualsAndHashCode(of = { "id", "relationshipModel", "createdAt" }) @ToString @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder
@Table(name = "RELATIONSHIP_LINK")
@Entity public class RelationshipLinkEntity implements RelationshipLink, Serializable {

	public static final String COLUMN_NAME_ID = "id";

	@CustomUuid
	@Column(name = COLUMN_NAME_ID)
	@Id private String id;

	@Column private String relationshipModel;

	@Column private Instant createdAt;

	@Column(name = "LINK_DOCUMENT_DOCREF") @Convert(converter = DocumentReferenceConverter.class)
	private DocumentReference linkDocumentDocRef;

	@Builder.Default
	@OneToMany(orphanRemoval = true)
	@MapKey(name = "name")
	@JoinColumn(name = "RELATIONSHIP_ID", referencedColumnName = "ID", nullable = false, insertable = false, updatable = false)
	private Map<String, RelationshipRoleEntity> roles = new HashMap<>();

	public RelationshipLinkEntity(String relationshipModel, DocumentReference linkDocumentDocRef) {
		this.relationshipModel = relationshipModel;
		this.linkDocumentDocRef = linkDocumentDocRef;
		this.roles = new HashMap<>();
	}

	@PrePersist private void onInsert() {
		this.createdAt = Instant.now();
	}

	@Transient public void addRole(RelationshipRole role) {
		RelationshipRoleEntity roleEntity = (RelationshipRoleEntity) role;
		roleEntity.setRelationship(this);
		roles.put(role.getName(), roleEntity);
	}

}
