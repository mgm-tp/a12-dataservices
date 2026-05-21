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
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.model.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The event is published before the model is updated.
 * It is composed of the persisted header, the persisted model content, and the updated model content.
 *
 * @topic Models events
 */
@Data @EqualsAndHashCode(callSuper = true)
@EventDocumentation public final class ModelBeforeUpdateEvent extends AbstractModelEvent<GenericModel> {

	private Model oldModel;

	/**
	 * Constructs an event published before updating a model.
	 *
	 * @param oldModel The current persisted state before applying the update; must not be null.
	 * @param model The new {@link GenericModel} state to be persisted; must not be null.
	 */
	public ModelBeforeUpdateEvent(GenericModel oldModel, GenericModel model) {
		super(model);
		this.oldModel = oldModel;
	}
}
