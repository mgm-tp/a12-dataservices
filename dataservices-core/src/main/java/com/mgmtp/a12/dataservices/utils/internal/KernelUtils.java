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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV1V2Converter;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KernelUtils {

	public static String getRootNameFromPath(String path) {
		return path.split("/")[1];
	}

	public static int[] concatRepetitions(int[]... repetitions) {
		return Arrays.stream(repetitions)
			.flatMapToInt(Arrays::stream)
			.filter(i -> i > 0)
			.toArray();
	}

	public static String concatPaths(String... paths) {
		return Arrays.stream(paths)
			.filter(StringUtils::isNotBlank)
			.flatMap(p -> Arrays.stream(p.split(IEntityInstance.PATH_SEPARATOR)))
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.joining(IEntityInstance.PATH_SEPARATOR, IEntityInstance.PATH_SEPARATOR, ""));
	}

	// This is used for get converter from document v1 to v2, will be remove in A12S-6428
	public static IDocumentV1V2Converter getIDocumentV1V2Converter() {
		return DocumentV1V2ConverterImpl.getInstance();
	}

	public static List<String> splitFieldPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return List.of(path.split("/"));
	}

	public static FieldInstanceV2 fromV1Value(IFieldInstance e) {
		return FieldInstanceV2.ofV1Value(e.getValue().orElse(null));
	}

	public static DocumentPointer fromDocumentPart(DocumentPart part) {
		return fromPathAndRepetitions(splitFieldPath(part.getPath()), part.getRepetitions());
	}

	public static DocumentPointer fromIEntityInstance(IEntityInstance iEntityInstance) {
		return DocumentPointer.of(KernelUtils.splitFieldPath(iEntityInstance.getPath()), Arrays.stream(iEntityInstance.getRepetitions()).boxed().toList());
	}

	public static DocumentPointer fromPathAndRepetitions(String pathWithRepetitions) {
		return DocumentPointer.of(pathWithRepetitions);
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
}
