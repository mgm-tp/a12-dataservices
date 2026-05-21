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
package com.mgmtp.a12.examples.attachment.virus.scan.clam.av;

import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanResult;
import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClamAVClientTest {

	private ClamAVClient newClient() {
		return new ClamAVClient("localhost", 3310, 100);
	}

	@Test
	public void populateVirusScanResult_shouldReturnPassed_onOk() {
		VirusScanResult r = invokePopulate("stream: OK");
		Assert.assertEquals(r.getStatus(), VirusScanStatus.PASSED);
	}

	@Test
	public void populateVirusScanResult_shouldReturnError_onEmpty() {
		VirusScanResult r = invokePopulate("");
		Assert.assertEquals(r.getStatus(), VirusScanStatus.ERROR);
	}

	@Test
	public void populateVirusScanResult_shouldReturnError_onErrorSuffix() {
		VirusScanResult r = invokePopulate("stream: something ERROR");
		Assert.assertEquals(r.getStatus(), VirusScanStatus.ERROR);
	}

	@Test
	public void populateVirusScanResult_shouldReturnFailed_defaultBranch() {
		VirusScanResult r = invokePopulate("unrecognized response");
		Assert.assertEquals(r.getStatus(), VirusScanStatus.FAILED);
	}

	@Test
	public void populateVirusScanResult_shouldReturnFailed_withoutSignature_dueToEndsWithCheckBug() {
		// Typical infected response from clamd would be: "stream: Eicar-Test-Signature FOUND"
		// Current implementation checks endsWith("stream:") so signature is not extracted.
		VirusScanResult r = invokePopulate("stream: Eicar-Test-Signature FOUND");
		Assert.assertEquals(r.getStatus(), VirusScanStatus.FAILED);
		Assert.assertNull(r.getSignature()); // Bug: signature not parsed
	}

	@Test
	public void ping_shouldReturnFalse_dueToClosedSocketBug() {
		// openConnection returns a closed socket (try-with-resources), causing failure.
		ClamAVClient client = newClient();
		Assert.assertFalse(client.ping());
	}

	@Test(expectedExceptions = IOException.class)
	public void scan_shouldThrowIOException_dueToClosedSocketBug() throws IOException {
		ClamAVClient client = newClient();
		client.scan(new ByteArrayInputStream("data".getBytes()));
	}

	private VirusScanResult invokePopulate(String result) {
		try {
			ClamAVClient client = newClient();
			Method m = ClamAVClient.class.getDeclaredMethod("populateVirusScanResult", String.class);
			m.setAccessible(true);
			return (VirusScanResult) m.invoke(client, result);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
