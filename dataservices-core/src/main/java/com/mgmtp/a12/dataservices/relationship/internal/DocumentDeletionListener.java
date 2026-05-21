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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.util.Collection;
import java.util.List;

import org.springframework.core.annotation.Order;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentsBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;

import lombok.RequiredArgsConstructor;

/**
 * A listener that will validate the link document existing when deleting documents.
 */
@RequiredArgsConstructor
public class DocumentDeletionListener {

	private final DefaultRelationshipLinkService relationshipLinkService;
	private final RelationshipLinkJpaRepository relationshipLinkJpaRepository;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * This listener has the lowest precedence because validating document from `DocumentBeforeDeleteEvent` should be performed lastly,
	 * this will make sure validation is also applied on any changes client project has made on `DocumentBeforeDeleteEvent`.
	 *
	 * @param documentBeforeDeleteEvent {@link DocumentBeforeDeleteEvent}
	 */
	@Order
	@CommonDataServicesEventListener
	public void onDeleteDocument(DocumentBeforeDeleteEvent documentBeforeDeleteEvent) {
		DocumentReference documentReference = documentBeforeDeleteEvent.getDocumentReference();
		validateDocumentsDeletion(List.of(documentReference));
	}

	/**
	 * This listener has the lowest precedence because validating document from `DocumentsBeforeDeleteEvent` should be performed lastly,
	 * this will make sure validation is also applied on any changes client project has made on `DocumentsBeforeDeleteEvent`.
	 *
	 * @param documentsBeforeDeleteEvent {@link DocumentBeforeDeleteEvent}
	 */
	@Order
	@CommonDataServicesEventListener
	public void onDeleteDocument(DocumentsBeforeDeleteEvent documentsBeforeDeleteEvent) {
		Collection<DocumentReference> documentReferences = documentsBeforeDeleteEvent.getDocumentReferences();
		validateDocumentsDeletion(documentReferences);
	}

	public void validateDocumentsDeletion(Collection<DocumentReference> documentReferences) {
		if (relationshipLinkService.countByLinkInDocumentDocRefs(documentReferences) > 0) {
			throw new IntegrityException(
				String.format("Document reference %s cannot be deleted as it is a link document in a relationship", documentReferences));
		}

		validateDisabledCascade(documentReferences);

		relationshipLinkService.deleteByRoleDocRefs(documentReferences);
	}

	private void validateDisabledCascade(Collection<DocumentReference> documentReferences) {
		List<String> modelsWithDisabledCascade = dataServicesCoreProperties.getDocuments()
			.getDelete()
			.getCascadeLinks()
			.getDisabledForModels();

		if (modelsWithDisabledCascade != null) {
			List<DocumentReference> disabledCascadeLinkDocRefs = documentReferences.stream()
				.filter(docRef -> GenericUtils.matchOrAll(docRef.getDocumentModelName(), modelsWithDisabledCascade))
				.toList();

			if (!disabledCascadeLinkDocRefs.isEmpty() && relationshipLinkJpaRepository.countNumberOfLinksForDocuments(disabledCascadeLinkDocRefs) > 0) {
				throw new IntegrityException(
					String.format("Document reference %s cannot be deleted because it is used in the link and cascade delete is disabled for this model.",
						String.join(", ", documentReferences)));
			}
		}
	}
}
