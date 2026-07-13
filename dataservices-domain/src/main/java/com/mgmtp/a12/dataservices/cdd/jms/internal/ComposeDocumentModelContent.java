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
package com.mgmtp.a12.dataservices.cdd.jms.internal;

import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModelConfig;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelContent;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComposeDocumentModelContent implements IDocumentModelContent {
	private final IDocumentModelContent documentContent;

	@Override public @NonNull IDocumentModelInfo getDocumentModelInfo() {
		return documentContent.getDocumentModelInfo();
	}

	@Override public @NonNull IDocumentModelConfig getDocumentModelConfig() {
		return documentContent.getDocumentModelConfig();
	}

	@Override public @NonNull List<IDocumentUniquenessCriterion> getDocumentUniquenessCriteria() {
		throw new UnsupportedOperationException("Uniqueness criteria are not supported in the context of Compose Document Models");
	}

	@Override public @NonNull List<IFieldTypeDefinition> getTypeDefinitions() {
		return documentContent.getTypeDefinitions();
	}

	@Override public @NonNull IGroup getDocumentModelRoot() {
		return documentContent.getDocumentModelRoot();
	}

}
