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
import type { Header } from "@com.mgmtp.a12.base/base-model-api";

export interface RelationshipModel {
	readonly header: Header;
	readonly content: Content;
}

interface Content {
	readonly displayLabel?: DisplayLabel[] | null;
	readonly associationType: AssociationType;
	readonly linkDocumentModel?: string | null;
	readonly duplicatesAllowed: boolean;
	readonly entityCharacteristics: EntityCharacteristics[];
	readonly storage: Storage;
	readonly embeddedGroupPath?: string | null;
}

export interface DisplayLabel {
	readonly language?: string | null;
	readonly value?: string | null;
}

enum AssociationType {
	OWNED = "OWNED",
	SHARED = "SHARED"
}

export interface EntityCharacteristics {
	readonly role: string;
	readonly displayLabel?: DisplayLabel[] | null;
	readonly documentModel: string;
	readonly ordered: boolean;
	readonly navigable?: boolean | null;
	readonly linkConstraints: LinkConstraints;
	readonly candidateConstraints?: CandidateConstraints | null;
}

interface LinkConstraints {
	readonly multiplicity: Multiplicity;
}

interface Multiplicity {
	readonly lowerLimit: number;
	readonly upperLimit?: number | null;
	readonly unbounded: boolean;
}

interface CandidateConstraints {
	readonly population?: string | null;
	readonly populationParameters?: PopulationParameters[] | null;
}

interface PopulationParameters {
	readonly name: string;
	readonly value: string;
}

enum Storage {
	EMBEDDED = "EMBEDDED",
	EXTERNAL = "EXTERNAL"
}
