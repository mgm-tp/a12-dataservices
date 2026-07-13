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
package com.mgmtp.a12.examples.document.sequence.generator;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testng.annotations.Test;

import com.mgmtp.a12.examples.AbstractITBase;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static org.mockito.ArgumentMatchers.any;
import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.CONTRACT_DOCUMENT_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.CONTRACT_DOCUMENT_MODEL_PATH;

@TestPropertySource(properties = "com.mgmtp.a12.examples.documents.sequence-id-generator.enabled=true")
public class SequenceIdGeneratorIT extends AbstractITBase {

	@MockitoSpyBean private SequenceIdGenerator sequenceIdGenerator;

	@Test public void testDocumentReference() throws IOException {
		modelsFunctions.createModel(CONTRACT_DOCUMENT_MODEL_PATH);
		documentFunctions.createDocumentFromFileAndGetDocRef(CONTRACT_DOCUMENT_MODEL, CONTRACT_DOCUMENT_FILE);

		// verify custom bean was called
		Mockito.verify(sequenceIdGenerator, Mockito.times(1)).generateId(any(DocumentV2.class));
	}
}
