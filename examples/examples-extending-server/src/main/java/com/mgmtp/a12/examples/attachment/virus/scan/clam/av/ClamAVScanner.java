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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanResult;
import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanStatus;
import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Clam AV Scanner class. For virus scanning example
 *
 */
@AllArgsConstructor
@Slf4j
public class ClamAVScanner implements VirusScanner {

	private ClamAVClient client;

	/**
	 * {@inheritDoc}
	 *
	 * @param bytes raw content to scan; must not be null. May be empty.
	 * @return a {@link VirusScanResult} describing the outcome and optional signature.
	 */
	@Override public VirusScanResult scan(byte[] bytes) {
		try {
			if (client.ping()) {
				try  {
					return client.scan(new ByteArrayInputStream(bytes));
				} catch (IOException e) {
					log.error("An error occurred while scanning file., " + e.getMessage());
					return VirusScanResult.builder()
						.status(VirusScanStatus.FAILED)
						.result(e.getMessage())
						.build();
				}
			} else {
				log.error("ClamAV did not respond to ping request!");
				return VirusScanResult.builder()
					.status(VirusScanStatus.CONNECTION_FAILED)
					.result("ClamAV did not respond to ping request!")
					.build();
			}
		} catch (Exception e) {
			log.error("An error occurred while scanning file., " + e.getMessage());
			return VirusScanResult.builder()
				.status(VirusScanStatus.ERROR)
				.result("An error occurred while scanning file.")
				.build();
		}
	}
}
