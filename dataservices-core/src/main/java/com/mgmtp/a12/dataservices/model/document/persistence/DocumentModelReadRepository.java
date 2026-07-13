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
package com.mgmtp.a12.dataservices.model.document.persistence;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

/**
 * Repository for reading {@link IDocumentModel} instances from storage.
 * Delegates deserialization to {@link com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils}.
 */
@Component public class DocumentModelReadRepository extends AbstractDocumentModelReadRepository<IDocumentModel> {

	protected DocumentModelReadRepository(ModelJpaRepository modelJpaRepository,
		ModelHeaderJpaRepository modelHeaderJpaRepository,
		ApplicationEventPublisher eventPublisher, DocumentModelUtils documentModelUtils) {
		super(modelJpaRepository, modelHeaderJpaRepository, eventPublisher, documentModelUtils);
	}

	@Override protected IDocumentModel buildModelFromHeaderAndContent(@NonNull Header header, @NonNull String modelContent) {
		return documentModelUtils.deserializeDocumentModel(header.getId(), modelContent);
	}
}
