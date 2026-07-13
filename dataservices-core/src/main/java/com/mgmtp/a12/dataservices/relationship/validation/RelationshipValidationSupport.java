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
package com.mgmtp.a12.dataservices.relationship.validation;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;

/**
 * Support interface for relationship link validation.
 *
 * Provides the validation contract used when creating relationship links.
 *
 * Customer projects can provide their own implementation of this interface as a Spring bean
 * (for example, a no-operation implementation to skip validation during bulk import).
 * Data Services auto-configuration creates the default validation bean only when no other bean
 * of this type is present in the application context.
 *
 * Example of a no-operation implementation:
 *
 * @see LinkDescriptor
 */
public interface RelationshipValidationSupport {

	/**
	 * Validates a relationship link before it is created.
	 *
	 * @param linkDescriptor the descriptor of the link to validate; must not be `null`
	 * @param linkDocRef     the document reference for the link document; may be `null`
	 * @throws com.mgmtp.a12.dataservices.relationship.exception.RelationshipInvalidLinkException
	 *         if the link descriptor is structurally invalid
	 */
	void validateLink(LinkDescriptor linkDescriptor, DocumentReference linkDocRef);
}
