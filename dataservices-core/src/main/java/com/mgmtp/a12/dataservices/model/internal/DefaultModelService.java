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
package com.mgmtp.a12.dataservices.model.internal;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterLoadEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterLoadEvent;
import com.mgmtp.a12.dataservices.model.exception.SerializationException;
import com.mgmtp.a12.dataservices.model.persistence.GenericModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static java.util.stream.Collectors.toList;

@DocumentationDiagram
@Slf4j
@RequiredArgsConstructor
@Service public class DefaultModelService implements ModelService {

	private final HeaderParser headerParser;
	private final ApplicationEventPublisher eventPublisher;
	private final List<IModelRepository> modelRepositories;
	private final ModelHeaderJpaRepository modelHeaderJpaRepository;
	private final GenericModelReadRepository genericModelReadRepository;
	private final ModelPermissionEvaluator<GenericModel> modelPermissionEvaluator;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final DocumentModelFieldsIndexer documentModelFieldsIndexer;

	@Transactional
	@Override public GenericModel create(@NonNull String modelContent) {
		StopWatch stopWatch = StopWatch.createStarted();
		Header header = getValidHeader(modelContent);
		modelPermissionEvaluator.checkModelCreatePermission(header);
		// We need to allow client projects to change model content and header structure with synchronous events
		GenericModel modelFromEvent = publishBeforeCreateEvent(GenericModel.of(header, modelContent));
		Header headerFromEvent = modelFromEvent.getHeader();
		checkDataIntegrity(headerFromEvent);
		documentModelFieldsIndexer.indexDocumentModelFieldsOnCreate(modelFromEvent);
		GenericModel savedModel = getSupportingRepository(headerFromEvent)
			.save(headerFromEvent, modelFromEvent.getContent().getRawContent());
		checkAndSaveHeader(headerFromEvent, x -> true);
		eventPublisher.publishEvent(new ModelAfterCreateEvent(savedModel));
		log.debug("Model [{}] has been created in [{}] ms", header.getId(), stopWatch.getTime());
		return savedModel;
	}

