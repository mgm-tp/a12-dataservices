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
package com.mgmtp.a12.dataservices.relationship.migrator.model.v1;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @deprecated Relationship model migration moved to the npm package.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
@Data @NoArgsConstructor
public class RelationshipModelV1 {

	private String associationType;
	private String linkDocumentModel;
	private Boolean duplicatesAllowed;
	private String storage;
	private String embeddedGroupPath;
	private MetaInformation metaInformation;
	private String name;
	private List<DisplayLabelV1> displayLabel = List.of();
	private List<EntityCharacteristicsV1> entityCharacteristics = List.of();

	/**
	 * Meta information for the V1 relationship model.
	 */
	@Data public static class MetaInformation {
		private String version;
	}

	/**
	 * Localized display label in V1 format.
	 */
	@Data public static class DisplayLabelV1 implements Serializable {
		private String language;
		private String value;
	}

	/**
	 * Entity characteristics of the relationship for the V1 model.
	 */
	@Data public static class EntityCharacteristicsV1 {

		private String role;
		private String documentModel;
		private Boolean ordered;
		private Boolean navigable;
		private LinkConstraintsV1 linkConstraints;
		private CandidateConstraintsV1 candidateConstraints;
		private List<DisplayLabelV1> displayLabel = List.of();

		/**
		 * Candidate constraints specifying selection rules for related entities.
		 */
		@Data public static class CandidateConstraintsV1 {

			private String population;
			private List<PopulationParameters> populationParameters = List.of();

			@Data
			private static class PopulationParameters {
				private String name;
				private String value;
			}

		}

		/**
		 * Link constraints specifying multiplicity and structural rules.
		 */
		@Data public static class LinkConstraintsV1 implements Serializable {
			private MultiplicityV1 multiplicity;

			/**
			 * Multiplicity limits for relationships (lower and upper bounds).
			 */
			@Data public static class MultiplicityV1 implements Serializable {
				private Integer lowerLimit;
				private Boolean unbounded;
				private Integer upperLimit;
			}
		}
	}
}
