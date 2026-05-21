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
package com.mgmtp.a12.contentstore.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Configuration properties for Content Store client.
 */
@Data
@Validated
@ConfigurationProperties("mgmtp.a12.dataservices.contentstore.client")
@PropertySource("classpath:content-store-client-default.properties")
@Configuration public class ContentStoreClientProperties {

	@Valid private ClientConfiguration configuration = new ClientConfiguration();
	private Content content = new Content();

	/**
	 * @topic contentstore_client
	 */
	@Data
	public static class ClientConfiguration {

		/**
		 * This is the Content Store remote URL that Data Services will use to communicate with the Content Store HTTP APIs.
		 * Please note that if Content Store is running on a cluster, then the host should be the Load Balancer domain name or service name,
		 * which Data Services can access within the intranet.
		 */
		@NotBlank(message = "The content store server url must not be blank") private String remoteUrl = "";

	}

	/**
	 * @topic contentstore_client
	 */

	@Data
	public static class Content {

		/**
		 * Base URL to use as prefix of content relative download URL.
		 * If download URL is relative and this property is configured properly,
		 * Content Store will concatenate this property with relative download URL to have a full download URL.
		 *
		 * E.g:
		 *
		 * - baseUrl = "http://localhost:8080"
		 * - relativeUrl = "/cs/api/content/93ebef0f-b034-4547-afb9-2ab51ab314ba"
		 * - expected downloadUrl = "http://localhost:8080/cs/api/content/93ebef0f-b034-4547-afb9-2ab51ab314ba"
		 *
		 */
		private String baseUrl;
	}
}
