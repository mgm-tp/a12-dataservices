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
import { deepStrictEqual, strictEqual } from "node:assert/strict";

import { configurationLoaded, DataServicesReducerMap } from "../monitor/redux/reducer.js";
import { DataServicesSelectors } from "../monitor/redux/selector.js";
import {
	DATASERVICES_CONFIG_SLICE,
	isConfiguration,
	type DataServicesConfiguration
} from "../monitor/redux/slice.js";

suite("Monitor", () => {
	const mockConfig: DataServicesConfiguration = {
		"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize": "4",
		"mgmtp.a12.dataservices.jsonRpc.maxMethodCallsPerRequest": "10",
		"mgmtp.a12.dataservices.query.maxQueryDepth": "5",
		"mgmtp.a12.dataservices.query.maxLinksSize": "100"
	};

	suite("Redux Slice", () => {
		suite("isConfiguration", () => {
			test("should validate correct configuration object", () => {
				strictEqual(isConfiguration(mockConfig), true);
			});

			test("should reject invalid configuration object", () => {
				strictEqual(isConfiguration(null), false);
				strictEqual(isConfiguration(undefined), false);
				strictEqual(isConfiguration("string"), false);
				strictEqual(isConfiguration(123), false);
				strictEqual(isConfiguration([]), false);
			});
		});
	});

	suite("Redux Reducer", () => {
		suite("reducer", () => {
			test("should return initial state when no action matches", () => {
				const reducer = DataServicesReducerMap[DATASERVICES_CONFIG_SLICE];
				const state = reducer(undefined, { type: "UNKNOWN_ACTION" });
				deepStrictEqual(state, {});
			});

			test("should update state when configurationLoaded is dispatched", () => {
				const reducer = DataServicesReducerMap[DATASERVICES_CONFIG_SLICE];

				const action = configurationLoaded(mockConfig);
				const state = reducer(undefined, action);
				deepStrictEqual(state.configuration, mockConfig);
			});

			test("should preserve state for unknown actions", () => {
				const reducer = DataServicesReducerMap[DATASERVICES_CONFIG_SLICE];

				const initialState = { configuration: mockConfig };
				const state = reducer(initialState, { type: "OTHER_ACTION" });
				deepStrictEqual(state, initialState);
			});

			test("should replace configuration completely", () => {
				const reducer = DataServicesReducerMap[DATASERVICES_CONFIG_SLICE];

				const newConfig: DataServicesConfiguration = {
					...mockConfig,
					"mgmtp.a12.dataservices.query.maxQueryDepth": "99"
				};
				const initialState = { configuration: mockConfig };
				const action = configurationLoaded(newConfig);
				const state = reducer(initialState, action);
				deepStrictEqual(state.configuration, newConfig);
			});
		});
	});

	suite("Redux Selector", () => {
		suite("configurationByKey", () => {
			test("should return configuration value for valid key", () => {
				const state = {
					[DATASERVICES_CONFIG_SLICE]: {
						configuration: mockConfig
					}
				};
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)(state);
				strictEqual(
					result,
					mockConfig["mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"]
				);
			});

			test("should return undefined for missing key", () => {
				const state = {
					[DATASERVICES_CONFIG_SLICE]: {
						configuration: { otherConfig: "value" }
					}
				};
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)(state);
				strictEqual(result, undefined);
			});

			test("should return undefined for invalid state", () => {
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)(null);
				strictEqual(result, undefined);
			});

			test("should return undefined for empty state", () => {
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)({});
				strictEqual(result, undefined);
			});
		});

		suite("selectConfigurationSlice", () => {
			test("should return configuration from valid state", () => {
				const state = {
					[DATASERVICES_CONFIG_SLICE]: {
						configuration: mockConfig
					}
				};
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)(state);
				strictEqual(
					result,
					mockConfig["mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"]
				);
			});

			test("should return undefined when slice is missing", () => {
				const state = { otherSlice: {} };
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)(state);
				strictEqual(result, undefined);
			});

			test("should return undefined when configuration is invalid", () => {
				const state = {
					[DATASERVICES_CONFIG_SLICE]: {
						configuration: "invalid"
					}
				};
				const result = DataServicesSelectors.configurationByKey(
					"mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize"
				)(state);
				strictEqual(result, undefined);
			});
		});
	});
});
