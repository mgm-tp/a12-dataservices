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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration;
import com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImporter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component public class ModelImporter extends AbstractImporter<Void, Void> {

	private final ModelBulkImporter modelBulkImporter;
	private final BulkImporterConfiguration configuration;

	public Void doImportLogic(Path modelsPath, Void metadata) {
		try {
			modelBulkImporter.doImport(modelsPath.toString(), configuration);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidInputException(ExceptionKeys.EXPORT_SEED_DATA_IMPORT_ERROR_KEY, "Cannot import models", e);
		}
		return null;
	}
}
