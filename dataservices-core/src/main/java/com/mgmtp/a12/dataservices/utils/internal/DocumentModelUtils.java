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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.exception.DocumentModelDeSerializationException;
import com.mgmtp.a12.dataservices.model.exception.DocumentModelSerializationException;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_INTEGRITY_ERROR_KEY;

/**
 * Internal tools to automatize common tasks with document models.
 */
@RequiredArgsConstructor
@Component public class DocumentModelUtils {

	private final DocumentModelServiceFactory documentModelServiceFactory;

	protected final IDocumentModelSerializer documentModelSerializer;

	protected final HeaderParser headerParser;

	public static IFieldType getFieldType(IFieldInstance fieldInstance, IDocumentModelSearchService modelSearchService) {
		return modelSearchService.getByPath(fieldInstance.getPath())
			.map(IField.class::cast)
			.flatMap(IField::getEffectiveType)
			.orElseThrow(() -> new InvalidInputException(DOCUMENT_INTEGRITY_ERROR_KEY,
				String.format("Unable to get effective type for field %s", fieldInstance.getPath())).withAnonymityMessage(
				"Unable to get effective field type."));
	}

	/**
	 * Returns all fields paths for the given document model.
	 *
	 * @param documentModel the document model to traverse.
	 * @return the paths of all fields in the given document model.
	 */
	public List<String> getAllFieldPaths(IDocumentModel documentModel) {
		List<String> paths = new ArrayList<>();
		IDocumentModelService documentModelService = documentModelServiceFactory.createDocumentModelService();
		new DocumentModelWalker().acceptDocumentModel(documentModel, new DocumentModelVisitor() {
			@Override public DocumentModelWalker.VisitProcess visitField(IField field) {
				paths.add(documentModelService.getPath(field));
				return super.visitField(field);
			}
		});
		return paths;
	}

	/**
	 * Checks whether field corresponding to fieldInstance is correct type.
	 *
	 * @param requiredType class of type the field is supposed to be.
	 * @param fieldInstance field instance to determine field definition.
	 * @param documentModel document model field definitions.
	 * @param <U> field type we expect.
	 * @return true in case the field is of required type. False otherwise.
	 */
	public <U extends IFieldType> boolean checkFieldType(Class<U> requiredType, IFieldInstance fieldInstance, IDocumentModel documentModel) {
		return findField(documentModel, fieldInstance.getPath())
			.flatMap(IField::getEffectiveType)
			.map(IFieldType::getClass)
			.filter(requiredType::isAssignableFrom)
			.isPresent();
	}

	/**
	 * Get field determined by path.
	 *
	 * @param documentModelSearchService search service to get the field definition.
	 * @param path path determining the field.
	 * @return Empty Optional if on the path there is not entity of type {@link IField}. Optional of field declaration otherwise.
	 */
	public static Optional<IField> findField(IDocumentModelSearchService documentModelSearchService, String path) {
		return Optional.ofNullable(documentModelSearchService)
			.flatMap(searchService -> searchService.getByPath(path))
			.filter(IField.class::isInstance)
			.map(IField.class::cast);
	}

	/**
	 * Get field determined by path from document model.
	 *
	 * @param documentModel document model holding field definitions.
	 * @param path path determining the field.
	 * @return Empty Optional if on the path there is not entity of type {@link IField}. Optional of field declaration otherwise.
	 */
	public Optional<IField> findField(IDocumentModel documentModel, String path) {
		IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
		return findField(documentModelSearchService, path);
	}

