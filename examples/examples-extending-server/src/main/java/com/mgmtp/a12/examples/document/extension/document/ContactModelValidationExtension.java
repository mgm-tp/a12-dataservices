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
package com.mgmtp.a12.examples.document.extension.document;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that represents the collection of event listeners utilizing mocked address validators or 3rd party address validators.
 * The goal of this class is to listen for event before creating or modifying documents of model type ContactModel.
 * In case of invalid document, e.g. missing field, custom exception will be thrown.
 *
 * Used Document's events:
 *
 * - DocumentBeforeCreateEvent
 * - DocumentBeforeUpdateEvent
 *
 * Available Document's events that can be listened to:
 *
 * - DocumentAfterControllerLoadEvent
 * - DocumentAfterCreateEvent
 * - DocumentAfterDeleteEvent
 * - DocumentAfterLoadEvent
 * - DocumentAfterRepositoryLoadEvent
 * - DocumentAfterUpdateEvent
 * - DocumentBeforeDeleteEvent
 * - DocumentBeforeIndexEvent
 * - DocumentBeforeRepositorySaveEvent
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.extension.document", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Component public class ContactModelValidationExtension {

	private final AddressValidator addressValidator;

	/**
	 * Validates the address before creating a document of model `ContactModel`.
	 *
	 * @param event the create event carrying the document to be persisted; never null.
	 */
	@CommonDataServicesEventListener
	public void beforeCreateListener(DocumentBeforeCreateEvent event) {
		addressValidator.validatedAddress(event.getCreatedDocument());
	}

	/**
	 * Validates the address before updating a document of model `ContactModel.
	 *
	 * @param event the update event carrying the modified document; never null.
	 */
	@CommonDataServicesEventListener
	public void beforeUpdateListener(DocumentBeforeUpdateEvent event) {
		addressValidator.validatedAddress(event.getUpdatedDocument());
	}

}

