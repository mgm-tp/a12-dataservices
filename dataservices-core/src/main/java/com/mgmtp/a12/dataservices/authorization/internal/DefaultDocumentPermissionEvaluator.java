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
package com.mgmtp.a12.dataservices.authorization.internal;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.DocumentUpdateResource;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.authorization.AuthConstants.ACCESS_DENIED;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.DOCUMENT_CREATE_PERMISSION;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.DOCUMENT_DELETE_PERMISSION;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.DOCUMENT_PARTIAL_UPDATE_PERMISSION;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.DOCUMENT_QUERY_PERMISSION;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.DOCUMENT_UPDATE_PERMISSION;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.EXPORT_LIST_CDD_PERMISSION;

@RequiredArgsConstructor
@Slf4j
@Component public class DefaultDocumentPermissionEvaluator implements DocumentPermissionEvaluator {

	private final AuthorizationService authorizationService;

	@Override public void checkDocumentCreatePermission(DocumentV2 document) {
		if (!hasDocumentCreatePermission(document)) {
			throw new AccessDeniedException(ACCESS_DENIED);
		}
	}

	@Override public void checkDocumentPartialUpdatePermission(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef) {
		checkPermission(createDocumentUpdateResource(oldDocument, newDocument, docRef), DOCUMENT_PARTIAL_UPDATE_PERMISSION, docRef);
	}

	@Override public void checkDocumentUpdatePermission(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef) {
		checkPermission(createDocumentUpdateResource(oldDocument, newDocument, docRef), DOCUMENT_UPDATE_PERMISSION, docRef);
	}

	@Override public void checkDocumentDeletePermission(DataServicesDocument document) {
		checkPermission(document, DOCUMENT_DELETE_PERMISSION, document.getMetadata().getDocRef());
	}

	@Override public void checkDocumentQueryPermission(String documentModel) {
		if (!hasDocumentQueryPermission(documentModel)) {
			throw new AccessDeniedException(ACCESS_DENIED);
		}
	}

	@Override public boolean hasDocumentQueryPermission(String documentModel) {
		log.debug("Check [{}] permission for document model [{}]", DOCUMENT_QUERY_PERMISSION, documentModel);
		return authorizationService.checkPermissions(documentModel, DOCUMENT_QUERY_PERMISSION).isPassed();
	}

	@Override public boolean hasDocumentCreatePermission(DocumentV2 document) {
		log.debug("Check [{}] permission for document [{}/{}]", DOCUMENT_CREATE_PERMISSION, document.getDocumentModelId(), document.getId());
		return authorizationService.checkPermissions(document, DOCUMENT_CREATE_PERMISSION).isPassed();
	}

	@Override public void checkDocumentMultiDeletePermission(Collection<Header> headers) {
		log.debug("Check document multi delete permission for models [{}]", headers.stream().map(Header::getId).collect(Collectors.joining(",")));

		if (authorizationService.checkPermissions(headers, AuthConstants.DOCUMENT_MULTI_DELETE_PERMISSION).isNotPassed()) {
			throw new AccessDeniedException("No " + AuthConstants.DOCUMENT_MULTI_DELETE_PERMISSION + " permission");
		}
	}

	@Override public boolean hasExportListCDDPermission() {
		return authorizationService.checkPermissions(null, EXPORT_LIST_CDD_PERMISSION).isPassed();
	}

	@Override public void checkExportListCDDPermission() {
		if (!hasExportListCDDPermission()) {
			throw new AccessDeniedException(ACCESS_DENIED);
		}
	}

	private void checkPermission(Object resource, String scopeName, DocumentReference docRef) {
		log.debug("Check [{}] permission for document [{}]", scopeName, docRef.toString());
		if (authorizationService.checkPermissions(resource, scopeName, Map.of("docRef", docRef)).isNotPassed()) {
			throw new AccessDeniedException(ACCESS_DENIED);
		}
	}

	private static DocumentUpdateResource createDocumentUpdateResource(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef) {
		return DocumentUpdateResource.builder()
			.oldDocument(oldDocument)
			.newDocument(newDocument)
			.docRef(docRef)
			.build();
	}
}
