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
package com.mgmtp.a12.dataservices.model.internal;

import java.util.Optional;

import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentModelIdProvider;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataServicesDocumentDynamicServiceConfig implements IDocumentDynamicServiceConfig {
	private final IModelCodeCache dataServicesModelCodeCache;

	@Override public IModelCodeCache getCache() {
		return dataServicesModelCodeCache;
	}

	public Optional<String> getVariant() {
		return Optional.empty();
	}

	@Override public Optional<ILabelProvider> getLabelProvider() {
		return Optional.empty();
	}

	/**
	 * Provides a model ID provider that extracts the document model ID from the model header.
	 *
	 * This allows the kernel framework to build cache keys without deserializing complete document models,
	 * improving memory usage and performance during model update and delete operations.
	 *
	 * @return Optional containing the model ID provider implementation
	 */
	@Override public Optional<IDocumentModelIdProvider> getModelIdProvider() {
		return Optional.of((documentModel) -> documentModel.getHeader().getId());
	}
}
