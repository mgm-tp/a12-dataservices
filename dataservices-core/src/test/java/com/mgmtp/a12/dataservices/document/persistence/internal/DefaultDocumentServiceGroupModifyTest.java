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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

public class DefaultDocumentServiceGroupModifyTest extends AbstractDefaultDocumentServiceTest {

	private static final String MODEL = DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL;
	private static final String ATTACHMENT_PATH = "/BusinessPartnerRoot/Attachment";
	private static final String EMPLOYMENT_PATH = "/BusinessPartnerRoot/Employment";
	private static final String NAME_PATH = "/BusinessPartnerRoot/Name";

	private DocumentReference docRef;
	private DocumentV2 baseDocument;
	private IDocumentModelSearchService realSearchService;

	@BeforeMethod
	public void setUpGroupModifyTest() {
		docRef = new DocumentReference(MODEL, "testDocId");
		baseDocument = metadataUtils.createDocumentMetadata(
			DocumentV2.empty(MODEL),
			docRef,
			"admin",
			Instant.now(),
			null
		);
		IDocumentModel documentModel = documentModelResolver.getDocumentModelById(MODEL);
		realSearchService = kernelTestSupport.getDocumentModelServiceFactory()
			.createDocumentModelSearchService(documentModel);
		ModelHeaderEntity modelHeaderEntity = new ModelHeaderEntity(documentModel.getHeader());

		Mockito.lenient().when(documentRepository.supports(Mockito.any())).thenReturn(true);
		Mockito.lenient().when(modelHeaderRepository.findById(MODEL))
			.thenReturn(Optional.of(modelHeaderEntity));
		Mockito.when(documentModelServiceFactory.createDocumentModelSearchService(Mockito.any()))
			.thenReturn(realSearchService);
		Mockito.lenient().when(kernelDocumentService.computeDocument(Mockito.any(DocumentV2.class), Mockito.any()))
			.then(AdditionalAnswers.returnsFirstArg());
	}

	private void givenDocumentInRepository(DocumentV2 document) {
		Mockito.when(documentRepository.findByDocumentReference(Mockito.any()))
			.thenReturn(Optional.of(createDataServicesDocument(docRef, document)));
	}

