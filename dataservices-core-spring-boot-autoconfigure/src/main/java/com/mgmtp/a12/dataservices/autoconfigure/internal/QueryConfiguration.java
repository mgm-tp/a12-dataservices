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

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentHeaderRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.export.IDocumentExporter;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.cdm.persistence.internal.ComposeDocumentModelLoader;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.DefaultQueryEnricher;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.ExactMatchOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.UndefinedMatchOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.projection.internal.CdmHelper;
import com.mgmtp.a12.dataservices.query.projection.internal.ExportCddCsvProjectionImplementation;
import com.mgmtp.a12.dataservices.query.projection.internal.JsonbCddProjectionImplementation;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration public class QueryConfiguration {

	@Bean public DocumentSearchIndexBehaviour jsonbReindexingIndexBehaviour(AggregatedDocumentRepository aggregatedDocumentRepository,
		DocumentFieldsJpaRepository documentFieldsJpaRepository, DocumentModelFieldsIndexer documentModelFieldsIndexer,
		DocumentServiceFactory documentServiceFactory, DocumentModelServiceFactory documentModelServiceFactory,
		@Qualifier("dsDataSource") DataSource dataSource, DocumentSearchJpaRepository documentSearchJpaRepository, EntityManager entityManager,
		IDocumentV2Serializer documentV2Serializer, SearchCustomizerRegistry searchCustomizerRegistry) {
		return new DocumentSearchIndexBehaviour(aggregatedDocumentRepository, documentFieldsJpaRepository, documentModelServiceFactory, dataSource,
			documentModelFieldsIndexer, documentServiceFactory, documentSearchJpaRepository, entityManager, documentV2Serializer, searchCustomizerRegistry);
	}

	@Bean public ExactMatchOperatorSqlGenerator jsonbExactMatchOperatorSqlGenerator(DataServicesCoreProperties dataServicesCoreProperties) {
		return new ExactMatchOperatorSqlGenerator(dataServicesCoreProperties);
	}

	@Bean public UndefinedMatchOperatorSqlGenerator jsonbUndefinedMatchOperatorSqlGenerator() {
		return new UndefinedMatchOperatorSqlGenerator();
	}

	@Order
	@Bean
	public JsonbCddProjectionImplementation jsonbCddProjectionImplementation(IDocumentModelService documentModelService,
		DocumentModelServiceFactory documentModelServiceFactory, DataServicesCoreProperties dataServicesCoreProperties, ObjectMapper objectMapper,
		DocumentTreeHelper documentTreeHelper, Optional<KernelDocumentService> kernelDocumentService, DocumentSupport documentSupport, CdmHelper cdmHelper) {

		return new JsonbCddProjectionImplementation(documentModelService, documentModelServiceFactory, dataServicesCoreProperties, objectMapper,
			documentTreeHelper, kernelDocumentService,
			documentSupport, cdmHelper);
	}

	@Order
	@Bean public ExportCddCsvProjectionImplementation exportCddCsvProjectionImplementation(
		DocumentTreeHelper documentTreeHelper, IDocumentModelService documentModelService, List<IDocumentExporter> documentExports,
		Optional<AttachmentHeaderRepository> attachmentHeaderRepositoryOpt, Optional<IAttachmentRepository> attachmentRepositoryOpt,
		DocumentPermissionEvaluator documentPermissionEvaluator, ComposeDocumentModelLoader composeDocumentModelLoader,
		DataServicesCoreProperties dataServicesCoreProperties
	) {
		return new ExportCddCsvProjectionImplementation(documentTreeHelper, documentModelService, documentExports, attachmentHeaderRepositoryOpt,
			attachmentRepositoryOpt, documentPermissionEvaluator, composeDocumentModelLoader, dataServicesCoreProperties);
	}

	@ConditionalOnMissingBean(QueryEnricher.class)
	@Bean
	public QueryEnricher queryEnricher(ModelTypeService modelTypeService, DocumentModelUtils documentModelUtils,
		DataServicesCoreProperties dataServicesCoreProperties, QueryAuthorizationService queryAuthorizationService, DocumentModelServiceFactory documentModelServiceFactory) {
		return new DefaultQueryEnricher(modelTypeService, documentModelUtils, dataServicesCoreProperties, queryAuthorizationService, documentModelServiceFactory);
	}

}
