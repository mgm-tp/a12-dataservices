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
package com.mgmtp.a12.contentstore.server.rest.api;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.contentstore.ContentPersistenceResult;
import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.ApiControllersEnabledCondition;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.contentstore.server.rest.RestConstants;
import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This API provides the ability to upload, delete content, or request download URL for public content.
 *
 * @title Content Store Private Controller REST API
 * @topic Content Store Private HTTP API
 */
@Slf4j
@RequiredArgsConstructor
@Conditional(ApiControllersEnabledCondition.class)
@RequestMapping("#{@contentStoreProperties.server.contextPath}/api/content")
@RestController public class ContentStorePrivateController {

	protected final ContentStoreService contentStoreService;

	/**
	 * Endpoint that allows uploading of content file.
	 *
	 * @param contentId The contentId to save.
	 * @param content Content upload data.
	 * @param persistentType Persistent Type for Content, public or private.
	 * @param filename The name of content.
	 * @param mimeType The mime type of uploading content, this mime type will only be accepted
	 * if `mgmtp.a12.dataservices.contentstore.server.api.mimeType.trustExternalMimeType.enabled` is true.
	 * @return Information about uploaded content, if content is public then return.
	 * @title Upload Content
	 * @responseSuccess 200 OK:: Content has been uploaded to file system.
	 */
	@PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.ALL_VALUE })
	public ResponseEntity<ContentPersistenceResult> upload(@RequestBody InputStreamResource content,
		@RequestParam(RestConstants.CONTENT_ID_PARAM) String contentId,
		@RequestParam(RestConstants.PERSISTENT_TYPE_PARAM) String persistentType,
		@RequestParam(required = false, name = RestConstants.FILENAME_PARAM) String filename,
		@RequestParam(required = false, name = RestConstants.MIME_TYPE_PARAM) String mimeType) {
		try (InputStream is = content.getInputStream()) {
			return ResponseEntity.ok(contentStoreService.saveContent(contentId, persistentType, is, filename, mimeType));
		} catch (IOException e) {
			throw new UnexpectedException(e).withAnonymityMessage("Content upload failed.");
		}

	}

	/**
	 * Endpoint that allows deleting content by id.
	 *
	 * @param id Path variable for deleting content by id.
	 * @title Delete Content
	 * @responseSuccess 204 NO_CONTENT:: Content has been deleted.
	 */
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@DeleteMapping(RestConstants.ID)
	public void deleteContent(@PathVariable(RestConstants.ID_PARAM) String id) {
		contentStoreService.deleteById(id);
	}

	/**
	 * Endpoint for requesting downloadable URL for public content by id.
	 *
	 * @param id The public content id for requesting url.
	 * @return Downloadable url from content id.
	 * @responseSuccess 200 OK:: Return downloadable URL from content id. The client can add the parameter "filename" to set the file name for the downloaded file.
	 * @title Get Download URL
	 */
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE }, path = RestConstants.ID)
	public ResponseEntity<DownloadUrlResponse> getDownloadUrl(@PathVariable(RestConstants.ID_PARAM) String id) {
		return contentStoreService.findPublicContentUrl(id)
			.map(url -> ResponseEntity.ok(new DownloadUrlResponse(url)))
			.orElseThrow(
				() -> new NotFoundException(ExceptionKeys.CONTENT_NOT_FOUND_ERROR_KEY, Constants.CANNOT_FIND_CONTENT_BY_ID_PATTERN.formatted(id))
			);
	}

}
