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
 * The event is published after the model is updated.
 *
 * @topic Models events
 */
@EqualsAndHashCode(callSuper = true) @Data
@EventDocumentation public final class ModelAfterUpdateEvent extends AbstractModelEvent<GenericModel> {

	private final Model oldModel;

	/**
	 * Constructs an event signaling that a model has been updated.
	 *
	 * @param oldModel The previous persisted state before the update; must not be null.
	 * @param model The updated {@link GenericModel} state; must not be null.
	 */
	public ModelAfterUpdateEvent(GenericModel oldModel, GenericModel model) {
		super(model);
		this.oldModel = oldModel;
	}

	/**
	 * Gets the previous persisted state before the update.
	 *
	 * @return The old model; never null.
	 */
	public Model getOldModel() {
		return oldModel;
	}

}
