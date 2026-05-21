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
import type {
	LoadAttachmentUrlJsonRpc2,
	LoadThumbnailUrlsJsonRpc2
} from "../Attachment/attachment.js";
import type {
	AddDocumentJsonRpc2Response,
	DocumentJsonRpc2Request,
	GetDocumentJsonRpc2Response,
	ModifyJsonRpc2Response
} from "../Document/index.js";
import type { JsonRpc2Response, JsonRpc2ResponseOK } from "../json-rpc/index.js";
import type {
	ListModelsJsonRpc2Response,
	ListValidationsJsonRpc2Response,
	ModelJsonRpc2Request
} from "../model/index.js";
import type { QueryJsonRpc2Request } from "../query/Request.js";
import type { QueryJsonRpc2Response } from "../query/Response.js";
import type {
	RelationshipJsonRpc2request,
	RelationshipJsonRpc2response
} from "../Relationship/index.js";

/**
 * A helper type that aggregates all supported request methods and their corresponding request and response types.
 *
 * When adding a new request, follow the structure:
 *
 *  <NAME_OF_REQUEST_METHOD>: {
 *    Req: <TS_TYPE_FOR_THE_REQUEST>,
 *    Res: <TS_TYPE_FOR_THE_RESPONSE> // make sure to be as specific as possible
 *  }
 */
export type SupportedMethodMap = {
	QUERY: {
		Req: QueryJsonRpc2Request;
		Res: QueryJsonRpc2Response;
	};

	// Document-related methods
	ADD_DOCUMENT: {
		Req: DocumentJsonRpc2Request.AddJsonRpc2Request;
		Res: AddDocumentJsonRpc2Response;
	};
	MODIFY_DOCUMENT: {
		Req: DocumentJsonRpc2Request.ModifyJsonRpc2Request;
		Res: ModifyJsonRpc2Response;
	};
	PARTIAL_MODIFY_DOCUMENT: {
		Req: DocumentJsonRpc2Request.PartialModifyJsonRpc2Request;
		Res: ModifyJsonRpc2Response;
	};
	DELETE_DOCUMENT: {
		Req: DocumentJsonRpc2Request.DeleteJsonRpc2Request;
		Res: JsonRpc2Response;
	};
	MULTI_DELETE_DOCUMENTS: {
		Req: DocumentJsonRpc2Request.MultiDeleteJsonRpc2Request;
		Res: JsonRpc2Response;
	};
	COPY_DOCUMENT: {
		Req: DocumentJsonRpc2Request.CopyJsonRpc2Request;
		Res: JsonRpc2ResponseOK;
	};
	GET_DOCUMENT: {
		Req: DocumentJsonRpc2Request.GetDocumentJsonRpc2Request;
		Res: GetDocumentJsonRpc2Response;
	};
	VALIDATE_DOCUMENT: {
		Req: DocumentJsonRpc2Request.ValidateJsonRpc2Request;
		Res: JsonRpc2ResponseOK;
	};

	// Link/Relationship-related methods
	ADD_LINK: {
		Req: RelationshipJsonRpc2request.AddLinkJsonRpc2request;
		Res: RelationshipJsonRpc2response.AddLinkJsonRpc2Response;
	};
	MODIFY_LINK: {
		Req: RelationshipJsonRpc2request.ModifyLinkJsonRpc2request;
		Res: JsonRpc2ResponseOK;
	};
	DELETE_LINK: {
		Req: RelationshipJsonRpc2request.DeleteLinkJsonRpc2request;
		Res: JsonRpc2ResponseOK;
	};
	RELINK_DOCUMENT: {
		Req: RelationshipJsonRpc2request.RelinkDocumentJsonRpc2request;
		Res: JsonRpc2ResponseOK;
	};

	// Model-related methods
	LIST_MODELS_INTERNAL: {
		Req: ModelJsonRpc2Request.ListModelsJsonRpc2Request;
		Res: ListModelsJsonRpc2Response;
	};
	LIST_DOCUMENT_VALIDATION_CODES_INTERNAL: {
		Req: ModelJsonRpc2Request.ListValidationsJsonRpc2Request;
		Res: ListValidationsJsonRpc2Response;
	};

	// Attachment-related methods
	LOAD_ATTACHMENT_URL: {
		Req: LoadAttachmentUrlJsonRpc2.Request;
		Res: LoadAttachmentUrlJsonRpc2.Response;
	};
	LOAD_THUMBNAIL_URLS_INTERNAL: {
		Req: LoadThumbnailUrlsJsonRpc2.Request;
		Res: LoadThumbnailUrlsJsonRpc2.Response;
	};
};

/**
 * A helper type that holds all the request types as a union
 *
 * This is used for ensuring that only request types "known by Data Services" are used in the dispatcher.
 */
export type SupportedRequest = SupportedMethodMap[keyof SupportedMethodMap]["Req"];

/**
 * A helper type that looks up the correct response type for a given request type.
 *
 * This uses the type mapping defined in `SupportedMethod` to find the corresponding match.
 */
export type ResponseFor<Request extends SupportedRequest> = {
	[K in keyof SupportedMethodMap]: Request extends SupportedMethodMap["QUERY"]["Req"]
		? QueryJsonRpc2Response.FromQuery<Request["params"]["query"]>
		: Request extends SupportedMethodMap[K]["Req"]
			? SupportedMethodMap[K]["Res"]
			: never;
}[Request["method"]];
