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
package com.mgmtp.a12.dataservices.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

/**
 * Extract ZIP input stream into a file.
 *
 */
public class ZipMessageConverter extends AbstractHttpMessageConverter<ZipInputStream> {

	/**
	 * Creates a converter for the `application/zip` media type.
	 */
	public ZipMessageConverter() {
		super(MediaType.parseMediaType("application/zip"));
	}

	/**
	 * Determines whether this converter supports the given class.
	 *
	 * @param clazz the target class to check; never `null`.
	 * @return `true` if the class equals {@link ZipInputStream}, otherwise `false`.
	 */
	@Override protected boolean supports(Class<?> clazz) {
		return ZipInputStream.class == clazz;
	}

	/**
	 * Reads a ZIP input stream from the HTTP message body using a temporary file as buffer.
	 *
	 * @param clazz the target type (ignored; constrained to {@link ZipInputStream}).
	 * @param inputMessage the HTTP input message containing the ZIP content.
	 * @return a {@link ZipInputStream} to consume the ZIP entries.
	 * @throws IOException if the stream cannot be read or the temporary file cannot be created.
	 * @throws HttpMessageNotReadableException if the payload cannot be converted.
	 */
	@Override protected ZipInputStream readInternal(Class<? extends ZipInputStream> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		File tmpFile = File.createTempFile("download", "tmp");
		tmpFile.deleteOnExit();
		StreamUtils.copy(inputMessage.getBody(), new FileOutputStream(tmpFile));
		return new ZipInputStream(new FileInputStream(tmpFile));
	}

	/**
	 * Writes a ZIP input stream directly to the HTTP output message body.
	 *
	 * @param t the input ZIP stream to copy; must be positioned at the desired entry.
	 * @param outputMessage the HTTP output message target.
	 * @throws IOException if writing to the output stream fails.
	 * @throws HttpMessageNotWritableException if the payload cannot be written.
	 */
	@Override protected void writeInternal(ZipInputStream t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		StreamUtils.copy(t, outputMessage.getBody());
	}

}
