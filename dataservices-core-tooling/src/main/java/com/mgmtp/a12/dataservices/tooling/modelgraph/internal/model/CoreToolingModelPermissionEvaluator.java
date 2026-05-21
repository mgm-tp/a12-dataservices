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
package com.mgmtp.a12.dataservices.tooling.modelgraph.internal.model;


import org.apache.commons.lang3.NotImplementedException;

import com.mgmtp.a12.dataservices.authorization.AuthConstants;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.tooling.modelgraph.internal.exception.AccessDeniedException;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;

public class CoreToolingModelPermissionEvaluator implements ModelPermissionEvaluator<Model> {

	public static final String METHOD_NOT_IMPLEMENTED = "Method not implemented";

	@Override public void checkModelCreatePermission(Header header) {
		throw new NotImplementedException(METHOD_NOT_IMPLEMENTED);
	}

	@Override public void checkModelUpdatePermission(Header header) {
		throw new NotImplementedException(METHOD_NOT_IMPLEMENTED);
	}

	@Override public void checkModelDeletePermission(Header header) {
		throw new NotImplementedException(METHOD_NOT_IMPLEMENTED);
	}

	@Override public void checkModelReadPermission(Header header) {
		throw new NotImplementedException(METHOD_NOT_IMPLEMENTED);
	}

	@Override public void checkModelReadPermission(String modelId) {
		if (!hasModelReadPermission(modelId)) {
			throw new AccessDeniedException(AuthConstants.ACCESS_DENIED);
		}
	}

	@Override public void checkModelReadPermission(Model model) {
		if (!hasModelReadPermission(model)) {
			throw new AccessDeniedException(AuthConstants.ACCESS_DENIED);
		}
	}

	@Override public boolean hasModelReadPermission(Header header) {
		return true;
	}

	@Override public boolean hasModelReadPermission(String modelId) {
		return true;
	}

	@Override public boolean hasModelReadPermission(Model model) {
		return true;
	}
}
