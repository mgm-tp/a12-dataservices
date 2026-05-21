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
package com.mgmtp.a12.dataservices.document.support.internal;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class DefaultDocumentSupportTest {

	private DocumentDeserializationConfig defaultDeserConfig;
	private DocumentSerializationConfig defaultSerConfig;
	private IDocumentModelResolver modelResolver;
	private IDocumentV2Serializer serializer;
	private DefaultDocumentSupport support;

	@BeforeMethod
	public void setUp() {
		defaultDeserConfig = mock(DocumentDeserializationConfig.class);
		defaultSerConfig = mock(DocumentSerializationConfig.class);
		modelResolver = mock(IDocumentModelResolver.class);
		serializer = mock(IDocumentV2Serializer.class);
		support = new DefaultDocumentSupport(defaultDeserConfig, defaultSerConfig, modelResolver, serializer);
	}

	@Test(description = "Deserializes document successfully with provided config")
	public void shouldDeserializeDocumentWhenNoProblems() throws Exception {
		DocumentDeserializationConfig custom = mock(DocumentDeserializationConfig.class);
		DocumentV2 doc = mock(DocumentV2.class);
		when(serializer.deserializeV2(any(Reader.class), eq("modelX"), eq(custom), any())).thenReturn(doc);

		DocumentV2 result = support.deserialize("modelX", new StringReader("{}"), custom);

		assertSame(result, doc);
		verify(serializer).deserializeV2(any(Reader.class), eq("modelX"), eq(custom), any());
	}

	@Test(description = "Uses default deserialization config when converting JSON without explicit config")
	public void shouldUseDefaultDeserializationConfigForConvertJSONToDocument() {
		DocumentV2 doc = mock(DocumentV2.class);
		when(serializer.deserializeV2(any(Reader.class), eq("modelY"), eq(defaultDeserConfig), any())).thenReturn(doc);

		DocumentV2 result = support.convertJSONToDocument("modelY", new StringReader("{}"));

		assertSame(result, doc);
		ArgumentCaptor<DocumentDeserializationConfig> captor = ArgumentCaptor.forClass(DocumentDeserializationConfig.class);
		verify(serializer).deserializeV2(any(Reader.class), eq("modelY"), captor.capture(), any());
		assertSame(captor.getValue(), defaultDeserConfig);
	}

	@Test(description = "Translates DocumentSerializationException to DataServicesDocumentSerializationException")
	public void shouldWrapDocumentSerializationException() {
		when(serializer.deserializeV2(any(Reader.class), anyString(), any(), any()))
			.thenThrow(new DocumentSerializationException("low-level"));
		assertThrows(DataServicesDocumentSerializationException.class,
			() -> support.convertJSONToDocument("modelZ", new StringReader("{}")));
	}

	@Test(description = "Translates UnsupportedOperationException to DataServicesDocumentSerializationException")
	public void shouldWrapUnsupportedOperationException() {
		when(serializer.deserializeV2(any(Reader.class), anyString(), any(), any()))
			.thenThrow(new UnsupportedOperationException("unsupported"));
		DataServicesDocumentSerializationException ex = assertThrows(
			DataServicesDocumentSerializationException.class,
			() -> support.convertJSONToDocument("modelZ", new StringReader("{}")));
		assertTrue(ex.getMessage().contains("unsupported"));
	}

	@Test(description = "Serializes with provided config")
	public void shouldSerializeDocumentWithProvidedConfig() {
		DocumentV2 doc = mock(DocumentV2.class);
		Writer writer = mock(Writer.class);
		DocumentSerializationConfig custom = mock(DocumentSerializationConfig.class);

		support.serialize(doc, writer, custom);

		verify(serializer).serializeV2(doc, writer, custom);
	}

	@Test(description = "Serializes with default config when converting to JSON")
	public void shouldUseDefaultSerializationConfigWhenConvertingDocumentToJSON() {
		DocumentV2 doc = mock(DocumentV2.class);
		Writer writer = mock(Writer.class);

		support.convertDocumentToJSON(doc, writer);

		verify(serializer).serializeV2(doc, writer, defaultSerConfig);
	}

	@Test(description = "Returns preferred locale when present in model")
	public void shouldReturnPreferredLocaleWhenPresentInModel() {
		DocumentV2 doc = mock(DocumentV2.class);
		when(doc.getDocumentModelId()).thenReturn("dm1");
		IDocumentModel model = mock(IDocumentModel.class);
		Header header = mock(Header.class);
		when(modelResolver.getDocumentModelById("dm1")).thenReturn(model);
		when(model.getHeader()).thenReturn(header);
		when(header.getLocales()).thenReturn(List.of(Locale.GERMAN, Locale.ENGLISH));

		Locale resolved = support.resolveLocale(doc, Locale.GERMAN, true);

		assertEquals(resolved, Locale.GERMAN);
	}

	@Test(description = "Returns preferred locale even if not in model when skipNonExisting is false")
	public void shouldReturnPreferredLocaleWhenNotInModelAndSkipNonExistingFalse() {
		DocumentV2 doc = mock(DocumentV2.class);
		when(doc.getDocumentModelId()).thenReturn("dm2");
		IDocumentModel model = mock(IDocumentModel.class);
		Header header = mock(Header.class);
		when(modelResolver.getDocumentModelById("dm2")).thenReturn(model);
		when(model.getHeader()).thenReturn(header);
		when(header.getLocales()).thenReturn(List.of(Locale.FRENCH));

		Locale preferred = Locale.ITALIAN; // not present
		Locale resolved = support.resolveLocale(doc, preferred, false);

		assertEquals(resolved, preferred, "Expected to retain preferred locale even if absent when skipNonExisting=false");
	}

	@Test(description = "Returns first model locale when preferred missing and skipNonExisting true")
	public void shouldReturnFirstLocaleWhenPreferredMissingAndSkipNonExistingTrue() {
		DocumentV2 doc = mock(DocumentV2.class);
		when(doc.getDocumentModelId()).thenReturn("dm3");
		IDocumentModel model = mock(IDocumentModel.class);
		Header header = mock(Header.class);
		when(modelResolver.getDocumentModelById("dm3")).thenReturn(model);
		when(model.getHeader()).thenReturn(header);
		when(header.getLocales()).thenReturn(List.of(Locale.ENGLISH, Locale.GERMAN));

		Locale resolved = support.resolveLocale(doc, Locale.JAPANESE, true);

		assertEquals(resolved, Locale.ENGLISH);
	}

	@Test(description = "Throws InvalidInputException when no locales configured")
	public void shouldThrowInvalidInputExceptionWhenNoLocales() {
		DocumentV2 doc = mock(DocumentV2.class);
		when(doc.getDocumentModelId()).thenReturn("dm4");
		IDocumentModel model = mock(IDocumentModel.class);
		Header header = mock(Header.class);
		when(modelResolver.getDocumentModelById("dm4")).thenReturn(model);
		when(model.getHeader()).thenReturn(header);
		when(header.getLocales()).thenReturn(List.of());

		assertThrows(InvalidInputException.class,
			() -> support.resolveLocale(doc, Locale.ENGLISH, true));
	}

	// Helper for assertThrows with TestNG (Java 21 - can use built-in if available; fallback simple impl)
	private <T extends Throwable> T assertThrows(Class<T> type, ThrowingRunnable r) {
		try {
			r.run();
		} catch (Throwable t) {
			if (type.isInstance(t)) {
				return type.cast(t);
			}
			fail("Unexpected exception type: " + t);
		}
		fail("Expected exception: " + type.getName());
		return null;
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}

