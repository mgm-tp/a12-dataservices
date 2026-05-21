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
package com.mgmtp.a12.dataservices.relationship.migrator.model;

/**
 * @deprecated As of release 38.1.0, replaced by migration done in npm package.
 *
 * @param <T> Source model representation type.
 * @param <R> Target model representation type.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
public interface IRelationshipModelMigrator<T, R> {

	/**
	 * Returns the logical model name of the source.
	 *
	 * @param source The source model to inspect; must not be null.
	 * @return The model name extracted from the source.
	 */
	String getModelName(T source);

	/**
	 * Returns the version string of the source representation.
	 *
	 * @param source The source model to inspect; must not be null.
	 * @return The source version (e.g., "1.0.0").
	 */
	String getVersion(T source);

	/**
	 * Migrates the given source to the target representation.
	 *
	 * @param source The source representation to migrate; must not be null.
	 * @return The migrated target representation.
	 */
	R migrateModel(T source);

	/**
	 * Returns the class token for the supported source type.
	 *
	 * @return The source type {@link Class}.
	 */
	Class<T> getSourceType();
}
