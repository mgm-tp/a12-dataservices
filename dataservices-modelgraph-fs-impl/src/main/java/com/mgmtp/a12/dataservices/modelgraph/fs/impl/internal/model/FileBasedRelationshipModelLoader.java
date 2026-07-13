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

import java.io.StringReader;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.RELATIONSHIP_MODEL_TYPE;

/**
 * File-system-based implementation of {@link RelationshipModelLoader} for use in standalone model graph generation.
 *
 * Loads all relationship models from the in-memory {@link com.mgmtp.a12.dataservices.model.ModelService}.
 * The single-model lookup ({@link #loadModel(String)}) is not supported and throws {@link UnsupportedOperationException}.
 */
@RequiredArgsConstructor
public class FileBasedRelationshipModelLoader implements RelationshipModelLoader {

	private final ModelService modelService;
	private final RelationshipModelSerializer relationshipModelSerializer;

	@Override public Set<RelationshipModel> loadAllRelationshipModels() {
		return modelService.findAllHeadersByType(RELATIONSHIP_MODEL_TYPE).stream()
			.map(header -> modelService.load(header.getId()))
			.map(genericModel -> relationshipModelSerializer.deserialize(new StringReader(genericModel.getContent().getRawContent()), genericModel.getHeader()))
			.collect(Collectors.toSet());
	}

	@Override public RelationshipModel loadModel(@NonNull String relationshipModelName) {
		throw new UnsupportedOperationException();
	}
}
