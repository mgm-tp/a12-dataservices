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
package com.mgmtp.a12.dataservices.server.autoconfigure;

import java.io.IOException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.mgmtp.a12.dataservices.common.events.internal.ThreadCleanupEvent;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Servlet filter that publishes {@link ThreadCleanupEvent} before and after request processing.
 * It notifies listeners to clear thread-local resources around the filter chain.
 */
@RequiredArgsConstructor
@Component public class ThreadCleanupFilter extends GenericFilterBean {

	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Publishes a {@link ThreadCleanupEvent} before and after delegating to the filter chain.
	 * This allows listeners to clear thread-local state around request processing. TODO A12S-6446: Clarify contract (uncertain behavior).
	 *
	 * @param request the current {@link ServletRequest}; never null.
	 * @param response the {@link ServletResponse} to write to; never null.
	 * @param chain the {@link FilterChain} used to continue processing.
	 * @throws IOException if an I/O error occurs during filtering.
	 * @throws ServletException if the filter chain raises a servlet exception.
	 */
	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		applicationEventPublisher.publishEvent(new ThreadCleanupEvent());
		chain.doFilter(request, response);
		applicationEventPublisher.publishEvent(new ThreadCleanupEvent());
	}
}