	@Transactional
	@Override public GenericModel update(@NonNull String modelContent) {
		StopWatch stopWatch = StopWatch.createStarted();
		Header newHeader = getValidHeader(modelContent);
		ModelHeaderEntity oldHeader = modelHeaderJpaRepository.findById(newHeader.getId())
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY,
				String.format("Model header [%s] not found in the persistent store", newHeader.getId())));
		modelPermissionEvaluator.checkModelUpdatePermission(oldHeader);
		checkModelTypeIntegrity(newHeader, oldHeader);
		GenericModel oldModel = getSupportingRepository(newHeader)
			.load(newHeader)
			.orElseThrow(() -> {
				log.warn(String.format("Model [%s] cannot be updated because it does not exist", ((Header) oldHeader).getId()));
				return new NotFoundException(ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY,
					String.format("Model [%s] could not be loaded", ((Header) oldHeader).getId()));
			});
		GenericModel model = publishBeforeUpdateEvent(modelContent, newHeader, oldModel);
		Header headerFromEvent = model.getHeader();
		documentModelFieldsIndexer.indexDocumentModelFieldsOnUpdate(model);
		GenericModel updatedModel = getSupportingRepository(headerFromEvent)
			.update(headerFromEvent, model.getContent().getRawContent());
		checkAndSaveHeader(headerFromEvent, h -> !StringUtils.equals(oldHeader.getId(), h.getId()));
		eventPublisher.publishEvent(new ModelAfterUpdateEvent(oldModel, updatedModel));
		log.debug("Model [{}] has been updated in [{}] ms", newHeader.getId(), stopWatch.getTime());
		return updatedModel;
	}

	@Transactional
	@Override public boolean delete(@NonNull String modelId) {
		StopWatch stopWatch = StopWatch.createStarted();
		boolean result = modelHeaderJpaRepository.findById(modelId)
			.flatMap(header -> getSupportingRepository(header).load(header))
			.map(model -> {
				modelPermissionEvaluator.checkModelDeletePermission(model.getHeader());
				return model;
			})
			.map(this::publishBeforeDeleteEvent)
			.map(this::deleteModelInternal)
			.orElse(true);
		log.debug("Model [{}] has been deleted in [{}] ms", modelId, stopWatch.getTime());
		return result;
	}

	private boolean deleteModelInternal(GenericModel model) {
		Header headerFromEvent = model.getHeader();
		documentModelFieldsIndexer.indexDocumentModelFieldsOnDelete(model);
		boolean result = getSupportingRepository(headerFromEvent)
			.delete(headerFromEvent);
		if (result) {
			modelHeaderJpaRepository.deleteById(headerFromEvent.getId());
			eventPublisher.publishEvent(new ModelAfterDeleteEvent(model));
		}
		return result;
	}

	@Transactional(readOnly = true)
	@Override public GenericModel load(@NonNull String modelId) {
		StopWatch stopWatch = StopWatch.createStarted();
		GenericModel model = genericModelReadRepository.readModel(modelId);
		modelPermissionEvaluator.checkModelReadPermission(model);
		log.debug("Model [{}] has been loaded in [{}] ms", modelId, stopWatch.getTime());
		eventPublisher.publishEvent(new ModelAfterLoadEvent<>(model));
		return model;
	}

	@Transactional(readOnly = true)
	@Override public Collection<GenericModel> load(@NonNull Collection<String> modelIds) {
		Collection<GenericModel> models = modelIds.stream()
			.filter(modelHeaderJpaRepository::existsById)
			.map(genericModelReadRepository::readModel)
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.toList();

		eventPublisher.publishEvent(new ModelsAfterLoadEvent<>(models));
		return models;
	}

	@Transactional(readOnly = true)
	@Override public List<Header> findAllHeadersByType(String type) {
		StopWatch stopWatch = StopWatch.createStarted();
		List<Header> headers = modelHeaderJpaRepository.findByModelType(type).stream()
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.toList();
		log.debug("All headers for model type [{}] has been loaded in [{}] ms", type, stopWatch.getTime());
		return headers;
	}

	@Transactional(readOnly = true)
	@Override public List<Header> findAllHeaders() {
		StopWatch stopWatch = StopWatch.createStarted();
		List<Header> headers = modelHeaderJpaRepository.findAll().stream()
			.map(Header.class::cast)
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.toList();
		log.debug("All headers has been loaded in [{}] ms", stopWatch.getTime());
		return headers;
	}

	@Transactional(readOnly = true)
	@Override public boolean exists(@NonNull Header header) {
		StopWatch stopWatch = StopWatch.createStarted();
		ModelUtils.validateHeader(header, dataServicesCoreProperties.getAuthorization().getRoleBased().isEnabled());
		boolean result = getSupportingRepository(header).exists(header);
		log.debug("Exists query for model [{}] has been executed in [{}] ms", header.getId(), stopWatch.getTime());
		return result;
	}

	@Override public IModelRepository getSupportingRepository(Header header) {
		return modelRepositories.stream()
			.filter(r -> r.supports(header))
			.findFirst()
			.orElseThrow(() -> new UnexpectedException(ExceptionKeys.NO_MODEL_REPOSITORY_FOUND, "There should be at least one IModelRepository available!"));
	}

	private void checkAndSaveHeader(Header headerFromEvent, Predicate<Header> headerFilter) {
		fixAbstractness(headerFromEvent);
		validateHeterogeneity(headerFromEvent, headerFilter);
		modelHeaderJpaRepository.save(new ModelHeaderEntity(headerFromEvent));
	}

	private Header getValidHeader(String modelContent) {
		Header newHeader = parseHeader(modelContent);
		ModelUtils.validateHeader(newHeader, dataServicesCoreProperties.getAuthorization().getRoleBased().isEnabled());
		return newHeader;
	}

	private static void checkModelTypeIntegrity(Header newHeader, Header oldHeader) {
		if (!Objects.equals(oldHeader.getModelType(), newHeader.getModelType())) {
			throw new IntegrityException(ExceptionKeys.MODEL_MISMATCH_ERROR_KEY,
				String.format("Changing of model type is not permitted for [%s]", oldHeader.getId()));
		}
	}

	private void checkDataIntegrity(Header headerFromEvent) {
		if (exists(headerFromEvent)) {
			log.warn("Model [{}] cannot be created because it already exists", headerFromEvent.getId());
			throw new IntegrityException(ExceptionKeys.MODEL_UNIQUENESS_ERROR_KEY,
				String.format("Model entity with ID [%s] already exists.", headerFromEvent.getId()));
		}
	}

	private Header parseHeader(String modelContent) {
		try {
			return headerParser.parseJson(modelContent);
		} catch (HeaderParseException ex) {
			log.error("Model header deserialization failed");
			throw new SerializationException(ExceptionKeys.MODEL_DESERIALIZATION_ERROR_KEY,
				String.format("Model validation failed. Model is not acceptable:%n:%s", modelContent), BaseException.MessagePriority.HIGH, ex)
				.withAnonymityMessage("Model is not acceptable.");
		}
	}

	private GenericModel publishBeforeCreateEvent(GenericModel modelToCreate) {
		ModelBeforeCreateEvent beforeEvent = new ModelBeforeCreateEvent(modelToCreate);
		eventPublisher.publishEvent(beforeEvent);
		return beforeEvent.getModel();
	}

	private GenericModel publishBeforeUpdateEvent(String modelContent, Header newHeader, GenericModel oldModel) {
		ModelBeforeUpdateEvent beforeEvent = new ModelBeforeUpdateEvent(oldModel, GenericModel.of(newHeader, modelContent));
		eventPublisher.publishEvent(beforeEvent);
		return beforeEvent.getModel();
	}

	private GenericModel publishBeforeDeleteEvent(GenericModel model) {
		ModelBeforeDeleteEvent beforeEvent = new ModelBeforeDeleteEvent(model);
		eventPublisher.publishEvent(beforeEvent);
		return beforeEvent.getModel();
	}

	private void fixAbstractness(Header header) {
		// TODO A12S-1770: Remove this tweak as soon as A12 ticket A12-12542 is resolved
		List<Annotation> annotations = header.getAnnotations();
		if (CollectionUtils.isEmpty(annotations)) {
			return;
		}
		annotations.replaceAll(a -> ("abstract".equals(a.getName()) && StringUtils.isBlank(a.getValue()))
			? new Annotation() {
			@Override
			public String getName() {
				return "abstract";
			}

			@Override
			public String getValue() {
				return "true";
			}
		}
			: a);
	}

	private void validateHeterogeneity(Header headerFromEvent, Predicate<Header> headerFilter) {
		List<Header> documentModelHeaders = modelHeaderJpaRepository.findByModelType(DOCUMENT_MODEL_TYPE).stream()
			.filter(headerFilter)
			.collect(toList());
		documentModelHeaders.add(headerFromEvent);
		ModelUtils.validateHeterogeneity(documentModelHeaders);
	}
}
