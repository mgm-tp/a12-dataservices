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
import type {
	DisplayLabel,
	EntityCharacteristics as EntityCharacteristicsV2,
	RelationshipModel as RelationshipModelV2
} from "../version-2.0.0/relationship-model.js";

import type {
	EntityCharacteristics as EntityCharacteristicsV3,
	Labels,
	RelationshipModel as RelationshipModelV3
} from "./relationship-model.js";

export default function migrateRelationshipModelV2ToV3(
	model: RelationshipModelV2
): RelationshipModelV3 {
	const {
		displayLabel,
		entityCharacteristics,
		...modelContentWithoutDisplayLabelAndCharacteristics
	} = model.content;

	return {
		...model,
		content: {
			labels: migrateDisplayLabelToLabels(displayLabel),
			entityCharacteristics: migrateEntityCharacteristics(entityCharacteristics),
			...modelContentWithoutDisplayLabelAndCharacteristics
		}
	};
}

function migrateDisplayLabelToLabels(
	displayLabels: DisplayLabel[] | undefined | null
): Labels[] | undefined {
	return displayLabels?.map(displayLabel => {
		return {
			locale: displayLabel.language,
			text: displayLabel.value
		};
	});
}

function migrateEntityCharacteristics(
	entityCharacteristics: EntityCharacteristicsV2[]
): EntityCharacteristicsV3[] {
	return entityCharacteristics.map(characteristics => {
		const { displayLabel, ...entityCharacteristicsWithoutDisplayLabels } = characteristics;

		return {
			labels: migrateDisplayLabelToLabels(displayLabel),
			...entityCharacteristicsWithoutDisplayLabels
		};
	});
}
