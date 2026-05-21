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
package com.mgmtp.a12.dataservices.query.validation;

import java.util.Collection;

import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;

import lombok.NonNull;

/**
 * Interface for query operator validators.
 * Implement this interface if you want to validate your custom query operator.
 */
public interface IQueryOperatorValidator {

	/**
	 * Validate the operator. The method should check that the operator is of the type supported by this validator at its start and return empty collection if the validator is not suitable for the operator passed in.
	 *
	 * @param operator the operator to validate
	 * @param parentDocumentModel the parent document model
	 * @param path the path to the operator
	 * @param context the query context holding data related to the query
	 * @param validationEnabled whether validation is enabled; when false, validators may skip checks.
	 * @return a collection of validation items
	 */
	@NonNull Collection<ValidationItem> validate(ILogicOperator operator, String parentDocumentModel, String[] path, QueryContext context, boolean validationEnabled);
}

