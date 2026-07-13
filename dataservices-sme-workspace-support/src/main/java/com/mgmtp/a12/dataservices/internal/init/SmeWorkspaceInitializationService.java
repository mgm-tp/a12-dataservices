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
import java.nio.file.Path;

import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.initialization.InitializationService;
import com.mgmtp.a12.dataservices.internal.configuration.SmeWorkspaceProperties;
import com.mgmtp.a12.dataservices.internal.service.SmeWorkspaceImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SmeWorkspaceInitializationService implements InitializationService {

	public static final String ERROR_MESSAGE_TEMPLATE = "Application SME Workspace initialization: {} Initialization is aborted at this step.";

	private final SmeWorkspaceImportService workspaceImportService;
	private final SmeWorkspaceProperties workspaceProperties;

	@Transactional
	@Override public void runInitialization() {
		try (InputStream inputStream = Files.newInputStream(Path.of(workspaceProperties.getInitialization().getSmeWorkspaceFile().getPath()))) {
			tryDeleteAllData();
			tryImportAllData(inputStream);
		} catch (IOException e) {
			rethrowWithLog(new RuntimeException(e), "Cannot read initialization file.");
		}

	}

	private void tryDeleteAllData() {
		try {
			workspaceImportService.deleteAllData();
		} catch (RuntimeException e) {
			rethrowWithLog(e, "Delete all data failed.");
		}
	}

	private void tryImportAllData(InputStream inputStream) {
		String errorMessage = "Import all data failed.";
		try {
			workspaceImportService.importData(inputStream);
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
