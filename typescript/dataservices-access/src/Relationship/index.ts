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
import { JsonRpc2Request, JsonRpc2Response } from "../json-rpc/index.js";

import { Relationship } from "./Relationship.js";

export * from "./isRelationshipModel.js";
export * from "./ModelGraph.js";
export * from "./Relationship.js";
export * from "./RelationshipModel.js";

/** @module Relationship/api */
export namespace RelationshipJsonRpc2response {
	export import LinkDescriptor = Relationship.LinkDescriptor;
	export import LinkEntitySpec = Relationship.LinkEntitySpec;
	export import LinkEntitySpecResponse = Relationship.LinkEntitySpecResponse;
	export import LinkWithDocument = Relationship.LinkWithDocument;

	export interface AddLinkJsonRpc2Response extends JsonRpc2Response {
		readonly result: Relationship.LinkRefResponse;
	}

	export interface RelinkDocumentResult extends JsonRpc2Response {
		readonly result: Relationship.LinkRefResponse;
	}

	export namespace AddLinkJsonRpc2Response {
		export function isInstance(obj: unknown): obj is AddLinkJsonRpc2Response {
			return (
				JsonRpc2Response.ok.isInstance(obj) &&
				"result" in obj &&
				typeof obj.result === "object" &&
				"linkDescriptor" in obj.result &&
				typeof obj.result.linkDescriptor === "object" &&
				"id" in obj.result &&
				typeof obj.result.id === "string"
			);
		}
	}

	export namespace RelinkDocumentJsonRpc2Response {
		export function isInstance(obj: unknown): obj is RelinkDocumentResult {
			return (
				JsonRpc2Response.ok.isInstance(obj) &&
				"result" in obj &&
				typeof obj.result === "object" &&
				"linkDescriptor" in obj.result &&
				typeof obj.result.linkDescriptor === "object" &&
				"id" in obj.result &&
				typeof obj.result.id === "string"
			);
		}
	}
}

export type RelationshipJsonRpc2request =
	| RelationshipJsonRpc2request.AddLinkJsonRpc2request
	| RelationshipJsonRpc2request.ModifyLinkJsonRpc2request
	| RelationshipJsonRpc2request.DeleteLinkJsonRpc2request
	| RelationshipJsonRpc2request.RelinkDocumentJsonRpc2request;

export namespace RelationshipJsonRpc2request {
	export function isInstance(
		obj: RelationshipJsonRpc2request | object
	): obj is RelationshipJsonRpc2request {
		const relationship_operations = ["ADD_LINK", "MODIFY_LINK", "DELETE_LINK", "RELINK_DOCUMENT"];
		return JsonRpc2Request.isInstance(obj) && relationship_operations.some(op => op === obj.method);
	}

	export interface AddLinkJsonRpc2request extends JsonRpc2Request {
		readonly method: "ADD_LINK";
		readonly params: {
			linkDescriptor: Relationship.LinkDescriptor;
			linkDocument?: object;
		};
	}

	export interface DeleteLinkJsonRpc2request extends JsonRpc2Request {
		readonly method: "DELETE_LINK";
		readonly params: {
			linkRef: Relationship.LinkRef;
		};
	}

	export interface ModifyLinkJsonRpc2request extends JsonRpc2Request {
		readonly method: "MODIFY_LINK";
		readonly params: {
			linkRef: Relationship.LinkRef;
			linkDocument?: object;
		};
	}

	export interface RelinkDocumentJsonRpc2request extends JsonRpc2Request {
		readonly method: "RELINK_DOCUMENT";
		readonly params: {
			linkDescriptor: Relationship.LinkDescriptor;
			linkRef: string;
		};
	}
}
