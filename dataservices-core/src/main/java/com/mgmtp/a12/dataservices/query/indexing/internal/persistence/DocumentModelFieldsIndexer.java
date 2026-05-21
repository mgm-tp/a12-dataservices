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
package com.mgmtp.a12.dataservices.query.indexing.internal.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.LocalizedFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.LocalizedFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_INDEXING;
import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;
@Component public class DocumentModelFieldsIndexer {

	public static final String INDEXED_FIELD_ANNOTATION = "indexed";
	private final DocumentModelUtils documentModelUtils;
	private final IDocumentModelService modelService;
	private final ModelFieldsJpaRepository modelFieldsJpaRepository;
	private final LocalizedFieldsJpaRepository localizedFieldsJpaRepository;
	private final ObjectMapper jsonMapper;
	private final SearchCustomizerRegistry searchCustomizerRegistry;

	public DocumentModelFieldsIndexer(DocumentModelUtils documentModelUtils, IDocumentModelService modelService,
		ModelFieldsJpaRepository modelFieldsJpaRepository, LocalizedFieldsJpaRepository localizedFieldsJpaRepository,
		SearchCustomizerRegistry searchCustomizerRegistry, ObjectMapper jsonMapper) {
		this.documentModelUtils = documentModelUtils;
		this.modelService = modelService;
		this.modelFieldsJpaRepository = modelFieldsJpaRepository;
		this.localizedFieldsJpaRepository = localizedFieldsJpaRepository;
		this.searchCustomizerRegistry = searchCustomizerRegistry;
		this.jsonMapper = jsonMapper.copy()
			.addMixIn(IField.class, IFieldJsonHints.class);
	}

	public void indexDocumentModelFieldsOnCreate(GenericModel updatedModel) {
		if (isDocumentModel(updatedModel.getHeader())) {
			updateNewFields(genericToDocumentModel(updatedModel));
		}
	}

	public void indexDocumentModelFieldsOnUpdate(IDocumentModel documentModel) {
		deleteOldFields(documentModel.getHeader().getId());
		updateNewFields(documentModel);
	}

	public void indexDocumentModelFieldsOnUpdate(GenericModel updatedModel) {
		if (isDocumentModel(updatedModel.getHeader())) {
			indexDocumentModelFieldsOnUpdate(genericToDocumentModel(updatedModel));
		}
	}

	public void indexDocumentModelFieldsOnDelete(GenericModel updatedModel) {
		Header header = updatedModel.getHeader();
		if (isDocumentModel(header)) {
			deleteOldFields(header.getId());
		}
	}

	public boolean isIndexable(IField field) {
		boolean isNotAttachmentContent = !AttachmentSupport.isAttachmentContentField(field);
		return isNotAttachmentContent &&
			field.getAnnotations().stream()
				.noneMatch(annotation -> Objects.equals(annotation.getName(), INDEXED_FIELD_ANNOTATION) &&
					Objects.equals(annotation.getValue(), "false"));
	}

	private void updateNewFields(IDocumentModel updatedModel) {
		String modelName = updatedModel.getHeader().getId();

		new DocumentModelWalker().acceptDocumentModel(updatedModel, new DocumentModelVisitor() {

			@Override public DocumentModelWalker.VisitProcess visitField(@NonNull IField field) {
				IFieldType effectiveFieldType = getEffectiveFieldType(field, QUERY_INDEXING);
				if (isIndexable(field)) {
					String path = modelService.getPath(field);

					ModelFieldEntity.ModelFieldEntityBuilder modelFieldEntityBuilder = ModelFieldEntity.builder()
						.modelName(modelName)
						.fieldName(path)
						.fieldType(QueryTopologyHelper.fieldTypeAsString(effectiveFieldType))
						.data(jsonMapper.valueToTree(field));
					Map<String, Map<String, LocalizedFieldEntity>> localizedFieldEntities = prepareLocalizedEntities(effectiveFieldType, path, modelName);
					searchCustomizerRegistry.customizeModelFields(modelName, path, field, effectiveFieldType, modelFieldEntityBuilder, localizedFieldEntities);
					modelFieldsJpaRepository.save(modelFieldEntityBuilder.build());
					processLocalizations(localizedFieldEntities);
				}
				return super.visitField(field);
			}
		});
	}

	private void processLocalizations(Map<String, Map<String, LocalizedFieldEntity>> localizedFieldEntities) {
		if (localizedFieldEntities.isEmpty()) {
			return;
		}

		// Collect all entities efficiently
		Set<LocalizedFieldEntity> localizedEntities = localizedFieldEntities.values().stream()
			.flatMap(innerMap -> innerMap.values().stream())
			.collect(Collectors.toSet());
		
		// Batch save all entities
		// Note: deleteOldFields() already deleted all localized fields for the model
		localizedFieldsJpaRepository.saveAll(localizedEntities);
		
	}

	private static @NonNull Map<String, Map<String, LocalizedFieldEntity>> prepareLocalizedEntities(IFieldType effectiveFieldType,
		String path, String modelName) {

		if (!(effectiveFieldType instanceof IEnumerationType enumerationType)) {
			return new HashMap<>();
		}

		// Optimized: Use pre-sized HashMap and avoid nested streams
		Map<String, Map<String, LocalizedFieldEntity>> result = new HashMap<>();
		
		for (var value : enumerationType.getValues()) {
			String originalValue = value.getValue();
			
			for (var labelEntry : value.getLabel().entrySet()) {
				String locale = labelEntry.getKey().toString();
				String localizedValue = labelEntry.getValue();
				
				// Create entity once with all values
				LocalizedFieldEntity entity = LocalizedFieldEntity.builder()
					.locale(locale)
					.localizedValue(localizedValue)
					.originalValue(originalValue)
					.modelName(modelName)
					.fieldName(path)
					.build();
				
				// Use computeIfAbsent to avoid multiple lookups
				result.computeIfAbsent(locale, k -> new HashMap<>())
					.put(originalValue, entity);
			}
		}
		
		return result;
	}

	private IDocumentModel genericToDocumentModel(GenericModel updatedModel) {
		return documentModelUtils.deserializeDocumentModel(updatedModel.getHeader().getId(), updatedModel.getContent().getRawContent());
	}

	private void deleteOldFields(String modelName) {
		// Delete model field entities and their corresponding localized fields
		// This removes all indexed fields, which will then be recreated for fields that are still indexable
		modelFieldsJpaRepository.deleteByModelName(modelName);
		localizedFieldsJpaRepository.deleteByModelName(modelName);
		modelFieldsJpaRepository.flush();
	}

	private static boolean isDocumentModel(Header header) {
		return StringUtils.equals(DOCUMENT_MODEL_TYPE, header.getModelType());
	}

	private interface IFieldJsonHints {

		@SuppressWarnings("unused")
		@JsonIgnore
		IGroup getParent();

		@SuppressWarnings("unused")
		@JsonIgnore
		Field getField();

		@SuppressWarnings("unused")
		@JsonIgnore
		Element getElement();

		@SuppressWarnings("unused")
		@JsonIgnore
		Optional<IFieldType> getEffectiveType();
	}
}
