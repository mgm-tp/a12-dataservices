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
import type { Header } from "@com.mgmtp.a12.base/base-model-api";
import type { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import { isArray, isNull, isObject, isString } from "../common/TypeGuardUtils.js";

import type { RelationshipModel } from "./RelationshipModel.js";
import { isRelationshipModel } from "./isRelationshipModel.js";

/** @module ModelGraph/api */
export interface ModelGraph {
	readonly documentModels: ModelGraph.DocumentModel[];
	readonly composeDocumentModels: ModelGraph.ComposeDocumentModel[];
	readonly genericModels?: ModelGraph.OtherModel[];
	readonly relationshipModels: RelationshipModel[];
}

export namespace ModelGraph {
	export interface ModelGraphElement {
		readonly modelId: string;
		readonly displayLabels?: Header["labels"];
		readonly modelReferences?: Header["modelReferences"];
	}

	export namespace ModelGraphElement {
		export function isInstance(obj: unknown): obj is ModelGraphElement {
			return isObject(obj) && "modelId" in obj && isString(obj.modelId);
		}
	}

	export interface DocumentModel extends ModelGraphElement {
		readonly relations: string[] | null;
		readonly subTypes: string[] | null;
		readonly abstractModel?: boolean;
	}

	export namespace DocumentModel {
		export function isInstance(obj: unknown): obj is DocumentModel {
			return (
				ModelGraphElement.isInstance(obj) &&
				"relations" in obj &&
				(isArray(obj.relations, isString) || isNull(obj.relations)) &&
				"subTypes" in obj &&
				(isArray(obj.subTypes, isString) || isNull(obj.subTypes))
			);
		}
	}

	export interface ComposeDocumentModel extends ModelGraphElement {
		readonly rootDocumentModelId: string;
	}

	export namespace ComposeDocumentModel {
		export function isInstance(obj: unknown): obj is ComposeDocumentModel {
			return (
				ModelGraphElement.isInstance(obj) &&
				"rootDocumentModelId" in obj &&
				isString(obj.rootDocumentModelId)
			);
		}
	}

	export interface OtherModel extends ModelGraphElement {
		readonly type: string;
	}

	export namespace OtherModel {
		export function isInstance(obj: unknown): obj is OtherModel {
			return ModelGraphElement.isInstance(obj) && "type" in obj && isString(obj.type);
		}
	}

	export function build(includeModelReferences = false): RestRequestPayload {
		return {
			method: "GET",
			relativeUrl: "/modelgraph" + (includeModelReferences ? "?includeModelReferences=true" : ""),
			customHeaders: [["Accepts", "application/json"]]
		};
	}

	export function isInstance(obj: unknown): obj is ModelGraph {
		return (
			isObject(obj) &&
			"documentModels" in obj &&
			isArray(obj.documentModels, ModelGraph.DocumentModel.isInstance) &&
			"composeDocumentModels" in obj &&
			isArray(obj.composeDocumentModels, ModelGraph.ComposeDocumentModel.isInstance) &&
			"relationshipModels" in obj &&
			isArray(obj.relationshipModels, isRelationshipModel)
		);
	}
}
