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
package com.mgmtp.a12.dataservices.internal;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.reflections.Reflections;

import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.constraint.internal.UnknownOperator;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.fields.aggregation.internal.UnknownFunction;
import com.mgmtp.a12.dataservices.query.projection.IQueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * CLI tool that generates a JSON Schema for {@link QueryRoot}.
 * Should not be used by customer projects.
 */
@Slf4j
@Command(
	name = "query-schema-generator",
	mixinStandardHelpOptions = true,
	description = "Generates a JSON Schema for QueryRoot."
)
public class QuerySchemaGenerator implements Callable<Integer> {

	private static final String BASE_PACKAGE = "com.mgmtp.a12.dataservices";
	private static final String SCHEMA_ID = "https://mgm-tp.com/a12/dataservices/schema/query-root-dataservices-vanila.json";
	private static final String DEFAULT_OUTPUT_PATH = "build/schema/query-root-dataservices-vanila.json";

	@Option(names = { "-o", "--schema-output" }, description = "Output path for the generated schema", defaultValue = DEFAULT_OUTPUT_PATH)
	private String outputPath;

	@Option(names = { "-p", "--base-package" }, description = "Base package to scan for implementations", defaultValue = BASE_PACKAGE)
	private String basePackage;

	@Option(names = { "--schema-id" }, description = "Value for the $id field in the generated schema", defaultValue = SCHEMA_ID)
	private String schemaId;

	/**
	 * Entry point for the CLI execution.
	 *
	 * @param args command line arguments; see `--help` for options.
	 */
	public static void main(String[] args) {
		int exit = new CommandLine(new QuerySchemaGenerator()).execute(args);
		System.exit(exit);
	}

	/**
	 * Generates and persists the JSON Schema based on discovered query components.
	 *
	 * @return the process exit code where 0 indicates success and non-zero indicates failure.
	 */
	@Override
	public Integer call() {
		StopWatch sw = StopWatch.createStarted();
		final ThreadLocal<Set<Class<?>>> generationGuard = ThreadLocal.withInitial(HashSet::new);
		try {
			log.info("Starting schema generation (output: {})", outputPath);
			log.debug("Scanning package {} for implementations", basePackage);

			Reflections reflections = new Reflections(basePackage);

			// find all concrete implementations
			List<Class<? extends ILogicOperator>> operatorImpls = findConcreteSubtypes(reflections, ILogicOperator.class,
				c -> !UnknownOperator.class.isAssignableFrom(c));
			List<Class<? extends IAggregationFunction>> functionImpls = findConcreteSubtypes(reflections, IAggregationFunction.class,
				c -> !UnknownFunction.class.isAssignableFrom(c));
			Set<StringNode> availableProjections = findConcreteSubtypes(reflections, IQueryProjection.class, c -> true).stream()
				.map(c -> c.getDeclaredAnnotation(QueryProjection.class))
				.map(QueryProjection::value)
				.map(JsonNodeFactory.instance::stringNode)
				.collect(Collectors.toSet());

			log.debug("Discovered: operators={}, aggregation functions={}, projections={}",
				operatorImpls.size(), functionImpls.size(), availableProjections.size());

			SchemaGeneratorConfigBuilder cfg = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
			cfg
				.forTypesInGeneral()
				.withSubtypeResolver(new QuerySubtypeResolver(operatorImpls, functionImpls))
				.withCustomDefinitionProvider(new QueryCustomDefinitionProvider(availableProjections, generationGuard));

			log.info("Generating JSON Schema for {}", QueryRoot.class.getSimpleName());

			ObjectNode schema = new SchemaGenerator(cfg.build())
				.generateSchema(QueryRoot.class);
			schema.put("$id", schemaId);

			saveSchema(schema, sw);

			return 0;

		} catch (Exception ex) {
			log.error("Schema generation failed", ex);
			return 1;
		} finally {
			generationGuard.remove();
		}
	}

	private void saveSchema(ObjectNode schema, StopWatch sw) throws IOException {
		JsonMapper mapper = JsonMapper.builder().build();
		Path out1 = Path.of(outputPath);
		Files.createDirectories(out1.getParent());
		Files.writeString(out1, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
		Path out = out1;

		log.info("Schema generated and saved to: {} in {}", out.toAbsolutePath(), sw.formatTime());
	}

	private static <T> List<Class<? extends T>> findConcreteSubtypes(Reflections reflections, Class<T> baseType, Predicate<Class<? extends T>> filter) {
		return reflections.getSubTypesOf(baseType).stream()
			.filter(QuerySchemaGenerator::isConcrete)
			.filter(filter)
			.toList();
	}

	private static boolean isConcrete(Class<?> c) {
		return !c.isInterface() && !Modifier.isAbstract(c.getModifiers());
	}
}
