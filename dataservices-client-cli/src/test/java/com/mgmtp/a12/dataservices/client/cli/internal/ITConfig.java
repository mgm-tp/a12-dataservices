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
package com.mgmtp.a12.dataservices.client.cli.internal;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ITConfig {

	@Bean
	@Primary
	public IApplicationOutput applicationTestOutput() {
		log.info("\n\n==========================================\nTEST application output registered.\n=============================================\n\n");
		return new IApplicationOutput() {

			@Override
			public void flush() {
				flush(ClientIT.dialogOutputPrintWriter);
				flush(ClientIT.stdout);
				flush(ClientIT.stderr);
			}

			@Override
			public void close() {
				close(ClientIT.dialogOutputPrintWriter);
				close(ClientIT.stdout);
				close(ClientIT.stderr);
			}

			@Override
			public PrintWriter getDialogOutput() {
				return ClientIT.dialogOutputPrintWriter;
			}

			@Override
			public OutputStream getDataOutput() {
				return ClientIT.stdout;
			}

			private void close(Closeable stdout) {
				Optional.ofNullable(stdout).ifPresent(s -> {
					try {
						s.close();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				});
			}

			private void flush(Flushable stdout) {
				Optional.ofNullable(stdout).ifPresent(s -> {
					try {
						s.flush();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				});
			}
		};
	}
}
