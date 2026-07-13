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
package com.mgmtp.a12.dataservices.document.uniqueconstraint.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.stream.Collectors;

import jakarta.annotation.Nullable;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.entity.DocumentUniqueConstraintEntity;
import com.mgmtp.a12.dataservices.document.persistence.internal.DocumentUniqueConstraintJpaRepository;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.UniqueConstraintViolationException;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.internal.UniqueConstraintHelper;
import com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResult;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the lifecycle of unique constraint tracking entries for documents.
 *
 * Handles validation, insertion, update, and deletion of rows in the
 * `DOCUMENT_UNIQUE_CONSTRAINT` table. The database unique index on
 * `(model_name, constraint_name, field_values_hash)` provides atomic
 * race-condition-safe enforcement.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniqueConstraintValidator {

	private static final String SHA_256 = "SHA-256";

	private final DocumentUniqueConstraintJpaRepository documentUniqueConstraintJpaRepository;
	private final DocumentModelReadRepository documentModelReadRepository;
	private final UniqueConstraintHelper uniqueConstraintHelper;

	/**
	 * Inserts unique constraint tracking entries for the given document.
	 *
	 * Must be called within an active transaction after document persistence.
	 * Relies on the database unique index for atomic race-condition-safe enforcement.
	 * Does nothing if the model defines no uniqueness criteria.
	 *
	 * @param document the document to insert tracking entries for.
	 * @param docRef the document reference, obtained from `DataServicesDocument`.
	 * @param locale the locale used to resolve the constraint's localized error message.
	 * @throws UniqueConstraintViolationException if a concurrent insert causes a duplicate.
	 */
	public void insert(DocumentV2 document, DocumentReference docRef, Locale locale) {
		String documentModelName = document.getDocumentModelId();
		List<IDocumentUniquenessCriterion> criteria = getConstraints(documentModelName);
		if (criteria.isEmpty()) {
			return;
		}
		doInsert(criteria, documentModelName, document, docRef, locale);
	}

	/**
	 * Updates unique constraint tracking entries for a modified document.
	 *
	 * Compares new field value hashes against existing tracking entries for the given `docRef`.
	 * If all hashes are unchanged the method is a no-op. If any hash differs, existing entries
	 * are deleted and new ones are inserted.
	 * Does nothing if the model defines no uniqueness criteria.
	 *
	 * @param document the updated document.
	 * @param docRef the document reference, obtained from `DataServicesDocument`.
	 * @param locale the locale used to resolve the constraint's localized error message.
	 * @throws UniqueConstraintViolationException if any new field value combination already exists.
	 */
	public void update(DocumentV2 document, DocumentReference docRef, Locale locale) {
		String documentModelName = document.getDocumentModelId();
		List<IDocumentUniquenessCriterion> criteria = getConstraints(documentModelName);
		if (criteria.isEmpty()) {
			return;
		}

		Map<String, String> existingHashes = documentUniqueConstraintJpaRepository.findByDocumentReference(docRef).stream()
			.collect(Collectors.toMap(
				DocumentUniqueConstraintEntity::getConstraintName,
				DocumentUniqueConstraintEntity::getFieldValuesHash));

		Map<String, String> newHashes = new LinkedHashMap<>(criteria.size());
		for (IDocumentUniquenessCriterion criterion : criteria) {
			newHashes.put(criterion.getName(), computeHash(document, criterion.getFieldFullNames()));
		}

		if (existingHashes.equals(newHashes)) {
			return;
		}

		documentUniqueConstraintJpaRepository.deleteByDocumentReference(docRef);
		doInsert(criteria, documentModelName, document, docRef, locale);
	}

	/**
	 * Removes all unique constraint tracking entries for the given document reference.
	 *
	 * @param docRef the document reference whose entries should be removed.
	 */
	public void deleteByDocRef(DocumentReference docRef) {
		documentUniqueConstraintJpaRepository.deleteByDocumentReference(docRef);
	}

	/**
	 * Cleans up unique constraint tracking entries when a Document Model is deleted.
	 *
	 * Deletes all tracking rows stored under `modelId` as `model_name`. With per-constraint
	 * topmost resolution, a model only owns rows for constraints where it is the topmost
	 * defining model. Shared constraint rows (stored under an ancestor model name) are
	 * unaffected and will be cleaned up when that ancestor is deleted.
	 *
	 * Must be called before the model header is removed so that the model hierarchy is
	 * still resolvable.
	 *
	 * @param modelId the id of the deleted Document Model.
	 */
	public void deleteByModel(String modelId) {
		log.debug("Deleting unique constraint tracking entries for model [{}]", modelId);
		documentUniqueConstraintJpaRepository.deleteByModelName(modelId);
	}

	/**
	 * Checks all uniqueness constraints defined in the model against the field values extracted
	 * from the given document.
	 *
	 * When `docRef` is provided, any conflict entry whose stored document reference equals
	 * `docRef` is excluded from the result (self-exclusion for update scenarios).
	 *
	 * @param modelName  the Document Model name.
	 * @param document   the document whose field values will be checked.
	 * @param docRef     the document reference of the document being updated; `null` for new
	 *                   documents.
	 * @return list of violated constraints; empty when all constraints are satisfied.
	 */
	public List<CheckUniquenessResult> checkAllConstraints(String modelName, DocumentV2 document,
		@Nullable DocumentReference docRef) {
		// Read constraints and locales from the model the caller submitted.
		// ILocalizedTextMap only exposes getOrDefault(Locale, String) — no entrySet/toMap.
		IDocumentModel model = documentModelReadRepository.readModel(modelName);
		List<IDocumentUniquenessCriterion> criteria = model.getContent().getDocumentUniquenessCriteria();
		// Locale objects from the model header — needed to enumerate ILocalizedTextMap entries
		// because ILocalizedTextMap only exposes getOrDefault(Locale, String).
		List<Locale> locales = model.getHeader().getLocales();
		if (criteria.isEmpty()) {
			return List.of();
		}

		List<CheckUniquenessResult> violations = new ArrayList<>();
		for (IDocumentUniquenessCriterion criterion : criteria) {
			String topmostModelName = uniqueConstraintHelper.findTopmostModelName(modelName, criterion.getName());
			Map<String, String> fieldValues = extractFieldValues(document, criterion.getFieldFullNames());
			String hash = computeHashFromValues(criterion.getFieldFullNames(), fieldValues);
			documentUniqueConstraintJpaRepository
				.findByModelNameAndConstraintNameAndFieldValuesHash(topmostModelName, criterion.getName(), hash)
				.stream()
				.map(DocumentUniqueConstraintEntity::getDocumentReference)
				.filter(conflicting -> !conflicting.equals(docRef))
				.findFirst()
				.ifPresent(conflicting -> violations.add(new CheckUniquenessResult(
					topmostModelName,
					criterion.getName(),
					conflicting,
					buildErrorMessageMap(criterion.getErrorMessage(), locales),
					criterion.getFieldFullNames(),
					buildErrorKey(topmostModelName, criterion.getName())
				)));
		}
		return Collections.unmodifiableList(violations);
	}

	private String buildErrorKey(String rootModelName, String constraintName) {
		return ExceptionKeys.UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY + "." + rootModelName + "." + constraintName;
	}

	private Map<String, String> buildErrorMessageMap(ILocalizedTextMap localizedTextMap, List<Locale> locales) {
		Map<String, String> result = new LinkedHashMap<>();
		for (Locale locale : locales) {
			String text = localizedTextMap.getOrDefault(locale, null);
			if (text != null) {
				result.put(locale.toLanguageTag(), text);
			}
		}
		return result;
	}

	private Map<String, String> extractFieldValues(DocumentV2 document, SequencedCollection<String> fieldPaths) {
		Map<String, String> values = new LinkedHashMap<>();
		for (String path : fieldPaths) {
			values.put(path, extractFieldValue(document, path));
		}
		return values;
	}

	public String computeHash(DocumentV2 document, SequencedCollection<String> fieldPaths) {
		String concatenated = fieldPaths.stream()
			.map(path -> encodeValue(extractFieldValue(document, path)))
			.collect(Collectors.joining());
		return sha256hex(concatenated);
	}

	private String computeHashFromValues(SequencedCollection<String> fieldPaths, Map<String, String> fieldValues) {
		String concatenated = fieldPaths.stream()
			.map(path -> encodeValue(fieldValues.get(path)))
			.collect(Collectors.joining());
		return sha256hex(concatenated);
	}

	/**
	 * Encodes a single field value using length-prefix encoding to prevent hash collisions
	 * when field values contain the separator character.
	 *
	 * Null is encoded as `"N:"`; non-null values as `"{len`:{value}"` where
	 * `len` is {@link String#length()}. This makes field boundaries unambiguous:
	 * `["a|b", "c"]` and `["a", "b|c"]` produce distinct encoded strings.
	 */
	private String encodeValue(String value) {
		if (value == null) {
			return "N:";
		}
		return value.length() + ":" + value;
	}

	private String extractFieldValue(DocumentV2 document, String fieldPath) {
		Object value = document.fieldValue(fieldPath);
		return value != null ? value.toString() : null;
	}

	private String sha256hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance(SHA_256);
			byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}

	private List<IDocumentUniquenessCriterion> getConstraints(String modelName) {
		return documentModelReadRepository.readModel(modelName).getContent().getDocumentUniquenessCriteria();
	}

	private void doInsert(List<IDocumentUniquenessCriterion> criteria,
		String documentModelName, DocumentV2 document, DocumentReference docRef, Locale locale) {
		for (IDocumentUniquenessCriterion criterion : criteria) {
			String topmostModelName = uniqueConstraintHelper.findTopmostModelName(documentModelName, criterion.getName());
			String hash = computeHash(document, criterion.getFieldFullNames());
			try {
				documentUniqueConstraintJpaRepository.saveAndFlush(new DocumentUniqueConstraintEntity(
					null, topmostModelName, criterion.getName(), hash, docRef));
			} catch (DataIntegrityViolationException e) {
				throwViolation(criterion, topmostModelName, locale);
			}
		}
	}

	private void throwViolation(IDocumentUniquenessCriterion criterion, String rootModelName, Locale locale) {
		String localizedMessage = criterion.getErrorMessage().getOrDefault(locale,
			"Unique constraint '%s' violated for model '%s'".formatted(criterion.getName(), rootModelName));
		log.warn("Unique constraint '{}' violated for model '{}' on fields {}",
			criterion.getName(), rootModelName, criterion.getFieldFullNames());
		throw new UniqueConstraintViolationException(criterion.getName(), rootModelName, localizedMessage);
	}
}
