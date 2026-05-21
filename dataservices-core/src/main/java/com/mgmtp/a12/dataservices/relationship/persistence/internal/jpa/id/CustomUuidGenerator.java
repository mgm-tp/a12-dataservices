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
package com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.id;

import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;

@Component public class CustomUuidGenerator implements IdentifierGenerator {

	@Override public Object generate(SharedSessionContractImplementor session, Object object) {
		if (object instanceof RelationshipLinkEntity entity && entity.getId() != null) {
			return entity.getId();
		}
		if (object instanceof RelationshipRoleEntity entity && entity.getId() != null) {
			return entity.getId();
		}
		return UUID.randomUUID().toString();
	}

	@Override
	public boolean allowAssignedIdentifiers() {
		return true;
	}

}
