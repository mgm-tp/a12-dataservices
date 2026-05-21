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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mgmtp.a12.dataservices.AbstractKernelAwareTest;
import com.mgmtp.a12.dataservices.TestResourceRelationshipModelLoader;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.internal.DefaultAttachmentService;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentAnnotationEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentAnnotationJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentHeaderJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.DirtyAttachmentJpaRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.SeedDataProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.constants.RelationshipModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.DocumentJpaRepository;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.document.support.internal.DefaultDocumentSupport;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.AttachmentExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.DocumentExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.LinkExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.MetadataExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.ModelExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.RoleExporter;
import com.mgmtp.a12.dataservices.internal.service.exporter.UserExporter;
import com.mgmtp.a12.dataservices.internal.service.importer.AttachmentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.DocumentImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.LinkImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.MetadataImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.ModelImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.RoleImporter;
import com.mgmtp.a12.dataservices.internal.service.importer.UserImporter;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.GenericModelContent;
import com.mgmtp.a12.dataservices.model.LocalUserHolder;
import com.mgmtp.a12.dataservices.model.SeedMetadata;
import com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration;
import com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImportException;
import com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImporter;
import com.mgmtp.a12.dataservices.model.internal.ModelCacheManager;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.LocalizedFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.relationship.internal.DataServicesRelationshipLink;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipLinkEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.entity.RelationshipRoleEntity;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipRoleJpaRepository;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;
import com.mgmtp.a12.dataservices.wcf.IWorkspaceConsumer;
import com.mgmtp.a12.dataservices.wcf.IWorkspaceSupplier;
import com.mgmtp.a12.dataservices.wcf.WorkspaceConversionService;
import com.mgmtp.a12.dataservices.wcf.WorkspaceFactory;
import com.mgmtp.a12.dataservices.wcf.internal.DefaultWorkspaceConsumer;
import com.mgmtp.a12.dataservices.wcf.internal.DefaultWorkspaceConversionService;
import com.mgmtp.a12.dataservices.wcf.internal.DefaultWorkspaceSupplier;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;
import com.mgmtp.a12.uaa.authentication.backend.AuthenticatedUserLoader;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;
import com.mgmtp.a12.uaa.authentication.principal.RoleMappingDataHolder;
import com.mgmtp.a12.uaa.authentication.principal.a12internal.RoleMappingProcessor;
import com.mgmtp.a12.uaa.authentication.principal.autoconfigure.AuthenticationPrincipalExtensionProperties;
import com.mgmtp.a12.uaa.authentication.user.LocalUser;
import com.mgmtp.a12.uaa.authentication.user.LocalUserManager;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SeedDataServiceTest extends AbstractKernelAwareTest {

	private final DataServicesCoreProperties dataServicesCoreProperties = spy(new DataServicesCoreProperties());
	private final SeedDataProperties seedDataProperties = spy(new SeedDataProperties());
	private final AuthenticationPrincipalExtensionProperties authenticationPrincipalExtensionProperties = spy(new AuthenticationPrincipalExtensionProperties());
	private final AuthenticatedUserLoader backendUserLoader = mock(AuthenticatedUserLoader.class);
	private final BackendAuthenticationService backendAuthenticationService = spy(new BackendAuthenticationService(true));
	private final CacheManager cacheManager = mock(CacheManager.class);
	private final ModelCacheManager modelCacheManager = mock(ModelCacheManager.class);
	private final RelationshipLinkJpaRepository relationshipLinkJpaRepository = mock(RelationshipLinkJpaRepository.class);
	private final RelationshipRoleJpaRepository relationshipRoleJpaRepository = mock(RelationshipRoleJpaRepository.class);
	private final DocumentJpaRepository documentJpaRepository = mock(DocumentJpaRepository.class);
	private final DefaultDocumentRepository defaultDocumentRepository = mock(DefaultDocumentRepository.class);
	private final QueryIndexManager queryIndexManager = mock(QueryIndexManager.class);
	private final ModelHeaderJpaRepository modelHeaderJpaRepository = mock(ModelHeaderJpaRepository.class);
	private final ModelJpaRepository modelJpaRepository = mock(ModelJpaRepository.class);
	private final IModelRepository modelRepository = mock(IModelRepository.class);
	private final ModelFieldsJpaRepository modelFieldsJpaRepository = mock(ModelFieldsJpaRepository.class);
	private final LocalizedFieldsJpaRepository localizedFieldsJpaRepository = mock(LocalizedFieldsJpaRepository.class);
	private final AttachmentAnnotationJpaRepository attachmentAnnotationJpaRepository = mock(AttachmentAnnotationJpaRepository.class);
	private final AttachmentHeaderJpaRepository attachmentHeaderJpaRepository = mock(AttachmentHeaderJpaRepository.class);
	private final AttachmentReferenceJpaRepository attachmentReferenceJpaRepository = mock(AttachmentReferenceJpaRepository.class);
	private final DirtyAttachmentJpaRepository dirtyAttachmentJpaRepository = mock(DirtyAttachmentJpaRepository.class);
	private final IAttachmentRepository attachmentRepository = mock(IAttachmentRepository.class);
	private final EntityManager entityManager = mock(EntityManager.class);
	private final RestTemplate restTemplate = mock(RestTemplate.class);
	private final DsResourceUtils dsResourceUtils = mock(DsResourceUtils.class);
	private final ResourcePatternResolver resourcePatternResolver = mock(ResourcePatternResolver.class);
	private final RelationshipModelLoader relationshipModelLoader = mock(RelationshipModelLoader.class);
	private final RelationshipLinkFactory relationshipLinkFactory = mock(RelationshipLinkFactory.class);
	private final RelationshipLinkRepository relationshipLinkRepository = mock(RelationshipLinkRepository.class);
	private final ModelBulkImporter modelBulkImporter = mock(ModelBulkImporter.class);
	private final DefaultAttachmentService defaultAttachmentService = mock(DefaultAttachmentService.class);
	private final DocumentService documentService = mock(DocumentService.class);
	private final RoleMappingProcessor roleMappingProcessor = mock(RoleMappingProcessor.class);
	private final LocalUserManager localUserManager = mock(LocalUserManager.class);
	private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

	private DataServicesRelationshipLink dataServicesRelationshipLink;

	private final DefaultRelationshipModelSerializer relationshipModelSerializer =
		spy(new DefaultRelationshipModelSerializer(kernelTestSupport.getJsonMapper()));
	private final DocumentSupport documentSupport = spy(new DefaultDocumentSupport(
		kernelTestSupport.getDocumentDeserializationConfig(),
		kernelTestSupport.getDocumentSerializationConfig(),
		kernelTestSupport.getDocumentModelResolver(),
		kernelTestSupport.getDocumentV2Serializer()
	));
	private final HeaderParser headerParser = spy(new DefaultHeaderParser());
	private final WorkspaceFactory workspaceFactory = spy(WorkspaceFactory.getInstance());
	private final IWorkspaceConsumer workspaceConsumer = spy(new DefaultWorkspaceConsumer());
	private final IWorkspaceSupplier workspaceSupplier = spy(new DefaultWorkspaceSupplier(workspaceFactory, headerParser));
	private final SeedDataWorkspaceConverter seedDataWorkspaceConverter = spy(new SeedDataWorkspaceConverter());
	private final WorkspaceConversionService workspaceConversionService =
		spy(new DefaultWorkspaceConversionService(List.of(seedDataWorkspaceConverter), workspaceSupplier, workspaceConsumer));

	// Importers need to be manually created and injected for testing
	private final MetadataImporter metadataImporter = spy(new MetadataImporter(seedDataProperties, jsonMapper));
	private final UserImporter userImporter = spy(new UserImporter(authenticationPrincipalExtensionProperties, Optional.of(localUserManager)));
	private final RoleImporter roleImporter = spy(new RoleImporter(authenticationPrincipalExtensionProperties, roleMappingProcessor));
	private final ModelImporter modelImporter = spy(new ModelImporter(modelBulkImporter, new BulkImporterConfiguration()));
	private final DocumentImporter documentImporter = spy(new DocumentImporter(documentService, documentSupport));
	private final AttachmentImporter attachmentImporter = spy(new AttachmentImporter(defaultAttachmentService));
	private final LinkImporter linkImporter = spy(new LinkImporter(relationshipLinkRepository, relationshipLinkFactory, jsonMapper));

	// Exporters need to be manually created and injected for testing
	private final MetadataExporter metadataExporter = spy(new MetadataExporter(seedDataProperties, dsResourceUtils, jsonMapper));
	private final UserExporter userExporter = spy(new UserExporter(authenticationPrincipalExtensionProperties, resourcePatternResolver));
	private final RoleExporter roleExporter = spy(new RoleExporter(authenticationPrincipalExtensionProperties));
	private final ModelExporter modelExporter = spy(new ModelExporter(modelHeaderJpaRepository, modelRepository));
	private final DocumentExporter documentExporter = spy(new DocumentExporter(modelHeaderJpaRepository, defaultDocumentRepository, jsonMapper));
	private final AttachmentExporter attachmentExporter = spy(new AttachmentExporter(attachmentRepository, httpServletRequest));
	private final LinkExporter linkExporter = spy(new LinkExporter(relationshipModelLoader, relationshipLinkJpaRepository, jsonMapper));

	private final SeedDataService seedDataService = spy(new SeedDataService(backendAuthenticationService, dataServicesCoreProperties, cacheManager,
		modelCacheManager, relationshipLinkJpaRepository, relationshipRoleJpaRepository, documentJpaRepository, queryIndexManager,
		modelHeaderJpaRepository, modelJpaRepository, modelFieldsJpaRepository, localizedFieldsJpaRepository, attachmentHeaderJpaRepository,
		attachmentReferenceJpaRepository, attachmentAnnotationJpaRepository, dirtyAttachmentJpaRepository, entityManager, workspaceConversionService,
		metadataImporter, userImporter, roleImporter, modelImporter, documentImporter, attachmentImporter, linkImporter, metadataExporter, userExporter,
		roleExporter, modelExporter, documentExporter, attachmentExporter, linkExporter));

	protected final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
	protected final ObjectMapper yamlObjectMapper = YAMLMapper.builder()
		.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
		.build();
	private final String attachmentId = "5b7f156d-a7b9-48a7-92d6-023c787b3e79";
	private final String documentId = "5b7f156d-a7b9-48a7-92d6-023c787b3e79";
	private final String linkId = "5c041644-96ce-4d1a-b72c-297a2d1be26e";

	@DataProvider public static Object[][] seedDataImportProvider() {
		return new Object[][] {
			new Object[] {
				"Import with models", true
			}, new Object[] {
			"Import without models", false
		},
		};
	}

	// ========================================
	// Test fixture builders
	// ========================================

	private AttachmentHeaderEntity createTestAttachmentHeader() {
		AttachmentHeaderEntity attachmentHeader = new AttachmentHeaderEntity();
		attachmentHeader.setId(UUID.randomUUID().toString());
		attachmentHeader.setFileName(RandomStringUtils.secure().nextAlphabetic(10));
		attachmentHeader.setMimeType("application/json");
		attachmentHeader.setTypeOfTheContent(TypeOfTheContent.ATTACHMENT_SECURED.toString());
		attachmentHeader.setAnnotations(List.of(
			AttachmentAnnotationEntity.builder()
				.id(1L)
				.name("foo")
				.annotationValue("bar")
				.build()
		));
		return attachmentHeader;
	}

	private void setupCommonMocksForImport(Path metadataFile, Path roleFile, Path adminFile) throws IOException, URISyntaxException {
		doReturn(List.of(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL)).when(modelBulkImporter).doImport(any(), any());
		dataServicesRelationshipLink = Mockito.mock(DataServicesRelationshipLink.class);
		doReturn(dataServicesRelationshipLink).when(relationshipLinkFactory).createLink(any(), any(), any());

		authenticationPrincipalExtensionProperties.setAccessRightsResource(new FileUrlResource(roleFile.toFile().getAbsolutePath()));
		authenticationPrincipalExtensionProperties.getLocalConfig()
			.setUserResources(new Resource[] { new FileUrlResource(adminFile.toFile().getAbsolutePath()) });

		seedDataProperties.getSeedData().getMetaData().setPath(metadataFile.toString());
	}

	private void setupCommonMocksForExport(AttachmentHeaderEntity attachmentHeader, String mockedAttachmentContent, AttachmentUrl attachmentUrl) {
		when(attachmentHeaderJpaRepository.findAll()).thenReturn(List.of(attachmentHeader));
		when(dsResourceUtils.getResource(seedDataProperties.getSeedData().getMetaData().getPath())).thenReturn(
			defaultResourceLoader.getResource("/meta/seed_metadata.json")
		);
		when(attachmentRepository.findUrl(attachmentHeader.getId(), attachmentHeader.getFileName(),
			TypeOfTheContent.valueOf(attachmentHeader.getTypeOfTheContent())))
			.thenReturn(Optional.of(attachmentUrl));
		when(restTemplate.exchange(attachmentUrl.getLocation(), HttpMethod.GET, null, byte[].class)).thenReturn(
			ResponseEntity.ok(mockedAttachmentContent.getBytes())
		);
	}

	// ========================================
	// Verification helpers
	// ========================================

	private void verifyImportedMetadata(Path metadataFile) throws IOException {
		assertEquals(Files.readAllBytes(metadataFile),
			defaultResourceLoader.getResource("classpath:import/data/meta/seed_metadata.json").getContentAsByteArray());
	}

	private void verifyAttachmentCreation() {
		verify(defaultAttachmentService, times(1)).createSecuredAttachment(
			eq(attachmentId),
			any(),
			eq("PassportScan_TBajus.jpg"),
			argThat(this::verifyAttachmentAnnotations)
		);
	}

	private boolean verifyAttachmentAnnotations(List<?> annotations) {
		assertEquals(annotations.size(), 1);
		com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation annotation =
			(com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation) annotations.getFirst();
		assertEquals(annotation.getName(), "foo");
		assertEquals(annotation.getValue(), "bar");
		return true;
	}

	private void verifyRelationshipLinkCreation() {
		verify(relationshipLinkFactory, times(1)).createLink(
			argThat(this::verifyLinkDescriptor),
			eq(new DocumentReference("CoInsuredAdditionalFields/94b3200f-80f7-4f52-99de-03807344534f")),
			argThat(this::verifyComputedRank)
		);
	}

	private boolean verifyLinkDescriptor(Object linkDes) {
		var descriptor = (com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor) linkDes;
		assertEquals(descriptor.getRelationshipModel(), RelationshipModelConstants.CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL);
		assertEquals(descriptor.getEntities().get(0).getRole(), RelationshipModelConstants.RoleConstants.PARTNER_ROLE);
		assertEquals(descriptor.getEntities().get(0).getModelName(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		assertEquals(descriptor.getEntities().get(0).getDocRef().toString(), "BusinessPartner/e9c11dd1-32c3-42d3-a850-6431b79cfb07");
		assertEquals(descriptor.getEntities().get(1).getRole(), RelationshipModelConstants.RoleConstants.CONTRACT_ROLE);
		assertEquals(descriptor.getEntities().get(1).getModelName(), DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		assertEquals(descriptor.getEntities().get(1).getDocRef().toString(), "Contract/5b7f156d-a7b9-48a7-92d6-023c787b3e79");
		return true;
	}

	private boolean verifyComputedRank(Object computedRank) {
		var rank = (com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank) computedRank;
		return rank.getOrderRank().equals("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssl")
			&& rank.getComplementaryOrderRank().equals("ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssst");
	}

	private void verifyImportedRolesAndUsers(Path adminFile) throws IOException {
		verify(roleMappingProcessor, times(1)).updateMappingData(
			defaultResourceLoader.getResource("classpath:import/data/user/roles.yaml").getContentAsString(StandardCharsets.UTF_8)
		);

		assertTrue(authenticationPrincipalExtensionProperties.getAccessRightsResource() instanceof FileUrlResource);
		assertEquals(authenticationPrincipalExtensionProperties.getAccessRightsResource().getContentAsString(StandardCharsets.UTF_8),
			defaultResourceLoader.getResource("classpath:import/data/user/roles.yaml").getContentAsString(StandardCharsets.UTF_8));

		LocalUser admin = yamlObjectMapper.readValue(adminFile.toFile(), LocalUser.class);
		assertEquals(admin.getEmail(), "admin@mgm-tp.com");
		assertEquals(admin.getFirstname(), "updateFirstName");
	}

	// ========================================
	// Export verification helpers
	// ========================================

	private static class ExportVerificationContext {
		boolean hadCheckMetadata = false;
		boolean hadCheckDocument = false;
		boolean hadCheckAttachment = false;
		boolean hadCheckLinks = false;
		boolean hadCheckRoles = false;
		boolean hadCheckUsers = false;
		boolean hadCheckModels = false;

		void assertAllChecked() {
			assertTrue(hadCheckMetadata || hadCheckDocument || hadCheckAttachment || hadCheckLinks || hadCheckRoles || hadCheckUsers || hadCheckModels);
		}

		void assertExportAllChecked() {
			assertTrue(hadCheckMetadata);
			assertTrue(hadCheckAttachment);
			assertTrue(hadCheckDocument);
			assertTrue(hadCheckLinks);
			assertTrue(hadCheckRoles);
			assertTrue(hadCheckUsers);
		}
	}

	private void verifyExportedTarContent(ByteArrayOutputStream outputStream, AttachmentHeaderEntity attachmentHeader,
		DocumentV2 document, RelationshipLinkEntity relationshipLinkEntity, String mockedAttachmentContent,
		GenericModel genericModel) throws IOException {

		ExportVerificationContext context = new ExportVerificationContext();

		try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
			TarArchiveInputStream tar = new TarArchiveInputStream(gzipIn)) {

			ArchiveEntry entry;
			while ((entry = tar.getNextEntry()) != null) {
				byte[] content = new byte[(int) entry.getSize()];
				IOUtils.read(tar, content);
				String entryName = entry.getName();

				if (entryName.equals("data/meta/seed_metadata.json")) {
					verifyMetadataEntry(content, attachmentHeader);
					context.hadCheckMetadata = true;
				} else if (document != null && entryName.equals("data/documents/BusinessPartner/" + document.getId().get() + ".json")) {
					verifyDocumentEntry(content);
					context.hadCheckDocument = true;
				} else if (attachmentHeader != null && entryName.startsWith("data/attachments/" + attachmentHeader.getId() + ".json")) {
					assertEquals(mockedAttachmentContent.getBytes(), content);
					context.hadCheckAttachment = true;
				} else if (relationshipLinkEntity != null && entryName.equals(
					"data/links/" + RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL + "/" + relationshipLinkEntity.getId() + ".json")) {
					verifyLinkEntry(content, relationshipLinkEntity);
					context.hadCheckLinks = true;
				} else if (entryName.equals("data/user/roles.yaml")) {
					verifyRolesEntry(content);
					context.hadCheckRoles = true;
				} else if (entryName.equals("data/user/users.yaml")) {
					verifyUsersEntry(content);
					context.hadCheckUsers = true;
				} else if (genericModel != null && entryName.equals("data/models/Address.json")) {
					assertEquals(content, genericModel.getContent().getRawContent().getBytes());
					context.hadCheckModels = true;
				}
			}
		}

		context.assertAllChecked();
	}

	private void verifyMetadataEntry(byte[] content, AttachmentHeaderEntity attachmentHeader) throws IOException {
		SeedMetadata metadata = jsonMapper.readValue(content, SeedMetadata.class);
		assertEquals(metadata.getDocuments().size(), 3);
		assertEquals(metadata.getAttachments().size(), 1);
		assertEquals(metadata.getAttachments().get(attachmentHeader.getId()).getFileName(), attachmentHeader.getFileName());
		assertEquals(metadata.getAttachments().get(attachmentHeader.getId()).getAnnotations().getFirst().getName(), "foo");
		assertEquals(metadata.getAttachments().get(attachmentHeader.getId()).getAnnotations().getFirst().getValue(), "bar");
	}

	private void verifyDocumentEntry(byte[] content) throws IOException {
		JsonNode jsonDoc = jsonMapper.readValue(content, JsonNode.class);
		assertEquals(jsonDoc.at("/BusinessPartnerRoot/Name").asText(), "Malcolm");
		assertEquals(jsonDoc.at("/BusinessPartnerRoot/PersonOrEntity").asText(), "Natural Person");
	}

	private void verifyLinkEntry(byte[] content, RelationshipLinkEntity relationshipLinkEntity) throws IOException {
		JsonNode jsonLink = jsonMapper.readValue(content, JsonNode.class);
		assertEquals(jsonLink.at("/relationshipModel").asText(), RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL);
		JsonNode entities = jsonLink.get("entities");
		assertTrue(entities.isArray());
		assertEquals(entities.get(0).at("/role").asText(), RelationshipModelConstants.RoleConstants.CONTRACT_ROLE);
		assertEquals(entities.get(0).at("/modelName").asText(), DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
		assertEquals(entities.get(0).at("/docRef").asText(), relationshipLinkEntity.getRoles().get("Contract").getDocRef().toString());
		assertEquals(entities.get(0).at("/roleOrder").asText(), relationshipLinkEntity.getRoles().get("Contract").getOrder());
		assertEquals(entities.get(1).at("/role").asText(), RelationshipModelConstants.RoleConstants.PARTNER_ROLE);
		assertEquals(entities.get(1).at("/modelName").asText(), DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		assertEquals(entities.get(1).at("/docRef").asText(), relationshipLinkEntity.getRoles().get("Partner").getDocRef().toString());
		assertEquals(entities.get(1).at("/roleOrder").asText(), relationshipLinkEntity.getRoles().get("Partner").getOrder());
	}

	private void verifyRolesEntry(byte[] content) throws IOException {
		RoleMappingDataHolder roleMappingDataHolder = yamlObjectMapper.readValue(content, RoleMappingDataHolder.class);
		assertEquals(roleMappingDataHolder.getRoles().size(), 3);
		assertEquals(roleMappingDataHolder.getRoles().get(0).getName(), "admin");
		assertEquals(roleMappingDataHolder.getRoles().get(1).getName(), "systemAdmin");
		assertEquals(roleMappingDataHolder.getRoles().get(2).getName(), "guest");
	}

	private void verifyUsersEntry(byte[] content) throws IOException {
		LocalUserHolder localUserHolder = yamlObjectMapper.readValue(content, LocalUserHolder.class);
		if (localUserHolder.users().size() == 1) {
			assertEquals(localUserHolder.users().getFirst().getUsername(), "admin");
			assertEquals(localUserHolder.users().getFirst().getEmail(), "test.admin@a12.dev.mgm-tp.com");
		} else {
			assertEquals(localUserHolder.users().size(), 3);
			localUserHolder.users().sort(Comparator.comparing(LocalUser::getUsername));
			assertEquals(localUserHolder.users().getFirst().getUsername(), "actuator");
			assertEquals(localUserHolder.users().getFirst().getEmail(), "Actuator@localhost");
			assertEquals(localUserHolder.users().getLast().getUsername(), "tester");
			assertEquals(localUserHolder.users().getLast().getEmail(), "tester@localhost");
		}
	}

	@BeforeMethod void beforeMethod() throws IllegalAccessException {
		doAnswer(a -> User.builder().username(a.getArgument(0)).password("").build())
			.when(backendUserLoader).loadUser(anyString());

		FieldUtils.writeField(backendAuthenticationService, "backendUserLoader", backendUserLoader, true);
		FieldUtils.writeField(attachmentExporter, "restTemplate", restTemplate, true);
		authenticationPrincipalExtensionProperties.setLocalConfig(new AuthenticationPrincipalExtensionProperties.LocalConfig());
	}

	@Test void testDeleteAll_success() {

		Mockito.reset();

		seedDataService.deleteAllData();

		verify(backendAuthenticationService, times(1)).executeWithBackendAuthentication(eq("superUser"), any());
		verify(relationshipRoleJpaRepository, times(1)).deleteAllInBatch();
		verify(relationshipLinkJpaRepository, times(1)).deleteAllInBatch();
		verify(queryIndexManager, times(1)).cleanIndex();
		verify(documentJpaRepository, times(1)).deleteAllInBatch();
		verify(attachmentReferenceJpaRepository, times(1)).deleteAllInBatch();
		verify(attachmentHeaderJpaRepository, times(1)).deleteAllInBatch();
		verify(attachmentAnnotationJpaRepository, times(1)).deleteAllInBatch();
		verify(dirtyAttachmentJpaRepository, times(1)).deleteAllInBatch();
		verify(modelFieldsJpaRepository, times(1)).deleteAllInBatch();
		verify(modelHeaderJpaRepository, times(1)).deleteAllInBatch();
		verify(modelJpaRepository, times(1)).deleteAllInBatch();
		verify(localizedFieldsJpaRepository, times(1)).deleteAllInBatch();
		verify(cacheManager, times(1)).getCacheNames();
		verify(entityManager, times(1)).flush();
		verify(modelCacheManager, times(1)).invalidateUnsecuredModelReadCaches();
	}

	@Test void testImportAll_success() throws IOException, URISyntaxException {
		assertImport(true);
	}

	@Test void testImportWithoutModels_success() throws IOException, URISyntaxException {
		assertImport(false);
	}

	private void assertImport(boolean hadModels) throws IOException, URISyntaxException {
		Mockito.reset(modelBulkImporter, roleMappingProcessor, defaultAttachmentService, documentService, relationshipLinkFactory, relationshipLinkRepository,
			roleMappingProcessor);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SeedDataServiceTestHelper.createSeedDataTar(outputStream, attachmentId, documentId, linkId, hadModels, true, true, true);

		Path path = Files.createTempDirectory("temp");
		Path metadataFile = path.resolve("metadata.json");
		Path roleFile = path.resolve("roles.yaml");
		Path adminFile = path.resolve("admin.yaml");
		Files.copy(defaultResourceLoader.getResource("/user/admin.yaml").getInputStream(), adminFile, REPLACE_EXISTING);

		setupCommonMocksForImport(metadataFile, roleFile, adminFile);

		seedDataService.importData(new ByteArrayInputStream(outputStream.toByteArray()));

		if (hadModels) {
			verify(modelBulkImporter, times(1)).doImport(any(), any(BulkImporterConfiguration.class));
		} else {
			Mockito.verifyNoInteractions(modelBulkImporter);
		}

		verifyImportedMetadata(metadataFile);
		verifyAttachmentCreation();
		verify(documentService, times(1)).create(any(DocumentV2.class), eq(null));
		verifyRelationshipLinkCreation();
		verify(dataServicesRelationshipLink).setId(linkId);
		verify(relationshipLinkRepository, times(1)).create(dataServicesRelationshipLink);
		verifyImportedRolesAndUsers(adminFile);

		FileUtils.deleteDirectory(path.toFile());
	}

	@Test void testImportAll_canOverrideUsesFile() throws IOException {
		ByteArrayOutputStream seedTar = new ByteArrayOutputStream();
		SeedDataServiceTestHelper.createSeedDataTar((OutputStream) seedTar, null, null, null, false, true, false, false);

		Path tempBasePath = Files.createTempDirectory("temp");
		Path usersFile = tempBasePath.resolve("users.yaml");

		authenticationPrincipalExtensionProperties.getLocalConfig()
			.setUserResources(new Resource[] { new FileUrlResource(usersFile.toFile().getAbsolutePath()) });

		seedDataService.importData(new ByteArrayInputStream(seedTar.toByteArray()));

		assertEquals(
			defaultResourceLoader.getResource("classpath:import/data/user/users.yaml").getContentAsByteArray(),
			Files.readAllBytes(usersFile));

		FileUtils.deleteDirectory(tempBasePath.toFile());
	}

	@Test void testImportAll_mixUsesFile() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SeedDataServiceTestHelper.createSeedDataTar((OutputStream) outputStream, null, null, null, false, true, false, false);

		Path path = Files.createTempDirectory("temp");
		Path usersFile = path.resolve("users.yaml");
		Path adminFile = path.resolve("admin.yaml");
		Files.copy(defaultResourceLoader.getResource("/user/admin.yaml").getInputStream(), adminFile, REPLACE_EXISTING);
		authenticationPrincipalExtensionProperties.getLocalConfig().setUserResources(
			new Resource[] {
				new FileUrlResource(usersFile.toFile().getAbsolutePath()),
				new FileUrlResource(adminFile.toFile().getAbsolutePath()),
			}
		);

		seedDataService.importData(
			new ByteArrayInputStream(outputStream.toByteArray())
		);

		assertEquals(
			defaultResourceLoader.getResource("classpath:import/data/user/users.yaml").getContentAsByteArray(),
			Files.readAllBytes(usersFile)
		);
		// admin file is not changed.
		assertEquals(
			defaultResourceLoader.getResource("/user/admin.yaml").getContentAsByteArray(),
			Files.readAllBytes(adminFile)
		);

		FileUtils.deleteDirectory(path.toFile());
	}

	@Test(expectedExceptions = ModelBulkImportException.class, expectedExceptionsMessageRegExp = "import error")
	void testImportAll_importModelsHasError() throws IOException, URISyntaxException {
		Mockito.reset(modelBulkImporter, seedDataService);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SeedDataServiceTestHelper.createSeedDataTar(outputStream, attachmentId, documentId, linkId, true, true, true, true);

		when(modelBulkImporter.doImport(any(), any())).thenThrow(new ModelBulkImportException(List.of(), "import error"));

		seedDataService.importData(
			new ByteArrayInputStream(outputStream.toByteArray())
		);
	}

	@Test(expectedExceptions = InvalidInputException.class, expectedExceptionsMessageRegExp = "Cannot import models")
	void testImportAll_importModelsHasIOError() throws IOException, URISyntaxException {
		Mockito.reset(modelBulkImporter, seedDataService);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SeedDataServiceTestHelper.createSeedDataTar(outputStream, attachmentId, documentId, linkId, true, true, true, true);

		when(modelBulkImporter.doImport(any(), any())).thenThrow(new IOException());

		seedDataService.importData(new ByteArrayInputStream(outputStream.toByteArray()));
	}

	@Test void testExportAll_success() throws IOException {
		ModelHeaderEntity modelHeaderEntity = spy(ModelHeaderEntity.class);
		modelHeaderEntity.setId(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL);
		DocumentV2 document = loadDocument(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
			DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL + ".json");

		AttachmentHeaderEntity attachmentHeader = createTestAttachmentHeader();
		String mockedAttachmentContent = defaultResourceLoader.getResource("/meta/seed_metadata.json").getContentAsString(StandardCharsets.UTF_8);
		AttachmentUrl attachmentUrl = new AttachmentUrl("http://localhost:8080/cs/download/7fe7181c-3ba1-42e7-b24e-8d69f16ca9e5");

		RelationshipModel relationshipModel = new TestResourceRelationshipModelLoader(relationshipModelSerializer)
			.loadModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL);
		RelationshipLinkEntity relationshipLinkEntity = mockRelationLinkEntity();

		setupMocksForExportTest(modelHeaderEntity, document, attachmentHeader, mockedAttachmentContent, attachmentUrl,
			relationshipModel, relationshipLinkEntity);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		seedDataService.exportAllData(outputStream, false);

		verifyExportedTarContent(outputStream, attachmentHeader, document, relationshipLinkEntity, mockedAttachmentContent, null);
	}

	private void setupMocksForExportTest(ModelHeaderEntity modelHeaderEntity, DocumentV2 document,
		AttachmentHeaderEntity attachmentHeader, String mockedAttachmentContent, AttachmentUrl attachmentUrl,
		RelationshipModel relationshipModel, RelationshipLinkEntity relationshipLinkEntity) {

		when(modelHeaderJpaRepository.findAll()).thenReturn(List.of(modelHeaderEntity));
		when(defaultDocumentRepository.findAllDocRefsForModel(Mockito.any())).thenReturn(
			List.of(DocumentReference.builder()
				.documentModelName(document.getDocumentModelId())
				.documentId(document.getId().get())
				.build())
		);

		authenticationPrincipalExtensionProperties.setAccessRightsResource(defaultResourceLoader.getResource("/user/roles.yaml"));
		authenticationPrincipalExtensionProperties.getLocalConfig().setUserResources(
			new Resource[] { defaultResourceLoader.getResource("/user/admin.yaml") }
		);

		setupCommonMocksForExport(attachmentHeader, mockedAttachmentContent, attachmentUrl);

		DataServicesDocument dataServicesDocument = dataServicesDocumentFactory.newDataServicesDocument(document);
		when(defaultDocumentRepository.findDocumentsByDocRefs(Mockito.any())).thenReturn(List.of(dataServicesDocument));

		when(relationshipModelLoader.loadAllRelationshipModels()).thenReturn(Set.of(relationshipModel));
		when(relationshipLinkJpaRepository.findByRelationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL, null))
			.thenReturn(new PageImpl<>(List.of(relationshipLinkEntity)));
	}

	@Test void testExportWithRelativeAndUsersAndModels_success() throws IOException {
		ModelHeaderEntity modelHeaderEntity = spy(ModelHeaderEntity.class);
		modelHeaderEntity.setId(DocumentModelConstants.ADDRESS_DOCUMENT_MODEL);

		AttachmentHeaderEntity attachmentHeader = createTestAttachmentHeader();
		GenericModel genericModel = new GenericModel();
		genericModel.setContent(new GenericModelContent(
			defaultResourceLoader.getResource("classpath:/models/document/Address.json").getContentAsString(StandardCharsets.UTF_8))
		);

		String mockedAttachmentContent = defaultResourceLoader.getResource("/meta/seed_metadata.json").getContentAsString(StandardCharsets.UTF_8);
		AttachmentUrl attachmentUrl = new AttachmentUrl("/cs/download/7fe7181c-3ba1-42e7-b24e-8d69f16ca9e5");

		setupMocksForExportWithModelsTest(modelHeaderEntity, attachmentHeader, mockedAttachmentContent, attachmentUrl, genericModel);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		seedDataService.exportAllData(outputStream, true);

		verifyExportedTarContent(outputStream, attachmentHeader, null, null, mockedAttachmentContent, genericModel);
	}

	private void setupMocksForExportWithModelsTest(ModelHeaderEntity modelHeaderEntity, AttachmentHeaderEntity attachmentHeader,
		String mockedAttachmentContent, AttachmentUrl attachmentUrl, GenericModel genericModel) {

		authenticationPrincipalExtensionProperties.setAccessRightsResource(defaultResourceLoader.getResource("/user/roles.yaml"));
		authenticationPrincipalExtensionProperties.getLocalConfig().setUserResources(
			new Resource[] {
				defaultResourceLoader.getResource("/user/admin.yaml"),
				defaultResourceLoader.getResource("/user/users.yaml")
			}
		);

		when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:9090/api/internal/seed-data"));
		when(modelHeaderJpaRepository.findAll()).thenReturn(List.of(modelHeaderEntity));
		when(modelRepository.load(modelHeaderEntity)).thenReturn(Optional.of(genericModel));

		setupCommonMocksForExport(attachmentHeader, mockedAttachmentContent, attachmentUrl);
		when(restTemplate.exchange("http://localhost:9090/cs/download/7fe7181c-3ba1-42e7-b24e-8d69f16ca9e5", HttpMethod.GET, null, byte[].class))
			.thenReturn(ResponseEntity.ok(mockedAttachmentContent.getBytes()));
	}

	private DocumentV2 loadDocument(String documentModelName, String documentName) {
		try (Reader r = new StringReader(defaultResourceLoader.getResource("/document/" + documentName).getContentAsString(StandardCharsets.UTF_8))) {
			ListIProblemReporter pr = new ListIProblemReporter();
			DocumentReference documentReference = DocumentReference.builder()
				.documentModelName(documentModelName)
				.documentId(UUID.randomUUID().toString())
				.build();

			return metadataUtils.createDocumentMetadata(
				kernelTestSupport.getDocumentV2Serializer().deserializeV2(r, documentModelName, kernelTestSupport.getDocumentDeserializationConfig(), pr),
				documentReference,
				"admin",
				Instant.now(),
				null
			).withId(documentReference.getDocumentId());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private RelationshipLinkEntity mockRelationLinkEntity() {
		String linkId = UUID.randomUUID().toString();
		RelationshipLinkEntity relationshipLinkEntity = new RelationshipLinkEntity();
		relationshipLinkEntity.setId(linkId);
		relationshipLinkEntity.setRelationshipModel(RelationshipModelConstants.CONTRACT_BUSINESS_PARTNER_MODEL);

		RelationshipRoleEntity sourceRoleEntity = new RelationshipRoleEntity();
		sourceRoleEntity.setRelationship(relationshipLinkEntity);
		sourceRoleEntity.setId(UUID.randomUUID().toString());
		sourceRoleEntity.setName(RelationshipModelConstants.RoleConstants.CONTRACT_ROLE);
		sourceRoleEntity.setDocRef(DocumentReference.builder()
			.documentModelName(DocumentModelConstants.CONTRACT_DOCUMENT_MODEL).documentId(UUID.randomUUID().toString())
			.build());
		sourceRoleEntity.setOrder("123445677");

		RelationshipRoleEntity targetRoleEntity = new RelationshipRoleEntity();
		targetRoleEntity.setRelationship(relationshipLinkEntity);
		targetRoleEntity.setId(UUID.randomUUID().toString());
		targetRoleEntity.setDocRef(DocumentReference.builder()
			.documentModelName(DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL).documentId(UUID.randomUUID().toString())
			.build());
		targetRoleEntity.setName(RelationshipModelConstants.RoleConstants.PARTNER_ROLE);
		targetRoleEntity.setOrder("123445678");

		relationshipLinkEntity.getRoles().put(
			RelationshipModelConstants.RoleConstants.CONTRACT_ROLE, sourceRoleEntity
		);
		relationshipLinkEntity.getRoles().put(
			RelationshipModelConstants.RoleConstants.PARTNER_ROLE, targetRoleEntity
		);

		return relationshipLinkEntity;
	}
}
