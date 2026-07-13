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

import java.io.InputStream;
import java.util.List;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;
/**
 * An implementation of this interface is used for creating content from list of CDDs depend on a format type.
 *
 */
public interface IDocumentExporter {

	/**
	 * Indicates whether this exporter supports the given output format.
	 *
	 * @param format Output format identifier (e.g., "csv"); must not be `null`.
	 * @return `true` if the format is supported, otherwise `false`.
	 */
	boolean supports(@NonNull String format);

	/**
	 * Converts a list of CDDs to a serialized representation for export.
	 *
	 * @param documentModel The target {@link IDocumentModel}; may be `null` for model-agnostic exporters.
	 * @param documents The list of document payloads to export; must not be `null`.
	 * @return An {@link InputStream} providing the exported content.
	 */
	InputStream export(IDocumentModel documentModel, List<JsonNode> documents);

	/**
	 * Returns the content type (i.e., the mime type together with its character set) of the exported data
	 * in the form "<type>/<subtype>;charset=<charset>" (e.g., "text/plain;charset=UTF-8").
	 *
	 * @return The content type (mime type together with its character set) of the exported data.
	 */
	String getContentType();
}
