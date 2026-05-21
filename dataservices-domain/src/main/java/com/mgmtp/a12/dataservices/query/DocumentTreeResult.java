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
package com.mgmtp.a12.dataservices.query;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceToStringConverter;
import com.mgmtp.a12.dataservices.query.topology.QueryTopology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Single document in the document graph result.
 */
@DocumentationDiagram
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data @Builder @RequiredArgsConstructor @AllArgsConstructor
public class DocumentTreeResult {

	/**
	 * Document reference of the document.
	 */
	@JsonSerialize(converter = DocumentReferenceToStringConverter.class)
	private DocumentReference docRef;
	/**
	 * Relationship link model if linked.
	 */
	private String relationshipModel;
	/**
	 * Source role of the link if linked.
	 */
	private String sourceRole;
	/**
	 * Source document reference if linked.
	 */
	@JsonSerialize(converter = DocumentReferenceToStringConverter.class)
	private DocumentReference sourceDocRef;
	/**
	 * Target role of the link if linked.
	 */
	private String targetRole;
	/**
	 * Target document reference if linked.
	 */
	@JsonSerialize(converter = DocumentReferenceToStringConverter.class)
	private DocumentReference targetDocRef;
	/**
	 * Content of the document.
	 */
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private JsonNode document;
	/**
	 * Type of this node.
	 * Could be {@link DocumentTreeNodeType#ROOT} for the root document,
	 * {@link DocumentTreeNodeType#CHILD} for a linked document or {@link DocumentTreeNodeType#LINK} for a link document.
	 */
	private DocumentTreeNodeType type;
	/**
	 * Temporary ID used to pair query and response.
	 *
	 * @see QueryTopology#getBackReference()
	 */
	private String backReference;

	/**
	 * For internal use only, to pair query part to the result. See {@link QueryTopology#getInternalId()}.
	 */
	@JsonIgnore
	private UUID internalId;
	/**
	 * ID of the relationship link.
	 */
	private String linkId;
	/**
	 * Depth of the tree structure.
	 */
	private int depth;
	/**
	 * To indicate that this node should be used for fields projection or not.
	 * In case of fields projection this node will be used to construct document with selected projection fields.
	 */
	@JsonIgnore
	private boolean fieldsProjection;

	/**
	 * Document model name of the document.
	 */
	public String getDocumentModelName() {
		return docRef.getDocumentModelName();
	}
}

