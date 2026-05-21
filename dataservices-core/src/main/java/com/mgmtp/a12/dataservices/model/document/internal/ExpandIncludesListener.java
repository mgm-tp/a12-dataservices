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
package com.mgmtp.a12.dataservices.model.document.internal;

import java.util.Locale;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.GenericModelContent;
import com.mgmtp.a12.dataservices.model.bulkload.internal.CollapsingDocumentModelReferenceResolver;
import com.mgmtp.a12.dataservices.model.events.AbstractModelEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.header.Header;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class ExpandIncludesListener {

	private final IDocumentModelService documentModelService;
	private final IDocumentModelResolver documentModelResolver;
	private final DocumentModelMetadataInjectorFactory documentModelMetadataInjectorFactory;
	private final Locale defaultLocale;
	private final CollapsingDocumentModelReferenceResolver.CollapsingDocumentModelReferenceResolverFactory collapsingDocumentModelReferenceResolverFactory;
	private final DocumentModelUtils documentModelUtils;

	@Order(0)
	@CommonDataServicesEventListener({ ModelBeforeCreateEvent.class, ModelBeforeUpdateEvent.class })
	public void expandIncludesForDocumentModel(AbstractModelEvent<GenericModel> event) {
		GenericModel genericModel = event.getModel();
		if (isaKindOfDocumentModel(genericModel.getHeader())) {
			GenericModelContent genericModelContent = genericModel.getContent();
			genericModelContent.setRawContent(modifyModelContent(genericModelContent.getRawContent()));
		}
	}

	private String modifyModelContent(String rawContent) {
		IDocumentModel dm = documentModelUtils.deserializeDocumentModel(rawContent);
		documentModelService.expand(dm, collapsingDocumentModelReferenceResolverFactory.getInstance(documentModelResolver));
		dm = documentModelMetadataInjectorFactory.getInstance(dm, defaultLocale).getDocumentModelWithMetadata(
			documentModelResolver.getDocumentModelById(DocumentModelMetadataInjectorFactory.DOCUMENT_META_DATA_MODEL_NAME),
			documentModelResolver.getDocumentModelById(DocumentModelMetadataInjectorFactory.ATTACHMENT_META_DATA_MODEL_NAME)
		);
		return documentModelUtils.serializeDocumentModel(dm);
	}

	private static boolean isaKindOfDocumentModel(Header header) {
		return ComposeDocumentModelUtils.isComposeDocumentModel(header) || ComposeDocumentModelUtils.isDocumentModel(header);
	}
}
