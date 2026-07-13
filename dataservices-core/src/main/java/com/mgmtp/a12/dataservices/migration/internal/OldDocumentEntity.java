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
package com.mgmtp.a12.dataservices.migration.internal;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.Immutable;

import com.mgmtp.a12.dataservices.common.repository.StringSequenceGenerator;
import com.mgmtp.a12.dataservices.document.DocumentReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains all resources for document definition.
 */
// TODO A12S-6875: Remove this entity and the DOCUMENT_BACKUP table — used only for 37→38 migration, obsolete since 39.
@Deprecated(since = "39.0.0")
@Table(name = "DOCUMENT_BACKUP")
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true)
@Immutable @Entity public class OldDocumentEntity implements Serializable {

	@GeneratedValue(generator = StringSequenceGenerator.GENERATOR_NAME)
	@StringSequenceGenerator.Sequence(name = "DOCUMENT_SEQ")
	@Id private String id;

	@NotNull @Column(name = "model_name") private String modelName;

	@NotNull @Column(name = "created_by") private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at") private Instant createdAt;

	@Column(name = "modified_by") private String modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_at") private Instant modifiedAt;

	@NotNull @Column(name = "xml_doc", columnDefinition = "clob") private String xmlDoc;

	@Transient public DocumentReference getDocRef() {
		return new DocumentReference(getModelName(), getId());
	}

	@Override public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OldDocumentEntity other = (OldDocumentEntity) obj;
		if (modelName == null) {
			if (other.modelName != null) {
				return false;
			}
		} else if (!modelName.equals(other.modelName)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;

	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}
