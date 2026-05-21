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
package com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;

import lombok.RequiredArgsConstructor;

@QueryOperatorGenerator({ HasOperator.class })
@RequiredArgsConstructor
@Component public class HasOperatorSqlGenerator implements ILogicOperatorGenerator<HasOperator> {

	@Override public StringBuilder renderCondition(StringBuilder sb, HasOperator operator, QueryGeneratorContext queryGeneratorContext) {
		renderExists(sb, operator, queryGeneratorContext);
		return sb;
	}

	private void renderExists(StringBuilder sb, HasOperator hasOperator, QueryGeneratorContext queryGeneratorContext) {

		String parent = queryGeneratorContext.getCurrentDocumentTableAlias();

		queryGeneratorContext.registerNewDocumentTableAlias();

		sb.append(QueryGeneratorConstants.EXISTS_OPERATOR).append('(');
		renderSelect(sb);

		renderFrom(sb, queryGeneratorContext);
		renderWhere(sb, hasOperator, parent, queryGeneratorContext);

		queryGeneratorContext.unregisterDocumentTableAlias();

		sb.append(')');

	}

	private static void renderSelect(StringBuilder sb) {
		sb.append(QueryGeneratorConstants.SELECT_KEYWORD);
		sb.append(1);
	}

	private static void renderFrom(StringBuilder sb, QueryGeneratorContext queryGeneratorContext) {
		sb.append(QueryGeneratorConstants.FROM_KEYWORD);
		SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema()).append(queryGeneratorContext.getDocumentTableName())
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(queryGeneratorContext.getCurrentDocumentTableAlias())
			.append(',');
		SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema()).append(QueryGeneratorConstants.TableNames.RELATIONSHIP_ROLE_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD).append(
				QueryGeneratorConstants.TableNames.SOURCE_ROLE_TABLE_ALIAS).append(',');
		SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema()).append(QueryGeneratorConstants.TableNames.RELATIONSHIP_LINK_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD)
			.append(
				QueryGeneratorConstants.TableNames.LINK_TABLE_ALIAS).append(',');
		SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema()).append(QueryGeneratorConstants.TableNames.RELATIONSHIP_ROLE_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD).append(QueryGeneratorConstants.TableNames.TARGET_ROLE_TABLE_ALIAS)
			.append(QueryGeneratorConstants.COMMA);
		SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema()).append(QueryGeneratorConstants.TableNames.LINK_ORDER_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD).append(QueryGeneratorConstants.TableNames.LINK_SOURCE_ORDER_TABLE_ALIAS)
			.append(QueryGeneratorConstants.COMMA);
		SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema()).append(QueryGeneratorConstants.TableNames.LINK_ORDER_TABLE_NAME)
			.append(QueryGeneratorConstants.AS_KEYWORD).append(QueryGeneratorConstants.TableNames.LINK_TARGET_ORDER_TABLE_ALIAS);
	}

	private void renderWhere(StringBuilder sb, HasOperator hasOperator, String parent, QueryGeneratorContext queryGeneratorContext) {

		sb.append(QueryGeneratorConstants.WHERE_KEYWORD);

		String targetDocumentModel = queryGeneratorContext.getEnrichments().getTargetDocumentModel(hasOperator);
		SqlGeneratorHelpersInternal.modelWhere(sb, queryGeneratorContext.getCurrentDocumentTableAlias(),
			Stream.concat(Stream.of(targetDocumentModel),
				Optional.ofNullable(queryGeneratorContext.getEnrichments().getModelSubtypes(targetDocumentModel))
					.stream()
					.flatMap(Collection::stream)));

		sb.append(QueryGeneratorConstants.AND_OPERATOR);

		sb.append(parent)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR).append(QueryGeneratorConstants.TableNames.SOURCE_ROLE_TABLE_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(QueryGeneratorConstants.ColumnNames.ROLE_DOCREF_COLUMN_NAME);

		SqlGeneratorHelpersInternal.linkWhere(sb, hasOperator, queryGeneratorContext);

		sb.append(QueryGeneratorConstants.AND_OPERATOR);

		sb.append(queryGeneratorContext.getCurrentDocumentTableAlias())
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME)
			.append(QueryGeneratorConstants.EQUALS_OPERATOR)
			.append(QueryGeneratorConstants.TableNames.TARGET_ROLE_TABLE_ALIAS)
			.append(QueryGeneratorConstants.DOT_JOINER)
			.append(QueryGeneratorConstants.ColumnNames.ROLE_DOCREF_COLUMN_NAME);

		SqlGeneratorHelpersInternal.renderConstraint(sb, hasOperator.getConstraint(), queryGeneratorContext, true);

		Optional.ofNullable(hasOperator.getLinkDocumentConstraint())
			.ifPresent(operator -> {
				queryGeneratorContext.registerNewTargetDocRef
					(QueryGeneratorConstants.TableNames.LINK_TABLE_ALIAS +
						QueryGeneratorConstants.DOT_JOINER +
						QueryGeneratorConstants.ColumnNames.LINK_DOCUMENT_DOCREF_COLUMN_NAME);
				SqlGeneratorHelpersInternal.renderExistsConstraint(sb, operator,
					QueryGeneratorConstants.TableNames.LINK_TABLE_ALIAS + QueryGeneratorConstants.DOT_JOINER
						+ QueryGeneratorConstants.ColumnNames.LINK_DOCUMENT_DOCREF_COLUMN_NAME, queryGeneratorContext, true);
				queryGeneratorContext.unregisterTargetDocRef();
			});
	}
}
