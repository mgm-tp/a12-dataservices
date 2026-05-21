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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Aggregator for all repositories that implements {@link IDocumentRepository} interface. This class is used to access documents within defined repositories.
 * To use custom repository class implementation of IDocumentRepository is required.
 */
@RequiredArgsConstructor
@Repository public class AggregatedDocumentRepository {

	private final List<IDocumentRepository> documentRepositories;

	public Optional<DataServicesDocument> getByDocumentReference(DocumentReference documentReference) {
		return documentRepositories.stream()
			.map(dr -> dr.findByDocumentReference(documentReference))
			.flatMap(Optional::stream)
			.findAny();
	}

	public @NonNull List<DocumentReference> findAllDocRefsForModel(String modelId) {
		return documentRepositories.stream()
			.flatMap(dr -> dr.findAllDocRefsForModel(modelId).stream())
			.toList();
	}

	public @NonNull List<DataServicesDocument> findDocumentsByDocRefs(List<DocumentReference> docRefs) {
		return documentRepositories.stream()
			.flatMap(dr ->dr.findDocumentsByDocRefs(docRefs).stream())
			.toList();
	}
}
