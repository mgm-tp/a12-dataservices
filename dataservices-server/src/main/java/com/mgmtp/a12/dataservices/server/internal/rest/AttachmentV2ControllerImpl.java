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
package com.mgmtp.a12.dataservices.server.internal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.server.internal.condition.AttachmentEndpointEnabledCondition;
import com.mgmtp.a12.dataservices.server.uaa.SecuredController;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * This API provides the ability to create attachments V2 (potentially with thumbnails).
 *
 * @version 2.0
 * @title Attachments REST API V2
 * @topic Attachments
 */
@Slf4j
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/v2/attachment")
@Conditional(AttachmentEndpointEnabledCondition.class)
@RestController @SecuredController public class AttachmentV2ControllerImpl implements ExceptionKeys, AuthConstants {

	@Autowired public AttachmentV2ControllerImpl(AttachmentService attachmentService, AttachmentMapper attachmentMapper) {
		this.attachmentService = attachmentService;
		this.attachmentMapper = attachmentMapper;
	}

	private final AttachmentService attachmentService;
	private final AttachmentMapper attachmentMapper;

	/**
	 * Endpoint allowing the upload of attachments. Big and small thumbnails are generated if the attachment is of type: JPEG, PNG, BMP, WBMP, GIF or SVG.
	 * An attachment can also be of text type (e.g. JSON, XML, TXT).
	 * NOTE: SVG is only supported for Thumbnailator. Enabling `mgmtp.a12.dataservices.attachments.thumbnail.optimization.url.enabled` would return
	 * an empty url for SVG.
	 *
	 * @param annotations List of attachment annotations.
	 * @param content Attachment content.
	 * @param filename Desired filename of attachment.
	 * @param documentModelName Document model name of uploaded document.
	 * @param pathToField Not yet implemented, but it is mandatory parameter. Empty string could be used.
	 * @return Information about uploaded attachment.
	 * @title Upload Attachment as Stream V2
	 * @headers Accept:: application/json
	 * @authorizationScope Model Read
	 * @authorizationScope Attachment Upload
	 * @responseSuccess 200 OK:: Attachment has been uploaded.
	 *
	 */
	@PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.ALL_VALUE })
	public AttachmentHeaderSpec upload(@RequestBody InputStreamResource content, @RequestParam("filename") String filename,
		@RequestParam("documentModelName") String documentModelName, @RequestParam("pathToField") String pathToField,
		@RequestParam(required = false, name = "annotations") String[] annotations) {

		try (InputStream is = content.getInputStream()) {
			AttachmentHeader attachmentHeader = attachmentService.createAttachment(is, filename, documentModelName, pathToField,
				Optional.ofNullable(annotations).stream()
					.flatMap(Arrays::stream)
					.filter(StringUtils::isNotBlank)
					.map(this::parseAnnotation)
					.filter(Objects::nonNull)
					.toList());
			return attachmentMapper.toHeaderSpec(
				attachmentHeader,
				attachmentService.findThumbnailUrl(attachmentHeader.getAttachmentId(), ThumbnailType.BIG).map(AttachmentUrl::getLocation).orElse(null),
				attachmentService.findThumbnailUrl(attachmentHeader.getAttachmentId(), ThumbnailType.SMALL).map(AttachmentUrl::getLocation).orElse(null)
			);
		} catch (IOException e) {
			throw new UnexpectedException(e).withAnonymityMessage("Upload attachment failed.");
		}
	}

	private AttachmentAnnotation parseAnnotation(@NonNull String annotation) {
		String[] split = annotation.split(":", 2);
		if (split.length < 1) {
			return null;
		} else {
			return new AttachmentAnnotation(split[0], split.length > 1 ? split[1] : "");
		}
	}
}
