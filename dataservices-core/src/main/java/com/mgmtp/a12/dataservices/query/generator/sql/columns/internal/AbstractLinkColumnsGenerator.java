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
package com.mgmtp.a12.dataservices.query.generator.sql.columns.internal;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AbstractLinkColumnsGenerator extends AbstractTopologyColumnGenerator {
	@Override public CharSequence getLinkDocumentValue() {
		return QueryGeneratorConstants.ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS;
	}

	@Override public CharSequence getTargetDocRefValue() {
		return QueryGeneratorConstants.TableNames.RESULT_JOIN_TABLE_ALIAS + QueryGeneratorConstants.DOT_JOINER + QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME;
	}

	@Override public CharSequence getTargetRoleValue() {
		return QueryGeneratorConstants.ColumnNames.TARGET_ROLE_COLUMN_ALIAS;
	}

	@Override public CharSequence getSourceDocRefValue() {
		return QueryGeneratorConstants.ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS;
	}

	@Override public CharSequence getSourceRoleValue() {
		return QueryGeneratorConstants.ColumnNames.SOURCE_ROLE_COLUMN_ALIAS;
	}

	@Override public CharSequence getRelationshipModelValue() {
		return QueryGeneratorConstants.ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS;
	}

	@Override public CharSequence getLinkIdValue() {
		return QueryGeneratorConstants.ColumnNames.LINK_ID_COLUMN_ALIAS;
	}
}
