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
package com.mgmtp.a12.dataservices.internal.query;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.fields.aggregation.function.Count;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.DefaultQueryGeneratorContext.QueryGeneratorContextFactory;
import com.mgmtp.a12.dataservices.query.fields.aggregation.AggregationProjector;
import com.mgmtp.a12.dataservices.query.fields.ProjectionField;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DateRangeOperator;

import lombok.SneakyThrows;

import static org.mockito.Mockito.mock;

public class TopologySerializationTest {

	protected ObjectMapper objectMapper = new ObjectMapper().registerModules(new JavaTimeModule());

	@BeforeMethod
	public void setUp() {
		new QueryGeneratorContextFactory(objectMapper, mock(ApplicationContext.class)).init();
	}

	@DataProvider
	public static Object[][] operatorProvider() throws IOException {
		return new Object[][] {

			/* SIMPLE *************************************************************************************************/
			new Object[] { "Simple",
				IOUtils.toString(Objects.requireNonNull(TopologySerializationTest.class.getResourceAsStream("/query_topology/simple.json"))),
				QueryRoot.builder().targetDocumentModel("Contract").build() },

			/* SIMPLE wit LOGIC ***************************************************************************************/
			new Object[] { "Simple with logic",
				IOUtils.toString(Objects.requireNonNull(TopologySerializationTest.class.getResourceAsStream("/query_topology/simple_with_logic.json"))),
				QueryRoot.builder()
					.targetDocumentModel("Contract")
					.constraint(AndOperator
						.builder()
						.operand(ExactMatchOperator.builder()
							.field("/RootGroup/SomeField")
							.value("any value")
							.build())
						.operand(DateRangeOperator.builder()
							.field("/RootGroup/SomeField")
							.from("2023-11-26T14:54:45.000")
							.to("2023-12-24T17:00:00.000")
							.build())
						.build())
					.build() },

			/* SIMPLE wit AGGREGATION ***************************************************************************************/
			new Object[] { "Simple with aggregation",
				IOUtils.toString(Objects.requireNonNull(TopologySerializationTest.class.getResourceAsStream("/query_topology/simple_with_aggregation.json"))),
				QueryRoot.builder()
					.targetDocumentModel("Contract")
					.aggregation(AggregationProjector.builder()
						.aggregations(
							List.of(Count.builder().field("countField").alias("countAlias").build()))
						.group(
							List.of(ProjectionField.builder().field("groupField").alias("groupAlias").build()))
						.docRef("xxx")
						.build())
					.build() },

			/* NESTED *************************************************************************************************/
			new Object[] { "Nested",
				IOUtils.toString(Objects.requireNonNull(TopologySerializationTest.class.getResourceAsStream("/query_topology/nested.json"))),
				QueryRoot
					.builder()
					.targetDocumentModel("Contract")
					.link(QueryLink
						.builder()
						.relationshipModel("BusinessContractRM")
						.targetRole("PartnerRole")
						.ordered(false)
						.constraint(AndOperator
							.builder()
							.operand(ExactMatchOperator
								.builder()
								.field("/RootGroup/SomeField")
								.value("any value")
								.build())
							.operand(DateRangeOperator
								.builder()
								.field("/RootGroup/SomeField")
								.from("2023-11-26T14:54:45.000")
								.to("2023-12-24T17:00:00.000")
								.build())
							.build())
						.linkDocumentFields(List.of("/RootGroup/SomeField"))
						.linkDocumentConstraint(ExactMatchOperator.builder()
							.field("/RootGroup/SomeField")
							.value("SomeText")
							.caseSensitive(true)
							.build())
						.build())
					.link(QueryLink
						.builder()
						.relationshipModel("BusinessContractRM")
						.targetRole("PartnerRole")
						.ordered(false)
						.constraint(AndOperator
							.builder()
							.operand(ExactMatchOperator
								.builder()
								.field("/RootGroup/SomeField")
								.value("any value")
								.build())
							.operand(DateRangeOperator
								.builder()
								.field("/RootGroup/SomeField")
								.from("2023-11-26T14:54:45.000")
								.to("2023-12-24T17:00:00.000")
								.build())
							.build())
						.build())
					.build() },

			/* NESTED with LOGIC **************************************************************************************/
			new Object[] { "Nested with logic",
				IOUtils.toString(Objects.requireNonNull(TopologySerializationTest.class.getResourceAsStream("/query_topology/nested_with_logic.json"))),
				QueryRoot
					.builder()
					.targetDocumentModel("Contract")
					.constraint(AndOperator
						.builder()
						.operand(ExactMatchOperator
							.builder()
							.field("/RootGroup/SomeField")
							.value("any value")
							.build())
						.operand(DateRangeOperator
							.builder()
							.field("/RootGroup/SomeField")
							.from("2023-11-26T14:54:45.000")
							.to("2023-12-24T17:00:00.000")
							.build())
						.build())
					.link(QueryLink
						.builder()
						.relationshipModel("BusinessContractRM")
						.targetRole("PartnerRole")
						.ordered(false)
						.constraint(AndOperator
							.builder()
							.operand(ExactMatchOperator
								.builder()
								.field("/RootGroup/SomeField")
								.value("any value")
								.build())
							.operand(DateRangeOperator
								.builder()
								.field("/RootGroup/SomeField")
								.from("2023-11-26T14:54:45.000")
								.to("2023-12-24T17:00:00.000")
								.build())
							.build())
						.build())
					.link(QueryLink
						.builder()
						.relationshipModel("BusinessContractRM")
						.targetRole("PartnerRole")
						.ordered(false)
						.constraint(AndOperator
							.builder()
							.operand(ExactMatchOperator
								.builder()
								.field("/RootGroup/SomeField")
								.value("any value")
								.build())
							.operand(DateRangeOperator
								.builder()
								.field("/RootGroup/SomeField")
								.from("2023-11-26T14:54:45.000")
								.to("2023-12-24T17:00:00.000")
								.build())
							.build())
						.build())
					.build() },

		};
	}

	@SneakyThrows
	@Test(dataProvider = "operatorProvider")
	public void testDeserialization(String description, String input, QueryRoot expectation) {
		Assert.assertEquals(objectMapper.readValue(input, QueryRoot.class), expectation);
		System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectation));
	}

	@SneakyThrows
	@Test(dataProvider = "operatorProvider")
	public void testSerialization(String description, String expectation, QueryRoot input) {
		JSONAssert.assertEquals(objectMapper.writeValueAsString(input), expectation, true);
		System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input));
	}
}
