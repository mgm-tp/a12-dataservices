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
package com.mgmtp.a12.dataservices.relationship.spec;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Relationship Link Spec class. Wrapper class for a {@link LinkDescriptor} and an id. Id always takes precedence over the link descriptor.
 *
 */
@Data @NoArgsConstructor @Builder
public class RelationshipLinkSpec implements Serializable {

	private LinkDescriptor linkDescriptor;
	private String id;

	public RelationshipLinkSpec(LinkDescriptor linkDescriptor, String id) {
		this.linkDescriptor = linkDescriptor;
		this.id = id;
	}

	public RelationshipLinkSpec(LinkDescriptor linkDescriptor, String id, String sourceRank, String targetRank) {
		this.linkDescriptor = linkDescriptor;
		this.id = id;
		this.sourceRank = sourceRank;
		this.targetRank = targetRank;
	}

	/**
	 * Server-assigned rank of the created link from the perspective of the source-role entity.
	 * Contains a lexicographic rank string (e.g. `"a"`, `"b"`, `"aa"`) or `null` when the
	 * relationship is not ordered or the rank has not been set.
	 */
	private String sourceRank;

	/**
	 * Server-assigned rank of the created link from the perspective of the target-role entity.
	 * Contains a lexicographic rank string (e.g. `"a"`, `"b"`, `"aa"`) or `null` when the
	 * relationship is not ordered or the rank has not been set.
	 */
	private String targetRank;

}
