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
package com.mgmtp.a12.contentstore.server.rest;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Constants used for HTTP endpoints of the content store.
 */
@OnlyForUsage public interface RestConstants {

	/** Request parameter name for a content identifier. */
	String CONTENT_ID_PARAM = "contentId";
	/** Path variable name for a generic resource identifier. */
	String ID_PARAM = "id";
	/** Optional request parameter to override the download file name. */
	String FILENAME_PARAM = "filename";
	/** Optional request parameter specifying cache duration in seconds. */
	String CACHE_DURATION_PARAM = "cacheDuration";
	/** Request parameter indicating persistence type (`public` or `private`). */
	String PERSISTENT_TYPE_PARAM = "persistentType";
	/** URI template segment for the `contentId` path variable. */
	String CONTENT_ID = "/{" + CONTENT_ID_PARAM + "}";
	/** URI template segment for the `id` path variable. */
	String ID = "/{" + ID_PARAM + "}";
	/** Optional request parameter providing an external MIME type. */
	String MIME_TYPE_PARAM = "mimeType";

}

