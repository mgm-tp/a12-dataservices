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
package com.mgmtp.a12.dataservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.internal.DefaultAttachmentService;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.attachment.OnEnabledAttachmentCondition;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Conditional(OnEnabledAttachmentCondition.class)
@Component public class AttachmentTestFunctions {

	public static final String BUSINESS_PARTNER = "BusinessPartner";
	private static final String BUSINESS_PARTNER_PATH_TO_FIELD = BUSINESS_PARTNER + "/" + "attachment";
	public static final String PNG_ATTACHMENT_PATH = "/attachment/image-attachment.png";
	private static final String WEBP_ATTACHMENT_PATH = "/attachment/image-attachment.webp";
	private static final String XML_ATTACHMENT_PATH = "/attachment/text-attachment.xml";
	private static final String DOCUMENT_ATTACHMENT_PATH = "/attachment/";
	protected static final String DOCUMENT_MODEL_ROOT_DIR = "models/document/";
	//	@Autowired private AttachmentService attachmentService;
	@Autowired private DefaultAttachmentService attachmentServiceV2;
	@Autowired protected ResourceFunctions resourceFunctions;
	@Autowired protected DocumentService documentService;
	@Autowired protected DocumentSupport documentSupport;
	@Autowired protected DocumentFunctions documentFunctions;
	@Autowired protected ModelsFunctions modelsFunctions;
	@Autowired private AuthorizationService authorizationService;
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;

	public void prepareDocumentModel() {
		modelsFunctions.createModel(DOCUMENT_MODEL_ROOT_DIR + BUSINESS_PARTNER + ".json");
	}

	public PreparedDocument prepareDocumentWith2AttachmentsV2() throws Exception {

		UploadedAttachmentV2 uploadedImageFile = uploadPngAttachmentV2();
		UploadedAttachmentV2 uploadedXMLFile = uploadXmlAttachmentV2();
		DataServicesDocument dataServicesDocument = loadDocumentWithAttachment(
			DOCUMENT_ATTACHMENT_PATH + "BusinessPartnerWith2Attachments.json", uploadedImageFile.getAttachmentId(), uploadedXMLFile.getAttachmentId());

		PreparedDocument preparedDocument = new PreparedDocument();
		preparedDocument.dataServicesDocument = dataServicesDocument;
		preparedDocument.imageAttachment = uploadedImageFile;
		preparedDocument.xmlAttachment = uploadedXMLFile;

		return preparedDocument;
	}

	public PreparedDocumentV2 prepareDocumentWithAttachmentV2() throws Exception {

		UploadedAttachmentV2 uploadedImageFile = uploadPngAttachmentV2();
		DataServicesDocument dataServicesDocument = loadDocumentWithAttachment(
			DOCUMENT_ATTACHMENT_PATH + "BusinessPartnerWith2Attachments.json", uploadedImageFile.getAttachmentId(), uploadedImageFile.getAttachmentId());

		PreparedDocumentV2 preparedDocumentV2 = new PreparedDocumentV2();
		preparedDocumentV2.dataServicesDocument = dataServicesDocument;
		preparedDocumentV2.imageAttachment = uploadedImageFile;

		return preparedDocumentV2;
	}

	public Prepared2Attachments prepare2Attachments() throws Exception {

		Prepared2Attachments prepared2Attachments = new Prepared2Attachments();
		prepared2Attachments.imageAttachment = uploadPngAttachmentV2();
		prepared2Attachments.xmlAttachment = uploadXmlAttachmentV2();

		return prepared2Attachments;
	}

	public File createTestImage() throws IOException {
		return loadTempFile(PNG_ATTACHMENT_PATH, "test", ".png");
	}

	public File createTestXMLFile() throws IOException {
		return loadTempFile(XML_ATTACHMENT_PATH, "test", ".xml");
	}

	private UploadedAttachmentV2 uploadPngAttachmentV2() throws IOException {
		return uploadAttachmentV2(loadTempFile(PNG_ATTACHMENT_PATH, "test", ".png"));
	}

	private UploadedAttachmentV2 uploadXmlAttachmentV2() throws IOException {
		return uploadAttachmentV2(loadTempFile(XML_ATTACHMENT_PATH, "test", ".xml"));
	}

	private File loadTempFile(String path, String targetFileName, String targetFileExtension) throws IOException {
		Resource xmlData = resourceFunctions.loadResourceFromClassPath(path);
		File tempFile = File.createTempFile(targetFileName, targetFileExtension);
		FileUtils.copyFile(xmlData.getFile(), tempFile);
		return tempFile;
	}

	private UploadedAttachmentV2 uploadAttachmentV2(File attachmentFile) {
		String originalFilename = attachmentFile.getName();
		try (InputStream is = FileUtils.openInputStream(attachmentFile)) {
			AttachmentHeader header =
				attachmentServiceV2.createAttachment(is, originalFilename, BUSINESS_PARTNER, BUSINESS_PARTNER_PATH_TO_FIELD, Collections.emptyList());
			return UploadedAttachmentV2.builder()
				.attachmentId(header.getAttachmentId())
				.thumbnailBigId(header.getThumbnailBigId())
				.thumbnailSmallId(header.getThumbnailSmallId())
				.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private DataServicesDocument loadDocumentWithAttachment(String path, @NonNull String imageAttachmentId, String xmlAttachmentId) throws IOException {
		Resource documentResource = resourceFunctions.loadResourceFromClassPath(path);
		String document = FileUtils.readFileToString(documentResource.getFile(), StandardCharsets.UTF_8);
		document = StringUtils.replace(document, "{attachment_1}", imageAttachmentId);
		if (xmlAttachmentId != null) {
			document = StringUtils.replace(document, "{attachment_2}", xmlAttachmentId);
		}
		return documentService.create(documentSupport.convertJSONToDocument(BUSINESS_PARTNER, new StringReader(document)), Locale.ENGLISH);
	}

	@Data
	public static class PreparedDocument {
		private DataServicesDocument dataServicesDocument;
		private UploadedAttachmentV2 imageAttachment;
		private UploadedAttachmentV2 xmlAttachment;
	}

	@Data
	public static class PreparedDocumentV2 {
		private DataServicesDocument dataServicesDocument;
		private UploadedAttachmentV2 imageAttachment;
		private UploadedAttachmentV2 xmlAttachment;
	}

	@Data
	public static class Prepared2Attachments {
		private UploadedAttachmentV2 imageAttachment;
		private UploadedAttachmentV2 xmlAttachment;
	}

	@Data
	@Builder
	public static class UploadedAttachmentV2 {

		private String attachmentId;
		private String thumbnailBigId;
		private String thumbnailSmallId;
		private File uploadedFile;
		private File thumbnailSmall;
		private File thumbnailBig;

	}

}
