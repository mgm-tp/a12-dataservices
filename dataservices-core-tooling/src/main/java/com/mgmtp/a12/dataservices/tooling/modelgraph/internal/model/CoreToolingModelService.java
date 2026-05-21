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
package com.mgmtp.a12.dataservices.tooling.modelgraph.internal.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
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

@RequiredArgsConstructor
@Component public class CoreToolingModelService implements ModelService {

	private final HeaderParser headerParser;

	@Getter private final Map<Header, String> models = new TreeMap<>(Comparator.comparing(Header::getId));

	@Override public GenericModel create(@NonNull String modelContent) {
		try {
			Header header = headerParser.parseJson(modelContent);
			if (header == null || header.getId() == null) {
				throw new HeaderParseException("Model header is missing ID.");
			}
			if (models.containsKey(header)) {
				throw new IntegrityException(String.format("Model %s added multiple times.", header.getId()));
			}
			return GenericModel.of(header, models.put(header, modelContent));
		} catch (HeaderParseException e) {
			throw new InvalidInputException(e.getMessage(), e).withAnonymityMessage("Model could not be created.");
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
			.orElseThrow(() -> new NotFoundException(String.format("Model %s can not be found", modelId)));
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
		return null;
	}

	private List<Header> getAllHeadersByTypeInsecure(String type) {
		return models.keySet().stream()
			.filter(h -> type.equals(h.getModelType())).toList();
	}
}
