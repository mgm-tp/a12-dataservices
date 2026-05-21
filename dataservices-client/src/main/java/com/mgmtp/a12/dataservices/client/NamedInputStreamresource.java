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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

/**
 * Wrapper on {@link InputStreamResource} which contains file name. The file name is used to build proper request to the server.  
 *
 */
public class NamedInputStreamresource extends InputStreamResource {

	private String fileName;

	/**
	 * Creates a resource wrapper for an {@link InputStream} with an explicit file name.
	 *
	 * @param inputStream the stream of file content; must not be `null`.
	 * @param fileName the logical file name used for requests and headers; must not be `null`.
	 */
	public NamedInputStreamresource(InputStream inputStream, String fileName) {
		super(inputStream, fileName);
		this.fileName = fileName;

	}

	@Override public String getFilename() {
		return fileName;
	}

	/**
	 * Returns `-1` to prevent the framework from reading the stream twice to determine content length.
	 *
	 * @return `-1` to signal unknown length.
	 * @throws IOException never thrown in this implementation.
	 */
	@Override public long contentLength() throws IOException {
		//we ned to resturn <0 otherwise stream will be read twice
		return -1;
	}

}
