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
package com.mgmtp.a12.dataservices.query.generator.sql.constraint.range.internal;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.NumericRangeOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.SqlGeneratorHelpers;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.range.AbstractRangeOperatorSqlGenerator;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames.NUMBER_VALUE_COLUMN_NAME;
import static com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour.CORE_SOURCE;

@QueryOperatorGenerator({ DoubleRangeOperator.class })
@Component public class NumericRangeOperatorSqlGenerator<O extends NumericRangeOperator<T>, T extends Number> extends AbstractRangeOperatorSqlGenerator<O, T>
	implements ILogicOperatorGenerator<O> {

	public NumericRangeOperatorSqlGenerator() {
		super(CORE_SOURCE);
	}

	@Override protected void renderCondition(O operator, StringBuilder sb, QueryGeneratorContext generatorContext) {

		renderDocumentFieldsTableRangeCondition(sb,
			NUMBER_VALUE_COLUMN_NAME,
			Objects.toString(generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.FROM_PROPERTY), null),
			Objects.toString(generatorContext.getEnrichments().getOperatorEnrichment(operator).get(Enrichments.TO_PROPERTY), null),
			generatorContext);
	}

	@Override protected String valueLiteral(String input, QueryGeneratorContext generatorContext) {
		return SqlGeneratorHelpers.addParam(input, generatorContext) + QueryGeneratorConstants.CAST_OPERATOR + QueryGeneratorConstants.NUMERIC_TYPE;
	}
}
