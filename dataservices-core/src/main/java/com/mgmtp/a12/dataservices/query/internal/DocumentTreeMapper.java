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
package com.mgmtp.a12.dataservices.query.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.exception.query.QueryJsonParsingException;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.DocumentTreeEntity;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_RESPONSE_MAPPING;
import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class DocumentTreeMapper {

	public static final String WRONG_PARTIAL_DOCUMENT_STRUCTURE_MESSAGE = "Wrong partial document structure.";
	@Autowired private ObjectMapper objectMapper;

	public static final String MISSING_VALUE = "''";

	public static final String ID_PREFIX = "id.";
	public static final String DOC_REF = "docRef";
	public static final String RELATIONSHIP_MODEL = "relationshipModel";
	public static final String SOURCE_ROLE = "sourceRole";
	public static final String SOURCE_DOC_REF = "sourceDocRef";
	public static final String TARGET_ROLE = "targetRole";
	public static final String TARGET_DOC_REF = "targetDocRef";
	public static final String LINK_ID = "linkId";
	public static final String TYPE = "type";
	public static final String BACK_REFERENCE = "backReference";
	public static final String INTERNAL_ID = "internalId";
	public static final String DEPTH = "depth";
	public static final String FIELDS_PROJECTION = "fieldsProjection";
	public static final String JAVA_PREFIX = "java(";
	public static final String JAVA_SUFFIX = ")";
	public static final String MISSING_VALUE_EQUALS = "!\"" + MISSING_VALUE + "\".equals(";
	public static final String MISSING_VALUE_SUFFIX = ")";

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Mapping(target = "document", expression = "java(contentToJsonNode(input.getContent()))")
	@Mapping(source = ID_PREFIX + DOC_REF, target = DOC_REF)
	@Mapping(source = ID_PREFIX + BACK_REFERENCE, target = BACK_REFERENCE, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + BACK_REFERENCE
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + INTERNAL_ID, target = INTERNAL_ID, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + INTERNAL_ID
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + LINK_ID, target = LINK_ID, conditionExpression = "java(linkId != null && !\"" + QueryGeneratorConstants.EMPTY_STRING
		+ "\".equals(linkId))")
	@Mapping(source = ID_PREFIX + RELATIONSHIP_MODEL, target = RELATIONSHIP_MODEL, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + RELATIONSHIP_MODEL
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + TYPE, target = TYPE)
	@Mapping(source = ID_PREFIX + SOURCE_DOC_REF, target = SOURCE_DOC_REF, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + SOURCE_DOC_REF
		+ MISSING_VALUE_SUFFIX + " && sourceDocRef.isValid()" + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + SOURCE_ROLE, target = SOURCE_ROLE, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + SOURCE_ROLE
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + TARGET_DOC_REF, target = TARGET_DOC_REF, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + TARGET_DOC_REF
		+ MISSING_VALUE_SUFFIX + " && targetDocRef.isValid()" + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + TARGET_ROLE, target = TARGET_ROLE, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + TARGET_ROLE
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + DEPTH, target = DEPTH)
	@Mapping(source = ID_PREFIX + FIELDS_PROJECTION, target = FIELDS_PROJECTION)

	public abstract DocumentTreeResult mapToDocumentTreeResult(DocumentTreeEntity input);

	@Named("stringToJsonNode") public JsonNode contentToJsonNode(String content) {
		try {
			return content == null ? objectMapper.createObjectNode() : objectMapper.readTree(content);
		} catch (JsonProcessingException e) {
			throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null, e)
				.withAnonymityMessage("Content is not JSON.");
		}
	}

	public static JsonNode stringToJsonNode(String jsonString) throws JsonProcessingException {

		JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonString);

		if (jsonNode instanceof ArrayNode fieldsContainer) {

			Map<String, Map<Integer, DocumentNode>> nodes = new HashMap<>();

			fieldsContainer.iterator().forEachRemaining(field -> {
				if (field instanceof ArrayNode fieldStructure) {
					String path = fieldStructure.get(0).textValue();
					int[] repetition = extractRepetition(fieldStructure);
					String value = fieldStructure.get(2).textValue();

					String[] segments = path.split(FIELD_SEPARATOR);

					Map<String, Map<Integer, DocumentNode>> parent = nodes;

					var ref = new Object() {
						String nodePath = "";
						int repetition = 0;
					};
					for (int i = 1; i < segments.length - 1; i++) {
						String segment = segments[i];
						ref.nodePath = "%s/%s".formatted(ref.nodePath, segment);
						ref.repetition = repetition[i - 1];
						parent = parent.computeIfAbsent(segment, s -> new HashMap<>())
							.computeIfAbsent(repetition[ref.repetition], r -> new DocumentNode(ref.nodePath, repetition[ref.repetition], null, new HashMap<>()))
							.children();
					}

					if (parent.containsKey(segments[segments.length - 1])) {
						throw new QueryInvalidInputException(QUERY_RESPONSE_MAPPING, null)
							.withAnonymityMessage(WRONG_PARTIAL_DOCUMENT_STRUCTURE_MESSAGE);
					} else {
						parent.computeIfAbsent(segments[segments.length - 1], s -> new HashMap<>())
							.put(repetition[segments.length - 2],
								new DocumentNode(ref.nodePath + '/' + segments[segments.length - 1], repetition[segments.length - 2], value, null));
					}

				} else {
					throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null)
						.withAnonymityMessage(WRONG_PARTIAL_DOCUMENT_STRUCTURE_MESSAGE);
				}
			});

			return nodesToJson(nodes);

		} else {
			throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null)
				.withAnonymityMessage(WRONG_PARTIAL_DOCUMENT_STRUCTURE_MESSAGE);
		}

	}

	private static JsonNode nodesToJson(Map<String, Map<Integer, DocumentNode>> nodes) {

		if (nodes == null) {
			return OBJECT_MAPPER.nullNode();
		}

		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		for (Map.Entry<String, Map<Integer, DocumentNode>> entry : nodes.entrySet()) {
			root.set(entry.getKey(), nodeToJson(entry.getValue()));
		}

		return root;
	}

	private static JsonNode nodeToJson(Map<Integer, DocumentNode> value) {
		if (MapUtils.isEmpty(value)) {
			return OBJECT_MAPPER.nullNode();
		} else if (value.size() == 1) {
			return nodeToJson(value.values().iterator().next());
		} else {
			ArrayNode root = OBJECT_MAPPER.createArrayNode();
			value.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(v -> root.add(nodeToJson(v.getValue())));
			return root;
		}
	}

	private static JsonNode nodeToJson(DocumentNode next) {
		if (next.value() == null) {
			return nodesToJson(next.children);
		} else {
			return new TextNode(next.value());
		}
	}

	private static int[] extractRepetition(ArrayNode arrayNode) {
		JsonNode jsonNode = arrayNode.get(1);
		if (jsonNode instanceof ArrayNode arrayNode1) {
			return OBJECT_MAPPER.convertValue(arrayNode1, int[].class);
		} else {
			throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null)
				.withAnonymityMessage(WRONG_PARTIAL_DOCUMENT_STRUCTURE_MESSAGE);
		}
	}

	record DocumentNode(String path, int repeatability, String value, Map<String, Map<Integer, DocumentNode>> children) {

		@Override public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			DocumentNode that = (DocumentNode) o;
			return repeatability == that.repeatability && Objects.equals(path, that.path);
		}

		@Override public int hashCode() {
			return Objects.hash(path, repeatability);
		}
	}
}
