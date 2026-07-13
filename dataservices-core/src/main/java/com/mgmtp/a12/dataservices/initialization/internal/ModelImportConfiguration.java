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
package com.mgmtp.a12.dataservices.initialization.internal;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Builder;
import lombok.NonNull;

/**
 * Configuration for model import during application initialization.
 * Holds paths, model types, and overwrite settings used by {@link BusinessModelInitializer}.
 */
@Builder
public class ModelImportConfiguration {
	private final Map<String, Boolean> overwriteModels = new TreeMap<>();
	private boolean overwriteDocumentModels = true;
	private boolean overwriteModelsDefault = true;
	private String[] paths;
	private List<String> modelTypes;

	/**
	 * Returns the default overwrite mode applied to model types that are not explicitly configured.
	 *
	 * @return true if models are overwritten by default; false otherwise.
	 */
	public boolean getOverwriteModelsDefault() {
		return overwriteModelsDefault;
	}

	/**
	 * Sets the default overwrite mode for model types without explicit configuration.
	 *
	 * @param mode true to overwrite models by default; false to keep existing models unless overridden per type.
	 */
	public void setOverwriteModelsDefault(boolean mode) {
		this.overwriteModelsDefault = mode;
	}

	/**
	 * Resolves whether models of the given type should be overwritten during import.
	 *
	 * @param type The model type key; must not be null.
	 * @return true if models of the given type are overwritten; false otherwise.
	 */
	public boolean getOverwriteModel(String type) {
		return overwriteModels.getOrDefault(type, getOverwriteModelsDefault());
	}

	/**
	 * Assigns an explicit overwrite mode for a specific model type.
	 *
	 * @param type The model type key; must not be null.
	 * @param mode true to allow overwriting; false to prevent it.
	 */
	public void setOverwriteModels(String type, boolean mode) {
		overwriteModels.put(type, mode);
	}

	/**
	 * Returns whether document models specifically should be overwritten.
	 *
	 * @return true if document model overwriting is enabled.
	 */
	public Boolean getOverwriteDocumentModels() {
		return overwriteDocumentModels;
	}

	/**
	 * Sets whether document models should be overwritten during import.
	 *
	 * @param overwriteDocumentModels true to enable overwriting document models.
	 */
	public void setOverwriteDocumentModels(Boolean overwriteDocumentModels) {
		this.overwriteDocumentModels = overwriteDocumentModels;
	}

	/**
	 * Returns a diagnostic string representation of the current configuration.
	 *
	 * @return A human-readable summary of overwrite modes and document model settings.
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + "{ ");
		sb.append("overwriteModelsDefault=%s, ".formatted(getOverwriteModelsDefault()));
		sb.append("overwriteDocumentModels=%s".formatted(getOverwriteDocumentModels()));
		for (String prop : overwriteModels.keySet()) {
			sb.append(", overwriteModels.%s=%s".formatted(prop, getOverwriteModel(prop)));
		}
		sb.append(" }");
		return sb.toString();
	}

	/**
	 * Initializes this configuration from another instance.
	 *
	 * @param configuration Source configuration whose values are copied; must not be null.
	 */
	public void initialize(@NonNull ModelImportConfiguration configuration) {
		setOverwriteDocumentModels(configuration.getOverwriteDocumentModels());
		setOverwriteModels(configuration.overwriteModels);
		setOverwriteModelsDefault(configuration.getOverwriteModelsDefault());
	}

	/**
	 * Bulk-assigns overwrite modes per model type.
	 *
	 * @param modelOverwrite Map of model type to overwrite mode; may be null to leave unchanged.
	 */
	public void setOverwriteModels(Map<String, Boolean> modelOverwrite) {
		if (modelOverwrite == null) {
			return;
		}
		modelOverwrite.forEach(this::setOverwriteModels);
	}

	/**
	 * Returns the path list used to scope resource discovery during import.
	 *
	 * @return An array of root paths to scan; may be null if not configured.
	 */
	public String[] getPaths() {
		return paths;
	}

	/**
	 * Sets the root paths used to locate models for import.
	 *
	 * @param paths Array of root paths (filesystem or classpath) to scan; may be null to clear.
	 */
	public void setPaths(String[] paths) {
		this.paths = paths;
	}

	/**
	 * Returns the model types to include when importing.
	 *
	 * @return A list of model type keys; may be null if not restricted.
	 */
	public List<String> getModelTypes() {
		return modelTypes;
	}

	/**
	 * Sets the model types to include during import.
	 *
	 * @param modelTypes List of model type keys to include; may be null to clear the restriction.
	 */
	public void setModelTypes(List<String> modelTypes) {
		this.modelTypes = modelTypes;
	}
}
