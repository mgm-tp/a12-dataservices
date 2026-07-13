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
package com.mgmtp.a12.dataservices.document.operation.internal;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResponse;
import com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResult;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.RPC_ERROR_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.CHECK_UNIQUENESS_ERROR_KEY;

/**
 * Read-only operation that checks whether a document would violate any uniqueness constraints
 * defined in its Document Model.
 *
 * Constraint violations are reported in the response as a list and do not cause an exception.
 * Exceptions are only thrown for infrastructure failures such as a missing model or an
 * unexpected database error.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.CHECK_UNIQUENESS_OPERATION, group = CoreOperationConstants.DOCUMENT_OPERATIONS_GROUP, isMutation = false)
public class CheckUniquenessOperation extends AbstractDocumentOperation {

	private static final String COULD_NOT_CHECK_UNIQUENESS_MESSAGE = "Could not check uniqueness";
	private static final String CHECK_UNIQUENESS_OPERATION_FAILED = "CHECK_UNIQUENESS operation failed with the following exception";

	private final UniqueConstraintValidator uniqueConstraintValidator;
	private final DocumentSupport documentSupport;

	public CheckUniquenessOperation(DocumentService documentService, Anonymizer anonymizer,
		UniqueConstraintValidator uniqueConstraintHandler, DocumentSupport documentSupport) {
		super(documentService, anonymizer);
		this.uniqueConstraintValidator = uniqueConstraintHandler;
		this.documentSupport = documentSupport;
	}

	/**
	 * Checks all uniqueness constraints defined for the given Document Model against the
	 * provided document.
	 *
	 * Returns a {@link CheckUniquenessResponse} with an empty `violations` list when the
	 * document satisfies all constraints, or a non-empty list describing each violated constraint.
	 *
	 * Constraint violations are always reported in the response — this method never throws
	 * because of them. Exceptions are only thrown for infrastructure failures such as a missing
	 * model or an unexpected database error.
	 *
	 * @param documentModelName the Document Model name.
	 * @param document          the full document content in JSON format.
	 * @param docRef            optional document reference of the document being updated;
	 *                          when provided, that document is excluded from conflict detection.
	 * @return the uniqueness check result; `violations` is empty when all constraints are satisfied.
	 */
	@Transactional(value = "dsTransactionManager", readOnly = true)
	public CheckUniquenessResponse rpc(
		@NonNull @JsonRpcParam("documentModelName") String documentModelName,
		@NonNull @JsonRpcParam("document") JsonNode document,
		@JsonRpcParam("docRef") DocumentReference docRef) {
		log.debug("{} called with parameters [documentModelName={}, docRefPresent={}]",
			CoreOperationConstants.CHECK_UNIQUENESS_OPERATION,
			anonymizer.apply(documentModelName),
			docRef != null
		);
		try {
			DocumentV2 documentV2 = documentSupport.convertJSONToDocument(documentModelName, document, docRef);
			List<CheckUniquenessResult> violations =
				uniqueConstraintValidator.checkAllConstraints(documentModelName, documentV2, docRef);
			return violations.isEmpty()
				? CheckUniquenessResponse.noViolations()
				: CheckUniquenessResponse.withViolations(violations);
		} catch (BaseException e) {
			log.info(CHECK_UNIQUENESS_OPERATION_FAILED, e);
			throw e;
		} catch (Exception e) {
			log.info(CHECK_UNIQUENESS_OPERATION_FAILED, e);
			throw RpcExceptionSupport.createException(RPC_ERROR_EXCEPTION_CODE, CHECK_UNIQUENESS_ERROR_KEY,
				COULD_NOT_CHECK_UNIQUENESS_MESSAGE, e.getMessage(),
				RemoteOperation.RemoteOperationHelper.getOperationId(this.getClass()), e);
		}
	}
}
