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
package com.mgmtp.a12.dataservices.documentation.internal;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentHeaderOperation;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentUrlOperation;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadThumbnailUrlOperation;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.PathConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.constants.UserConstants;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.GetDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.ListValidationCodesOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.LoadThumbnailUrlsOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.ModifyDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.MultiDeleteDocumentsOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.PartialModifyDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.ValidateDocumentOperation;
import com.mgmtp.a12.dataservices.documentation.internal.domain.AbstractLoggedElement;
import com.mgmtp.a12.dataservices.documentation.internal.domain.OperationCall;
import com.mgmtp.a12.dataservices.model.operation.internal.ListModelsOperation;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.operation.internal.QueryOperation;
import com.mgmtp.a12.dataservices.query.projection.internal.DocumentProjectionImplementation;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.DeleteLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.ModifyLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.RelinkDocumentOperation;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.NonNull;
import lombok.SneakyThrows;

import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.ADD_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.ADD_LINK_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.COPY_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.DELETE_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.DELETE_LINK_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.GET_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.LIST_DOCUMENT_VALIDATION_CODES_INTERNAL_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.LIST_MODELS_INTERNAL_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.LOAD_ATTACHMENT_HEADER_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.LOAD_ATTACHMENT_URL_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.LOAD_THUMBNAIL_URLS_INTERNAL_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.LOAD_THUMBNAIL_URL_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.MODIFY_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.MODIFY_LINK_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.MULTI_DELETE_DOCUMENTS_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.PARTIAL_MODIFY_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.QUERY_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.RELINK_DOCUMENT_OPERATION;
import static com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants.VALIDATE_DOCUMENT_OPERATION;
import static org.testng.Assert.assertTrue;

@EnableAspectJAutoProxy
@Test public class DocumentationTest extends AbstractSpringContextIT {

	@Value("${com.mgmtp.a12.dataservices.documentation.outputDir:build/docs/calldoc}")
	private String outputDir;
	private DocumentReference contractDocumentReference;
	private DocumentReference bpDocumentReference;
	private final Set<Triple<String, AbstractLoggedElement, String>> diagramPaths = new HashSet<>();

	@Autowired private ObjectMapper objectMapper;
	@Autowired private DocumentationAspectAndEventListener documentationAspectAndEventListener;
	@Autowired private AttachmentService attachmentService;

	@Autowired private LoadAttachmentHeaderOperation loadAttachmentHeaderOperation;
	@Autowired private LoadAttachmentUrlOperation loadAttachmentUrlOperation;
	@Autowired private LoadThumbnailUrlOperation loadThumbnailUrlOperation;
	@Autowired private AddDocumentOperation addDocumentOperation;
	@Autowired private CopyDocumentOperation copyDocumentOperation;
	@Autowired private DeleteDocumentOperation deleteDocumentOperation;
	@Autowired private GetDocumentOperation getDocumentOperation;
	@Autowired private QueryOperation queryOperation;
	@Autowired private ModifyDocumentOperation modifyDocumentOperation;
	@Autowired private PartialModifyDocumentOperation partialModifyDocumentOperation;
	@Autowired private ValidateDocumentOperation validateDocumentOperation;
	@Autowired private AddLinkOperation addLinkOperation;
	@Autowired private DeleteLinkOperation deleteLinkOperation;
	@Autowired private ModifyLinkOperation modifyLinkOperation;
	@Autowired private RelinkDocumentOperation relinkDocumentOperation;
	@Autowired private ListModelsOperation listModelsOperation;
	@Autowired private ListValidationCodesOperation listValidationCodesOperation;
	@Autowired private LoadThumbnailUrlsOperation loadThumbnailUrlsOperation;
	@Autowired private MultiDeleteDocumentsOperation multiDeleteDocumentsOperation;
	@Autowired private ApplicationContext applicationContext;
	private RelationshipLinkSpec link;
	private final Set<Method> calledRemoteMethods = new HashSet<>();
	private AttachmentHeader attachment;

