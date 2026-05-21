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
package com.mgmtp.a12.dataservices.document.operation;

/**
 * Constants for all DS core operations and operation groups.
 *
 */
public interface CoreOperationConstants {

	/** Operation identifier for retrieving a single document. */
	String GET_DOCUMENT_OPERATION = "GET_DOCUMENT";

	/** Operation identifier for running a query. */
	String QUERY_OPERATION = "QUERY";

	/** Operation identifier for creating a new document. */
	String ADD_DOCUMENT_OPERATION = "ADD_DOCUMENT";

	/** Operation identifier for creating a copy of an existing document. */
	String COPY_DOCUMENT_OPERATION = "COPY_DOCUMENT";

	/** Operation identifier for replacing an existing document. */
	String MODIFY_DOCUMENT_OPERATION = "MODIFY_DOCUMENT";

	/** Operation identifier for partially updating a document. */
	String PARTIAL_MODIFY_DOCUMENT_OPERATION = "PARTIAL_MODIFY_DOCUMENT";

	/** Operation identifier for deleting a single document. */
	String DELETE_DOCUMENT_OPERATION = "DELETE_DOCUMENT";

	/** Operation identifier for deleting multiple documents. */
	String MULTI_DELETE_DOCUMENTS_OPERATION = "MULTI_DELETE_DOCUMENTS";

	/** Operation identifier for validating a document. */
	String VALIDATE_DOCUMENT_OPERATION = "VALIDATE_DOCUMENT";

	/** Operation identifier for adding a relationship link. */
	String ADD_LINK_OPERATION = "ADD_LINK";

	/** Operation identifier for deleting a relationship link. */
	String DELETE_LINK_OPERATION = "DELETE_LINK";

	/** Operation identifier for relinking a document. */
	String RELINK_DOCUMENT_OPERATION = "RELINK_DOCUMENT";

	/** Operation identifier for modifying a relationship link. */
	String MODIFY_LINK_OPERATION = "MODIFY_LINK";

	/** Operation identifier for loading an attachment header. */
	String LOAD_ATTACHMENT_HEADER_OPERATION = "LOAD_ATTACHMENT_HEADER";

	/** Operation identifier for loading an attachment URL. */
	String LOAD_ATTACHMENT_URL_OPERATION = "LOAD_ATTACHMENT_URL";

	/** Operation identifier for loading a thumbnail URL. */
	String LOAD_THUMBNAIL_URL_OPERATION = "LOAD_THUMBNAIL_URL";

	/** Operation identifier for loading multiple thumbnail URLs (internal). */
	String LOAD_THUMBNAIL_URLS_INTERNAL_OPERATION = "LOAD_THUMBNAIL_URLS_INTERNAL";

	/** Operation identifier for listing models (internal). */
	String LIST_MODELS_INTERNAL_OPERATION = "LIST_MODELS_INTERNAL";

	/** Operation identifier for listing document validation codes (internal). */
	String LIST_DOCUMENT_VALIDATION_CODES_INTERNAL_OPERATION = "LIST_DOCUMENT_VALIDATION_CODES_INTERNAL";

	/** Operation group identifier for internal A12 operations. */
	String A12_INTERNAL_OPERATIONS_GROUP = "A12_INTERNAL_OPERATIONS";

	// TODO A12S-5063 remove it in breaking release.
	/** Alias for internal A12 operations group kept for backward compatibility. */
	String A12_INTERNAL_GROUP = "A12_INTERNAL_OPERATIONS";

	/** Operation group identifier for document operations. */
	String DOCUMENT_OPERATIONS_GROUP = "DOCUMENT_OPERATIONS";

	/** Operation group identifier for relationship link operations. */
	String LINK_OPERATIONS_GROUP = "LINK_OPERATIONS";

	/** Operation group identifier for attachment operations. */
	String ATTACHMENT_OPERATIONS_GROUP = "ATTACHMENT_OPERATIONS";
}
