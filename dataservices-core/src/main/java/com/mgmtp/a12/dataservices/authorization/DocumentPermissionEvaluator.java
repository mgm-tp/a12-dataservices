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

import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Central place to check document related permissions.
 *
 */
@OnlyForUsage public interface DocumentPermissionEvaluator {
	/**
	 * Check if current user has Document Create permission.
	 *
	 * @param document The evaluated document.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentCreatePermission(DocumentV2 document);

	/**
	 * Check if current user has Document Create permission.
	 *
	 * @param document The evaluated document.
	 * @return `true` if user has permission.
	 */
	boolean hasDocumentCreatePermission(DocumentV2 document);

	/**
	 * Check if current user has Document Partial Update permission.
	 *
	 * @param oldDocument The old document, i.e. the one before update.
	 * @param newDocument The new document, i.e. the one it should be after the update.
	 * @param docRef The DocumentReference of the document that should be updated.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentPartialUpdatePermission(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef);

	/**
	 * Check if current user has Document Update permission to update existing document.
	 * Please note that both document and documentReference are passed to the UAA as decision context.
	 *
	 * @param oldDocument The old document, i.e. the one before update.
	 * @param newDocument The new document, i.e. the one it should be after the update.
	 * @param docRef The DocumentReference of the document that should be updated.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentUpdatePermission(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef);

	/**
	 * Check if current user has Document Delete permission.
	 *
	 * @param document The evaluated document.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentDeletePermission(DataServicesDocument document);

	/**
	 * Check if current user has Document Multi Delete permission.
	 *
	 * @param headers Model headers.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Document Multi Delete permission.
	 */
	void checkDocumentMultiDeletePermission(Collection<Header> headers);

	/**
	 * Check if current user has Document Delete permission for the given model.
	 * This is a coarse-grained model-level check that does not require a document instance.
	 * Used as an early authorization gate before business logic (e.g., document lookup).
	 *
	 * @param documentModel The document model name.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentDeletePermissionByModel(String documentModel);

	/**
	 * Check if current user has Document Update permission for the given model.
	 * This is a coarse-grained model-level check that does not require a document instance.
	 * Used as an early authorization gate before business logic (e.g., document lookup).
	 *
	 * @param documentModel The document model name.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentUpdatePermissionByModel(String documentModel);

	/**
	 * Check if current user has Document Create permission for the given model.
	 * This is a coarse-grained model-level check that does not require a document instance.
	 * Used as an early authorization gate before business logic (e.g., document conversion/validation).
	 *
	 * @param documentModel The document model name.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentCreatePermissionByModel(String documentModel);

	/**
	 * Check if current user has Document Partial Update permission for the given model.
	 * This is a coarse-grained model-level check that does not require a document instance.
	 * Used as an early authorization gate before business logic (e.g., document lookup).
	 *
	 * @param documentModel The document model name.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentPartialUpdatePermissionByModel(String documentModel);

	/**
	 * Check if current user has Export List CDD permission.
	 *
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkExportListCDDPermission();

	/**
	 * Check if current user has Export List CDD permission.
	 *
	 * @return `true` if user has permission.
	 */
	boolean hasExportListCDDPermission();

	/**
	 * Checks if current user has permission to query documents.
	 * @param documentModel The target document model of the query.
	 * @throws AccessDeniedException if user does not have permission.
	 */
	void checkDocumentQueryPermission(String documentModel);

	/**
	 * Check if the current user has Query permission.
	 *
	 * @param documentModel The target document model of the root query.
	 * @return `true` if the user has permission.
	 */
	boolean hasDocumentQueryPermission(String documentModel);
}
