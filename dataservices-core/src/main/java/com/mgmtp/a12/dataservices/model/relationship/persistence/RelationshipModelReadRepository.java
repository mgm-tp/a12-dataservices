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
package com.mgmtp.a12.dataservices.model.relationship.persistence;

import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.dataservices.model.persistence.AbstractModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesIOUtils;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.RELATIONSHIP_MODEL_TYPE;

/**
 * Repository for reading {@link RelationshipModel} instances from the persistent store.
 * Applies type filtering to ensure only relationship models are returned and uses
 * {@link ModelCacheManager} for transparent caching.
 */
@Repository public class RelationshipModelReadRepository extends AbstractModelReadRepository<RelationshipModel> {

	@Autowired private RelationshipModelSerializer relationshipModelSerializer;

	@Cacheable(value = ModelCacheManager.RELATIONSHIP_MODEL_READ_CACHE)
	@Override public RelationshipModel readModel(@NonNull String modelId) {
		return super.readModel(modelId);
	}

	@Override protected String getModelNotFoundErrorKey() {
		return ExceptionKeys.RELATIONSHIP_MODEL_NOT_FOUND_ERROR_KEY;
	}

	@Override protected String getModelTypeForMessage() {
		return "Relationship model";
	}

	@Override protected RelationshipModel buildModelFromHeaderAndContent(@NonNull Header header, @NonNull String modelContent) {
		return DataServicesIOUtils.readString(modelContent, r -> relationshipModelSerializer.deserialize(r, header));
	}

	@Override protected Predicate<? super ModelHeaderEntity> filterModelsToMatchModelType() {
		return h -> RELATIONSHIP_MODEL_TYPE.equals(h.getModelType());
	}

}
