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

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentMapper;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent;
import com.mgmtp.a12.dataservices.document.exception.DataServicesDocumentSerializationException;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentFactory;
import com.mgmtp.a12.dataservices.document.internal.entity.DocumentEntity;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default services repository implementation
 */
@DocumentationDiagram
@Order
@RequiredArgsConstructor
@Slf4j
@Repository("defaultDocumentRepository") public class DefaultDocumentRepository implements IDocumentRepository {

	private final DocumentJpaRepository documentJpaRepository;
	private final DocumentSupport documentSupport;
	private final ApplicationEventPublisher eventPublisher;
	private final DocumentMapper documentMapper;
	private final DefaultDataServicesDocumentFactory dataServicesDocumentFactory;

	/**
	 * @event {@link DocumentBeforeRepositorySaveEvent}
	 */
	@Override public void create(@NonNull DataServicesDocument dataServicesDocument) {
		StopWatch stopWatch = StopWatch.createStarted();
		String documentContent = beforeDocumentSave(dataServicesDocument);

		DocumentEntity documentEntity = documentMapper.toDocumentEntity(dataServicesDocument, documentContent);
		documentJpaRepository.save(documentEntity);
		log.trace("Document [{}] has been stored in database in [{}] ms", dataServicesDocument.getMetadata().getDocRef(), stopWatch.getTime());
	}

	/**
	 * @event {@link DocumentBeforeRepositorySaveEvent}
	 */
	@Override public void update(@NonNull DataServicesDocument dataServicesDocument) {
		StopWatch stopWatch = StopWatch.createStarted();
		DocumentEntity documentEntity = findDocumentForUpdate(dataServicesDocument);
		documentEntity.setContent(beforeDocumentSave(dataServicesDocument));
		documentJpaRepository.save(documentEntity);
		log.trace("Document [{}] has been updated in database in [{}] ms", dataServicesDocument.getMetadata().getDocRef(), stopWatch.getTime());
	}

	@Override public void delete(@NonNull DocumentReference documentReference) {
		StopWatch stopWatch = StopWatch.createStarted();
		documentJpaRepository.deleteByModelNameAndId(documentReference.getDocumentModelName(), documentReference.getDocumentId());
		log.trace("Document [{}] has been deleted from database in [{}] ms", documentReference, stopWatch.getTime());
	}

	@Override public void deleteAll(@NonNull String modelName, @NonNull Collection<DocumentReference> documentReferences) {
		documentJpaRepository.deleteAllByModelNameAndIdIn(modelName, documentReferences.stream().map(DocumentReference::getDocumentId).toList());
	}

	/**
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	@Override public Optional<DataServicesDocument> findByDocumentReference(@NonNull DocumentReference documentReference) {
		StopWatch stopWatch = StopWatch.createStarted();
		Optional<DataServicesDocument> document = loadDocumentEntity(documentReference).map(this::documentAfterRepositoryLoad);
		log.trace("Document [{}] has been loaded from database in [{}] ms", documentReference, stopWatch.getTime());
		return document;
	}

	@Override public @NonNull List<DocumentReference> findAllDocRefsForModel(@NonNull String modelId) {
		StopWatch stopWatch = StopWatch.createStarted();
		List<DocumentReference> documentReferences = documentJpaRepository.findIdByModelName(modelId).stream()
			.map(id -> new DocumentReference(modelId, id))
			.toList();
		log.trace("All document references of model [{}] have been loaded in [{}] ms", modelId, stopWatch.getTime());
		return documentReferences;
	}

	@Override public @NonNull List<DocumentReference> findAllDocRefsForModel(@NonNull String modelId, Pageable pageable) {
		return documentJpaRepository.findIdByModelName(modelId, pageable).stream()
			.map(id -> new DocumentReference(modelId, id))
			.toList();
	}

	/**
	 * @event {@link DocumentAfterRepositoryLoadEvent}
	 */
	@Override public @NonNull List<DataServicesDocument> findDocumentsByDocRefs(@NonNull List<DocumentReference> docRefs) {
		return documentJpaRepository.findByIdInOrderById(getDocumentIds(docRefs)).stream()
			.map(this::detachEntity)
			.map(entity -> {
				try {
					return documentAfterRepositoryLoad(entity);
				} catch (DataServicesDocumentSerializationException documentSerializationException) {
					/**
					 * DS skips documents that can not be deserialized. The error message must be logged to inform about the problem. Stacktrace is not logged
					 * as it will pollute the log files with every index rebuild. The important information about the exception and the docRef of a problematic document
					 * is in the exception message.
					 *
					 * All other exceptions must be propagated as they indicate a more serious problem (e.g. database connection issue).
					 */
					log.error(documentSerializationException.getMessage());
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();
	}

	@Override public boolean supports(@NonNull DocumentV2 document) {
		return true;
	}

	@Override public boolean supports(@NonNull String modelName, @NonNull Optional<String> metadata) {
		return true;
	}

	private static @NonNull List<String> getDocumentIds(@NonNull List<DocumentReference> docRefs) {
		return docRefs.stream()
			.map(DocumentReference::getDocumentId)
			.toList();
	}

	private DataServicesDocument documentAfterRepositoryLoad(DocumentEntity persistedEntity) {
		DocumentAfterRepositoryLoadEvent repositoryLoadEvent = new DocumentAfterRepositoryLoadEvent(persistedEntity.getDocRef(), persistedEntity.getContent());
		eventPublisher.publishEvent(repositoryLoadEvent);
		persistedEntity.setContent(repositoryLoadEvent.getDocumentContent());

		return dataServicesDocumentFactory.newDataServicesDocument(documentMapper.convertToDocumentContent(persistedEntity));
	}

	private Optional<DocumentEntity> loadDocumentEntity(DocumentReference documentReference) {
		return documentJpaRepository.findById(documentReference.getDocumentId())
			.filter(entity -> Objects.equals(entity.getModelName(), documentReference.getDocumentModelName()))
			.map(this::detachEntity);
	}

	private DocumentEntity detachEntity(DocumentEntity entity) {
		// We can not use `EntityManager.detach(entity), because it causes "possible non-threadsafe access to session" exception.
		// Looks like EM requires flushing before which is not desired here.
		DocumentEntity detachedEntity = new DocumentEntity();
		BeanUtils.copyProperties(entity, detachedEntity);
		return detachedEntity;
	}

	private String convertDocumentToJson(DataServicesDocument dataServicesDocument) {
		try (StringWriter documentContentWriter = new StringWriter()) {
			documentSupport.convertDocumentToJSON(dataServicesDocument.getKernelDocument(), documentContentWriter);
			return documentContentWriter.toString();
		} catch (Exception e) {
			throw new UnexpectedException(e).withAnonymityMessage("Conversion of document failed.");
		}
	}

	private String beforeDocumentSave(DataServicesDocument dataServicesDocument) {
		DocumentBeforeRepositorySaveEvent documentBeforeRepositorySaveEvent =
			new DocumentBeforeRepositorySaveEvent(dataServicesDocument.getKernelDocument().getDocumentModelId(),
				convertDocumentToJson(dataServicesDocument));
		eventPublisher.publishEvent(documentBeforeRepositorySaveEvent);
		return documentBeforeRepositorySaveEvent.getDocumentContent();
	}

	private DocumentEntity findDocumentForUpdate(DataServicesDocument dataServicesDocument) {
		return documentJpaRepository.findById(dataServicesDocument.getMetadata().getDocRef().getDocumentId())
			.orElseThrow(() -> new NotFoundException(String.format("Document [%s] not found", dataServicesDocument.getMetadata().getDocRef())));
	}

}
