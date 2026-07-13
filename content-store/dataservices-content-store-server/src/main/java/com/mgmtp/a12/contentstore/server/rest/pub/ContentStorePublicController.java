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
package com.mgmtp.a12.contentstore.server.rest.pub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.PublicControllersEnabledCondition;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.content.ContentStream;
import com.mgmtp.a12.contentstore.server.rest.RestConstants;
import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import lombok.RequiredArgsConstructor;

/**
 * This API provides the ability to download content.
 *
 * @version 1.0
 * @title Content Store Public Controller REST API
 * @topic Content Store Public HTTP API
 */
@RequiredArgsConstructor
@Conditional(PublicControllersEnabledCondition.class)
@RequestMapping("#{@contentStoreProperties.server.contextPath}/download")
@RestController public class ContentStorePublicController {

	private final ContentStoreService contentStoreService;
	private final ContentStoreProperties contentStoreProperties;

	/**
	 * Endpoint allows downloading a content file.
	 *
	 * @param id contentId of public content or ticketId of private content.
	 * @param filename The content file name of the response, if it's empty the id would be used.
	 * @param cacheDuration The duration of cache config in seconds and 0 is disabled cache, negative number is maximum cache duration - 2147483647 seconds. Default value can be configure through key `mgmtp.a12.dataservices.contentstore.cache.timeout`.
	 * @return {@link ResponseEntity#ok()} in case of success. Response body is {@link InputStreamResource}.
	 * @responseSuccess 200 OK:: File is ready to be downloaded.
	 * @title Get Content
	 */
	@GetMapping(RestConstants.ID)
	public ResponseEntity<InputStreamResource> downloadContent(
		@PathVariable(RestConstants.ID_PARAM) String id,
		@RequestParam(name = RestConstants.FILENAME_PARAM, required = false) Optional<String> filename,
		@RequestParam(name = RestConstants.CACHE_DURATION_PARAM, required = false) Integer cacheDuration
	) {
		if (cacheDuration == null) {
			cacheDuration = contentStoreProperties.getCache().getTimeout();
		}
		ContentStream content = contentStoreService.getContent(id);
		return makeResponseEntity(content, content.getContentSupplier(), filename.orElse(id), cacheDuration);
	}

	private static ResponseEntity<InputStreamResource> makeResponseEntity(ContentStream content, Supplier<InputStream> inputStreamSupplier, String filename,
		int cacheDuration) {
		try (InputStream inputStream = inputStreamSupplier.get()) {
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

			return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(content.getContentType()))
				.headers(responseHeaders)
				.cacheControl(makeCacheControl(content.isPublic(), cacheDuration))
				.body(new InputStreamResource(inputStream));
		} catch (IOException e) {
			throw new UnexpectedException(e.getMessage()).withAnonymityMessage("Response entity creation failed.");
		}
	}

	private static CacheControl makeCacheControl(boolean isPublic, int cacheDuration) {
		if (cacheDuration < 0) {
			cacheDuration = Integer.MAX_VALUE;
		}

		return isPublic ?
			CacheControl.maxAge(cacheDuration, TimeUnit.SECONDS).cachePublic().mustRevalidate() :
			CacheControl.noStore().cachePrivate().mustRevalidate();
	}
}
