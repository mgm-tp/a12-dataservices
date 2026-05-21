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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.reference.GenericReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Attachments can be assigned to different entities, e.g. documents. This class represents a reference to such an entity.
 * Currently, only references to documents are supported.
 *
 */
@Slf4j
@Data @AllArgsConstructor(access = AccessLevel.PRIVATE) @NoArgsConstructor @Builder
public class AttachmentReference<T extends GenericReference> {
	private AttachmentReferenceType type;

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
	private T reference;

	/**
	 * Parses a textual reference into a typed {@link AttachmentReference}.
	 *
	 * @param <T> The concrete {@link GenericReference} type represented by the reference.
	 * @param type The reference kind determining the target class; must not be null.
	 * @param ref The textual reference value (e.g., document reference string); must not be null.
	 * @return A new {@link AttachmentReference} of the requested type containing the parsed reference.
	 * @throws InvalidInputException If the target type cannot be instantiated from the given text.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GenericReference> AttachmentReference<T> parse(AttachmentReferenceType type, String ref) {
		try {
			return AttachmentReference.<T>builder()
				.type(type)
				.reference(((Constructor<T>) type.getTypeClass().getConstructor(String.class)).newInstance(ref))
				.build();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
		         InvocationTargetException e) {
			throw new InvalidInputException(
				String.format("Could not initialise class of type %s => could not construct AttachmentReference.", type.getTypeClass().getCanonicalName()), e);
		}
	}

	/**
	 * Creates a {@link AttachmentReference} for a document from its textual reference.
	 *
	 * @param docRef The document reference string; must not be null.
	 * @return A {@link AttachmentReference} targeting the given {@link DocumentReference}.
	 */
	public static AttachmentReference<DocumentReference> fromDocRef(String docRef) {
		return AttachmentReference.<DocumentReference>builder()
			.reference(new DocumentReference(docRef))
			.type(AttachmentReferenceType.DOCUMENT)
			.build();
	}

	/**
	 * Creates a {@link AttachmentReference} for a document from an existing {@link DocumentReference}.
	 *
	 * @param docRef The document reference instance; must not be null.
	 * @return A {@link AttachmentReference} targeting the given {@link DocumentReference}.
	 */
	public static AttachmentReference<DocumentReference> fromDocRef(DocumentReference docRef) {
		return AttachmentReference.<DocumentReference>builder()
			.reference(docRef)
			.type(AttachmentReferenceType.DOCUMENT)
			.build();
	}
}

