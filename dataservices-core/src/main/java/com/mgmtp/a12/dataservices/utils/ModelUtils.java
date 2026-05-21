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
package com.mgmtp.a12.dataservices.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import com.mgmtp.a12.dataservices.authorization.internal.UaaConnector;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.internal.ModelSubtypes;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static com.mgmtp.a12.dataservices.document.DocumentReference.MODEL_NAME_PATTERN;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUB_TYPES_ANNOTATION_KEY;
import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.SUPER_TYPES_ANNOTATION_KEY;

/**
 * @deprecated This class will be moved into internal package as it is not meant for public use.
 */
@Deprecated(since = "37.0.0")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelUtils {

	private static final Set<String> HETEROGENEITY_ANNOTATIONS = Set.of(SUPER_TYPES_ANNOTATION_KEY, SUB_TYPES_ANNOTATION_KEY);
	private static final String ROLES_ANNOTATION = "roles";

	/**
	 * Checks if the model name matches the allowed pattern {@link com.mgmtp.a12.dataservices.document.DocumentReference}
	 *
	 * @param modelName the model name to be checked
	 * @return true if model name is valid
	 */
	public static boolean isModelNameValid(String modelName) {
		return MODEL_NAME_PATTERN.matcher(modelName).matches();
	}

	/**
	 * Validates that there is no loop cycle in document models heterogeneity.
	 *
	 * @param modelHeaders model headers to validate.
	 * @throws InvalidInputException in case of loop cycle
	 */
	public static void validateHeterogeneity(@NonNull List<? extends Header> modelHeaders) {
		computeModelHeterogeneity(modelHeaders).values()
			.forEach(modelSubtypes -> validateHeterogeneity(modelSubtypes, new HashSet<>(), modelSubtypes.getModelName()));
	}

	/**
	 * @param modelHeaders All available document model headers. Supertypes and subtypes are computed from these headers.
	 * @return Map of model name as a key and {@link ModelSubtypes} structure as a value.
	 * 	This structure is capable of returning either direct subtypes or the whole type hierarchy.
	 */
	@NonNull public static Map<String, ModelSubtypes> computeModelHeterogeneity(@NonNull List<? extends Header> modelHeaders) {
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

	public static void validateHeader(@NonNull Header header, boolean roleBased) {
		if (StringUtils.isEmpty(header.getId())) {
			throw new InvalidInputException(ExceptionKeys.MODEL_ID_NOT_FOUND_ERROR_KEY, "Model cannot be created without id");
		}

		if (!isModelNameValid(header.getId())) {
			throw new InvalidInputException(ExceptionKeys.MODEL_ID_NOT_VALID_ERROR_KEY,
				String.format("Model name [%s] is not valid", header.getId()));
		}

		if (StringUtils.isEmpty(header.getModelType())) {
			throw new InvalidInputException(ExceptionKeys.MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY,
				String.format("Model [%s] does not have modelType defined", header.getId()));
		}

		if (roleBased && getObjectRoles(header).isEmpty()) {
			throw new InvalidInputException(ExceptionKeys.MODEL_ROLES_NOT_FOUND_ERROR_KEY,
				String.format("Model [%s] does not have roles defined", header.getId()));
		}

		if (annotationsContainNullOrBlankName(header)) {
			throw new InvalidInputException(ExceptionKeys.MODEL_HEADER_ANNOTATION_IS_MISSING_ERROR_KEY,
				String.format("Header annotation of model [%s] is missing", header.getId()));
		}

	}

	public static List<String> getObjectRoles(@NonNull Header header) {
		return Optional.ofNullable(header.getAnnotations()).stream()
			.flatMap(Collection::stream)
			.filter(a -> ROLES_ANNOTATION.equals(a.getName()))
			.map(Annotation::getValue)
			.map(a -> a.split(","))
			.flatMap(Arrays::stream)
			.map(StringUtils::trim)
			.filter(StringUtils::isNotBlank)
			.toList();
	}

	/**
	 * Collect all the mutual roles from roles annotation in document header and roles from authenticated user's authorities.
	 * @param header Document header to be collected mutual roles.
	 * @return collection of user granted authorities which also exist in document header roles annotation.
	 */
	public static Collection<GrantedAuthority> getMatchingRoles(Header header) {
		List<String> objectRoles = getObjectRoles(header);
		return UaaConnector.getCurrentUserAuthorities()
			.stream()
			.filter(auth -> objectRoles.contains(auth.getAuthority()))
			.map(GrantedAuthority.class::cast)
			.toList();
	}

	private static void chainSubTypes(ModelSubtypes typeFromAnnotation, ModelSubtypes annotatedType, String annotationName) {
		switch (annotationName) {
		case SUPER_TYPES_ANNOTATION_KEY -> typeFromAnnotation.getDirectSubtypes().add(annotatedType);
		case SUB_TYPES_ANNOTATION_KEY -> annotatedType.getDirectSubtypes().add(typeFromAnnotation);
		}
	}

	private static void validateHeterogeneity(ModelSubtypes modelSubtypes, Set<String> alreadyProcessed, String topLevelModelName) {
		alreadyProcessed.add(modelSubtypes.getModelName());
		modelSubtypes.getDirectSubtypes().forEach(subEntity -> {
			if (alreadyProcessed.contains(subEntity.getModelName())) {
				throw new InvalidInputException(ExceptionKeys.DOCUMENT_MODEL_HETEROGENEITY_ERROR_KEY, String.format(
					"Heterogeneity inheritance loop in model's %s chain: Nested model %s has subtype %s which is already in the chain of its supertypes.",
					topLevelModelName, modelSubtypes.getModelName(), subEntity.getModelName()));
			}
			validateHeterogeneity(subEntity, new HashSet<>(alreadyProcessed), topLevelModelName);
		});
	}

	private static boolean annotationsContainNullOrBlankName(Header header) {
		return com.mgmtp.a12.dataservices.utils.internal.ModelUtils.getAnnotations(header)
			.anyMatch(annotation -> StringUtils.isBlank(annotation.getName()));
	}
}
