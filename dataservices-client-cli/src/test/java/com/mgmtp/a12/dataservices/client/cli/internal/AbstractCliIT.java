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
package com.mgmtp.a12.dataservices.client.cli.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

import com.mgmtp.a12.dataservices.client.model.ModelsClient;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.client.cli.internal.commands.HelpCommand.JAVA_COMMAND;
import static com.mgmtp.a12.dataservices.client.cli.internal.commands.HelpCommand.USAGE_MSG;

@Slf4j
@SpringBootTest(properties = "spring.main.banner-mode=off" )
public abstract class AbstractCliIT extends AbstractTestNGSpringContextTests {

	public static final String CLASSPATH_MODELS_DOCUMENT_CONTRACT_JSON = "classpath:models/document/Contract.json";
	public static final String CLASSPATH_BULK_MODELS_JAR = "classpath:/bulk/models.jar";
	public static final String MODEL_GRAPH_RESPONSE_PATH = "classpath:/rpc/bulk/modelGraphResponse.json";
	protected static ByteArrayOutputStream stdout = new ByteArrayOutputStream();
	protected static StringWriter stderr = new StringWriter();
	static PrintWriter dialogOutputPrintWriter = new PrintWriter(stderr);

	protected static final List<String> LIST_INSURANCE_BULK_MODELS =
		Arrays.asList("Address", "AddressOther", "BusinessPartnerSuper", "BusinessPartnerSuperInclude1", "BusinessPartnerSuperInclude2",
			"BusinessPartnerSuperInclude3", "BusinessPartnerSuperInclude4", "BusinessPartnerSuperInclude5", "BusinessPartnerSuperOther",
			"Contract", "DemoModelBare", "DemoModelWithAllFields");

	public static final String RPC_HELP_OUTPUT = """
		
		Command [json rpc]
		  Options:
		  input_path   json file or folder to the files to execute the json-rpc requests
		  output_dir   output directory to store the responses of the requests
		  Examples:
		   %s json rpc --input_path=my_request.json --output_dir=my_output_dir
		   %s json rpc --input_path=my_folder --output_dir=my_output_dir
		""".formatted(JAVA_COMMAND, JAVA_COMMAND);

	public static final String MODEL_GRAPH_HELP_OUTPUT = """
		
		Command [model graph]
		  Options:
		      output   Puts retrieved Model Graph to file
		  Examples:
		   %s model graph
		   %s model graph --output=./example/output.json
		""".formatted(JAVA_COMMAND, JAVA_COMMAND);

	public static final String MODEL_UPLOAD_HELP_OUTPUT = """
		
		Command [model upload]
		  Arguments:
		  MODEL.json   upload single model
		   MODEL.zip   upload all models in zip file
		  Examples:
		   %s model upload my_model.json
		   %s model upload my_models.zip
		""".formatted(JAVA_COMMAND, JAVA_COMMAND);

	protected static final String HELP_CMD_OUTPUT = """
		
		Command [help]
		  Options:
		          -h   print help message
		  Arguments:
		        help   print help message
		""";

	public static final String HELP_OUTPUT =
		USAGE_MSG +
			HELP_CMD_OUTPUT +
			RPC_HELP_OUTPUT +
			MODEL_GRAPH_HELP_OUTPUT +
			MODEL_UPLOAD_HELP_OUTPUT;

	@Autowired protected Client client;
	@Autowired private IApplicationOutput applicationOutput;
	@Autowired private ModelsClient modelsClient;

	@BeforeMethod
	public void setUp() throws IOException {
		initialize();
	}

	public void initialize() throws IOException {
		applicationOutput.close();
		stdout = new ByteArrayOutputStream();
		stderr = new StringWriter();
		dialogOutputPrintWriter = new PrintWriter(stderr);
	}

	protected void cleanUpByDocumentModel(final String documentModel) {
		try {
			modelsClient.deleteModel(documentModel);
		} catch (final Exception e) {
			log.warn("can't clean document model " + documentModel, e);
		}
	}

	protected String getCleanLineEndings(String s) {
		return s.replace("\r\n", "\n")
			.replace('\r', '\n');
	}
}
