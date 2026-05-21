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
package com.mgmtp.a12.dataservices.relationship;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mgmtp.a12.dataservices.marshalling.JsonRawValuesSetDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Generic element of the model graph.
 */
@Getter @ToString @EqualsAndHashCode @AllArgsConstructor @NoArgsConstructor @Builder
public class ModelGraphRoot implements Serializable {

	/**
	 * All relevant document models except CDM's are in this section. CDM's are in {@link #composeDocumentModels}
	 */
	@JsonDeserialize(as = HashSet.class)
	@Setter private Set<ModelGraphDocumentModelElement> documentModels;

	/**
	 * All relevant CDMs are in this section.
	 */
	@JsonSerialize(contentAs = ModelGraphComposeDocumentModelElement.class)
	@JsonDeserialize(as = HashSet.class, contentAs = ModelGraphComposeDocumentModelElement.class)
	@Setter private Set<ModelGraphElement> composeDocumentModels;

	/**
	 * All other relevant models except the ones supported by Data Services (no Document models and Relationship models here) are in this section.
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Set<ModelGraphOtherModelElement> genericModels;

	/**
	 * All relevant Relationship models are in this section.
	 */
	@JsonRawValue @JsonDeserialize(using = JsonRawValuesSetDeserializer.class, as = HashSet.class)
	private Set<String> relationshipModels;


	/**
	 * Builder for {@link ModelGraphRoot}.
	 * Provides convenience methods to initialize complex properties.
	 */
	public static class ModelGraphRootBuilder {
		private Set<String> relationshipModels;

		/**
		 * Initializes relationship model IDs from a map of model name to ID.
		 *
		 * @param relationshipModelMap map of model names to their IDs; must not be null.
		 * @return this builder for method chaining.
		 */
		public ModelGraphRootBuilder relationshipModelMap(Map<String, String> relationshipModelMap) {
			this.relationshipModels = new HashSet<>(relationshipModelMap.values());
			return this;
		}
	}
}
