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
package com.mgmtp.a12.examples.document.metadata;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Example listeners that enrich document metadata with an extension field before create/update operations.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.metadata", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class CustomMetadata {

	/** Metadata version assigned on document creation. */
	public static final String CREATE_VALUE = "1.0.0";

	/** Metadata version assigned on document update. */
	public static final String UPDATE_VALUE = "2.0.0";

	/** Metadata version assigned when document participates in a relationship link. */
	public static final String LINK_VALUE = "3.0.0";

	final DocumentService documentService;

	/**
	 * Sets metadata version to {@value #CREATE_VALUE} before a document is created.
	 *
	 * @param documentBeforeCreateEvent the create event containing the document being created
	 */
	@CommonDataServicesEventListener public void customMetadataBeforeCreate(DocumentBeforeCreateEvent documentBeforeCreateEvent) {
		documentBeforeCreateEvent.setCreatedDocument(
			documentBeforeCreateEvent.getCreatedDocument().withFieldValue("/__meta/extensions/metadataVersion", CREATE_VALUE)
		);
	}

	/**
	 * Updates the metadata version before the document is persisted during update.
	 *
	 * @param documentBeforeUpdateEvent the event with the document to be updated; never null.
	 */
	/**
	 * Updates metadata version to {@value #UPDATE_VALUE} before a document is updated.
	 *
	 * Only updates if the current version is {@value #CREATE_VALUE} to avoid overwriting
	 * the version set by {@link #modifyMetadataOnAddLink(RelationshipLinkAfterCreateEvent)}.
	 *
	 * @param documentBeforeUpdateEvent the update event containing the document being updated
	 */
	@CommonDataServicesEventListener public void customMetadataBeforeUpdate(DocumentBeforeUpdateEvent documentBeforeUpdateEvent) {
		documentBeforeUpdateEvent.setUpdatedDocument(
			documentBeforeUpdateEvent.getUpdatedDocument().withFieldValue("/__meta/extensions/metadataVersion", UPDATE_VALUE)
		);
	}

	/**
	 * Updates metadata version to {@value #LINK_VALUE} for all documents participating in a newly created relationship link.
	 *
	 * Iterates over all roles in the link and updates each referenced document's metadata version.
	 * This occurs after the link is created.
	 *
	 * @param relationshipLinkAfterCreateEvent the event containing the newly created relationship link
	 */
	@CommonDataServicesEventListener public void modifyMetadataOnAddLink(RelationshipLinkAfterCreateEvent  relationshipLinkAfterCreateEvent) {
		relationshipLinkAfterCreateEvent.getLink().getRoles().forEach((roleName, role) -> {
			DocumentReference docRef = role.getDocRef();
			//DS validation prevents null there.
			DataServicesDocument updatedDocument = documentService.load(docRef).orElse(null);
			DocumentV2 kernelDocument = updatedDocument.getKernelDocument().withFieldValue("/__meta/extensions/linkAssignment", LINK_VALUE);
			documentService.update(docRef, kernelDocument, null);
		});
	}
}
