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
package com.mgmtp.a12.examples.document.staticcode;

import java.util.Optional;

import com.mgmtp.a12.kernel.md.rt.api.IDocumentStaticServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.api.IStaticModelCodeCache;
/**
 * Example configuration for static document services.
 * Provides the package location of generated model code and optional service hooks.
 */
public class ExampleDocumentStaticServiceConfig implements IDocumentStaticServiceConfig {

	/**
	 * {@inheritDoc}
	 *
	 * @return an empty {@link java.util.Optional} because variants are not supported.
	 */
	public Optional<String> getVariant() {
		// not supported in A12
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return an empty {@link java.util.Optional} indicating no label provider is configured.
	 */
	@Override
	public Optional<ILabelProvider> getLabelProvider() {
		return Optional.empty();
	}

	/**
	 * Returns the Java package of statically generated model code for the given document model.
	 *
	 * @param documentModelId the document model identifier; never null.
	 * @return the package name containing generated classes.
	 */
	@Override
	public Optional<String> getModelPackage(String documentModelId) {
		// TODO: Check if 'documentModelId' is adjusted during code generation
		return Optional.of("com.mgmtp.a12.kernel.generated."+documentModelId);
	}

	/**
	 * {@inheritDoc}
	 * No cache from kernel is needed, DS is using its own cache implementation.
	 *
	 * @return an empty {@link java.util.Optional} since DataServices uses its own cache.
	 */
	@Override
	public Optional<IStaticModelCodeCache> getCache() {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param documentModelId the document model identifier; never null.
	 * @return an empty {@link java.util.Optional} because generated code is loaded by the base classloader.
	 */
	@Override
	public Optional<ClassLoader> getModelCodeClassLoader(String documentModelId) {
		return Optional.empty(); // the statically generated code is expected to be available in the base classloader
	}
}

