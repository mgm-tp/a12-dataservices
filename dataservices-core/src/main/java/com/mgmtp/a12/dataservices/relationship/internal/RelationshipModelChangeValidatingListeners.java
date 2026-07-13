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
package com.mgmtp.a12.dataservices.relationship.internal;

import java.io.StringReader;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeCreateEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeDeleteEvent;
import com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

import tools.jackson.databind.ObjectMapper;

import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.META_MODEL_NAME;

/**
 *
 * Relationship models have further validation requirements than there are specified in the DefaultModelRepository. Those requirements are validated here
 * with the usage of Events. Other option would be to create specific RelationshipModelReadRepository but it would contain a lot of duplicated code. Any further
 * relationship model validation should be added here if it can be solved by events
 */
@Component class RelationshipModelChangeValidatingListeners {

	@Autowired private DefaultRelationshipLinkService relationshipLinkService;
	@Autowired private KernelDocumentService kernelDocumentService;
	@Autowired private DocumentDeserializationConfig documentJsonDeserializationConfig;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private IDocumentV2Serializer documentV2Serializer;
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;

	/**
	 * Method validates Relationship Model before updating.
	 * This listener has the lowest precedence because validating model from `ModelBeforeUpdateEvent` should be performed lastly,
	 * this will make sure validation is also applied on any changes client project has made on `ModelBeforeUpdateEvent`.
	 *
	 * @param event contains old and new Models.
	 */
	@Order
	@CommonDataServicesEventListener public void relationshipModelModified(ModelBeforeUpdateEvent event) {
		validateModel(event.getModel());
	}

	/**
	 * Method validates Relationship Model before creating.
	 * This listener has the lowest precedence because validating model from `ModelBeforeCreateEvent` should be performed lastly,
	 * this will make sure validation is also applied on any changes client project has made on `ModelBeforeCreateEvent`.
	 *
	 * @param event Contains new Header and new Model Content.
	 */
	@Order
	@CommonDataServicesEventListener public void relationshipModelCreated(ModelBeforeCreateEvent event) {
		validateModel(event.getModel());
	}

	/**
	 * Method validates if links from relationship model are still exist or not.
	 * This listener has the lowest precedence because validating model from `ModelBeforeDeleteEvent` should be performed lastly,
	 * this will make sure validation is also applied on any changes client project has made on `ModelBeforeDeleteEvent`.
	 *
	 * @param event contains deleting Model content.
	 */
	@Order
	@CommonDataServicesEventListener public void relationshipModelDeleted(ModelBeforeDeleteEvent event) {
		if (dataServicesCoreProperties.getModels().getRelationship().getSafeDelete().isEnabled()) {
			Model model = event.getModel();
			if (isRelationshipModel(model) && relationshipLinkService.countByRelationshipModel(model.getHeader().getId()) > 0) {
				throw new InvalidInputException(ExceptionKeys.RELATIONSHIP_MODEL_DELETE_LINK_EXISTS_ERROR_KEY,
					"Relationship model %s cannot be deleted because links for that model exist in the persistent store".formatted(
						model.getHeader().getId()));
			}
		}
	}

	private void validateModel(Model model) {
		if (dataServicesCoreProperties.getModels().getRelationship().getValidation().isEnabled()) {
			Optional.of(model)
				.filter(this::isRelationshipModel)
				.ifPresent(m -> {
					validateRelationshipModelVersion(m.getHeader());
					validateRelationshipModel(m);
				});
		}
	}

	private void validateRelationshipModel(Model relationshipModel) {
		Header header = relationshipModel.getHeader();
		try {
			String modelContent = objectMapper.writeValueAsString(relationshipModel.getContent());
			ListIProblemReporter pr = new ListIProblemReporter();
			DocumentV2 relationshipMetaModel =
				documentV2Serializer.deserializeV2(new StringReader(modelContent), META_MODEL_NAME, documentJsonDeserializationConfig, pr);
			IDocumentValidationResult results = kernelDocumentService.validateFull(relationshipMetaModel, null);

			if (!results.noErrorOccurred()) {
				throw new InvalidInputException(ExceptionKeys.MODEL_VALIDATION_ERROR_KEY,
					"Relationship model [%s] is not valid:%n%s".formatted(header.getId(), results.getMessages())).withAnonymityMessage(
					"Relationship model is not valid.");
			}
		} catch (DocumentSerializationException e) {
			throw new InvalidInputException(ExceptionKeys.MODEL_VALIDATION_ERROR_KEY, "Relationship model [%s] is not valid".formatted(header.getId()), e);
		}
	}

	private void validateRelationshipModelVersion(Header header) {
		if (!RelationshipModel.VERSION.equals(header.getModelVersion())) {
			throw new InvalidInputException(ExceptionKeys.MODEL_VERSION_MISMATCH_ERROR_KEY,
				"Currently supported version for relationship models is %s".formatted(RelationshipModel.VERSION));
		}
	}

	private boolean isRelationshipModel(Model model) {
		return RelationshipModel.RELATIONSHIP_MODEL_TYPE.equalsIgnoreCase(model.getHeader().getModelType());
	}
}
