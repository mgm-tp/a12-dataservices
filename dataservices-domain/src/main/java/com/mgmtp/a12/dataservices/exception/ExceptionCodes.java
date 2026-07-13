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
package com.mgmtp.a12.dataservices.exception;

import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Exception Codes interface. Codes for all exceptions DS throws. The codes are mostly used in java client to derive
 * the error without reliance on localization key or HTTP status code.
 */
@OnlyForUsage public interface ExceptionCodes {

	/** Exception code for attempts to use a feature that has been disabled by configuration. */
	int FUNCTIONALITY_DISABLED_EXCEPTION_CODE = -32000;
	/** Exception code for data-integrity violations detected while persisting or reading documents. */
	int INTEGRITY_EXCEPTION_CODE = -32001;
	/** Exception code for failures while serializing or deserializing a model between its wire format and its domain representation. */
	int MODEL_SERIALIZATION_EXCEPTION_CODE = -32003;
	/** Exception code for a conflict between two RPC requests sharing the same request identifier. */
	int REQUEST_ID_CONFLICT_EXCEPTION_CODE = -32005;
	/** Exception code for security-related errors that are not specifically access-denied. */
	int SECURITY_EXCEPTION_CODE = -32006;
	/** Exception code raised when persisted data is detected to be corrupted or inconsistent. */
	int CORRUPTED_DATA_EXCEPTION_CODE = -32008;
	/** Exception code for failures while deserializing a document model or document from its persisted or wire form. */
	int DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE = -32009;
	/** Exception code for failures while serializing a document model to its persisted form. */
	int DOCUMENT_MODEL_SERIALIZATION_EXCEPTION_CODE = -32010;
	/** Exception code for generic relationship validation failures. */
	int RELATIONSHIP_VALIDATION_EXCEPTION_CODE = -32012;
	/** Exception code for relationship operations rejected due to a version mismatch on the link. */
	int RELATIONSHIP_VERSION_VALIDATION_EXCEPTION_CODE = -32013;
	/** Exception code for references to a relationship role name that does not exist on the model. */
	int RELATIONSHIP_ROLE_NAME_NOT_FOUND_EXCEPTION_CODE = -32014;
	/** Exception code for relationship operations where supplied roles do not match the model definition. */
	int RELATIONSHIP_ROLE_MISMATCH_EXCEPTION_CODE = -32015;
	/** Exception code for relationship operations where a role-side document cannot be located. */
	int RELATIONSHIP_ROLE_DOCUMENT_NOT_FOUND_EXCEPTION_CODE = -32016;
	/** Exception code for relationship operations where the relationship model does not match the supplied data. */
	int RELATIONSHIP_MODEL_MISMATCH_EXCEPTION_CODE = -32017;
	/** Exception code for relationship link entities that fail structural validation. */
	int RELATIONSHIP_LINK_ENTITY_INVALID_EXCEPTION_CODE = -32018;
	/** Exception code for failures while serializing a relationship link document. */
	int RELATIONSHIP_LINK_DOCUMENT_SERIALIZATION_EXCEPTION_CODE = -32019;
	/** Exception code raised when a relationship link document is not permitted on the relationship model. */
	int RELATIONSHIP_LINK_DOCUMENT_NOT_ALLOWED_EXCEPTION_CODE = -32020;
	/** Exception code for relationship link operations missing the required link-document model. */
	int RELATIONSHIP_LINK_DOCUMENT_MODEL_MISSING_EXCEPTION_CODE = -32021;
	/** Exception code for relationship link operations where the link-document model does not match expectations. */
	int RELATIONSHIP_LINK_DOCUMENT_MODEL_MISMATCH_EXCEPTION_CODE = -32022;
	/** Exception code for relationship link operations where the link document itself is missing. */
	int RELATIONSHIP_LINK_DOCUMENT_MISSING_EXCEPTION_CODE = -32023;
	/** Exception code for relationship operations referencing a link that is invalid. */
	int RELATIONSHIP_INVALID_LINK_EXCEPTION_CODE = -32024;
	/** Exception code for relationship operations where one or more document references are invalid. */
	int RELATIONSHIP_INVALID_DOCUMENT_REFERENCES_EXCEPTION_CODE = -32025;
	/** Exception code for relationship operations where the referenced document model is invalid. */
	int RELATIONSHIP_INVALID_DOCUMENT_MODEL_EXCEPTION_CODE = -32026;

