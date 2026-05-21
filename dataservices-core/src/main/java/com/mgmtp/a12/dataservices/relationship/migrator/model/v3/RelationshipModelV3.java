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
package com.mgmtp.a12.dataservices.relationship.migrator.model.v3;

import java.util.List;

import com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelV1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.relationship.model.RelationshipModel.RELATIONSHIP_MODEL_TYPE;

/**
 * @deprecated Use `dataservices-relationship-model-migration` npm package instead.
 *
 * Structure of relationship model divided into header and content.
 */
@Data @AllArgsConstructor @NoArgsConstructor
@Deprecated(since = "38.1.0", forRemoval = true)
public class RelationshipModelV3 {

	private RelationshipModelHeaderV3 header;
	private RelationshipModelContentV3 content;

	/**
	 * Localized label entry (V3).
	 */
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class LabelV3 {
		private String locale;
		private String text;
	}

	/**
	 * Content part of the relationship model (V3).
	 */
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class RelationshipModelContentV3 {

		private String associationType;
		private String linkDocumentModel;
		private Boolean duplicatesAllowed;
		private String storage;
		private String embeddedGroupPath;
		private List<LabelV3> labels;
		private List<EntityCharacteristicsV3> entityCharacteristics;

		/**
		 * Entity characteristics associated with the relationship (V3).
		 */
		@Data @NoArgsConstructor @AllArgsConstructor
		public static class EntityCharacteristicsV3 {

			private String role;
			private String documentModel;
			private Boolean ordered;
			private Boolean navigable;
			private RelationshipModelV1.EntityCharacteristicsV1.LinkConstraintsV1 linkConstraints;
			private RelationshipModelV1.EntityCharacteristicsV1.CandidateConstraintsV1 candidateConstraints;
			private List<LabelV3> labels = List.of();

		}
	}

	/**
	 * Header part of the relationship model (V3).
	 */
	@Data @NoArgsConstructor @AllArgsConstructor
	public static class RelationshipModelHeaderV3 {

		public static final String V3_MODEL_VERSION = "3.0.0";

		private final String modelType = RELATIONSHIP_MODEL_TYPE;
		private final String modelVersion = V3_MODEL_VERSION;
		private String id;
		private List<LabelV3> labels = List.of();
		private List<AnnotationV3> annotations = List.of();
		private List<LocaleCodeV3> locales = List.of();
		private List<ModelReferenceV3> modelReferences = List.of();

		/**
		 * Annotation entry in the header (V3).
		 */
		@Data @AllArgsConstructor @NoArgsConstructor
		public static class AnnotationV3 {
			private String name;
			private String value;
		}

		/**
		 * Locale code entry (V3).
		 */
		@Data @AllArgsConstructor @NoArgsConstructor
		public static class LocaleCodeV3 {

			private String code;
		}

		/**
		 * Reference to a related model (V3).
		 */
		@Data @AllArgsConstructor @NoArgsConstructor
		public static class ModelReferenceV3 {
			private String purpose;
			private String modelType;
			private String alias;
			private String reference;
		}
	}
}
