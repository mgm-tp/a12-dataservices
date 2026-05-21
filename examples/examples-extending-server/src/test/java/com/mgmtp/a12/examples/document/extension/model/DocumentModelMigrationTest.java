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
package com.mgmtp.a12.examples.document.extension.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.mockito.ArgumentCaptor;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.GenericModelContent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeRepositorySaveEvent;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class DocumentModelMigrationTest {

	private final DocumentModelMigration migration = new DocumentModelMigration();


	@Test
	public void decryptModelAfterLoad_repositoryLoad_decodesContent() {
		String original = "{\"key\":1}";
		String encoded = encode(original);

		ModelAfterRepositoryLoadEvent event = new ModelAfterRepositoryLoadEvent(
			DocumentModelMigration.DOCUMENT_MODEL_TYPE,
			"test-user",
			encoded
		);

		migration.decryptModelAfterLoad(event);

		Assert.assertEquals(event.getModelEntityContent(), original);
	}

	@Test
	public void decryptModelAfterLoad_nonDocument_noChange() {
		String encoded = encode("data");
		ModelAfterRepositoryLoadEvent event = new ModelAfterRepositoryLoadEvent(
			"other",
			"test-user",
			encoded
		);

		String contentBefore = event.getModelEntityContent();
		migration.decryptModelAfterLoad(event);

		Assert.assertEquals(event.getModelEntityContent(), contentBefore);
	}

	@Test
	public void decryptModelAfterLoad_nullContent_noChange() {
		ModelAfterRepositoryLoadEvent event = new ModelAfterRepositoryLoadEvent(
			DocumentModelMigration.DOCUMENT_MODEL_TYPE,
			"test-user",
			null
		);

		migration.decryptModelAfterLoad(event);

		Assert.assertNull(event.getModelEntityContent());
	}

	@Test
	public void encryptModelBeforeSave_repositorySave_encodesContent() {
		String original = "{\"x\":42}";
		String expectedEncoded = encode(original);

		ModelBeforeRepositorySaveEvent event = mock(ModelBeforeRepositorySaveEvent.class);
		when(event.getModelEntityContent()).thenReturn(original);
		when(event.getModelType()).thenReturn(DocumentModelMigration.DOCUMENT_MODEL_TYPE);
		when(event.getModelName()).thenReturn("doc2");

		migration.encryptModelBeforeSave(event);

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(event).setModelEntityContent(captor.capture());
		Assert.assertEquals(captor.getValue(), expectedEncoded);
	}

	@Test
	public void encryptModelBeforeSave_nonDocument_noChange() {
		ModelBeforeRepositorySaveEvent event = mock(ModelBeforeRepositorySaveEvent.class);
		when(event.getModelEntityContent()).thenReturn("plain");
		when(event.getModelType()).thenReturn("other");
		migration.encryptModelBeforeSave(event);
		verify(event, never()).setModelEntityContent(any());
	}

	@Test
	public void encryptModelBeforeSave_nullContent_noChange() {
		ModelBeforeRepositorySaveEvent event = mock(ModelBeforeRepositorySaveEvent.class);
		when(event.getModelEntityContent()).thenReturn(null);
		when(event.getModelType()).thenReturn(DocumentModelMigration.DOCUMENT_MODEL_TYPE);
		migration.encryptModelBeforeSave(event);
		verify(event, never()).setModelEntityContent(any());
	}



	@Test
	public void decryptModelAfterUpdate_documentModel_decodesContent() {
		String original = "{\"a\":1}";
		String encoded = encode(original);

		ModelAfterUpdateEvent event = mock(ModelAfterUpdateEvent.class);
		GenericModel model = mockModelWithGenericContent(encoded, DocumentModelMigration.DOCUMENT_MODEL_TYPE);
		when(event.getModel()).thenReturn(model);

		migration.decryptModelAfterUpdate(event);

		GenericModelContent content = model.getContent();
		verify(content).setRawContent(original);
	}

	@Test
	public void decryptModelAfterLoad_afterCreate_decodesContent() {
		String original = "{\"b\":2}";
		String encoded = encode(original);

		ModelAfterCreateEvent event = mock(ModelAfterCreateEvent.class);
		GenericModel model = mockModelWithGenericContent(encoded, DocumentModelMigration.DOCUMENT_MODEL_TYPE);
		when(event.getModel()).thenReturn(model);

		migration.decryptModelAfterLoad(event);

		GenericModelContent content = model.getContent();
		verify(content).setRawContent(original);
	}

	@Test
	public void decryptModelAfterUpdate_nonDocument_noChange() {
		String original = "{\"c\":3}";
		String encoded = encode(original);

		ModelAfterUpdateEvent event = mock(ModelAfterUpdateEvent.class);
		GenericModel model = mockModelWithGenericContent(encoded, "other");
		when(event.getModel()).thenReturn(model);

		migration.decryptModelAfterUpdate(event);

		GenericModelContent content = model.getContent();
		verify(content, never()).setRawContent(any());
	}

	@Test
	public void decryptModelAfterUpdate_nonGenericContent_noDecode() {
		ModelAfterUpdateEvent event = mock(ModelAfterUpdateEvent.class);

		// Use GenericModel as expected by ModelAfterUpdateEvent#getModel()
		GenericModel model = mock(GenericModel.class);
		Header header = mock(Header.class);
		when(header.getModelType()).thenReturn("generic");
		when(header.getId()).thenReturn("doc4");
		when(model.getHeader()).thenReturn(header);

		// Non-generic content (not GenericModelContent)
		GenericModelContent nonGenericContent = mock(GenericModelContent.class);
		when(model.getContent()).thenReturn(nonGenericContent);
		when(event.getModel()).thenReturn(model);

		migration.decryptModelAfterUpdate(event);

		// Ensure no decode interaction happens on nonGenericContent
		verify(nonGenericContent, never()).setRawContent(anyString());
	}

	private GenericModel mockModelWithGenericContent(String encoded, String modelType) {
		GenericModel model = mock(GenericModel.class);
		Header header = mock(Header.class);
		when(header.getModelType()).thenReturn(modelType);
		when(header.getId()).thenReturn("doc3");
		when(model.getHeader()).thenReturn(header);

		GenericModelContent genericContent = mock(GenericModelContent.class);
		when(genericContent.getRawContent()).thenReturn(encoded);
		when(model.getContent()).thenReturn(genericContent);
		return model;
	}

	private String encode(String s) {
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}
}