	/** Exception code for failures while computing derived document fields. */
	int DOCUMENT_COMPUTATION_EXCEPTION_CODE = -32028;
	/** Exception code for failures during a document import operation. */
	int DOCUMENT_IMPORT_EXCEPTION_CODE = -32029;
	/** Exception code raised when a document reference cannot be resolved to a document. */
	int INVALID_DOCUMENT_REFERENCE_EXCEPTION_CODE = -32030;
	/** Exception code for generic serialization failures not covered by more specific codes. */
	int SERIALIZATION_EXCEPTION_CODE = -32031;
	/** Exception code for failures while generating an attachment thumbnail. */
	int THUMBNAIL_CONVERSION_EXCEPTION_CODE = -32032;

	// TODO A12S-6616 : Refactor dependencies to avoid this duplication.
	/** Exception code for the generic JSON-RPC error response wrapper. */
	int RPC_ERROR_EXCEPTION_CODE = ErrorDetail.RPC_ERROR_EXCEPTION_CODE;
	/** Exception code raised when an RPC batch operation cannot execute because a previous operation in the same batch has already failed. */
	int OPERATION_FAILED_EXCEPTION_CODE = -32036;
	/** Exception code for relationship link operations missing a required role reference. */
	int RELATIONSHIP_LINK_ROLE_MISSING_EXCEPTION_CODE = -32038;
	/** Exception code raised when an add-link operation references a predecessor link that cannot be found. */
	int RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_EXCEPTION_CODE = -32039;

	/** Exception code raised when a relink operation targets a document that is not compatible with the relationship. */
	int RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_EXCEPTION_CODE = -32041;

	/** Exception code for errors reported by the content store client. */
	int CONTENT_STORE_CLIENT_EXCEPTION_CODE = -32042;

	/** Exception code raised when a configured hard limit has been exceeded by a request. */
	int HARD_LIMIT_EXCEEDED_EXCEPTION_CODE = -32043;
	/** Exception code for failures while converting a document between representations. */
	int DOCUMENT_CONVERSION_EXCEPTION_CODE = -32044;
	/** Exception code for failures while joining or composing document models. */
	int DOCUMENT_MODEL_JOINING_EXCEPTION_CODE = -32045;
	/** Exception code for failures while converting a field instance to its Java representation. */
	int FIELD_INSTANCE_TO_JAVA_TYPE_EXCEPTION_CODE = -32046;
	/** Exception code for failures while generating validation codes for a document model. */
	int VALIDATION_CODES_GENERATION_EXCEPTION_CODE = -32047;
	/** Exception code raised when the number of RPC operations in a single request exceeds the configured limit. */
	int LIMIT_OF_RPC_OPERATIONS_EXCEEDED_ERROR_CODE = -32048;
	/** Exception code for errors raised by the query index during query execution. */
	int QUERY_INDEX_ERROR_CODE = -32049;

	/** Exception code for queries rejected because their input is invalid. */
	int QUERY_INVALID_INPUT_ERROR_CODE = -32051;

	/** Exception code raised when a referenced query cannot be found. */
	int QUERY_NOT_FOUND_ERROR_CODE = -32053;

	/** Exception code for failures while parsing the JSON payload of a query. */
	int QUERY_JSON_PARSING_ERROR_CODE = 32056;
	/** Exception code for generic query execution failures not covered by more specific codes. */
	int QUERY_GENERAL_ERROR_CODE = -32057;

	// TODO A12S-5063 remove it in breaking release.
	/** Exception code for failures while serializing a document model to its persisted form. */
	int DOCUMENT_MODEL_SERIALIZATION_EXCEPTION = -32010;

	/**
	 * Copy of {@link com.mgmtp.a12.dataservices.common.exception.InvalidSizeException#INVALID_SIZE_EXCEPTION_CODE}
	 * which cannot use this class.
	 *
	 * TODO: Better exception code management with A12S-6341.
	 */
	int INVALID_SIZE_EXCEPTION_CODE = -32058;

	/**
	 * Exception code for all access denied error cases.
	 */
	int ACCESS_DENIED_EXCEPTION_CODE = -32059;

	/**
	 * Exception code for unique constraint violation errors.
	 * Thrown when a document operation violates a Document Uniqueness Criterion.
	 */
	int UNIQUE_CONSTRAINT_VIOLATION_EXCEPTION_CODE = -32060;

	/**
	 * Exception code for model unique constraint definition errors.
	 * Thrown when a Document Model upload contains an invalid Uniqueness Criterion definition.
	 */
	int MODEL_UNIQUE_CONSTRAINT_VALIDATION_EXCEPTION_CODE = -32061;
}
