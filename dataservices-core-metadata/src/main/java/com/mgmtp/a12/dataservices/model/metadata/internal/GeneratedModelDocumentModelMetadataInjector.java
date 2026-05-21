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
package com.mgmtp.a12.dataservices.model.metadata.internal;

import java.util.Locale;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

public class GeneratedModelDocumentModelMetadataInjector extends AbstractDocumentModelMetadataInjector {

	public GeneratedModelDocumentModelMetadataInjector(IDocumentModel documentModel, DocumentModelJoiningService documentModelJoiningService,
		DocumentModelService documentModelService, Locale locale) {

		super(documentModel, documentModelJoiningService, documentModelService, locale);
	}

	/**
	 * Add document metadata into all root groups.
	 *
	 * @param enrichedModel         model to enrich by metadata.
	 * @param documentMetadataGroup document metadata group.
	 */
	@Override protected void addMetadata(Group documentMetadataGroup, DocumentModel enrichedModel) {
		enrichedModel.getContent().getModelRoot().getElements().stream()
			.filter(Group.class::isInstance)
			.map(Group.class::cast)
			.forEach(g -> g.addElement(copyGroupAndAddParentIdPrefixToAllElements(documentMetadataGroup, g)));
	}

}
