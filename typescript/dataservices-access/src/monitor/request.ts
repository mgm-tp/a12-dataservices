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
import type { Store } from "redux";

import type { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-connector";

import { Dispatcher } from "../dispatch/index.js";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { configurationLoaded, type DataServicesReducerMap } from "./redux/reducer.js";
import { isConfiguration } from "./redux/slice.js";

export function buildConfigurationRequest(): RestRequestPayload {
	return {
		method: "GET",
		relativeUrl: "/monitored-properties",
		customHeaders: [["Accept", "application/json"]]
	};
}

/**
 * Requests the Data Services configuration from the server and dispatches it to the given redux store.
 *
 * Requires the registration of the {@link DataServicesReducerMap}.
 */
export async function loadDataServicesConfiguration(store: Store): Promise<void> {
	try {
		const config = await Dispatcher.rest(buildConfigurationRequest(), isConfiguration);

		store.dispatch(configurationLoaded(config));
	} catch (error) {
		console.log("Failed to load Data Services configuration");
		console.log(error);
	}
}
