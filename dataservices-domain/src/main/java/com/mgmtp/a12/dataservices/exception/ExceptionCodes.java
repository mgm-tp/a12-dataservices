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

/**
 * Exception Codes interface. Codes for all exceptions DS throws. The codes are mostly used in java client to derive
 * the error without reliance on localization key or HTTP status code.
 */
public interface ExceptionCodes {

	int FUNCTIONALITY_DISABLED_EXCEPTION_CODE = -32000;
	int INTEGRITY_EXCEPTION_CODE = -32001;
	int MODEL_SERIALIZATION_EXCEPTION_CODE = -32003;
	int REQUEST_ID_CONFLICT_EXCEPTION_CODE = -32005;
	int SECURITY_EXCEPTION_CODE = -32006;
	int CORRUPTED_DATA_EXCEPTION_CODE = -32008;
	int DOCUMENT_MODEL_DE_SERIALIZATION_EXCEPTION_CODE = -32009;
	int DOCUMENT_MODEL_SERIALIZATION_EXCEPTION_CODE = -32010;
	int INVALID_FACET_EXCEPTION_CODE = -32011;
	int RELATIONSHIP_VALIDATION_EXCEPTION_CODE = -32012;
	int RELATIONSHIP_VERSION_VALIDATION_EXCEPTION_CODE = -32013;
	int RELATIONSHIP_ROLE_NAME_NOT_FOUND_EXCEPTION_CODE = -32014;
	int RELATIONSHIP_ROLE_MISMATCH_EXCEPTION_CODE = -32015;
	int RELATIONSHIP_ROLE_DOCUMENT_NOT_FOUND_EXCEPTION_CODE = -32016;
	int RELATIONSHIP_MODEL_MISMATCH_EXCEPTION_CODE = -32017;
	int RELATIONSHIP_LINK_ENTITY_INVALID_EXCEPTION_CODE = -32018;
	int RELATIONSHIP_LINK_DOCUMENT_SERIALIZATION_EXCEPTION_CODE = -32019;
	int RELATIONSHIP_LINK_DOCUMENT_NOT_ALLOWED_EXCEPTION_CODE = -32020;
	int RELATIONSHIP_LINK_DOCUMENT_MODEL_MISSING_EXCEPTION_CODE = -32021;
	int RELATIONSHIP_LINK_DOCUMENT_MODEL_MISMATCH_EXCEPTION_CODE = -32022;
	int RELATIONSHIP_LINK_DOCUMENT_MISSING_EXCEPTION_CODE = -32023;
	int RELATIONSHIP_INVALID_LINK_EXCEPTION_CODE = -32024;
	int RELATIONSHIP_INVALID_DOCUMENT_REFERENCES_EXCEPTION_CODE = -32025;
	int RELATIONSHIP_INVALID_DOCUMENT_MODEL_EXCEPTION_CODE = -32026;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int LIST_TERMINATING_LINKS_MAX_RESULT_LIMIT_EXCEEDED_EXCEPTION_CODE = -32027;
	int DOCUMENT_COMPUTATION_EXCEPTION_CODE = -32028;
	int DOCUMENT_IMPORT_EXCEPTION_CODE = -32029;
	int INVALID_DOCUMENT_REFERENCE_EXCEPTION_CODE = -32030;
	int SERIALIZATION_EXCEPTION_CODE = -32031;
	int THUMBNAIL_CONVERSION_EXCEPTION_CODE = -32032;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int SEARCH_SERVICE_EXCEPTION_CODE = -32033;
	int MODEL_BULK_IMPORT_EXCEPTION_CODE = -32034;
	int RPC_ERROR_EXCEPTION_CODE = -32035;
	int OPERATION_FAILED_EXCEPTION_CODE = -32036;
	int RELATIONSHIP_LINK_ROLE_MISSING_EXCEPTION_CODE = -32038;
	int RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_EXCEPTION_CODE = -32039;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int RPC_ROLLBACK_FAILED_EXCEPTION_CODE = -32040;
	int RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_EXCEPTION_CODE = -32041;

	int CONTENT_STORE_CLIENT_EXCEPTION_CODE = -32042;

	int HARD_LIMIT_EXCEEDED_EXCEPTION_CODE = -32043;
	int DOCUMENT_CONVERSION_EXCEPTION_CODE = -32044;
	int DOCUMENT_MODEL_JOINING_EXCEPTION_CODE = -32045;
	int FIELD_INSTANCE_TO_JAVA_TYPE_EXCEPTION_CODE = -32046;
	int VALIDATION_CODES_GENERATION_EXCEPTION_CODE = -32047;
	int LIMIT_OF_RPC_OPERATIONS_EXCEEDED_ERROR_CODE = -32048;
	int QUERY_INDEX_ERROR_CODE = -32049;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int QUERY_PROJECTION_NOT_FOUND_ERROR_CODE = -32050;
	int QUERY_INVALID_INPUT_ERROR_CODE = -32051;


	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int QUERY_ACCESS_DENIED_ERROR_CODE = -32052;
	int QUERY_NOT_FOUND_ERROR_CODE = -32053;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int QUERY_VALIDATION_ERROR_CODE = -32054;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int QUERY_FUNCTIONALITY_DISABLED_ERROR_CODE = -32055;
	int QUERY_JSON_PARSING_ERROR_CODE = 32056;
	int QUERY_GENERAL_ERROR_CODE = -32057;
	// TODO A12S-5063 remove 5 constants below in breaking release.

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int DOCUMENT_CONVERSION_ERROR_CODE = -32044;
	int DOCUMENT_MODEL_SERIALIZATION_EXCEPTION = -32010;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int RELATIONSHIP_LINK_ROLE_MISSING_ERROR_CODE = -32038;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_ERROR_CODE = -32039;

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	int RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_ERROR_CODE = -32041;

	/**
	 * Copy of {@link com.mgmtp.a12.dataservices.common.exception.InvalidSizeException#INVALID_SIZE_EXCEPTION_CODE}
	 * which cannot use this class.
	 *
	 * TODO: Better exception code management with A12S-6341.
	 */
	int INVALID_SIZE_EXCEPTION_CODE = -32058;
}
