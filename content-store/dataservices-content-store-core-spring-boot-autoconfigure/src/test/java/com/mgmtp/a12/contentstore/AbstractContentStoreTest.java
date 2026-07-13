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
package com.mgmtp.a12.contentstore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;

import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreAutoConfiguration;
import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreRepositoryConfiguration;
import com.mgmtp.a12.contentstore.content.internal.ContentService;
import com.mgmtp.a12.contentstore.content.internal.jpa.repository.ContentJpaRepository;
import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.contentstore.ticket.internal.TicketService;

/**
 * Base test class for content store with all necessary configurations to run repository/service tests
 */
@SpringBootTest(classes = { TestConfiguration.class,
	ContentStoreAutoConfiguration.class, ContentStoreRepositoryConfiguration.class })
public abstract class AbstractContentStoreTest extends AbstractTestNGSpringContextTests {

	public static final String UPLOAD_CONTENT = "hello";
	public static final String PRIVATE_UPLOAD_CONTENT = "privateContent";
	public static final String PUBLIC_UPLOAD_CONTENT = "publicContent";
	public static final String FILE_NAME = "filename.txt";
	public String privateContentId;
	public String publicContentId;

	@Autowired
	public ContentStoreService contentStoreService;
	@Autowired
	public ContentService contentService;
	@Autowired
	public TicketService ticketService;
	@Autowired
	public ApplicationContext applicationContext;
	@Autowired
	public ResourceLoader resourceLoader;
	@Autowired
	public ContentJpaRepository contentJpaRepository;

	@DataProvider
	public Object[][] mimeTypeContentCases() {

		return new Object[][] {
			new Object[] { "Attachment.css", "text/css" },
			new Object[] { "Attachment.csv", "text/csv" },
			new Object[] { "Attachment.doc", "application/msword" },
			new Object[] { "Attachment.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			new Object[] { "Attachment.html", "text/html" },
			new Object[] { "Attachment.js", "text/javascript" },
			new Object[] { "Attachment.pdf", "application/pdf" },
			new Object[] { "Attachment.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation" },
			new Object[] { "Attachment.webp", "image/webp" },
			new Object[] { "Attachment.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			new Object[] { "AttachmentGif.gif", "image/gif" },
			new Object[] { "AttachmentImage.jpg", "image/jpeg" },
			new Object[] { "AttachmentImagePng.png", "image/png" },
			new Object[] { "AttachmentJson.json", "application/json" },
			new Object[] { "AttachmentJsonArray.json", "application/json" },
			new Object[] { "AttachmentXML.xml", "application/xml" },
			new Object[] { "AttachmentJsonNoExt", "text/plain" }
		};
	}

	public ContentPersistenceResult persistContent(String contentId, String persistentType, byte[] contentBytes, String filename) {
		InputStream contentInputStream = new ByteArrayInputStream(contentBytes);
		return contentStoreService.saveContent(contentId, persistentType, contentInputStream, filename);
	}
}
