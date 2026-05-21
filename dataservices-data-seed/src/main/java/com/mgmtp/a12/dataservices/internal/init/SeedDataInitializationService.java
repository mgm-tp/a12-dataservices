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
package com.mgmtp.a12.dataservices.internal.init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.configuration.SeedDataProperties;
import com.mgmtp.a12.dataservices.initialization.InitializationService;
import com.mgmtp.a12.dataservices.internal.service.SeedDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SeedDataInitializationService implements InitializationService {

	public static final String ERROR_MESSAGE_TEMPLATE = "Application seed data initialization: {} Initialization is aborted at this step.";

	private final SeedDataService seedDataService;
	private final SeedDataProperties seedDataProperties;

	@Transactional
	@Override public void runInitialization() {
		try (InputStream inputStream = Files.newInputStream(Paths.get(seedDataProperties.getInitialization().getSeedData().getSeedFile().getPath()))) {
			tryDeleteAllData();
			tryImportAllData(inputStream);
		} catch (IOException e) {
			rethrowWithLog(new RuntimeException(e), "Cannot read initialization file.");
		}

	}

	private void tryDeleteAllData() {
		try {
			seedDataService.deleteAllData();
		} catch (RuntimeException e) {
			rethrowWithLog(e, "Delete all data failed.");
		}
	}

	private void tryImportAllData(InputStream inputStream) {
		String errorMessage = "Import all data failed.";
		try {
			seedDataService.importData(inputStream);
		} catch (RuntimeException e) {
			rethrowWithLog(e, errorMessage);
		} catch (IOException e) {
			rethrowWithLog(new RuntimeException(e), errorMessage);
		}
	}

	private static void rethrowWithLog(RuntimeException e, String message) throws RuntimeException {
		log.error(ERROR_MESSAGE_TEMPLATE, message);
		throw e;
	}
}
