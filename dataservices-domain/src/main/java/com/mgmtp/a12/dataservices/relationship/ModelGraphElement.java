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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mgmtp.a12.model.header.Label;
import com.mgmtp.a12.model.header.ModelReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model Graph Element class. This class is only public because it is used in the public API of {@link ModelGraphRoot} that is used in the java client.
 *
 */
@Data @NoArgsConstructor
public class ModelGraphElement implements Serializable {

	private String modelId;

	@JsonDeserialize(contentAs = SerializableLabel.class)
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private List<Label> displayLabels;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonDeserialize(contentAs = SerializableModelReference.class)
	private List<ModelReference> modelReferences;

	/**
	 * Creates a model graph element with optional labels and references.
	 *
	 * @param modelId unique identifier of the model; never null.
	 * @param displayLabels localized labels to display; may be null or empty.
	 * @param modelReferences references to other models; may be null or empty.
	 */
	public ModelGraphElement(String modelId, List<Label> displayLabels, List<ModelReference> modelReferences) {
		this.modelId = modelId;
		this.modelReferences = modelReferences;
		if (displayLabels != null) {
			this.displayLabels = displayLabels.stream()
				.map(l -> new SerializableLabel(l.getLocale(), l.getText()))
				.collect(Collectors.toList());
		}
	}

	@Data @AllArgsConstructor
	private static class SerializableLabel implements Serializable, Label {
		private Locale locale;
		private String text;
	}

	@JsonIgnoreProperties({ "purpose", "alias" })
	@Data @AllArgsConstructor
	private static class SerializableModelReference implements Serializable, ModelReference {
		private String modelType;
		private String reference;
		private String purpose;
		private String alias;
	}
}
