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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.cache.CacheManager;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import workbench.WbManager;
import workbench.db.CommitType;
import workbench.db.ConnectionProfile;
import workbench.db.TableDeleter;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.importer.TableDependencySorter;

@Slf4j
public class TestEnvironmentCleaner {

	private static final List<String> IGNORED_TABLES =
		Arrays.asList("databasechangelog", "databasechangeloglock", "cs_databasechangelog", "cs_databasechangeloglock");
	private static final AtomicLong INIT_COUNT = new AtomicLong();
	private static final AtomicLong CLEAN_COUNT = new AtomicLong();
	private static List<TableIdentifier> tablesToDelete = null;
	private static List<TableIdentifier> contentStoreTablesToDelete = null;
	private static CharSequence deleterScript = null;
	private static CharSequence contentStoreDeleterScript = null;

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
		WbManager.prepareForEmbedded();
		try (Connection connection = dataSource.getConnection()) {
			log.debug("Cleaning tables:\n{}", deleterScript);
			final WbConnection wbCon = prepareConnection(connection);
			final TableDeleter deleter = new TableDeleter(wbCon);
			final List<TableIdentifier> tablesToDelete = getTablesToDelete(wbCon, deleter);

			deleter.deleteTableData(tablesToDelete, false, false, false);
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
		WbManager.prepareForEmbedded();
		try (Connection connection = contentStoreDataSource.getConnection()) {
			log.debug("Cleaning content store tables:\n{}", contentStoreDeleterScript);
			final WbConnection wbCon = prepareConnection(connection);
			final TableDeleter deleter = new TableDeleter(wbCon);
			if (contentStoreTablesToDelete == null) {
				prepareContentStoreData(wbCon, deleter);
			}
			deleter.deleteTableData(contentStoreTablesToDelete, false, false, false);
			log.info("cleanup content store DB: database cleaned in {}", timer);
		} catch (final Exception e) {
			log.error("Unable to clean-up content store database", e);
		}
	}

	public void cleanUpCache(final CacheManager cacheManager) {
		StopWatch timer = StopWatch.createStarted();
		cacheManager.getCacheNames().stream()
			.map(cacheManager::getCache)
			.filter(Objects::nonNull)
			.forEach(cache -> {
				try {
					// In case of different cache configuration don't fail.
					cache.clear();
				} catch (Exception ignored) {}
			});
		log.info("cleanupDB: caches cleaned in {}", timer);
	}

	/**
	 * Prepare sorted table list for deletion.
	 * Sorting of tables is eager operation, that's why we assure that tables are calculated and ordered only once per
	 * application run.
	 * Next time this method is called, return already prepared list.
	 */
	@NonNull private static List<TableIdentifier> getTablesToDelete(WbConnection wbCon, TableDeleter deleter) throws SQLException {
		if (tablesToDelete == null) {
			INIT_COUNT.addAndGet(1);
			tablesToDelete = prepareTables(wbCon);
			deleterScript = deleter.generateScript(tablesToDelete, CommitType.never, false, false);
		}
		log.debug("Table cleaner initialized {} times, called {} times", INIT_COUNT.get(), CLEAN_COUNT.get());
		return tablesToDelete;
	}

	/**
	 * Prepare sorted table list for content store.
	 * Sorting of tables is eager operation, that's why we assure that tables are calculated and ordered only once per
	 * application run.
	 * Next time this method is called, return already prepared list.
	 */
	@NonNull private static void prepareContentStoreData(WbConnection wbCon, TableDeleter deleter) throws SQLException {
		contentStoreTablesToDelete = prepareTables(wbCon);
		contentStoreDeleterScript = deleter.generateScript(contentStoreTablesToDelete, CommitType.never, false, false);
	}

	private static List<TableIdentifier> prepareTables(WbConnection wbCon) throws SQLException {
		final TableDependencySorter sorter = new TableDependencySorter(wbCon);
		sorter.setValidateTables(false);
		return sorter.sortForDelete(
			wbCon.getMetadata().getTableList().stream()
				.filter(table -> !IGNORED_TABLES.contains(table.getObjectName().toLowerCase()))
				.collect(Collectors.toList()), true);
	}

	@NonNull private static WbConnection prepareConnection(Connection connection) throws SQLException {
		ConnectionProfile connectionProfile = new ConnectionProfile();
		connectionProfile.setUrl(connection.getMetaData().getURL());
		return new WbConnection("scripter", connection, connectionProfile);
	}
}
