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
package com.mgmtp.a12.dataservices.query.indexing.internal;

import java.util.Arrays;
import java.util.LinkedList;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentSearchIndexRecursionHelper {
	public static ObjectNode prepareParentNode(IDocumentModelSearchService documentModelSearchService, ObjectNode rootNode, DocumentPointer pointerRelativeToBase) {
		LinkedList<String> pathElements = new LinkedList<>(Arrays.asList(pointerRelativeToBase.fullName().substring(1).split(FIELD_SEPARATOR)));
		LinkedList<Integer> repetitions = new LinkedList<>(pointerRelativeToBase.repetitionIndexes());
		return prepareParentNodeRecursion(rootNode, "", pathElements, repetitions, documentModelSearchService);
	}

	private static ObjectNode prepareParentNodeRecursion(ObjectNode parentNode, String path, LinkedList<String> pathElements,
		LinkedList<Integer> repetitions,
		IDocumentModelSearchService documentModelSearchService) {

		if (pathElements.isEmpty()) {
			return parentNode;
		}

		String currentName = pathElements.poll();
		String currentPath = "%s/%s".formatted(path, currentName);
		int currentRepetition = repetitions.poll();

		if (DocumentSearchIndexHelper.isRepeatableGroup(documentModelSearchService, currentPath)) {
			ArrayNode currentNode;
			if (parentNode.has(currentName)) {
				currentNode = (ArrayNode) parentNode.get(currentName);
			} else {
				currentNode = DocumentSearchIndexBehaviour.OBJECT_MAPPER.createArrayNode();
				parentNode.set(currentName, currentNode);
			}
			while (currentNode.size() <= currentRepetition) {
				currentNode.add(DocumentSearchIndexBehaviour.OBJECT_MAPPER.createObjectNode());
			}
			return prepareParentNodeRecursion((ObjectNode) currentNode.get(currentRepetition - 1), currentPath, pathElements, repetitions,
				documentModelSearchService);
		} else {
			ObjectNode currentNode;
			if (parentNode.has(currentName)) {
				currentNode = (ObjectNode) parentNode.get(currentName);
			} else {
				currentNode = DocumentSearchIndexBehaviour.OBJECT_MAPPER.createObjectNode();
				parentNode.set(currentName, currentNode);
			}
			return prepareParentNodeRecursion(currentNode, currentPath, pathElements, repetitions, documentModelSearchService);
		}
	}
}
