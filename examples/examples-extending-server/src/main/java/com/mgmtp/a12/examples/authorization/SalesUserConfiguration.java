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
package com.mgmtp.a12.examples.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mgmtp.a12.uaa.authentication.user.LocalUserLoader;

/**
 * Sales User Configuration class. This class specifies additional properties of a Sales user.
 *
 */
@Configuration
@Profile(SalesUserConfiguration.DATASERVICES_EXAMPLE_AUTHORIZATION_ENV)
public class SalesUserConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(SalesUser.class);
	/**
	 * Spring profile that activates the Sales user authorization example environment.
	 */
	public static final String DATASERVICES_EXAMPLE_AUTHORIZATION_ENV = "dataservices-example-authorization_env";

	private final ObjectMapper usersDeserializer = new ObjectMapper(new YAMLFactory());

	/**
	 * Provides a loader for {@link SalesUser} instances deserialized from YAML resources.
	 * The loader returns null if the provided resource cannot be deserialized.
	 *
	 * @return a {@link LocalUserLoader} for {@link SalesUser}.
	 */
	@Bean public LocalUserLoader<SalesUser> localSalesUserLoader() {
		return new LocalUserLoader<>() {
			@Override public SalesUser loadUser(Resource resource) {
				try {
					return usersDeserializer.readValue(resource.getInputStream(), SalesUser.class);
				} catch (Exception e) {
					LOGGER.error(String.format("Unable to load user from resource [%s]", resource.getFilename()), e);
					return null;
				}
			}
		};
	}

}
