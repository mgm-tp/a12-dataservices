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
const loadAttachmentHeaderRequest = {
	jsonrpc: "2.0",
	id: "loadAttachmentHeader",
	method: "LOAD_ATTACHMENT_HEADER",
	params: {
		attachmentId: "attachmentId11",
		docRef: "BusinessPartner/22"
	}
};

const loadAttachmentUrlRequest = {
	jsonrpc: "2.0",
	id: "LoadAttachmentUrl",
	method: "LOAD_ATTACHMENT_URL",
	params: {
		attachmentId: "Attachment1",
		docRef: "Contract/1"
	}
};

const loadThumbnailUrlRequest = {
	jsonrpc: "2.0",
	id: "loadThumbnailUrl",
	method: "LOAD_THUMBNAIL_URL",
	params: {
		attachmentId: "attachmentId11"
	}
};

const loadThumbnailUrlsRequest = {
	jsonrpc: "2.0",
	method: "LOAD_THUMBNAIL_URLS_INTERNAL",
	id: "LoadThumbnailUrlsInternal",
	params: {}
};

export {
	loadAttachmentHeaderRequest,
	loadThumbnailUrlRequest,
	loadThumbnailUrlsRequest,
	loadAttachmentUrlRequest
};