	/**
	 * Get the field determined by the path from one of the passed models.
	 *
	 * @param documentModels A collection of document models.
	 * @param path The path determining the field.
	 * @return Empty Optional if on the path there is not entity of type {@link IField}. Optional of field declaration otherwise.
	 */
	public Optional<IField> findFieldInModels(Collection<IDocumentModel> documentModels, String path) {
		for (IDocumentModel documentModel : documentModels) {
			Optional<IField> field = findField(documentModel, path);
			if (field.isPresent()) {
				return field;
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the type of the field determined by path from document model.
	 *
	 * @param documentModelSearchService search service to get the field definition.
	 * @param path path determining the field.
	 * @return Empty Optional if on the path there is not entity of type {@link IField}. Optional of field type declaration otherwise.
	 */
	public static Optional<IFieldType> getFieldType(IDocumentModelSearchService documentModelSearchService, String path) {
		return findField(documentModelSearchService, path)
			.map(IField::getFieldType);
	}

	/**
	 * Serialize {@link IDocumentModel} and write it to writer.
	 *
	 * @param writer to write document model content to.
	 * @param documentModel document model to serialize.
	 * @throws DocumentModelSerializationException in case of serialization problem.
	 */
	public void serializeDocumentModel(Writer writer, IDocumentModel documentModel) {
		try {
			ListIProblemReporter pr = new ListIProblemReporter();
			documentModelSerializer.serialize(documentModel, writer, pr);
			pr.validate(ExceptionCodes.DOCUMENT_MODEL_SERIALIZATION_EXCEPTION_CODE, ExceptionKeys.DOCUMENT_MODEL_SERIALIZATION_ERROR_KEY,
				"Error while document model serialization");
		} catch (IOException e) {
			throw new DocumentModelSerializationException(documentModel.getHeader().getId(), e).withAnonymityMessage("Deserialization of model failed.");
		}
	}

	/**
	 * Serialize {@link IDocumentModel} and return its content as a String.
	 *
	 * @param documentModel document model to serialize.
	 * @return content of the document model.
	 * @throws DocumentModelSerializationException in case of serialization problem.
	 */
	public String serializeDocumentModel(IDocumentModel documentModel) {
		try (Writer w = new StringWriter()) {
			serializeDocumentModel(w, documentModel);
			return w.toString();
		} catch (IOException e) {
			throw new UnexpectedException(e);
		}
	}

	/**
	 * Get {@link IDocumentModel} from {@link ModelEntity} instance.
	 *
	 * @param modelEntity model entity holding the model content.
	 * @return {@link IDocumentModel} created from modelEntity content.
	 * @throws DocumentModelDeSerializationException in case of deserialization problem.
	 */
	public IDocumentModel deserializeDocumentModel(ModelEntity modelEntity) {
		return deserializeDocumentModel(modelEntity.getId(), modelEntity.getContent());
	}

	/**
	 * Get {@link IDocumentModel} from content of model.
	 *
	 * @param modelContent content of the model.
	 * @return {@link IDocumentModel} created from model content.
	 * @throws DocumentModelDeSerializationException in case of deserialization problem.
	 */
	public IDocumentModel deserializeDocumentModel(String modelContent) {
		try (StringReader reader = new StringReader(modelContent)) {
			return deserializeDocumentModel(reader);
		}
	}

	/**
	 * Get {@link IDocumentModel} from content of model.
	 *
	 * @param documentModelName model ID.
	 * @param modelContent content of the model.
	 * @return {@link IDocumentModel} created from model modelContent.
	 * @throws DocumentModelDeSerializationException in case of deserialization problem.
	 */
	public IDocumentModel deserializeDocumentModel(String documentModelName, String modelContent) {
		try (Reader modelReader = new StringReader(modelContent)) {
			return deserializeDocumentModel(documentModelName, modelReader);
		} catch (IOException e) {
			throw new UnexpectedException(e);
		}
	}

	/**
	 * Get {@link IDocumentModel} from content of model.
	 *
	 * @param documentModelName model ID.
	 * @param modelContentReader content of the model.
	 * @return {@link IDocumentModel} created from model modelContent.
	 * @throws DocumentModelDeSerializationException in case of deserialization problem.
	 */
	public IDocumentModel deserializeDocumentModel(String documentModelName, Reader modelContentReader) {
		String modelContent = null;
		try {
			if (documentModelName == null) {
				// If the documentModelName is not provided, we store the whole model content to parse the modelId from the header in case of an exception.
				// Otherwise, the original Reader could be used because there is no other need to read it again.
				modelContent = IOUtils.toString(modelContentReader);
				modelContentReader = new StringReader(modelContent);
			}
			return documentModelSerializer.deserialize(modelContentReader);
		} catch (IOException e) {
			if (documentModelName == null && modelContent != null) {
				try {
					documentModelName = headerParser.parseJson(modelContent).getId();
				} catch (Exception ignored) {
					// If the documentModelName is not provided, we try to parse it from the model. If it fails, we keep the model name as null.
				}
			}
			throw new DocumentModelDeSerializationException(documentModelName, e).withAnonymityMessage("Model deserialization failed.");
		}
	}

	/**
	 * Get {@link IDocumentModel} from content of model.
	 *
	 * @param modelContent content of the model.
	 * @return {@link IDocumentModel} created from model modelContent.
	 * @throws DocumentModelDeSerializationException in case of deserialization problem.
	 */
	public IDocumentModel deserializeDocumentModel(Reader modelContent) {
		return deserializeDocumentModel(null, modelContent);
	}

	/**
	 * Util for abstract property look-up from header
	 *
	 * @param header of the possibly abstract model
	 * @return true if header is of abstract model
	 */
	public boolean isAbstract(Header header) {
		return Optional.ofNullable(header.getAnnotations()).stream()
			.flatMap(Collection::stream)
			.filter(a -> "abstract".equals(a.getName()))
			.map(Annotation::getValue)
			.anyMatch(value -> StringUtils.isBlank(value) || "true".equalsIgnoreCase(value));
	}
}
