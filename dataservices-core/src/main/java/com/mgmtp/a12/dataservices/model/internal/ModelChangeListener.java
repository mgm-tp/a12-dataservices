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

import java.util.Objects;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelConstants;
import com.mgmtp.a12.dataservices.model.events.AbstractModelEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.model.header.Header;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j @RequiredArgsConstructor
@Component class ModelChangeListener {

	private final ModelCacheManager modelCacheManager;
	private final DataServicesModelCodeCache modelCodeCache;
	private final UniqueConstraintValidator uniqueConstraintValidator;

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@CommonDataServicesEventListener public void listenOnBulkImportEvent(ModelsAfterImportEvent event) {
		modelCacheManager.invalidateModelGraphCaches();
		modelCacheManager.invalidateDocumentModelSearchServiceCache();
		event.getImportedModels().stream()
			.filter(Objects::nonNull)
			.filter(header -> ModelConstants.DOCUMENT_MODEL_TYPE.equals(header.getModelType()))
			.forEach(header -> {
				String documentModelId = header.getId();
				modelCacheManager.invalidateValidationCodeCacheForDocumentModel(documentModelId);
				modelCodeCache.remove(documentModelId);
			});
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@CommonDataServicesEventListener public void listenOnModelCreated(ModelAfterCreateEvent event) {
		modelCacheManager.invalidateModelGraphCaches();
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@CommonDataServicesEventListener({ ModelBeforeUpdateEvent.class, ModelBeforeDeleteEvent.class })
	public void listenBeforeModelUpdated(AbstractModelEvent<GenericModel> event) {
		// we clean up modelCodeCache in "before" events because the user may add their custom encryption for model, at the time we reach "after" events,
		// data has been encrypted,  and we don't know how to decrypt it to get model id
		Header header = event.getModel().getHeader();
		if (ModelConstants.DOCUMENT_MODEL_TYPE.equals(header.getModelType())) {
			modelCodeCache.remove(header.getId());
		}
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@CommonDataServicesEventListener(ModelBeforeDeleteEvent.class)
	public void listenBeforeModelDeleted(ModelBeforeDeleteEvent event) {
		Header header = event.getModel().getHeader();
		if (ModelConstants.DOCUMENT_MODEL_TYPE.equals(header.getModelType())) {
			uniqueConstraintValidator.deleteByModel(header.getId());
		}
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@CommonDataServicesEventListener({ ModelAfterUpdateEvent.class, ModelAfterDeleteEvent.class })
	public void listenOnModelUpdated(AbstractModelEvent<GenericModel> event) {
		String modelName = event.getModel().getHeader().getId();
		modelCacheManager.invalidateModelGraphCaches();
		modelCacheManager.invalidateValidationCodeCacheForDocumentModel(modelName);
		modelCacheManager.invalidateSecuredModelReadCaches(modelName);
		modelCacheManager.invalidateDocumentModelSearchServiceCache();
		modelCacheManager.invalidateModelReadCaches(modelName);
	}

}
