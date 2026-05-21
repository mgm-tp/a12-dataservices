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
package com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.logical;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;

import lombok.RequiredArgsConstructor;

@QueryOperatorGenerator({ NotOperator.class })
@RequiredArgsConstructor
@Component public class NotOperatorSqlGenerator implements ILogicOperatorGenerator<NotOperator> {

	@Override
	public StringBuilder renderCondition(StringBuilder sb, NotOperator operator, QueryGeneratorContext queryGeneratorContext) {
		sb.append(QueryGeneratorConstants.NOT_OPERATOR).append(QueryGeneratorConstants.OPENING_BRACKET);
		SqlGeneratorHelpersInternal.renderConstraint(sb, operator.getOperand(), queryGeneratorContext, false);
		return sb.append(QueryGeneratorConstants.CLOSING_BRACKET);
	}
}
