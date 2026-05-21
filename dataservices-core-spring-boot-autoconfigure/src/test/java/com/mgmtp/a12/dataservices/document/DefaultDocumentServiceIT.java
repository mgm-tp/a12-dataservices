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
package com.mgmtp.a12.dataservices.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.AttachmentTestFunctions;
import com.mgmtp.a12.dataservices.AttachmentTestFunctions.Prepared2Attachments;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentReferenceType;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.dataservices.document.exception.DocumentValidationException;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.LinkPosition;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.query.PageSpec;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@SpringBootTest(properties = {
	"mgmtp.a12.dataservices.documents.delete.cascadeLinks.disabledForModels="
})
@Slf4j
public class DefaultDocumentServiceIT extends AbstractSpringContextIT {

	@Autowired private DefaultDocumentService documentService;
	@Autowired private KernelDocumentService kernelDocumentService;
	@Autowired private RelationshipLinkService relationshipLinkService;
	@Autowired private PlatformTransactionManager transactionManager;


	private DocumentReference docRefAddress;
	private DocumentReference docRefBusinessPartner;

	@BeforeMethod
	public void init() throws Exception {
		super.cleanUpTestEnvironment();
		modelsFunctions.createModels(
			PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PathConstants.ADDRESS_DOCUMENT_MODEL_PATH
		);

		docRefAddress = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, PathConstants.DOCUMENTS_PATH + "Address.json");

