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
package com.mgmtp.a12.dataservices.configuration;

import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;

import jakarta.persistence.EntityManagerFactory;

/**
 * Unit tests to verify Hibernate batching configuration properties are correctly applied.
 *
 * These tests verify that the Hibernate JDBC batching configuration is properly set up
 * to improve INSERT/UPDATE performance for bulk operations.
 *
 * Expected Behavior:
 *
 * - `hibernate.jdbc.batch_size` should be set to `50` for efficient batch processing
 * - `hibernate.order_inserts` should be `true` to group similar INSERT statements
 * - `hibernate.order_updates` should be `true` to group similar UPDATE statements
 *
 * Implementation Note: These properties optimize bulk operations by reducing the number of
 * round-trips to the database. The batch size of 50 is a balanced default that works well
 * for most use cases without consuming excessive memory.
 *
 * @see <a href="https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#batch">Hibernate Batching</a>
 */
@SpringBootTest
public class HibernateBatchingConfigurationTest extends AbstractSpringContextIT {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	/**
	 * Verifies that `hibernate.jdbc.batch_size` property is configured to `50`.
	 *
	 * Expected Behavior:
	 * The Hibernate JDBC batch size should be set to 50, allowing Hibernate to batch up to
	 * 50 INSERT or UPDATE statements together before sending them to the database.
	 *
	 * Why This Matters:
	 * Batching reduces database round-trips, significantly improving performance for operations
	 * that insert or update multiple entities (e.g., `DocumentFieldEntity` records during
	 * document indexing).
	 *
	 * Configuration Location:
	 * `dataservices-core-spring-boot-autoconfigure/src/main/resources/dataservices-common_jpa.properties`
	 *
	 * Property:
	 * `spring.datasources.dataservices.jpa.properties.hibernate.jdbc.batch_size=50`
	 */
	@Test(description = "Should have hibernate.jdbc.batch_size configured to 50")
	public void shouldHaveBatchSizeConfigured() {
		// Given: Hibernate session factory is initialized with properties
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		Map<String, Object> properties = sessionFactory.getProperties();

		// When: We retrieve the batch size configuration
		Object batchSize = properties.get("hibernate.jdbc.batch_size");

		// Then: Batch size should be configured to 50
		Assert.assertNotNull(batchSize, "hibernate.jdbc.batch_size should be configured");
		Assert.assertEquals(batchSize.toString(), "50",
			"hibernate.jdbc.batch_size should be set to 50 for optimal batch performance");
	}

	/**
	 * Verifies that `hibernate.order_inserts` property is configured to `true`.
	 *
	 * Expected Behavior:
	 * Hibernate should be configured to order INSERT statements by entity type, allowing
	 * better batching efficiency when inserting multiple entities of the same type.
	 *
	 * Why This Matters:
	 * When `order_inserts=true`, Hibernate groups INSERT statements for the same entity type
	 * together, enabling JDBC batching to work effectively. Without this, mixed entity types
	 * would prevent efficient batching.
	 *
	 * Configuration Location:
	 * `dataservices-core-spring-boot-autoconfigure/src/main/resources/dataservices-common_jpa.properties`
	 *
	 * Property:
	 * `spring.datasources.dataservices.jpa.properties.hibernate.order_inserts=true`
	 */
	@Test(description = "Should have hibernate.order_inserts configured to true")
	public void shouldHaveOrderInsertsEnabled() {
		// Given: Hibernate session factory is initialized with properties
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		Map<String, Object> properties = sessionFactory.getProperties();

		// When: We retrieve the order_inserts configuration
		Object orderInserts = properties.get("hibernate.order_inserts");

		// Then: Order inserts should be enabled
		Assert.assertNotNull(orderInserts, "hibernate.order_inserts should be configured");
		Assert.assertEquals(orderInserts.toString(), "true",
			"hibernate.order_inserts should be true to enable efficient batching of INSERT statements");
	}

	/**
	 * Verifies that `hibernate.order_updates` property is configured to `true`.
	 *
	 * Expected Behavior:
	 * Hibernate should be configured to order UPDATE statements by entity type, allowing
	 * better batching efficiency when updating multiple entities of the same type.
	 *
	 * Why This Matters:
	 * Similar to `order_inserts`, this property groups UPDATE statements for the same entity
	 * type together, enabling JDBC batching for bulk update operations.
	 *
	 * Configuration Location:
	 * `dataservices-core-spring-boot-autoconfigure/src/main/resources/dataservices-common_jpa.properties`
	 *
	 * Property:
	 * `spring.datasources.dataservices.jpa.properties.hibernate.order_updates=true`
	 */
	@Test(description = "Should have hibernate.order_updates configured to true")
	public void shouldHaveOrderUpdatesEnabled() {
		// Given: Hibernate session factory is initialized with properties
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		Map<String, Object> properties = sessionFactory.getProperties();

		// When: We retrieve the order_updates configuration
		Object orderUpdates = properties.get("hibernate.order_updates");

		// Then: Order updates should be enabled
		Assert.assertNotNull(orderUpdates, "hibernate.order_updates should be configured");
		Assert.assertEquals(orderUpdates.toString(), "true",
			"hibernate.order_updates should be true to enable efficient batching of UPDATE statements");
	}
}
