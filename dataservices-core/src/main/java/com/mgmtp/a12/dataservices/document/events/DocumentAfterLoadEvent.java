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
package com.mgmtp.a12.dataservices.document.events;

import java.util.Optional;

import com.mgmtp.a12.dataservices.common.events.internal.EventDocumentation;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The event is published after the document is loaded.
 *
 * @note No data modifications are persisted.
 * @note To update the document, you must reassign the updated instance because {@link DocumentV2} is immutable.
 *
 * @topic Document events
 */
@Getter @EqualsAndHashCode @ToString
@EventDocumentation public final class DocumentAfterLoadEvent {

	private DocumentReference documentReference;
	@Setter private DocumentV2 document;
	private DataServicesDocument dataServicesDocument;

	/**
	 * Creates a new event with an explicit {@link DocumentReference} and {@link DocumentV2}.
	 *
	 * @param documentReference the reference of the loaded document; may be null if {@link #dataServicesDocument} is set
	 * @param document the loaded document instance; may be null if {@link #dataServicesDocument} is set
	 */
	public DocumentAfterLoadEvent(DocumentReference documentReference, DocumentV2 document) {
		this.documentReference = documentReference;
		this.document = document;
	}

	/**
	 * Returns the {@link DocumentReference} of the loaded document.
	 * If the explicit reference is null, derives it from {@link #dataServicesDocument} if available.
	 *
	 * @return the resolved document reference, or `null` if neither source is available
	 */
	public DocumentReference getDocumentReference() {
		return Optional.ofNullable(documentReference)
			.orElse(Optional.ofNullable(dataServicesDocument)
				.map(d->d.getMetadata().getDocRef())
				.orElse(null));
	}

	/**
	 * Returns the loaded {@link DocumentV2}.
	 * If the explicit document is null, derives it from {@link #dataServicesDocument} if available.
	 *
	 * @return the resolved document instance, or `null` if neither source is available
	 */
	public DocumentV2 getDocument() {
		return Optional.ofNullable(document)
			.orElse(Optional.ofNullable(dataServicesDocument)
				.map(DataServicesDocument::getKernelDocument)
				.orElse(null));
	}
}
