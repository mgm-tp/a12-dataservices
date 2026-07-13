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
package com.mgmtp.a12.dataservices.common.content.internal;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.util.MimeTypeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent;

public class JsonContentTypeListenerTest {

	private JsonContentTypeListener jsonContentTypeListener = new JsonContentTypeListener(new ObjectMapper());

	@Test
	public void jsonMimeTypeDetection_shouldReturnJsonMimeType_whenGivenCorrectJsonFormatInput() {
		try (InputStream jsonInputStream = this.getClass().getResourceAsStream("/attachments/application.json")) {
			ContentTypeDetectedEvent event = new ContentTypeDetectedEvent(MimeTypeUtils.TEXT_PLAIN_VALUE, null, () -> jsonInputStream);
			jsonContentTypeListener.jsonMimeTypeDetection(event);

			Assert.assertEquals(event.getDetectedMimeType(), MimeTypeUtils.APPLICATION_JSON_VALUE);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void jsonMimeTypeDetection_shouldReturnDetectedMimeType_whenGivenIncorrectJsonFormatInput() {
		try (InputStream incorrectInputStream = IOUtils.toInputStream("Plain Text", StandardCharsets.UTF_8)) {
			ContentTypeDetectedEvent event = new ContentTypeDetectedEvent(MimeTypeUtils.TEXT_PLAIN_VALUE, "application.json", () -> incorrectInputStream);
			jsonContentTypeListener.jsonMimeTypeDetection(event);

			Assert.assertEquals(event.getDetectedMimeType(), MimeTypeUtils.TEXT_PLAIN_VALUE);
		} catch (Exception e) {
			Assert.fail();
		}
	}
}
