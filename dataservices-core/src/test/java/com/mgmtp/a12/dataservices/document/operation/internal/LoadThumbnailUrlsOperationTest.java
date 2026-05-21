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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;

@Listeners(MockitoTestNGListener.class)
public class LoadThumbnailUrlsOperationTest {

	@InjectMocks LoadThumbnailUrlsOperation loadThumbnailUrlsOperation;

	@Mock private ThumbnailUtil thumbnailUtil;
	@Mock private AttachmentReferenceJpaRepository attachmentReferenceJpaRepository;

	@BeforeMethod void init() throws IllegalAccessException {
		FieldUtils.writeField(loadThumbnailUrlsOperation, "thumbnailUtilOpt", Optional.of(thumbnailUtil), true);
	}
	@Test public void testLoadThumbnailUrls_success() {
		try (MockedStatic<LoadedDocumentReferencesContextHolder> mockedLoadedDocumentReferencesContextHolder = Mockito.mockStatic(
			LoadedDocumentReferencesContextHolder.class)) {
			Mockito.when(LoadedDocumentReferencesContextHolder.getAllDocumentReferences())
				.thenReturn(Set.of(new DocumentReference("BusinessPartner", "1")));

			loadThumbnailUrlsOperation.rpc();

			mockedLoadedDocumentReferencesContextHolder.verify(LoadedDocumentReferencesContextHolder::getAllDocumentReferences, Mockito.times(1));
			Mockito.verify(attachmentReferenceJpaRepository, Mockito.times(1)).findAllByTypeAndReference(Mockito.any(), Mockito.any());
		}
	}
	@Test public void testHasNoThumbnailUtilBean_returnEmpty() throws IllegalAccessException {
		FieldUtils.writeField(loadThumbnailUrlsOperation, "thumbnailUtilOpt", Optional.empty(), true);

		try (MockedStatic<LoadedDocumentReferencesContextHolder> mockedLoadedDocumentReferencesContextHolder = Mockito.mockStatic(
			LoadedDocumentReferencesContextHolder.class)) {
			Map<String, Map<String, AttachmentThumbnailUrl>> result = loadThumbnailUrlsOperation.rpc();
			mockedLoadedDocumentReferencesContextHolder.verifyNoInteractions();
			Mockito.verifyNoInteractions(attachmentReferenceJpaRepository);
			Assert.assertTrue(result.isEmpty());
		}
	}
}
