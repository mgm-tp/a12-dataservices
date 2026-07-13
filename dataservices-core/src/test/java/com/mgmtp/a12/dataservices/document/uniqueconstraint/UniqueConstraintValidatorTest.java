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
package com.mgmtp.a12.dataservices.document.uniqueconstraint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.entity.DocumentUniqueConstraintEntity;
import com.mgmtp.a12.dataservices.document.persistence.internal.DocumentUniqueConstraintJpaRepository;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.UniqueConstraintViolationException;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.internal.UniqueConstraintHelper;
import com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResult;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelContent;
import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.model.header.Header;

/**
 * Unit tests for `UniqueConstraintValidator`.
 *
 * Tests are organized by method:
 * - `computeHash` — hash determinism, sensitivity to value and order
 * - `validate` — conflict detection with self-exclusion for update
 * - `insert` — tracking entry persistence, topmost model resolution, duplicate detection
 * - `update` — no-op when values unchanged, delete+re-insert when changed
 * - `deleteByDocRef` — delegation to repository
 * - `deleteByModelName` — always deletes tracking rows stored under the given model name
 * - `checkAllConstraints` — checks all constraints against a DocumentV2 document
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueConstraintValidatorTest {

	@InjectMocks
	private UniqueConstraintValidator validator;

	@Mock
	private DocumentUniqueConstraintJpaRepository repository;
	@Mock
	private DocumentModelReadRepository documentModelReadRepository;
	@Mock
	private UniqueConstraintHelper uniqueConstraintHelper;

	@BeforeMethod
	public void setUp() {
		Mockito.reset(repository, documentModelReadRepository, uniqueConstraintHelper);
	}

	// -------------------------------------------------------------------------
	// computeHash
	// -------------------------------------------------------------------------

	@Test(description = "Should produce a deterministic hash for the same field values")
	public void shouldComputeConsistentHashForSameValues() {
		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("Root/Name")).thenReturn("Alice");
		List<String> paths = List.of("Root/Name");

		String hash1 = validator.computeHash(document, paths);
		String hash2 = validator.computeHash(document, paths);

		Assert.assertEquals(hash1, hash2);
		Assert.assertFalse(hash1.isEmpty());
	}

	@Test(description = "Should produce different hashes when field values differ")
	public void shouldProduceDifferentHashForDifferentValues() {
		DocumentV2 docA = mock(DocumentV2.class);
		when(docA.fieldValue("Root/Name")).thenReturn("Alice");

		DocumentV2 docB = mock(DocumentV2.class);
		when(docB.fieldValue("Root/Name")).thenReturn("Bob");

		Assert.assertNotEquals(
			validator.computeHash(docA, List.of("Root/Name")),
			validator.computeHash(docB, List.of("Root/Name")));
	}

	@Test(description = "Should produce a consistent hash for null field values, distinct from any non-null value")
	public void shouldHandleNullFieldValueInHash() {
		DocumentV2 documentWithNull = mock(DocumentV2.class);
		when(documentWithNull.fieldValue("Root/Name")).thenReturn(null);

		DocumentV2 documentWithValue = mock(DocumentV2.class);
		when(documentWithValue.fieldValue("Root/Name")).thenReturn("someValue");

		// null is consistently hashed
		Assert.assertEquals(
			validator.computeHash(documentWithNull, List.of("Root/Name")),
			validator.computeHash(documentWithNull, List.of("Root/Name")));

		// null is distinct from any non-null value
		Assert.assertNotEquals(
			validator.computeHash(documentWithNull, List.of("Root/Name")),
			validator.computeHash(documentWithValue, List.of("Root/Name")));
	}

	@Test(description = "Should produce different hashes when a field value contains the separator character")
	public void shouldProduceDifferentHashWhenFieldValueContainsSeparatorCharacter() {
		DocumentV2 docA = mock(DocumentV2.class);
		when(docA.fieldValue("Root/F1")).thenReturn("a|b");
		when(docA.fieldValue("Root/F2")).thenReturn("c");

		DocumentV2 docB = mock(DocumentV2.class);
		when(docB.fieldValue("Root/F1")).thenReturn("a");
		when(docB.fieldValue("Root/F2")).thenReturn("b|c");

		Assert.assertNotEquals(
			validator.computeHash(docA, List.of("Root/F1", "Root/F2")),
			validator.computeHash(docB, List.of("Root/F1", "Root/F2")));
	}

	@Test(description = "Should produce different hashes when composite field order differs")
	public void shouldHashDependOnFieldOrder() {
		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("Root/First")).thenReturn("John");
		when(document.fieldValue("Root/Last")).thenReturn("Doe");

		Assert.assertNotEquals(
			validator.computeHash(document, List.of("Root/First", "Root/Last")),
			validator.computeHash(document, List.of("Root/Last", "Root/First")));
	}

	// -------------------------------------------------------------------------
	// insert
	// -------------------------------------------------------------------------

	@Test(description = "Should do nothing when the model has no uniqueness criteria")
	public void shouldSkipInsertWhenNoCriteria() {
		DocumentV2 document = mockDocument("MyModel");
		setupModel("MyModel", List.of());

		validator.insert(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH);

		verify(repository, never()).saveAndFlush(any());
	}

	@Test(description = "Should save a tracking entry with correct fields")
	public void shouldInsertTrackingEntryWithCorrectFields() {
		DocumentV2 document = mockDocument("MyModel");
		when(document.fieldValue("Root/Name")).thenReturn("Alice");

		setupModel("MyModel", List.of(buildConstraint("c1", "Root/Name")));

		validator.insert(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH);

		ArgumentCaptor<DocumentUniqueConstraintEntity> captor =
			ArgumentCaptor.forClass(DocumentUniqueConstraintEntity.class);
		verify(repository).saveAndFlush(captor.capture());

		DocumentUniqueConstraintEntity saved = captor.getValue();
		Assert.assertEquals(saved.getModelName(), "MyModel");
		Assert.assertEquals(saved.getConstraintName(), "c1");
		Assert.assertEquals(saved.getDocumentReference().toString(), "MyModel/doc-001");
		Assert.assertNotNull(saved.getFieldValuesHash());
	}

	@Test(description = "Should load constraints from and store entries under the topmost model when the document model has a parent")
	public void shouldUseTopmostModelNameOnInsert() {
		DocumentV2 document = mockDocument("ChildModel");
		when(document.fieldValue("Root/Name")).thenReturn("Alice");

		setupModelWithParent("ChildModel", "ParentModel", List.of(buildConstraint("c1", "Root/Name")));

		validator.insert(document, new DocumentReference("ChildModel/doc-001"), Locale.ENGLISH);

		ArgumentCaptor<DocumentUniqueConstraintEntity> captor =
			ArgumentCaptor.forClass(DocumentUniqueConstraintEntity.class);
		verify(repository).saveAndFlush(captor.capture());
		Assert.assertEquals(captor.getValue().getModelName(), "ParentModel");
	}

	@Test(description = "Should throw UniqueConstraintViolationException when DB unique index is violated")
	public void shouldThrowOnDuplicateHashInInsert() {
		DocumentV2 document = mockDocument("MyModel");
		when(document.fieldValue("Root/Name")).thenReturn("Alice");

		setupModel("MyModel", List.of(buildConstraint("c1", "Root/Name")));

		doThrow(new DataIntegrityViolationException("duplicate key"))
			.when(repository).saveAndFlush(any(DocumentUniqueConstraintEntity.class));

		UniqueConstraintViolationException ex = Assert.expectThrows(
			UniqueConstraintViolationException.class, () -> validator.insert(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH));
		Assert.assertEquals(ex.getConstraintName(), "c1");
		Assert.assertEquals(ex.getModelName(), "MyModel");
	}

	// -------------------------------------------------------------------------
	// update
	// -------------------------------------------------------------------------

	@Test(description = "Should do nothing when the model has no uniqueness criteria")
	public void shouldSkipUpdateWhenNoCriteria() {
		DocumentV2 document = mockDocument("MyModel");
		setupModel("MyModel", List.of());

		validator.update(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH);

		verify(repository, never()).findByDocumentReference(any());
		verify(repository, never()).deleteByDocumentReference(any());
		verify(repository, never()).saveAndFlush(any());
	}

	@Test(description = "Should do nothing when the constrained field values have not changed")
	public void shouldSkipUpdateWhenHashesUnchanged() {
		DocumentV2 document = mockDocument("MyModel");
		when(document.fieldValue("Root/Name")).thenReturn("Alice");

		setupModel("MyModel", List.of(buildConstraint("c1", "Root/Name")));

		String existingHash = validator.computeHash(document, List.of("Root/Name"));
		when(repository.findByDocumentReference(new DocumentReference("MyModel/doc-001")))
			.thenReturn(List.of(entity("doc-001", "MyModel", "c1", existingHash)));

		validator.update(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH);

		verify(repository, never()).deleteByDocumentReference(any());
		verify(repository, never()).saveAndFlush(any());
	}

	@Test(description = "Should delete old entries and insert new ones when a constrained field value changed")
	public void shouldDeleteAndReinsertWhenValueChanged() {
		DocumentV2 document = mockDocument("MyModel");
		when(document.fieldValue("Root/Name")).thenReturn("Bob");

		setupModel("MyModel", List.of(buildConstraint("c1", "Root/Name")));

		when(repository.findByDocumentReference(new DocumentReference("MyModel/doc-001")))
			.thenReturn(List.of(entity("doc-001", "MyModel", "c1", "old-hash-alice")));

		validator.update(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH);

		verify(repository).deleteByDocumentReference(new DocumentReference("MyModel/doc-001"));
		verify(repository).saveAndFlush(any(DocumentUniqueConstraintEntity.class));
	}

	@Test(description = "Should throw UniqueConstraintViolationException when changed values conflict with another document")
	public void shouldThrowWhenChangedValueConflictsWithAnotherDocument() {
		DocumentV2 document = mockDocument("MyModel");
		when(document.fieldValue("Root/Name")).thenReturn("Bob");

		setupModel("MyModel", List.of(buildConstraint("c1", "Root/Name")));

		when(repository.findByDocumentReference(new DocumentReference("MyModel/doc-001")))
			.thenReturn(List.of(entity("doc-001", "MyModel", "c1", "old-hash-alice")));

		doThrow(new DataIntegrityViolationException("duplicate key"))
			.when(repository).saveAndFlush(any(DocumentUniqueConstraintEntity.class));

		UniqueConstraintViolationException ex = Assert.expectThrows(
			UniqueConstraintViolationException.class, () -> validator.update(document, new DocumentReference("MyModel/doc-001"), Locale.ENGLISH));
		Assert.assertEquals(ex.getConstraintName(), "c1");
		Assert.assertEquals(ex.getModelName(), "MyModel");
	}

	// -------------------------------------------------------------------------
	// deleteByDocRef
	// -------------------------------------------------------------------------

	@Test(description = "Should delegate deleteByDocRef to the repository")
	public void shouldDelegateDeleteByDocRef() {
		validator.deleteByDocRef(new DocumentReference("MyModel/doc-001"));

		verify(repository).deleteByDocumentReference(new DocumentReference("MyModel/doc-001"));
	}

	// -------------------------------------------------------------------------
	// deleteByModelName
	// -------------------------------------------------------------------------

	@Test(description = "Should delete tracking entries by model name for any deleted model")
	public void shouldDeleteTrackingEntriesForDeletedModel() {
		validator.deleteByModel("MyModel");

		verify(repository).deleteByModelName("MyModel");
	}

	@Test(description = "Should delete tracking entries for a child model (cleans up its own constraint rows)")
	public void shouldDeleteTrackingEntriesForDeletedChildModel() {
		validator.deleteByModel("ChildModel");

		verify(repository).deleteByModelName("ChildModel");
	}

	// -------------------------------------------------------------------------
	// helpers
	// -------------------------------------------------------------------------

	private DocumentV2 mockDocument(String modelId) {
		DocumentV2 document = mock(DocumentV2.class);
		when(document.getDocumentModelId()).thenReturn(modelId);
		return document;
	}

	private IDocumentUniquenessCriterion buildConstraint(String constraintName, String... fieldPaths) {
		IDocumentUniquenessCriterion criterion = mock(IDocumentUniquenessCriterion.class);
		when(criterion.getName()).thenReturn(constraintName);
		Mockito.doReturn(new LinkedHashSet<>(List.of(fieldPaths))).when(criterion).getFieldFullNames();
		ILocalizedTextMap errorMessage = mock(ILocalizedTextMap.class);
		Mockito.lenient().doAnswer(inv -> inv.getArgument(1)).when(errorMessage).getOrDefault(any(), any());
		Mockito.lenient().when(criterion.getErrorMessage()).thenReturn(errorMessage);
		return criterion;
	}

	/** Stubs model constraints and topmost model resolution for a flat model (no supertype). */
	private void setupModel(String modelId, List<IDocumentUniquenessCriterion> criteria) {
		stubModel(modelId, criteria);
		Mockito.lenient().when(uniqueConstraintHelper.findTopmostModelName(eq(modelId), any())).thenReturn(modelId);
	}

	/** Stubs model constraints and topmost model resolution for a child model with a parent. */
	private void setupModelWithParent(String childModelId, String parentModelId,
		List<IDocumentUniquenessCriterion> criteria) {
		stubModel(childModelId, criteria);
		when(uniqueConstraintHelper.findTopmostModelName(eq(childModelId), any())).thenReturn(parentModelId);
	}

	private void stubModel(String modelId, List<IDocumentUniquenessCriterion> criteria) {
		IDocumentModelContent content = mock(IDocumentModelContent.class);
		when(content.getDocumentUniquenessCriteria()).thenReturn(criteria);
		IDocumentModel documentModel = mock(IDocumentModel.class);
		when(documentModel.getContent()).thenReturn(content);
		when(documentModelReadRepository.readModel(modelId)).thenReturn(documentModel);
	}

	private DocumentUniqueConstraintEntity entity(String docRef, String modelName,
		String constraintName, String hash) {
		return new DocumentUniqueConstraintEntity(1L, modelName, constraintName, hash,
			new DocumentReference(modelName + "/" + docRef));
	}

	/**
	 * Stubs model constraints, locale codes, and topmost model resolution for use with
	 * `checkAllConstraints` tests.
	 */
	private void stubModelWithLocales(String modelId, List<IDocumentUniquenessCriterion> criteria,
		List<Locale> locales) {
		IDocumentModelContent content = mock(IDocumentModelContent.class);
		when(content.getDocumentUniquenessCriteria()).thenReturn(criteria);
		Header header = mock(Header.class);
		when(header.getLocales()).thenReturn(locales);
		IDocumentModel documentModel = mock(IDocumentModel.class);
		when(documentModel.getContent()).thenReturn(content);
		when(documentModel.getHeader()).thenReturn(header);
		when(documentModelReadRepository.readModel(modelId)).thenReturn(documentModel);
		Mockito.lenient().when(uniqueConstraintHelper.findTopmostModelName(eq(modelId), any())).thenReturn(modelId);
	}

	@Test(description = "Should return empty list when the model has no uniqueness criteria")
	public void shouldReturnEmptyListWhenNoCriteriaForCheckAll() {
		// Given
		stubModelWithLocales("PersonModel", List.of(), List.of(Locale.ENGLISH));
		DocumentV2 document = mock(DocumentV2.class);

		// When
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, null);

		// Then
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}

	@Test(description = "Should return empty list when no document hash matches any constraint entry")
	public void shouldReturnEmptyListWhenNoConflictFound() {
		// Given
		IDocumentUniquenessCriterion criterion = buildConstraint("UniquePersonName", "/PersonRoot/firstName");
		stubModelWithLocales("PersonModel", List.of(criterion), List.of(Locale.ENGLISH));

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/firstName")).thenReturn("Alice");

		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniquePersonName"), any()))
			.thenReturn(List.of());

		// When
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, null);

		// Then
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}

	@Test(description = "Should return one violation when a single constraint is violated")
	public void shouldReturnOneViolationWhenSingleConstraintViolated() {
		// Given
		IDocumentUniquenessCriterion criterion = buildConstraint("UniquePersonName", "/PersonRoot/firstName");
		stubModelWithLocales("PersonModel", List.of(criterion), List.of(Locale.ENGLISH));

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/firstName")).thenReturn("Alice");

		DocumentUniqueConstraintEntity conflictEntity = entity("doc-002", "PersonModel", "UniquePersonName", "some-hash");
		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniquePersonName"), any()))
			.thenReturn(List.of(conflictEntity));

		// When
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, null);

		// Then
		Assert.assertEquals(results.size(), 1);
		CheckUniquenessResult violation = results.getFirst();
		Assert.assertEquals(violation.modelName(), "PersonModel");
		Assert.assertEquals(violation.constraintName(), "UniquePersonName");
		Assert.assertEquals(violation.conflictingDocRef().toString(), "PersonModel/doc-002");
		Assert.assertEquals(violation.errorKey(),
			ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY + ".PersonModel.UniquePersonName");
	}

	@Test(description = "Should return multiple violations when several constraints are violated")
	public void shouldReturnMultipleViolationsWhenMultipleConstraintsViolated() {
		// Given
		IDocumentUniquenessCriterion criterion1 = buildConstraint("UniqueFirstName", "/PersonRoot/firstName");
		IDocumentUniquenessCriterion criterion2 = buildConstraint("UniqueEmail", "/PersonRoot/email");
		stubModelWithLocales("PersonModel", List.of(criterion1, criterion2), List.of(Locale.ENGLISH));

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/firstName")).thenReturn("Alice");
		when(document.fieldValue("/PersonRoot/email")).thenReturn("alice@example.com");

		DocumentUniqueConstraintEntity conflict1 = entity("doc-002", "PersonModel", "UniqueFirstName", "hash1");
		DocumentUniqueConstraintEntity conflict2 = entity("doc-003", "PersonModel", "UniqueEmail", "hash2");
		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniqueFirstName"), any()))
			.thenReturn(List.of(conflict1));
		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniqueEmail"), any()))
			.thenReturn(List.of(conflict2));

		// When
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, null);

		// Then
		Assert.assertEquals(results.size(), 2);
		Assert.assertEquals(results.get(0).modelName(), "PersonModel");
		Assert.assertEquals(results.get(0).constraintName(), "UniqueFirstName");
		Assert.assertEquals(results.get(1).modelName(), "PersonModel");
		Assert.assertEquals(results.get(1).constraintName(), "UniqueEmail");
	}

	@Test(description = "Should exclude the self document when docRef is provided")
	public void shouldExcludeSelfDocRefFromConflictCheck() {
		// Given
		IDocumentUniquenessCriterion criterion = buildConstraint("UniquePersonName", "/PersonRoot/firstName");
		stubModelWithLocales("PersonModel", List.of(criterion), List.of(Locale.ENGLISH));

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/firstName")).thenReturn("Alice");

		// The only conflicting entry is the document being updated (self)
		DocumentUniqueConstraintEntity selfEntity = entity("doc-001", "PersonModel", "UniquePersonName", "some-hash");
		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniquePersonName"), any()))
			.thenReturn(List.of(selfEntity));

		// When — passing the same docRef as the conflicting entity
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, new DocumentReference("PersonModel/doc-001"));

		// Then — self is excluded, no violation
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}

	@Test(description = "Should treat missing field as null in hash computation")
	public void shouldTreatMissingFieldAsNull() {
		// Given — document does not have the constrained field (fieldValue returns null)
		IDocumentUniquenessCriterion criterion = buildConstraint("UniquePersonName", "/PersonRoot/firstName");
		stubModelWithLocales("PersonModel", List.of(criterion), List.of(Locale.ENGLISH));

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/firstName")).thenReturn(null);

		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniquePersonName"), any()))
			.thenReturn(List.of());

		// When — should not throw; missing field is treated as null
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, null);

		// Then
		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
		// Verify the repository was called (null was hashed and passed through)
		verify(repository).findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniquePersonName"), any());
	}

	@Test(description = "Should resolve nested field path via DocumentV2.fieldValue")
	public void shouldResolveNestedFieldPathViaDocumentV2() {
		// Given — two-level nested path /PersonRoot/address/city
		IDocumentUniquenessCriterion criterion = buildConstraint("UniqueCity", "/PersonRoot/address/city");
		stubModelWithLocales("PersonModel", List.of(criterion), List.of(Locale.ENGLISH));

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/address/city")).thenReturn("Munich");

		DocumentUniqueConstraintEntity conflictEntity = entity("doc-002", "PersonModel", "UniqueCity", "some-hash");
		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("PersonModel"), eq("UniqueCity"), any()))
			.thenReturn(List.of(conflictEntity));

		// When
		List<CheckUniquenessResult> results = validator.checkAllConstraints("PersonModel", document, null);

		// Then
		Assert.assertEquals(results.size(), 1);
		Assert.assertEquals(results.get(0).modelName(), "PersonModel");
		Assert.assertEquals(results.get(0).constraintName(), "UniqueCity");
	}

	@Test(description = "Should use topmost model name when checking all constraints for a child model")
	public void shouldUseTopmostModelNameWhenCheckingAllForChildModel() {
		// Given — child model with parent; child has the same constraint (required by modeling rule)
		IDocumentUniquenessCriterion criterion = buildConstraint("UniquePersonName", "/PersonRoot/firstName");
		// Stub the CHILD model — checkAllConstraints reads from the submitted model name
		IDocumentModelContent content = mock(IDocumentModelContent.class);
		when(content.getDocumentUniquenessCriteria()).thenReturn(List.of(criterion));
		Header header = mock(Header.class);
		when(header.getLocales()).thenReturn(List.of(Locale.ENGLISH));
		IDocumentModel childModel = mock(IDocumentModel.class);
		when(childModel.getContent()).thenReturn(content);
		when(childModel.getHeader()).thenReturn(header);
		when(documentModelReadRepository.readModel("ChildModel")).thenReturn(childModel);
		// Topmost for "UniquePersonName" in "ChildModel" is "ParentModel"
		when(uniqueConstraintHelper.findTopmostModelName("ChildModel", "UniquePersonName")).thenReturn("ParentModel");

		DocumentV2 document = mock(DocumentV2.class);
		when(document.fieldValue("/PersonRoot/firstName")).thenReturn("Alice");

		DocumentUniqueConstraintEntity conflictEntity = entity("doc-002", "ParentModel", "UniquePersonName", "some-hash");
		when(repository.findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("ParentModel"), eq("UniquePersonName"), any()))
			.thenReturn(List.of(conflictEntity));

		// When — pass the child model name
		List<CheckUniquenessResult> results = validator.checkAllConstraints("ChildModel", document, null);

		// Then — conflict reported with parent model entry
		Assert.assertEquals(results.size(), 1);
		Assert.assertEquals(results.get(0).modelName(), "ParentModel");
		Assert.assertEquals(results.get(0).constraintName(), "UniquePersonName");
		Assert.assertEquals(results.get(0).conflictingDocRef().toString(), "ParentModel/doc-002");
		// Verify the repository was called with the parent model name (topmost)
		verify(repository).findByModelNameAndConstraintNameAndFieldValuesHash(
			eq("ParentModel"), eq("UniquePersonName"), any());
	}
}
