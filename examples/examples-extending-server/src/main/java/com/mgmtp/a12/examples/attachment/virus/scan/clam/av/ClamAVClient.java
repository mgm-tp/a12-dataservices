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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanResult;
import com.mgmtp.a12.examples.attachment.virus.scan.VirusScanStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The ClamAV Client which opens a TCP socket to ClamD Daemon of Clam AV Server to send corresponding commands.
 * As mentioned in document: https://docs.clamav.net/manual/Usage/Scanning.html
 * Please do not use this approach in production.
 */
@RequiredArgsConstructor
@Slf4j
public class ClamAVClient {
	private static final int CHUNK_SIZE = 2048;
	private static final String RESPONSE_OK = "stream: OK";
	private static final String PONG = "PONG";
	private static final String FOUND_SUFFIX = "FOUND";
	private static final String ERROR_SUFFIX = "ERROR";
	private static final String STREAM_PREFIX = "stream:";

	private final String host;
	private final int port;
	private final int timeout;

	/**
	 * Sends a PING command to the ClamAV daemon and verifies connectivity.
	 *
	 * @return true if the daemon responds with `PONG`; false if an error occurs or no response is received.
	 */
	public boolean ping() {
		String response = "";
		try (Socket socket = openConnection()) {
			try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

				// Send PING command.
				dos.write("zPING\0".getBytes());
				dos.flush();

				// Read the PONG, from Input Stream of Socket.
				InputStream serverInputStream = socket.getInputStream();
				int read = CHUNK_SIZE;
				byte[] buffer = new byte[CHUNK_SIZE];
				while (read == CHUNK_SIZE) {
					try {
						read = serverInputStream.read(buffer);
					} catch (IOException e) {
						log.error("Error reading result from socket, " + e.getMessage());
						break;
					}
					response = new String(buffer, 0, read);
				}
			}

			return PONG.equalsIgnoreCase(response.trim());
		} catch (Exception e) {
			log.error("Error pinging to ClamAV, " + e.getMessage());
			return false;
		}
	}

	/**
	 * Streams the provided content to the ClamAV daemon using the `INSTREAM` command.
	 *
	 * @param fileInputStream the content to scan; must not be null. The stream is consumed by this method.
	 * @return the {@link com.mgmtp.a12.examples.attachment.virusScan.VirusScanResult} indicating scan status and optional signature.
	 * @throws IOException if a socket or protocol error is encountered while scanning.
	 */
	public VirusScanResult scan(InputStream fileInputStream) throws IOException {

		try (Socket socket = openConnection()) {
			try (OutputStream serverOutputStream = new BufferedOutputStream(socket.getOutputStream())) {
				// Send the INSTREAM command
				serverOutputStream.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
				serverOutputStream.flush();

				// Read the bytes from fileInputStream then send it to serverOutputStream with CHUNK_SIZE
				byte[] buffer = new byte[CHUNK_SIZE];
				try (InputStream serverInputStream = socket.getInputStream()) {
					int read = fileInputStream.read(buffer);

					while (read >= 0) {
						byte[] chunkSize = ByteBuffer.allocate(4).putInt(read).array();
						serverOutputStream.write(chunkSize);
						serverOutputStream.write(buffer, 0, read);

						if (serverInputStream.available() > 0) {
							byte[] reply = IOUtils.toByteArray(serverInputStream);
							throw new IOException(
								"Reply from server: " + new String(reply, StandardCharsets.UTF_8));
						}
						read = fileInputStream.read(buffer);
					}

					// Send terminating command
					serverOutputStream.write(new byte[] {0, 0, 0, 0});
					serverOutputStream.flush();

					String result = new String(IOUtils.toByteArray(serverInputStream)).trim();

					return populateVirusScanResult(result);
				}
			}
		}
	}

	private Socket openConnection() throws IOException {
		try(Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			socket.setSoTimeout(timeout);
			return socket;
		}
	}

	private VirusScanResult populateVirusScanResult(String result) {

		if (StringUtils.isEmpty(result)) {
			return VirusScanResult.builder()
				.status(VirusScanStatus.ERROR)
				.build();
		}

		if (RESPONSE_OK.equals(result)) {
			return VirusScanResult.builder()
				.status(VirusScanStatus.PASSED)
				.build();
		}

		if (result.endsWith(STREAM_PREFIX)) {
			return VirusScanResult.builder()
				.status(VirusScanStatus.FAILED)
				.signature(result.substring(STREAM_PREFIX.length(), result.lastIndexOf(FOUND_SUFFIX) - 1).trim())
				.build();
		}

		if (result.endsWith(ERROR_SUFFIX)) {
			return VirusScanResult.builder()
				.status(VirusScanStatus.ERROR)
				.build();
		}

		return VirusScanResult.builder()
			.status(VirusScanStatus.FAILED)
			.build();
	}
}
