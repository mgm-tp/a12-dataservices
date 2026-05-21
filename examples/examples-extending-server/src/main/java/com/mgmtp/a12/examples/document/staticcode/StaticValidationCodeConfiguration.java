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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mgmtp.a12.dataservices.autoconfigure.DataServicesCoreAutoconfiguration;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentServiceConfig;

/**
 * Static Validation Code Configuration class. This configuration provides a static validation code provider and an
 * IDocumentServiceConfig, which is needed to replace the default ones from DS.
 *
 */
@Configuration
@ConditionalOnProperty(
	name = "com.mgmtp.a12.examples.documents.static-code.enabled",
	havingValue = "true"
)
@AutoConfigureBefore(DataServicesCoreAutoconfiguration.class)
public class StaticValidationCodeConfiguration {

	/**
	 * Registers a validation code provider that loads static JavaScript-based validation rules.
	 *
	 * @return the {@link com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider} bean.
	 */
	@Bean
	public IValidationCodeProvider staticValidationCodeProvider() {
		return new StaticValidationCodeProvider("js/");
	}

	// I really do not understand why this is necessary, but it is.
	/**
	 * Provides an {@link com.mgmtp.a12.kernel.md.rt.api.IDocumentServiceConfig} to locate generated static model code.
	 *
	 * @return the {@link ExampleDocumentStaticServiceConfig} bean.
	 */
	@Bean
	public IDocumentServiceConfig exampleDocumentStaticServiceConfig() {
		return new ExampleDocumentStaticServiceConfig();
	}
}

