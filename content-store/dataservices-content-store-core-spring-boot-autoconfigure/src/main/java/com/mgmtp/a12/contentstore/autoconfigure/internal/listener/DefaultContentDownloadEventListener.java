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
package com.mgmtp.a12.contentstore.autoconfigure.internal.listener;

import org.springframework.core.annotation.Order;

import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultContentDownloadEventListener {

	/**
	 * This listener is default behavior for handling `ContentBeforeDownloadEvent`, from the beginning `ContentStream` in this event is initialized as not ready.
	 * In case we don't have any specific customization for `ContentStream`, this listener just simply set `ready` to `true`.
	 * Any other listeners that handle this event should set Order higher than this (default this listener has the lowest precedence).
	 * This default listener will finally set `ContentStream` to ready and notify waiting process.
	 *
	 * @param event {@link com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent}
	 */
	@Order
	@CommonDataServicesEventListener public void listenOnContentBeforeDownload(ContentBeforeDownloadEvent event) {
		log.debug("Receive ContentBeforeDownloadEvent for content: {}", event.getContentId());
		event.getContentStream().setReady();
	}
}
