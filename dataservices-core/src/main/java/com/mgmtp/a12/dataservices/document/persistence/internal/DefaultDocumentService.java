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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.io.StringReader;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentPart;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterUpdateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryCreateEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentAfterRepositoryUpdateEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentsAfterDeleteEvent;
import com.mgmtp.a12.dataservices.document.events.internal.DocumentsBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.dataservices.document.exception.DocumentValidationException;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.internal.DocumentValidationResultMapper;
import com.mgmtp.a12.dataservices.document.internal.MetadataUtils;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy;
import com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.dataservices.utils.internal.GroupValueConverter;
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_ABSTRACT_MODEL_ERROR_KEY;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.DOCUMENT_NOT_FOUND_ERROR_KEY;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY;

/**
 * Default implementation for all documents
 */
@DocumentationDiagram
@Slf4j
@RequiredArgsConstructor
@Component public class DefaultDocumentService implements DocumentService {

	private final ApplicationEventPublisher eventPublisher;
	private final Optional<AttachmentHandler> attachmentHandler;
	private final Optional<AttachmentSupport> attachmentSupport;
	private final List<IDocumentRepository> documentRepositories;
	private final DocumentFieldsJpaRepository documentFieldsJpaRepository;
	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final DocumentPermissionEvaluator documentPermissionEvaluator;
	private final ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	private final DocumentModelUtils documentModelUtils;
	private final KernelDocumentService kernelDocumentService;
	private final DocumentUtils documentUtils;
	private final ModelHeaderJpaRepository modelHeaderRepository;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final MetadataUtils metadataUtils;
	private final DefaultDataServicesDocumentFactory dataServicesDocumentFactory;
	private final DocumentModelServiceFactory documentModelServiceFactory;
	private final IModelLoader<IDocumentModel> documentModelLoader;
	private final Optional<DocumentSearchIndexBehaviour> indexBehavior;
	private final ModelFieldsJpaRepository modelFieldsJpaRepository;
	private final DocumentSearchJpaRepository documentSearchJpaRepository;
	private final QueryService queryService;
	private final DocumentSupport documentSupport;
	private final UniqueConstraintValidator uniqueConstraintValidator;
	private final IDocumentV2Serializer documentV2Serializer;

	private static final String REPOSITORY_NOT_FOUND = "No Document Repository found for model [%s]";

