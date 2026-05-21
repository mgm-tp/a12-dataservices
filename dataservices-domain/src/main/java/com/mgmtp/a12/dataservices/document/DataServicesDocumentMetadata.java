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
package com.mgmtp.a12.dataservices.document;

import java.time.Instant;

/**
 * Metadata proxy to mirror metadata from {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2} using Java methods.
 * It's possible to extend this metadata by custom ones.
 * In this case you must provide your own bean of {@link IDataServicesDocumentMetadataExtractor}.
 */
public interface DataServicesDocumentMetadata {

	/**
	 * Returns the document reference of the current document.
	 *
	 * @return {@link DocumentReference} identifying the document; never null.
	 */
	DocumentReference getDocRef();

	/**
	 * Returns the document model reference string (model identifier).
	 *
	 * @return The model reference; never null.
	 */
	String getDocumentModelReference();

	/**
	 * Returns the document model version.
	 *
	 * @return The model version; never null.
	 */
	String getDocumentModelVersion();

	/**
	 * Returns the creator (user or system) of the document.
	 *
	 * @return Creator identifier; may be null if not tracked.
	 */
	String getCreator();

	/**
	 * Returns the last modifier (user or system) of the document.
	 *
	 * @return Modifier identifier; may be null if not tracked.
	 */
	String getModifier();

	/**
	 * Returns the creation timestamp of the document.
	 *
	 * @return Creation time; may be null if not tracked.
	 */
	Instant getCreatedAt();

	/**
	 * Returns the last modification timestamp of the document.
	 *
	 * @return Modification time; may be null if not tracked.
	 */
	Instant getModifiedAt();
}
