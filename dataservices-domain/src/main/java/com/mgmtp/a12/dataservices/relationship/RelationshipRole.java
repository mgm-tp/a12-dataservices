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

import com.mgmtp.a12.dataservices.document.DocumentReference;

/**
 * Every relationship has 2 roles, one for each side of the relationship. This interface represents such a role.
 * The directionality of the relationship is defined by the roles. For example, in a parent-child relationship, one role
 * would be "parent" and the other "child". If client needs a child document, it would query for documents that have
 * targetRole of "child". The sourceRole would be "parent" in this case.
 */
public interface RelationshipRole extends Serializable {

	/**
	 *
	 * @return the name of the role
	 */
	String getName();

	/**
	 *
	 * @return the document reference of the role
	 */
	DocumentReference getDocRef();

	/**
	 *
	 * @return the order of the role
	 */
	String getOrder();

	/**
	 *
	 * @param order the order to set
	 */
	void setOrder(String order);
}
