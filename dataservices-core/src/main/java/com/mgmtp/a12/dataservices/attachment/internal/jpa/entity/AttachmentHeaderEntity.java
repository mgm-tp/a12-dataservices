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
package com.mgmtp.a12.dataservices.attachment.internal.jpa.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contains all resources for attachment header definition.
 */
@Table(name = "ATTACHMENT_HEADER")
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true)
@Entity public class AttachmentHeaderEntity implements Serializable {

	@Id private String id;
	@Column private String thumbnailBigId;
	@Column private String thumbnailSmallId;
	@NotNull @Column private String fileName;
	@NotNull @Column private Instant createdAt;
	@NotNull @Column private String createdBy;
	@Column private Instant modifiedAt;
	@Column private String modifiedBy;
	@NotNull @Column private String mimeType;
	@Column(name = "ATTACHMENT_SIZE") private Long size;
	@Column(name = "TYPE_OF_THE_CONTENT") private String typeOfTheContent;

	@Setter(AccessLevel.NONE)
	@OneToMany(mappedBy = "attachmentHeader")
	private List<AttachmentReferenceEntity> references;

	public AttachmentReferenceEntity addReference(AttachmentReferenceEntity reference) {
		if (CollectionUtils.isEmpty(references)) {
			references = new ArrayList<>();
		}
		references.add(reference);
		reference.setAttachmentHeader(this);
		return reference;
	}

	public AttachmentReferenceEntity removeReference(AttachmentReferenceEntity reference) {
		if (CollectionUtils.isNotEmpty(references)) {
			references.remove(reference);
		}
		return reference;
	}

	public List<AttachmentReferenceEntity> removeAllReferences() {
		if (CollectionUtils.isNotEmpty(references)) {
			List<AttachmentReferenceEntity> refs = references;
			references = new ArrayList<>();
			refs.forEach(r -> r.setAttachmentHeader(null));
			return refs;
		} else {
			return List.of();
		}
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "ATTACHMENT_HEADER_ID", referencedColumnName = "ID")
	private List<AttachmentAnnotationEntity> annotations;

	@PrePersist private void onInsert() {
		this.createdAt = Instant.now();
		this.modifiedAt = this.createdAt;
	}

	@PreUpdate private void onUpdate() {
		this.modifiedAt = Instant.now();
	}
}
