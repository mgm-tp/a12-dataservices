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
package com.mgmtp.a12.dataservices.export;

import java.util.List;

import com.mgmtp.a12.dataservices.rpc.query.FilterSpec;
import com.mgmtp.a12.dataservices.rpc.query.SortSpec;

import lombok.NonNull;

/**
 * @deprecated The interface is based on Solr API which is no longer supported. It will be removed in future versions.
 *
 * An implementation of the interface is used for exporting list of CDDs.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
public interface DocumentExportService {

	/**
	 * Exports a list of CDDs based on the specified filter, pagination, and format.
	 *
	 * @param baseDocumentModel The name of the base document model to be exported.
	 * @param filterSpec The criteria used to filter the documents for export.
	 * @param sortSpec The sorting details specifying the order of the results to export.
	 * @param format The format of the export. Currently, only `csv` is supported by default.
	 * @return The URL for downloading the exported document.
	 */
	String exportDocuments(@NonNull String baseDocumentModel, @NonNull FilterSpec filterSpec, @NonNull List<SortSpec> sortSpec, @NonNull String format);
}