	/**
	 * @event {@link DocumentBeforeCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument create(@NonNull DocumentV2 document, Locale locale) {
		return create(document, locale, DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);
	}

	/**
	 * @event {@link DocumentBeforeCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument create(@NonNull DocumentV2 document, Locale locale,
		@NonNull DocumentValidationStrategy validationStrategy, @NonNull DocumentComputationStrategy computationStrategy) {
		documentPermissionEvaluator.checkDocumentCreatePermission(document);

		StopWatch stopWatch = StopWatch.createStarted();
		IDocumentRepository documentRepository = getDocumentRepository(document);

		DocumentReference documentReference = documentUtils.generateDocRef(document);
		document = metadataUtils.createDocumentMetadata(document, documentReference, UaaConnector.getCurrentUserName(), Instant.now(), null);
		DataServicesDocument documentToCreate = documentBeforeCreate(document);

		DocumentV2 computedDocument = computeAndValidateDocument(documentToCreate.getKernelDocument(), locale, validationStrategy, computationStrategy);
		DataServicesDocument updatedDataServiceDocument = dataServicesDocumentFactory.newDataServicesDocument(computedDocument);
		uniqueConstraintValidator.insert(computedDocument, updatedDataServiceDocument.getMetadata().getDocRef(), locale);
		documentRepository.create(updatedDataServiceDocument);
		indexFields(updatedDataServiceDocument);
		eventPublisher.publishEvent(new DocumentAfterRepositoryCreateEvent(updatedDataServiceDocument));
		syncAttachments(null, updatedDataServiceDocument);

		eventPublisher.publishEvent(new DocumentAfterCreateEvent(updatedDataServiceDocument));
		log.debug("Document [{}] has been created in [{}] ms", updatedDataServiceDocument.getMetadata().getDocRef(), stopWatch.getDuration());
		return updatedDataServiceDocument;
	}

	/**
	 * @event {@link DocumentBeforeCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument create(@NonNull String documentModelName, @NonNull JsonNode documentContent, Locale locale) {
		DocumentV2 document;
		try {
			document = documentSupport.convertJSONToDocument(documentModelName, documentContent);
		} catch (NotFoundException e) {
			// Guard: check permission before revealing that the model does not exist
			documentPermissionEvaluator.checkDocumentCreatePermissionByModel(documentModelName);
			// User has permission — wrap as serialization exception to distinguish from persistence-phase NotFoundException
			throw new DataServicesDocumentSerializationException(e.getCode(), e.getShortMessage().getKey(),
				e.getShortMessage().getDefaultMessage(), e);
		}
		return create(document, locale);
	}

	/**
	 * @event {@link DocumentBeforeCreateEvent}
	 * @event {@link DocumentAfterCreateEvent}
	 * @event {@link DocumentAfterLoadEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument copy(@NonNull DocumentReference documentReference, Locale locale) {
		DataServicesDocument sourceDocument = load(documentReference)
			.orElseThrow(() -> {
				// Guard: check permission before revealing that the document does not exist
				documentPermissionEvaluator.checkDocumentCreatePermissionByModel(documentReference.getDocumentModelName());
				// User has permission — safe to reveal "document not found"
				return new NotFoundException(DOCUMENT_NOT_FOUND_ERROR_KEY, "Document [%s] not found".formatted(documentReference));
			});
		return create(sourceDocument.getKernelDocument().withId(null), locale);
	}

	/**
	 * @event {@link DocumentBeforeUpdateEvent}
	 * @event {@link DocumentAfterUpdateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull DocumentV2 document, Locale locale) {
		return update(documentReference, document, locale, DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);
	}

	/**
	 * @event {@link DocumentBeforeUpdateEvent}
	 * @event {@link DocumentAfterUpdateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull DocumentV2 newDocument, Locale locale,
		@NonNull DocumentValidationStrategy validationStrategy, @NonNull DocumentComputationStrategy computationStrategy) {
		IDocumentRepository documentRepository = getDocumentRepository(newDocument);
		DataServicesDocument oldDocument = documentRepository.findByDocumentReference(documentReference)
			.orElseThrow(() -> {
				// Guard: check permission before revealing that the document does not exist
				documentPermissionEvaluator.checkDocumentUpdatePermissionByModel(documentReference.getDocumentModelName());
				// User has permission — safe to reveal "document not found"
				log.warn("Document [{}] not found", documentReference);
				return new NotFoundException(DOCUMENT_NOT_FOUND_ERROR_KEY, "Document [%s] not found".formatted(documentReference));
			});

		documentPermissionEvaluator.checkDocumentUpdatePermission(oldDocument.getKernelDocument(), newDocument, documentReference);
		return update(oldDocument, newDocument, documentRepository, locale, validationStrategy, computationStrategy);
	}

	/**
	 * @event {@link DocumentBeforeUpdateEvent}
	 * @event {@link DocumentAfterUpdateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull List<DocumentPart> documentParts, Locale locale) {
		return update(documentReference, documentParts, locale, DocumentValidationStrategy.DEFAULT_CONFIGURATION,
			DocumentComputationStrategy.DEFAULT_CONFIGURATION);
	}

	/**
	 * @event {@link DocumentBeforeUpdateEvent}
	 * @event {@link DocumentAfterUpdateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull List<DocumentPart> documentParts, Locale locale,
		@NonNull DocumentValidationStrategy validationStrategy, @NonNull DocumentComputationStrategy computationStrategy) {
		DataServicesDocument oldDocument = findByDocumentReference(documentReference)
			.orElseThrow(() -> {
				// Guard: check permission before revealing that the document does not exist
				documentPermissionEvaluator.checkDocumentPartialUpdatePermissionByModel(documentReference.getDocumentModelName());
				// User has permission — safe to reveal "document not found"
				return new NotFoundException("Partial modify document failed, document not found for [docRef = %s]".formatted(documentReference));
			});
		DocumentV2 kernelDocument = oldDocument.getKernelDocument();
		DocumentV2 updatedDocV2 = kernelDocument.withGroup(
			DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH, kernelDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)
		);

		IDocumentModel documentModel = documentModelLoader.loadModel(kernelDocument.getDocumentModelId());
		IDocumentModelSearchService modelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);

		Map<String, Boolean> isGroupPathMap = new HashMap();
		for (DocumentPart documentPart : documentParts) {
			if (isGroupPath(isGroupPathMap, modelSearchService, documentPart.getPath())) {
				validateForGroup(documentPart);
				updatedDocV2 = modifyDocumentWithGroup(updatedDocV2, documentPart, documentModel, modelSearchService);
			} else {
				validateForField(documentPart);
				updatedDocV2 = modifyDocumentWithField(updatedDocV2, documentPart);
			}
		}
		documentPermissionEvaluator.checkDocumentPartialUpdatePermission(oldDocument.getKernelDocument(), updatedDocV2, documentReference);
		return update(oldDocument, updatedDocV2, getDocumentRepository(kernelDocument), locale, validationStrategy, computationStrategy);
	}

	private DataServicesDocument update(DataServicesDocument oldDocument, DocumentV2 newDocument, IDocumentRepository documentRepository,
		Locale locale, DocumentValidationStrategy validationStrategy, DocumentComputationStrategy computationStrategy) {
		StopWatch stopWatch = StopWatch.createStarted();
		DocumentV2 updatedKernelDocument = metadataUtils.updateDocumentMetadata(
			oldDocument.getKernelDocument(),
			newDocument,
			UaaConnector.getCurrentUserName(),
			Instant.now()
		);
		DocumentV2 updateKernelDocument = computeAndValidateDocument(
			documentBeforeUpdate(oldDocument, updatedKernelDocument).getKernelDocument(),
			locale,
			validationStrategy,
			computationStrategy
		);
		DataServicesDocument updatedDocument = dataServicesDocumentFactory.newDataServicesDocument(updateKernelDocument);
		uniqueConstraintValidator.update(updateKernelDocument, updatedDocument.getMetadata().getDocRef(), locale);
		indexBehavior.ifPresent(behavior -> behavior.dropDocumentFields(oldDocument));
		documentRepository.update(updatedDocument);
		indexFields(updatedDocument);
		eventPublisher.publishEvent(new DocumentAfterRepositoryUpdateEvent(oldDocument, updatedDocument));
		syncAttachments(oldDocument, updatedDocument);

		eventPublisher.publishEvent(new DocumentAfterUpdateEvent(oldDocument.getMetadata().getDocRef(), oldDocument, updatedDocument));
		log.debug("Document [{}] has been updated in [{}] ms", oldDocument.getMetadata().getDocRef(), stopWatch.getDuration());
		return updatedDocument;
	}

	/**
	 * @event {@link DocumentBeforeDeleteEvent}
	 * @event {@link DocumentAfterDeleteEvent}
	 */
	@Override
	@Transactional
	public void delete(@NonNull DocumentReference documentReference) {
		StopWatch stopWatch = StopWatch.createStarted();
		modelPermissionEvaluator.checkModelReadPermission(documentReference.getDocumentModelName());
		Optional<DataServicesDocument> dataServicesDocumentOpt = findByDocumentReference(documentReference);

		dataServicesDocumentOpt.ifPresentOrElse(dataServicesDocument -> {
			documentPermissionEvaluator.checkDocumentDeletePermission(dataServicesDocument);
			documentBeforeDelete(dataServicesDocument);
			indexBehavior.ifPresent(behavior -> behavior.dropDocumentFields(dataServicesDocument));
			getDocumentRepository(dataServicesDocument.getKernelDocument()).delete(documentReference);
			uniqueConstraintValidator.deleteByDocRef(documentReference);

			eventPublisher.publishEvent(new DocumentAfterRepositoryDeleteEvent(dataServicesDocument));
			attachmentHandler.ifPresent(handler -> handler.deleteAttachmentsForDocument(dataServicesDocument.getKernelDocument(), documentReference));

			eventPublisher.publishEvent(new DocumentAfterDeleteEvent(dataServicesDocument));
			log.debug("Document [{}] has been deleted in [{}] ms", documentReference, stopWatch.getTime());
		}, () -> {
			// Guard: check permission before silently revealing that the document does not exist
			documentPermissionEvaluator.checkDocumentDeletePermissionByModel(documentReference.getDocumentModelName());
			// User has permission — no-op (same as original behavior when document not found)
		});
	}

