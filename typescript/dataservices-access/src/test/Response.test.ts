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

import { Query, QueryJsonRpc2Response } from "../query/index.js";

suite("Response Test", () => {
	suite("JSON-RPC Response Test", () => {
		const response = {
			jsonrpc: "2.0",
			id: "1",
			result: {
				fullSize: 100,
				page: {
					pageNumber: 1,
					pageSize: 10
				},
				entries: [],
				links: [],
				otherResults: {}
			}
		};

		test("QueryJsonRpc2Response Instance", () => {
			strictEqual(QueryJsonRpc2Response.isInstance(response), true);
		});

		test("QueryJsonRpc2Response Instance - Missing result", () => {
			const { result, ...responseWithoutResult } = response;
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithoutResult),
				false,
				"Check response without result!"
			);
		});

		test("QueryJsonRpc2Response Instance - Missing fullSize", () => {
			const { fullSize, ...resultWithoutFullSize } = response.result;
			const responseWithoutFullSize = { ...response, result: resultWithoutFullSize };
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithoutFullSize),
				false,
				"Check response without fullSize!"
			);
		});

		test("QueryJsonRpc2Response Instance - Missing page", () => {
			const { page, ...resultWithoutPage } = response.result;
			const responseWithoutPage = { ...response, result: resultWithoutPage };
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithoutPage),
				false,
				"Check response without page!"
			);
		});

		test("QueryJsonRpc2Response Instance - Missing entries", () => {
			const { entries, ...resultWithoutEntries } = response.result;
			const responseWithoutEntries = { ...response, result: resultWithoutEntries };
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithoutEntries),
				false,
				"Check response without entries!"
			);
		});

		test("QueryJsonRpc2Response Instance - Wrong entries", () => {
			const responseWithWrongEntries = {
				...response,
				result: { ...response.result, entries: "not an array" }
			};
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithWrongEntries),
				false,
				"Check response with wrong entries!"
			);
		});

		test("QueryJsonRpc2Response Instance - Missing links", () => {
			const { links, ...resultWithoutLinks } = response.result;
			const responseWithoutLinks = { ...response, result: resultWithoutLinks };
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithoutLinks),
				false,
				"Check response without links!"
			);
		});

		test("QueryJsonRpc2Response Instance - Wrong links", () => {
			const responseWithWrongLinks = {
				...response,
				result: { ...response.result, links: "not an array" }
			};
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithWrongLinks),
				false,
				"Check response with wrong links!"
			);
		});

		test("QueryJsonRpc2Response Instance - Missing otherResults", () => {
			const { otherResults, ...resultWithoutOtherResults } = response.result;
			const responseWithoutOtherResults = { ...response, result: resultWithoutOtherResults };
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithoutOtherResults),
				false,
				"Check response without otherResults!"
			);
		});

		test("QueryJsonRpc2Response Instance - Wrong otherResults", () => {
			const responseWithWrongOtherResults = {
				...response,
				result: { ...response.result, otherResults: "not an object" }
			};
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithWrongOtherResults),
				false,
				"Check response with wrong otherResults!"
			);
		});

		test("QueryJsonRpc2Response Instance - Wrong fullSize type", () => {
			const responseWithWrongFullSize = {
				...response,
				result: { ...response.result, fullSize: "100" }
			};
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithWrongFullSize),
				false,
				"Check response with wrong fullSize type!"
			);
		});

		test("QueryJsonRpc2Response Instance - Wrong page type", () => {
			const responseWithWrongPage = {
				...response,
				result: { ...response.result, page: "invalid" }
			};
			strictEqual(
				QueryJsonRpc2Response.isInstance(responseWithWrongPage),
				false,
				"Check response with wrong page type!"
			);
		});
	});

	suite("Base Entry Instance Test", () => {
		const baseEntry = {
			type: Query.DocumentTreeNodeType.ROOT,
			docRef: "docRef",
			documentModelName: "modelName",
			document: {}
		};

		test("BaseEntry Instance", () => {
			strictEqual(QueryJsonRpc2Response.BaseEntry.isInstance(baseEntry), true);
		});

		test("BaseEntry Instance - Missing type", () => {
			const { type, ...entryWithoutType } = baseEntry;
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithoutType),
				false,
				"Check BaseEntry without type!"
			);
		});

		test("BaseEntry Instance - Wrong type", () => {
			const entryWithWrongType = { ...baseEntry, type: "not ROOT" };
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithWrongType),
				false,
				"Check BaseEntry with wrong type!"
			);
		});

		test("BaseEntry Instance - Missing docRef", () => {
			const { docRef, ...entryWithoutDocRef } = baseEntry;
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithoutDocRef),
				false,
				"Check BaseEntry without docRef!"
			);
		});

		test("BaseEntry Instance - Missing documentModelName", () => {
			const { documentModelName, ...entryWithoutDocumentModelName } = baseEntry;
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithoutDocumentModelName),
				false,
				"Check BaseEntry without documentModelName!"
			);
		});

		test("BaseEntry Instance - Missing document", () => {
			const { document, ...entryWithoutDocument } = baseEntry;
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithoutDocument),
				false,
				"Check BaseEntry without document!"
			);
		});

		test("BaseEntry Instance - Wrong document", () => {
			const entryWithWrongDocument = { ...baseEntry, document: "not an object" };
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithWrongDocument),
				false,
				"Check BaseEntry with wrong document!"
			);
		});

		test("BaseEntry Instance - Wrong docRef type", () => {
			const entryWithWrongDocRef = { ...baseEntry, docRef: 123 };
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithWrongDocRef),
				false,
				"Check BaseEntry with wrong docRef type!"
			);
		});

		test("BaseEntry Instance - Wrong documentModelName type", () => {
			const entryWithWrongModelName = { ...baseEntry, documentModelName: true };
			strictEqual(
				QueryJsonRpc2Response.BaseEntry.isInstance(entryWithWrongModelName),
				false,
				"Check BaseEntry with wrong documentModelName type!"
			);
		});
	});

	suite("Link Instance Test", () => {
		const link = {
			type: Query.DocumentTreeNodeType.LINK,
			documentModelName: "modelName",
			docRef: "docRef",
			document: {},
			relationshipModel: "relationshipModel",
			linkId: "linkId",
			depth: 1,
			sourceRole: "sourceRole",
			sourceDocRef: "sourceDocRef",
			targetRole: "targetRole",
			targetDocRef: "targetDocRef"
		};

		test("Link Instance", () => {
			strictEqual(QueryJsonRpc2Response.Link.isInstance(link), true);
		});

		test("Link Instance - Valid CHILD type", () => {
			const linkWithChildType = { ...link, type: Query.DocumentTreeNodeType.CHILD };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithChildType),
				true,
				"Check Link with CHILD type!"
			);
		});

		test("Link Instance - Missing type", () => {
			const { type, ...linkWithoutType } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutType),
				false,
				"Check Link without type!"
			);
		});

		test("Link Instance - Wrong type", () => {
			const linkWithWrongType = { ...link, type: "not LINK or CHILD" };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithWrongType),
				false,
				"Check Link with wrong type!"
			);
		});

		test("Link Instance - Missing documentModelName", () => {
			const { documentModelName, ...linkWithoutDocumentModelName } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutDocumentModelName),
				false,
				"Check Link without documentModelName!"
			);
		});

		test("Link Instance - Missing docRef", () => {
			const { docRef, ...linkWithoutDocRef } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutDocRef),
				false,
				"Check Link without docRef!"
			);
		});

		test("Link Instance - Missing document", () => {
			const { document, ...linkWithoutDocument } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutDocument),
				false,
				"Check Link without document!"
			);
		});

		test("Link Instance - Wrong document", () => {
			const linkWithWrongDocument = { ...link, document: "not an object" };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithWrongDocument),
				false,
				"Check Link with wrong document!"
			);
		});

		test("Link Instance - Missing relationshipModel", () => {
			const { relationshipModel, ...linkWithoutRelationshipModel } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutRelationshipModel),
				false,
				"Check Link without relationshipModel!"
			);
		});

		test("Link Instance - Missing linkId", () => {
			const { linkId, ...linkWithoutLinkId } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutLinkId),
				false,
				"Check Link without linkId!"
			);
		});

		test("Link Instance - Missing depth", () => {
			const { depth, ...linkWithoutDepth } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutDepth),
				false,
				"Check Link without depth!"
			);
		});

		test("Link Instance - Missing sourceRole", () => {
			const { sourceRole, ...linkWithoutSourceRole } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutSourceRole),
				false,
				"Check Link without sourceRole!"
			);
		});

		test("Link Instance - Missing sourceDocRef", () => {
			const { sourceDocRef, ...linkWithoutSourceDocRef } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutSourceDocRef),
				false,
				"Check Link without sourceDocRef!"
			);
		});

		test("Link Instance - Missing targetRole", () => {
			const { targetRole, ...linkWithoutTargetRole } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutTargetRole),
				false,
				"Check Link without targetRole!"
			);
		});

		test("Link Instance - Missing targetDocRef", () => {
			const { targetDocRef, ...linkWithoutTargetDocRef } = link;
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithoutTargetDocRef),
				false,
				"Check Link without targetDocRef!"
			);
		});

		test("Link Instance - Wrong docRef type", () => {
			const linkWithWrongDocRef = { ...link, docRef: 456 };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithWrongDocRef),
				false,
				"Check Link with wrong docRef type!"
			);
		});

		test("Link Instance - Wrong relationshipModel type", () => {
			const linkWithWrongRelModel = { ...link, relationshipModel: 789 };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithWrongRelModel),
				false,
				"Check Link with wrong relationshipModel type!"
			);
		});

		test("Link Instance - Wrong linkId type", () => {
			const linkWithWrongLinkId = { ...link, linkId: null };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithWrongLinkId),
				false,
				"Check Link with wrong linkId type!"
			);
		});

		test("Link Instance - Wrong depth type", () => {
			const linkWithWrongDepth = { ...link, depth: "1" };
			strictEqual(
				QueryJsonRpc2Response.Link.isInstance(linkWithWrongDepth),
				false,
				"Check Link with wrong depth type!"
			);
		});
	});
});
