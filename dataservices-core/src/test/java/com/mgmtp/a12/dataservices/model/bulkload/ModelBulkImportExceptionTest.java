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
package com.mgmtp.a12.dataservices.model.bulkload;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.BaseError;
import com.mgmtp.a12.dataservices.exception.IntegrityException;

public class ModelBulkImportExceptionTest {

	@Test
	public void testProblemMessage() throws Exception {
		ModelBulkImportException ex = new ModelBulkImportException(
				List.of(
				new IntegrityException("key1", "message1", new NullPointerException("NPE1")), 
				new IntegrityException("key2", "message2", new NullPointerException("NPE2"))), "Bulk import reported problems: message1 and message2");
		String msg = ex.getMessage();
		Assert.assertTrue(msg.contains("message1") && msg.contains("message2"), "Problems should be listed inside message");
		List<BaseError> errors = ex.getErrors();
		Assert.assertTrue(errors != null && errors.size() == 2);
		Assert.assertTrue(errors.stream()
				.anyMatch(e -> e.getLongMessage().getKey().concat(e.getLongMessage().getDefaultMessage()).equals("key1message1")));
		Assert.assertTrue(errors.stream()
				.anyMatch(e -> e.getLongMessage().getKey().concat(e.getLongMessage().getDefaultMessage()).equals("key2message2")));
	}

}
