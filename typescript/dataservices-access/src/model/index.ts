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
import { isModelInstance, type Model } from "@com.mgmtp.a12.base/base-model-api";
import type { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import { ErrorPayload, ErrorResponse } from "../common/index.js";
import { JsonRpc2Response, JsonRpc2Request } from "../json-rpc/index.js";

/** @module models/api */

export namespace ModelsResult {
	export function isInstance(obj: unknown): obj is Model {
		return isModelInstance(obj);
	}
}

export type ModelsResponse = Model | ErrorPayload;

export namespace ModelsResponse {
	export function isInstance(obj: unknown): obj is Model | ErrorPayload {
		return isModelInstance(obj) || ErrorPayload.isInstance(obj);
	}
}

export interface ModelsRequest {
	id: string;
}

export namespace ModelsRequest {
	export function build({ id }: ModelsRequest): RestRequestPayload {
		return {
			method: "GET",
			relativeUrl: "/v2/models/" + id,
			customHeaders: [["Accept", "application/json"]]
		};
	}
}

export namespace CreateModelsRequest {
	export function build(model: Model): RestRequestPayload {
		return {
			method: "POST",
			relativeUrl: "/v2/models",
			body: JSON.stringify(model),
			customHeaders: [["Accept", "application/json"]]
		};
	}
}

export namespace UpdateModelsRequest {
	export function build(model: Model): RestRequestPayload {
		return {
			method: "PUT",
			relativeUrl: "/v2/models",
			body: JSON.stringify(model),
			customHeaders: [["Accept", "application/json"]]
		};
	}
}

export type DeleteModelsResponse = null | ErrorPayload;

export namespace DeleteModelsRequest {
	export function build({ id }: ModelsRequest): RestRequestPayload {
		return {
			method: "DELETE",
			relativeUrl: "/v2/models/" + id,
			customHeaders: [["Accept", "application/json"]]
		};
	}
}

export namespace GetModelsValidationCodeRequest {
	export function build(documentModelName: string): RestRequestPayload {
		return {
			method: "GET",
			relativeUrl: "/v2/models/" + documentModelName + "/validationCode",
			customHeaders: [["Accept", "application/javascript"]]
		};
	}
}

export type ModelJsonRpc2Request =
	| ModelJsonRpc2Request.ListModelsJsonRpc2Request
	| ModelJsonRpc2Request.ListValidationsJsonRpc2Request;

export namespace ModelJsonRpc2Request {
	/**
	 * List model subtree with the rootModelName parameter as the root.
	 * The model with rootModelName is scanned for model references recurring deeply through all nested references.
	 * The result is then filtered by the model type, and the resulting list of models is returned.
	 *
	 * @remarks
	 * The result should be cached on the client, and the operation should be called as little as possible,
	 * because of the performance reasons.
	 *
	 * @experimental
	 */
	export interface ListModelsJsonRpc2Request extends JsonRpc2Request {
		readonly method: "LIST_MODELS_INTERNAL";
		readonly params: {
			readonly modelNames: string[];
		};
	}

	export namespace ListModelsJsonRpc2Request {
		export function isInstance(obj: unknown): obj is ListModelsJsonRpc2Request {
			return (
				JsonRpc2Request.isInstance(obj) &&
				obj.method === "LIST_MODELS_INTERNAL" &&
				"modelNames" in obj.params
			);
		}
	}

	/**
	 * Return map of validation codes for requested document models.
	 * The key is document model name, value is the validation code.
	 *
	 * @remarks
	 * The returned map should be cached on the client, and the operation should be called as little as possible,
	 * because of the performance reasons.
	 *
	 * @experimental
	 */
	export interface ListValidationsJsonRpc2Request extends JsonRpc2Request {
		readonly method: "LIST_DOCUMENT_VALIDATION_CODES_INTERNAL";
		readonly params: {
			readonly documentModelNames: string[];
		};
	}
}

export namespace ListValidationsJsonRpc2Request {
	export function isInstance(
		obj: unknown
	): obj is ModelJsonRpc2Request.ListValidationsJsonRpc2Request {
		return (
			JsonRpc2Request.isInstance(obj) &&
			obj.method === "LIST_DOCUMENT_VALIDATION_CODES_INTERNAL" &&
			"documentModelNames" in obj.params
		);
	}
}

export interface ListModelsJsonRpc2Response extends JsonRpc2Response {
	readonly result: { models: Record<string, Model> };
}

export namespace ListModelsJsonRpc2Response {
	export function isInstance(obj: unknown): obj is ListModelsJsonRpc2Response {
		return JsonRpc2Response.ok.isInstance(obj) && "models" in obj.result;
	}
}

export interface ListValidationsJsonRpc2Response extends JsonRpc2Response {
	readonly result: { documentValidationCodes: Record<string, string> };
}

export namespace ListValidationsJsonRpc2Response {
	export function isInstance(obj: unknown): obj is ListValidationsJsonRpc2Response {
		return JsonRpc2Response.ok.isInstance(obj) && "documentValidationCodes" in obj.result;
	}
}

/**
 * Error code returned by the model REST API (POST/PUT `/v2/models`) when a unique constraint
 * definition in a Document Model is invalid — for example, when a field path referenced by a
 * constraint does not exist in the model.
 * Appears as the string value of {@link ErrorResponse.ErrorDetail.code} in the error response body.
 */
export const MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_CODE = "-32061";

export namespace ModelsErrorResponse {
	/**
	 * Type guard that narrows an unknown value to {@link ErrorResponse} and confirms it is a
	 * model unique-constraint validation error (error code `-32061`).
	 * Use this to distinguish this specific failure from other model save errors.
	 *
	 * @example
	 * ```typescript
	 * const response = await dispatcher.send(UpdateModelsRequest.build(model));
	 * if (ModelsErrorResponse.isModelUniqueConstraintValidationError(response)) {
	 *     // response.errorDetail.code === "-32061"
	 *     // response.shortMessage / response.longMessage carry the localized error details
	 * }
	 * ```
	 */
	export function isModelUniqueConstraintValidationError(obj: unknown): obj is ErrorResponse {
		return (
			ErrorResponse.isInstance(obj) &&
			obj.errorDetail?.code === MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_CODE
		);
	}
}
