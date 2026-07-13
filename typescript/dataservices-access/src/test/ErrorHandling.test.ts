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

import { ErrorPayload, ErrorResponse } from "../common/index.js";

suite("Error Response Tests", () => {
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
		level: "ERROR",
		errorDetail: {
			code: "ERROR_CODE",
			subsystem: "DataService",
			time: "2024-01-01T00:00:00Z"
		}
	};

	suite("Assert Type Guard", () => {
		test("Assert instance", () => {
			strictEqual(ErrorResponse.isInstance(errorResponse), true);
		});

		test("Assert Error Response - Missing level", () => {
			const { level, ...responseWithoutLevel } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutLevel),
				false,
				"Without level should be invalid"
			);
		});

		test("Assert Error Response - Invalid level", () => {
			const invalidLevelResponse = { ...errorResponse, level: "INVALID_LEVEL" };
			strictEqual(
				ErrorResponse.isInstance(invalidLevelResponse),
				false,
				"With invalid level should be invalid"
			);
		});

		test("Assert Error Response - Optional fields", () => {
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

			const invalidShortMessageResponse = { ...errorResponse, shortMessage: "Message" };
			strictEqual(
				ErrorResponse.isInstance(invalidShortMessageResponse),
				false,
				"Invalid shortMessage should be invalid"
			);

			const { longMessage, ...responseWithoutLongMessage } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutLongMessage),
				true,
				"Without longMessage should be valid"
			);

			const invalidLongMessageResponse = { ...errorResponse, longMessage: "Message" };
			strictEqual(
				ErrorResponse.isInstance(invalidLongMessageResponse),
				false,
				"Invalid longMessage should be invalid"
			);

			const { errorDetail, ...responseWithoutErrorDetail } = errorResponse;
			strictEqual(
				ErrorResponse.isInstance(responseWithoutErrorDetail),
				true,
				"Without errorDetail should be valid"
			);
		});
	});

	suite("Assert Localized Entry", () => {
		test("Localized Entry", () => {
			strictEqual(ErrorResponse.LocalizedEntry.isInstance(errorResponse.shortMessage), true);
		});

		test("Localized Entry - Missing key", () => {
			const { key, ...response } = errorResponse.shortMessage;
			strictEqual(ErrorResponse.LocalizedEntry.isInstance(response), false);
		});

		test("Localized Entry - Invalid key", () => {
			const invalidKeyResponse = { ...errorResponse.shortMessage, key: 123 };
			strictEqual(ErrorResponse.LocalizedEntry.isInstance(invalidKeyResponse), false);
		});

		test("Localized Entry - Missing default", () => {
			const { default: defaultValue, ...response } = errorResponse.shortMessage;
			strictEqual(ErrorResponse.LocalizedEntry.isInstance(response), false);
		});

		test("Localized Entry - Invalid default", () => {
			const invalidDefaultResponse = { ...errorResponse.shortMessage, default: 123 };
			strictEqual(ErrorResponse.LocalizedEntry.isInstance(invalidDefaultResponse), false);
		});
	});

	suite("Assert Error Detail", () => {
		test("Error Detail", () => {
			strictEqual(ErrorResponse.ErrorDetail.isInstance(errorResponse.errorDetail), true);
		});

		test("Error Detail - Missing code", () => {
			const { code, ...errorDetail } = errorResponse.errorDetail;
			strictEqual(
				ErrorResponse.ErrorDetail.isInstance(errorDetail),
				false,
				"Without code should be invalid"
			);
		});

		test("Error Detail - Missing Subsystem", () => {
			const { subsystem, ...errorDetailWithoutSubsystem } = errorResponse.errorDetail;
			strictEqual(
				ErrorResponse.ErrorDetail.isInstance(errorDetailWithoutSubsystem),
				false,
				"Without subsystem should be invalid"
			);
		});

		test("Error Detail - Missing Time", () => {
			const { time, ...errorDetailWithoutTime } = errorResponse.errorDetail;
			strictEqual(
				ErrorResponse.ErrorDetail.isInstance(errorDetailWithoutTime),
				false,
				"Without time should be invalid"
			);
		});

		test("Error Detail - Invalid Time", () => {
			strictEqual(
				ErrorResponse.ErrorDetail.isInstance({
					...errorResponse.errorDetail,
					time: {
						epochSecond: 1690000000,
						nano: 0
					}
				}),
				false,
				"With non-string time should be invalid"
			);
		});
	});

	suite("Error Payload Tests", () => {
		const errorPayload = {
			errorType: "ERROR_TYPE",
			message: "Error message"
		};

		test("Error Payload - Valid", () => {
			strictEqual(ErrorPayload.isInstance(errorPayload), true);
		});

		test("Error Payload - Missing errorType", () => {
			const { errorType, ...payloadWithoutErrorType } = errorPayload;
			strictEqual(
				ErrorPayload.isInstance(payloadWithoutErrorType),
				false,
				"Without errorType should be invalid"
			);
		});

		test("Error Payload - Missing message", () => {
			const { message, ...payloadWithoutMessage } = errorPayload;
			strictEqual(
				ErrorPayload.isInstance(payloadWithoutMessage),
				false,
				"Without message should be invalid"
			);
		});

		test("Error Payload - Invalid errorType", () => {
			const invalidErrorTypePayload = { ...errorPayload, errorType: 123 };
			strictEqual(
				ErrorPayload.isInstance(invalidErrorTypePayload),
				false,
				"With invalid errorType should be invalid"
			);
		});

		test("Error Payload - Invalid message", () => {
			const invalidMessagePayload = { ...errorPayload, message: 123 };
			strictEqual(
				ErrorPayload.isInstance(invalidMessagePayload),
				false,
				"With invalid message should be invalid"
			);
		});
	});
});
