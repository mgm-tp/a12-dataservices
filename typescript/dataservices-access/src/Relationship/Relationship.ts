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
import {
	isArray,
	isNull,
	isObject,
	isOptionalFieldOfType,
	isString
} from "../common/TypeGuardUtils.js";

/** @module Relationship/api */
export namespace Relationship {
	export interface LinkRef {
		readonly linkDescriptor: LinkDescriptor;
		readonly id: string;
	}

	export namespace LinkRef {
		export function isInstance(obj: unknown): obj is LinkRef {
			return (
				isObject(obj) &&
				"linkDescriptor" in obj &&
				LinkDescriptor.isInstance(obj.linkDescriptor) &&
				"id" in obj &&
				isString(obj.id)
			);
		}
	}

	export interface LinkRefResponse {
		readonly linkDescriptor: LinkDescriptorResponse;
		readonly id?: string | null;
		readonly sourceRank?: string | null;
		readonly targetRank?: string | null;
	}

	export namespace LinkRefResponse {
		export function isInstance(obj: unknown): obj is LinkRefResponse {
			return (
				isObject(obj) &&
				"linkDescriptor" in obj &&
				LinkDescriptorResponse.isInstance(obj.linkDescriptor)
			);
		}
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

	export namespace LinkDescriptor {
		export function isInstance(obj: unknown): obj is LinkDescriptor {
			return (
				isObject(obj) &&
				"relationshipModel" in obj &&
				isString(obj.relationshipModel) &&
				"entities" in obj &&
				isArray(obj.entities, LinkEntitySpec.isInstance) &&
				isOptionalFieldOfType(
					obj,
					"predecessorLinkRef",
					value => isNull(value) || isString(value)
				) &&
				isOptionalFieldOfType(
					obj,
					"position",
					value => isNull(value) || LinkPosition.isInstance(value)
				)
			);
		}
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

	export namespace LinkDescriptorResponse {
		export function isInstance(obj: unknown): obj is LinkDescriptorResponse {
			return (
				isObject(obj) &&
				"relationshipModel" in obj &&
				isString(obj.relationshipModel) &&
				"entities" in obj &&
				isArray(obj.entities, LinkEntitySpecResponse.isInstance) &&
				isOptionalFieldOfType(obj, "linkDocumentDocRef", isString) &&
				isOptionalFieldOfType(
					obj,
					"predecessorLinkRef",
					value => isNull(value) || isString(value)
				) &&
				isOptionalFieldOfType(
					obj,
					"position",
					value => isNull(value) || LinkPosition.isInstance(value)
				)
			);
		}
	}

	export interface LinkEntitySpec {
		readonly role: string;
		readonly docRef: string;
	}

	export namespace LinkEntitySpec {
		export function isInstance(obj: unknown): obj is LinkEntitySpec {
			return (
				isObject(obj) &&
				"role" in obj &&
				isString(obj.role) &&
				"docRef" in obj &&
				isString(obj.docRef)
			);
		}
	}

	export interface LinkEntitySpecResponse extends LinkEntitySpec {
		readonly modelName: string;
	}

	export namespace LinkEntitySpecResponse {
		export function isInstance(obj: unknown): obj is LinkEntitySpecResponse {
			return LinkEntitySpec.isInstance(obj) && "modelName" in obj && isString(obj.modelName);
		}
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

	export namespace LinkPosition {
		export function isInstance(obj: unknown): obj is LinkPosition {
			return obj === LinkPosition.TOP || obj === LinkPosition.BOTTOM;
		}
	}

	export interface Candidate {
		readonly linkRef: LinkRefResponse;
		readonly document: object;
	}

	export namespace Candidate {
		export function isInstance(obj: unknown): obj is Candidate {
			return (
				isObject(obj) &&
				"linkRef" in obj &&
				LinkRefResponse.isInstance(obj.linkRef) &&
				"document" in obj &&
				isObject(obj.document)
			);
		}
	}

	export interface LinkWithDocument {
		readonly linkRef: LinkRefResponse;
		readonly document: object;
	}

	export namespace LinkWithDocument {
		export function isInstance(obj: unknown): obj is LinkWithDocument {
			return (
				isObject(obj) &&
				"linkRef" in obj &&
				LinkRefResponse.isInstance(obj.linkRef) &&
				"document" in obj &&
				isObject(obj.document)
			);
		}
	}
}
