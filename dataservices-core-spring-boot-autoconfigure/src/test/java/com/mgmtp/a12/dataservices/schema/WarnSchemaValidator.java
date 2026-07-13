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
package com.mgmtp.a12.dataservices.schema;

import java.util.Locale;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.hibernate.tool.schema.extract.spi.SequenceInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.hibernate.tool.schema.internal.GroupedSchemaValidatorImpl;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WarnSchemaValidator extends GroupedSchemaValidatorImpl {

	public WarnSchemaValidator(HibernateSchemaManagementTool tool, SchemaFilter validateFilter) {
		super(tool, validateFilter);
	}

	@Override protected void validateTable(Table table, TableInformation tableInformation, Metadata metadata, ExecutionOptions options, Dialect dialect) {
		if (tableInformation == null) {
			log.warn("Schema-validation: missing table [%s]".formatted(table.getQualifiedTableName().toString()));
			return;
		}

		for (Selectable selectable : table.getColumns()) {
			if (selectable instanceof Column column) {
				ColumnInformation existingColumn = tableInformation.getColumn(Identifier.toIdentifier(column.getQuotedName()));
				if (existingColumn == null) {
					String warnMessage = "Schema-validation: missing column [%s] in table [%s]".formatted(column.getName(), table.getQualifiedTableName());
					DatabaseSchemaValidationErrorsHolder.addValidationError(
						new DatabaseSchemaValidationErrorsHolder.SchemaValidationError(column.getName(), table.getQualifiedTableName().toString(),
							warnMessage));
					log.warn(warnMessage);

					return;
				}

				validateColumnType(table, column, existingColumn, metadata, options, dialect);
			}
		}
	}

	protected void validateColumnType(Table table, Column column, ColumnInformation columnInformation, Metadata metadata, ExecutionOptions options,
		Dialect dialect) {
		boolean typesMatch = column.getSqlTypeCode(metadata) == columnInformation.getTypeCode();
		if (!typesMatch) {
			String warnMessage = String.format(
				"Schema-validation: wrong column type encountered in column [%s] in " +
					"table [%s]; found [%s (Types#%s)], but expecting [(Types#%s)]",
				column.getName(),
				table.getQualifiedTableName(),
				columnInformation.getTypeName().toLowerCase(Locale.ROOT),
				JdbcTypeNameMapper.getTypeName(columnInformation.getTypeCode()),
				JdbcTypeNameMapper.getTypeName(column.getSqlTypeCode(metadata))
			);
			DatabaseSchemaValidationErrorsHolder.addValidationError(
				new DatabaseSchemaValidationErrorsHolder.SchemaValidationError(column.getName(), table.getQualifiedTableName().toString(),
					JdbcTypeNameMapper.getTypeName(columnInformation.getTypeCode()), JdbcTypeNameMapper.getTypeName(column.getSqlTypeCode(metadata)),
					warnMessage));
			log.warn(warnMessage);
		}
	}

	@Override protected void validateSequence(Sequence sequence, SequenceInformation sequenceInformation) {
		//Needed to satisfy the interface, but not used in the test
	}
}
