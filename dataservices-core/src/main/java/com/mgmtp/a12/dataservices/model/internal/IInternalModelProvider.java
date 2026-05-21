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

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

/**
 * Interface allowing to inject internal models into {@link com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentModelResolver}.
 * It's used to provide the relationship metamodel, document metadata model, and attachment metadata model.
 * Every system provided model could be provided using this mechanism.
 * Internal models provided this way have precedence before user provided models.
 */
public interface IInternalModelProvider {

	/**
	 * If this method returns true, then the model of this provider is returned.
	 *
	 * @param modelName modelname to ask for.
	 * @return true, if implementation of this interface should provide the model instead of default behavior.
	 */
	boolean supports(String modelName);

	/**
	 * Returns the model, if {@link #supports(String)} of this implementation returns true.
	 * This method is always called from {@link com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentModelResolver},
	 * so it should be performant.
	 * It's recommended
	 * to use {@link AbstractInternalModelProvider} and implement just {@link AbstractInternalModelProvider#getModelName()} and {@link AbstractInternalModelProvider#getModelPath()},
	 * or overwrite {@link AbstractInternalModelProvider#init()}.
	 *
	 * @return the model to be returned.
	 */
	IDocumentModel getModel();
}
