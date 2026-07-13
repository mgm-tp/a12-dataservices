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
package com.mgmtp.a12.dataservices.query.enrichement;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.mgmtp.a12.dataservices.query.LinkAware;
import com.mgmtp.a12.dataservices.query.TargetDocumentModelAware;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.enrichement.internal.FieldPathValidator;
import com.mgmtp.a12.dataservices.query.internal.aggregation.RepeatableAggField;

import lombok.Getter;


/**
 * A class that holds query enrichment information.
 *
 * Currently it stores information/enrichments about:
 *
 * - field types
 * - operators
 * - document models
 * - roles
 *
 * @see FieldDescriptor
 */
public class Enrichments {

	private final Map<ILogicOperator, Map<String, Object>> operatorEnrichments = new IdentityHashMap<>();
	private final Map<String, FieldDescriptor> fieldEnrichments = new HashMap<>();
	private final Map<String, Set<String>> modelSubtypes = new HashMap<>();
	private final Map<String, String> modelLocales = new HashMap<>();
	private final Map<LinkAware, String> sourceRoles = new IdentityHashMap<>();
	private final Map<Object, String> targetDocumentModels = new IdentityHashMap<>();
	private final Map<Object, String> relationshipOrderSourceRoles = new IdentityHashMap<>();
	private final Map<String, Deque<RepeatableAggField>> repeatableAggFields = new HashMap<>();
	@Getter private final Map<String, String> repeatableAggGroupAliases = new HashMap<>();

	/**
	 * Property key representing the value part of a range or typed entry.
	 */
	public static final String VALUE_PROPERTY = "value";
	/**
	 * Property key representing the start (from) of a range.
	 */
	public static final String FROM_PROPERTY = "from";
	/**
	 * Property key representing the end (to) of a range.
	 */
	public static final String TO_PROPERTY = "to";

	/**
	 * Returns enrichment metadata associated with a particular logic operator.
	 * Creates an empty enrichment map if none exists yet.
	 *
	 * @param l the logic operator to retrieve enrichment for; must not be null.
	 * @return a mutable map of enrichment properties for the operator.
	 */
	public Map<String, Object> getOperatorEnrichment(ILogicOperator l) {
		return operatorEnrichments.computeIfAbsent(l, k -> new HashMap<>());
	}

	/**
	 * Sets the field information (FieldDescriptor) for the given field,
	 *
	 * @param fieldPath the path of the field inside the document model
	 * @param fd the FieldDescriptor to be used
	 */
	public void setFieldDescriptor(String fieldPath, FieldDescriptor fd) {
		Objects.requireNonNull(fd, "field descriptor must not be null");
		FieldPathValidator.validateFieldPath(fieldPath);
		fieldEnrichments.put(fieldPath, fd);
	}

	/**
	 * Returns the field descriptor/metadata for the given field.
	 *
	 * @param fieldPath the full path of the field
	 * @return the descriptor for this field, never null.
	 */
	public FieldDescriptor getFieldDescriptor(String fieldPath) {
		FieldPathValidator.validateFieldPath(fieldPath);
		return fieldEnrichments.computeIfAbsent(fieldPath, k -> new FieldDescriptor());
	}

	/**
	 * Resolves the target document model for the given input.
	 * If the input implements {@link TargetDocumentModelAware}, its value is used; otherwise,
	 * a previously stored mapping is returned.
	 *
	 * @param input the source object to resolve the document model for; may be null.
	 * @return the target document model identifier; may be null if unknown.
	 */
	public String getTargetDocumentModel(Object input) {
		return input instanceof TargetDocumentModelAware targetDocumentModelAware
			? targetDocumentModelAware.getTargetDocumentModel()
			: targetDocumentModels.get(input);
	}

	/**
	 * Associates a target document model with the given object.
	 *
	 * @param object the key object to associate with a model; must not be null.
	 * @param modelId the identifier of the target document model; may be null to clear association.
	 */
	public void setTargetDocumentModel(Object object, String modelId) {
		targetDocumentModels.put(object, modelId);
	}

	/**
	 * Returns the source role associated with a given link-aware input.
	 *
	 * @param input the link reference to look up; may be null.
	 * @return the source role; may be null if none is associated.
	 */
	public String getSourceRole(LinkAware input) {
		return sourceRoles.get(input);
	}

	/**
	 * Associates a source role with the given link-aware input.
	 *
	 * @param input the link reference; must not be null.
	 * @param role the role name; may be null to clear association.
	 */
	public void setSourceRole(LinkAware input, String role) {
		sourceRoles.put(input, role);
	}

	/**
	 * Returns the source role associated with the given relationship order.
	 *
	 * @param order the relationship order; may be null.
	 * @return the source role name; may be null if none is associated.
	 */
	public String getSourceRoleForRelationshipOrder(Object order) {
		return relationshipOrderSourceRoles.get(order);
	}

	/**
	 * Associates a source role with the given relationship order.
	 *
	 * @param order the relationship order; must not be null.
	 * @param role the source role name; may be null to clear association.
	 */
	public void setSourceRoleForRelationshipOrder(Object order, String role) {
		relationshipOrderSourceRoles.put(order, role);
	}

	/**
	 * Returns the known subtypes for the given document model.
	 *
	 * @param modelId the model identifier; must not be null.
	 * @return the set of subtypes; may be null if not computed yet.
	 */
	public Set<String> getModelSubtypes(String modelId) {
		return modelSubtypes.get(modelId);
	}

	/**
	 * Computes and caches subtypes for the given document model if not present.
	 *
	 * @param targetDocumentModel the model identifier; must not be null.
	 * @param generator the function that computes subtypes for the model; must not be null.
	 */
	public void computeModelSubtypes(String targetDocumentModel, Function<String, Set<String>> generator) {
		modelSubtypes.computeIfAbsent(targetDocumentModel, generator);
	}

	/**
	 * Returns the locale configured for the given document model.
	 *
	 * @param modelId the model identifier; must not be null.
	 * @return the locale; may be null if not computed yet.
	 */
	public String getModelLocale(String modelId) {
		return modelLocales.get(modelId);
	}

	/**
	 * Computes and caches the locale for the given document model if not present.
	 *
	 * @param targetDocumentModel the model identifier; must not be null.
	 * @param generator the function that computes the locale for the model; must not be null.
	 */
	public void computeModelLocale(String targetDocumentModel, Function<String, String> generator) {
		modelLocales.computeIfAbsent(targetDocumentModel, generator);
	}

	/**
	 * Returns the stack of repeatable aggregation fields for the given path.
	 *
	 * @param path the field path; must not be null.
	 * @return a deque representing the stack of repeatable aggregation fields; may be null if none exist.
	 */
	public Deque<RepeatableAggField> getRepeatableAggFields(String path) {
		return repeatableAggFields.get(path);
	}

	/**
	 * Adds a repeatable aggregation field for the given path if not already present
	 * (based on temporary aggregation name).
	 *
	 * @param path the field path; must not be null.
	 * @param repeatableAggField the aggregation field descriptor; must not be null.
	 */
	public void addRepeatableAggField(String path, RepeatableAggField repeatableAggField) {
		repeatableAggFields.computeIfAbsent(path, k -> new ArrayDeque<>());
		if (repeatableAggFields.get(path).stream()
			.noneMatch(field -> field.tempAggName().equals(repeatableAggField.tempAggName()))) {
			repeatableAggFields.get(path).push(repeatableAggField);
		}
	}

}

