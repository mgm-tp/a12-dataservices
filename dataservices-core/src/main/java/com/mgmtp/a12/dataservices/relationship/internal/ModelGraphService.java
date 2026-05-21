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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.ModelSubtypes;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.ModelGraphComposeDocumentModelElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphComposeDocumentModelElement.ModelGraphComposeDocumentModelElementBuilder;
import com.mgmtp.a12.dataservices.relationship.ModelGraphDocumentModelElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphOtherModelElement;
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.ComposeDocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.model.internal.ModelCacheManager.MODEL_GRAPH_CACHE;

/**
 * Model graphs service should not be used directly because the model graph is cached for better performance during production scenario but those caches
 * cannot be used during modeling scenario, Please use {@link ModelTypeService} instead.
 */
@Service public class ModelGraphService {

	private final ModelService modelService;
	private final RelationshipModelLoader relationshipModelLoader;
	private final RelationshipModelSerializer relationshipModelSerializer;
	private final DocumentModelUtils documentModelUtils;
	private final ModelTypeService modelTypeService;
	private final Optional<DefaultModelTypeService> defaultModelTypeServiceOpt;
	private final ModelPermissionEvaluator<Model> modelPermissionEvaluator;

	public ModelGraphService(ModelService modelService, RelationshipModelLoader relationshipModelLoader,
		RelationshipModelSerializer relationshipModelSerializer, DocumentModelUtils documentModelUtils, ModelTypeService modelTypeService,
		Optional<DefaultModelTypeService> defaultModelTypeServiceOpt, ModelPermissionEvaluator<Model> modelPermissionEvaluator) {
		this.modelService = modelService;
		this.relationshipModelLoader = relationshipModelLoader;
		this.relationshipModelSerializer = relationshipModelSerializer;
		this.documentModelUtils = documentModelUtils;
		this.modelTypeService = modelTypeService;
		this.defaultModelTypeServiceOpt = defaultModelTypeServiceOpt;
		this.modelPermissionEvaluator = modelPermissionEvaluator;
	}

	/**
	 * @param username is only used as a cache key, and so it must be declared as a parameter.
	 */
	@Cacheable(value = MODEL_GRAPH_CACHE, key = "{#username, "
		+ "T(com.mgmtp.a12.dataservices.authorization.internal.UaaConnector).getCurrentUserAuthoritiesAsString()}")
	@Transactional(readOnly = true)
	public ModelGraphRoot constructModelGraph(String username) {
		return constructModelGraph();
	}

	@Transactional(readOnly = true) public ModelGraphRoot constructModelGraph() {
		Set<RelationshipModel> availableRMs = relationshipModelLoader.loadAllRelationshipModels();
		List<Header> documentModels = modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE);

		return ModelGraphRoot.builder()
			.documentModels(constructDocumentModels(availableRMs, documentModels))
			.genericModels(constructOtherModels())
			.relationshipModelMap(constructRelationshipModels(availableRMs))
			.composeDocumentModels(constructCdms(documentModels))
			.build();
	}

	private Set<ModelGraphOtherModelElement> constructOtherModels() {
		return modelService.findAllHeaders().stream()
			.filter(h -> !DOCUMENT_MODEL_TYPE.equals(h.getModelType()) && !RelationshipModel.RELATIONSHIP_MODEL_TYPE.equals(h.getModelType()))
			.map(h -> ModelGraphOtherModelElement.builder()
				.modelId(h.getId())
				.displayLabels(h.getLabels())
				.modelType(h.getModelType())
				.modelReferences(h.getModelReferences())
				.build())
			.collect(Collectors.toSet());
	}

	@NonNull private Set<ModelGraphDocumentModelElement> constructDocumentModels(Set<RelationshipModel> availableRMs, List<Header> documentModels) {

		Map<String, Set<String>> subtypeData = defaultModelTypeServiceOpt
			.map(defaultModelTypeService -> getDirectSubtypeMapWithReadPermission(documentModels))
			.orElse(null);

		return documentModels.stream()
			.filter(ComposeDocumentModelUtils::isDocumentModel)
			.map(header -> ModelGraphDocumentModelElement.builder()
				.modelId(header.getId())
				.modelReferences(header.getModelReferences())
				.abstractModel(documentModelUtils.isAbstract(header))
				.displayLabels(header.getLabels()).build())
			.map(modelGraphElement -> enrichModelGraphDocumentElement(availableRMs, modelGraphElement, subtypeData))
			.collect(Collectors.toSet());
	}

	/**
	 * Return map of model names as a key and it's subtype set as a value.
	 * Only models that user has permission to read are returned.
	 * CAUTION: All available document model headers must be passed to this method to properly compute subtypes from supertypes.
	 *
	 * @param availableDocumentModelHeaders prefetched list of all available headers which will be used as a source of the subtype map.
	 * @return map of model names as a key and it's subtype set as a value.
	 */
	private Map<String, Set<String>> getDirectSubtypeMapWithReadPermission(List<Header> availableDocumentModelHeaders) {
		return ModelUtils.computeModelHeterogeneity(availableDocumentModelHeaders).entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, m -> m.getValue().getDirectSubtypes().stream()
				.map(ModelSubtypes::getModelName)
				.filter(modelPermissionEvaluator::hasModelReadPermission)
				.collect(Collectors.toSet())));
	}

	@NonNull private static Set<ModelGraphElement> constructCdms(List<Header> documentModels) {
		return documentModels.stream()
			.filter(ComposeDocumentModelUtils::isComposeDocumentModel)
			.map(header -> {
				ModelGraphComposeDocumentModelElementBuilder builder = ModelGraphComposeDocumentModelElement.builder()
					.modelId(header.getId())
					.modelReferences(header.getModelReferences())
					.displayLabels(header.getLabels());
				return builder.rootDocumentModelId(ComposeDocumentModelUtils.getCrdModelName(header))
					.build();
			})
			.collect(Collectors.toSet());
	}

	@NonNull private Map<String, String> constructRelationshipModels(Set<RelationshipModel> availableRMs) {
		return availableRMs.stream()
			.map(rm -> new ImmutablePair<>(rm.getHeader().getId(), relationshipModelSerializer.serialize(rm)))
			.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	private ModelGraphDocumentModelElement enrichModelGraphDocumentElement(Set<RelationshipModel> allRelationShipModels, ModelGraphDocumentModelElement model,
		Map<String, Set<String>> subtypeData) {
		model.setRelations(findRelatedRelationshipModelsForDocumentModel(allRelationShipModels, model.getModelId()));
		model.setSubTypes(
			subtypeData != null
				? subtypeData.getOrDefault(model.getModelId(), Set.of())
				: modelTypeService.findDirectSubtypes(model.getModelId())
		);
		return model;
	}

	private List<String> findRelatedRelationshipModelsForDocumentModel(Collection<RelationshipModel> allRelationShipModels, String documentModelName) {
		return allRelationShipModels.stream()
			.map(relationshipModelRoot -> {
				List<EntityCharacteristics> entityCharacteristics = relationshipModelRoot.getContent().getEntityCharacteristics();

				return isDocumentModelPresentInRelationship(entityCharacteristics, documentModelName) ? relationshipModelRoot.getHeader().getId() : null;
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private boolean isDocumentModelPresentInRelationship(List<EntityCharacteristics> entityCharacteristics, String searchedDocumentModel) {
		return entityCharacteristics.stream()
			.map(characteristics -> characteristics.getDocumentModel().equalsIgnoreCase(searchedDocumentModel) ? characteristics.getRole() : null)
			.anyMatch(StringUtils::isNotBlank);
	}

}
