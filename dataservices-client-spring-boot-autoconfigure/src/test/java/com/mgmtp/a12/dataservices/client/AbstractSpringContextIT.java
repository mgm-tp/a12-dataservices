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
package com.mgmtp.a12.dataservices.client;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.client.exception.A12ClientException;
import com.mgmtp.a12.dataservices.client.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.client.exception.GenericErrorException;
import com.mgmtp.a12.dataservices.client.exception.MissingDataException;
import com.mgmtp.a12.dataservices.client.model.ModelsClient;
import com.mgmtp.a12.dataservices.client.relationship.rest.RestRelationshipClient;
import com.mgmtp.a12.dataservices.client.rpc.RequestBuilderFactory;
import com.mgmtp.a12.dataservices.client.rpc.RpcOperationsClient;
import com.mgmtp.a12.dataservices.client.rpc.internal.JsonRpc2RequestBuilder;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.query.ResultSet;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Base test class with all necessary configurations to run repository/service tests
 */

@Slf4j
@SpringBootTest( classes = { ITConfiguration.class } )
public abstract class AbstractSpringContextIT extends AbstractTestNGSpringContextTests {
	protected static final String RELATIONSHIP_MODEL_PATH = "models/relationship/";
	protected static final String CONTRACT_MODEL_NAME = "Contract";
	protected static final String PARTNER_ROLE = "Partner";
	protected static final String CONTRACT_MODEL_FILE = "models/document/Contract.json";

	protected static final String ADDRESS_MODEL_NAME = "Address";
	protected static final String ADDRESS_MODEL_FILE = "models/document/Address.json";

	protected static final String BUSINESS_PARTNER_MODEL_NAME = "BusinessPartner";
	protected static final String BUSINESS_PARTNER_MODEL_FILE = "models/document/BusinessPartner.json";

	protected static final String BUSINESS_PARTNER_SUPER_MODEL_NAME = "BusinessPartnerSuper";
	protected static final String BUSINESS_PARTNER_SUPER_MODEL_FILE = "models/document/BusinessPartnerSuper.json";
	protected static final String CONTRACT_CO_INSURED_ADDITIONAL_FIELDS_MODEL_FILE = "models/document/CoInsuredAdditionalFields.json";
	protected static final String CONTRACT_CO_INSURED_PARTNER_MODEL_NAME = "ContractCoInsuredPartner";
	protected static final String CONTRACT_CO_INSURED_PARTNER_MODEL_FILE = RELATIONSHIP_MODEL_PATH + CONTRACT_CO_INSURED_PARTNER_MODEL_NAME + ".json";
	protected static final String CONTRACT_CO_INSURED_PARTNER_RELATIONSHIP_MODEL_INVALID_JSON = "model/relationship/ContractCoInsuredPartner_Invalid.json";
	protected static final String CONTRACT_CO_INSURED_PARTNER_RELATIONSHIP_MODEL_VALID_JSON = "model/relationship/ContractCoInsuredPartner_Valid.json";

	protected static final String CO_INSURED_ADDITIONAL_FIELD_MODEL_NAME = "CoInsuredAdditionalFields";
	protected static final String CO_INSURED_ADDITIONAL_FIELD_MODEL_FILE = "models/document/CoInsuredAdditionalFields.json";
	protected static final String FILENAME = "AttachmentImage.jpg";
	protected static final String ATTACHMENT_FILE = "src/test/resources/attachment/" + FILENAME;
	private static final String META_MODEL_NAME = RelationshipModel.META_MODEL_NAME;
	protected static final String BUSINESS_PARTNER_DOCUMENT = "document/BusinessPartnerWith1Attachment.json";
	protected static final String CONTRACT_DOCUMENT = "document/Contract-1.json";
	public static final String BUSINESS_PARTNER_1_DOCUMENT = "document/BusinessPartner-1.json";
	public static final String BUSINESS_PARTNER_2_DOCUMENT = "document/BusinessPartner-2.json";
	@Autowired protected ResourcePatternResolver resourcePatternResolver;
	@Autowired public RpcOperationsClient rpcOperationsClient;
	@Autowired public RestRelationshipClient relationshipClient;
	@Autowired public ModelsClient modelsClient;
	@Autowired public HeaderParser headerParser;
	@Autowired protected RequestBuilderFactory requestBuilderFactory;
	@Autowired protected ObjectMapper objectMapper;

	protected String readFile(final String path) {
		try {
			return resourcePatternResolver.getResource("classpath:" + path)
				.getContentAsString(StandardCharsets.UTF_8);
		} catch (final IOException ex) {
			throw new IllegalStateException("Resource classpath:" + path + " cannot be found");
		}
	}

	@SneakyThrows
	protected void cleanUpByDocumentModel(final String documentModel) {
		JsonRpc2RequestBuilder rpcRequestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		listDocumentsByQuery(documentModel, null, 1000, 0, null).getEntries().stream()
			.map(DocumentSpec::getDocRef)
			.map(DocumentReference::toString)
			.forEach(ds -> rpcRequestBuilder
				.addMethodCall(CoreOperationConstants.DELETE_DOCUMENT_OPERATION)
				.id("delete_" + ds)
				.putParameter("docRef", ds));
		handleError(rpcOperationsClient.invoke(rpcRequestBuilder.build()));
		modelsClient.deleteModel(documentModel);
	}

