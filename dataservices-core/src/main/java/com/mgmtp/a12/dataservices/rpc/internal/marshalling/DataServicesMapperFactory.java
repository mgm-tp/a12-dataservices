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
package com.mgmtp.a12.dataservices.rpc.internal.marshalling;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.cfg.MapperBuilder;

/**
 * Factory for creating pre-configured Jackson mappers for DataServices.
 *
 * All mappers are configured with field-based visibility (ignoring getters/setters/constructors)
 * so that Lombok-generated classes with `@Builder` and `@NonNull` fields work correctly.
 * Jackson annotations on fields (like `@JsonSerialize`, `@JsonDeserialize`) are properly respected.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class DataServicesMapperFactory {

	/**
	 * Configures the given mapper builder with DataServices field visibility settings.
	 *
	 * This method can be used when additional configuration is needed on the builder
	 * before calling `build()`. Works with any mapper type (JsonMapper, YAMLMapper, etc.).
	 *
	 * Example usage:
	 * [source,java]
	 * ----
	 * YAMLMapper yamlMapper = DataServicesMapperFactory.configureVisibility(YAMLMapper.builder())
	 *     .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
	 *     .build();
	 * ----
	 *
	 * @param builder The mapper builder to configure.
	 * @param <M> The mapper type.
	 * @param <B> The builder type.
	 * @return The configured builder for method chaining.
	 */
	public static <M extends tools.jackson.databind.ObjectMapper, B extends MapperBuilder<M, B>> B configureVisibility(B builder) {
		return builder
			.changeDefaultVisibility(vc -> vc
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
	}
}
