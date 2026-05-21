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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.contentstore.autoconfigure.internal.validation.condition.AbstractContentStoreCondition;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.internal.CleanUpDirtyAttachmentsJob;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentHeaderOperation;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentUrlOperation;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadThumbnailUrlOperation;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.AbstractDataServicesCondition;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.DocumentJpaRepository;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.bulkload.internal.CollapsingDocumentModelReferenceResolver;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.internal.DefaultModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.GenericModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.operation.internal.QueryOperation;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.RelationshipRole;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationListener;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.ExceptionDetail;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2Response;
import com.mgmtp.a12.dataservices.rpc.JsonRpc2ResponseError;
import com.mgmtp.a12.dataservices.rpc.OperationError;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher;
import com.mgmtp.a12.dataservices.rpc.internal.jpa.repository.RequestIdRepository;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Base test class with all necessary configurations to run repository/service tests
 */
@Slf4j
@WithUserDetails("test")
@TestPropertySource(
	locations = "classpath:services-version.properties",
	properties = {
		"spring.datasources.dataservices.embedded-postgres.enabled=true",
		"spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
		"spring.datasources.contentstore.embedded-postgres.enabled=true",
		"spring.main.allow-bean-definition-overriding=true"
	})
@TestExecutionListeners(
	listeners = {
		WithSecurityContextTestExecutionListener.class,
		MockitoTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
	},
	mergeMode = MergeMode.MERGE_WITH_DEFAULTS
)
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@SpringBootTest(classes = { InitialITConfiguration.class })
public abstract class AbstractSpringContextIT extends AbstractTestNGSpringContextTests {


	public static final String TEST_USER_WITH_NO_ACCESS_RIGHTS = "testUserWithNoAccessRights";

	// Content store
	protected static final String SECURED_ATTACHMENT_ID = "da6c08f5-1dc7-4c43-9099-6dab9d735230";
	protected static final String PUBLIC_ATTACHMENT_ID = "eb7d1906-1dc7-4c43-9099-7ebcae846341";
	protected static final String CONTENT = "CONTENT";
	protected static final String FILENAME = "filename";

	protected static final Predicate<String> ATTACHMENT_SECURED_URL_PATTERN =
		Pattern.compile("^http://localhost:[0-9]+/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\?filename=.*\\.png$").asPredicate();
	protected static final Predicate<String> PUBLIC_URL_PATTERN =
		Pattern.compile("^http://localhost:[0-9]+/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").asPredicate();

	protected static final Predicate<String> RELATIVE_THUMBNAIL_URL_PATTERN =
		Pattern.compile("^/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").asPredicate();
	protected static final Predicate<String> RELATIVE_ATTACHMENT_URL_PATTERN =
		Pattern.compile("^/cs/download/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\?filename=.*\\.png$").asPredicate();

	private final TestEnvironmentCleaner testEnvironmentCleaner = new TestEnvironmentCleaner();

