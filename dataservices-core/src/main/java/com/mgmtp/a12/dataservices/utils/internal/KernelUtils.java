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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.Arrays;
import java.util.List;

import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KernelUtils {

	public static List<String> splitFieldPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return List.of(path.split("/"));
	}

	public static DocumentPointer fromDocumentPart(DocumentPart part) {
		return fromPathAndRepetitions(splitFieldPath(part.getPath()), part.getRepetitions());
	}

	public static DocumentPointer fromPathAndRepetitions(String path, int[] repetitions) {
		return fromPathAndRepetitions(splitFieldPath(path), repetitions);
	}

	public static DocumentPointer fromPathAndRepetitions(List<String> paths, int[] repetitions) {
		return of(paths, Arrays.stream(repetitions).boxed().toList());
	}

	public static DocumentPointer of(List<String> paths, List<Integer> repetitions) {
		return DocumentPointer.of(paths, repetitions);
	}

	/**
	 * Returns `true` when `repetitions` is non-null, non-empty, and its last element is `0` (the wildcard index).
	 */
	public static boolean isLastRepetitionWildcard(int[] repetitions) {
		return repetitions != null && repetitions.length > 0 && repetitions[repetitions.length - 1] == 0;
	}

	/**
	 * Returns `true` when any non-last element of `repetitions` is `0` (an intermediate wildcard).
	 */
	public static boolean hasIntermediateWildcard(int[] repetitions) {
		if (repetitions == null || repetitions.length <= 1) {
			return false;
		}
		for (int i = 0; i < repetitions.length - 1; i++) {
			if (repetitions[i] == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns `true` when any element of `repetitions` is `0` (a wildcard index), whether intermediate or last.
	 */
	public static boolean hasAnyWildcard(int[] repetitions) {
		if (repetitions == null) {
			return false;
		}
		for (int repetition : repetitions) {
			if (repetition == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Builds a `DocumentPointer` from the given path and repetitions, keeping a trailing wildcard `0`
	 * as the last repetition index without normalizing or rejecting it.
	 */
	public static DocumentPointer pointerPreservingWildcard(String path, int[] repetitions) {
		return fromPathAndRepetitions(splitFieldPath(path), repetitions);
	}
}
