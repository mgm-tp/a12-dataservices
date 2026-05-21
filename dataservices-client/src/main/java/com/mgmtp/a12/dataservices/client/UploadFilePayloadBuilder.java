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
package com.mgmtp.a12.dataservices.client;

import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.mgmtp.a12.connector.rest.RestServerRequest;

/**
 * Upload File Payload Builder class. Builds a {@link RestServerRequest} with a multipart/form-data payload containing
 * the given file resource.
 *
 */
public class UploadFilePayloadBuilder {

	private static final String HEADER_ATTACHMENT_FILE = "form-data; name=\"%s\"; filename=\"%s\";";
	private static final String HEADER_CONTENT_ID = "Content-ID";

	private Resource resource;
	private String contentType;
	private String resourceFileName = "inputFile";

	private UploadFilePayloadBuilder(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Creates a builder with the default resource file name `inputFile`.
	 *
	 * @param resource the file resource to upload; must not be `null`.
	 * @return a new builder instance.
	 */
	public static UploadFilePayloadBuilder with(Resource resource) {
		return new UploadFilePayloadBuilder(resource);
	}

	/**
	 * Sets the content type for the upload request.
	 *
	 * @param contentType the MIME type to use for the multipart payload; may be `null`.
	 * @return this builder for fluent configuration.
	 */
	public UploadFilePayloadBuilder withContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/**
	 * Sets the logical file name used in multipart headers and as `Content-ID`.
	 *
	 * @param resourceFileName the name to use for the multipart part; must not be `null`.
	 * @return this builder for fluent configuration.
	 */
	public UploadFilePayloadBuilder withResourceFileName(String resourceFileName) {
		this.resourceFileName = resourceFileName;
		return this;
	}

	/**
	 * Builds a {@link RestServerRequest} with multipart/form-data containing the resource and appropriate headers.
	 *
	 * Adds `Content-Type` (if provided), `Content-Disposition` with filename, and `Content-ID`.
	 *
	 * @return the multipart request payload ready to be sent.
	 */
	public RestServerRequest<MultiValueMap<String, Object>> build() {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add(resourceFileName, resource);
		if (contentType != null) {
			body.add(HttpHeaders.CONTENT_TYPE, contentType);
		}
		body.add(HttpHeaders.CONTENT_DISPOSITION, String.format(HEADER_ATTACHMENT_FILE, resourceFileName, Optional.ofNullable(resource.getFilename()).orElse(resource.getDescription())));
		body.add(HEADER_CONTENT_ID, resourceFileName);

		return RestServerRequest.withPayload(body)
			.withContentType(MediaType.MULTIPART_FORM_DATA);
	}

}
