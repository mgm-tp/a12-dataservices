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

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

@Listeners(MockitoTestNGListener.class)
public class ResourceUtilTest {

	@Mock
	private ResourceLoader resourceLoader;

	@Mock
	private Resource resource;

	private ResourceUtil resourceUtil;

	@BeforeMethod
	public void setUp() {
		resourceUtil = new ResourceUtil(resourceLoader);
	}

	@Test
	public void getInputStream_returnsStream_fromResourceLoader() throws Exception {
		String location = "classpath:test.txt";
		byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
		InputStream is = new ByteArrayInputStream(data);

		when(resourceLoader.getResource(eq(location))).thenReturn(resource);
		when(resource.getInputStream()).thenReturn(is);

		InputStream result = resourceUtil.getInputStream(location);
		byte[] read = result.readAllBytes();
		assertEquals(new String(read, StandardCharsets.UTF_8), "hello");
	}

	@Test
	public void getInputStream_throws_whenResourceLoaderFails() throws Exception {
		String location = "file:/missing.txt";
		when(resourceLoader.getResource(eq(location))).thenReturn(resource);
		when(resource.getInputStream()).thenThrow(new IOException("not found"));

		assertThrows(IOException.class, () -> resourceUtil.getInputStream(location));
	}

	@Test
	public void readString_mapsReader_withProvidedFunction() {
		String input = "abc123";
		Function<Reader, Integer> countChars = reader -> {
			try {
				int total = 0;
				char[] buf = new char[4];
				int n;
				while ((n = reader.read(buf)) != -1) {
					total += n;
				}
				return total;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};

		Integer result = ResourceUtil.readString(input, countChars);
		assertEquals(result.intValue(), input.length());
	}

	@Test
	public void readString_returnsTransformedValue() {
		String input = "line1\nline2";
		Function<Reader, String> toUpper = reader -> {
			try {
				StringBuilder sb = new StringBuilder();
				char[] buf = new char[8];
				int n;
				while ((n = reader.read(buf)) != -1) {
					sb.append(buf, 0, n);
				}
				return sb.toString().toUpperCase();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};

		String result = ResourceUtil.readString(input, toUpper);
		assertEquals(result, "LINE1\nLINE2");
	}
}
