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
package com.mgmtp.a12.dataservices.model.events;

import com.mgmtp.a12.dataservices.common.events.internal.EventDocumentation;

import lombok.Data;

/**
 * The event is published before the model is saved to the repository.
 *
 * @topic Models events
 */
@Data
@EventDocumentation public final class ModelBeforeRepositorySaveEvent {
	
	private String modelType;
	private String modelName;
	private String modelEntityContent;

	/**
	 * Constructs an event published before persisting a model entity to the repository.
	 *
	 * @param modelType The type of the model (e.g., document, relationship); must not be null.
	 * @param modelName The unique identifier of the model; must not be null.
	 * @param modelEntityContent The content to be saved to the repository; must not be null.
	 */
	public ModelBeforeRepositorySaveEvent(String modelType, String modelName, String modelEntityContent) {
		this.modelType = modelType;
		this.modelName = modelName;
		this.modelEntityContent = modelEntityContent;
	}

}
