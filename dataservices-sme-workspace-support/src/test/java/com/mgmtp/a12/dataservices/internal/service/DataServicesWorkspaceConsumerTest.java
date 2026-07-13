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
package com.mgmtp.a12.dataservices.internal.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.internal.service.importer.AttachmentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.DocumentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.LinkImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.MetadataImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.ModelImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.RoleImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.UserImporter;
import com.mgmtp.a12.dataservices.internal.model.SmeWorkspaceMetadata;
import com.mgmtp.a12.dataservices.wcf.domain.FileTuple;
import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.Header;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class DataServicesWorkspaceConsumerTest {

	@Mock private MetadataImporter metadataImporter;
	@Mock private UserImporter userImporter;
	@Mock private RoleImporter roleImporter;
	@Mock private ModelImporter modelImporter;
	@Mock private DocumentImporter documentImporter;
	@Mock private AttachmentImporter attachmentImporter;
	@Mock private LinkImporter linkImporter;

	private DataServicesWorkspaceConsumer consumer;
	private Path tempDir;

	@BeforeMethod
	void setUp() throws IOException {
		tempDir = Files.createTempDirectory("consumer-test");

		lenient().when(metadataImporter.doImport(any(byte[].class))).thenReturn(new SmeWorkspaceMetadata());

		consumer = new DataServicesWorkspaceConsumer(
			metadataImporter,
			userImporter,
			roleImporter,
			modelImporter,
			documentImporter,
			attachmentImporter,
			linkImporter
		);
	}

	@AfterMethod
	void tearDown() {
		try {
			FileUtils.deleteDirectory(tempDir.toFile());
		} catch (IOException e) {
			// ignore cleanup errors
		}
	}

	@Test(description = "Should route files and import in correct dependency order")
	void shouldImportInCorrectOrder() {
		byte[] metaContent = "{\"documents\":{}}".getBytes(StandardCharsets.UTF_8);
		byte[] userContent = "users:\n- admin".getBytes(StandardCharsets.UTF_8);
		byte[] roleContent = "roles:\n- admin".getBytes(StandardCharsets.UTF_8);
		byte[] docContent = "{\"doc\":true}".getBytes(StandardCharsets.UTF_8);
		byte[] attachContent = "binary-data".getBytes(StandardCharsets.UTF_8);
		byte[] linkContent = "{\"link\":true}".getBytes(StandardCharsets.UTF_8);

		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/meta/workspacedata_items.json",
			mockFileTuple(metaContent, "data/meta/workspacedata_items.json"));
		files.put("data/user/users.yaml",
			mockFileTuple(userContent, "data/user/users.yaml"));
		files.put("data/user/roles.yaml",
			mockFileTuple(roleContent, "data/user/roles.yaml"));
		files.put("data/documents/BusinessPartner/abc.json",
			mockFileTuple(docContent, "data/documents/BusinessPartner/abc.json"));
		files.put("data/attachments/attach-id.json",
			mockFileTuple(attachContent, "data/attachments/attach-id.json"));
		files.put("data/links/RelModel/link-id.json",
			mockFileTuple(linkContent, "data/links/RelModel/link-id.json"));

		ModelTuple model = mockModelTuple("TestModel", "{\"model\":true}");
		Workspace workspace = mockWorkspace(Map.of("TestModel", model), files);

		consumer.accept(workspace, tempDir);

		InOrder inOrder = inOrder(metadataImporter, userImporter, roleImporter, modelImporter,
			attachmentImporter, documentImporter, linkImporter);
		inOrder.verify(metadataImporter).doImport(metaContent);
		inOrder.verify(userImporter).doImport(userContent);
		inOrder.verify(roleImporter).doImport(roleContent);
		inOrder.verify(modelImporter).importModels(anyMap());
		inOrder.verify(attachmentImporter).importAttachment(eq("attach-id"), eq(attachContent), any(SmeWorkspaceMetadata.class));
		inOrder.verify(documentImporter).importDocument(eq("BusinessPartner"), eq("abc"), eq(docContent));
		inOrder.verify(linkImporter).importLink(eq("link-id"), eq(linkContent));
	}

	@Test(description = "Should pass metadata from MetadataImporter to attachment importer")
	void shouldPassMetadataToAttachmentImporter() {
		SmeWorkspaceMetadata expectedMetadata = new SmeWorkspaceMetadata();
		byte[] metaContent = "{\"documents\":{}}".getBytes(StandardCharsets.UTF_8);
		when(metadataImporter.doImport(metaContent)).thenReturn(expectedMetadata);

		byte[] attachContent = "binary-data".getBytes(StandardCharsets.UTF_8);
		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/meta/workspacedata_items.json",
			mockFileTuple(metaContent, "data/meta/workspacedata_items.json"));
		files.put("data/attachments/att-1",
			mockFileTuple(attachContent, "data/attachments/att-1"));

		Workspace workspace = mockWorkspace(Map.of(), files);
		consumer.accept(workspace, tempDir);

		verify(attachmentImporter).importAttachment(eq("att-1"), eq(attachContent), eq(expectedMetadata));
	}

	@Test(description = "Should route models from workspace to model importer")
	void shouldRouteModelsToImporter() {
		ModelTuple model1 = mockModelTuple("Model1", "{\"model1\":true}");
		ModelTuple model2 = mockModelTuple("Model2", "{\"model2\":true}");
		Map<String, ModelTuple> models = Map.of("Model1", model1, "Model2", model2);

		Workspace workspace = mockWorkspace(models, Map.of());
		consumer.accept(workspace, tempDir);

		verify(modelImporter).importModels(models);
	}

	@Test(description = "Should route document files extracting model name and entity ID from path")
	void shouldRouteDocumentsWithCorrectModelAndId() {
		byte[] content = "{\"doc\":true}".getBytes(StandardCharsets.UTF_8);
		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/documents/Contract/doc-123.json",
			mockFileTuple(content, "data/documents/Contract/doc-123.json"));

		Workspace workspace = mockWorkspace(Map.of(), files);
		consumer.accept(workspace, tempDir);

		verify(documentImporter).importDocument("Contract", "doc-123", content);
	}

	@Test(description = "Should read file content from input directory when FileTuple content is null")
	void shouldReadContentFromInputDir() throws IOException {
		Path inputDir = tempDir.resolve("in");
		Files.createDirectories(inputDir.resolve("data/user"));
		byte[] expectedContent = "roles:\n- admin".getBytes(StandardCharsets.UTF_8);
		Files.write(inputDir.resolve("data/user/roles.yaml"), expectedContent);

		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/user/roles.yaml",
			mockFileTuple(null, "data/user/roles.yaml"));

		Workspace workspace = mockWorkspace(Map.of(), files);
		when(workspace.getInputDir()).thenReturn(inputDir.toString());

		consumer.accept(workspace, tempDir);

		verify(roleImporter).doImport(expectedContent);
	}

	@Test(description = "Should skip files without outputPath set")
	void shouldSkipFilesWithoutOutputPath() {
		Map<String, FileTuple> files = new HashMap<>();
		files.put("some/unrelated/file.txt",
			mockFileTuple("content".getBytes(StandardCharsets.UTF_8), null));

		Workspace workspace = mockWorkspace(Map.of(), files);
		consumer.accept(workspace, tempDir);

		verify(userImporter, never()).doImport(any(byte[].class));
		verify(roleImporter, never()).doImport(any(byte[].class));
		verify(documentImporter, never()).importDocument(any(), any(), any());
		verify(attachmentImporter, never()).importAttachment(any(), any(), any());
		verify(linkImporter, never()).importLink(any(), any());
	}

	@Test(description = "Should use default empty metadata when no metadata file in workspace")
	void shouldUseDefaultMetadataWhenAbsent() {
		byte[] attachContent = "binary-data".getBytes(StandardCharsets.UTF_8);
		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/attachments/att-1",
			mockFileTuple(attachContent, "data/attachments/att-1"));

		Workspace workspace = mockWorkspace(Map.of(), files);
		consumer.accept(workspace, tempDir);

		verify(metadataImporter, never()).doImport(any(byte[].class));
		verify(attachmentImporter).importAttachment(eq("att-1"), eq(attachContent), any(SmeWorkspaceMetadata.class));
	}

	@Test(description = "Should skip non-JSON files like .DS_Store under document and link paths")
	void shouldSkipNonJsonFiles() {
		byte[] dsStoreContent = new byte[]{0, 0, 0, 1};
		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/documents/BusinessPartner/.DS_Store",
			mockFileTuple(dsStoreContent, "data/documents/BusinessPartner/.DS_Store"));
		files.put("data/documents/.DS_Store",
			mockFileTuple(dsStoreContent, "data/documents/.DS_Store"));
		files.put("data/links/.DS_Store",
			mockFileTuple(dsStoreContent, "data/links/.DS_Store"));

		Workspace workspace = mockWorkspace(Map.of(), files);
		consumer.accept(workspace, tempDir);

		verify(documentImporter, never()).importDocument(any(), any(), any());
		verify(linkImporter, never()).importLink(any(), any());
	}

	@Test(description = "Should handle empty workspace without errors")
	void shouldHandleEmptyWorkspace() {
		Workspace workspace = mockWorkspace(Map.of(), Map.of());
		consumer.accept(workspace, tempDir);

		verify(modelImporter).importModels(Map.of());
		verify(metadataImporter, never()).doImport(any(byte[].class));
		verify(userImporter, never()).doImport(any(byte[].class));
		verify(roleImporter, never()).doImport(any(byte[].class));
		verify(documentImporter, never()).importDocument(any(), any(), any());
		verify(attachmentImporter, never()).importAttachment(any(), any(), any());
		verify(linkImporter, never()).importLink(any(), any());
	}

	@Test(description = "Should not write any files to the output path")
	void shouldNotWriteToOutputPath() {
		byte[] metaContent = "{\"documents\":{}}".getBytes(StandardCharsets.UTF_8);
		byte[] userContent = "users:\n- admin".getBytes(StandardCharsets.UTF_8);

		Map<String, FileTuple> files = new HashMap<>();
		files.put("data/meta/workspacedata_items.json",
			mockFileTuple(metaContent, "data/meta/workspacedata_items.json"));
		files.put("data/user/users.yaml",
			mockFileTuple(userContent, "data/user/users.yaml"));

		ModelTuple model = mockModelTuple("TestModel", "{\"model\":true}");
		Workspace workspace = mockWorkspace(Map.of("TestModel", model), files);

		Path outputPath = tempDir.resolve("out");
		consumer.accept(workspace, outputPath);

		// The output path should not exist — consumer must not create it
		org.testng.Assert.assertFalse(Files.exists(outputPath),
			"Consumer should not write any files to the output path");
	}

	// -- Helper methods --

	private ModelTuple mockModelTuple(String id, String content) {
		ModelTuple tuple = mock(ModelTuple.class);
		Header header = mock(Header.class);
		lenient().when(header.getId()).thenReturn(id);
		lenient().when(tuple.getHeader()).thenReturn(header);
		lenient().when(tuple.getContent()).thenReturn(content);
		return tuple;
	}

	private FileTuple mockFileTuple(byte[] content, String outputPathValue) {
		FileTuple fileTuple = mock(FileTuple.class);
		lenient().when(fileTuple.getContent()).thenReturn(content);
		when(fileTuple.getOutputPath()).thenReturn(outputPathValue);
		return fileTuple;
	}

	private Workspace mockWorkspace(Map<String, ModelTuple> models, Map<String, FileTuple> files) {
		Workspace workspace = mock(Workspace.class);
		when(workspace.getModels()).thenReturn(models);
		when(workspace.getFiles()).thenReturn(files);
		return workspace;
	}
}
