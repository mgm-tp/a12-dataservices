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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEnvironmentCleaner {

	private static final Set<String> IGNORED_TABLES =
		Set.of("databasechangelog", "databasechangeloglock", "cs_databasechangelog", "cs_databasechangeloglock");

	public void cleanUpTestEnvironment(final DataSource dataSource, final CacheManager cacheManager, final Optional<DataSource> contentStoreDataSource) {
		StopWatch timer = StopWatch.createStarted();
		cleanUpDatabase(dataSource);
		cleanUpContentStoreDatabase(contentStoreDataSource);
		cleanUpCache(cacheManager);
		log.info("testEnvironmentCleanUp: test environment cleaned in {}", timer);
	}

	public void cleanUpTestEnvironment(final DataSource dataSource, final CacheManager cacheManager) {
		StopWatch timer = StopWatch.createStarted();
		cleanUpDatabase(dataSource);
		cleanUpCache(cacheManager);
		log.info("testEnvironmentCleanUp: test environment cleaned in {}", timer);
	}

	public void cleanUpDatabase(final DataSource dataSource) {
		StopWatch timer = StopWatch.createStarted();
		try (Connection connection = dataSource.getConnection()) {
			List<String> tables = collectTables(connection);
			if (!tables.isEmpty()) {
				String sql = "TRUNCATE TABLE " + String.join(", ", tables) + " CASCADE";
				log.debug("Cleaning tables: {}", sql);
				try (Statement stmt = connection.createStatement()) {
					stmt.execute(sql);
				}
			}
			log.info("cleanupDB: database cleaned in {}", timer);
		} catch (final Exception e) {
			log.error("Unable to clean-up database", e);
		}
	}

	public void cleanUpContentStoreDatabase(final Optional<DataSource> contentStoreDataSourceOpt) {
		if (contentStoreDataSourceOpt.isEmpty())
			return;
		DataSource contentStoreDataSource = contentStoreDataSourceOpt.get();
		StopWatch timer = StopWatch.createStarted();
		try (Connection connection = contentStoreDataSource.getConnection()) {
			List<String> tables = collectTables(connection);
			if (!tables.isEmpty()) {
				String sql = "TRUNCATE TABLE " + String.join(", ", tables) + " CASCADE";
				log.debug("Cleaning content store tables: {}", sql);
				try (Statement stmt = connection.createStatement()) {
					stmt.execute(sql);
				}
			}
			log.info("cleanup content store DB: database cleaned in {}", timer);
		} catch (final Exception e) {
			log.error("Unable to clean-up content store database", e);
		}
	}

	public void cleanUpCache(final CacheManager cacheManager) {
		StopWatch timer = StopWatch.createStarted();
		try {
			cacheManager.getCacheNames().stream()
				.map(cacheManager::getCache)
				.filter(Objects::nonNull)
				.forEach(cache -> {
					try {
						// In case of different cache configuration don't fail.
						cache.clear();
					} catch (Exception ignored) {}
				});
			log.info("cleanupCache: caches cleaned in {}", timer);
		} catch (Exception e) {
			// Handle cases where the cache manager is not available (e.g., Hazelcast instance not active)
			log.debug("Unable to clean up cache: {}", e.getMessage());
		}
	}

	private static List<String> collectTables(Connection connection) throws SQLException {
		List<String> tables = new ArrayList<>();
		try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
			while (rs.next()) {
				String name = rs.getString("TABLE_NAME");
				if (!IGNORED_TABLES.contains(name.toLowerCase())) {
					tables.add(name);
				}
			}
		}
		return tables;
	}
}
