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

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single modification unit for the `PARTIAL_MODIFY_DOCUMENT` operation.
 *
 * A `DocumentPart` identifies a field or group inside a document by its `path`, an optional `value`, and a `repetitions` array.
 *
 * When `path` addresses a group element, `value` must be a `Map` (or equivalent JSON object)
 * whose keys and values correspond to the group's fields and nested groups as defined in the
 * document model.
 *
 * When `path` addresses a field element, `value` must be a scalar of the type as defined in the
 * document model.
 *
 * When `repetitions` contains only concrete (non-zero) indices, the addressed group ot field instance is
 * replaced if it already exists, or inserted at that position if it does not, or removed if `value` is `null`. Missing ancestor
 * repetitions are created automatically.
 *
 * When the last element of `repetitions` is `0` (the wildcard index) - only allowed if path points to a group - , the supplied group is
 * appended as a new repetition of the addressed repeatable group. If the group currently has no
 * repetitions, the appended entry becomes the first one. The target group must be declared as
 * repeatable in the document model; an intermediate `0` in `repetitions` is not permitted.

 */
@JsonInclude
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentPart {

	/**
	 * JSON path identifying the target field or group within the document
	 * (e.g. `/BusinessPartnerRoot/Name`).
	 */
	String path;
	/**
	 * The new value for the addressed field or group, or `null` to remove the element.
	 * For fields, this must be a scalar of a type matching the document model specification.
	 * For groups, this must be a `Map` whose structure matches the document model. Must not be null.
	 * If the value is null, the repetitions array must not contain any `0` wildcard.
	 */
	Object value;
	/**
	 * Repetition indices selecting a specific instance within the document (field or group).
	 * A trailing `0` is allowed only if the path points to a repeatable group, and signals an append operation;
	 * all other indices must be concrete (non-zero) positive integers. Must not be null.
	 */
	int[] repetitions;
}
