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
package com.mgmtp.a12.dataservices.query.internal.aggregation;

import java.util.Deque;

/**
 * Represents a repeatable aggregate field, used to manage unique temporary names and their
 * corresponding field paths within the context of query aggregation.
 *
 * This record is typically used when handling scenarios where the same field might appear
 * multiple times in an aggregation, requiring a unique alias for each instance to avoid
 * naming conflicts in the generated query.
 *
 * @param tempAggName The temporary, unique name generated for this repeatable aggregate field.
 *                    This name is used as an alias in the generated query.
 * @param fieldPaths A {@link Deque} of strings representing the hierarchical path of the field
 *                   within the data structure. This path helps in identifying the specific
 *                   instance of the repeatable field.
 */
public record RepeatableAggField(String tempAggName, Deque<String> fieldPaths) {

}
