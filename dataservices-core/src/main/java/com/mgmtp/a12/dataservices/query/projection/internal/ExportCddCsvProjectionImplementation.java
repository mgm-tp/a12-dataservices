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
package com.mgmtp.a12.dataservices.query.projection.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentHelper;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentHeaderRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.FunctionalityDisabledException;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.export.IDocumentExporter;
import com.mgmtp.a12.dataservices.export.internal.csv.CsvDocumentExporter;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.internal.RootBasedPageImpl;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.query.projection.internal.ExportCddCsvProjectionImplementation.PROJECTION_NAME;
import static com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils.getCrdModelName;

@RequiredArgsConstructor
@QueryProjection(PROJECTION_NAME) public class ExportCddCsvProjectionImplementation implements IQueryProjection<Void> {

	public static final String PROJECTION_NAME = "exportCddCsv";
	public static final String DOWNLOADED_URL_PARAM = "downloadUrl";

	private final DocumentTreeHelper documentTreeHelper;
	private final IDocumentModelService documentModelService;
	private final List<IDocumentExporter> documentExports;
	private final Optional<AttachmentHeaderRepository> attachmentHeaderRepositoryOpt;
	private final Optional<IAttachmentRepository> attachmentRepositoryOpt;
	private final DocumentPermissionEvaluator documentPermissionEvaluator;
	private final IModelLoader<ComposeDocumentModel> composeDocumentModelLoader;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	@Override public @NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context) {
		documentPermissionEvaluator.checkExportListCDDPermission();
		Paging paging = Paging.builder()
			.pageNumber(0)
			.pageSize(dataServicesCoreProperties.getCdd().getExport().getMaxRowSize())
			.build();

		return originalQuery.toBuilder()
			.paging(paging)
			.targetDocumentModel(getCrdModelName(context.getDocumentModel(originalQuery.getTargetDocumentModel())))
			.fields(documentTreeHelper.getAllFieldNamesOfCdm(originalQuery.getTargetDocumentModel(), context, documentModelService))
			.build();
	}

	@Override public @NonNull QueryPage<Void> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult,
		QueryContext context) {
		if (attachmentHeaderRepositoryOpt.isEmpty()) {
			throw new FunctionalityDisabledException("Attachment Upload is disabled");
		}
		IDocumentExporter documentExporter = getSuitableDocumentExporter(CsvDocumentExporter.CSV_TYPE);
		String contentType = documentExporter.getContentType();
		String baseDocumentModel = originalQuery.getTargetDocumentModel();
		InputStream exportedStream = documentExporter
			.export(
				composeDocumentModelLoader.loadModel(baseDocumentModel), queryResult.getContent().stream().map(DocumentTreeResult::getDocument).toList()
			);

		String downloadUrl =
			createAttachment(baseDocumentModel, CsvDocumentExporter.CSV_TYPE, exportedStream, contentType, attachmentRepositoryOpt.get(),
				attachmentHeaderRepositoryOpt.get());
		RootBasedPageImpl<Void> result = new RootBasedPageImpl<>(Collections.emptyList(), queryResult.getPageable(),
			queryResult.getTotalElements());
		result.getOtherResults()
			.put(DOWNLOADED_URL_PARAM, downloadUrl);
		return result;
	}

	private static String createAttachment(String baseDocumentModel, String format, InputStream inputStream, String contentType,
		IAttachmentRepository attachmentRepository, AttachmentHeaderRepository attachmentHeaderRepository) {
		String fileName = String.format("export_%s.%s", baseDocumentModel, format);
		AttachmentHeader attachmentHeader = AttachmentHelper.prepareAttachmentHeader(fileName, new ArrayList<>());

		AttachmentPersistenceResult persistenceResult =
			attachmentRepository.create(attachmentHeader.getAttachmentId(), inputStream, fileName, TypeOfTheContent.ATTACHMENT_SECURED, contentType);
		attachmentHeader.setSize(persistenceResult.getSize());
		attachmentHeader.setMimeType(persistenceResult.getMimeType());

		attachmentHeaderRepository.create(attachmentHeader);

		Optional<AttachmentUrl> attachmentUrlOptional =
			attachmentRepository.findUrl(attachmentHeader.getAttachmentId(), fileName, TypeOfTheContent.ATTACHMENT_SECURED);

		return attachmentUrlOptional.map(AttachmentUrl::getLocation).orElse(null);
	}

	private IDocumentExporter getSuitableDocumentExporter(String format) {
		return (documentExports == null ? Stream.<IDocumentExporter>empty() : documentExports.stream())
			.filter(documentExport -> documentExport.supports(format))
			.findFirst()
			.orElseThrow(() -> new IntegrityException(ExceptionKeys.INVALID_QUERY_ERROR_KEY, "There are no suitable result mapping for " + format));
	}

}
