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

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.model.header.Header;

import tools.jackson.databind.ObjectMapper;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_INDEXING;
import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

@Component public class DocumentModelFieldsIndexer {

	public static final String INDEXED_FIELD_ANNOTATION = "indexed";
	private final DocumentModelUtils documentModelUtils;
	private final IDocumentModelService modelService;
	private final ModelFieldsJpaRepository modelFieldsJpaRepository;
	private final ObjectMapper jsonMapper;

	public DocumentModelFieldsIndexer(DocumentModelUtils documentModelUtils, IDocumentModelService modelService,
		ModelFieldsJpaRepository modelFieldsJpaRepository, ObjectMapper jsonMapper) {
		this.documentModelUtils = documentModelUtils;
		this.modelService = modelService;
		this.modelFieldsJpaRepository = modelFieldsJpaRepository;
		this.jsonMapper = jsonMapper.rebuild()
			.addMixIn(IField.class, IFieldJsonHints.class)
			.build();
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

		new DocumentModelWalker().acceptDocumentModel(
			updatedModel, new DocumentModelVisitor() {

				@Override public DocumentModelWalker.VisitProcess visitField(@NotNull IField field) {
					IFieldType effectiveFieldType = getEffectiveFieldType(field, QUERY_INDEXING);
					if (isIndexable(field)) {
						String path = modelService.getPath(field);
						modelFieldsJpaRepository.save(ModelFieldEntity.builder()
							.modelName(modelName)
							.fieldName(path)
							.fieldType(QueryTopologyHelper.fieldTypeAsString(effectiveFieldType))
							.data(jsonMapper.valueToTree(field))
							.build());
					}

					return super.visitField(field);
				}
			});
	}

	private IDocumentModel genericToDocumentModel(GenericModel updatedModel) {
		return documentModelUtils.deserializeDocumentModel(updatedModel.getHeader().getId(), updatedModel.getContent().getRawContent());
	}

	private void deleteOldFields(String modelName) {
		modelFieldsJpaRepository.deleteByModelName(modelName);
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
