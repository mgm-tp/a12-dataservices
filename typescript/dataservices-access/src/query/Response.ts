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
import { JsonRpc2Response } from "../json-rpc/index.js";

import type { Query } from "./Query.js";

/**
 * Represents a JSON-RPC 2.0 response for a query.
 * @extends JsonRpc2Response
 */
export interface QueryJsonRpc2Response<
	Entries = unknown,
	Links = unknown,
	OtherResults = unknown
> extends JsonRpc2Response {
	/**
	 * The result of the query.
	 * @readonly
	 */
	readonly result: {
		/**
		 * The total number of items in the result set.
		 * @readonly
		 */
		readonly fullSize: number;

		/**
		 * The page specification for the current result set.
		 * @readonly
		 */
		readonly page: Query.Paging;

		/**
		 * Array of document tree results of type ROOT, i.e. result items of root query.
		 * @readonly
		 */
		readonly entries: Entries;

		/**
		 * Array of document tree results of type LINK or CHILD, i.e. result items of links.
		 * @readonly
		 */
		readonly links: Links;

		/**
		 * A map of other results for extended responses
		 * @readonly
		 */
		readonly otherResults: OtherResults;
	};
}

export namespace QueryJsonRpc2Response {
	export function isInstance(object: unknown): object is QueryJsonRpc2Response {
		return (
			JsonRpc2Response.ok.isInstance(object) &&
			!!object.result &&
			typeof object.result === "object" &&
			"entries" in object.result &&
			Array.isArray(object.result.entries) &&
			typeof object.result.fullSize === "number"
		);
	}

	/**
	 * Extracting the response typing from the query based on projection type.
	 */
	export type FromQuery<Q extends Query.QueryRoot> = ProjectionByName<Q>[Q["projectionName"]];

	interface ProjectionByName<Q extends Query.QueryRoot> {
		document: DocumentProjection<Q>;
		"document-graph": DocumentGraphProjection;
		cdd: CddProjection<Q>;
		exportCddCsv: ExportCddCsvProjection;
	}

	export type DocumentProjection<
		Q extends Query.QueryRoot,
		Entry = HasAggregation<Q> extends true ? AggregationEntry : DocumentEntry
	> = QueryJsonRpc2Response<
		IsQuerySingleDocumentConstraint<Q> extends true ? [Entry?] : Entry[],
		HasLinkConstraints<Q> extends true ? Link[] : []
	>;

	export type DocumentGraphProjection = QueryJsonRpc2Response<[DocumentEntry?], Link[]>;

	export type CddProjection<
		Q extends Query.QueryRoot,
		Entry = HasAggregation<Q> extends true ? AggregationEntry : DocumentEntry
	> = QueryJsonRpc2Response<
		IsQuerySingleDocumentConstraint<Q> extends true ? [Entry?] : Entry[],
		[]
	>;

	export type ExportCddCsvProjection = QueryJsonRpc2Response<
		undefined,
		undefined,
		{ downloadUrl: string }
	>;

	export interface BaseEntry<Document = unknown> {
		readonly type: Query.DocumentTreeNodeType.ROOT;
		readonly docRef: string;
		readonly documentModelName: string;

		readonly document: Document;
	}

	export type DocumentEntry = BaseEntry<GenericDocument>;
	export type AggregationEntry = BaseEntry<AggregationResult>;
	export type AggregationResult = [unknown, ...number[]];
	export type GenericDocument = object;

	export interface Link {
		readonly type: Query.DocumentTreeNodeType.LINK | Query.DocumentTreeNodeType.CHILD;

		readonly documentModelName: string;
		readonly docRef: string;
		readonly document: GenericDocument;

		readonly relationshipModel: string;
		readonly linkId: string;
		readonly depth: number;
		readonly sourceRole: string;
		readonly sourceDocRef: string;
		readonly targetRole: string;
		readonly targetDocRef: string;
	}

	/**
	 * Checks if the query has link constraints.
	 */
	export type HasLinkConstraints<Q extends Query.QueryRoot> = "links" extends keyof Q
		? true
		: false;

	/**
	 * Checks if the query has aggregation.
	 */
	export type HasAggregation<Q extends Query.QueryRoot> = "aggregation" extends keyof Q
		? true
		: false;

	/**
	 * Checks if the query has a single document reference constraint.
	 */
	export type IsQuerySingleDocumentConstraint<Q extends Query.QueryRoot> =
		"constraint" extends keyof Q
			? Q["constraint"] extends Query.DocRefExactMatchOperator
				? "field" extends keyof Q["constraint"]
					? true
					: false
				: false
			: false;
}