	@BeforeClass public void beforeTest() {
		setUserTo(UserConstants.ADMIN_USER);
		// Models of insurance domain
		modelsFunctions.createModels(
			PathConstants.ADDRESS_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.BUSINESS_PARTNER_SUPER_DOCUMENT_MODEL_PATH,
			PathConstants.COINSURED_ADDITIONAL_PARTNER_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_DOCUMENT_MODEL_PATH,
			PathConstants.CONTRACT_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PathConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_RELATIONSHIP_MODEL_PATH,
			PathConstants.PARTNER_ADDRESSES_RELATIONSHIP_MODEL_PATH,
			PathConstants.PARTNER_POSTAL_ADDRESS_RELATIONSHIP_MODEL_PATH
		);
		modelsFunctions.saveCdms(
			PathConstants.CONTRACT_CDM_MODEL_PATH,
			PathConstants.ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL_PATH
		);

		new File(outputDir).mkdirs();

	}

	private static boolean isRpcMethod(Method m) {
		return AnnotationUtils.findAnnotation(m, JsonRpcMethod.class) != null || Objects.equals("rpc", m.getName());
	}

	@SneakyThrows private void markMethodCalled(Object caller, String name, Class<?>... params) {
		Method m = caller.getClass().getMethod(name, params);
		calledRemoteMethods.add(m);
	}