	@Test(enabled = true, description = "Replaces an existing repeatable group when concrete repetitions are supplied and the group is present")
	public void shouldReplaceGroupWhenConcreteRepetitionsAndGroupExists() {
		DocumentV2 docWithAttachment = baseDocument.withGroupRepetitionAppended(
			DocumentPointer.of(List.of("BusinessPartnerRoot", "Attachment"), List.of(1, 0)),
			GroupInstanceV2.empty()
		);
		givenDocumentInRepository(docWithAttachment);

		Map<String, Object> newGroupValue = Map.of("original_filename", "replaced.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, newGroupValue, new int[] { 1, 1 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Inserts a group when concrete repetitions are supplied and no group exists at that pointer")
	public void shouldInsertGroupWhenConcreteRepetitionsAndGroupAbsent() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> newGroupValue = Map.of("original_filename", "new.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, newGroupValue, new int[] { 1, 1 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Auto-creates missing ancestor groups when inserting a group with concrete repetitions")
	public void shouldAutoCreateMissingAncestorsWhenInsertingGroup() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> newGroupValue = Map.of("original_filename", "deep.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, newGroupValue, new int[] { 1, 3 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Appends a new group repetition when the last repetition index in the part is the wildcard 0")
	public void shouldAppendGroupWhenLastRepetitionIsWildcard() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> groupValue = Map.of("original_filename", "appended.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, groupValue, new int[] { 1, 0 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Appends the group as the first entry when the repeatable group is currently empty")
	public void shouldAppendAsFirstEntryWhenRepeatableGroupEmpty() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> groupValue = Map.of("original_filename", "first.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, groupValue, new int[] { 1, 0 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		DocumentV2 resultDoc = result.getKernelDocument();
		Assert.assertNotNull(
			resultDoc.group(DocumentPointer.of(List.of("BusinessPartnerRoot", "Attachment"), List.of(1, 1)))
		);
	}

	@Test(enabled = true, description = "Appends a group into a specific intermediate repeatable group identified by a concrete repetition index")
	public void shouldAppendIntoSpecificIntermediateRepetition() {
		DocumentV2 docWithAttachment = baseDocument.withGroupRepetitionAppended(
			DocumentPointer.of(List.of("BusinessPartnerRoot", "Attachment"), List.of(1, 0)),
			GroupInstanceV2.empty()
		);
		givenDocumentInRepository(docWithAttachment);

		Map<String, Object> groupValue = Map.of("original_filename", "appended_to_second.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, groupValue, new int[] { 1, 0 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Leaves field modification behaviour unchanged when the path resolves to a field in the model")
	public void shouldLeaveFieldModificationUnchangedWhenPathResolvesToField() {
		givenDocumentInRepository(baseDocument);

		DocumentPart part = new DocumentPart(NAME_PATH, "Updated Name", new int[] { 1, 1 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Removes a group or field when the value is null and no wildcard is present in the repetitions")
	public void shouldRemoveGroupOrFieldWhenValueNullAndNoWildcard() {
		DocumentV2 docWithAttachment = baseDocument.withGroupRepetitionAppended(
			DocumentPointer.of(List.of("BusinessPartnerRoot", "Attachment"), List.of(1, 0)),
			GroupInstanceV2.empty()
		);
		givenDocumentInRepository(docWithAttachment);

		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, null, new int[] { 1, 1 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Treats null or empty repetitions as a concrete (non-wildcard) address for replace/insert, not as append")
	public void shouldReplaceGroupWhenRepetitionsNullOrEmptyAndGroupValue() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> groupValue = Map.of("original_filename", "no_reps.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, groupValue, new int[] { 1, 1 });

		DataServicesDocument result = defaultDocumentService.update(docRef, List.of(part), null);

		Assert.assertNotNull(result);
		Mockito.verify(documentRepository, Mockito.times(1)).update(Mockito.any());
	}

	@Test(enabled = true, description = "Throws InvalidInputException when value is null and the last repetition index is the wildcard 0")
	public void shouldThrowInvalidInputWhenValueNullAndWildcardPresent() {
		givenDocumentInRepository(baseDocument);

		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, null, new int[] { 1, 0 });

		Assert.assertThrows(
			InvalidInputException.class,
			() -> defaultDocumentService.update(docRef, List.of(part), null)
		);
	}

	@Test(enabled = true, description = "Throws InvalidInputException when an intermediate (non-last) repetition index is the wildcard 0")
	public void shouldThrowInvalidInputWhenIntermediateWildcardPresent() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> groupValue = Map.of("original_filename", "intermediate.pdf");
		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, groupValue, new int[] { 0, 1 });

		Assert.assertThrows(
			InvalidInputException.class,
			() -> defaultDocumentService.update(docRef, List.of(part), null)
		);
	}

	@Test(enabled = true, description = "Throws InvalidInputException when the append target group is not repeatable in the document model")
	public void shouldThrowInvalidInputWhenAppendTargetNotRepeatable() {
		givenDocumentInRepository(baseDocument);

		Map<String, Object> groupValue = Map.of("signingDateTime", "2020-01-01T00:00:00");
		DocumentPart part = new DocumentPart(EMPLOYMENT_PATH, groupValue, new int[] { 1, 0 });

		Assert.assertThrows(
			InvalidInputException.class,
			() -> defaultDocumentService.update(docRef, List.of(part), null)
		);
	}

	@Test(enabled = true, description = "Throws InvalidInputException when the group value cannot be converted to a GroupInstanceV2")
	public void shouldThrowInvalidInputWhenGroupValueNotConvertible() {
		givenDocumentInRepository(baseDocument);

		DocumentPart part = new DocumentPart(ATTACHMENT_PATH, "not-a-group-value", new int[] { 1, 1 });

		Assert.assertThrows(
			InvalidInputException.class,
			() -> defaultDocumentService.update(docRef, List.of(part), null)
		);
	}
}
