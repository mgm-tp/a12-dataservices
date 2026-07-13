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
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCREF_METADATA_PATH;

/**
 * Base class for transaction and concurrency integration tests.
 *
 * Provides common utilities for:
 *
 * - Document field access via kernel API
 * - Document field updates via kernel API
 * - Query building for verification
 * - Transaction template creation
 *
 * These tests verify database and search index consistency under various concurrent
 * and transactional scenarios.
 */
public abstract class AbstractTransactionIT extends AbstractSpringContextIT {

	/** Standard field path for the Name field in BusinessPartner documents. */
	protected static final String NAME_FIELD_PATH = "/BusinessPartnerRoot/Name";

	/** Standard field path for the Industry field in BusinessPartner documents. */
	protected static final String INDUSTRY_FIELD_PATH = "/BusinessPartnerRoot/Industry";

	@Autowired
	protected PlatformTransactionManager transactionManager;

	/**
	 * Creates a new TransactionTemplate configured with REQUIRES_NEW propagation.
	 *
	 * This ensures each operation runs in its own independent transaction,
	 * which is essential for testing concurrent transaction behavior.
	 *
	 * @return a new TransactionTemplate with REQUIRES_NEW propagation
	 */
	protected TransactionTemplate createNewTransactionTemplate() {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return txTemplate;
	}

	/**
	 * Creates and initializes a BusinessPartner document for testing.
	 *
	 * @return the DocumentReference of the created document
	 * @throws IOException if the document file cannot be read
	 */
	protected DocumentReference createTestDocument() throws IOException {
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
		DataServicesDocument doc = documentFunctions.createDocumentFromFile(
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			PathConstants.DOCUMENTS_PATH + "BusinessPartner-1.json"
		);
		return doc.getMetadata().getDocRef();
	}

	/**
	 * Gets a field value from a document using the kernel API.
	 *
	 * @param doc the DataServicesDocument to read from
	 * @param fieldPath the path to the field (e.g., "/BusinessPartnerRoot/Name")
	 * @return the field value as a String, or null if not found
	 */
	protected String getFieldValue(DataServicesDocument doc, String fieldPath) {
		return getFieldValueFromKernelDocument(doc.getKernelDocument(), fieldPath);
	}

	/**
	 * Gets a field value from a kernel document using the kernel API.
	 *
	 * @param document the DocumentV2 (kernel document) to read from
	 * @param fieldPath the path to the field (e.g., "/BusinessPartnerRoot/Name")
	 * @return the field value as a String, or null if not found
	 */
	protected String getFieldValueFromKernelDocument(DocumentV2 document, String fieldPath) {
		Optional<Object> value = DocumentUtils.findSingleValue(document, fieldPath);
		return value.map(Object::toString).orElse(null);
	}

	/**
	 * Updates a field value in a kernel document using the kernel API.
	 *
	 * @param document the DocumentV2 to update
	 * @param fieldPath the path to the field (e.g., "/BusinessPartnerRoot/Name")
	 * @param newValue the new value to set
	 * @return a new DocumentV2 instance with the updated field value
	 */
	protected DocumentV2 updateDocumentField(DocumentV2 document, String fieldPath, String newValue) {
		UpdateAction updateAction = DocumentUtils.createFieldUpdateAction(fieldPath, new int[] { 1, 1 }, newValue);
		return document.withBatchUpdates(List.of(updateAction));
	}

	/**
	 * Updates a field value in a JSON string.
	 *
	 * This method is provided for backward compatibility and for cases where
	 * direct JSON manipulation is more convenient than the kernel API.
	 *
	 * @param json the JSON string to update
	 * @param fieldPath the path to the field (e.g., "/BusinessPartnerRoot/Name")
	 * @param newValue the new value to set
	 * @return the updated JSON string
	 * @throws IllegalArgumentException if the field is not found or has an unsupported value type
	 */
	protected String updateJsonField(String json, String fieldPath, String newValue) {
		String[] parts = fieldPath.split("/");
		String fieldName = parts[parts.length - 1];

		int fieldIndex = json.indexOf("\"" + fieldName + "\"");
		if (fieldIndex == -1) {
			throw new IllegalArgumentException("Field not found: " + fieldPath);
		}

		int colonIndex = json.indexOf(":", fieldIndex);
		int valueStart = colonIndex + 1;

		while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
			valueStart++;
		}

		int valueEnd;
		if (json.charAt(valueStart) == '"') {
			valueStart++;
			valueEnd = json.indexOf("\"", valueStart);
			return json.substring(0, valueStart) + newValue + json.substring(valueEnd);
		} else if (json.substring(valueStart).startsWith("null")) {
			valueEnd = valueStart + 4;
			return json.substring(0, valueStart) + "\"" + newValue + "\"" + json.substring(valueEnd);
		} else {
			throw new IllegalArgumentException("Unsupported value type at field: " + fieldPath);
		}
	}

	/**
	 * Converts a document's JSON, updates a field, and returns a new kernel document.
	 *
	 * This is a convenience method that combines JSON manipulation with kernel document creation.
	 *
	 * @param doc the source document
	 * @param fieldPath the path to the field to update
	 * @param newValue the new value to set
	 * @return a new DocumentV2 with the updated field
	 */
	protected DocumentV2 updateDocumentFieldViaJson(DataServicesDocument doc, String fieldPath, String newValue) {
		String json = documentFunctions.convertDocumentToJson(doc);
		String updatedJson = updateJsonField(json, fieldPath, newValue);
		return documentSupport.convertJSONToDocument(
			doc.getMetadata().getDocRef().getDocumentModelName(),
			new StringReader(updatedJson)
		);
	}

	/**
	 * Builds a verification query to check if a document exists with a specific field value.
	 *
	 * @param documentReference the document reference to query
	 * @param field the field path to match
	 * @param value the expected field value
	 * @return a QueryRoot configured for the verification query
	 */
	protected static QueryRoot buildVerificationQuery(DocumentReference documentReference, String field, String value) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(documentReference.getDocumentModelName())
			.constraint(AndOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field(DOCREF_METADATA_PATH)
					.value(documentReference.toString())
					.build())
				.operand(ExactMatchOperator.builder()
					.field(field)
					.value(value)
					.build())
				.build()
			)
			.fields(List.of(DOCREF_METADATA_PATH))
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(1)
				.build())
			.build();
	}

	/**
	 * Builds a verification query to check if a document exists with two specific field values.
	 *
	 * @param documentReference the document reference to query
	 * @param field1 the first field path to match
	 * @param value1 the expected first field value
	 * @param field2 the second field path to match
	 * @param value2 the expected second field value
	 * @return a QueryRoot configured for the verification query
	 */
	protected static QueryRoot buildVerificationQuery(
			DocumentReference documentReference,
			String field1, String value1,
			String field2, String value2) {
		return QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(documentReference.getDocumentModelName())
			.constraint(AndOperator.builder()
				.operand(ExactMatchOperator.builder()
					.field(DOCREF_METADATA_PATH)
					.value(documentReference.toString())
					.build())
				.operand(ExactMatchOperator.builder()
					.field(field1)
					.value(value1)
					.build())
				.operand(ExactMatchOperator.builder()
					.field(field2)
					.value(value2)
					.build()).build()
			)
			.fields(List.of(DOCREF_METADATA_PATH))
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(1)
				.build())
			.build();
	}
}
