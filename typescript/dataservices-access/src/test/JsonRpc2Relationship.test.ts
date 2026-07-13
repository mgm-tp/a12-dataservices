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
import { strictEqual } from "node:assert/strict";

import type { Relationship } from "../Relationship/index.js";
import {
	RelationshipJsonRpc2request,
	RelationshipJsonRpc2response
} from "../Relationship/index.js";

import { loadResource, omitAtPath } from "./utils/ObjectUtils.js";

suite("JSON-RPC Relationship Tests", () => {
	suite("Add Link Operation", () => {
		test("Request", () => {
			const rpcRequest = loadResource(
				"./src/test/resources/jsonrpc/request/jsonrpc_links_add_request.json"
			);

			strictEqual(
				RelationshipJsonRpc2request.isInstance(rpcRequest),
				true,
				"Add Link request was not recognized!"
			);

			strictEqual(rpcRequest.id, "AddLink");
			strictEqual(rpcRequest.method, "ADD_LINK");
			assertSizeAndModel(rpcRequest.params.linkDescriptor, "ContractBusinessPartner");
			assertLinkDescriptor(rpcRequest.params.linkDescriptor, 0, null, "Contract/1", "Contract");
			assertLinkDescriptor(
				rpcRequest.params.linkDescriptor,
				1,
				null,
				"BusinessPartner/2",
				"Partner"
			);
		});

		test("Response", () => {
			const rpcResponse = {
				jsonrpc: "2.0",
				id: "AddLink",
				result: {
					linkDescriptor: {
						relationshipModel: "ContractBusinessPartner",
						entities: [
							{
								role: "Contract",
								modelName: "Contract",
								docRef: "Contract/1"
							},
							{
								role: "Partner",
								modelName: "BusinessPartner",
								docRef: "BusinessPartner/2"
							}
						],
						predecessorLinkRef: null,
						position: "TOP"
					},
					id: "3"
				}
			};

			strictEqual(
				RelationshipJsonRpc2response.AddLinkJsonRpc2Response.isInstance(rpcResponse),
				true,
				"Add Link response was not recognized!"
			);

			const responseWithoutLinkDesc = omitAtPath(rpcResponse, ["result", "linkDescriptor"]);
			strictEqual(
				RelationshipJsonRpc2response.AddLinkJsonRpc2Response.isInstance(responseWithoutLinkDesc),
				false,
				"Add Link response without linkDescriptor was incorrectly recognized!"
			);

			const responseWithoutId = omitAtPath(rpcResponse, ["result", "id"]);
			strictEqual(
				RelationshipJsonRpc2response.AddLinkJsonRpc2Response.isInstance(responseWithoutId),
				true,
				"Add Link response without id was not recognized!"
			);
		});
	});

	suite("Modify Link Operation", () => {
		test("Request", () => {
			const rpcRequest = loadResource(
				"./src/test/resources/jsonrpc/request/jsonrpc_links_modify_request.json"
			);

			strictEqual(
				RelationshipJsonRpc2request.isInstance(rpcRequest),
				true,
				"Modify Link request was not recognized!"
			);

			strictEqual(rpcRequest.id, "ModifyLink");
			strictEqual(rpcRequest.method, "MODIFY_LINK");
			assertLinkRef(rpcRequest.params.linkRef, "ContractBusinessPartner", "3");
			assertLinkDescriptor(
				rpcRequest.params.linkRef.linkDescriptor,
				0,
				null,
				"Contract/1",
				"Contract"
			);
			assertLinkDescriptor(
				rpcRequest.params.linkRef.linkDescriptor,
				1,
				null,
				"BusinessPartner/2",
				"Partner"
			);
		});
	});

	suite("Delete Link Operation", () => {
		test("Request", () => {
			const rpcRequest = loadResource(
				"./src/test/resources/jsonrpc/request/jsonrpc_links_delete_request.json"
			);

			strictEqual(
				RelationshipJsonRpc2request.isInstance(rpcRequest),
				true,
				"Delete Link request was not recognized!"
			);

			strictEqual(rpcRequest.id, "DeleteLink");
			strictEqual(rpcRequest.method, "DELETE_LINK");
			assertLinkRef(rpcRequest.params.linkRef, "ContractBusinessPartner", "9");
			assertLinkDescriptor(
				rpcRequest.params.linkRef.linkDescriptor,
				0,
				null,
				"Contract/7",
				"Contract"
			);
			assertLinkDescriptor(
				rpcRequest.params.linkRef.linkDescriptor,
				1,
				null,
				"BusinessPartner/8",
				"Partner"
			);
		});
	});

	suite("Relink Link Operation", () => {
		test("Request", () => {
			const rpcRequest = loadResource(
				"./src/test/resources/jsonrpc/request/jsonrpc_links_relink_request.json"
			);

			strictEqual(
				RelationshipJsonRpc2request.isInstance(rpcRequest),
				true,
				"Relink Link request was not recognized!"
			);

			strictEqual(rpcRequest.id, "RelinkDocument");
			strictEqual(rpcRequest.method, "RELINK_DOCUMENT");
			assertSizeAndModel(rpcRequest.params.linkDescriptor, "ContractBusinessPartner");
			assertLinkDescriptor(rpcRequest.params.linkDescriptor, 0, null, "Contract/10", "Contract");
			assertLinkDescriptor(
				rpcRequest.params.linkDescriptor,
				1,
				null,
				"BusinessPartner/11",
				"Partner"
			);
		});

		test("Response", () => {
			const rpcResponse = {
				jsonrpc: "2.0",
				id: "RelinkDocument",
				result: {
					linkDescriptor: {
						relationshipModel: "ContractCoInsuredPartner",
						entities: [
							{
								role: "Contract",
								modelName: "Contract",
								docRef: "Contract/4"
							},
							{
								role: "Partner",
								modelName: "BusinessPartner",
								docRef: "BusinessPartner/5"
							}
						],
						predecessorLinkRef: null,
						position: "TOP"
					},
					id: "6"
				}
			};

			strictEqual(
				RelationshipJsonRpc2response.RelinkDocumentJsonRpc2Response.isInstance(rpcResponse),
				true,
				"Relink Link response was not recognized!"
			);

			const responseWithoutLinkDesc = omitAtPath(rpcResponse, ["result", "linkDescriptor"]);
			strictEqual(
				RelationshipJsonRpc2response.RelinkDocumentJsonRpc2Response.isInstance(
					responseWithoutLinkDesc
				),
				false,
				"Relink Link response without linkDescriptor was incorrectly recognized!"
			);

			const responseWithoutId = omitAtPath(rpcResponse, ["result", "id"]);
			strictEqual(
				RelationshipJsonRpc2response.RelinkDocumentJsonRpc2Response.isInstance(responseWithoutId),
				true,
				"Relink Link response without id was not recognized!"
			);
		});
	});
});

function assertLinkDescriptor(
	{ entities }: Relationship.LinkDescriptor,
	index: number,
	expModel: string | null,
	expDocRef: string,
	expRole: string
): void {
	const { docRef, role } = entities[index];
	strictEqual(expDocRef, docRef);
	strictEqual(expRole, role);
}

export function assertSizeAndModel(
	{ relationshipModel, entities }: Relationship.LinkDescriptor,
	model: string
): void {
	strictEqual(relationshipModel, model);
	strictEqual(entities.length, 2);
}

function assertLinkRef(
	{ linkDescriptor, id }: Relationship.LinkRef,
	model: string,
	expectedId: string
): void {
	assertSizeAndModel(linkDescriptor, model);
	strictEqual(id, expectedId);
}
