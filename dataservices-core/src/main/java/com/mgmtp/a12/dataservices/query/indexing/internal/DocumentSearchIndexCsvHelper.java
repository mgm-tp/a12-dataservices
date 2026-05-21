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
package com.mgmtp.a12.dataservices.query.indexing.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.jsonb.DocumentSearchEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentSearchIndexCsvHelper {
	public static byte[] getDocumentSearchFieldsAsCsv(List<DocumentSearchEntity> documentFieldEntities) throws IOException {
		StringWriter stringWriter = new StringWriter();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema.Builder builder = CsvSchema.builder().setUseHeader(false);
		DocumentSearchIndexHelper.getColumnNames(DocumentSearchIndexBehaviour.DOCUMENTS_TARGET).forEach(builder::addColumn);
		CsvSchema schema = builder.build();

		for (DocumentSearchEntity fieldEntity : documentFieldEntities) {
			csvMapper.writer(schema).writeValue(stringWriter,
				new String[] {
					fieldEntity.getModelName(),
					fieldEntity.getDocRef(),
					fieldEntity.getOriginalValue(),
					fieldEntity.getValue(),
					fieldEntity.getSearchData() });
		}

		return stringWriter.toString().getBytes(StandardCharsets.UTF_8);
	}

	public static byte[] getRangeFieldsAsCSV(List<DocumentFieldEntity> fields) throws IOException {
		StringWriter stringWriter = new StringWriter();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema.Builder builder = CsvSchema.builder().setUseHeader(false);
		DocumentSearchIndexHelper.getColumnNames(DocumentSearchIndexBehaviour.FIELDS_TARGET).forEach(builder::addColumn);
		CsvSchema schema = builder.build();

		for (DocumentFieldEntity fieldEntity : fields) {
			String repetitions = Arrays.stream(fieldEntity.getRepetitions()).mapToObj(String::valueOf).collect(Collectors.joining(", "));
			csvMapper.writer(schema).writeValue(stringWriter,
				new String[] {
					fieldEntity.getModelName(),
					fieldEntity.getDocRef(),
					fieldEntity.getFieldName(),
					"{" + repetitions + "}",
					Objects.toString(fieldEntity.getFieldTypeId(), ""),
					Objects.toString(fieldEntity.getFieldType(), ""),
					Objects.toString(fieldEntity.getValue(), ""),
					Objects.toString(fieldEntity.getTypedValue(), ""),
					fieldEntity.getNumberValue() == null ? "" : fieldEntity.getNumberValue().toPlainString(),
					Objects.toString(fieldEntity.getTimestampValue(), ""),
					fieldEntity.getTsRangeValue() == null ? "" : fieldEntity.getTsRangeValue().asString(),
					fieldEntity.getSource()
				}
			);
		}

		return stringWriter.toString().getBytes(StandardCharsets.UTF_8);
	}
}
