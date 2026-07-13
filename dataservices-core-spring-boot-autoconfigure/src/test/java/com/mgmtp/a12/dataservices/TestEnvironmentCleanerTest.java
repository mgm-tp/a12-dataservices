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
package com.mgmtp.a12.dataservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the SQL-building logic of `TestEnvironmentCleaner`.
 * Verifies table collection, Liquibase-table filtering, and TRUNCATE statement construction
 * using Mockito-stubbed JDBC objects — no live database required.
 */
public class TestEnvironmentCleanerTest {

	private TestEnvironmentCleaner cleaner;
	private DataSource dataSource;
	private Connection connection;
	private DatabaseMetaData databaseMetaData;
	private Statement statement;

	@BeforeMethod
	public void setUp() throws Exception {
		cleaner = new TestEnvironmentCleaner();
		dataSource = mock(DataSource.class);
		connection = mock(Connection.class);
		databaseMetaData = mock(DatabaseMetaData.class);
		statement = mock(Statement.class);

		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(databaseMetaData);
		when(connection.createStatement()).thenReturn(statement);
	}

	@Test(description = "Should include all non-system tables in TRUNCATE statement")
	public void shouldIncludeAllNonSystemTablesInTruncateStatement() throws Exception {
		// Given
		ResultSet resultSet = mock(ResultSet.class);
		when(databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getString("TABLE_NAME")).thenReturn("table1", "table2");

		// When
		cleaner.cleanUpDatabase(dataSource);

		// Then
		verify(statement).execute("TRUNCATE TABLE table1, table2 CASCADE");
	}

	@Test(description = "Should exclude Liquibase tables from TRUNCATE statement")
	public void shouldExcludeLiquibaseTablesFromTruncateStatement() throws Exception {
		// Given
		ResultSet resultSet = mock(ResultSet.class);
		when(databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true, true, true, false);
		when(resultSet.getString("TABLE_NAME")).thenReturn("databasechangelog", "databasechangeloglock", "my_table");

		// When
		cleaner.cleanUpDatabase(dataSource);

		// Then
		verify(statement).execute("TRUNCATE TABLE my_table CASCADE");
	}

	@Test(description = "Should skip TRUNCATE when no tables are present")
	public void shouldSkipTruncateWhenNoTablesArePresent() throws Exception {
		// Given
		ResultSet resultSet = mock(ResultSet.class);
		when(databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(false);

		// When
		cleaner.cleanUpDatabase(dataSource);

		// Then
		verify(statement, never()).execute(org.mockito.ArgumentMatchers.anyString());
	}
}
