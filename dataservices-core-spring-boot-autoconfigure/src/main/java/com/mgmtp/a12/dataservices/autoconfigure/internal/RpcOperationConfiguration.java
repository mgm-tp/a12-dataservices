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
package com.mgmtp.a12.dataservices.autoconfigure.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.DefaultAttachmentService;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentHeaderOperation;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadAttachmentUrlOperation;
import com.mgmtp.a12.dataservices.attachment.internal.operation.LoadThumbnailUrlOperation;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.OnEnabledRpcCondition;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.attachment.OnEnabledAttachmentCondition;
import com.mgmtp.a12.dataservices.document.DocumentService;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.CheckUniquenessOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.GetDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.ListValidationCodesOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.LoadThumbnailUrlsOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.ModifyDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.MultiDeleteDocumentsOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.PartialModifyDocumentOperation;
import com.mgmtp.a12.dataservices.document.operation.internal.ValidateDocumentOperation;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.document.uniqueconstraint.internal.UniqueConstraintValidator;
import com.mgmtp.a12.dataservices.export.internal.csv.CsvDocumentExporter;
import com.mgmtp.a12.dataservices.export.internal.helper.ExportHelper;
import com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer;
import com.mgmtp.a12.dataservices.model.document.SecuredValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelService;
import com.mgmtp.a12.dataservices.model.operation.internal.ListModelsOperation;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationListener;
import com.mgmtp.a12.dataservices.relationship.operation.internal.AddLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.DeleteLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.ModifyLinkOperation;
import com.mgmtp.a12.dataservices.relationship.operation.internal.RelinkDocumentOperation;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Conditional(OnEnabledRpcCondition.class)
@RequiredArgsConstructor
@Configuration public class RpcOperationConfiguration {

	@Value("#{new Boolean(${mgmtp.a12.dataservices.test.rpc.debug:false}).booleanValue()}") boolean debugRpcResponses;

	private final DataServicesCoreProperties dataServicesCoreProperties;

	@Bean public JsonRpcInitializer jsonRpcInitializer(JsonRpcBasicServer server, ObjectMapper objectMapper, DataServicesCoreProperties properties,
		ResourcePatternResolver resourcePatternResolver) {
		List<String> paths = properties.getInitialization().getScripts().getJsonRpc().getPaths();

		if (CollectionUtils.isNotEmpty(paths)) {
			return new JsonRpcInitializer(server, objectMapper, paths, resourcePatternResolver);
		}

		// return empty list to be null safe
		return new JsonRpcInitializer(server, objectMapper, Collections.emptyList(), resourcePatternResolver);
	}

	@Bean
	public JsonRpcBasicServer jsonRpcOperationServer(
		RelationshipLinkValidationListener linkValidator,
		ApplicationEventPublisher applicationEventPublisher,
		DataServicesCoreProperties dataServicesCoreProperties,
		ObjectMapper objectMapper,
		TransactionHandler transactionHandler,
		Environment environment) {

		return new JsonRpcOperationDispatcher(
			getAllowedOperations(),
			linkValidator,
			applicationEventPublisher,
			objectMapper,
			dataServicesCoreProperties,
			isSpelAllowed(),
			debugRpcResponses,
			transactionHandler,
			environment);
	}

	@Bean public GetDocumentOperation getDocumentOperation(DocumentService documentService, Anonymizer anonymizer, DocumentSupport documentSupport,
		ApplicationEventPublisher applicationEventPublisher) {
		return new GetDocumentOperation(documentService, anonymizer, documentSupport, applicationEventPublisher);
	}

