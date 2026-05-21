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
package com.mgmtp.a12.dataservices.authorization;

/**
 * All authorization related constants are defined here.
 */
public interface AuthConstants {

	/**
	 * Prefix of the SpEL expression used by {@link org.springframework.security.access.prepost.PreAuthorize}
	 * to check UAA permissions.
	 */
	String UAA_PERMISSION_TEMPLATE_PREFIX = "hasUAAPermission('";

	/**
	 * Suffix of the SpEL expression used by {@link org.springframework.security.access.prepost.PreAuthorize}
	 * to check UAA permissions.
	 */
	String UAA_PERMISSION_TEMPLATE_SUFFIX = "')";

	/**
	 * Permission label for creating a document.
	 */
	String DOCUMENT_CREATE_PERMISSION = "Document Create";

	/**
	 * Permission label for replacing an existing document.
	 */
	String DOCUMENT_UPDATE_PERMISSION = "Document Update";

	/**
	 * Permission label for partially updating an existing document.
	 */
	String DOCUMENT_PARTIAL_UPDATE_PERMISSION = "Document Partial Update";

	/**
	 * Permission label for deleting a single document.
	 */
	String DOCUMENT_DELETE_PERMISSION = "Document Delete";

	/**
	 * Permission label for deleting multiple documents in a single operation.
	 */
	String DOCUMENT_MULTI_DELETE_PERMISSION = "Document Multi Delete";

	/**
	 * Permission label for executing queries.
	 */
	String DOCUMENT_QUERY_PERMISSION = "Query";

	/**
	 * Permission label for creating a document model.
	 */
	String MODEL_CREATE_PERMISSION = "Model Create";

	/**
	 * Permission label for updating a document model.
	 */
	String MODEL_UPDATE_PERMISSION = "Model Update";

	/**
	 * Permission label for reading a document model.
	 */
	String MODEL_READ_PERMISSION = "Model Read";

	/**
	 * Permission label for deleting a document model.
	 */
	String MODEL_DELETE_PERMISSION = "Model Delete";

	/**
	 * Permission label for accessing endpoints guarded by
	 * {@link com.mgmtp.a12.dataservices.server.uaa.SecuredController}.
	 */
	String ENDPOINT_PERMISSION = "Endpoint";

	/**
	 * SpEL expression used by {@link com.mgmtp.a12.dataservices.server.uaa.SecuredController}
	 * to enforce the `Endpoint` permission.
	 */
	String UAA_ENDPOINT_PERMISSION = UAA_PERMISSION_TEMPLATE_PREFIX + ENDPOINT_PERMISSION + UAA_PERMISSION_TEMPLATE_SUFFIX;

	/**
	 * Permission label for uploading attachments.
	 */
	String ATTACHMENT_UPLOAD_PERMISSION = "Attachment Upload";

	/**
	 * Permission label for exporting lists (CDD).
	 */
	String EXPORT_LIST_CDD_PERMISSION = "Export List CDD";

	/**
	 * Default message used when authorization fails.
	 */
	String ACCESS_DENIED = "Access Denied";

}