	protected Optional<String> findDocumentModelById(final String documentModel) {
		try {
			return Optional.ofNullable(modelsClient.loadModel(documentModel));
		} catch (Exception e) {
			// NOOP
		}
		return Optional.empty();
	}

	protected ResultSet<DocumentSpec> listDocumentsByQuery(String documentModel, String fullText, int limit, int offset, String lang)
		throws JsonProcessingException {
		JsonRpc2RequestBuilder rpcRequestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequestBuilder.addMethodCall(CoreOperationConstants.QUERY_OPERATION)
			.id("listByModel")
			.putParameter("query", QueryRoot.builder()
				.targetDocumentModel(documentModel)
				.projectionName("document")
				.paging(Paging.builder().pageSize(100).pageNumber(0).build())
				.build()
			)
		;

		List<JsonRpc2Response> results = rpcOperationsClient.invoke(rpcRequestBuilder.build());
		log.info("RPC results: {}", new ObjectMapper().writeValueAsString(results));
		Optional<JsonRpc2Response> jsonRpc2ResponseStream = results.stream()
			.filter(rs -> "listByModel".equals(rs.getId()))
			.findAny();
		jsonRpc2ResponseStream
			.map(JsonRpc2Response::getError)
			.ifPresent(e -> {
				throw new RuntimeException(e.toString());
			});
		Optional<JsonNode> operationResult = jsonRpc2ResponseStream
			.map(JsonRpc2Response::getResult);
		logOpt("ListDocumentsByQuery result", operationResult);
		Optional<ResultSet<DocumentSpec>> result = operationResult
			.map(this::getDocumentSpecResultSet);

		logOpt("Operation result", result);
		return result.orElse(null);
	}

	@SneakyThrows
	private ResultSet<DocumentSpec> getDocumentSpecResultSet(JsonNode node) {
		return jsonToObject(node, new TypeReference<>() {});
	}

	@SneakyThrows
	protected <T> T jsonToObject(TreeNode node, TypeReference<T> type) {
		return objectMapper
			.treeAsTokens(node)
			.readValueAs(type);
	}

	@SneakyThrows
	protected <T> T jsonToObject(TreeNode node, Class<T> type) {
		return objectMapper
			.treeAsTokens(node)
			.readValueAs(type);
	}

	private void logOpt(String s, Optional<? extends Object> object) throws JsonProcessingException {

		String className = object
			.map(Object::getClass)
			.map(Class::getName)
			.orElse("UNKNOWN class");
		log.info("{} [{}]: {}", s, className, new ObjectMapper().writeValueAsString(object.orElse(null)));
	}

	protected void createDocumentModelIfNotExist(String documentModelContent) {
		try {
			modelsClient.createModel(new StringReader(documentModelContent));
		} catch (A12ClientException e) {
			//document could not be created
		}
	}

	protected void cleanupRelationshipModel() {
		try {
			String relationshipMetaModel = modelsClient.loadModel(META_MODEL_NAME);
			relationshipClient.getModelGraph().getRelationshipModels().forEach(relationshipModel -> {
				try {
					Header header = headerParser.parseJson(relationshipModel);
					modelsClient.deleteModel(header.getId());
				} catch (HeaderParseException e) {
					log.warn("Cannot clean relationship model " + relationshipModel, e);
				}
			});
			if (relationshipMetaModel != null) {
				modelsClient.deleteModel(META_MODEL_NAME);
			}
		} catch (MissingDataException e) {
			//Imminent due to prevention of already deleted of non-existing relationship
		}
	}

	@SneakyThrows protected String createModelFromFile(String path) {
		return modelsClient.createModel(new StringReader(resourcePatternResolver.getResource("classpath:" + path)
			.getContentAsString(StandardCharsets.UTF_8)));
	}

	protected String createModelFromContent(String modelContent) {
		return modelsClient.createModel(new StringReader(modelContent));
	}

	protected DocumentReference createDocumentFromJson(String documentModel, String document) throws JsonProcessingException {
		JsonRpc2RequestBuilder rpcRequestBuilder = requestBuilderFactory.newJsonRpc2RequestBuilder();
		rpcRequestBuilder
			.addMethodCall(CoreOperationConstants.ADD_DOCUMENT_OPERATION)
			.id("AddDocument")
			.putParameter("documentModelName", documentModel)
			.putParameter("document", objectMapper.readTree(document));
		List<JsonRpc2Response> invoke = rpcOperationsClient.invoke(rpcRequestBuilder.build());
		JsonNode jsonNode = objectMapper.valueToTree(invoke.getFirst().getResult());
		return new DocumentReference(jsonNode.get("docRef").asText());
	}

	protected Header createHeader(String modelContent) {
		try {
			return headerParser.parseJson(modelContent);
		} catch (Exception e) {
			throw new InvalidInputException("Cannot parse JSON");
		}
	}

	protected void handleError(List<JsonRpc2Response> invoke) {
		invoke
			.stream()
			.map(JsonRpc2Response::getError)
			.filter(Objects::nonNull)
			.forEach(e -> {
					try {
						log.error("RPC error:\n{}", objectMapper.writeValueAsString(e));
						throw new GenericErrorException(e.getMessage(), objectMapper.treeAsTokens(e.getData()).readValueAs(ErrorDetail.class));
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
			);
	}
}