	@BeforeMethod public void setUp() {
		documentationAspectAndEventListener.setActive(false);

		contractDocumentReference = addDocumentOperation.rpc(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, objectMapper.createObjectNode(), Locale.ENGLISH);
		bpDocumentReference = addDocumentOperation.rpc(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL, objectMapper.createObjectNode(), Locale.ENGLISH);

		attachment =
			attachmentService.createAttachment(IOUtils.toInputStream("text\n", StandardCharsets.UTF_8), "fileName.txt",
				contractDocumentReference.getDocumentModelName(), "/", Collections.emptyList());
		@NonNull AttachmentReference<?> attachmentReference = AttachmentReference.fromDocRef(contractDocumentReference.toString());
		attachmentHeaderService.assignAttachment(attachment, attachmentReference);

		link = addLinkOperation.rpc(new LinkDescriptor(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL, List.of(
			new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, contractDocumentReference),
			new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.PARTNER_ROLE, bpDocumentReference))), null);

		documentationAspectAndEventListener.reset();
		documentationAspectAndEventListener.setActive(true);
	}

	@AfterMethod public void tearDown(ITestResult result) {
		if (result.isSuccess()) {

			ITestNGMethod method = result.getMethod();

			documentationAspectAndEventListener.setActive(false);
			renderDiagram(new PlantumlSequenceRenderer(method.getDescription(), documentationAspectAndEventListener.getThreads()),
				method.getMethodName() + "Sequence");
			renderDiagram(new PlantumlTimingRenderer(method.getDescription(), documentationAspectAndEventListener.getThreads()),
				method.getMethodName() + "Timing");
		}
	}

	@SneakyThrows private void renderDiagram(AbstractRenderer plantumlSequenceRenderer, String diagramFile) {
		diagramPaths.add(
			Triple.of(plantumlSequenceRenderer.getTitle(), plantumlSequenceRenderer.getThreads().keySet().stream().findFirst().orElse(null), diagramFile));
		try (PrintStream output = new PrintStream(new File(outputDir, diagramFile + ".puml"), StandardCharsets.UTF_8)) {
			plantumlSequenceRenderer.render(output);
		}
	}

	@AfterClass public void afterTest() {
		renderAdoc("callSequence-puml.adoc", DocumentationTest::renderPuml);
		renderAdoc("callSequence.adoc", DocumentationTest::innerRenderSvg);
		diagramPaths.stream()
			.filter(a -> a.getRight().endsWith("Sequence"))
			.forEach(this::renderAdoc);
		assertMethodsCalled();
	}

	@SneakyThrows private void renderAdoc(Triple<String, ? extends AbstractLoggedElement, String> a) {
		try (PrintWriter adoc = new PrintWriter(new File(outputDir, getFileName(a.getMiddle()) + "_sequence.adoc"))) {
			innerRenderSvg(adoc, a);
		}
	}

	private static String getFileName(AbstractLoggedElement a) {
		return Optional.of(a)
			.filter(OperationCall.class::isInstance)
			.map((OperationCall.class::cast))
			.map(OperationCall::getOperation)
			.orElse(a.getName());
	}

	@SneakyThrows private void renderAdoc(String output, BiConsumer<PrintWriter, Triple<String, AbstractLoggedElement, String>> render) {
		try (PrintWriter adoc = new PrintWriter(new File(outputDir, output))) {
			adoc.println("= Sequences of calls for operations");
			adoc.println();
			final AtomicReference<String> title = new AtomicReference<>();
			diagramPaths.stream().sorted(Comparator.comparing(Triple::getRight))
				.forEach(p -> {
					if (!Objects.equals(title.get(), p.getLeft())) {
						title.set(p.getLeft());
						adoc.println();
						adoc.print("== ");
						adoc.print(title.get());
						adoc.println();
					}
					renderFigure(adoc, p);
					render.accept(adoc, p);
				});
		}
	}

	private static void renderFigure(PrintWriter adoc, Triple<String, ? extends AbstractLoggedElement, String> p) {
		adoc.print(".");
		adoc.print(p.getRight().endsWith("Timing") ? "Timing" : "Sequence");
		adoc.print(" of ");
		adoc.print(p.getLeft());
		adoc.println(" calls.");
	}

	private static void renderPuml(PrintWriter adoc, Triple<String, ? extends AbstractLoggedElement, String> p) {
		adoc.println("[plantuml, format=svg]");
		adoc.println("----");
		adoc.print("include::");
		adoc.print(p.getRight());
		adoc.println(".puml[]");
		adoc.println("----");
	}

	private static void innerRenderSvg(PrintWriter adoc, Triple<String, ? extends AbstractLoggedElement, String> p) {
		adoc.print("image::{resourcesPrefix}images/");
		adoc.print(p.getRight());
		adoc.println(".svg[]");
	}

	private void assertMethodsCalled() {
		Set<Method> rpcMethods = applicationContext.getBeansWithAnnotation(RemoteOperation.class).values().stream()
			.map(Object::getClass)
			.filter(c -> !c.getName().startsWith("com.mgmtp.a12.dataservices.examples"))
			.map(Class::getMethods)
			.flatMap(Arrays::stream)
			.filter(DocumentationTest::isRpcMethod)
			.filter(m -> !calledRemoteMethods.remove(m))
			.collect(Collectors.toSet());
		assertTrue(CollectionUtils.isEmpty(rpcMethods),
			String.format("RPC methods not called:%n  %s%nPlease add tests for it into the  %s test class.",
				rpcMethods.stream()
					.map(Method::toString)
					.collect(Collectors.joining("%n  ")),
				this.getClass().getCanonicalName()));
		assertTrue(CollectionUtils.isEmpty(calledRemoteMethods),
			String.format("Called methods are not RPC:%n  %s%nPLease check the %s class to fix the issue.",
				calledRemoteMethods.stream()
					.map(Method::toString)
					.collect(Collectors.joining("%n  ")),
				this.getClass().getCanonicalName()));
	}

	@Test(description = ADD_DOCUMENT_OPERATION) public void document_addDocumentOperation() {
		addDocumentOperation.rpc(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, objectMapper.createObjectNode(), Locale.ENGLISH);
		markMethodCalled(addDocumentOperation, "rpc", String.class, JsonNode.class, Locale.class);
	}

	@Test(description = GET_DOCUMENT_OPERATION) public void document_getDocumentOperation() {
		getDocumentOperation.rpc(contractDocumentReference);
		markMethodCalled(getDocumentOperation, "rpc", DocumentReference.class);
	}

	@Test(description = MODIFY_DOCUMENT_OPERATION) public void document_modifyDocumentOperation() {
		modifyDocumentOperation.rpc(contractDocumentReference, objectMapper.createObjectNode(), Locale.ENGLISH);
		markMethodCalled(modifyDocumentOperation, "rpc", DocumentReference.class, JsonNode.class, Locale.class);
	}

	@Test(description = DELETE_DOCUMENT_OPERATION) public void document_deleteDocumentOperation() {
		deleteDocumentOperation.rpc(contractDocumentReference, Locale.ENGLISH);
		markMethodCalled(deleteDocumentOperation, "rpc", DocumentReference.class, Locale.class);
	}

	@Test(description = MULTI_DELETE_DOCUMENTS_OPERATION) public void document_multiDeleteDocumentsOperation() {
		multiDeleteDocumentsOperation.rpc(List.of(contractDocumentReference));
		markMethodCalled(multiDeleteDocumentsOperation, "rpc", Collection.class);
	}

	@Test(description = VALIDATE_DOCUMENT_OPERATION) public void document_validateDocumentOperation() {
		validateDocumentOperation.rpc(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL, objectMapper.createObjectNode(), false, Locale.ENGLISH);
		markMethodCalled(validateDocumentOperation, "rpc", String.class, JsonNode.class, Boolean.class, Locale.class);
	}

	@Test(description = PARTIAL_MODIFY_DOCUMENT_OPERATION) public void document_partialModifyDocumentOperation() {
		partialModifyDocumentOperation.rpc(contractDocumentReference, List.of(), Locale.ENGLISH);
		markMethodCalled(partialModifyDocumentOperation, "rpc", DocumentReference.class, List.class, Locale.class);
	}

	@Test(description = COPY_DOCUMENT_OPERATION) public void document_copyDocumentOperation() {
		copyDocumentOperation.rpc(contractDocumentReference, Locale.ENGLISH);
		markMethodCalled(copyDocumentOperation, "rpc", DocumentReference.class, Locale.class);
	}

	@Test(description = QUERY_OPERATION) public void document_queryOperation() throws JsonProcessingException {
		queryOperation.rpc(QueryRoot.builder()
			.projectionName(DocumentProjectionImplementation.PROJECTION_NAME)
			.targetDocumentModel(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL)
			.paging(Paging.builder()
				.pageNumber(0)
				.pageSize(1)
				.build())
			.build());
		markMethodCalled(queryOperation, "rpc", QueryRoot.class);
	}

	@Test(description = LOAD_ATTACHMENT_HEADER_OPERATION) public void attachment_loadAttachmentHeaderOperation() {
		loadAttachmentHeaderOperation.rpc(attachment.getAttachmentId(), contractDocumentReference);
		markMethodCalled(loadAttachmentHeaderOperation, "rpc", String.class, DocumentReference.class);
	}

	@Test(description = LOAD_ATTACHMENT_URL_OPERATION) public void attachment_loadAttachmentUrlOperation() {
		loadAttachmentUrlOperation.rpc(attachment.getAttachmentId(), contractDocumentReference);
		markMethodCalled(loadAttachmentUrlOperation, "rpc", String.class, DocumentReference.class);
	}

	@Test(description = LOAD_THUMBNAIL_URL_OPERATION) public void attachment_loadThumbnailUrlOperation() {
		loadThumbnailUrlOperation.rpc(attachment.getAttachmentId());
		markMethodCalled(loadThumbnailUrlOperation, "rpc", String.class);
	}

	@Test(description = ADD_LINK_OPERATION) public void link_addLinkOperation() {
		addLinkOperation.rpc(new LinkDescriptor(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL, List.of(
			new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, contractDocumentReference),
			new RelationshipRoleSpec(RelationshipModelConstants.RoleConstants.PARTNER_ROLE, bpDocumentReference))), null);
		markMethodCalled(addLinkOperation, "rpc", LinkDescriptor.class, JsonNode.class);
	}

	@Test(description = DELETE_LINK_OPERATION) public void link_deleteLinkOperation() {
		deleteLinkOperation.rpc(link);
		markMethodCalled(deleteLinkOperation, "rpc", RelationshipLinkSpec.class);
	}

	@Test(description = MODIFY_LINK_OPERATION) public void link_modifyLinkOperation() {
		modifyLinkOperation.rpc(link, null);
		markMethodCalled(modifyLinkOperation, "rpc", RelationshipLinkSpec.class, JsonNode.class);
	}

	@Test(description = RELINK_DOCUMENT_OPERATION) public void link_relinkDocumentOperation() {
		relinkDocumentOperation.rpc(link.getLinkDescriptor(), link.getId());
		markMethodCalled(relinkDocumentOperation, "rpc", LinkDescriptor.class, String.class);
	}

	@Test(description = LIST_MODELS_INTERNAL_OPERATION) public void model_listModelsByTypeOperation() {
		markMethodCalled(listModelsOperation, "rpc", Collection.class);
		throw new SkipException("Internal operations are not documented.");
	}

	@Test(description = LIST_DOCUMENT_VALIDATION_CODES_INTERNAL_OPERATION) public void model_listDocumentValidationCodesOperation() {
		markMethodCalled(listValidationCodesOperation, "rpc", Collection.class);
		throw new SkipException("Internal operations are not documented.");
	}

	@Test(description = LOAD_THUMBNAIL_URLS_INTERNAL_OPERATION) public void attachment_loadThumbnailUrlsInternalOperation() {
		markMethodCalled(loadThumbnailUrlsOperation, "rpc");
		throw new SkipException("Internal operations are not documented.");
	}

}
