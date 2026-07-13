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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.anonymizing.AsterixAnonymizer;
import com.mgmtp.a12.dataservices.export.internal.csv.CsvDocumentExporter;
import com.mgmtp.a12.dataservices.export.internal.helper.ExportHelper;
import com.mgmtp.a12.dataservices.export.internal.helper.Header;

import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import static org.mockito.Mockito.when;

public class CsvDocumentExporterTest extends AbstractDataServicesCoreTest {

	@Mock
	private ExportHelper exportHelper;

	private Anonymizer anonymizer = new AsterixAnonymizer();

	@DataProvider(name = "delimiters")
	Object[][] delimiters() {
		return new Object[][] { { ';' }, { '|' } };
	}

	@Test(dataProvider = "delimiters")
	void testExport_runSuccessfully(char delimiter) throws IOException {
		CsvDocumentExporter csvDocumentExporter = new CsvDocumentExporter(exportHelper, delimiter, anonymizer, StandardCharsets.UTF_8.name());
		ComposeDocumentModel composeDocumentModel = Mockito.mock(ComposeDocumentModel.class);
		List<JsonNode> documents = Stream.of(makeTestBusinessPartnerDsDocument(), makeTestBusinessPartnerDsDocument())
			.map(doc -> objectMapper.convertValue(doc.getKernelDocument(), JsonNode.class)).toList();

		Header header1 = new Header(RandomStringUtils.randomAlphabetic(7), null);
		Header header2 = new Header(RandomStringUtils.randomAlphabetic(10), null);
		List<Header> headers = List.of(header1, header2);
		List<Object> row1 = List.of(RandomStringUtils.randomAlphabetic(7), RandomStringUtils.randomAlphabetic(10));
		List<Object> row2 = List.of(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(6));

		when(exportHelper.collectHeaders(composeDocumentModel)).thenReturn(headers);
		when(exportHelper.getRows(documents, composeDocumentModel, headers)).thenReturn(List.of(row1, row2));

		try (InputStream result = csvDocumentExporter.export(composeDocumentModel, documents)) {
			Assert.assertEquals(result.readAllBytes(), createMockCsv(
					headers.stream().map(Header::getName).toList(),
					List.of(row1, row2),
					delimiter
				)
			);
		}
	}

	private byte[] createMockCsv(List<String> headers, List<List<Object>> datas, char delimiter) {
		CsvMapper mapper = new CsvMapper();
		CsvSchema.Builder schemaBuilder = CsvSchema.builder()
			.setUseHeader(true)
			.setColumnSeparator(delimiter);
		for (String header : headers) {
			schemaBuilder.addColumn(header);
		}
		List<List<Object>> data = new ArrayList<>(datas);
		CsvSchema csvSchema = schemaBuilder.build();
		return mapper.writer(csvSchema).writeValueAsBytes(data);
	}

}
