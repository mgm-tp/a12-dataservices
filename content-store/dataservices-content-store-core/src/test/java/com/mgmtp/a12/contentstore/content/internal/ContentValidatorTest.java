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
package com.mgmtp.a12.contentstore.content.internal;

import java.io.IOException;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.contentstore.AbstractContentStoreTest;
import com.mgmtp.a12.dataservices.common.exception.InvalidSizeException;
import com.mgmtp.a12.contentstore.utils.Constants;

import lombok.NonNull;

public class ContentValidatorTest extends AbstractContentStoreTest {

	@Override
	@BeforeMethod public void init() throws IOException {
		super.init();
		Mockito.when(contentStoreProperties.getLimitSize()).thenReturn("1 kB");
	}

	@Test public void test_shouldSuccess_whenGivenContentSmallerThanSizeLimit() throws IOException {
		Mockito.when(contentTypeDetector.getContentLength(ArgumentMatchers.any())).thenReturn(1000L);

		initContentValidator().getSizeAndValidate(new ClassPathResource("/content/content_small.pdf").getContentAsByteArray());
	}

	@Test public void test_shouldThrowInvalidSizeException_whenGivenBiggerContentThanSizeLimit() throws IOException {
		Mockito.when(contentTypeDetector.getContentLength(ArgumentMatchers.any())).thenReturn(1001L);

		InvalidSizeException invalidSizeException = Assert.expectThrows(InvalidSizeException.class,
			() -> initContentValidator().getSizeAndValidate(new ClassPathResource("/content/content_big.pdf").getContentAsByteArray()));
		Assert.assertNotNull(invalidSizeException);
		Assert.assertEquals(invalidSizeException.getMessage(),
			Constants.CONTENT_SIZE_CANNOT_EXCEED_LIMIT_PATTERN.formatted(contentStoreProperties.getLimitSize()));
	}

	@NonNull private ContentValidator initContentValidator() {
		return new ContentValidator(contentTypeDetector, contentStoreProperties);
	}
}
