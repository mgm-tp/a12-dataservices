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
package com.mgmtp.a12.dataservices.query.generator.sql.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.internal.query.generator.sql.constraint.AbstractSqlGeneratorTest;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.Direction;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder.NullHandling;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.RelationshipOrder;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;

import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_FRAGMENT_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.DATE_TIME_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE;
import static com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.FieldTypes.NUMBER_FIELD_TYPE;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link RootCteGenerator} verifying relationship order SQL generation.
 *
 * Each test case specifies a query, optional field descriptor overrides, and the name of an
 * expected SQL snapshot file located in `src/test/resources/sql-snapshots/`.
 *
 * To regenerate snapshot files after intentional SQL changes, run
 * `RootCteGeneratorSqlCapture#captureSqlSnapshots` and commit the updated files.
 */
public class RootCteGeneratorTest extends AbstractSqlGeneratorTest {

	record SqlTestCase(
		String description,
		QueryRoot query,
		Map<String, FieldDescriptor> fieldDescriptors,
		String expectedSqlFile
	) {
		SqlTestCase(String description, QueryRoot query, String expectedSqlFile) {
			this(description, query, Map.of(), expectedSqlFile);
		}
	}

	@DataProvider(name = "queryVariants")
	public Object[][] queryVariants() {
		return new Object[][] {

			{ new SqlTestCase(
				"single-level relationship order: Contract → Partner via ContractBusinessPartner",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner", "/name", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				"01-single-level-relationship-order.sql"
			) },

			{ new SqlTestCase(
				"nested relationship order: Contract → Partner → Address via two hops",
				contractQueryWithSort(List.of(
					new RelationshipOrder("ContractBusinessPartner", "Partner",
						new RelationshipOrder("PartnerAddresses", "Address",
							new DirectFieldOrder("/city", Direction.ASC, false, NullHandling.NULLS_FIRST)))
				)),
				"02-nested-relationship-order.sql"
			) },

			{ new SqlTestCase(
				"case-insensitive relationship order: applies LOWER() to string field",
				contractQueryWithSort(List.of(
					new RelationshipOrder("ContractBusinessPartner", "Partner",
						new DirectFieldOrder("/name", Direction.ASC, true, NullHandling.NULLS_LAST))
				)),
				"03-case-insensitive-relationship-order.sql"
			) },

			{ new SqlTestCase(
				"mixed sort: direct field order followed by relationship order",
				contractQueryWithSort(List.of(
					new DirectFieldOrder("/contractNumber", Direction.ASC),
					relOrder("ContractBusinessPartner", "Partner", "/name", Direction.DESC, NullHandling.NULLS_FIRST)
				)),
				"04-mixed-direct-and-relationship-order.sql"
			) },

			{ new SqlTestCase(
				"two flat relationship orders: unique aliases rl1/d1 and rl2/d2",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner", "/name", Direction.ASC, NullHandling.NULLS_LAST),
					relOrder("ContractInsurer", "Insurer", "/companyName", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				"05-two-flat-relationship-orders.sql"
			) },

			{ new SqlTestCase(
				"date field (IDateType): uses timestamp_value from document_fields JOIN",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/StartOfRelationship", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				Map.of("/BusinessPartnerRoot/StartOfRelationship",
					FieldDescriptor.builder().fieldType(DATE_FIELD_TYPE).repeatable(false).build()),
				"07-date-field.sql"
			) },

			{ new SqlTestCase(
				"datetime field (IDateTimeType): uses timestamp_value from document_fields JOIN",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/EndOfRelationship", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				Map.of("/BusinessPartnerRoot/EndOfRelationship",
					FieldDescriptor.builder().fieldType(DATE_TIME_FIELD_TYPE).repeatable(false).build()),
				"08-datetime-field.sql"
			) },

			{ new SqlTestCase(
				"number field (INumberType): uses number_value from document_fields JOIN",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/accountNumber", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				Map.of("/BusinessPartnerRoot/accountNumber",
					FieldDescriptor.builder().fieldType(NUMBER_FIELD_TYPE).repeatable(false).build()),
				"09-number-field.sql"
			) },

