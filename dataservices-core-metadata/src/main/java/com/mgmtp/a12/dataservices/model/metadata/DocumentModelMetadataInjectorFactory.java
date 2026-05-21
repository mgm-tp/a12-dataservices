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
package com.mgmtp.a12.dataservices.model.metadata;

import java.util.Locale;
import java.util.regex.Pattern;

import com.mgmtp.a12.dataservices.model.metadata.internal.CdmDocumentModelMetadataInjector;
import com.mgmtp.a12.dataservices.model.metadata.internal.DmDocumentModelMetadataInjector;
import com.mgmtp.a12.dataservices.model.metadata.internal.GeneratedModelDocumentModelMetadataInjector;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_QUERY_ROOT_ANNOTATION;
import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * Factory class to construct {@link DocumentModelMetadataInjector} instance based on document model.
 */
@RequiredArgsConstructor
public class DocumentModelMetadataInjectorFactory {

	/**
	 * Model identifier used for attachment metadata.
	 */
	public static final String ATTACHMENT_META_DATA_MODEL_NAME = "attachment-meta-data";
	/**
	 * Model identifier used for document metadata.
	 */
	public static final String DOCUMENT_META_DATA_MODEL_NAME = "document-meta-data";
	private final DocumentModelJoiningService documentModelJoiningService;
	private final IDocumentFactory documentFactory;
	private final DocumentModelService documentModelService;

	/**
	 * Pattern that matches all metadata groups that may be stripped from a model.
	 * It matches the document metadata group `__meta` and attachment metadata groups `__attachment_meta_<field>`.
	 */
	public static final Pattern META_GROUP_NAME_PATTERN = Pattern.compile("^(?:__meta|__attachment_meta_.*)$");

	/**
	 * Create an instance of the {@link DocumentModelMetadataInjector}.
	 *
	 * @param documentModel selector for the proper instance.
	 * @param locale required by Kernel.
	 * @return new instance of the {@link DocumentModelMetadataInjector} based on the document model.
	 */
	public DocumentModelMetadataInjector getInstance(IDocumentModel documentModel, Locale locale) {
		if (isGenerated(documentModel)) {
			return new GeneratedModelDocumentModelMetadataInjector(documentModel, documentModelJoiningService, documentModelService, locale);
		} else if (isCdm(documentModel)) {
			return new CdmDocumentModelMetadataInjector(documentModel, documentModelJoiningService, documentModelService, locale);
		} else {
			return new DmDocumentModelMetadataInjector(documentModel, documentModelJoiningService, documentModelService, locale);
		}
	}

	private static boolean isCdm(IDocumentModel documentModel) {
		@NonNull Header header = documentModel.getHeader();
		return DOCUMENT_MODEL_TYPE.equals(header.getModelType()) && ModelUtils.getAnnotations(header)
			.map(Annotation::getName)
			.anyMatch(CDM_QUERY_ROOT_ANNOTATION::equals);
	}

	/**
	 * Detect whether the document model is generated.
	 *
	 * @param documentModel document model to inspect.
	 * @return true if the model is generated.
	 */
	private static boolean isGenerated(IDocumentModel documentModel) {
		return documentModel.getHeader().getId().endsWith("__generated");
	}
}
