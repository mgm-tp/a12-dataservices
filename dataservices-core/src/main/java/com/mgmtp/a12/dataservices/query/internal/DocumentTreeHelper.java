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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.cdd.CddConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.model.ModelConstants.FIELD_SEPARATOR;
import static com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils.getCrdModelName;
import static com.mgmtp.a12.dataservices.utils.internal.ModelUtils.isRepeatable;

@Slf4j
@RequiredArgsConstructor
@Component public class DocumentTreeHelper {

	private final ObjectMapper objectMapper;

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
			|| jsonNode.isContainer() && jsonNode.isEmpty()
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