	@Autowired protected ResourcePatternResolver resourcePatternResolver;
	@Autowired protected JsonRpcOperationDispatcher rpcOperationDispatcher;
	@Autowired protected DocumentJpaRepository documentJpaRepository;
	@Autowired protected IDocumentRepository documentRepository;
	@Autowired protected HeaderParser headerParser;
	@Autowired protected ModelJpaRepository modelRepository;
	@Autowired protected ModelHeaderJpaRepository modelHeaderJpaRepository;
	@Autowired protected ModelService modelService;
	@Autowired protected GenericModelLoader genericModelLoader;
	@Autowired protected IModelLoader<IDocumentModel> documentModelLoader;
	@Autowired protected DocumentModelUtils documentModelUtils;
	@Autowired protected IDocumentModelResolver documentModelResolver;
	@Autowired protected IDocumentModelService documentModelService;
	@Autowired protected ModelPermissionEvaluator<Model> modelPermissionEvaluator;
	@Autowired protected IDocumentModelSerializer documentModelSerializer;
	@Autowired protected DocumentSupport documentSupport;
	@Autowired protected DocumentService documentService;
	@Autowired protected DocumentModelServiceFactory documentModelServiceFactory;
	@Autowired protected AttachmentService attachmentService;
	@Autowired protected AttachmentHeaderService attachmentHeaderService;
	@Autowired protected AttachmentTestFunctions attachmentTestFunctions;
	@Autowired protected CleanUpDirtyAttachmentsJob cleanUpDirtyAttachmentsJob;
	@Autowired protected DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired protected ObjectMapper objectMapper;
	@Autowired protected AddLinkOperation addLinkOperation;
	@Autowired @Qualifier("dsDataSource") protected DataSource dataSource;
	@Autowired @Qualifier("contentstoreDataSource") protected Optional<DataSource> contentStoreDataSource;
	@Autowired private CacheManager cacheManager;
	@Autowired private RelationshipLinkValidationListener linkValidator;
	@Autowired protected UserDetailsService userDetailsService;
	@Autowired protected ModelCacheManager modelCacheManager;
	@Autowired RelationshipLinkService relationshipLinkService;
	@Autowired protected RequestIdRepository requestIdRepository;
	@Autowired protected BackendAuthenticationService backendAuthenticationService;
	@Autowired protected Locale defaultLocale;
	@Autowired protected CollapsingDocumentModelReferenceResolver.CollapsingDocumentModelReferenceResolverFactory
		collapsingDocumentModelReferenceResolverFactory;
	@Autowired protected QueryService queryService;
	@Autowired protected QueryOperation queryOperation;

	@Autowired protected LoadAttachmentHeaderOperation loadAttachmentHeaderOperation;
	@Autowired protected LoadThumbnailUrlOperation loadThumbnailUrlOperation;
	@Autowired protected LoadAttachmentUrlOperation loadAttachmentUrlOperation;
	@Autowired protected IAttachmentRepository attachmentRepository;
	@Autowired private DefaultModelRepository defaultModelRepository;

	@Autowired protected DocumentFunctions documentFunctions;
	@Autowired protected ResourceFunctions resourceFunctions;
	@Autowired protected ModelsFunctions modelsFunctions;
	@Autowired protected LinksFunctions linksFunctions;

