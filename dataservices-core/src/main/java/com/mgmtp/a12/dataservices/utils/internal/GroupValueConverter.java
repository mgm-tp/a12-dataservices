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

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IGroup;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts a raw document-part value (typically a `Map` or JSON-compatible object) into a
 * DM-typed `GroupInstanceV2` by reusing the kernel document deserializer.
 */
// TODO A12S-6969: Get rid of this helper class as soon as Kernel provides this functionality as public API
@RequiredArgsConstructor
public class GroupValueConverter {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();
	private static final DocumentDeserializationConfig JSON_DESERIALIZATION_CONFIG =
		DocumentDeserializationConfig.builder().build();

	private final IDocumentV2Serializer documentV2Serializer;
	private final IModelLoader<IDocumentModel> documentModelLoader;

	/**
	 * Converts the given `value` to a `GroupInstanceV2` typed according to the document model
	 * identified by `documentModelId`, at the element `path`.
	 *
	 * @param documentModel the document model
	 * @param path the path of the target group element within the model
	 * @param value the raw value to convert (expected to be a `Map` or equivalent JSON-compatible object)
	 * @return a DM-typed `GroupInstanceV2`
	 * @throws InvalidInputException when the value cannot be converted to a group instance
	 */
	public GroupInstanceV2 toGroupInstance(IDocumentModel documentModel, String path, Object value) {
		JsonNode valueNode;
		try {
			valueNode = MAPPER.valueToTree(value);
		} catch (Exception e) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Value cannot be converted to a group instance for path: " + path,
				e
			);
		}

		if (!valueNode.isObject()) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Value is not a group-compatible object for path: " + path
			);
		}

		try {
			List<String> segments = KernelUtils.splitFieldPath(path);
			List<IGroup> groupsAlongPath = findGroupsAlongPath(documentModel, segments);

			JsonNode wrapperNode = buildWrapperJson(segments, groupsAlongPath, valueNode);

			ListIProblemReporter pr = new ListIProblemReporter();
			DocumentV2 document = documentV2Serializer.deserializeV2(wrapperNode, documentModel.getHeader().getId(), JSON_DESERIALIZATION_CONFIG, pr);

			List<Integer> singleRepetitions = segments.stream().map(s -> 1).toList();
			DocumentPointer pointer = KernelUtils.of(segments, singleRepetitions);
			GroupInstanceV2 group = document.group(pointer);

			if (group == null) {
				throw new InvalidInputException(
					ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
					"Path does not resolve to a group in the document model: " + path
				);
			}
			return group;
		} catch (InvalidInputException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Value cannot be converted to a group instance for path: " + path,
				e
			);
		}
	}

	private List<IGroup> findGroupsAlongPath(IDocumentModel model, List<String> segments) {
		List<IGroup> groups = new ArrayList<>();
		IGroup current = model.getContent().getDocumentModelRoot();

		for (String segment : segments) {
			IGroup found = null;
			for (IElement element : current.getElements()) {
				if (element instanceof IGroup g && segment.equals(element.getName())) {
					found = g;
					break;
				}
			}
			if (found == null) {
				throw new InvalidInputException(
					ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
					"Path segment not found as a group in the document model: " + segment
				);
			}
			groups.add(found);
			current = found;
		}
		return groups;
	}

	private JsonNode buildWrapperJson(List<String> segments, List<IGroup> groups, JsonNode valueNode) {
		JsonNode current = valueNode;

		for (int i = segments.size() - 1; i >= 0; i--) {
			String segmentName = segments.get(i);
			IGroup group = groups.get(i);

			JsonNode nodeToEmbed;
			if (group.getRepeatability() > 1) {
				ArrayNode array = MAPPER.createArrayNode();
				array.add(current);
				nodeToEmbed = array;
			} else {
				nodeToEmbed = current;
			}

			ObjectNode wrapper = MAPPER.createObjectNode();
			wrapper.set(segmentName, nodeToEmbed);
			current = wrapper;
		}

		return current;
	}
}
