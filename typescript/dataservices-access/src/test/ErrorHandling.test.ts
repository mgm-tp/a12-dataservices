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

import { ErrorResponse } from "../common/ErrorPayload.js";

import { loadResource } from "./utils/ObjectUtils.js";

suite("Error Response Tests", () => {
	suite("Assert Type Guard", () => {
		const errorResponse = {
			longMessage: {
				key: "error.message.long",
				default: "Error Long Message"
			},
			shortMessage: {
				key: "error.message.short",
				default: "Error Short Message"
			},
			operationId: "12345",
			level: "ERROR"
		};

		test("Assert instance", () => {
			strictEqual(ErrorResponse.isInstance(errorResponse), true);
		});

		test("Assert omitted fields", () => {
			const { level, ...responseWithoutLevel } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutLevel),
				false,
				"Without level should be invalid"
			);

			const { operationId, ...responseWithoutOperationId } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutOperationId),
				true,
				"Without operationId should be valid"
			);

			const { shortMessage, ...responseWithoutShortMessage } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutShortMessage),
				true,
				"Without shortMessage should be valid"
			);

			const { longMessage, ...responseWithoutLongMessage } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutLongMessage),
				true,
				"Without longMessage should be valid"
			);
		});
	});

	suite("Assert Messages", () => {
		const errorResponse = loadResource("./src/test/resources/error/errorResponse.json");

		strictEqual(ErrorResponse.isInstance(errorResponse), true);

		test("Short Message", () => {
			strictEqual(errorResponse.shortMessage?.key, "error.message.short");
			strictEqual(errorResponse.shortMessage?.default, "Error Short Message");
		});

		test("Long Message", () => {
			strictEqual(errorResponse.longMessage?.key, "error.message.long");
			strictEqual(errorResponse.longMessage?.default, "Error Long Message");
		});
	});
});