	@Bean public AddDocumentOperation addDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		return new AddDocumentOperation(documentService, anonymizer);
	}

	@Bean public CopyDocumentOperation copyDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		return new CopyDocumentOperation(documentService, anonymizer);
	}

	@Bean public ModifyDocumentOperation modifyDocumentOperation(DocumentService documentService, Anonymizer anonymizer, DocumentSupport documentSupport) {
		return new ModifyDocumentOperation(documentService, anonymizer, documentSupport);
	}

	@Bean public ValidateDocumentOperation validateDocumentOperation(DocumentService documentService, Anonymizer anonymizer,
		KernelDocumentService kernelDocumentService, DocumentDeserializationConfig documentJsonDeserializationConfig,
		IDocumentV2Serializer documentV2Serializer) {
		return new ValidateDocumentOperation(documentService, anonymizer, kernelDocumentService, documentJsonDeserializationConfig, documentV2Serializer);
	}

	@Bean public CheckUniquenessOperation checkUniquenessOperation(DocumentService documentService, Anonymizer anonymizer,
		UniqueConstraintValidator uniqueConstraintValidator, DocumentSupport documentSupport) {
		return new CheckUniquenessOperation(documentService, anonymizer, uniqueConstraintValidator, documentSupport);
	}

	@Bean public PartialModifyDocumentOperation partialModifyDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		return new PartialModifyDocumentOperation(documentService, anonymizer);
	}

	@Bean public DeleteDocumentOperation deleteDocumentOperation(DocumentService documentService, Anonymizer anonymizer) {
		return new DeleteDocumentOperation(documentService, anonymizer);
	}

	@Bean public MultiDeleteDocumentsOperation multiDeleteDocumentsOperation(DocumentService documentService, Anonymizer anonymizer) {
		return new MultiDeleteDocumentsOperation(documentService, anonymizer);
	}

	@Bean public AddLinkOperation addLinkOperation(
		RelationshipLinkService relationshipLinkService,
		Anonymizer anonymizer
	) {
		return new AddLinkOperation(relationshipLinkService, anonymizer);
	}

	@Bean public DeleteLinkOperation deleteLinkOperation(
		RelationshipLinkService relationshipLinkService,
		Anonymizer anonymizer
	) {
		return new DeleteLinkOperation(relationshipLinkService, anonymizer);
	}

	@Bean public RelinkDocumentOperation relinkDocumentOperation(
		RelationshipLinkService relationshipLinkService,
		Anonymizer anonymizer
	) {
		return new RelinkDocumentOperation(relationshipLinkService, anonymizer);
	}

	@Bean public ListModelsOperation listModelsOperation(DefaultModelService defaultModelService, DataServicesCoreProperties dataServicesCoreProperties) {
		return new ListModelsOperation(defaultModelService, dataServicesCoreProperties);
	}

	@Bean public ListValidationCodesOperation listValidationCodesOperation(SecuredValidationCodeGenerator securedValidationCodeGenerator,
		DataServicesCoreProperties dataServicesCoreProperties, ModelHeaderJpaRepository modelHeaderJpaRepository) {
		return new ListValidationCodesOperation(securedValidationCodeGenerator, dataServicesCoreProperties, modelHeaderJpaRepository);
	}

	@Bean public ModifyLinkOperation modifyLinkOperation(RelationshipLinkService relationshipLinkService, Anonymizer anonymizer) {
		return new ModifyLinkOperation(relationshipLinkService, anonymizer);
	}

	@Conditional(OnEnabledAttachmentCondition.class)
	@Bean public LoadAttachmentHeaderOperation loadAttachmentHeaderOperation(AttachmentHeaderService attachmentHeaderService, DocumentService documentService,
		AttachmentMapper attachmentMapper, DefaultAttachmentService attachmentService) {
		return new LoadAttachmentHeaderOperation(attachmentHeaderService, documentService, attachmentMapper, attachmentService);
	}

	@Conditional(OnEnabledAttachmentCondition.class)
	@Bean public LoadAttachmentUrlOperation loadAttachmentUrlOperation(AttachmentService attachmentService, AttachmentMapper attachmentMapper) {
		return new LoadAttachmentUrlOperation(attachmentService, attachmentMapper);
	}

	@Conditional(OnEnabledAttachmentCondition.class)
	@Bean public LoadThumbnailUrlOperation loadThumbnailUrlOperation(ThumbnailUtil thumbnailUtil) {
		return new LoadThumbnailUrlOperation(thumbnailUtil);
	}

	@Bean public LoadThumbnailUrlsOperation loadThumbnailUrlsOperation(Optional<ThumbnailUtil> thumbnailUtilOpt,
		AttachmentReferenceJpaRepository attachmentReferenceJpaRepository) {
		return new LoadThumbnailUrlsOperation(thumbnailUtilOpt, attachmentReferenceJpaRepository);
	}

	@Conditional({ OnEnabledAttachmentCondition.class })
	@Bean
	public CsvDocumentExporter csvDocumentExporter(ExportHelper exportHelper, DataServicesCoreProperties dataServicesCoreProperties, Anonymizer anonymizer) {
		return new CsvDocumentExporter(exportHelper, dataServicesCoreProperties.getCdd().getExport().getCsv().getDelimiter(),
			anonymizer, dataServicesCoreProperties.getCdd().getExport().getCharset());
	}

	@Conditional({ OnEnabledAttachmentCondition.class })
	@Bean public ExportHelper exportHelper(
		IDocumentModelService documentModelService) {
		return new ExportHelper(documentModelService);
	}

	private boolean isSpelAllowed() {
		return Optional.ofNullable(dataServicesCoreProperties)
			.map(DataServicesCoreProperties::getJsonRpc)
			.map(DataServicesCoreProperties.JsonRpc::getSpel)
			.map(DataServicesCoreProperties.JsonRpc.Spel::isEnabled)
			.orElse(false);
	}

	private Set<String> getAllowedOperations() {
		return Optional.ofNullable(dataServicesCoreProperties)
			.map(DataServicesCoreProperties::getJsonRpc)
			.map(DataServicesCoreProperties.JsonRpc::getAllowedOperations)
			.orElse(Collections.emptySet());
	}
}
