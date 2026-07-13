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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.IntegrityException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * File-system-based implementation of {@link ModelService} for use in standalone model graph generation.
 *
 * Models are loaded from files at startup and held in memory.
 * Mutation operations (`update`, `delete`) are not supported and throw {@link UnsupportedOperationException}.
 */
@RequiredArgsConstructor
public class FileBasedModelService implements ModelService {

	private final HeaderParser headerParser;

	@Getter private final Map<Header, String> models = new TreeMap<>(Comparator.comparing(Header::getId));

	@Override public GenericModel create(@NonNull String modelContent) {
		try {
			Header header = headerParser.parseJson(modelContent);
			if (header == null || header.getId() == null) {
				throw new HeaderParseException("Model header is missing ID.");
			}
			if (models.containsKey(header)) {
				throw new IntegrityException("Model %s added multiple times.".formatted(header.getId()));
			}
			return GenericModel.of(header, models.put(header, modelContent));
		} catch (HeaderParseException e) {
			throw new InvalidInputException(ExceptionKeys.MODEL_DESERIALIZATION_ERROR_KEY, e.getMessage(), e).withAnonymityMessage("Model could not be created.");
		}
	}

	@Override public GenericModel update(@NonNull String modelContent) {
		throw new UnsupportedOperationException();
	}

	@Override public boolean delete(@NonNull String modelId) {
		throw new UnsupportedOperationException();
	}

	@Override public GenericModel load(@NonNull String modelId) {
		Map.Entry<Header, String> modelEntry = models.entrySet().stream()
			.filter(e -> Objects.equals(e.getKey().getId(), modelId))
			.findAny()
			.orElseThrow(() -> new NotFoundException(ExceptionKeys.MODEL_NOT_FOUND_ERROR_KEY, "Model %s can not be found".formatted(modelId)));
		return GenericModel.of(modelEntry.getKey(), modelEntry.getValue());
	}

	@Override public Collection<GenericModel> load(@NonNull Collection<String> modelIds) {
		return models.entrySet()
			.stream()
			.filter(entry -> modelIds.contains(entry.getKey().getId()))
			.map(entry -> GenericModel.of(entry.getKey(), entry.getValue()))
			.toList();
	}

	@Override public List<Header> findAllHeadersByType(String type) {
		return getAllHeadersByTypeInsecure(type);
	}

	@Override public Set<Header> findAllHeaders() {
		return models.keySet();
	}

	@Override public boolean exists(@NonNull Header header) {
		throw new UnsupportedOperationException();
	}

	@Override public IModelRepository getSupportingRepository(Header header) {
		throw new UnsupportedOperationException("getSupportingRepository is not implemented");
	}

	private List<Header> getAllHeadersByTypeInsecure(String type) {
		return models.keySet().stream()
			.filter(h -> type.equals(h.getModelType())).toList();
	}
}
