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
package com.mgmtp.a12.dataservices.model.metadata;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;

/**
 * Implementation of this interface allows enriching a DocumentModel by its metadata structure,
 * defined in the DocumentMetadataModel, and also allows stripping this metadata structure.
 */
public interface DocumentModelMetadataInjector {

	/**
	 * Create an enriched copy of a document model with attachment metadata and document metadata on all expected places.
	 *
	 * - For non-generated document models, there will be added "__meta" group to the root of the model.
	 * - For generated documents, group "__meta" will be added to every root group.
	 * - For CDM, group "__meta" will be added to each group annotated by `cdm.relationship` and also to its children group named "relationship" if it exists.
	 * - [.line-through]#All attachment fields will be decorated by attachment metadata group named `__attachment_meta_<attachment field name>`.#
	 *
	 * CAUTION: Attachment decoration is not implemented yet.
	 *
	 * @param documentMetadataModel document model metadata.
	 * @param attachmentMetadataModel attachment metadata,
	 * @return copy of the original document model enriched by the metadata.
	 */
	@NonNull IDocumentModel getDocumentModelWithMetadata(IDocumentModel documentMetadataModel, IDocumentModel attachmentMetadataModel);

	/**
	 * Remove all document metadata and attachment metadata groups by its name pattern.
	 *
	 * @return copy of the original document with all metadata removed.
	 */

	IDocumentModel getDocumentModelWithoutMetadata();
}
