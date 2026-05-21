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
package com.mgmtp.a12.dataservices.client.attachment;

import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.connector.rest.RestPostConnector;
import com.mgmtp.a12.connector.rest.RestServerRequest;
import com.mgmtp.a12.connector.rest.UrlBuilderSupport;
import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;

import lombok.NonNull;

/**
 * REST implementation of {@link AttachmentClientV2} which uses HTTP client with Basic Auth to issue HTTP request to server.
 */
public class RestAttachmentV2Client implements AttachmentClientV2 {

	private static final String ATTACHMENT_CONTEXT = "attachment";

	private final RestPostConnector postConnector;
	private final UrlBuilderSupport urlBuilderSupport;

	/**
	 * Creates a REST-based attachment client targeting the given base URL.
	 *
	 * @param baseUrl the server base URL; must not be `null`.
	 * @param postConnector the POST connector used to send upload requests; must not be `null`.
	 */
	public RestAttachmentV2Client(@NonNull String baseUrl, @NonNull RestPostConnector postConnector) {
		this.postConnector = postConnector;
		urlBuilderSupport = UrlBuilderSupport.withBaseUrl(baseUrl, "v2", ATTACHMENT_CONTEXT);
	}

	/**
	 * Uploads an attachment to a document field. The parameters `filename`, `documentModelName`, `pathToField`,
	 * and `annotations` are automatically URL-encoded when building the request URI.
	 *
	 * @param content the binary content stream; must not be `null`.
	 * @param filename the original filename used for server-side metadata; must not be `null`.
	 * @param documentModelName the model name of the target document; must not be `null`.
	 * @param pathToField the JSON path to the attachment field within the document; must not be `null`.
	 * @param annotations optional annotations to attach to the file; may be `null` or empty.
	 * @return the created attachment header specification returned by the server.
	 */
	@Override public AttachmentHeaderSpec uploadAttachment(@NonNull InputStream content, @NonNull String filename, @NonNull String documentModelName, @NonNull String pathToField,
														   Collection<AttachmentAnnotation> annotations) {
		UriComponentsBuilder uriComponentsBuilder = urlBuilderSupport.createBuilder()
			.queryParam("filename", filename)
			.queryParam("documentModelName", documentModelName)
			.queryParam("pathToField", pathToField);

		Optional.ofNullable(annotations).stream()
			.flatMap(Collection::stream)
			.map(a -> String.format("%s:%s", a.getName(), a.getValue()))
			.forEach(a -> uriComponentsBuilder.queryParam("annotations", a));
		return postConnector.callServer(uriComponentsBuilder.build().encode().toUri(),
				RestServerRequest.withPayload(new InputStreamResource(content)).withContentType(MediaType.APPLICATION_OCTET_STREAM), AttachmentHeaderSpec.class)
			.getData();
	}
}
