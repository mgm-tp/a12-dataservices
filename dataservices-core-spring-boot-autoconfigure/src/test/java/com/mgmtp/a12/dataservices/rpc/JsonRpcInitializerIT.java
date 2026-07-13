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
package com.mgmtp.a12.dataservices.rpc;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tools.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import static ch.qos.logback.classic.Level.DEBUG;
import static com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer.JSON_RPC_ERROR_FOR_PATH;
import static com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer.JSON_RPC_SUCCESS_FOR_PATH;
import static org.testng.Assert.assertTrue;

public class JsonRpcInitializerIT extends AbstractSpringContextIT {

	private static final String FOLDER_PATH_SUCCESS = "src/test/resources/rpc/bulk/input-success/**/*.*";
	private static final String FOLDER_PATH_ERROR = "src/test/resources/rpc/bulk/input/**/*.*";
	private static final String FILE_1_PATH = "src/test/resources/rpc/bulk/input/folder1/1-addDoc.json";
	private static final String FILE_2_PATH = "src/test/resources/rpc/bulk/input/folder1/2-listDocs.json";
	private static final String FILE_3_PATH = "src/test/resources/rpc/bulk/input/folder2/1-wrong.json";
	private static final String COMPLEX_FILE_PATH = "src/test/resources/rpc/bulk/request_add_and_delete_document.json";
	private static final String INVALID_PATH = "invalidPath";

	@Autowired JsonRpcBasicServer jsonRpcBasicServer;
	@Autowired ObjectMapper objectMapper;
	@Autowired ResourcePatternResolver resourcePatternResolver;

	private MemoryAppender memoryAppender;

	@BeforeMethod
	public void setUp() throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger(JsonRpcInitializer.class);
		memoryAppender = new MemoryAppender();
		logger.setLevel(DEBUG);
		logger.addAppender(memoryAppender);
		memoryAppender.start();
		cleanUpTestEnvironment();
		modelsFunctions.createModel(PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH);
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void invalidPathWithError() {
		memoryAppender.reset();
		getInitializer(INVALID_PATH).execute();
	}

	@Test
	public void singleFirstFile() {
		memoryAppender.reset();
		getInitializer(FILE_1_PATH).execute();
		memoryAppender.assertNoError();
		memoryAppender.assertSuccess(".*.rpc.bulk.input.folder1.1-addDoc.json",
			"\\{\"jsonrpc\":\"2.0\",\"id\":\"AddDocument1\",\"result\":\\{\"docRef\":\"BusinessPartner/\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}\"}}\n");
	}

	@Test
	public void singleSecondFile() {
		memoryAppender.reset();
		getInitializer(FILE_2_PATH).execute();
		memoryAppender.assertNoError();
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void singleThirdFileWithError() {
		memoryAppender.reset();
		getInitializer(FILE_3_PATH).execute();
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void passFolderWithError() {
		memoryAppender.reset();
		getInitializer(FOLDER_PATH_ERROR).execute();
	}

	@Test
	public void passFolderShouldExecuteInOrderAndCorrectly() {
		memoryAppender.reset();
		getInitializer(FOLDER_PATH_SUCCESS).execute();
		memoryAppender.assertSuccess(".*.rpc.bulk.input-success.folder1.1-addDoc.json",
				"\\{\"jsonrpc\":\"2.0\",\"id\":\"AddDocument1\",\"result\":\\{\"docRef\":\"BusinessPartner/\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}\"}}\n");
	}

	@Test
	public void addDocumentAndReferenceItToDelete() {
		memoryAppender.reset();
		getInitializer(COMPLEX_FILE_PATH).execute();
		memoryAppender.assertNoError();
		memoryAppender.assertSuccess(".*.rpc.bulk.request_add_and_delete_document.json",
			"\\[\\{\"jsonrpc\":\"2.0\",\"id\":\"AddDocument1\",\"result\":\\{\"docRef\":\"BusinessPartner/\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}\"}},\\{\"jsonrpc\":\"2.0\",\"id\":\"deleteDocument\",\"result\":null}]\n");
	}

	private JsonRpcInitializer getInitializer(String path) {
		return new JsonRpcInitializer(jsonRpcBasicServer, objectMapper, List.of(path), resourcePatternResolver);
	}

	private static class MemoryAppender extends ListAppender<ILoggingEvent> {

		public void reset() {
			this.list.clear();
		}

		public void assertNoError() {
			assertTrue(list.stream()
				.map(ILoggingEvent::getMessage)
				.noneMatch(m -> m.startsWith(JSON_RPC_ERROR_FOR_PATH)), "Error message found!");
		}

		public void assertSuccess(String... args) {
			assertMessage(args, JSON_RPC_SUCCESS_FOR_PATH);
		}

		private void assertMessage(String[] args, String jsonRpcSuccessForPath) {
			Pattern[] patterns = Arrays.stream(args)
				.map(regex -> Pattern.compile(regex, Pattern.DOTALL))
				.toArray(Pattern[]::new);
			Optional<String> error = Optional.empty();
			for (ILoggingEvent event : list) {
				if (event.getMessage().startsWith(jsonRpcSuccessForPath)) {
					error = assertArgs(event.getArgumentArray(), patterns);
					if (error.isEmpty()) {
						return;
					}
				}
			}
			throw error.map(AssertionError::new)
				.orElseThrow(() -> new AssertionError("NO SUCCESS MESSAGE FOUND:\n%s".formatted(this.list.stream()
				.map(ILoggingEvent::getFormattedMessage)
				.collect(Collectors.joining("\n")))));
		}

		private Optional<String> assertArgs(Object[] argumentArray, Pattern[] args) {
			for (int i = 0; i < args.length; i++) {
				if (!args[i].matcher(argumentArray[i].toString()).matches()) {
					return Optional.of("ARG:\n%s\nPATTERN:\n%s".formatted(argumentArray[i], args[i]));
				}
			}
			return Optional.empty();
		}
	}
}
