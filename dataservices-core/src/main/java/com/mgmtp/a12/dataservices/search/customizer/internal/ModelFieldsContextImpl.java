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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.LocalizedFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.search.customizer.ModelFieldsContext;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class ModelFieldsContextImpl implements ModelFieldsContext {

	private final String modelName;
	private final String path;
	private final IField field;
	private final IFieldType effectiveFieldType;
	private final ModelFieldEntity.ModelFieldEntityBuilder modelFieldEntityBuilder;
	private final Map<String, Map<String, LocalizedFieldEntity>> localizedFieldEntities;

	@Override public String getOriginalLocalizedValue(String locale, String key) {
		return Optional.of(localizedFieldEntities)
			.map(locales -> locales.get(locale))
			.map(keys -> keys.get(key))
			.map(LocalizedFieldEntity::getLocalizedValue)
			.orElse(null);
	}

	@Override public ModelFieldsContext putLocalizedValue(String locale, String key, String value) {
		localizedFieldEntities.getOrDefault(locale, new HashMap<>()).getOrDefault(key, LocalizedFieldEntity.builder().build()).setLocalizedValue(value);
		return this;
	}

	@Override public ModelFieldsContext setFieldValue(String fieldType, JsonNode data) {
		if (fieldType != null) {
			modelFieldEntityBuilder.fieldType(fieldType);
		}
		if (data != null) {
			modelFieldEntityBuilder.data(data);
		}
		return this;
	}
}
