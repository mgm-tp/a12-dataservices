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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.mgmtp.a12.dataservices.cdd.CddConstants;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.exception.query.QueryJsonParsingException;
import com.mgmtp.a12.dataservices.exception.query.QueryNotFoundException;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.projection.internal.CddProjectionImplementation;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_GENERAL;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_RESPONSE_MAPPING;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY;
import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;
import static com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils.getCrdModelName;
import static com.mgmtp.a12.dataservices.utils.internal.ModelUtils.isRepeatable;

@Slf4j
@RequiredArgsConstructor
@Component public class DocumentTreeHelper {

	private final IDocumentFactory documentFactory;
	private final ObjectMapper objectMapper;
	private final DocumentModelUtils documentModelUtils;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	public void removeUnwantedFields(DocumentTreeResult documentTreeResult, Collection<String[]> fieldElements) {
		JsonNode parentNode = documentTreeResult.getDocument();
		List<Pair<ObjectNode, String>> toRemove = removeUnwantedFieldsRecursively(parentNode, fieldElements).toList();
		toRemove.forEach(p -> p.getLeft().remove(p.getRight()));
	}

	private static Stream<Pair<ObjectNode, String>> removeUnwantedFieldsRecursively(JsonNode parentNode, Collection<String[]> fieldElements) {
		if (parentNode instanceof ObjectNode objectNode) {
			Set<String> headElements = fieldHeads(fieldElements);
			Collection<String[]> fieldsWithoutHeads = fieldsWithoutHead(fieldElements);
			return objectNode.properties().stream()
				.flatMap(child -> headElements.contains(child.getKey())
					? removeUnwantedFieldsRecursively(child.getValue(), fieldsWithoutHeads)
					: Stream.of(Pair.of(objectNode, child.getKey())));
		} else if (parentNode instanceof ArrayNode arrayNode) {
			return StreamSupport.stream(arrayNode.spliterator(), false)
				.flatMap(child -> removeUnwantedFieldsRecursively(child, fieldElements));
		} else {
			return Stream.empty();
		}
	}

