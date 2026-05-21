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
package com.mgmtp.a12.dataservices;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.metadata.internal.AbstractDocumentModelMetadataInjector.SELECTION_MODEL_V_1_MODEL_NAME;
import static com.mgmtp.a12.dataservices.model.metadata.internal.AttachmentMetadataMetaModelProvider.ATTACHMENT_METADATA_MODEL_PATH;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.META_MODEL_JSON_LOCATION;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.META_MODEL_NAME;

@Slf4j
@RequiredArgsConstructor public class TestResourcesDocumentModelResolver
	implements IDocumentModelResolver, IDocumentModelReferenceResolver, IModelLoader<IDocumentModel> {

	public static final String DOCUMENT_METADATA_MODEL_NAME = "DefaultDocumentMetadata";
	public static final String ATTACHMENT_METADATA_MODEL_NAME = "AttachmentMetadata";
	private final KernelTestSupport kernelTestSupport;

	private final Map<String, Optional<IDocumentModelSearchService>> documentModelSearchServiceCache = new ConcurrentSkipListMap<>();
	private final Map<String, Optional<IDocumentModel>> expandedDocumentModelCache = new ConcurrentSkipListMap<>();
	private final Map<String, Optional<IDocumentModel>> documentModelCache = new ConcurrentSkipListMap<>();
	private final Map<String, Resource> customModels = new ConcurrentSkipListMap<>();
	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	@Override public IDocumentModel getDocumentModelById(String id) {
		return expandedDocumentModelCache.computeIfAbsent(id, k -> Optional.ofNullable(getDocumentModel(k))
				.map(this::expand)
				.map(this::addMetaData))
			.orElseThrow(() -> new NotFoundException("Model %s not found".formatted(id)));
	}

	@SneakyThrows @Override public IDocumentModel getDocumentModel(String reference) {
		return documentModelCache.computeIfAbsent(reference, this::getModel)
			.orElseThrow(() -> new NotFoundException("Model %s not found".formatted(reference)));
	}

	@Override public Optional<IDocumentModelSearchService> getDocumentModelSearchService(String documentModelId) {
		return documentModelSearchServiceCache.computeIfAbsent(documentModelId,
			k -> Optional.ofNullable(kernelTestSupport.getDocumentModelServiceFactory().createDocumentModelSearchService(getDocumentModelById(k))));
	}

	public void addCustomModel(String name, Resource resource) {
		customModels.put(name, resource);
	}

	private IDocumentModel expand(IDocumentModel documentModel) {
		kernelTestSupport.getIDocumentModelService().collapse(documentModel);
		kernelTestSupport.getIDocumentModelService().expand(documentModel, this);
		return documentModel;
	}

	@SneakyThrows @NonNull private Optional<IDocumentModel> getModel(String id) {
		return Optional.ofNullable(customModels.get(id))
			.or(() -> findModelOnClasspath(id))
			.map(resource -> {
				try (Reader r = new StringReader(resource.getContentAsString(StandardCharsets.UTF_8))) {

					return kernelTestSupport.getDocumentModelSerializer().deserialize(r);
				} catch (IOException | NullPointerException e) {
					log.warn("Model %s not found.".formatted(id), e);
					return null;
				}
			});
	}

	@SneakyThrows @NotNull private Optional<Resource> findModelOnClasspath(String id) {
		String path = determinePath(id);
		Optional<Resource> res = Optional.of(resourcePatternResolver.getResources(path))
			.filter(r -> r.length > 0)
			.map(r -> r[0]);
		if (res.isEmpty()) {
			log.warn("Model {} not found at path {}.", id, path);
		}
		return res;
	}

	private IDocumentModel addMetaData(IDocumentModel documentModel) {
		return new DocumentModelMetadataInjectorFactory(kernelTestSupport.getDocumentModelJoiningService(),
			kernelTestSupport.getDocumentFactory(), kernelTestSupport.getDocumentModelService()).getInstance(documentModel, Locale.ENGLISH)
			.getDocumentModelWithMetadata(
				getModel(DOCUMENT_METADATA_MODEL_NAME).orElseThrow(() -> new NotFoundException("Document metadata model not found.")),
				getModel(ATTACHMENT_METADATA_MODEL_NAME).orElseThrow(() -> new NotFoundException("Attachment metadata model not found.")));
	}

	private static String determinePath(String id) {
		DataServicesCoreProperties dataServicesCoreProperties = new DataServicesCoreProperties();
		return switch (id) {
			case SELECTION_MODEL_V_1_MODEL_NAME -> "classpath*:/a12-selectionmodel-v1.json";
			case DOCUMENT_METADATA_MODEL_NAME -> "classpath*:" + dataServicesCoreProperties.getModels().getMetadata().getDocument().getPath();
			case ATTACHMENT_METADATA_MODEL_NAME -> "classpath*:" + ATTACHMENT_METADATA_MODEL_PATH;
			case META_MODEL_NAME -> "classpath*:" + META_MODEL_JSON_LOCATION;
			default -> "classpath*:/models/**/%s.json".formatted(id);
		};
	}

	@Override public IDocumentModel loadModel(@NonNull String modelId) {
		return getDocumentModelById(modelId);
	}
}
