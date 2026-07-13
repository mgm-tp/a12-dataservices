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
package com.mgmtp.a12.dataservices.search.customizer.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.search.customizer.SearchCustomizer;
import com.mgmtp.a12.dataservices.search.customizer.SearchDataContext;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.extern.slf4j.Slf4j;

/**
 * Registry for managing and applying search customizers to documents and model fields.
 * This component acts as a central point for applying custom search data transformations
 * through registered {@link SearchCustomizer} instances.
 */
@Slf4j
@Component public class SearchCustomizerRegistry {

	private final List<SearchCustomizer> customizers;
	private final boolean hasCustomizers;

	/**
	 * Constructs a new SearchCustomizerRegistry with the provided list of customizers.
	 *
	 * @param customizers the list of search customizers to register, may be null
	 */
	public SearchCustomizerRegistry(@Autowired(required = false) List<SearchCustomizer> customizers) {
		this.customizers = customizers == null ? Collections.emptyList() : customizers;
		if (this.customizers.isEmpty()) {
			hasCustomizers = false;
		} else {
			hasCustomizers = true;
			log.info("Registered {} search customizer(s): {}", this.customizers.size(),
				this.customizers.stream().map(c -> c.getClass().getSimpleName()).toList());
		}
	}

	/**
	 * Applies all registered search customizers to the provided search data.
	 *
	 * @param documentModelSearchService the document model search service
	 * @param indexableDocument the document being indexed
	 * @param modelName the name of the document model
	 * @param baseSearchData the initial search data to be customized
	 * @return the customized search data after applying all customizers, or the base search data if no customizers are registered
	 */
	public String customizeSearchData(IDocumentModelSearchService documentModelSearchService, DocumentV2 indexableDocument, String modelName,
		String baseSearchData) {

		if (hasCustomizers()) {
			SearchDataContext ctx = new SearchDataContextImpl(documentModelSearchService, indexableDocument, modelName, baseSearchData);
			customizers.forEach(searchCustomizer -> searchCustomizer.customizeSearchData(ctx));
			return ctx.getCurrentSearchData();
		} else {
			return baseSearchData;
		}
	}

	/**
	 * Applies all registered search customizers to document fields during indexing.
	 *
	 * @param documentModelSearchService the document model search service
	 * @param documentReference the reference to the document being processed
	 * @param pointerRelativeToBase the document pointer relative to the base document
	 * @param field the field instance being processed
	 * @param iField the field definition from the model
	 * @param fieldTypeIdProvider function to provide field type IDs based on model and field type name
	 * @return the document field context containing customized field data, or null if no customizers are registered
	 */
	public DocumentFieldContextImpl customizeDocumentFields(IDocumentModelSearchService documentModelSearchService, DocumentReference documentReference,
		DocumentPointer pointerRelativeToBase, FieldInstanceV2 field, IField iField, BiFunction<String, String, Long> fieldTypeIdProvider) {

		if (hasCustomizers()) {
			DocumentFieldContextImpl ctx = new DocumentFieldContextImpl(documentModelSearchService, documentReference, pointerRelativeToBase, field, iField,
				fieldTypeIdProvider);
			customizers.forEach(searchCustomizer -> searchCustomizer.customizeDocumentFields(ctx));
			return ctx;
		} else {
			throw new IllegalStateException(
				"existence of customizers should be chacked on top level and the code shouldn't reach this point if customizers are not defined!");
		}
	}

	/**
	 * Applies all registered search customizers to model fields during model field entity creation.
	 *
	 * @param modelName the name of the document model
	 * @param path the path to the field within the model
	 * @param field the field definition from the model
	 * @param effectiveFieldType the effective field type after resolution
	 * @param modelFieldEntityBuilder the builder for the model field entity being constructed
	 * @param localizedFieldEntities map of localized field entities by locale and field path
	 */
	public void customizeModelFields(String modelName, String path, IField field, IFieldType effectiveFieldType,
		ModelFieldEntity.ModelFieldEntityBuilder modelFieldEntityBuilder, Map<String, Map<String, String>> localizedFieldEntities) {

		if (hasCustomizers()) {
			ModelFieldsContextImpl ctx =
				new ModelFieldsContextImpl(modelName, path, field, effectiveFieldType, modelFieldEntityBuilder, localizedFieldEntities);
			customizers.forEach(searchCustomizer -> searchCustomizer.customizeModelFields(ctx));
		}
	}

	/**
	 * Checks whether any search customizers are registered.
	 *
	 * @return true if at least one customizer is registered, false otherwise
	 */
	public boolean hasCustomizers() {
		return hasCustomizers;
	}
}
