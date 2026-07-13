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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUB_TYPES_ANNOTATION_KEY;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUPER_TYPES_ANNOTATION_KEY;

/**
 * Abstract base class providing infrastructure-agnostic implementations of {@link ModelTypeService#findAllSubtypes(String)}
 * and {@link ModelTypeService#findDirectSubtypes(String)}.
 *
 * Subclasses may wrap these methods with caching or additional cross-cutting concerns.
 * Results are computed on every call in this base class — no caching is performed here.
 */
public abstract class AbstractModelTypeService implements ModelTypeService {

	private static final Set<String> HETEROGENEITY_ANNOTATIONS = Set.of(SUPER_TYPES_ANNOTATION_KEY, SUB_TYPES_ANNOTATION_KEY);

	private final ModelService modelService;
	private final ModelPermissionEvaluator<Model> modelPermissionEvaluator;

	protected AbstractModelTypeService(ModelService modelService, ModelPermissionEvaluator<Model> modelPermissionEvaluator) {
		this.modelService = modelService;
		this.modelPermissionEvaluator = modelPermissionEvaluator;
	}

	@Override public Set<String> findAllSubtypes(String documentModelName) {
		return Optional.of(computeAllSubtypes())
			.map(subtypesMap -> subtypesMap.get(documentModelName))
			.orElse(Set.of());
	}

	@Override public Set<String> findDirectSubtypes(String documentModelName) {
		return Optional.of(ModelUtils.computeModelHeterogeneity(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE)))
			.map(subTypes -> remapModelsToSubtypes(subTypes, this::getDirectSubtypesWithReadPermission))
			.map(subtypesMap -> subtypesMap.get(documentModelName))
			.orElse(Set.of());
	}

	@Override public Set<String> findModelNameAndAllSubtypes(String documentModelName) {
		return SetUtils.union(findAllSubtypes(documentModelName), Set.of(documentModelName));
	}

	@Override public boolean isSubtype(String parentModelName, String testedModelName) {
		return findModelNameAndAllSubtypes(parentModelName).stream().anyMatch(m -> Objects.equals(m, testedModelName));
	}

	/**
	 * Computes the inheritance structure for the given model headers by processing
	 * `superTypes` and `subTypes` annotations.
	 *
	 * @param modelHeaders All available document model headers from which the structure is derived.
	 * @return Map of model name to its {@link ModelSubtypes} node, which exposes both direct and transitive subtypes.
	 */
	protected Map<String, ModelSubtypes> computeModelHeterogeneity(@NonNull List<? extends Header> modelHeaders) {
		Map<String, ModelSubtypes> inheritanceStructureHolder = new HashMap<>();
		modelHeaders.forEach(header -> {
			ModelSubtypes annotatedType = inheritanceStructureHolder.computeIfAbsent(header.getId(), ModelSubtypes::new);
			com.mgmtp.a12.dataservices.utils.internal.ModelUtils.getAnnotations(header)
				.filter(a -> HETEROGENEITY_ANNOTATIONS.contains(a.getName()))
				.filter(a -> StringUtils.isNotBlank(a.getValue()))
				.forEach(annotation -> Arrays.stream(annotation.getValue().split(","))
					.filter(StringUtils::isNotBlank)
					.forEach(annotationValues ->
						chainSubTypes(inheritanceStructureHolder.computeIfAbsent(annotationValues, ModelSubtypes::new), annotatedType, annotation.getName())));
		});
		return inheritanceStructureHolder;
	}

	/**
	 * Computes the full subtype map for all document models.
	 * Results are not cached in this base implementation.
	 *
	 * @return Map of document model name to the set of all (deeply nested) subtypes with read permission.
	 */
	protected Map<String, Set<String>> computeAllSubtypes() {
		return remapModelsToSubtypes(ModelUtils.computeModelHeterogeneity(modelService.findAllHeadersByType(DOCUMENT_MODEL_TYPE)),
			this::getAllSubtypesWithReadPermission);
	}

	/**
	 * Links two {@link ModelSubtypes} nodes according to the given annotation.
	 * A `superTypes` annotation makes `annotatedType` a direct subtype of `typeFromAnnotation`;
	 * a `subTypes` annotation makes `typeFromAnnotation` a direct subtype of `annotatedType`.
	 *
	 * @param typeFromAnnotation The node referenced in the annotation value.
	 * @param annotatedType      The node carrying the annotation.
	 * @param annotationName     Either {@link com.mgmtp.a12.dataservices.relationship.model.RelationshipModel#SUPER_TYPES_ANNOTATION_KEY}
	 *                           or {@link com.mgmtp.a12.dataservices.relationship.model.RelationshipModel#SUB_TYPES_ANNOTATION_KEY}.
	 */
	protected void chainSubTypes(ModelSubtypes typeFromAnnotation, ModelSubtypes annotatedType, String annotationName) {
		switch (annotationName) {
		case SUPER_TYPES_ANNOTATION_KEY -> typeFromAnnotation.getDirectSubtypes().add(annotatedType);
		case SUB_TYPES_ANNOTATION_KEY -> annotatedType.getDirectSubtypes().add(typeFromAnnotation);
		}
	}

	private Set<String> getDirectSubtypesWithReadPermission(ModelSubtypes value) {
		return value.getDirectSubtypes().stream()
			.map(ModelSubtypes::getModelName)
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.collect(Collectors.toSet());
	}


	private Set<String> getAllSubtypesWithReadPermission(ModelSubtypes modelInheritanceEntity) {
		return StreamSupport.stream(modelInheritanceEntity.spliterator(), false)
			.map(ModelSubtypes::getModelName)
			.filter(modelPermissionEvaluator::hasModelReadPermission)
			.collect(Collectors.toSet());
	}

	private Map<String, Set<String>> remapModelsToSubtypes(Map<String, ModelSubtypes> modeInheritanceEntity, Function<ModelSubtypes, Set<String>> transformer) {
		return modeInheritanceEntity.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, m -> transformer.apply(m.getValue())));
	}
}
