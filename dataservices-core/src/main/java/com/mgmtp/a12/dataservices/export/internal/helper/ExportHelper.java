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
package com.mgmtp.a12.dataservices.export.internal.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;

@Slf4j @RequiredArgsConstructor
public class ExportHelper {

	private final IDocumentModelService documentModelService;

	public List<Header> collectHeaders(IDocumentModel documentModel) {
		HeaderCollectingDocumentModelVisitor pathCollector = new HeaderCollectingDocumentModelVisitor(documentModelService);
		new DocumentModelWalker().acceptDocumentModel(documentModel, pathCollector);
		return pathCollector.getHeaders();
	}

	public List<List<Object>> getRows(List<JsonNode> jsonNodes, IDocumentModel documentModel, List<Header> fieldNames) {
		TimeZone timeZone = TimeZone.getTimeZone(documentModel.getContent().getDocumentModelConfig().getTimeZone());
		return jsonNodes.stream()
			.map(d -> flattenJsonRecursive("", d, new HashMap<>()))
			.map(dataMap -> fieldNames.stream()
				.map(field -> getFieldValue(dataMap, field, timeZone))
				.toList()
			).toList();
	}

	public boolean isNumeric(IFieldType fieldType) {
		return DocumentUtils.SupportedDataTypes.NUMBER.matches(fieldType);
	}

	public Object getFieldValue(Map<String, String> dataMap, Header header, TimeZone timeZone) {
		try {
			return format(header.getFieldType(), dataMap.get(header.getName()), timeZone);
		} catch (Exception e) {
			throw new InvalidInputException(ExceptionKeys.EXPORT_LIST_CDD_ERROR_KEY, "Document export failed", e);
		}
	}

	private Object format(IFieldType fieldType, Object value, TimeZone timeZone) {
		if (value == null) {
			return null;
		} else if (isNumeric(fieldType)) {
			return formatNumber(value);
		} else {
			return DocumentUtils.format(fieldType, value, timeZone);
		}
	}

	private static Object formatNumber(Object value) {
		return Double.valueOf(value.toString());
	}

	@Data @EqualsAndHashCode(callSuper = true)
	private static class HeaderCollectingDocumentModelVisitor extends DocumentModelVisitor {
		private final IDocumentModelService modelService;
		private final List<Header> headers = new LinkedList<>();

		public HeaderCollectingDocumentModelVisitor(IDocumentModelService modelService) {
			this.modelService = modelService;
		}

		@Override public DocumentModelWalker.VisitProcess visitField(IField field) {
			String fieldPath = modelService.getPath(field);
			headers.add(new Header(fieldPath, field.getFieldType()));
			return DocumentModelWalker.VisitProcess.CONTINUE_TRAVERSAL;
		}
	}

	private static Map<String, String> flattenJsonRecursive(String prefix, JsonNode jsonNode, Map<String, String> flattenedMap) {
		if (jsonNode.isObject()) {
			jsonNode.propertyStream().forEach(field -> {
				String key = field.getKey();
				JsonNode value = field.getValue();
				String newPrefix = prefix.isEmpty() ? FIELD_SEPARATOR + key : prefix + FIELD_SEPARATOR + key;
				processNode(newPrefix, value, flattenedMap);
			});
		} else if (jsonNode.isArray()) {
			for (int i = 0; i < jsonNode.size(); i++) {
				String newPrefix = prefix + "[" + i + "]";
				processNode(newPrefix, jsonNode.get(i), flattenedMap);
			}
		} else {
			flattenedMap.put(prefix, jsonNode.isNull() ? null : jsonNode.asText());
		}
		return flattenedMap;
	}

	private static void processNode(String prefix, JsonNode node, Map<String, String> flattenedMap) {
		if (node.isObject() || node.isArray()) {
			flattenJsonRecursive(prefix, node, flattenedMap);
		} else {
			flattenedMap.put(prefix, node.isNull() ? null : node.asText());
		}
	}

}
