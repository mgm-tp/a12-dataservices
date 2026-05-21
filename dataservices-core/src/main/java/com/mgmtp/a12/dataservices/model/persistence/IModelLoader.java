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
package com.mgmtp.a12.dataservices.model.persistence;

import org.springframework.security.access.AccessDeniedException;

import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.model.Model;

import lombok.NonNull;

/**
 * Contract for loading a persisted model instance from the Data Services (DS). DS provides 4 implementations of this interface:
 *
 * - {@link com.mgmtp.a12.dataservices.model.persistence.internal.GenericModelLoader} for {@link com.mgmtp.a12.dataservices.model.GenericModel}
 * - {@link com.mgmtp.a12.dataservices.model.document.persistence.internal.DocumentModelLoader} for {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}
 * - {@link com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader} for {@link com.mgmtp.a12.dataservices.relationship.model.RelationshipModel}
 * - {@link com.mgmtp.a12.dataservices.model.cdm.persistence.internal.ComposeDocumentModelLoader} for {@link com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel}
 *
 * Inject the appropriate implementation based on the model type you need to load.
 * Responsibilities / guarantees:
 *
 * - Authorization: implementation MUST verify the current user/principal has read permission for the target model.
 * - Null safety: NEVER returns null; absence or denied access MUST raise a domain runtime exception.
 * - Caching: DS Implementations always transparently cache loaded models; repeated invocations with the same modelId
 *   can leverage this cache. Invalidation is internal and transparent to callers.
 * - Consistency: Returned instance must represent the latest committed state at load time.
 *
 * @param <T> The model type handled by the loader.
 */
public interface IModelLoader<T extends Model> {

	/**
	 * Loads a model instance by its unique identifier.
	 *
	 * @param modelId Unique identifier of the model to load; must not be null.
	 * @return The loaded model instance; never null.
	 * @throws NotFoundException if no model with the given ID exists.
	 * @throws AccessDeniedException if the current user/principal is not allowed to read the target model.
	 */
	T loadModel(@NonNull String modelId);
}
