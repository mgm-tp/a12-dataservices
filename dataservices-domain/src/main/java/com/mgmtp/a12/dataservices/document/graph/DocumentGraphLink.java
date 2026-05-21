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
package com.mgmtp.a12.dataservices.document.graph;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceToStringConverter;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @deprecated Please use `QueryService` instead with `document-graph` projection
 *
 */
@Data @AllArgsConstructor @NoArgsConstructor
@Deprecated(since = "38.1.0", forRemoval = true)
public class DocumentGraphLink implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	private String linkId;
	private DocumentGraphLinkDescriptor linkDescriptor;

	@JsonSerialize(converter = DocumentReferenceToStringConverter.class)
	private DocumentReference linkDocRef;

	@Data @AllArgsConstructor @NoArgsConstructor
	public static class DocumentGraphLinkDescriptor {

		private String relationshipModelName;
		private List<RelationshipRoleSpec> entities;

	}
}
