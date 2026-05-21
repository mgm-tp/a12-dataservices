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
package com.mgmtp.a12.dataservices.export.internal.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.export.IDocumentExporter;
import com.mgmtp.a12.dataservices.export.internal.helper.ExportHelper;
import com.mgmtp.a12.dataservices.export.internal.helper.Header;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CsvDocumentExporter implements IDocumentExporter {

	private final ExportHelper exportHelper;
	private final Character delimiter;
	protected final Anonymizer anonymizer;
	private final String charset;

	public static final String CSV_TYPE = "csv";

	@Override
	public boolean supports(@NonNull String format) {
		return CSV_TYPE.equalsIgnoreCase(format);
	}

	@Override public InputStream export(IDocumentModel documentModel, List<JsonNode> documents) {
		try {
			List<Header> headers = exportHelper.collectHeaders(documentModel);
			CsvSchema.Builder schemaBuilder = CsvSchema.builder()
				.setUseHeader(true)
				.setColumnSeparator(delimiter);
			for (Header header : headers) {
				schemaBuilder.addColumn(header.getName(), getColumnType(header.getFieldType()));
			}

			List<List<Object>> data = exportHelper.getRows(documents, documentModel, headers);
			// We have to use intermediate OutputStreamWriter to apply charset other than UTF-8 (which is used by ObjectMapper#writeValueAsString by default)
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(buffer, charset)) {
				outputStreamWriter.write(new CsvMapper().writer(schemaBuilder.build()).writeValueAsString(data));
			}

			return new ByteArrayInputStream(buffer.toByteArray());
		} catch (IOException e) {
			log.error("Data export failed for json list with model {}", anonymizer.apply(documentModel.getContent().getDocumentModelInfo().getName()));
			throw new IntegrityException(ExceptionKeys.EXPORT_LIST_CDD_ERROR_KEY,
				"There has been issue exporting documents with json format");
		}
	}

	@Override public String getContentType() {
		return "text/csv; charset=" + charset;
	}

	private CsvSchema.ColumnType getColumnType(IFieldType fieldType) {
		return exportHelper.isNumeric(fieldType) ? CsvSchema.ColumnType.NUMBER : CsvSchema.ColumnType.STRING;
	}
}
