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
package com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity;

import java.io.Serializable;
import java.time.Instant;

import com.mgmtp.a12.model.header.Header;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "model")
@Entity public class ModelEntity implements Serializable {

	@Id @Getter @Setter @Column(nullable = false, updatable = false) private String id;

	@Getter @Setter @Column(nullable = false, updatable = false) private Instant createdAt;

	@Getter @Setter @Column(updatable = false) private String createdBy;

	@Getter @Setter @Column(nullable = false) private Instant updatedAt;

	@Getter @Setter @Column private String updatedBy;

	@Getter @Setter @Column(nullable = false) private String content;

	public ModelEntity() {
	}

	public ModelEntity(Header header, String content) {
		this.content = content;
		this.id = header.getId();
		updatedAt = Instant.now();
		createdAt = Instant.now();
	}

	@PreUpdate public void onUpdate() {
		updatedAt = Instant.now();
	}

	@PrePersist public void onInsert() {
		createdAt = Instant.now();
		updatedAt = Instant.now();
	}
}
