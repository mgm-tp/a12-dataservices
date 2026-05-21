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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mgmtp.a12.dataservices.document.exception.InvalidDocumentReferenceException;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceDeserializer;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceFromStringConverter;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceSerializer;
import com.mgmtp.a12.dataservices.reference.GenericReference;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a reference to a document by its model name and document id.
 * Neither part may contain "/" or "..". The model name must have at least two characters.
 */
@JsonSerialize(using = DocumentReferenceSerializer.class)
@JsonDeserialize(converter = DocumentReferenceFromStringConverter.class, using = DocumentReferenceDeserializer.class)
@Data @EqualsAndHashCode @Builder
public class DocumentReference implements Comparable<DocumentReference>, GenericReference {

	protected static final String SEPARATOR = "/";
	public static final Pattern MODEL_NAME_PATTERN = Pattern.compile("[_a-zA-Z][-_.a-zA-Z0-9]{0,99}");

	private String documentModelName;
	private String documentId;

	/**
	 * Creates an empty reference instance for usage in extending classes.
	 * Fields must be assigned via setters before use.
	 */
	protected DocumentReference() {
	}

	/**
	 * Creates a reference from separate model name and document id, validating the inputs.
	 *
	 * @param documentModelName Name of the document model; must be at least two characters and match {@link #MODEL_NAME_PATTERN}.
	 * @param documentId Identifier of the document; must not be blank.
	 * @throws InvalidDocumentReferenceException If validation fails.
	 */
	public DocumentReference(String documentModelName, String documentId) {
		setIfValid(documentModelName, documentId);
	}

	/**
	 * Creates a reference from a combined string of the form "DocumentModelName/DocumentId", validating the input.
	 *
	 * @param docRef Combined reference value; must contain a single "/" separator and no relative path segments.
	 * @throws InvalidDocumentReferenceException If validation fails.
	 */
	public DocumentReference(String docRef) {
		setIfValid(docRef);
	}

	/**
	 * Checks whether this reference is structurally valid (non-blank model name and id).
	 *
	 * @return `true` if both parts are non-blank; otherwise `false`.
	 */
	public boolean isValid() {
		return StringUtils.isNotBlank(documentModelName) && StringUtils.isNotBlank(documentId);
	}

	/**
	 * Formats the reference as "model/id".
	 *
	 * @return Concatenation of model name, separator "/", and document id.
	 */
	@Override public String toString() {
		return this.documentModelName.concat(SEPARATOR).concat(this.documentId);
	}

	@Override public int length() {
		return toString().length();
	}

	@Override public char charAt(int index) {
		return toString().charAt(index);
	}

	@Override public CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}

	/**
	 * Compares references by model name first, then by document id (lexicographically).
	 *
	 * @param o Other reference to compare to; may be null.
	 * @return A negative, zero, or positive integer as this reference is less than, equal to, or greater than the specified reference.
	 */
	@Override public int compareTo(DocumentReference o) {
		if (o == null) {
			return 1;
		}
		int modelComparison = StringUtils.compare(getDocumentModelName(), o.getDocumentModelName());
		if (modelComparison == 0) {
			return StringUtils.compare(getDocumentId(), o.getDocumentId());
		} else {
			return modelComparison;
		}
	}

	/**
	 * Method sets documentModelName and documentId fields if provided document reference is valid.
	 * Document reference is considered valid if it follows DocumentModelName/DocumentId pattern where DocumentModelName is at least 2 characters long and DocumentId is not blank.
	 *
	 * @param docRef Reference to the document
	 * @throws InvalidDocumentReferenceException if no separator('/') is present, documentModelName starts with '../' or './', documentModelName length is less than two characters or documentId is blank
	 */
	protected void setIfValid(String docRef) {
		if (StringUtils.countMatches(docRef, SEPARATOR) < 1 || StringUtils.contains(docRef, "../") || StringUtils.contains(docRef, "./")) {
			throw new InvalidDocumentReferenceException(String.format("docRef [%s] is not a valid DocumentReference", docRef));
		}

		String[] s = docRef.split(SEPARATOR, 2);
		setIfValid(s[0], s[1]);
	}

	/**
	 * Method sets documentModelName and documentId fields if provided document reference is valid.
	 * Document reference is considered valid if it follows DocumentModelName/DocumentId pattern where DocumentModelName is at least 2 characters long and DocumentId is not blank.
	 *
	 * @param documentModelName Name of the documentModel
	 * @param documentId Id of the document
	 * @throws InvalidDocumentReferenceException if documentModelName length is less than two characters or documentId is blank
	 */
	protected void setIfValid(String documentModelName, String documentId) {
		if (StringUtils.length(documentModelName) < 2 || StringUtils.isBlank(documentId)) {
			throw new InvalidDocumentReferenceException(String.format("docRef [%s/%s] is not a valid DocumentReference", documentModelName, documentId));
		}

		setDocumentModelName(documentModelName);
		setDocumentId(documentId);
	}
}
