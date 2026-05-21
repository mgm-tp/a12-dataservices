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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.test.context.TestPropertySource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;

@TestPropertySource(properties = {
	"spring.datasources.dataservices.jpa.properties.hibernate.hbm2ddl.auto=validate",
	"spring.datasources.dataservices.jpa.properties.hibernate.schema_management_tool=com.mgmtp.a12.dataservices.schema.WarnSchemaValidatorManagementTool"
})
public class DatabaseSchemaValidationTest extends AbstractSpringContextIT {

	List<DatabaseSchemaValidationErrorsHolder.SchemaValidationError> postgresSchemaValidationErrors = List.of(
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("timestamp", "request_id", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [timestamp] in table [request_id]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("created_at", "relationship_link", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [created_at] in table [relationship_link]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("updated_at", "model", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [updated_at] in table [model]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("created_at", "model", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [created_at] in table [model]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("metadata", "migration_step", "OTHER", "JSON",
			"Schema-validation: wrong column type encountered in column [metadata] in table [migration_step]; found [json (Types#OTHER)], but expecting [(Types#JSON)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("execution_date", "migration_step", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [execution_date] in table [migration_step]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("modified_at", "document_backup", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [modified_at] in table [document_backup]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("created_at", "document_backup", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [created_at] in table [document_backup]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("last_try", "dirty_attachment", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [last_try] in table [dirty_attachment]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("modified_at", "attachment_header", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [modified_at] in table [attachment_header]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("created_at", "dirty_attachment", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [created_at] in table [dirty_attachment]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("created_at", "attachment_header", "TIMESTAMP", "TIMESTAMP_UTC",
			"Schema-validation: wrong column type encountered in column [created_at] in table [attachment_header]; found [timestamp (Types#TIMESTAMP)], but expecting [(Types#TIMESTAMP_UTC)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("data", "model_fields", "OTHER", "JSON",
			"Schema-validation: wrong column type encountered in column [data] in table [model_fields]; found [jsonb (Types#OTHER)], but expecting [(Types#JSON)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("original_value", "document_search", "OTHER", "JSON",
			"Schema-validation: wrong column type encountered in column [original_value] in table [document_search]; found [jsonb (Types#OTHER)], but expecting [(Types#JSON)]"),
		new DatabaseSchemaValidationErrorsHolder.SchemaValidationError("value", "document_search", "OTHER", "JSON",
			"Schema-validation: wrong column type encountered in column [value] in table [document_search]; found [jsonb (Types#OTHER)], but expecting [(Types#JSON)]")
	);

	@Test public void testPostgresSchemaValidity() {
		Assert.assertEquals(DatabaseSchemaValidationErrorsHolder.getErrors().size(), postgresSchemaValidationErrors.size(),
			getErrorMessage(postgresSchemaValidationErrors));
		postgresSchemaValidationErrors.forEach(error -> Assert.assertTrue(DatabaseSchemaValidationErrorsHolder.getErrors().contains(error), error.getReason()));
	}

	private String getErrorMessage(List<DatabaseSchemaValidationErrorsHolder.SchemaValidationError> postgresSchemaValidationErrors) {
		Collection<DatabaseSchemaValidationErrorsHolder.SchemaValidationError> unexpectedErrors =
			CollectionUtils.subtract(DatabaseSchemaValidationErrorsHolder.getErrors(), postgresSchemaValidationErrors);
		Collection<DatabaseSchemaValidationErrorsHolder.SchemaValidationError> missingErrors =
			CollectionUtils.subtract(postgresSchemaValidationErrors, DatabaseSchemaValidationErrorsHolder.getErrors());

		String unexpectedString = unexpectedErrors.stream()
			.map(DatabaseSchemaValidationErrorsHolder.SchemaValidationError::getReason)
			.collect(Collectors.joining("\n- "));

		String missingString = missingErrors.stream()
			.map(DatabaseSchemaValidationErrorsHolder.SchemaValidationError::getReason)
			.collect(Collectors.joining("\n- "));

		return "unexpected:\n%s\nmissing:\n%s\n".formatted(unexpectedString, missingString);
	}
}
