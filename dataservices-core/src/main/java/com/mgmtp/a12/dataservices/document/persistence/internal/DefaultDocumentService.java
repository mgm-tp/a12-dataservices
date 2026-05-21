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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.mgmtp.a12.dataservices.utils.internal.KernelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	private final DocumentSearchIndexBehaviour indexBehavior;
	private final ModelFieldsJpaRepository modelFieldsJpaRepository;
	private final DocumentSearchJpaRepository documentSearchJpaRepository;
	private final QueryService queryService;
	private final DocumentSupport documentSupport;

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
		documentRepository.create(updatedDataServiceDocument);
		indexFields(updatedDataServiceDocument);
		eventPublisher.publishEvent(new DocumentAfterRepositoryCreateEvent(updatedDataServiceDocument));
		syncAttachments(null, updatedDataServiceDocument);

		eventPublisher.publishEvent(new DocumentAfterCreateEvent(updatedDataServiceDocument));
		log.debug("Document [{}] has been created in [{}] ms", updatedDataServiceDocument.getMetadata().getDocRef(), stopWatch.getDuration());
		return updatedDataServiceDocument;
	}

	/**
	 * @event {@link DocumentBeforeUpdateEvent}
	 * @event {@link DocumentAfterUpdateEvent}
	 */
	@Override
	@Transactional
	public DataServicesDocument update(@NonNull DocumentReference documentReference, @NonNull DocumentV2 document, Locale locale) {
		return update(documentReference, document, locale,DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);
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
				log.warn("Document [{}] not found", documentReference);
				return new NotFoundException(DOCUMENT_NOT_FOUND_ERROR_KEY, String.format("Document [%s] not found", documentReference));
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
		return update(documentReference, documentParts, locale, DocumentValidationStrategy.DEFAULT_CONFIGURATION, DocumentComputationStrategy.DEFAULT_CONFIGURATION);
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
			.orElseThrow(() -> new NotFoundException(String.format("Partial modify document failed, document not found for [docRef = %s]", documentReference)));
		DocumentV2 kernelDocument = oldDocument.getKernelDocument();
		DocumentV2 updatedDocV2 = kernelDocument.withGroup(
			DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH, kernelDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)
		);

		for (DocumentPart documentPart : documentParts) {
			updatedDocV2 = modifyDocument(updatedDocV2, documentPart);
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
		DataServicesDocument updatedDocument = dataServicesDocumentFactory.newDataServicesDocument(
			computeAndValidateDocument(
				documentBeforeUpdate(oldDocument, updatedKernelDocument).getKernelDocument(),
				locale,
				validationStrategy,
				computationStrategy
			)
		);

		indexBehavior.dropDocumentFields(oldDocument);
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

		dataServicesDocumentOpt.ifPresent(dataServicesDocument -> {
			documentPermissionEvaluator.checkDocumentDeletePermission(dataServicesDocument);
			documentBeforeDelete(dataServicesDocument);
			indexBehavior.dropDocumentFields(dataServicesDocument);
			getDocumentRepository(dataServicesDocument.getKernelDocument()).delete(documentReference);

			eventPublisher.publishEvent(new DocumentAfterRepositoryDeleteEvent(dataServicesDocument));
			attachmentHandler.ifPresent(handler -> handler.deleteAttachmentsForDocument(dataServicesDocument.getKernelDocument(), documentReference));

			eventPublisher.publishEvent(new DocumentAfterDeleteEvent(dataServicesDocument));
			log.debug("Document [{}] has been deleted in [{}] ms", documentReference, stopWatch.getTime());
		});
	}

	@Override
	@Transactional public void deleteAll(@NonNull Collection<DocumentReference> documentReferences) {
		StopWatch stopWatch = StopWatch.createStarted();
		if (documentReferences.size() > dataServicesCoreProperties.getDocuments().getMultiDelete().getLimit()) {
			throw new InvalidInputException(ExceptionCodes.HARD_LIMIT_EXCEEDED_EXCEPTION_CODE, ExceptionKeys.HARD_LIMIT_EXCEEDED_ERROR_KEY,
				String.format("Number of documents to be deleted is higher than maximum allowed one %s",
					dataServicesCoreProperties.getDocuments().getMultiDelete().getLimit()));
		}

		checkModelReadAndDocumentMultiDeletePermission(documentReferences);

		eventPublisher.publishEvent(new DocumentsBeforeDeleteEvent(documentReferences));

		List<String> docRefStrings = documentReferences.stream().map(DocumentReference::toString).toList();
		documentFieldsJpaRepository.deleteDocumentFieldEntitiesByDocRefIn(docRefStrings);
		documentSearchJpaRepository.deleteDocumentSearchEntitiesByDocRefIn(docRefStrings);
		documentReferences.stream()
			.collect(Collectors.groupingBy(DocumentReference::getDocumentModelName))
			.forEach((key, value) -> getDocumentRepository(key).deleteAll(key, value));

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
		DataServicesDocument afterPublishedDocument = publishDocumentBeforeIndexEvent(documentToCreate);
		IDocumentModel documentModel = documentModelLoader.loadModel(afterPublishedDocument.getMetadata().getDocumentModelReference());
		IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
		indexBehavior.saveDocumentFields(afterPublishedDocument, documentModelSearchService, this::getIdOfTheFieldType);
	}

	private DataServicesDocument publishDocumentBeforeIndexEvent(DataServicesDocument toIndex) {
		DocumentBeforeIndexEvent documentsBeforeIndexEvent = new DocumentBeforeIndexEvent(toIndex);
		eventPublisher.publishEvent(documentsBeforeIndexEvent);
		return documentsBeforeIndexEvent.getDataServicesDocument();
	}

	private long getIdOfTheFieldType(String m, String f) {
		return modelFieldsJpaRepository.getByModelNameAndFieldName(m, f).getId();
	}

	private DocumentV2 modifyDocument(DocumentV2 document, DocumentPart part) {
		try {
			if (part.getValue() == null) {
				return implyNullValue(document, part);
			} else {
				return document.withFieldValue(
					KernelUtils.fromPathAndRepetitions(part.getPath(), part.getRepetitions()),
					documentUtils.transformToV2Value(document.getDocumentModelId(), part.getPath(), part.getValue())
				);
			}
		} catch (Exception e) {
			throw new InvalidInputException("Invalid documentPart for partial modify document", e);
		}
	}

	private static DocumentV2 implyNullValue(DocumentV2 document, DocumentPart part) {
		DocumentPointer dp = KernelUtils.fromPathAndRepetitions(part.getPath(), part.getRepetitions());
		GroupInstanceV2 groupInstanceV2 = document.group(dp);
		if (groupInstanceV2 != null) {
			return document.withGroupRemoved(dp);
		} else if (document.field(dp) != null) {
			return document.withFieldValue(dp, null);
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
			.orElseThrow(() -> new NotFoundException(MODEL_NOT_FOUND_ERROR_KEY, String.format("Document model [%s] not found", modelName)));
		modelPermissionEvaluator.checkModelReadPermission(header);
		if (documentModelUtils.isAbstract(header)) {
			log.warn("Document creation failed as associated document model [{}] is defined as abstract", header);
			throw new InvalidInputException(DOCUMENT_ABSTRACT_MODEL_ERROR_KEY, String.format("Document model [%s] is defined as Abstract", header.getId()));
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
					String.format("Document is not valid:%n%s", validationResult.getMessages())).withAnonymityMessage("Validation of document failed.");
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
				log.error(String.format(REPOSITORY_NOT_FOUND, document.getDocumentModelId()));
				return new NotFoundException(String.format(REPOSITORY_NOT_FOUND, document.getDocumentModelId()));
			});
	}

	private IDocumentRepository getDocumentRepository(String modelId) {
		return documentRepositories.stream()
			.filter(repository -> repository.supports(modelId, Optional.empty()))
			.findFirst()
			.orElseThrow(() -> {
				log.error(String.format(REPOSITORY_NOT_FOUND, modelId));
				return new NotFoundException(String.format(REPOSITORY_NOT_FOUND, modelId));
			});
	}
}
