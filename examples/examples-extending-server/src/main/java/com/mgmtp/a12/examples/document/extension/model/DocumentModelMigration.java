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
package com.mgmtp.a12.examples.document.extension.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.model.GenericModelContent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterRepositoryLoadEvent;
import com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeRepositorySaveEvent;
import com.mgmtp.a12.model.Content;
import com.mgmtp.a12.model.Model;

import lombok.extern.slf4j.Slf4j;

/**
 * Class containing example listener to modify document models after loading from repository but before de-serialization.
 *
 * Implementation details:
 * The event `ModelAfterRepositoryLoadEvent` is published right after having received the model from the repository.
 * After execution of the code of this `CommonDataServicesEventListener` the modified model is stored in `ModelAfterRepositoryLoadEvent`
 * and thus passed back to the `IModelLoader` implementation where it can be de-serialized and returned to the client.
 * No changes are made to the data inside the repository, instead this modification is done repeatedly on each model load.
 *
 * As an example the 'migration' consists of decoding base64 encoded models. To accomplish this and to enable model load during
 * tests all models are base64 encoded before save/update.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.extension.model", name = "enabled", havingValue = "true")
@Slf4j
@Component public class DocumentModelMigration {
	public static final String DOCUMENT_MODEL_TYPE = "document";

	/**
	 * Decodes Base64-encoded model content right after loading from the repository.
	 *
	 * @param modelAfterRepositoryLoadEvent the event containing raw model content and metadata; content may be null.
	 * @throws HeaderParseException if header parsing fails during downstream processing.
	 */
	@CommonDataServicesEventListener public void decryptModelAfterLoad(ModelAfterRepositoryLoadEvent modelAfterRepositoryLoadEvent) {
		String content = modelAfterRepositoryLoadEvent.getModelEntityContent();
		String modelType = modelAfterRepositoryLoadEvent.getModelType();
		String modelName = modelAfterRepositoryLoadEvent.getModelName();
		if (content != null && DOCUMENT_MODEL_TYPE.equals(modelType)) {
			log.info("Loaded model content of model [{}] of type [{}] from repository. We should decrypt it now.",
				modelName, modelType);
			String decodedContent = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
			modelAfterRepositoryLoadEvent.setModelEntityContent(decodedContent);
			log.info("Model [{}] of type [{}] successfully decoded", modelName, modelType);
		}
	}

	/**
	 * Decodes Base64-encoded model content after an update operation.
	 *
	 * @param modelAfterUpdateEvent the event with the updated model; never null.
	 * @throws HeaderParseException if header parsing fails during downstream processing.
	 */
	@CommonDataServicesEventListener public void decryptModelAfterUpdate(ModelAfterUpdateEvent modelAfterUpdateEvent) {
		modelAfterSaveEvent(modelAfterUpdateEvent.getModel());
	}

	/**
	 * Decodes Base64-encoded model content after a create operation.
	 *
	 * @param modelAfterCreateEvent the event with the created model; never null.
	 * @throws HeaderParseException if header parsing fails during downstream processing.
	 */
	@CommonDataServicesEventListener public void decryptModelAfterLoad(ModelAfterCreateEvent modelAfterCreateEvent) {
		modelAfterSaveEvent(modelAfterCreateEvent.getModel());
	}

	private static void modelAfterSaveEvent(Model modelAfterUpdateEvent) {
		Content modelContent = modelAfterUpdateEvent.getContent();
		String modelType = modelAfterUpdateEvent.getHeader().getModelType();
		String modelName = modelAfterUpdateEvent.getHeader().getId();
		if (modelContent instanceof GenericModelContent genericModelContent) {
			String content = genericModelContent.getRawContent();
			if (content != null && DOCUMENT_MODEL_TYPE.equals(modelType)) {
				log.info("Loaded model content of model [{}] of type [{}] from repository. We should decrypt it now.",
					modelName, modelType);
				String decodedContent = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
				genericModelContent.setRawContent(decodedContent);
				log.info("Model [{}] of type [{}] successfully decoded", modelName, modelType);
			}
		}
	}

	/**
	 * Encodes model content in Base64 just before saving to the repository.
	 *
	 * @param modelBeforeRepositorySaveEvent the event carrying raw model content to be persisted; content may be null.
	 */
	@CommonDataServicesEventListener public void encryptModelBeforeSave(ModelBeforeRepositorySaveEvent modelBeforeRepositorySaveEvent) {
		String content = modelBeforeRepositorySaveEvent.getModelEntityContent();
		String modelType = modelBeforeRepositorySaveEvent.getModelType();
		String modelName = modelBeforeRepositorySaveEvent.getModelName();
		if (content != null && DOCUMENT_MODEL_TYPE.equals(modelType)) {
			log.info("Just before saving model [{}] of type [{}]. We should encrypt it.",
				modelName, modelType);
			String encodedContent = new String(Base64.getEncoder().encode(content.getBytes()), StandardCharsets.UTF_8);
			modelBeforeRepositorySaveEvent.setModelEntityContent(encodedContent);
			log.info("Model [{}] of type [{}] successfully encoded", modelName, modelType);
		}
	}

}
