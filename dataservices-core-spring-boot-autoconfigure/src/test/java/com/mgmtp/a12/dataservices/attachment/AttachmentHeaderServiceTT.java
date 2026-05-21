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
package com.mgmtp.a12.dataservices.attachment;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.reference.GenericReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class AttachmentHeaderServiceTT extends AbstractAttachmentTT {

	private static final String ATTACHMENT_ID_1 = "some attachment id";
	private static final String ATTACHMENT_ID_2 = "another attachment id";
	private static final String ATTACHMENT_ID_3 = "third attachment id";
	private static final String FILE_NAME = "some file name";
	private static final String MIME_TYPE = "some mime type";
	private static final String DOCUMENT_ID = "some document ID";
	private static final String MODEL_NAME = "some model name";
	private static final String ANNOTATION_NAME_1 = "someAnnotationName";
	private static final String ANNOTATION_VALUE_1 = "some annotation value";
	private static final String ANNOTATION_NAME_2 = "someOtherAnnotationName";
	private static final String ANNOTATION_VALUE_2 = "some other annotation value";
	private static final String ANNOTATION_NAME_3 = "stillSomeOtherAnnotationName";
	private static final String ANNOTATION_VALUE_3 = "still some other annotation value";
	private static final String DOCUMENT_REFERENCE = "somedocument/reference";
	private static final String DOCUMENT_REFERENCE_3 = "somedocument2/reference3";
	private static final DocumentReference DOC_REF_2 = new DocumentReference(MODEL_NAME, DOCUMENT_ID);
	private static final AttachmentReference<? extends GenericReference> ATTACHMENT_REFERENCE_2 =
		AttachmentReference.parse(AttachmentReferenceType.DOCUMENT, DOC_REF_2.toString());
	private static final AttachmentAnnotation ANNOTATION_1 = new AttachmentAnnotation(ANNOTATION_NAME_1, ANNOTATION_VALUE_1);
	private static final AttachmentAnnotation ANNOTATION_2 = new AttachmentAnnotation(ANNOTATION_NAME_2, ANNOTATION_VALUE_2);
	private static final AttachmentAnnotation ANNOTATION_3 = new AttachmentAnnotation(ANNOTATION_NAME_3, ANNOTATION_VALUE_3);

	@Autowired AttachmentHeaderService attachmentHeaderService;

	public AttachmentHeaderServiceTT(String storageType) {
		super(storageType);
	}

	@BeforeMethod public void init() {
		// Create an attachment for testing loading and updating functions
		AttachmentHeader ah = createAttachmentHeader(ATTACHMENT_ID_2);
		attachmentHeaderService.assignAttachment(ah, ATTACHMENT_REFERENCE_2);
	}

	@AfterMethod public void cleanUp() {
		attachmentHeaderService.load(ATTACHMENT_ID_1)
			.ifPresent(a -> attachmentHeaderService.delete(a.getAttachmentId()));
		attachmentHeaderService.load(ATTACHMENT_ID_2)
			.ifPresent(a -> attachmentHeaderService.delete(a.getAttachmentId()));
		attachmentHeaderService.load(ATTACHMENT_ID_3)
			.ifPresent(a -> attachmentHeaderService.delete(a.getAttachmentId()));
	}

	@Test public void testCreateAttachmentHeader() {
		List<AttachmentReference<? extends GenericReference>> references =
			List.of(Objects.requireNonNull(
				AttachmentReference.parse(AttachmentReferenceType.DOCUMENT, new DocumentReference(MODEL_NAME, DOCUMENT_ID).toString())));
		AttachmentHeader header = createAttachmentHeader(ATTACHMENT_ID_1);
		references.stream().forEach(ref -> attachmentHeaderService.assignAttachment(header, ref));
		assertNotNull(header);
		assertEquals(header.getAttachmentId(), ATTACHMENT_ID_1);
		assertEquals(header.getMimeType(), MIME_TYPE);
		assertAnnotation(header, ANNOTATION_1);
		assertAnnotation(header, ANNOTATION_2);
	}

	@Test public void testLoadAttachmentHeader() {
		Optional<AttachmentHeader> header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertEquals(header.get().getAttachmentId(), ATTACHMENT_ID_2);
		assertEquals(header.get().getMimeType(), MIME_TYPE);
		assertAnnotation(header.get(), ANNOTATION_1);
		assertAnnotation(header.get(), ANNOTATION_2);
	}

	@Test public void testAssignAttachment() {
		AttachmentReference<DocumentReference> reference = createAttachmentReference(new DocumentReference(DOCUMENT_REFERENCE));
		Optional<AttachmentHeader> header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertNotNull(header.get().getReferences());
		assertFalse(header.get().getReferences().contains(reference));

		attachmentHeaderService.assignAttachment(header.get(), reference);

		header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertNotNull(header.get().getReferences());
		assertTrue(header.get().getReferences().contains(reference));
	}

	@Test public void testAssignDuplicateAttachment() {
		AttachmentReference<DocumentReference> reference = createAttachmentReference(DOC_REF_2);
		Optional<AttachmentHeader> header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertNotNull(header.get().getReferences());
		int refCount = header.get().getReferences().size();
		assertTrue(header.get().getReferences().contains(reference));

		attachmentHeaderService.assignAttachment(header.get(), reference);

		header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertNotNull(header.get().getReferences());
		assertTrue(header.get().getReferences().contains(reference));
		// No new reference has been added
		assertEquals(header.get().getReferences().size(), refCount);
	}

	@Test public void testDeleteAttachment() {
		createAttachmentHeader(ATTACHMENT_ID_3);
		assertTrue(attachmentHeaderService.load(ATTACHMENT_ID_3).isPresent());

		attachmentHeaderService.delete(ATTACHMENT_ID_3);
		assertFalse(attachmentHeaderService.load(ATTACHMENT_ID_3).isPresent());
	}

	@Test public void testUpdateAttachmentReferences() {
		Optional<AttachmentHeader> header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertNotNull(header.get().getReferences());
		assertTrue(header.get().getReferences().contains(ATTACHMENT_REFERENCE_2));

		AttachmentReference<? extends GenericReference> reference = AttachmentReference.<DocumentReference>builder()
			.reference(new DocumentReference(DOCUMENT_REFERENCE_3))
			.type(AttachmentReferenceType.DOCUMENT)
			.build();

		attachmentHeaderService.assignAttachment(header.get(), reference);
		attachmentHeaderService.unAssignAttachment(header.get(), ATTACHMENT_REFERENCE_2);

		header = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(header.isPresent());
		assertNotNull(header.get().getReferences());
		assertTrue(header.get().getReferences().contains(reference));
		assertFalse(header.get().getReferences().contains(ATTACHMENT_REFERENCE_2));
	}

	@Test public void testUpdateAttachmentAnnotation() {
		// Check existing attachment header
		Optional<AttachmentHeader> optionalHeader = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(optionalHeader.isPresent());
		AttachmentHeader header = optionalHeader.get();
		assertEquals(header.getAttachmentId(), ATTACHMENT_ID_2);
		assertAnnotation(header, ANNOTATION_1);
		assertAnnotation(header, ANNOTATION_2);

		// Update annotation list
		List<AttachmentAnnotation> updatedAnnotations = header.getAnnotations();
		updatedAnnotations.add(ANNOTATION_3);
		header.setAnnotations(updatedAnnotations);
		attachmentHeaderService.create(header);

		// Check updated attachment header
		optionalHeader = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(optionalHeader.isPresent());
		header = optionalHeader.get();
		assertEquals(header.getAttachmentId(), ATTACHMENT_ID_2);
		assertAnnotation(header, ANNOTATION_1);
		assertAnnotation(header, ANNOTATION_2);
		assertAnnotation(header, ANNOTATION_3);

		// Check annotation list
		assertTrue(header.getAnnotations().containsAll(List.of(ANNOTATION_1, ANNOTATION_2, ANNOTATION_3)));
	}

	@Test public void testDeleteAttachmentAnnotation() {
		// Check existing attachment header
		Optional<AttachmentHeader> optionalHeader = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(optionalHeader.isPresent());
		AttachmentHeader header = optionalHeader.get();
		assertEquals(header.getAttachmentId(), ATTACHMENT_ID_2);
		assertAnnotation(header, ANNOTATION_1);
		assertAnnotation(header, ANNOTATION_2);

		// Delete annotation
		List<AttachmentAnnotation> annotations = header.getAnnotations();
		annotations.remove(ANNOTATION_1);
		header.setAnnotations(annotations);
		attachmentHeaderService.create(header);

		// Check deletion of annotation
		optionalHeader = attachmentHeaderService.load(ATTACHMENT_ID_2);
		assertTrue(optionalHeader.isPresent());
		header = optionalHeader.get();
		assertEquals(header.getAttachmentId(), ATTACHMENT_ID_2);
		// Only annotation_2 left (shifted to lower index)
		assertEquals(header.getAnnotations().size(), 1);
		assertAnnotation(header, ANNOTATION_2);
	}

	private AttachmentHeader createAttachmentHeader(String attachmentId) {
		return attachmentHeaderService.create(AttachmentHeader.builder()
			.attachmentId(attachmentId)
			.filename(FILE_NAME)
			.mimeType(MIME_TYPE)
			.annotations(List.of(ANNOTATION_1, ANNOTATION_2))
			.createdAt(Instant.now())
			.createdBy(UaaConnector.getCurrentUserName())
			.build());
	}

	private AttachmentReference<DocumentReference> createAttachmentReference(DocumentReference docRef) {
		return AttachmentReference.<DocumentReference>builder()
			.reference(docRef)
			.type(AttachmentReferenceType.DOCUMENT)
			.build();
	}

	private void assertAnnotation(AttachmentHeader header, AttachmentAnnotation annotation) {
		assertNotNull(header.getAnnotations());
		assertTrue(header.getAnnotations().contains(annotation));
	}
}

