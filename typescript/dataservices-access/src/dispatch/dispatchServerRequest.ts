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
	ConnectorLocator,
	type RestRequestPayload,
	type RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector/lib/main/index.js";

import type { JsonRpc2ResponseOK } from "../json-rpc/index.js";
import { JsonRpc2Request, JsonRpc2Response } from "../json-rpc/index.js";

import type { ResponseFor, SupportedRequest } from "./ResponseTypings.js";
import { TypeGuards } from "./TypeGuards.js";

type TypeGuard<T> = (value: unknown) => value is T;

async function fetchServerRequest(request: RestRequestPayload): Promise<Response> {
	const response = await (
		ConnectorLocator.getInstance().getServerConnector() as RestServerConnector
	).fetchData(request);

	if (!response.ok) {
		throw new Error(response.statusText);
	}

	return response;
}

async function dispatchRpc<T extends JsonRpc2ResponseOK>(
	language: string,
	...requests: JsonRpc2Request[]
): Promise<T[]> {
	const request = JsonRpc2Request.build(requests);

	const requestWithLanguageHeader: typeof request = {
		...request,
		customHeaders: [...(request.customHeaders ?? []), ["Accept-Language", language]]
	};

	function responseChecker(value: unknown): value is JsonRpc2Response | JsonRpc2Response[] {
		return JsonRpc2Response.isInstance(value) || isJsonRpc2ResponseArray(value);
	}

	const response = await rest(requestWithLanguageHeader, responseChecker);

	if (!Array.isArray(response) && JsonRpc2Response.hasError(response)) {
		throw response;
	}

	if (Array.isArray(response) && JsonRpc2Response.hasErrors(response)) {
		const errors = response.filter(JsonRpc2Response.error.isInstance);
		throw errors;
	}

	return (Array.isArray(response) ? response : [response]) as T[];
}

function isJsonRpc2ResponseArray(value: unknown): value is JsonRpc2Response[] {
	return Array.isArray(value) && value.every(JsonRpc2Response.isInstance);
}

export async function rest<T>(
	request: RestRequestPayload,
	responseChecker: TypeGuard<T>
): Promise<T> {
	const response = await fetchServerRequest(request);
	const data = await response.json();

	if (!responseChecker(data)) {
		throw new Error("The server response does not match the given typeguard!");
	}

	return data;
}

export async function rpc<
	Requests extends (SupportedRequest | undefined)[],
	Responses = {
		[K in keyof Requests]: Requests[K] extends SupportedRequest
			? ResponseFor<Requests[K]>
			: ResponseFor<NonNullable<Requests[K]>> | undefined;
	}
>(language: string, requests: [...Requests]): Promise<Responses> {
	const nonNullableRequests = requests.filter(request => !!request);

	const responses = await dispatchRpc(language, ...nonNullableRequests);

	return requests.map(request => {
		if (!request) {
			return undefined;
		}

		const response = responses.find(r => r.id === request.id);
		if (!response) {
			throw new Error(`No response found for request ${request.id}`);
		}

		if (!TypeGuards[request.method](response)) {
			throw new Error(
				`Expect a response of type ${request.method}. Got: ${JSON.stringify(response)}`
			);
		}

		return response;
	}) as Responses;
}