	/**
	 * Convert Json ArrayNode from {@link DocumentTreeResult} to stream of {@link IFieldInstance}.
	 *
	 * @param prefix the string value to prepend to each extracted field path.
	 * @param documentTreeResult the result need to be converted its document content into fields.
	 * @param hierarchy links hierarchy for calculating prefix repetitions.
	 * @param cdm The model of cdm to construct stream of {@link IFieldInstance}, in case of null value, the calculation will treat result as normal `Document`.
	 * @return set of converted {@link IFieldInstance} from {@link DocumentTreeResult}.
	 * @deprecated Used only in deprecated {@link CddProjectionImplementation}
	 */
	@Deprecated(since = "38.0.0")
	public Stream<IFieldInstance> toFields(String prefix, DocumentTreeResult documentTreeResult, LinkHierarchy hierarchy,
		IDocumentModel cdm) {
		JsonNode document = documentTreeResult.getDocument();
		if (document instanceof ArrayNode arrayNode) {
			Optional<IDocumentModelSearchService> documentModelSearchServiceOptional =
				Optional.ofNullable(cdm).map(documentModelServiceFactory::createDocumentModelSearchService);

			return cdm == null ?
				StreamSupport.stream(arrayNode.spliterator(), false)
					.map(a -> toField(prefix, new int[0], a, documentModelSearchServiceOptional))
					.filter(Objects::nonNull)
				: StreamSupport.stream(arrayNode.spliterator(), false)
				.map(a -> toField(
					prefix,
					calculateRepetitionPrefix(prefix, documentTreeResult, hierarchy),
					a,
					documentModelSearchServiceOptional))
				.filter(Objects::nonNull)
				// Only add fields that are in the CDM
				.filter(field -> isPresentInCdm(field, cdm));
		} else {
			throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null)
				.withAnonymityMessage("Can not convert document content to fields. Array is expected.");
		}
	}

	/**
	 * Retrieves all field names of a given model.
	 *
	 * @param cdmName The name of the model whose field names are to be retrieved.
	 * @param context The query context associated with the request.
	 * @param documentModelService The service used to access document model information.
	 * @return A list of strings representing the field names of the model.
	 */
	public List<String> getAllFieldNamesOfCdm(@NonNull String cdmName, @NonNull QueryContext context, @NonNull IDocumentModelService documentModelService) {

		List<String> fields = new ArrayList<>();
		IDocumentModelSearchService crdSearchService = context.getDocumentModelSearchService(getCrdModelName(context.getDocumentModel(cdmName)));
		new DocumentModelWalker().acceptDocumentModel(context.getDocumentModel(cdmName), new DocumentModelVisitor() {
			@Override public DocumentModelWalker.VisitProcess visitField(IField field) {
				if (!AttachmentSupport.isAttachmentContentField(field)) {
					Optional.ofNullable(field)
						.map(documentModelService::getPath)
						.filter(path -> crdSearchService.getByPath(path).isPresent())
						.ifPresent(fields::add);
				}
				return super.visitField(field);
			}
		});
		return fields;
	}

	/**
	 * Convert a single {@link JsonNode} which should be an {@link ArrayNode} into {@link IFieldInstance}.
	 * {@link UnexpectedException} will be thrown in case input node is not instanceof {@link ArrayNode}
	 *
	 * @param prefix the string value to prepend to the extracted field path.
	 * @param repetitionPrefix the integer array to be used for appending to field repetition.
	 * @param node The input {@link JsonNode} to be converted.
	 * @param documentModelSearchServiceOptional The document model search service of cdm to construct stream of {@link IFieldInstance}, in case of null value, the calculation will treat result as normal `Document`.
	 * @return {@link IFieldInstance} from {@link JsonNode}.
	 * @deprecated The only caller is deprecated.
	 */
	@Deprecated(since = "38.0.0")
	private IFieldInstance toField(String prefix, int[] repetitionPrefix, JsonNode node,
		Optional<IDocumentModelSearchService> documentModelSearchServiceOptional) {
		if (node instanceof ArrayNode arrayNode) {
			String fieldPath = extractPath(arrayNode);
			String fullFieldPath = "%s%s".formatted(prefix, fieldPath);
			return documentFactory.createFieldInstance(
				fullFieldPath,
				ArrayUtils.addAll(repetitionPrefix, extractRepetitions(arrayNode, objectMapper)),
				extractValue(arrayNode, documentModelSearchServiceOptional, fullFieldPath)
			);
		} else {
			throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null)
				.withAnonymityMessage("Can not convert field content to field entity. Array is expected.");
		}
	}

	/**
	 * Extract the value part from {@link ArrayNode}.
	 * The input arrayNode should have this pattern: "["{field_path}", [{repetition}], "{field_value}"]".
	 *
	 * @param arrayNode the input {@link ArrayNode} to be extracted.
	 * @param documentModelSearchServiceOptional documentModelSearchServiceOptional The document model search service of cdm to construct stream of {@link IFieldInstance}, in case of null value, the calculation will treat result as normal `Document`.
	 * @param fullFieldPath The full path of field in case for CDM.
	 * @return String value from {@link ArrayNode}.
	 */
	private Object extractValue(ArrayNode arrayNode, Optional<IDocumentModelSearchService> documentModelSearchServiceOptional, String fullFieldPath) {
		JsonNode n;
		n = arrayNode.get(2);
		if (n.isNull()) {
			return null;
		}
		Assert.isTrue(n.isTextual(), "Can not convert field content to field entity. Value must be a text.");
		String textValue = n.textValue();

		return documentModelSearchServiceOptional
			.flatMap(documentModelSearchService -> DocumentModelUtils.findField(documentModelSearchService, fullFieldPath))
			.map(field -> field.getEffectiveType().orElseThrow(() -> new QueryNotFoundException(
				QUERY_GENERAL, MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY, "Effective type code not be found for field [%s]".formatted(field))
				.withAnonymityMessage("Effective type code could not be found.")))
			.map(fieldType -> {
				try {
					if (fieldType instanceof INumberType) {
						return new BigDecimal(textValue);
					} else {
						return textValue;
					}
				} catch (Exception e) {
					log.warn("Cannot properly format {}.", fullFieldPath);
					return textValue;
				}
			}).orElse(textValue);
	}

	/**
	 * Extract the repetition part from {@link ArrayNode}.
	 * The input arrayNode should have this pattern: "["{field_path}", [{repetition}], "{field_value}"]".
	 *
	 * @param arrayNode the input {@link ArrayNode} to be extracted.
	 * @return Integer repetition array from {@link ArrayNode}.
	 */
	private static int[] extractRepetitions(ArrayNode arrayNode, ObjectMapper objectMapper) {
		JsonNode n;
		n = arrayNode.get(1);
		assert n.isArray() : "Can not convert field content to field entity. Repetitions must be an array.";
		int[] repetitions;
		try {
			repetitions = objectMapper.treeToValue(n, int[].class);
		} catch (JsonProcessingException e) {
			throw new QueryJsonParsingException(QUERY_RESPONSE_MAPPING, null, e)
				.withAnonymityMessage("Can not convert field content to field entity. Repetitions is not deserializable into int[].");
		}
		return repetitions;
	}

	/**
	 * Extract the path part from {@link ArrayNode}.
	 * The input arrayNode should have this pattern: "["{field_path}", [{repetition}], "{field_value}"]".
	 *
	 * @param arrayNode the input {@link ArrayNode} to be extracted.
	 * @return String path value from {@link ArrayNode}.
	 */
	private static String extractPath(ArrayNode arrayNode) {
		JsonNode n = arrayNode.get(0);
		assert n.isTextual() : "Can not convert field content to field entity. Path must be a text.";
		return n.textValue();
	}

	public ObjectNode constructDocumentFromFieldsAndLinks(DocumentTreeResult rootDocument, IDocumentModel documentModel,
		@NonNull Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linkedDocuments) {

		ObjectNode document = objectMapper.createObjectNode();
		constructResultRecursively(document, documentModel.getContent().getDocumentModelRoot(), rootDocument, "", JsonPointer.empty(), linkedDocuments);
		return document;
	}

	private void constructResultRecursively(ObjectNode parentJsonNode, IGroup parentGroup, DocumentTreeResult currentDocument, String documentPathPrefix,
		JsonPointer jsonPointerPrefix, Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linkedDocuments) {

		parentGroup.getElements().forEach(e -> {
			if (e instanceof IGroup group) {
				if (!ModelUtils.hasAnnotation(group, CddConstants.CDM_RELATIONSHIP_ANNOTATION)
					&& isEmptyElement(currentDocument.getDocument().at(jsonPointerPrefix.appendProperty(group.getName())))) {
					return;
				}
				String documentPath = "%s%s%s".formatted(documentPathPrefix, FIELD_SEPARATOR, group.getName());
				ModelUtils.getAnnotationValue(group, CDM_RELATIONSHIP_ANNOTATION)
					.ifPresentOrElse(
						r -> processLinkGroup(parentJsonNode, currentDocument, linkedDocuments, group, documentPath),
						() -> processNativeGroup(parentJsonNode, currentDocument, documentPathPrefix, jsonPointerPrefix, linkedDocuments, group,
							documentPath));
			} else if (e instanceof IField field) {
				JsonNode valueNode = currentDocument.getDocument().at(jsonPointerPrefix.appendProperty(field.getName()));
				if (valueNode instanceof ValueNode value && !value.isNull() && !value.isMissingNode()) {
					parentJsonNode.set(field.getName(), valueNode);
				}
			}

		});
	}

	private static boolean isEmptyElement(JsonNode jsonNode) {
		return jsonNode.isMissingNode()
			|| jsonNode.isContainerNode() && jsonNode.isEmpty()
			|| jsonNode.isValueNode() && jsonNode.isNull();
	}

	private void processNativeGroup(ObjectNode parentJsonNode, DocumentTreeResult currentDocument, String documentPathPrefix, JsonPointer jsonPointerPrefix,
		Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linkedDocuments, IGroup group, String documentPath) {
		if (isRepeatable(group)) {
			processRepeatableGroup(parentJsonNode, currentDocument, documentPathPrefix, jsonPointerPrefix, linkedDocuments, group);
		} else {
			constructResultRecursively(
				parentJsonNode.withObject(group.getName()), group, currentDocument,
				documentPath, jsonPointerPrefix.appendProperty(group.getName()),
				linkedDocuments);
		}
	}

	private void processRepeatableGroup(ObjectNode parentJsonNode, DocumentTreeResult currentDocument, String documentPathPrefix, JsonPointer jsonPointerPrefix,
		Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linkedDocuments, IGroup group) {
		ArrayNode parentArray = parentJsonNode.withArray(group.getName());
		JsonNode arrayNode = currentDocument.getDocument().at(jsonPointerPrefix.appendProperty(group.getName()));
		if (!arrayNode.isMissingNode()) {
			for (int i = 0; i < arrayNode.size(); i++) {
				ObjectNode on = objectMapper.createObjectNode();
				parentArray.add(on);
				constructResultRecursively(on, group, currentDocument, documentPathPrefix + FIELD_SEPARATOR + group.getName(),
					jsonPointerPrefix.appendProperty(group.getName()).appendIndex(i), linkedDocuments);
			}
		}
	}

	private void processLinkGroup(ObjectNode parentJsonNode, DocumentTreeResult currentDocument,
		Map<DocumentReference, Map<String, List<DocumentTreeResult>>> linkedDocuments, IGroup group, String currentDocumentPath) {
		if (isRepeatable(group)) {

			ArrayNode arrayNode = parentJsonNode.withArray(group.getName());
			Optional.ofNullable(linkedDocuments.get(currentDocument.getDocRef()))
				.map(m -> m.get(currentDocumentPath)).stream()
				.flatMap(Collection::stream)
				.forEach(link -> {
					ObjectNode on = objectMapper.createObjectNode();
					arrayNode.add(on);
					constructResultRecursively(on, group, link, currentDocumentPath, JsonPointer.empty(), linkedDocuments);
				});
		} else {
			Optional.ofNullable(linkedDocuments.get(currentDocument.getDocRef()))
				.map(m -> m.get(currentDocumentPath)).stream()
				.flatMap(Collection::stream)
				.findAny()
				.ifPresent(link -> constructResultRecursively(parentJsonNode.withObject(group.getName()), group, link, currentDocumentPath, JsonPointer.empty(),
					linkedDocuments));
		}
	}

	private static int @NonNull [] calculateRepetitionPrefix(String prefix, DocumentTreeResult documentTreeResult,
		LinkHierarchy hierarchy) {
		if (hierarchy == null) {
			return new int[0];
		}
		int[] repetitionPrefix = IntStream.range(1, prefix.split("/").length).map(x -> 1).toArray();
		if (repetitionPrefix.length > 0) {
			repetitionPrefix[repetitionPrefix.length - 1] = hierarchy.getRepetition(documentTreeResult);
		}
		return repetitionPrefix;
	}

	private boolean isPresentInCdm(IFieldInstance field, IDocumentModel cdm) {
		return documentModelUtils.findField(cdm, field.getPath()).isPresent();
	}

	public static class LinkHierarchy {

		/**
		 * relationshipModel -> sourceRole -> sourceDocRef -> linkId -> localRepetition
		 */
		private final Map<String, Map<String, Map<DocumentReference, Map<String, Integer>>>> hierarchy = new HashMap<>();

		public LinkHierarchy(Iterable<DocumentTreeResult> links) {
			links.forEach(link -> {
				Map<String, Integer> documentReferenceIntegerMap = hierarchy
					.computeIfAbsent(link.getRelationshipModel(), k -> new HashMap<>())
					.computeIfAbsent(link.getSourceRole(), k -> new HashMap<>())
					.computeIfAbsent(link.getSourceDocRef(), k -> new HashMap<>());
				documentReferenceIntegerMap.putIfAbsent(link.getLinkId(), documentReferenceIntegerMap.size() + 1);
			});
		}

		public int getRepetition(DocumentTreeResult documentTreeResult) {
			return hierarchy
				.getOrDefault(documentTreeResult.getRelationshipModel(), new HashMap<>())
				.getOrDefault(documentTreeResult.getSourceRole(), new HashMap<>())
				.getOrDefault(documentTreeResult.getSourceDocRef(), new HashMap<>())
				.getOrDefault(documentTreeResult.getLinkId(), 1);
		}
	}

	@NotNull private static Set<String> fieldHeads(Collection<String[]> fieldElements) {
		@NotNull Set<String> headElements = fieldElements.stream()
			.filter(Objects::nonNull)
			.filter(fe -> fe.length > 0)
			.map(fe -> fe[0])
			.collect(Collectors.toSet());
		return headElements;
	}

	@NotNull private static Collection<String[]> fieldsWithoutHead(Collection<String[]> fieldElements) {
		fieldElements = fieldElements.stream()
			.filter(Objects::nonNull)
			.filter(fe -> fe.length > 0)
			.map(e -> Arrays.copyOfRange(e, 1, e.length))
			.collect(Collectors.toSet());
		return fieldElements;
	}
}
