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
package com.mgmtp.a12.dataservices.relationship.model;

import com.mgmtp.a12.model.header.Header;

import java.io.Reader;
import lombok.NonNull;

/**
 * Serializer interface for {@link RelationshipModel}.
 */
public interface RelationshipModelSerializer {

	/**
	 * Deserializes a relationship model from text content and its associated header.
	 *
	 * @param modelContent Reader providing the serialized model content; must not be null.
	 * @param header Header metadata for the model (id, type, etc.); must not be null.
	 * @return A {@link RelationshipModel} instance reconstructed from the content.
	 */
	RelationshipModel deserialize(@NonNull Reader modelContent, @NonNull Header header);

	/**
	 * Serializes the given relationship model into a textual representation.
	 *
	 * @param relationshipModel The model to serialize; must not be null.
	 * @return The serialized model content as a string.
	 */
	String serialize(@NonNull RelationshipModel relationshipModel);
}
