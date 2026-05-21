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
package com.mgmtp.a12.dataservices.document.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.metadata.internal.DocumentMetadataMetaModelProvider;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

public class MetadataUtilsTest {

	private MetadataUtils metadataUtils;
	private DocumentMetadataMetaModelProvider modelProvider;
	private DocumentModelServiceFactory documentModelServiceFactory;
	private IDocumentModelSearchService searchService;
	private IDocumentModel metadataModel;

	@BeforeMethod
	public void setUp() {
		modelProvider = mock(DocumentMetadataMetaModelProvider.class);
		documentModelServiceFactory = mock(DocumentModelServiceFactory.class);
		searchService = mock(IDocumentModelSearchService.class);
		metadataModel = mock(IDocumentModel.class);

		when(modelProvider.getModel()).thenReturn(metadataModel);
		when(documentModelServiceFactory.createDocumentModelSearchService(metadataModel)).thenReturn(searchService);

		// Default: all fields are present
		when(searchService.getByPath(anyString())).thenReturn(Optional.of(mock(IElement.class)));

		metadataUtils = new MetadataUtils(modelProvider, documentModelServiceFactory);
	}

	@DataProvider
	public Object[][] documentMetadataScenarios() {
		return new Object[][] {
			{"TestModel_DM", "DOC-001", "user1", "1.0"},
			{"Contract_DM", "CONTRACT-123", "admin", "2.5"},
			{"Project_DM", "PRJ-XYZ", "service-account", "3.0.1"}
		};
	}

	@DataProvider
	public Object[][] modelVersionScenarios() {
		return new Object[][] {
			{"1.0"},
			{"2.5.3"},
			{"10.0.0"}
		};
	}

	@DataProvider
	public Object[][] cddMetadataScenarios() {
		return new Object[][] {
			{"Model_A", "ID-A", "1.0"},
			{"Model_B", "ID-B", "2.0"},
			{"Model_C", "ID-C", "3.5.1"}
		};
	}

	@DataProvider
	public Object[][] userNameScenarios() {
		return new Object[][] {
			{"john.doe"},
			{"admin"},
			{"service-account-123"},
			{"user@domain.com"}
		};
	}

	@DataProvider
	public Object[][] timestampScenarios() {
		return new Object[][] {
			{Instant.parse("2025-01-01T00:00:00Z")},
			{Instant.parse("2025-06-15T12:30:45Z")},
			{Instant.parse("2025-12-31T23:59:59Z")},
			{Instant.now()}
		};
	}

	@DataProvider
	public Object[][] documentReferenceScenarios() {
		return new Object[][] {
			{"SimpleModel_DM", "ID-001"},
			{"ComplexModel_DM", "COMPLEX-XYZ-999"},
			{"Model_With_Underscores_DM", "REF_WITH_UNDERSCORES"},
			{"Short_DM", "A"}
		};
	}

