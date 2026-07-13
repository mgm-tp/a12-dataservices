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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.io.StringReader;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.OffsetBasedPageRequest;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkSpecification;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterDeleteEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterUpdateEvent;
import com.mgmtp.a12.dataservices.relationship.factory.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipValidationException;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultRelationshipLinkService implements RelationshipLinkService {

	private final RelationshipLinkRepository repository;
	private final RelationshipLinkFactory relationshipLinkFactory;
	private final RelationshipModelLoader modelLoader;
	private final DocumentSupport documentSupport;
	private final DataServicesCoreProperties properties;
	private final ApplicationEventPublisher eventPublisher;
	private final DocumentService documentService;

	@Transactional
	@Override public RelationshipLink create(@NonNull LinkDescriptor linkDescriptor) {
		RelationshipLink relationshipLink = relationshipLinkFactory.createLink(linkDescriptor, null);
		return createInRepository(relationshipLink);
	}

	@Transactional
	@Override public RelationshipLink create(@NonNull LinkDescriptor linkDescriptor, @NonNull String linkDocument) {
		RelationshipModel relationshipModel = modelLoader.loadModel(linkDescriptor.getRelationshipModel());
		String linkDocumentModelName = relationshipModel.getContent().getLinkDocumentModel();
		DocumentReference docRef = createLinkDocument(linkDocumentModelName, linkDocument);
		if (Objects.equals(linkDocumentModelName, docRef.getDocumentModelName())) {
			RelationshipLink newLink = relationshipLinkFactory.createLink(linkDescriptor, docRef);
			return createInRepository(newLink);
		} else {
			throw new AssertionError("Link document model mismatch for relationship %s and document %s. Expected model is %s.".formatted(
				relationshipModel.getHeader().getId(), docRef, linkDocumentModelName));
		}
	}

	@Transactional
	@Override public RelationshipLink create(@NonNull LinkDescriptor linkDescriptor, @NonNull DocumentReference linkDocRef) {
		RelationshipLink relationshipLink = relationshipLinkFactory.createLink(linkDescriptor, linkDocRef);
		return createInRepository(relationshipLink);
	}

	@Transactional
	@Override public RelationshipLink update(@NonNull String id, @NonNull LinkDescriptor linkDescriptor, String linkDocument) {
		String relationshipModelName = linkDescriptor.getRelationshipModel();
		RelationshipModel relationshipModel = modelLoader.loadModel(relationshipModelName);

		RelationshipLink relationshipLink = load(id);
		DocumentReference originalLinkDocumentDocRef = relationshipLink.getLinkDocumentDocRef();

		RelationshipValidationSupport.validateLinkCharacteristic(linkDescriptor);
		RelationshipValidationSupport.validateLink(relationshipLink, linkDescriptor);
		RelationshipValidationSupport.validateLinkDocument(linkDocument, relationshipModel);

		DocumentReference newLinkDocRef = (Objects.isNull(linkDocument) || linkDocument.isBlank()) ?
			null :
			createLinkDocument(relationshipModel.getContent().getLinkDocumentModel(), linkDocument);

		relationshipLink.setLinkDocumentDocRef(newLinkDocRef);

		if (Objects.nonNull(originalLinkDocumentDocRef)) {
			documentService.delete(originalLinkDocumentDocRef);
		}

		RelationshipLink updatedLink = repository.update(id, relationshipLink);

		eventPublisher.publishEvent(new RelationshipLinkAfterUpdateEvent(relationshipModel, relationshipLink));

		log.debug("Relationship Link has been modified for relationship [{}] between documents {}", relationshipLink.getRelationshipModel(),
			updatedLink.getRoles().values().stream().map(RelationshipRole::getDocRef).toArray());

		return updatedLink;
	}

	@Transactional
	@Override public RelationshipLink relink(@NonNull LinkDescriptor linkDescriptor, @NonNull String linkId) {
		RelationshipModel targetRelationshipModel = modelLoader.loadModel(linkDescriptor.getRelationshipModel());
		RelationshipLink originalLink = load(linkId);

		boolean modelMatches = targetRelationshipModel.getHeader().getId().equals(originalLink.getRelationshipModel());

		if (!modelMatches) {
			throw new RelationshipValidationException(
				new RelationshipValidationException.Builder(
					ExceptionCodes.RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_EXCEPTION_CODE,
					ExceptionKeys.RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_ERROR_KEY,
					"Relink outside of relationship model is not allowed",
					"Relink outside of relationship model is not allowed"
				)
			);
		}

		RelationshipValidationSupport.validateLinkCharacteristic(linkDescriptor);
		RelationshipLink relationshipLink = relationshipLinkFactory.createLink(linkDescriptor, originalLink.getLinkDocumentDocRef());
		RelationshipLink newLink = createInRepository(relationshipLink);

		originalLink.setLinkDocumentDocRef(null);
		delete(originalLink.getId());
		return newLink;
	}

	@Transactional
	@Override public void delete(@NonNull String id) {
		try {
			// We load it first to check authorization.
			RelationshipLink linkToDelete = load(id);
			repository.delete(linkToDelete.getId());

			if (Objects.nonNull(linkToDelete.getLinkDocumentDocRef())) {
				documentService.delete(linkToDelete.getLinkDocumentDocRef());
			}

			RelationshipModel relationshipModel = modelLoader.loadModel(linkToDelete.getRelationshipModel());

			eventPublisher.publishEvent(new RelationshipLinkAfterDeleteEvent(relationshipModel, linkToDelete));
			log.debug("Relationship Link has been deleted for relationship [{}] between documents {}",
				relationshipModel.getHeader().getId(), linkToDelete.getRoles().values().stream().map(RelationshipRole::getDocRef).toArray());

		} catch (NotFoundException ignored) {
			// NO-OP
		}
	}

	@Transactional
	@Override public void deleteAllByIds(@NonNull Set<String> ids) {
		ids.forEach(this::delete);
	}

	@Override public RelationshipLink load(@NonNull String id) throws NotFoundException {
		return repository.findById(id)
			.map(relationshipLink -> {
				// load model for authorization
				modelLoader.loadModel(relationshipLink.getRelationshipModel());
				return relationshipLink;
			})
			.orElseThrow(
				() -> new NotFoundException(ExceptionKeys.RELATIONSHIP_LINK_NOT_FOUND_ERROR_KEY, "Relationship link [%s] not found".formatted(id)));
	}

	@Override public Page<? extends RelationshipLink> load(@NonNull RelationshipLinkSpecification specification, Pageable pageable) {
		RelationshipModel model = modelLoader.loadModel(specification.getRelationshipModelName());
		Pageable newPage = enforcePage(pageable, properties.getQuery().getMaxLinksSize());

		return listLinks(specification, model, newPage);
	}


	@Transactional
	@Override public void deleteByRoleDocRefs(Collection<DocumentReference> documentReferences) {
		deleteAllByIds(
			repository.findAllByRoleDocRef(documentReferences, OffsetBasedPageRequest.unpaged())
				.getContent()
				.stream()
				.map(RelationshipLink::getId)
				.collect(Collectors.toSet())
		);
	}

	public long countByLinkInDocumentDocRefs(Collection<DocumentReference> documentReferences) {
		return repository.countByLinkInDocumentDocRefs(documentReferences);
	}

	public long countByRoles(String relationshipModelName, RelationshipRole sourceRole, RelationshipRole targetRole) {
		return repository.countByRoles(relationshipModelName, sourceRole, targetRole);
	}

	public long countByRole(@NonNull String relationshipModelName, @NonNull String role, @NonNull DocumentReference docRef) {
		return repository.countByRole(relationshipModelName, role, docRef);
	}

	public long countByRelationshipModel(String relationshipModelName) {
		return repository.countByRelationshipModel(relationshipModelName);
	}

	private DocumentReference createLinkDocument(String linkDocumentModel, String linkDocumentContent) {
		DocumentV2 linkDoc = documentSupport.convertJSONToDocument(linkDocumentModel, new StringReader(linkDocumentContent));
		return documentService.create(linkDoc, null).getMetadata().getDocRef();
	}

	private static Pageable enforcePage(Pageable pageable, Integer maxResultsPerPage) {
		return pageable.getPageSize() > maxResultsPerPage ? PageRequest.of(pageable.getPageNumber(), maxResultsPerPage, pageable.getSort()) : pageable;
	}

	private void checkRoleNameExistInModel(String roleName, RelationshipModel relationshipModel) {
		relationshipModel.getEntityCharacteristicsFromRole(roleName);
	}

	private RelationshipLink createInRepository(RelationshipLink relationshipLink) {
		RelationshipLink createdLink = repository.create(relationshipLink);
		RelationshipModel relationshipModel = modelLoader.loadModel(relationshipLink.getRelationshipModel());
		eventPublisher.publishEvent(new RelationshipLinkAfterCreateEvent(relationshipModel, createdLink));
		log.debug("Relationship Link has been created for relationship [{}] between documents {}", relationshipModel.getHeader().getId(),
			relationshipLink.getRoles().values().stream().map(RelationshipRole::getDocRef).toArray());
		return createdLink;
	}

	private Page<? extends RelationshipLink> listLinks(@NotNull RelationshipLinkSpecification specification, RelationshipModel model,
		Pageable page) {
		if (Objects.isNull(specification.getTargetFilter()) && Objects.isNull(specification.getSourceFilter())) {
			return repository.findByRelationshipModelName(specification.getRelationshipModelName(), page);
		}

		checkRoleNameExistInModel(specification.getSourceFilter().name(), model);
		if (Objects.isNull(specification.getTargetFilter())) {
			return repository.findByRelationshipModelNameAndSource(
				specification.getRelationshipModelName(),
				specification.getSourceFilter().name(),
				specification.getSourceFilter().docRef(),
				page
			);
		}

		checkRoleNameExistInModel(specification.getTargetFilter().name(), model);
		return repository.findByRelationshipModelNameAndSourceAndTarget(
			specification.getRelationshipModelName(),
			specification.getSourceFilter().name(),
			specification.getSourceFilter().docRef(),
			specification.getTargetFilter().name(),
			specification.getTargetFilter().docRef(),
			page
		);
	}
}
