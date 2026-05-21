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

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class ClamAVScannerTest {

	@Test
	public void scan_shouldReturnScanResult_whenPingOkAndScanOk() throws Exception {
		ClamAVClient client = mock(ClamAVClient.class);
		VirusScanResult expected = VirusScanResult.builder()
			.status(VirusScanStatus.PASSED)
			.result("OK")
			.build();
		when(client.ping()).thenReturn(true);
		when(client.scan(any())).thenReturn(expected);

		ClamAVScanner scanner = new ClamAVScanner(client);
		VirusScanResult actual = scanner.scan("data".getBytes());

		Assert.assertEquals(actual.getStatus(), VirusScanStatus.PASSED);
		Assert.assertEquals(actual.getResult(), "OK");
		verify(client, times(1)).ping();
		verify(client, times(1)).scan(any());
	}

	@Test
	public void scan_shouldReturnFailed_whenPingOkAndScanThrowsIOException() throws Exception {
		ClamAVClient client = mock(ClamAVClient.class);
		when(client.ping()).thenReturn(true);
		when(client.scan(any())).thenThrow(new IOException("io fail"));

		ClamAVScanner scanner = new ClamAVScanner(client);
		VirusScanResult actual = scanner.scan("data".getBytes());

		Assert.assertEquals(actual.getStatus(), VirusScanStatus.FAILED);
		Assert.assertEquals(actual.getResult(), "io fail");
		verify(client, times(1)).ping();
		verify(client, times(1)).scan(any());
	}

	@Test
	public void scan_shouldReturnConnectionFailed_whenPingReturnsFalse() throws IOException {
		ClamAVClient client = mock(ClamAVClient.class);
		when(client.ping()).thenReturn(false);

		ClamAVScanner scanner = new ClamAVScanner(client);
		VirusScanResult actual = scanner.scan("data".getBytes());

		Assert.assertEquals(actual.getStatus(), VirusScanStatus.CONNECTION_FAILED);
		Assert.assertTrue(actual.getResult().contains("did not respond"));
		verify(client, times(1)).ping();
		verify(client, never()).scan(any());
	}

	@Test
	public void scan_shouldReturnError_whenPingThrowsRuntimeException() throws IOException {
		ClamAVClient client = mock(ClamAVClient.class);
		when(client.ping()).thenThrow(new RuntimeException("boom"));

		ClamAVScanner scanner = new ClamAVScanner(client);
		VirusScanResult actual = scanner.scan("data".getBytes());

		Assert.assertEquals(actual.getStatus(), VirusScanStatus.ERROR);
		Assert.assertTrue(actual.getResult().contains("An error occurred"));
		verify(client, times(1)).ping();
		verify(client, never()).scan(any());
	}
}