		docRefBusinessPartner = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2()
			.getDataServicesDocument().getMetadata().getDocRef();
		attachmentTestFunctions.prepare2Attachments();
	}

	@Test
	public void testGetDocRefFromDifferentModel() {
		DocumentReference wrongDocRef = new DocumentReference(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, docRefAddress.getDocumentId());
		Optional<DataServicesDocument> optDocument = documentService.load(wrongDocRef);
		assertTrue(optDocument.isEmpty());
	}

	@Test
	public void testLoadByDocRef() {
		Optional<DataServicesDocument> documentFound = documentService.load(docRefAddress);
		assertTrue(documentFound.isPresent());
		assertEquals(documentFound.get().getMetadata().getDocRef().getDocumentModelName(), DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
	}

	@Test
	public void testLoadForModel() {
		List<DocumentReference> documentRefFound = documentService.loadForModel(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL,
			PageRequest.of(PageSpec.NO_OFFSET, PageSpec.DEFAULT_MAX_RESULTS));
		assertFalse(documentRefFound.isEmpty());
		assertTrue(documentRefFound.size() <= PageSpec.DEFAULT_MAX_RESULTS);
		assertSame(documentRefFound.getFirst().getDocumentModelName(), DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);
	}

	@Test
	public void createDocWithInlineAttachment() throws IOException {
		DocumentV2 document =
			documentFunctions.getKernelDocumentFromFile(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
				PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-1.json");
		DataServicesDocument savedDocument = documentService.create(document, null);
		assertTrue(documentService.load(savedDocument.getMetadata().getDocRef()).isPresent());
	}

	@Test
	public void createInvalidDoc() throws IOException {

		DocumentV2 document = documentFunctions.getKernelDocumentFromFile(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.ATTACHMENT_PATH + "BusinessPartnerRuleViolation.json");
		runWithFieldOverwritten("validationEnabledByDefault", true, kernelDocumentService, () -> {
			try {
				documentService.create(document, null);
				Assert.fail();
			} catch (DocumentValidationException e) {
				Assert.assertEquals(e.getDocumentValidationResults().size(), 3);
				Assert.assertEquals(e.getDocumentValidationResults().get(0).getErrorText(), "The value is not allowed for the enumeration.");
				Assert.assertEquals(e.getDocumentValidationResults().get(1).getErrorText(), "Internal Error: Name must be filled.");
				Assert.assertEquals(e.getDocumentValidationResults().get(2).getErrorText(), "Internal Error: Industry must be filled.");
				Assert.assertEquals(e.getDocumentValidationResults().get(0).getErrorCode(), "stringFalschesMuster");
				Assert.assertEquals(e.getDocumentValidationResults().get(1).getErrorCode(), "ErrorR33");
				Assert.assertEquals(e.getDocumentValidationResults().get(2).getErrorCode(), "ErrorR34");
			}
		});
	}

	@Test
	public void createDocWithInlineAttachmentImage() throws IOException {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.ATTACHMENT_PATH + "BusinessPartnerWithInlineAttachment-ImagePlaceholder.json");
		InputStream unicornStream = resourceFunctions.loadResourceAsStream(PathConstants.DOCUMENTS_PATH + "unicorn.jpg");
		String image = Base64.getEncoder().withoutPadding().encodeToString(IOUtils.toByteArray(unicornStream));
		jsonDocument = String.format(jsonDocument, image, image.length());
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DataServicesDocument savedDocument = documentService.create(document, null);
		assertTrue(documentService.load(savedDocument.getMetadata().getDocRef()).isPresent());
	}

	@Test
	public void createDocument() throws IOException {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "Address.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DataServicesDocument savedDocument = documentService.create(document, null);
		assertTrue(documentService.load(savedDocument.getMetadata().getDocRef()).isPresent());
	}

	@Test
	public void createDocumentWithLocale() throws IOException {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "BusinessPartner-1.json");

		// Language only in model
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));
		// Language only in document
		DataServicesDocument savedDocument = documentService.create(document, new Locale.Builder().setLanguage("en").build());
		assertTrue(documentService.load(savedDocument.getMetadata().getDocRef()).isPresent());
		// Language and country in document
		savedDocument = documentService.create(document, new Locale.Builder().setLanguage("en").setRegion("US").build());
		assertTrue(documentService.load(savedDocument.getMetadata().getDocRef()).isPresent());
	}

	@Test
	public void createDocWithInternalAttachment() throws Exception {
		Prepared2Attachments prepared2Attachments = attachmentTestFunctions.prepare2Attachments();

		String jsonDocument = prepareJsonDocumentWithAttachments(prepared2Attachments);
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DataServicesDocument documentEntity = documentService.create(document, null);
		Assert.assertNotNull(documentEntity);
		assertAttachmentExists(documentEntity.getMetadata().getDocRef(), prepared2Attachments.getImageAttachment().getAttachmentId());
	}

	@Test(expectedExceptions = DataServicesDocumentSerializationException.class, expectedExceptionsMessageRegExp = "The deserialization of document null failed. The document will not be available for any data retrieval API")
	public void createBrokenDocument() throws IOException {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "BusinessPartnerBroken.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));
		documentService.create(document, null);
	}

	@Test
	public void createDocumentWithDateEntityInstanceAdded() throws IOException {
		String jsonDocument = resourceFunctions.loadResource(PathConstants.DOCUMENTS_PATH + "BusinessPartner-1.json");
		DocumentV2 document = documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(jsonDocument));
		DocumentPointer documentPointer = KernelUtils.of(
			List.of("BusinessPartnerRoot", "EndOfRelationship"),
			List.of(1, 1)
		);
		document.withFieldRemoved(documentPointer);

		document = document.withFieldValue(documentPointer, LocalDateTime.parse("2023-02-01T11:28:40").toInstant(ZoneOffset.UTC));
		DataServicesDocument savedDocument = documentService.create(document, null);
		assertTrue(documentService.load(savedDocument.getMetadata().getDocRef()).isPresent());
	}

	@Test
	public void updateDocWithAttachment() throws Exception {
		AttachmentTestFunctions.PreparedDocument preparedDocument = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();

		Prepared2Attachments newPrepared2Attachments = attachmentTestFunctions.prepare2Attachments();
		Optional<AttachmentUrl> attachment =
			attachmentService.findAttachmentUrl(preparedDocument.getImageAttachment().getAttachmentId(),
				preparedDocument.getDataServicesDocument().getMetadata().getDocRef());
		Assert.assertTrue(attachment.isPresent());

		String newJsonDocument = prepareJsonDocumentWithAttachments(newPrepared2Attachments);
		DocumentV2 newDocument =
			documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(newJsonDocument));
		DataServicesDocument newDocumentEntity =
			documentService.update(preparedDocument.getDataServicesDocument().getMetadata().getDocRef(), newDocument, null);
		Assert.assertNotNull(newDocumentEntity);
		Optional<AttachmentHeader> header = attachmentHeaderService.load(newPrepared2Attachments.getImageAttachment().getAttachmentId());
		Assert.assertTrue(header.isPresent());
		Assert.assertTrue(header.get().getReferences().contains(AttachmentReference.builder()
			.reference(newDocumentEntity.getMetadata().getDocRef())
			.type(AttachmentReferenceType.DOCUMENT)
			.build()));
	}

	@Test
	public void updateDocumentPartialModify() {
		List<DocumentPart> documentParts = new ArrayList<>();

		// Values that will be changed
		final String newName = "New Name";
		final String newDiscount = "New Discount";
		final BigDecimal newSize = BigDecimal.valueOf(3000);
		final String originalFileName1 = "internalFilename1";
		final String newFileName3 = "internal_filename_3";
		final String newMimeType3 = "image/png";
		final String newContent3 = "content3";
		final BigDecimal newSize3 = BigDecimal.valueOf(9000);
		final String partnerNotes = "Some notes";

		// Update existing data
		documentParts.add(constructPart("/BusinessPartnerRoot/Name", new int[] { 1, 1 }, newName));
		documentParts.add(constructPart("/BusinessPartnerRoot/CustomerDiscount", new int[] { 1, 1 }, newDiscount));
		documentParts.add(constructPart("/BusinessPartnerRoot/Attachment/size", new int[] { 1, 1, 1 }, newSize));
		documentParts.add(constructPart("/BusinessPartnerRoot/Industry", new int[] { 1, 1 }, null));
		documentParts.add(constructPart("/BusinessPartnerRoot/Notes", new int[] { 1, 1 }, partnerNotes));

		// Delete middle group
		documentParts.add(constructPart("/BusinessPartnerRoot/Attachment", new int[] { 1, 2 }, null));

		// Try to create with value null => will not be created
		documentParts.add(constructPart("/BusinessPartnerRoot/NonExisting", new int[] { 1, 1 }, null));

		// Create 3rd attachment group
		documentParts.add(constructPart("/BusinessPartnerRoot/Attachment/internal_filename", new int[] { 1, 3, 1 }, newFileName3));
		documentParts.add(constructPart("/BusinessPartnerRoot/Attachment/size", new int[] { 1, 3, 1 }, newSize3));
		documentParts.add(constructPart("/BusinessPartnerRoot/Attachment/mime_type", new int[] { 1, 3, 1 }, newMimeType3));
		documentParts.add(constructPart("/BusinessPartnerRoot/Attachment/content", new int[] { 1, 3, 1 }, newContent3));

		DocumentV2 partialModifyDocument = documentService.update(docRefBusinessPartner, documentParts, new Locale.Builder().setLanguage("en").build()).getKernelDocument();

		// Check modified document
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Name", new int[] { 1, 1 }, newName);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/CustomerDiscount", new int[] { 1, 1 }, newDiscount);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/size", new int[] { 1, 1, 1 }, newSize);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Industry", new int[] { 1, 1 }, null);
		assertGroupExists(partialModifyDocument, "/BusinessPartnerRoot/NonExisting", new int[] { 1, 1 }, false);

		// Check proper deletion of middle group
		assertGroupExists(partialModifyDocument, "/BusinessPartnerRoot/Attachment", new int[] { 1, 1 }, true);
		assertGroupExists(partialModifyDocument, "/BusinessPartnerRoot/Attachment", new int[] { 1, 2 }, true);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/internal_filename", new int[] { 1, 1, 1 }, originalFileName1);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/internal_filename", new int[] { 1, 2, 1 }, null);

		// New 3rd attachment group
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/internal_filename", new int[] { 1, 3, 1 }, newFileName3);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/size", new int[] { 1, 3, 1 }, newSize3);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/mime_type", new int[] { 1, 3, 1 }, newMimeType3);
		assertFieldExistsAndHasValue(partialModifyDocument, "/BusinessPartnerRoot/Attachment/content", new int[] { 1, 3, 1 }, newContent3);

		// Assert non-indexed field should not be indexed to search table -> field projection should return error non-indexed field
		QueryRoot query = DocumentUtils.buildQueryLoadDocumentByDocRef(docRefBusinessPartner);
		query.setFields(List.of("/BusinessPartnerRoot/Notes"));
		try {
			queryService.query(query, null);
		} catch (QueryValidationException e) {
			Assert.assertTrue(e.getMessage().contains("Field /BusinessPartnerRoot/Notes not found."));
		}
	}

	/**
	 * = Concurrent Document Update Test
	 *
	 * This integration test simulates two concurrent transactions both attempting
	 * to update the same document entity via {@link DocumentService#update(DocumentReference, DocumentV2, Locale)}.
	 *
	 * This test ensures that concurrent updates do not result in `javax.persistence.OptimisticLockException`.
	 *
	 * == Test Steps
	 *
	 * . Prepare a document with two attachments.
	 * . Create a new updated version of that document.
	 * . Run two separate threads in parallel, each executing the update in its own transaction.
	 * . Wait for both to complete, then assert that no `OptimisticLockException` occurred.
	 *
	 * == Purpose
	 *
	 * To reproduce a locking issue, this test can be run against different repository implementations:
	 *
	 * * Derived query version of `deleteDocumentFieldEntitiesByDocRef()` may throw `OptimisticLockException`.
	 * * `@Query`-based version should pass this test.
	 */
	@Test
	public void updateDocumentConcurrently() throws Exception {
		// --- Arrange --------------------------------------------------------------
		AttachmentTestFunctions.PreparedDocument preparedDocument =
			attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();

		Prepared2Attachments newPrepared2Attachments = attachmentTestFunctions.prepare2Attachments();

		Optional<AttachmentUrl> attachment = attachmentService.findAttachmentUrl(
			preparedDocument.getImageAttachment().getAttachmentId(),
			preparedDocument.getDataServicesDocument().getMetadata().getDocRef()
		);
		assertTrue(attachment.isPresent(), "Attachment should exist before update");

		String newJsonDocument = prepareJsonDocumentWithAttachments(newPrepared2Attachments);
		DocumentV2 newDocument = documentSupport.convertJSONToDocument(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			new StringReader(newJsonDocument)
		);

		// --- Act -----------------------------------------------------------------
		ExecutorService executor = Executors.newFixedThreadPool(2);

		Callable<Void> concurrentUpdateTask = () -> {
			TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
			txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			txTemplate.executeWithoutResult(status -> {
				documentService.update(preparedDocument.getDataServicesDocument().getMetadata().getDocRef(), newDocument, null);
				// Short artificial delay to increase likelihood of overlapping DB operations
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			});
			return null;
		};

		Future<Void> future1 = executor.submit(concurrentUpdateTask);
		Future<Void> future2 = executor.submit(concurrentUpdateTask);

		executor.shutdown();
		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		assertTrue(finished, "Executor did not finish within timeout");

		// --- Assert --------------------------------------------------------------
		boolean optimisticLockThrown = false;

		for (Future<Void> future : List.of(future1, future2)) {
			try {
				future.get();
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof OptimisticLockException ||
					(cause.getCause() instanceof OptimisticLockException)) {
					optimisticLockThrown = true;
					log.warn("Caught expected OptimisticLockException: {}", cause.toString());
				} else {
					log.error("Unexpected exception during concurrent update", cause);
					throw e;
				}
			}
		}

		assertFalse(optimisticLockThrown,
			"OptimisticLockException should not occur with @Query-based delete implementation");
	}

	@Test(expectedExceptions = NotFoundException.class)
	public void updateInNotExistScenarios() throws Exception {
		Prepared2Attachments newPrepared2Attachments = attachmentTestFunctions.prepare2Attachments();
		String newJsonDocument = prepareJsonDocumentWithAttachments(newPrepared2Attachments);
		DocumentReference documentReference = new DocumentReference(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, "randomDocId");
		DocumentV2 newDocument =
			documentSupport.convertJSONToDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, new StringReader(newJsonDocument));
		documentService.update(documentReference, newDocument, null);
	}

	@Test
	public void deleteDocWithAttachment() throws Exception {
		AttachmentTestFunctions.PreparedDocument preparedDocument = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();
		String thisImageAttachmentId = preparedDocument.getImageAttachment().getAttachmentId();
		DocumentReference docRef = preparedDocument.getDataServicesDocument().getMetadata().getDocRef();
		Optional<AttachmentUrl> attachment = attachmentService.findAttachmentUrl(thisImageAttachmentId, docRef);
		Assert.assertTrue(attachment.isPresent());

		documentService.delete(docRef);
		Assert.expectThrows(NotFoundException.class, () -> attachmentService.findAttachmentUrl(thisImageAttachmentId, docRef));
	}

	@Test
	public void testDeleteDocument() {
		DocumentReference documentReference = new DocumentReference(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, docRefAddress.getDocumentId());
		documentService.delete(documentReference);
		Assert.assertFalse(documentService.load(documentReference).isPresent());
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void testDeleteAccessDeniedScenarios() {
		DocumentReference documentReference = new DocumentReference(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL, docRefAddress.getDocumentId());
		setUserTo(UserConstants.GUEST_USER);
		documentService.delete(documentReference);
	}

	@Test
	public void testDeleteAllDocuments() throws Exception {
		AttachmentTestFunctions.PreparedDocument preparedDocument1 = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();
		AttachmentTestFunctions.PreparedDocument preparedDocument2 = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();
		List<DocumentReference> uploadedDocumentReferences =
			List.of(preparedDocument1.getDataServicesDocument().getMetadata().getDocRef(),
				preparedDocument2.getDataServicesDocument().getMetadata().getDocRef());

		documentService.deleteAll(uploadedDocumentReferences);

		assertEquals(uploadedDocumentReferences.stream()
			.map(documentService::load)
			.filter(Optional::isPresent)
			.toList().size(), 0);
	}

	@Test
	public void testDeleteAllDocuments_withAttachments() throws Exception {
		AttachmentTestFunctions.PreparedDocument preparedDocument1 = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();
		AttachmentTestFunctions.PreparedDocument preparedDocument2 = attachmentTestFunctions.prepareDocumentWith2AttachmentsV2();

		documentService.deleteAll(
			List.of(
				preparedDocument1.getDataServicesDocument().getMetadata().getDocRef(),
				preparedDocument2.getDataServicesDocument().getMetadata().getDocRef()
			)
		);

		assertThrows(
			NotFoundException.class,
			() -> attachmentService.findAttachmentUrl(
				preparedDocument1.getImageAttachment()
					.getAttachmentId(),
				preparedDocument1.getDataServicesDocument()
					.getMetadata().getDocRef()
			)
		);

		assertThrows(
			NotFoundException.class,
			() -> attachmentService.findAttachmentUrl(
				preparedDocument1.getXmlAttachment()
					.getAttachmentId(),
				preparedDocument1.getDataServicesDocument()
					.getMetadata().getDocRef()
			)
		);

		assertThrows(
			NotFoundException.class,
			() -> attachmentService.findAttachmentUrl(
				preparedDocument2.getImageAttachment()
					.getAttachmentId(),
				preparedDocument2.getDataServicesDocument()
					.getMetadata().getDocRef()
			)
		);

		assertThrows(
			NotFoundException.class,
			() -> attachmentService.findAttachmentUrl(
				preparedDocument2.getXmlAttachment()
					.getAttachmentId(),
				preparedDocument2.getDataServicesDocument()
					.getMetadata().getDocRef()
			)
		);
	}

	@Test
	public void testDeleteAllDocuments_withRelationshipLinks() throws IOException {
		DocumentReference partnerDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-1.json");
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		RelationshipLink relationshipLink = createPartnerContractLink(partnerDocRef, contractDocRef);
		documentService.deleteAll(List.of(partnerDocRef));

		assertThrows(NotFoundException.class, () -> relationshipLinkService.load(relationshipLink.getId()));
	}

	@Test(expectedExceptions = IntegrityException.class)
	public void testDeleteAllDocuments_asRelationshipLinkDocument() throws IOException {
		DocumentReference partnerDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_FILTERING_PATH + "BusinessPartner-1.json");
		DocumentReference contractDocRef = documentFunctions.createDocumentFromFileAndGetDocRef(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_SEARCH_PATH + "singleModelSort/Contract-beth.json");
		RelationshipLink relationshipLink = createPartnerContractLink(partnerDocRef, contractDocRef);

		documentService.deleteAll(List.of(relationshipLink.getLinkDocumentDocRef()));
	}

	private RelationshipLink createPartnerContractLink(DocumentReference partnerDocRef, DocumentReference contractDocRef) {
		RelationshipRoleSpec partnerRole = new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.PARTNER_ROLE, partnerDocRef);
		RelationshipRoleSpec contractRole = new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, contractDocRef);
		LinkDescriptor linkDescriptor =
			new LinkDescriptor(RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL, Arrays.asList(contractRole, partnerRole),
				LinkPosition.TOP);

		JsonNode rootGroup = objectMapper.createObjectNode().put("Name", "TestName");
		rootGroup = objectMapper.createObjectNode().set(DocumentModelConstants.FieldConstants.CO_INSURED_ADDITIONAL_FIELDS_ROOT, rootGroup);
		RelationshipLinkSpec relationshipLinkSpec = linksFunctions.addLink(linkDescriptor, rootGroup);
		return relationshipLinkService.load(relationshipLinkSpec.getId());
	}

	private String prepareJsonDocumentWithAttachments(Prepared2Attachments prepared2Attachments) throws IOException {
		Resource documentResource = resourceFunctions.loadResourceFromClassPath(PathConstants.ATTACHMENT_PATH + "BusinessPartnerWith2Attachments.json");
		String document = FileUtils.readFileToString(documentResource.getFile(), StandardCharsets.UTF_8);
		document = Strings.CS.replace(document, "{attachment_1}", prepared2Attachments.getImageAttachment().getAttachmentId());
		document = Strings.CS.replace(document, "{attachment_2}", prepared2Attachments.getXmlAttachment().getAttachmentId());

		return document;
	}

	private void assertFieldExistsAndHasValue(DocumentV2 document, String path, int[] repetitions, Object value) {
		DocumentPointer documentPointer = KernelUtils.fromPathAndRepetitions(path, repetitions);

		if (value == null) {
			assertNull(document.fieldValue(documentPointer));
		} else {
			Assert.assertNotNull(document.field(documentPointer));
			assertEquals(document.fieldValue(documentPointer).toString(), value.toString());
		}
	}

	private void assertGroupExists(DocumentV2 document, String path, int[] repetitions, boolean shouldExist) {
		DocumentPointer documentPointer = KernelUtils.fromPathAndRepetitions(path, repetitions);
		GroupInstanceV2 groupInstanceV2 = document.group(documentPointer);
		assertEquals((shouldExist), groupInstanceV2 != null);
	}

	private DocumentPart constructPart(String path, int[] repetitions, Object value) {
		return DocumentPart.builder().path(path).repetitions(repetitions).value(value).build();
	}
}
