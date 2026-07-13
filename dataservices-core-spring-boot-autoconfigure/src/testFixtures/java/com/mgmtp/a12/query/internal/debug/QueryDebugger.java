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
package com.mgmtp.a12.query.internal.debug;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.QueryContextFactory;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.DefaultQueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.RootCteGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SpringBootConfiguration
@EnableConfigurationProperties
@Import(QueryDebuggerConfiguration.class)
public class QueryDebugger implements ApplicationRunner, ExitCodeGenerator {
	private final DefaultQueryGeneratorContext.QueryGeneratorContextFactory queryGeneratorContextFactory;
	private final QueryContextFactory queryContextFactory;
	private final ObjectMapper objectMapper;
	private final YAMLMapper yamlMapper;
	private final QueryDebuggerProperties queryDebuggerProperties;

	@Override public void run(ApplicationArguments args) throws IOException {
		QueryGeneratorContext queryGeneratorContext = queryGeneratorContextFactory.createContext(queryContextFactory.createContext(null));
		System.out.println("""
			
			Paste a query in JSON:
			
			""");
		QueryRoot queryRoot = objectMapper.readValue(System.in, QueryRoot.class);
		RootCteGenerator rootGenerator = RootCteGenerator.builder()
			.query(queryRoot)
			.generatorContext(queryGeneratorContext)
			.types(List.of(DocumentTreeNodeType.ROOT, DocumentTreeNodeType.LINK, DocumentTreeNodeType.CHILD))
			.build();

		String sql = rootGenerator.render(new StringBuilder()).toString();

		System.out.println();
		System.out.println();
		try (PygmentsHighlighter sqlHighlighter = new PygmentsHighlighter(PygmentsHighlighter.Lexer.SQL, queryDebuggerProperties.getStyle());
			PygmentsHighlighter jsonHighlighter = new PygmentsHighlighter(PygmentsHighlighter.Lexer.JSON, queryDebuggerProperties.getStyle());
			PygmentsHighlighter yamlHighlighter = new PygmentsHighlighter(PygmentsHighlighter.Lexer.YAML, queryDebuggerProperties.getStyle())) {
			if (queryGeneratorContext.getParamHolder().isEmpty()) {
				System.out.printf("""
					Query SQL:
					--------------------------------------------------------------------------------
					%s
					--------------------------------------------------------------------------------
					%n""", sqlHighlighter.highlight(SqlFormatter.of(Dialect.PostgreSql).format(sql)));
			} else {
				System.out.printf("""
						Query JSON:
						--------------------------------------------------------------------------------
						%s--------------------------------------------------------------------------------
						
						Query SQL:
						--------------------------------------------------------------------------------
						%s--------------------------------------------------------------------------------
						
						Query SQL params:
						--------------------------------------------------------------------------------
						%s--------------------------------------------------------------------------------
						
						Query SQL with values:
						--------------------------------------------------------------------------------
						%s--------------------------------------------------------------------------------
						""",
					jsonHighlighter.highlight(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(queryRoot)),
					sqlHighlighter.highlight(SqlFormatter.of(Dialect.PostgreSql).format(sql)),
					yamlHighlighter.highlight(yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(queryGeneratorContext.getParamHolder())),
					sqlHighlighter.highlight(SqlGeneratorHelpersInternal.getQueryWithValues(sql, queryGeneratorContext)));
			}
		}
	}

	@Override public int getExitCode() {
		return 0;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplicationBuilder(QueryDebugger.class)
			.bannerMode(Banner.Mode.OFF)
			.web(WebApplicationType.NONE)
			.logStartupInfo(false)
			.build();
		try {
			System.exit(SpringApplication.exit(app.run(args)));
		} catch (Throwable e) {
			log.error("Unable to start the application.", e);
			PrintStream dialogOutput = System.err;
			dialogOutput.println();
			dialogOutput.println("===================================================");
			dialogOutput.println("!!   Unable to start the application             !!");
			dialogOutput.println();
			dialogOutput.flush();
			System.exit(1);
		}
	}

}
