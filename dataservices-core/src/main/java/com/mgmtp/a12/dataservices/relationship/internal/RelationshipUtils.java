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

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.relationship.model.EntityCharacteristics;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component public class RelationshipUtils {

	private final DefaultModelTypeService modelTypeService;

	public boolean isRecursive(RelationshipModel rm) {

		List<String> allParticipatingModels = rm.getContent().getEntityCharacteristics().stream()
			.map(EntityCharacteristics::getDocumentModel)
			.flatMap(documentModelName -> modelTypeService.findModelNameAndAllSubtypes(documentModelName).stream().distinct())
			.toList();

		for (String t : allParticipatingModels) {
			if (Collections.frequency(allParticipatingModels, t) > 1) {
				return true;
			}
		}
		return false;
	}

}