	@Test(description = "Should create document metadata with all required fields when provided valid input", dataProvider = "documentMetadataScenarios")
	public void shouldCreateDocumentMetadataWhenValidInput(String modelName, String docId, String userName, String modelVersion) {
		// Given
		DocumentReference docRef = new DocumentReference(modelName, docId);
		Instant timestamp = Instant.parse("2025-10-29T10:15:30Z");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, userName, timestamp, modelVersion);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(resultDocument);
	}

	@Test(description = "Should create CDD metadata with model reference, docRef and version")
	public void shouldCreateCddMetadataWhenCalled() {
		// Given
		DocumentReference docRef = new DocumentReference("TestModel_DM", "DOC-001");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);

		// When
		DocumentV2 result = metadataUtils.createMandatoryMetadata(document, docRef);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(updatedDocument);
	}



	@Test(description = "Should create CDD metadata for different models and versions", dataProvider = "cddMetadataScenarios")
	public void shouldCreateCddMetadataForVariousScenarios(String modelName, String docId, String version) {
		// Given
		DocumentReference docRef = new DocumentReference(modelName, docId);
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);

		// When
		DocumentV2 result = metadataUtils.createMandatoryMetadata(document, docRef);

		// Then
		assertThat(result).isNotNull();
	}

	@Test(description = "Should update document metadata with modifier and modifiedAt while preserving original metadata")
	public void shouldUpdateDocumentMetadataWhenCalled() {
		// Given
		DocumentV2 originalDocument = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);
		GroupInstanceV2 metadataGroup = mock(GroupInstanceV2.class);
		GroupInstanceV2 extensionsGroup = mock(GroupInstanceV2.class);

		String userName = "updater";
		Instant timestamp = Instant.parse("2025-10-29T12:00:00Z");

		when(originalDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)).thenReturn(metadataGroup);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(extensionsGroup);
		when(updatedDocument.withGroup(eq(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH), eq(metadataGroup)))
			.thenReturn(updatedDocument);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.updateDocumentMetadata(originalDocument, updatedDocument, userName, timestamp);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(resultDocument);
	}



	@Test(description = "Should handle different user name formats when creating metadata", dataProvider = "userNameScenarios")
	public void shouldHandleDifferentUserNamesWhenCreatingMetadata(String userName) {
		// Given
		DocumentReference docRef = new DocumentReference("Test_DM", "DOC-001");
		Instant timestamp = Instant.now();
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, userName, timestamp, "1.0");

		// Then
		assertThat(result).isNotNull();
	}

	@Test(description = "Should preserve extensions group when updating document metadata")
	public void shouldPreserveExtensionsGroupWhenUpdatingMetadata() {
		// Given
		DocumentV2 originalDocument = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 intermediateDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);
		GroupInstanceV2 originalMetadataGroup = mock(GroupInstanceV2.class);
		GroupInstanceV2 extensionsGroup = mock(GroupInstanceV2.class);

		when(originalDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)).thenReturn(originalMetadataGroup);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(extensionsGroup);
		when(updatedDocument.withGroup(eq(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH), eq(originalMetadataGroup)))
			.thenReturn(intermediateDocument);
		when(intermediateDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.updateDocumentMetadata(
			originalDocument,
			updatedDocument,
			"testUser",
			Instant.now()
		);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(resultDocument);
	}



	@Test(description = "Should handle different timestamps when creating metadata", dataProvider = "timestampScenarios")
	public void shouldHandleDifferentTimestampsWhenCreatingMetadata(Instant timestamp) {
		// Given
		DocumentReference docRef = new DocumentReference("Test_DM", "DOC-001");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, "user", timestamp, "1.0");

		// Then
		assertThat(result).isNotNull();
	}

	@Test(description = "Should handle different model version types when creating metadata", dataProvider = "modelVersionScenarios")
	public void shouldHandleDifferentModelVersionTypesWhenCreatingMetadata(Object modelVersion) {
		// Given
		DocumentReference docRef = new DocumentReference("Test_DM", "DOC-001");
		Instant timestamp = Instant.now();
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, "user", timestamp, modelVersion);

		// Then
		assertThat(result).isNotNull();
	}

	@Test(description = "Should create metadata with same creator and modifier for new documents")
	public void shouldCreateMetadataWithSameCreatorAndModifierWhenNew() {
		// Given
		String userName = "creator-user";
		DocumentReference docRef = new DocumentReference("Test_DM", "NEW-DOC");
		Instant timestamp = Instant.parse("2025-10-29T10:00:00Z");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(updatedDocument);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, userName, timestamp, "1.0");

		// Then
		assertThat(result).isNotNull();
	}

	@Test(description = "Should only update modifier metadata when updating existing document")
	public void shouldOnlyUpdateModifierMetadataWhenUpdating() {
		// Given
		DocumentV2 originalDocument = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);
		GroupInstanceV2 originalMetadataGroup = mock(GroupInstanceV2.class);
		GroupInstanceV2 originalExtensions = mock(GroupInstanceV2.class);
		GroupInstanceV2 extensionsGroup = mock(GroupInstanceV2.class);

		String currentModifier = "current-modifier";
		Instant updateTime = Instant.parse("2025-10-29T15:00:00Z");

		when(originalDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)).thenReturn(originalMetadataGroup);
		when(originalDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(originalExtensions);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(extensionsGroup);
		when(updatedDocument.withGroup(eq(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH), eq(originalMetadataGroup)))
			.thenReturn(updatedDocument);
		when(updatedDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.updateDocumentMetadata(originalDocument, updatedDocument, currentModifier, updateTime);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(resultDocument);
	}

	@Test(description = "Should detect missing modelVersion field when not present in model")
	public void shouldDetectMissingModelVersionFieldWhenNotInModel() {
		// Given
		DocumentMetadataMetaModelProvider documentMetadataMetaModelProvider = mock(DocumentMetadataMetaModelProvider.class);
		DocumentModelServiceFactory customFactory = mock(DocumentModelServiceFactory.class);
		IDocumentModel customMetadataModel = mock(IDocumentModel.class);
		IDocumentModelSearchService customSearchService = mock(IDocumentModelSearchService.class);

		when(documentMetadataMetaModelProvider.getModel()).thenReturn(customMetadataModel);
		when(customFactory.createDocumentModelSearchService(customMetadataModel)).thenReturn(customSearchService);

		// Set default first, then specific override
		when(customSearchService.getByPath(anyString())).thenReturn(Optional.of(mock(IElement.class)));
		String modelVersionPath = DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH
			+ DocumentMetadataConstants.DOCUMENT_METADATA_PATH_SEPARATOR + "modelVersion";
		when(customSearchService.getByPath(modelVersionPath)).thenReturn(Optional.empty());

		// When
		MetadataUtils utils = new MetadataUtils(documentMetadataMetaModelProvider, customFactory);

		// Then
		assertThat(utils.isHasModelVersionField()).isFalse();
		assertThat(utils.isHasCreatorField()).isTrue();
		assertThat(utils.isHasCreatedAtField()).isTrue();
		assertThat(utils.isHasModifierField()).isTrue();
		assertThat(utils.isHasModifiedAtField()).isTrue();
	}

	@Test(description = "Should handle model with no optional fields present")
	public void shouldHandleModelWithNoOptionalFieldsWhenMinimal() {
		// Given
		IDocumentModelSearchService customSearchService = mock(IDocumentModelSearchService.class);
		when(documentModelServiceFactory.createDocumentModelSearchService(metadataModel)).thenReturn(customSearchService);
		when(customSearchService.getByPath(anyString())).thenReturn(Optional.empty());

		// When
		MetadataUtils utils = new MetadataUtils(modelProvider, documentModelServiceFactory);

		// Then
		assertThat(utils.isHasModelVersionField()).isFalse();
		assertThat(utils.isHasCreatorField()).isFalse();
		assertThat(utils.isHasCreatedAtField()).isFalse();
		assertThat(utils.isHasModifierField()).isFalse();
		assertThat(utils.isHasModifiedAtField()).isFalse();
	}

	@Test(description = "Should create metadata without optional fields when fields not present in model")
	public void shouldCreateMetadataWithoutOptionalFieldsWhenNotInModel() {
		// Given
		IDocumentModelSearchService customSearchService = mock(IDocumentModelSearchService.class);
		when(documentModelServiceFactory.createDocumentModelSearchService(metadataModel)).thenReturn(customSearchService);
		when(customSearchService.getByPath(anyString())).thenReturn(Optional.empty());

		MetadataUtils utils = new MetadataUtils(modelProvider, documentModelServiceFactory);

		DocumentReference docRef = new DocumentReference("Test_DM", "DOC-001");
		Instant timestamp = Instant.parse("2025-10-29T10:00:00Z");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 mandatoryDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(mandatoryDocument);
		when(mandatoryDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(mandatoryDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = utils.createDocumentMetadata(document, docRef, "user", timestamp, "1.0");

		// Then
		assertThat(result).isNotNull();
		verify(document).withBatchUpdates(any(List.class));
		verify(mandatoryDocument).withBatchUpdates(any(List.class));	}

	@Test(description = "Should update metadata without extensions group when extensions group is null")
	public void shouldUpdateMetadataWithoutExtensionsWhenExtensionsNull() {
		// Given
		DocumentV2 originalDocument = mock(DocumentV2.class);
		DocumentV2 updatedDocument = mock(DocumentV2.class);
		DocumentV2 intermediateDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);
		GroupInstanceV2 originalMetadataGroup = mock(GroupInstanceV2.class);

		when(originalDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)).thenReturn(originalMetadataGroup);
		when(updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(updatedDocument.withGroup(eq(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH), eq(originalMetadataGroup)))
			.thenReturn(intermediateDocument);
		when(intermediateDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.updateDocumentMetadata(originalDocument, updatedDocument, "user", Instant.now());

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(resultDocument);
	}

	@Test(description = "Should create mandatory metadata with model reference and docRef")
	public void shouldCreateMandatoryMetadataWhenCalled() {
		// Given
		DocumentReference docRef = new DocumentReference("TestModel_DM", "DOC-123");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createMandatoryMetadata(document, docRef);

		// Then
		assertThat(result).isNotNull();
		verify(document).withBatchUpdates(any(List.class));
	}

	@Test(description = "Should handle document with extensions group when creating metadata")
	public void shouldHandleDocumentWithExtensionsWhenCreatingMetadata() {
		// Given
		DocumentReference docRef = new DocumentReference("Test_DM", "DOC-001");
		Instant timestamp = Instant.parse("2025-10-29T10:00:00Z");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 mandatoryDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);
		GroupInstanceV2 extensionsGroup = mock(GroupInstanceV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(mandatoryDocument);
		when(mandatoryDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(extensionsGroup);
		when(mandatoryDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, "user", timestamp, "1.0");

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(resultDocument);
	}

	@Test(description = "Should handle document without extensions group when creating metadata")
	public void shouldHandleDocumentWithoutExtensionsWhenCreatingMetadata() {
		// Given
		DocumentReference docRef = new DocumentReference("Test_DM", "DOC-001");
		Instant timestamp = Instant.parse("2025-10-29T10:00:00Z");
		DocumentV2 document = mock(DocumentV2.class);
		DocumentV2 mandatoryDocument = mock(DocumentV2.class);
		DocumentV2 resultDocument = mock(DocumentV2.class);

		when(document.withBatchUpdates(any(List.class))).thenReturn(mandatoryDocument);
		when(mandatoryDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH)).thenReturn(null);
		when(mandatoryDocument.withBatchUpdates(any(List.class))).thenReturn(resultDocument);

		// When
		DocumentV2 result = metadataUtils.createDocumentMetadata(document, docRef, "user", timestamp, "1.0");

		// Then
		assertThat(result).isNotNull();
		verify(mandatoryDocument).withBatchUpdates(any(List.class));
	}
}
