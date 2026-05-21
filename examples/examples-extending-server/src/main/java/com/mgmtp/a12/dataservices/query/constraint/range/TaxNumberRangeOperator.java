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
package com.mgmtp.a12.dataservices.query.constraint.range;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Tax number range operator that filters results by matching the numeric component
 * of tax numbers within the specified bounds.
 *
 * This operator works with TaxNumberCustomFieldType fields (format: US-YYYYMMDDNNNNNNNN)
 * and queries based on the numeric component (last 8 digits).
 *
 * Example usage:
 * ```
 * {
 * "field": "/Person/TaxNumber",
 * "operator": "tax_number_range",
 * "from": "US10000000",
 * "to": "US99999999"
 * }
 * ```
 *
 * Both bounds are optional; providing one creates an open-ended range.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonClassDescription("Tax number range operator for filtering by numeric component of tax numbers (format: US-YYYYMMDDNNNNNNNN)")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@QueryOperator("tax_number_range")
public class TaxNumberRangeOperator extends RangeOperator<String> {

}