	@Override
	@Transactional
	public void deleteAll(@NonNull Collection<DocumentReference> documentReferences) {
		StopWatch stopWatch = StopWatch.createStarted();
		if (documentReferences.size() > dataServicesCoreProperties.getDocuments().getMultiDelete().getLimit()) {
			throw new InvalidInputException(ExceptionCodes.HARD_LIMIT_EXCEEDED_EXCEPTION_CODE, ExceptionKeys.HARD_LIMIT_EXCEEDED_ERROR_KEY,
				"Number of documents to be deleted [%d] is higher than maximum allowed [%d]".formatted(
					documentReferences.size(), dataServicesCoreProperties.getDocuments().getMultiDelete().getLimit()));
		}

		checkModelReadAndDocumentMultiDeletePermission(documentReferences);

		eventPublisher.publishEvent(new DocumentsBeforeDeleteEvent(documentReferences));

		List<String> docRefStrings = documentReferences.stream().map(DocumentReference::toString).toList();
		if (indexBehavior.isPresent()) {
			documentFieldsJpaRepository.deleteDocumentFieldEntitiesByDocRefIn(docRefStrings);
			documentSearchJpaRepository.deleteDocumentSearchEntitiesByDocRefIn(docRefStrings);
		}
		documentReferences.stream()
			.collect(Collectors.groupingBy(DocumentReference::getDocumentModelName))
			.forEach((key, value) -> getDocumentRepository(key).deleteAll(key, value));
		documentReferences.forEach(uniqueConstraintValidator::deleteByDocRef);

		attachmentHandler.ifPresent(ah -> ah.deleteAttachmentsForDocuments(documentReferences));

		eventPublisher.publishEvent(new DocumentsAfterDeleteEvent(documentReferences));

		log.debug("Documents [{}] have been deleted in [{}] ms", docRefStrings,
			stopWatch.getTime(TimeUnit.MILLISECONDS));
	}

