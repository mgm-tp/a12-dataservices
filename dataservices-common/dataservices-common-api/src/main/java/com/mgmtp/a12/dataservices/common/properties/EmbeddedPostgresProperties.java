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
package com.mgmtp.a12.dataservices.common.properties;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * Properties class to define embedded `Postgres` configuration.
 *
 * @topic DataSource
 */
@Data
public class EmbeddedPostgresProperties {

	public static final String LC_CTYPE = "lc-ctype";
	public static final String LC_COLLATE = "lc-collate";
	public static final String EN_US_UTF_8 = "en_US.UTF-8";

	/**
	 * Switch for enabling/disabling embedded Postgres.
	 *
	 * @default `false`
	 */
	private boolean enabled = false;

	/**
	 * Indicate Postgres embedded file based storage location. If this path is null, then it means all embedded `Postgres` data will be in temp directory.
	 * Temp directory location can be set by using system property `java.io.tmpdir`, default value depends on OS. The property is ignored if the
	 * `spring.datasources.dataservices.embedded-postgres.enabled` property is `false`.
	 * 
	 * If using classpath resource, the folder must be existed in classpath resource already.
	 * Please make sure that the application has write permission on target location.
	 * @default `null`.
	 */
	private String path;

	/**
	 * All `Postgres` connection parameters that can be either set in JDBC URL, in Driver properties or in datasource setters.
	 */
	private Map<String, String> connectConfig = new HashMap<>();

	/**
	 * All `Postgres` `locale-ctype` option for configuring datasource locale, this option will be set in 'initdb' command.
	 */
	private String localeCType = EN_US_UTF_8;

	/**
	 * All `Postgres` `locale-collate` option for configuring datasource collation, this option will be set in 'initdb' command.
	 * Must match `localeCType` on platforms where different collate and ctype values are not supported (e.g. Windows with Postgres 18+).
	 */
	private String localeCollate = EN_US_UTF_8;

	/**
	 * Specifies options to be passed directly to `the pg_ctl` command.
	 */
	private Map<String, String> postgresConfig = new HashMap<>();

	/**
	 * Port of the embedded `Postgres` database. Defaults to `5434`.
	 *
	 * @default `5434`
	 */
	private int port = 5434;

	/**
	 * Set to true in tests for isolation, false in production to persist data.
	 * CAUTION: Never set this to true in production, as all data will be deleted on startup.
	 *
	 * @default `false`
	 */
	private boolean cleanDataDirectory = false;

	/**
	 * Optional directory path used to override the default location for extracting
	 * and storing the PostgreSQL binary files (executables).
	 *
	 * If set to a non-null value, this directory will be used instead of the
	 * default system temporary directory (`java.io.tmpdir/embedded-pg`)
	 * to cache executables like `initdb` and `postgres`.
	 * This is primarily useful for ensuring binaries are stored in a persistent,
	 * accessible location across multiple application runs. Examples:
	 *
	 * - file:/path/to/folder/
	 * - file:./path/to/folder
	 * - file:/C:\path\to\folder
	 *
	 * @default `null`
	 */
	private File overrideWorkingDirectory = null;

	/**
	 * Maximum time to wait for the embedded PostgreSQL server to start accepting connections.
	 * Increase this value when running in resource-constrained environments
	 * where I/O contention may delay server readiness beyond the default.
	 *
	 * Configured via Spring Boot's standard duration notation (e.g. `30s`, `1m`).
	 *
	 * @default 30 seconds
	 * @see java.time.Duration
	 */
	private Duration pgStartupWait = Duration.ofSeconds(30);
}
