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
package com.mgmtp.a12.dataservices.uniqueconstraint;

import java.util.Map;
import java.util.SequencedCollection;

import com.mgmtp.a12.dataservices.document.DocumentReference;

/**
 * Represents a single uniqueness constraint violation found during a `CHECK_UNIQUENESS`
 * operation.
 *
 * The `CHECK_UNIQUENESS` operation returns a `List<CheckUniquenessResult>` — an
 * empty list means the document satisfies all constraints defined in the model.
 *
 * A non-empty list is a normal validation outcome, not an error.
 *
 * Note: an empty list does not guarantee that the document will still be unique at write time
 * due to concurrent operations.
 *
 * @param modelName         the topmost model name in the inheritance hierarchy that defines this
 *                          constraint. For constraints shared up to a parent model this is the
 *                          parent; for constraints defined only on the submitted model this is
 *                          the submitted model itself. Each constraint in the result may have a
 *                          different `modelName`.
 * @param constraintName    the name of the violated uniqueness constraint.
 * @param conflictingDocRef the document reference of the document that holds the conflicting
 *                          value combination.
 * @param errorMessage      map of locale code to localized error message text, as defined in
 *                          the Document Model; may be empty if no messages are defined.
 * @param fieldFullNames    ordered sequence of field paths that form this constraint
 *                          (e.g. `"/PersonRoot/firstName"`). The order is significant:
 *                          it must match the order used when the tracking entry was stored.
 * @param errorKey          key composed as `"error.document.unique.constraint.violation.{modelName}.{constraintName}"`,
 *                          where `{modelName}` is `modelName()` of this result.
 */
public record CheckUniquenessResult(
	String modelName,
	String constraintName,
	DocumentReference conflictingDocRef,
	Map<String, String> errorMessage,
	SequencedCollection<String> fieldFullNames,
	String errorKey
) {}
