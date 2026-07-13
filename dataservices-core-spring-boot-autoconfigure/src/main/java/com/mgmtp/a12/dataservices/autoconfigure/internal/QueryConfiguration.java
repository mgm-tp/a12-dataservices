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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
import com.mgmtp.a12.dataservices.query.enrichment.internal.HasOperatorEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.IQueryAPIOperatorEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.AggregationEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.ConstraintEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.DefaultQueryEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.LinkEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.QueryAPIOperatorWalker;
import com.mgmtp.a12.dataservices.query.enrichment.internal.SortEnricher;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.ExactMatchOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.constraint.internal.matching.UndefinedMatchOperatorSqlGenerator;
import com.mgmtp.a12.dataservices.query.indexing.internal.DocumentSearchIndexBehaviour;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.internal.DocumentTreeHelper;
import com.mgmtp.a12.dataservices.query.projection.internal.CddProjectionImplementation;
import com.mgmtp.a12.dataservices.query.projection.internal.CdmHelper;
import com.mgmtp.a12.dataservices.query.projection.internal.ExportCddCsvProjectionImplementation;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@PersistenceContext(unitName = "dsPersistenceUnit")
@Configuration public class QueryConfiguration {

	@PersistenceContext(unitName = "dsPersistenceUnit") EntityManager entityManager;

	@ConditionalOnProperty(name = "mgmtp.a12.dataservices.query.search-indexing.enabled", havingValue = "true", matchIfMissing = true)
	@DependsOnDatabaseInitialization
	@Bean public DocumentSearchIndexBehaviour jsonbReindexingIndexBehaviour(AggregatedDocumentRepository aggregatedDocumentRepository,
		DocumentFieldsJpaRepository documentFieldsJpaRepository, DocumentModelFieldsIndexer documentModelFieldsIndexer,
		DocumentServiceFactory documentServiceFactory, DocumentModelServiceFactory documentModelServiceFactory,
		@Qualifier("dsDataSource") DataSource dataSource, DocumentSearchJpaRepository documentSearchJpaRepository,
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
	public CddProjectionImplementation jsonbCddProjectionImplementation(IDocumentModelService documentModelService,
		DocumentModelServiceFactory documentModelServiceFactory, DataServicesCoreProperties dataServicesCoreProperties, ObjectMapper objectMapper,
		DocumentTreeHelper documentTreeHelper, Optional<KernelDocumentService> kernelDocumentService, DocumentSupport documentSupport, CdmHelper cdmHelper) {

		return new CddProjectionImplementation(documentModelService, documentModelServiceFactory, dataServicesCoreProperties, objectMapper,
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

	@Bean public HasOperatorEnricher hasOperatorEnricher(
		ModelTypeService modelTypeService,
		DocumentModelUtils documentModelUtils,
		DocumentModelServiceFactory documentModelServiceFactory,
		QueryAuthorizationService queryAuthorizationService) {
		return new HasOperatorEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, queryAuthorizationService);
	}

	@Bean public QueryAPIOperatorWalker queryAPIOperationWalker(
		List<IQueryAPIOperatorEnricher> enrichers,
		DocumentModelUtils documentModelUtils,
		ModelTypeService modelTypeService) {
		return new QueryAPIOperatorWalker(enrichers, documentModelUtils, modelTypeService);
	}

	@Bean public SortEnricher sortEnricher(ModelTypeService modelTypeService, DocumentModelUtils documentModelUtils,
		DocumentModelServiceFactory documentModelServiceFactory) {
		return new SortEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory);
	}

	@Bean public ConstraintEnricher constraintEnricher(QueryAuthorizationService queryAuthorizationService, QueryAPIOperatorWalker queryAPIOperatorWalker) {
		return new ConstraintEnricher(queryAuthorizationService, queryAPIOperatorWalker);
	}

	@Bean public AggregationEnricher aggregationEnricher() {
		return new AggregationEnricher();
	}

	@Bean public LinkEnricher linkEnricher(ModelTypeService modelTypeService, DocumentModelUtils documentModelUtils,
		DocumentModelServiceFactory documentModelServiceFactory, DataServicesCoreProperties dataServicesCoreProperties,
		QueryAPIOperatorWalker queryAPIOperatorWalker, ConstraintEnricher constraintEnricher) {
		return new LinkEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, dataServicesCoreProperties, queryAPIOperatorWalker,
			constraintEnricher);
	}

	@ConditionalOnMissingBean(QueryEnricher.class)
	@Bean
	public QueryEnricher queryEnricher(ModelTypeService modelTypeService, DocumentModelUtils documentModelUtils,
		DocumentModelServiceFactory documentModelServiceFactory, SortEnricher sortEnricher, ConstraintEnricher constraintEnricher,
		AggregationEnricher aggregationEnricher, LinkEnricher linkEnricher) {

		return new DefaultQueryEnricher(modelTypeService, documentModelUtils, documentModelServiceFactory, sortEnricher, constraintEnricher,
			aggregationEnricher, linkEnricher);
	}

}