	/**
	 * @event {@link DocumentAfterLoadEvent}
	 */
	@Override
	public Optional<DataServicesDocument> load(@NonNull DocumentReference documentReference) {
		StopWatch stopWatch = StopWatch.createStarted();

		QueryPage<DocumentTreeResult> result = queryService.query(DocumentUtils.buildQueryLoadDocumentByDocRef(documentReference), null);

		Optional<DataServicesDocument> dataServicesDocument = result.getContent().stream()
			.findFirst()
			.map(DocumentTreeResult::getDocument)
			.map(JsonNode::toString)
			.map(json -> documentSupport.convertJSONToDocument(documentReference.getDocumentModelName(), new StringReader(json)))
			.map(dataServicesDocumentFactory::newDataServicesDocument);

		dataServicesDocument.ifPresent(d -> {
			eventPublisher.publishEvent(new DocumentAfterLoadEvent(documentReference, d.getKernelDocument()));
			log.debug("Document [{}] has been loaded in {} ms", documentReference, stopWatch.getTime());
		});
		return dataServicesDocument;
	}

	/**
	 * Method returns a paged list of associated document references for the input document model.
	 * Please note that this method is unsecure.
	 *
	 * @param modelId identifier of the model
	 * @param pageable the page to be retrieved
	 * @return list of document references
	 */
	public List<DocumentReference> loadForModel(String modelId, Pageable pageable) {
		StopWatch stopWatch = StopWatch.createStarted();
		List<DocumentReference> documentReferences = documentRepositories.stream()
			.map(dr -> dr.findAllDocRefsForModel(modelId, pageable))
			.flatMap(Collection::stream)
			.toList();
		log.debug("Document for model [{}] loaded in [{}] ms", modelId, stopWatch.getTime());
		return documentReferences;
	}

	/**
	 * Method to get a document by document reference
	 * Please note that method is unsecure.
	 *
	 * @param documentReference document reference
	 * @return optional with the document
	 */
	public Optional<DataServicesDocument> findByDocumentReference(DocumentReference documentReference) {
		StopWatch stopWatch = StopWatch.createStarted();
		Optional<DataServicesDocument> dataServicesDocument = documentRepositories.stream()
			.map(dr -> dr.findByDocumentReference(documentReference))
			.flatMap(Optional::stream)
			.findAny();

		log.debug("Document loaded for reference [{}] in [{}] ms", documentReference, stopWatch.getTime());
		return dataServicesDocument;
	}