			{ new SqlTestCase(
				"enumeration field without locale: uses value from document_fields JOIN",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/CustomerDiscount", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				Map.of("/BusinessPartnerRoot/CustomerDiscount",
					FieldDescriptor.builder().fieldType(ENUMERATION_FIELD_TYPE).enumerationType(true).repeatable(false).build()),
				"10-enumeration-field.sql"
			) },

			{ new SqlTestCase(
				"date fragment field (IDateFragmentType): uses typed_value from document_fields JOIN",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/ApproximateOfferDate", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				Map.of("/BusinessPartnerRoot/ApproximateOfferDate",
					FieldDescriptor.builder().fieldType(DATE_FRAGMENT_FIELD_TYPE).repeatable(false).build()),
				"11-date-fragment-field.sql"
			) },

			{ new SqlTestCase(
				"two flat relationship orders with typed fields: distinct df1 and df2 aliases",
				contractQueryWithSort(List.of(
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/StartOfRelationship", Direction.ASC, NullHandling.NULLS_LAST),
					relOrder("ContractBusinessPartner", "Partner",
						"/BusinessPartnerRoot/EndOfRelationship", Direction.ASC, NullHandling.NULLS_LAST)
				)),
				Map.of(
					"/BusinessPartnerRoot/StartOfRelationship",
					FieldDescriptor.builder().fieldType(DATE_FIELD_TYPE).repeatable(false).build(),
					"/BusinessPartnerRoot/EndOfRelationship",
					FieldDescriptor.builder().fieldType(DATE_TIME_FIELD_TYPE).repeatable(false).build()
				),
				"12-two-flat-typed-fields.sql"
			) },

			{ new SqlTestCase(
				"two-level nested relationship order: document_fields joined at terminal level df2 only",
				contractQueryWithSort(List.of(
					new RelationshipOrder("ContractBusinessPartner", "Partner",
						new RelationshipOrder("PartnerAddresses", "Address",
							new DirectFieldOrder("/AddressRoot/City", Direction.ASC, false, NullHandling.NULLS_LAST)))
				)),
				Map.of("/AddressRoot/City",
					FieldDescriptor.builder().fieldType("IStringType").repeatable(false).build()),
				"13-two-level-nested-typed-field.sql"
			) },

			{ new SqlTestCase(
				"direct string field order case-sensitive (ignoreCase=false): uses #>> with COLLATE \"C\"",
				contractQueryWithSort(List.of(
					new DirectFieldOrder("/ContractRoot/name", Direction.ASC, false, NullHandling.NULLS_LAST)
				)),
				Map.of("/ContractRoot/name",
					FieldDescriptor.builder().fieldType("IStringType").repeatable(false).build()),
				"14-direct-string-field-case-sensitive.sql"
			) },

			{ new SqlTestCase(
				"direct string field order case-insensitive (ignoreCase=true): uses #>> without COLLATE \"C\"",
				contractQueryWithSort(List.of(
					new DirectFieldOrder("/ContractRoot/name", Direction.ASC, true, NullHandling.NULLS_LAST)
				)),
				Map.of("/ContractRoot/name",
					FieldDescriptor.builder().fieldType("IStringType").repeatable(false).build()),
				"15-direct-string-field-case-insensitive.sql"
			) },
		};
	}

	@Test(dataProvider = "queryVariants",
		description = "Should generate correct SQL for each query and sort variant")
	public void shouldGenerateCorrectSqlForQueryVariant(SqlTestCase testCase) {
		QueryGeneratorContext context = buildContext(testCase.fieldDescriptors());

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(testCase.query())
			.generatorContext(context)
			.build();

		StringBuilder sb = new StringBuilder();
		generator.render(sb);
		String sql = sb.toString();

		String expectedSql = loadExpectedSql(testCase.expectedSqlFile());
		assertEquals(normalizeWhitespace(sql), expectedSql,
			"[%s] Generated SQL does not match snapshot %s".formatted(testCase.description(), testCase.expectedSqlFile()));
	}

	@Test(description = "Should qualify a direct enumeration sort field with the cte_root alias when combined "
		+ "with a relationship order, to avoid an ambiguous \"value\" column reference against the joined "
		+ "relationship document tables")
	public void shouldQualifyEnumDirectSortWithCteRootAliasWhenCombinedWithRelationshipOrder() {
		QueryGeneratorContext context = spy(queryGeneratorContextFactory.createContext(newQueryContext()));
		context.setLocale("de");
		context.getEnrichments().setFieldDescriptor("/gender",
			FieldDescriptor.builder().fieldType(ENUMERATION_FIELD_TYPE).enumerationType(true).repeatable(false).build());

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(
				relOrder("ContractBusinessPartner", "Partner", "/name", Direction.ASC, NullHandling.NULLS_LAST),
				new DirectFieldOrder("/gender", Direction.ASC, false, NullHandling.NULLS_LAST)))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(context)
			.build();

		String sql = generator.render(new StringBuilder()).toString();

		assertTrue(sql.contains("cte_root.value #> '{\"gender\",\"de\"}'"),
			"Enum direct sort must be qualified with the cte_root alias. Generated SQL:\n" + sql);
		assertFalse(sql.matches("(?s).*[\\s,]value #> '\\{\"gender\",\"de\"\\}'.*"),
			"Enum direct sort must not emit an unqualified \"value\" reference. Generated SQL:\n" + sql);
	}

	@Test(description = "Should qualify direct doc_ref and model metadata sort fields with the cte_root alias when "
		+ "combined with a relationship order, to avoid ambiguous \"doc_ref\"/\"model_name\" column references "
		+ "against the joined relationship document tables")
	public void shouldQualifyMetadataDirectSortWithCteRootAliasWhenCombinedWithRelationshipOrder() {
		QueryGeneratorContext context = spy(queryGeneratorContextFactory.createContext(newQueryContext()));
		context.getEnrichments().setFieldDescriptor(DocumentMetadataConstants.DOCREF_METADATA_PATH,
			FieldDescriptor.builder().fieldType("IStringType").repeatable(false).build());
		context.getEnrichments().setFieldDescriptor(DocumentMetadataConstants.MODEL_REFERENCE_PATH,
			FieldDescriptor.builder().fieldType("IStringType").repeatable(false).build());

		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(List.of(
				relOrder("ContractBusinessPartner", "Partner", "/name", Direction.ASC, NullHandling.NULLS_LAST),
				new DirectFieldOrder(DocumentMetadataConstants.DOCREF_METADATA_PATH, Direction.ASC, false, NullHandling.NULLS_LAST),
				new DirectFieldOrder(DocumentMetadataConstants.MODEL_REFERENCE_PATH, Direction.ASC, false, NullHandling.NULLS_LAST)))
			.build();

		RootCteGenerator generator = RootCteGenerator.builder()
			.query(query)
			.generatorContext(context)
			.build();

		String sql = generator.render(new StringBuilder()).toString();
		// Scope the unqualified-reference checks to the ROW_NUMBER() window ORDER BY, since the outer
		// projection legitimately references the bare "doc_ref"/"model_name" columns of the single CTE.
		String window = extractSortWindow(sql);

		assertTrue(window.contains("cte_root.doc_ref"),
			"doc_ref metadata sort must be qualified with the cte_root alias. Generated SQL:\n" + sql);
		assertTrue(window.contains("cte_root.model_name"),
			"model metadata sort must be qualified with the cte_root alias. Generated SQL:\n" + sql);
		assertFalse(window.matches("(?s).*[\\s,]doc_ref[\\s,].*"),
			"doc_ref metadata sort must not emit an unqualified \"doc_ref\" reference. Generated SQL:\n" + sql);
		assertFalse(window.matches("(?s).*[\\s,]model_name[\\s,].*"),
			"model metadata sort must not emit an unqualified \"model_name\" reference. Generated SQL:\n" + sql);
	}

	private static String extractSortWindow(String sql) {
		int start = sql.indexOf("OVER (ORDER BY");
		int end = sql.indexOf(") AS _sort_rank", start);
		if (start < 0 || end < 0) {
			throw new IllegalStateException("No ROW_NUMBER() sort window found in generated SQL:\n" + sql);
		}
		return sql.substring(start, end);
	}

	// --- helpers ---

	private static QueryRoot contractQueryWithSort(List<Order> sort) {
		return QueryRoot.builder()
			.targetDocumentModel("Contract")
			.sort(sort)
			.build();
	}

	private static RelationshipOrder relOrder(String relationshipModel, String targetRole, String field, DirectFieldOrder.Direction direction,
		DirectFieldOrder.NullHandling nullHandling) {
		return new RelationshipOrder(relationshipModel, targetRole,
			new DirectFieldOrder(field, direction, false, nullHandling));
	}

	private static RelationshipOrder relOrder(RelationshipOrder nested, DirectFieldOrder.Direction direction, DirectFieldOrder.NullHandling nullHandling) {
		// For nested relationship orders, create a new RelationshipOrder wrapping the nested one
		return new RelationshipOrder(nested.relationshipModel(), nested.targetRole(), nested.sortBy());
	}

	private QueryGeneratorContext buildContext(Map<String, FieldDescriptor> fieldDescriptors) {
		QueryGeneratorContext context = fieldDescriptors.isEmpty()
			? queryGeneratorContextFactory.createContext(newQueryContext())
			: spy(queryGeneratorContextFactory.createContext(newQueryContext()));
		fieldDescriptors.forEach((field, descriptor) ->
			context.getEnrichments().setFieldDescriptor(field, descriptor));
		return context;
	}

	private static String loadExpectedSql(String fileName) {
		String resourcePath = "sql-snapshots/" + fileName;
		InputStream stream = RootCteGeneratorTest.class.getClassLoader().getResourceAsStream(resourcePath);
		if (stream == null) {
			throw new IllegalStateException("SQL snapshot resource not found: " + resourcePath);
		}
		try (stream) {
			String raw = new String(stream.readAllBytes());
			return normalizeWhitespace(raw);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Strips SQL line comments (`--`) and collapses any whitespace sequence into a single space,
	 * enabling freely formatted and commented snapshot files.
	 */
	private static String normalizeWhitespace(String sql) {
		return sql
			.replaceAll("--[^\n]*", "")
			.replaceAll("\\s+", " ")
			.strip();
	}
}
