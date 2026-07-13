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
package com.mgmtp.a12.examples.document.sequence.generator;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants;
import com.mgmtp.a12.dataservices.document.IDocumentIdGenerator;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Document ID generator producing sequential IDs from the database sequence `document_seq`.
 * If an ID is already present and not marked with {@link SmeWorkspaceConstants#IGNORED_ID}, the generator respects it.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.sequence-id-generator", name = "enabled", havingValue = "true")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component public class SequenceIdGenerator implements IDocumentIdGenerator {

	@PersistenceContext(unitName = "dsPersistenceUnit") private EntityManager entityManager;

	/**
	 * Generates a document ID using the database sequence when no usable ID is present.
	 *
	 * @param document the document to inspect for an existing ID; may be null.
	 * @return an {@link java.util.Optional} containing the resolved ID.
	 */
	@Override public Optional<String> generateId(DocumentV2 document) {
		return Optional.ofNullable(document)
			.flatMap(DocumentV2::getId)
			.filter(id -> !SmeWorkspaceConstants.IGNORED_ID.equals(id))
			.or(() -> Optional.of(String.valueOf(entityManager.createNativeQuery("SELECT NEXTVAL('document_seq')").getSingleResult())));
	}
}