	private void indexFields(DataServicesDocument documentToCreate) {
		indexBehavior.ifPresent(behavior -> {
			DataServicesDocument afterPublishedDocument = publishDocumentBeforeIndexEvent(documentToCreate);
			IDocumentModel documentModel = documentModelLoader.loadModel(afterPublishedDocument.getMetadata().getDocumentModelReference());
			IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
			behavior.saveDocumentFields(afterPublishedDocument, documentModelSearchService, this::getIdOfTheFieldType);
		});
	}

	private DataServicesDocument publishDocumentBeforeIndexEvent(DataServicesDocument toIndex) {
		DocumentBeforeIndexEvent documentsBeforeIndexEvent = new DocumentBeforeIndexEvent(toIndex);
		eventPublisher.publishEvent(documentsBeforeIndexEvent);
		return documentsBeforeIndexEvent.getDataServicesDocument();
	}

	private long getIdOfTheFieldType(String m, String f) {
		return modelFieldsJpaRepository.getByModelNameAndFieldName(m, f).getId();
	}

	private boolean isGroupPath(Map<String, Boolean> isGroupPathMap, IDocumentModelSearchService modelSearchService, String path) {
		if (!isGroupPathMap.containsKey(path)) {
			isGroupPathMap.put(path, isGroupPath(modelSearchService, path));
		}
		return isGroupPathMap.get(path);

	}

