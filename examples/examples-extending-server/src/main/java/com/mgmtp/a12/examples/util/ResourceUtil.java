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
package com.mgmtp.a12.examples.util;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Utility for accessing resources via Spring's {@link org.springframework.core.io.ResourceLoader}.
 * Provides helpers to open an {@link java.io.InputStream} for a location and to read a {@link String} through a {@link java.io.Reader}.
 */
@Component @RequiredArgsConstructor
public class ResourceUtil {

	private final ResourceLoader resourceLoader;

	/**
	 * Opens an {@link InputStream} for the given resource location.
	 *
	 * @param location the resource location (e.g., classpath:, file:, http:); must not be null.
	 * @return an input stream for reading the resource.
	 */
	@SneakyThrows @NonNull public InputStream getInputStream(String location) {
		return resourceLoader.getResource(location).getInputStream();
	}

	/**
	 * Reads a string via a {@link Reader} and maps it to a value.
	 *
	 * @param <T> the result type returned by the mapping function.
	 * @param s the string to read; must not be null.
	 * @param f a mapping function that consumes the {@link Reader} and returns a result; must not be null.
	 * @return the value produced by the mapping function.
	 */
	public static <T> T readString(String s, Function<Reader, T> f) {
		try (StringReader r = new StringReader(s)) {
			return f.apply(r);
		}
	}
}
