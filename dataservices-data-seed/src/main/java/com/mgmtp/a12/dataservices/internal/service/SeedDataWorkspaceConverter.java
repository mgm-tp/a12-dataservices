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
package com.mgmtp.a12.dataservices.internal.service;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import com.mgmtp.a12.dataservices.wcf.WorkspaceConverter;
import com.mgmtp.a12.dataservices.wcf.annotations.WcfConverter;
import com.mgmtp.a12.dataservices.wcf.domain.FileTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;

import static com.mgmtp.a12.dataservices.internal.service.SeedDataService.FULL_ATTACHMENT_PATH;
import static com.mgmtp.a12.dataservices.internal.service.SeedDataService.FULL_DOCUMENT_PATH;
import static com.mgmtp.a12.dataservices.internal.service.SeedDataService.FULL_LINK_PATH;
import static com.mgmtp.a12.dataservices.internal.service.SeedDataService.FULL_META_PATH;
import static com.mgmtp.a12.dataservices.internal.service.SeedDataService.FULL_ROLE_PATH;
import static com.mgmtp.a12.dataservices.internal.service.SeedDataService.FULL_USER_PATH;

@WcfConverter(order = 0) public class SeedDataWorkspaceConverter implements WorkspaceConverter {
	@Override public Workspace convert(Workspace workspace) {
		workspace.getFiles().entrySet().stream()
			.filter(f -> f.getValue().getOutputPath() == null)
			.filter(SeedDataWorkspaceConverter::belongsToSeedData)
			.forEach(f -> f.getValue().setOutputPath(f.getKey()));
		return workspace;
	}

	private static boolean belongsToSeedData(Map.Entry<String, FileTuple> f) {
		Path filename = Path.of(f.getKey());
		return Stream.of(FULL_USER_PATH, FULL_ROLE_PATH, FULL_META_PATH, FULL_LINK_PATH, FULL_DOCUMENT_PATH, FULL_ATTACHMENT_PATH)
			.anyMatch(filename::startsWith);
	}
}