	/**
	 * Determines whether the given part path addresses a group (rather than a field) in the document model.
	 * Returns `false` when no model search service is available, preserving the field-modification fallback.
	 */
	private static boolean isGroupPath(IDocumentModelSearchService modelSearchService, String path) {
		try {
			return modelSearchService != null
				&& modelSearchService.getByPath(path)
				.filter(IGroup.class::isInstance)
				.isPresent();
		} catch (Exception e) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Invalid documentPart for partial modify document",
				e
			);
		}
	}

	/**
	 * For fields, no wildcards are allowed in `DocumentPart.getRepetitions()`.
	 */
	private void validateForField(DocumentPart part) {
		// No wildcards are allowed for field operations
		if (KernelUtils.hasAnyWildcard(part.getRepetitions())) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Wildcard repetition index is not supported for a field at path: " + part.getPath()
			);
		}
	}

	/**
	 * For groups, in `DocumentPart.getRepetitions()`
	 * - no intermediate wildcard are allowed
	 * - a wildcard at the end is allowed only if `DocumentPart.getValue()` is not equal to `null`.
	 */

	private void validateForGroup(DocumentPart part) {
		int[] repetitions = part.getRepetitions();

		// For groups, wildcard `0` is allowed only at the end of the repetitions array
		if (KernelUtils.hasIntermediateWildcard(repetitions)) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Intermediate wildcard repetition index is not supported"
			);
		}

		if (part.getValue() == null) {
			// Removal by wildcard is not allowed
			if (KernelUtils.isLastRepetitionWildcard(repetitions)) {
				throw new InvalidInputException(
					ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
					"Cannot append a null value: wildcard repetition requires a non-null group value"
				);
			}
		}

	}

	/**
	 * Modifies the document when the part path addresses a group: replaces or inserts the group for concrete
	 * repetitions, or appends a new repetition when the trailing repetition index is the wildcard `0`.
	 * A group accepts a wildcard only at the last repetition index.
	 */
	private DocumentV2 modifyDocumentWithGroup(DocumentV2 document, DocumentPart part, IDocumentModel documentModel,
		IDocumentModelSearchService modelSearchService) {
		try {
			if (part.getValue() == null) {
				// Remove the group from the document
				return removeGroupFromDocument(document, part);
			}
			int[] repetitions = part.getRepetitions();
			GroupValueConverter groupValueConverter = new GroupValueConverter(documentV2Serializer, documentModelLoader);
			if (KernelUtils.isLastRepetitionWildcard(repetitions)) {
				// We want to append to a repeatable group
				boolean repeatable = modelSearchService.getByPath(part.getPath())
					.filter(IGroup.class::isInstance)
					.map(IGroup.class::cast)
					.map(g -> g.getRepeatability() > 1)
					.orElse(false);
				if (!repeatable) {
					throw new InvalidInputException(
						ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
						"Cannot append to a non-repeatable group at path: " + part.getPath()
					);
				}
				// Append the group at the end of a repeatable group list
				GroupInstanceV2 groupInstance = groupValueConverter.toGroupInstance(documentModel, part.getPath(), part.getValue());
				DocumentPointer pointer = KernelUtils.pointerPreservingWildcard(part.getPath(), repetitions);
				return document.withGroupRepetitionAppended(pointer, groupInstance);
			} else {
				// Update an existing group or add a new one if it does not exist
				GroupInstanceV2 groupInstance = groupValueConverter.toGroupInstance(documentModel, part.getPath(), part.getValue());
				DocumentPointer pointer = KernelUtils.fromPathAndRepetitions(part.getPath(),repetitions != null ? repetitions : new int[0]);
				return document.withGroup(pointer, groupInstance);
			}
		} catch (InvalidInputException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Invalid documentPart for partial modify document",
				e
			);
		}
	}

	/**
	 * Modifies the document when the part path addresses a field: sets the field value, or removes it when the
	 * value is null. A field does not accept any wildcard repetition index.
	 */
	private DocumentV2 modifyDocumentWithField(DocumentV2 document, DocumentPart part) {
		try {
			if (part.getValue() == null) {
				// value null means deletion
				return removeFieldFromDocument(document, part);
			} else {
				// update the field value or add the field if it does not exist
				return document.withFieldValue(
					KernelUtils.fromPathAndRepetitions(part.getPath(), part.getRepetitions()),
					documentUtils.transformToV2Value(document.getDocumentModelId(), part.getPath(), part.getValue())
				);
			}
		} catch (InvalidInputException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidInputException(
				ExceptionKeys.MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY,
				"Invalid documentPart for partial modify document",
				e
			);
		}
	}

	/**
	 * Removes the group addressed by the part from the document, returning the document unchanged when no group
	 * is present at that pointer.
	 */
	private static DocumentV2 removeGroupFromDocument(DocumentV2 document, DocumentPart part) {
		DocumentPointer pointer = KernelUtils.fromPathAndRepetitions(part.getPath(), part.getRepetitions());
		if (document.group(pointer) != null) {
			return document.withGroupRemoved(pointer);
		}
		return document;
	}

	/**
	 * Removes the field addressed by the part from the document by setting its value to null, returning the
	 * document unchanged when no field is present at that pointer.
	 */
	private static DocumentV2 removeFieldFromDocument(DocumentV2 document, DocumentPart part) {
		DocumentPointer pointer = KernelUtils.fromPathAndRepetitions(part.getPath(), part.getRepetitions());
		if (document.field(pointer) != null) {
			return document.withFieldValue(pointer, null);
		}
		return document;
	}

	private void documentBeforeDelete(DataServicesDocument deletedDocument) {
		DocumentReference documentReference = deletedDocument.getMetadata().getDocRef();
		eventPublisher.publishEvent(new DocumentBeforeDeleteEvent(documentReference, deletedDocument.getKernelDocument()));
	}

	private DataServicesDocument documentBeforeCreate(DocumentV2 document) {
		DocumentBeforeCreateEvent documentBeforeCreateEvent = new DocumentBeforeCreateEvent(document);
		eventPublisher.publishEvent(documentBeforeCreateEvent);
		return dataServicesDocumentFactory.newDataServicesDocument(documentBeforeCreateEvent.getCreatedDocument());
	}

	private DataServicesDocument documentBeforeUpdate(DataServicesDocument existingDocument, DocumentV2 updatedDocument) {

		DocumentBeforeUpdateEvent documentBeforeUpdateEvent =
			new DocumentBeforeUpdateEvent(existingDocument.getMetadata().getDocRef(), updatedDocument, existingDocument.getKernelDocument());
		eventPublisher.publishEvent(documentBeforeUpdateEvent);

		return dataServicesDocumentFactory.newDataServicesDocument(documentBeforeUpdateEvent.getUpdatedDocument());
	}

	private DocumentV2 computeAndValidateDocument(DocumentV2 document, Locale locale, DocumentValidationStrategy validationStrategy,
		DocumentComputationStrategy computationStrategy) {
		String modelName = document.getDocumentModelId();
		Header header = modelHeaderJpaRepository.findById(document.getDocumentModelId())
			.orElseThrow(() -> new NotFoundException(MODEL_NOT_FOUND_ERROR_KEY, "Document model [%s] not found".formatted(modelName)));
		modelPermissionEvaluator.checkModelReadPermission(header);

		if (documentModelUtils.isAbstract(header)) {
			log.warn("Document creation failed as associated document model [{}] is defined as abstract", header);
			throw new InvalidInputException(DOCUMENT_ABSTRACT_MODEL_ERROR_KEY, "Document model [%s] is defined as Abstract".formatted(header.getId()));
		}

		document = switch (computationStrategy) {
			case FULL_COMPUTATION -> kernelDocumentService.compute(document, locale);
			case NO_COMPUTATION -> document;
			case DEFAULT_CONFIGURATION -> kernelDocumentService.computeDocument(document, locale);
		};

		Optional<IDocumentValidationResult> documentValidationResult = switch (validationStrategy) {
			case FULL_VALIDATION -> Optional.ofNullable(kernelDocumentService.validateFull(document, locale));
			case PARTIAL_VALIDATION -> Optional.ofNullable(kernelDocumentService.validatePartially(document, locale));
			case NO_VALIDATION -> Optional.empty();
			case DEFAULT_CONFIGURATION -> kernelDocumentService.validateDocument(document, locale);
		};
		documentValidationResult.ifPresent(validationResult -> {
			if (!validationResult.noErrorOccurred()) {
				throw new DocumentValidationException(DocumentValidationResultMapper.toDocumentValidationResults(validationResult),
					"Document is not valid:%n%s".formatted(validationResult.getMessages())).withAnonymityMessage("Validation of document failed.");
			}
		});
		return document;
	}

	private void syncAttachments(DataServicesDocument oldDocument, DataServicesDocument dataServicesDocument) {
		attachmentHandler.ifPresent(handler ->
			handler.synchronizeAttachments(
				attachmentSupport.map(support -> support.collectAttachmentIDs(dataServicesDocument.getKernelDocument()))
					.orElse(Collections.emptyList()),
				Optional.ofNullable(oldDocument)
					.map(DataServicesDocument::getKernelDocument)
					.map(attachmentSupport.get()::collectAttachmentIDs)
					.orElse(Collections.emptyList()),
				dataServicesDocument.getMetadata().getDocRef())
		);
	}

	private void checkModelReadAndDocumentMultiDeletePermission(Collection<DocumentReference> documentReferences) {
		Collection<String> modelNames = documentReferences.stream().map(DocumentReference::getDocumentModelName).collect(Collectors.toSet());
		List<Header> modelHeaders = modelHeaderRepository.findAllByIdIn(modelNames);
		modelHeaders.forEach(modelPermissionEvaluator::checkModelReadPermission);

		documentPermissionEvaluator.checkDocumentMultiDeletePermission(modelHeaders);
	}

	private IDocumentRepository getDocumentRepository(DocumentV2 document) {
		return documentRepositories.stream()
			.filter(repository -> repository.supports(document))
			.findFirst()
		.orElseThrow(() -> {
			log.error(REPOSITORY_NOT_FOUND.formatted(document.getDocumentModelId()));
			return new NotFoundException(ExceptionKeys.NO_MODEL_REPOSITORY_FOUND, REPOSITORY_NOT_FOUND.formatted(document.getDocumentModelId()));
		});
	}

	private IDocumentRepository getDocumentRepository(String modelId) {
		return documentRepositories.stream()
			.filter(repository -> repository.supports(modelId, Optional.empty()))
			.findFirst()
		.orElseThrow(() -> {
			log.error(REPOSITORY_NOT_FOUND.formatted(modelId));
			return new NotFoundException(ExceptionKeys.NO_MODEL_REPOSITORY_FOUND, REPOSITORY_NOT_FOUND.formatted(modelId));
		});
	}
}
