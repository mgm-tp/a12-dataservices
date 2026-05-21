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
/** @module Relationship/api */
export namespace Relationship {
	export interface LinkRef {
		readonly linkDescriptor: LinkDescriptor;
		readonly id: string;
	}

	export interface LinkRefResponse {
		readonly linkDescriptor: LinkDescriptorResponse;
		readonly id?: string | null;
	}

	export interface LinkDescriptor {
		readonly relationshipModel: string;
		readonly entities: LinkEntitySpec[];
		readonly predecessorLinkRef?: string | null;
		/* position will be ignored if predecessorLinkRef is not null
		 * if position is also null, then in server-side we consider TOP as its default value
		 */
		readonly position?: LinkPosition | null;
	}

	export interface LinkDescriptorResponse {
		readonly relationshipModel: string;
		readonly entities: LinkEntitySpecResponse[];
		readonly linkDocumentDocRef?: string;
		readonly predecessorLinkRef?: string | null;
		/* position will be ignored if predecessorLinkRef is not null
		 * if position is also null, then in server-side we consider TOP as its default value
		 */
		readonly position?: LinkPosition | null;
	}

	export interface LinkEntitySpec {
		readonly role: string;
		readonly docRef: string | null;
	}

	export interface LinkEntitySpecResponse extends LinkEntitySpec {
		readonly modelName: string;
	}

	/**
	 * This indicates if the desired link will be at the TOP or the BOTTOM in the order of the relationship.
	 * It will be ignored if predecessorLinkRef is being used and the TOP is the default value on
	 * the server-side when predecessorLinkRef is null
	 */
	export enum LinkPosition {
		TOP = "TOP",
		BOTTOM = "BOTTOM"
	}

	export interface Candidate {
		readonly linkRef: LinkRefResponse;
		readonly document: object;
	}

	export namespace Candidate {
		export function isInstance(obj: Candidate | object): obj is Candidate {
			return "linkRef" in obj && typeof obj.linkRef === "object";
		}
	}

	export interface LinkWithDocument {
		readonly linkRef: LinkRefResponse;
		readonly document: object;
	}

	export namespace LinkWithDocument {
		export function isInstance(obj: LinkWithDocument | object): obj is LinkWithDocument {
			return "linkRef" in obj && typeof obj.linkRef === "object";
		}
	}
}
