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
package com.mgmtp.a12.examples.attachment.virus.scan;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mgmtp.a12.examples.attachment.AttachmentContentValidator;
import com.mgmtp.a12.examples.attachment.virus.scan.clam.av.ClamAVClient;
import com.mgmtp.a12.examples.attachment.virus.scan.clam.av.ClamAVScanner;
import com.mgmtp.a12.examples.configuration.ExtendedServerConfigurationProperties;

import lombok.RequiredArgsConstructor;

/**
 * Virus Scan Configuration class. ClamAV virus scanner is configured here.
 *
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.virus-scan", name = "enabled", havingValue = "true")
public class VirusScanConfiguration {
	private final ExtendedServerConfigurationProperties properties;

	/**
	 * Configures the {@link VirusScanner} bean backed by a {@link ClamAVScanner}.
	 *
	 * @return a {@link VirusScanner} that delegates to the configured {@link ClamAVClient}.
	 */
	@Bean
	public VirusScanner virusScanner() {
		return new ClamAVScanner(clamAVClient());
	}

	/**
	 * Creates a {@link ClamAVClient} using properties from {@link ExtendedServerConfigurationProperties}.
	 *
	 * @return a client for communicating with the ClamAV daemon over TCP.
	 */
	@Bean
	public ClamAVClient clamAVClient() {
		return new ClamAVClient(
			properties.getAttachments().getVirusScan().getClamAV().getHost(),
			properties.getAttachments().getVirusScan().getClamAV().getPort(),
			properties.getAttachments().getVirusScan().getClamAV().getTimeout()
		);
	}

	/**
	 * Registers the virus scan validator that uses the configured {@link VirusScanner}.
	 *
	 * @return a validator that fails if the scan result does not indicate {@link VirusScanStatus#PASSED}.
	 */
	@Bean
	public AttachmentContentValidator virusScanValidator() {
		return new VirusScanValidator(virusScanner());
	}
}