	/**
	 * General helper for temporary overwriting value of private fields by Reflections.
	 *
	 * CAUTION: Be careful, this method is not thread-safe!
	 *
	 * @param classFieldName name of field to overwrite
	 * @param newValue new value to be used inside the nested block
	 * @param objectInstance instance of object to overwrite value on
	 * @param closure consumer accepting the instance with overwritten value. The value will be reverted after consumer call.
	 * @param <T> type of the field
	 * @param <U> type of instance of the object on which overwriting applies
	 * @param <E> exception allowed throwing from inside the consumer
	 * @throws E exception allowed throwing from inside the consumer
	 */
	protected static <T, U, E extends Throwable> void runWithFieldOverwritten(String classFieldName, T newValue, U objectInstance, ThrowingRunnable<E> closure)
		throws E {
		T oldValue = (T) ReflectionTestUtils.getField(objectInstance, classFieldName);
		ReflectionTestUtils.setField(objectInstance, classFieldName, newValue);
		try {
			closure.run();
		} finally {
			ReflectionTestUtils.setField(objectInstance, classFieldName, oldValue);
		}

	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@BeforeClass public void cleanUpTestEnvironment() {
		testEnvironmentCleaner.cleanUpTestEnvironment(dataSource, cacheManager, contentStoreDataSource);
	}

	@Order(0)
	@BeforeClass protected void initialize() {
		reboundDataServicesCoreProperties();
		reboundContentStoreCoreProperties();
		backendAuthenticationService.executeWithBackendAuthentication(UserConstants.ADMIN_USER, this::initializeWithSecurityBypassWrapper);
	}

	@SneakyThrows private Void initializeWithSecurityBypassWrapper() {
		initializeWithSecurityBypass();
		return null;
	}

	/**
	 * Since we have the "boundProperties" in {@link AbstractDataServicesCondition} as static dependency, which makes
	 * running test with multiple instances fails because the static dependency won't reset its default value.
	 */
	private void reboundDataServicesCoreProperties() {
		ReflectionTestUtils.setField(AbstractDataServicesCondition.class, "boundProperties", Optional.empty());
	}

	private void reboundContentStoreCoreProperties() {
		ReflectionTestUtils.setField(AbstractContentStoreCondition.class, "boundProperties", Optional.empty());
	}

	protected void initializeWithSecurityBypass() throws Exception {
		// Empty but subclass can override it to have custom initialization here
	}

	@BeforeMethod public void clearAllOperationCaches() {
		/* ADD_LINK and DELETE_LINK operations add LinkRefs to the caches which are only cleared in the Dispatcher. If the dispatcher is not called the caches
		are not evicted and the next run of dispatcher might fail because of the invalid links in the caches. Caches are stored in ThreadLocal variables which
		are reused in tests. Therefore those caches need to be evicted before each test run */

		linkValidator.clearLinks();
	}

	public RelationshipLink makeRelationshipLink(String relationshipModelName, Collection<RelationshipRole> roles, DocumentReference linkDocumentDocRef) {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(relationshipModelName);
		List<RelationshipRoleSpec> roleSpecs = roles.stream()
			.map(role -> {
				RelationshipRoleSpec roleSpec = new RelationshipRoleSpec();
				roleSpec.setRole(role.getName());
				roleSpec.setDocRef(role.getDocRef());
				roleSpec.setModelName(role.getDocRef().getDocumentModelName());
				return roleSpec;
			}).toList();
		linkDescriptor.setEntities(roleSpecs);
		return Objects.isNull(linkDocumentDocRef) ?
			relationshipLinkService.create(linkDescriptor) :
			relationshipLinkService.create(linkDescriptor, linkDocumentDocRef);
	}

	@SneakyThrows
	protected String loadResourceFromClasspathAsString(final String relativePath) {
		final Resource resource = resourcePatternResolver.getResource(String.format("classpath:%s", relativePath));
		final Writer stringWriter = new StringWriter();
		IOUtils.copy(resource.getInputStream(), stringWriter, StandardCharsets.UTF_8);

		String content = stringWriter.toString();
		if (SystemUtils.IS_OS_WINDOWS) {
			content = content.replace("\r\n", "\n");
		}

		if (SystemUtils.IS_OS_MAC) {
			content = content.replace("\r", "\n");
		}

		return content;
	}

	protected Reader loadResourceAsReader(final String relativePath) throws IOException {
		final Resource resource = resourcePatternResolver.getResource(String.format("classpath:%s", relativePath));
		return new InputStreamReader(resource.getInputStream());
	}

	protected void createInvalidModel(String modelContent) throws HeaderParseException {
		Header headerFromEvent = headerParser.parseJson(modelContent);
		modelPermissionEvaluator.checkModelCreatePermission(headerFromEvent);
		defaultModelRepository.save(headerFromEvent, modelContent);
		modelHeaderJpaRepository.save(new ModelHeaderEntity(headerFromEvent));
	}

	protected GenericModel createModel(String modelContent) {
		return modelService.create(modelContent);
	}

	protected List<JsonRpc2Response> sendRpcRequest(String request) throws IOException {
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		rpcOperationDispatcher.handleRequest(IOUtils.toInputStream(request, StandardCharsets.UTF_8), response);
		return objectMapper.readValue(response.toString(), new TypeReference<>() {});
	}

	protected <T> T convertResponse(String value, Class<T> clazz) {
		try {
			return objectMapper.readValue(value, clazz);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot parse response data");
		}
	}

	@SneakyThrows
	protected OperationError createOperationError(JsonRpc2Response inputResponse) {
		ExceptionDetail exceptionDetail = objectMapper.treeToValue(inputResponse.getError().getData(), ExceptionDetail.class);
		return OperationError.builder()
			.operationId(exceptionDetail.getSource())
			.level(ErrorLevel.ERROR)
			.shortMessage(exceptionDetail.getTitle())
			.longMessage(exceptionDetail.getDescription())
			.build();
	}

	protected List<JsonRpc2Response> handleErrors(List<JsonRpc2Response> sendRpcRequest) {
		sendRpcRequest.stream()
			.filter(m -> !m.isSuccess())
			.forEach(m -> {
					JsonRpc2ResponseError e = m.getError();
					try (JsonParser jsonParser = objectMapper.treeAsTokens(e.getData())) {
						log.error("Unable to cleanup links:\n{}", objectMapper.writeValueAsString(e));
						try {
							ExceptionDetail exceptionDetail = jsonParser.readValueAs(ExceptionDetail.class);
							throw new RpcException(e.getMessage(), OperationError.builder()
								.operationId(m.getId())
								.level(ErrorLevel.ERROR)
								.genericMessage()
								.errorDetail(exceptionDetail.getDetails())
								.build());
						} catch (JsonMappingException jsonMappingException) {
							ErrorDetail errorDetail = jsonParser.readValueAs(ErrorDetail.class);
							throw new RpcException(e.getMessage(), OperationError.builder()
								.operationId(m.getId())
								.level(ErrorLevel.ERROR)
								.genericMessage()
								.errorDetail(errorDetail)
								.build());
						}
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
			);
		return sendRpcRequest;
	}

	protected void assertAttachmentExists(DocumentReference documentReference, String attachmentId) {
		Assert.assertTrue(attachmentExists(documentReference, attachmentId));
	}

	public boolean attachmentExists(DocumentReference documentReference, String attachmentId) {
		return attachmentService.findAttachmentUrl(attachmentId, documentReference).isPresent();
	}

	protected LinkDescriptor createLinkDescriptor(String relationship, String role1, DocumentReference docRef1, String role2, DocumentReference docRef2) {
		LinkDescriptor linkDescriptor = new LinkDescriptor();
		linkDescriptor.setRelationshipModel(relationship);
		RelationshipRoleSpec relationshipRoleSpec1 = new RelationshipRoleSpec(role1, docRef1);
		RelationshipRoleSpec relationshipRoleSpec2 = new RelationshipRoleSpec(role2, docRef2);
		linkDescriptor.setEntities(Arrays.asList(relationshipRoleSpec1, relationshipRoleSpec2));
		return linkDescriptor;
	}

	@SneakyThrows protected String addJoinInfo(String modelStringContent) {
		JsonNode jsonTree = objectMapper.readTree(modelStringContent);
		String modelName = jsonTree.at("/header/id").textValue();
		ObjectNode content = (ObjectNode) jsonTree.get("content");
		if (!content.has("modelInfo")) {
			content.set("modelInfo", objectMapper.createObjectNode());
		}
		ObjectNode modelInfo = (ObjectNode) content.get("modelInfo");
		modelInfo.put("joinedModelsInfo", "%s-SelectionModel_V1+Sel".formatted(modelName));
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
	}

	protected interface ThrowingRunnable<E extends Throwable> {
		void run() throws E;
	}

	protected void setUserTo(String username) {
		UaaTestHelper.setCurrentUserName(userDetailsService.loadUserByUsername(username));
	}

	protected void assertUserReadPermissionToModel(ModelPermissionEvaluator modelPermissionEvaluator, String user, String modelId,
		boolean expectedAccess) {
		setUserTo(user);
		String message = String.format("Expected user [%s] to have permission [%s] to read model [%s] but it was [%s]", user, expectedAccess, modelId,
			!expectedAccess);
		Assert.assertEquals(modelPermissionEvaluator.hasModelReadPermission(findHeaderByModelName(modelId)), expectedAccess, message);
	}

	protected ModelHeaderEntity findHeaderByModelName(String modelName) {
		return modelHeaderJpaRepository.findById(modelName)
			.orElseThrow(() -> new NotFoundException("Header with model name '" + modelName + "' not found"));
	}

	protected ModelEntity findModelByName(String modelName) {
		return modelRepository.findById(modelName)
			.orElseThrow(() -> new NotFoundException("Model with name '" + modelName + "' not found"));
	}

	@SneakyThrows
	public DataServicesDocument createDocument(String documentModel, String pathToDocument) {
		String jsonDocument = resourceFunctions.loadResource(pathToDocument);
		DocumentV2 document = documentSupport.convertJSONToDocument(documentModel, new StringReader(jsonDocument));
		return documentService.create(document, defaultLocale);
	}
}
