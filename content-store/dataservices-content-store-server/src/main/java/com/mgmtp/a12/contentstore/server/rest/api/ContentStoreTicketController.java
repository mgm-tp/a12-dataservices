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

import java.util.Optional;

import javax.measure.MeasurementException;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.contentstore.DownloadUrlResponse;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.ApiControllersEnabledCondition;
import com.mgmtp.a12.contentstore.configuration.ContentStoreProperties;
import com.mgmtp.a12.contentstore.exception.ExceptionKeys;
import com.mgmtp.a12.contentstore.server.rest.RestConstants;
import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.contentstore.utils.Constants;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers;

import lombok.RequiredArgsConstructor;

/**
 * This API provides the ability to create a ticket for downloading private content.
 *
 * @version 1.0
 * @title Content Store Ticket Controller REST API
 * @topic Content Store Private HTTP API
 */
@RequiredArgsConstructor
@Conditional(ApiControllersEnabledCondition.class)
@RequestMapping("#{@contentStoreProperties.server.contextPath}/api/ticket")
@RestController public class ContentStoreTicketController {

	private final ContentStoreService contentStoreService;
	private final ContentStoreProperties contentStoreProperties;

	/**
	 * Endpoint that allows generating a download URL of private content.
	 *
	 * @param contentId contentId to get download url.
	 * @param duration Input string for transferring to seconds in long number, input case is insensitive.
	 * @return URL with registered ticketId for downloading content.
	 * @title Request Ticket to Download Private Content
	 * @responseSuccess 200 OK:: Return downloadable URL with ticket id. The client can use this URL to download the content and can add parameter "filename"
	 * to set the file name for the downloaded file.
	 */
	@GetMapping(RestConstants.CONTENT_ID)
	public ResponseEntity<DownloadUrlResponse> getTicket(@PathVariable(RestConstants.CONTENT_ID_PARAM) String contentId,
		@RequestParam(required = false) String duration) {
		// return link with ticket id
		String url = contentStoreService.requestContentUrl(contentId, getDuration(duration, contentStoreProperties.getTicketDuration()));
		return ResponseEntity.ok(new DownloadUrlResponse(url));
	}

	private static long getDuration(String duration, String defaultDuration) {
		String durationInput = Optional.ofNullable(duration).orElse(defaultDuration);
		try {
			return QuantityParsers.parseTimeQuantity(durationInput);
		} catch (MeasurementException ex) {
			throw new InvalidInputException(ExceptionKeys.INVALID_INPUT_ERROR_KEY, String.format(Constants.INVALID_INPUT_ERROR_PATTEN, durationInput));
		}
	}
}
