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
import { LoadAttachmentUrlJsonRpc2, LoadThumbnailUrlsJsonRpc2 } from "../Attachment/attachment.js";
import {
	AddDocumentJsonRpc2Response,
	GetDocumentJsonRpc2Response,
	ModifyJsonRpc2Response
} from "../Document/index.js";
import { JsonRpc2Response } from "../json-rpc/index.js";
import { ListModelsJsonRpc2Response, ListValidationsJsonRpc2Response } from "../model/index.js";
import { QueryJsonRpc2Response } from "../query/Response.js";
import { RelationshipJsonRpc2response } from "../Relationship/index.js";

import type { SupportedMethodMap } from "./ResponseTypings.js";

/**
 * A type that expresses an object where:
 * - the keys must be the names of supported methods
 * - the values must be the corresponding type guard functions for that method
 */
type MethodNameToTypeGuardMap = {
	[K in keyof SupportedMethodMap]: (obj: unknown) => obj is SupportedMethodMap[K]["Res"];
};

/**
 * An object that maps the name of each request method to its corresponding type guard function.
 *
 * NOTE: When adding new methods, ensure that their type guards are listed here!
 */
export const TypeGuards = {
	QUERY: QueryJsonRpc2Response.isInstance,
	ADD_DOCUMENT: AddDocumentJsonRpc2Response.isInstance,
	MODIFY_DOCUMENT: ModifyJsonRpc2Response.isInstance,
	PARTIAL_MODIFY_DOCUMENT: ModifyJsonRpc2Response.isInstance,
	DELETE_DOCUMENT: JsonRpc2Response.isInstance,
	MULTI_DELETE_DOCUMENTS: JsonRpc2Response.isInstance,
	COPY_DOCUMENT: JsonRpc2Response.ok.isInstance,
	GET_DOCUMENT: GetDocumentJsonRpc2Response.isInstance,
	VALIDATE_DOCUMENT: JsonRpc2Response.ok.isInstance,
	ADD_LINK: RelationshipJsonRpc2response.AddLinkJsonRpc2Response.isInstance,
	MODIFY_LINK: JsonRpc2Response.ok.isInstance,
	DELETE_LINK: JsonRpc2Response.ok.isInstance,
	RELINK_DOCUMENT: JsonRpc2Response.ok.isInstance,
	LIST_MODELS_INTERNAL: ListModelsJsonRpc2Response.isInstance,
	LIST_DOCUMENT_VALIDATION_CODES_INTERNAL: ListValidationsJsonRpc2Response.isInstance,
	LOAD_ATTACHMENT_URL: LoadAttachmentUrlJsonRpc2.Response.isInstance,
	LOAD_THUMBNAIL_URLS_INTERNAL: LoadThumbnailUrlsJsonRpc2.Response.isInstance
} satisfies MethodNameToTypeGuardMap;
