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
package com.mgmtp.a12.query.internal.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mgmtp.a12.dataservices.AbstractDataServiceTest;
import com.mgmtp.a12.dataservices.RelationshipModelResolver;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.IndexedModelFieldCache;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.persistence.IModelReadRepository;
import com.mgmtp.a12.dataservices.model.persistence.IModelRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelReadRepository;
import com.mgmtp.a12.dataservices.model.relationship.persistence.internal.DefaultRelationshipModelLoader;
import com.mgmtp.a12.dataservices.query.QueryContextFactory;
import com.mgmtp.a12.dataservices.query.QueryRepository;
import com.mgmtp.a12.dataservices.query.enrichment.QueryEnricher;
import com.mgmtp.a12.dataservices.query.enrichment.internal.DefaultQueryEnricher;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.DefaultQueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.LocalizedFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContextFactory;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryService;
import com.mgmtp.a12.dataservices.query.internal.ProjectionProvider;
import com.mgmtp.a12.dataservices.query.internal.QueryContextHelper;
import com.mgmtp.a12.dataservices.query.security.IQueryResultAuthorization;
import com.mgmtp.a12.dataservices.query.security.QueryAuthorizationService;
import com.mgmtp.a12.dataservices.query.validation.IQueryOperatorValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.FieldsValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.LinkAwareValidator;
import com.mgmtp.a12.dataservices.query.validation.internal.QueryValidator;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipUtils;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Import({ QueryDebuggerGeneratorsConfiguration.class, QuryDebuggerJpaConfiguration.class })
@Configuration public class QueryDebuggerConfiguration extends AbstractDataServiceTest {

	@ConfigurationProperties(QueryDebuggerProperties.PERFIX)
	@Bean QueryDebuggerProperties queryDebuggerProperties() {
		return new QueryDebuggerProperties();
	}

	@Primary
	@Bean ObjectMapper objectMapper() {
		return jsonMapper;
	}

	@Bean YAMLMapper yamlMapper() {
		return YAMLMapper.builder().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build();
	}

	@Bean IModelLoader<IDocumentModel> documentModelLoader() {
		return documentModelResolver;
	}

	@Bean RelationshipModelResolver relationshipModelResolver() {
		return relationshipModelResolver;
	}

	@Bean IModelReadRepository<RelationshipModel> relationshipModelRepository() {
		return new RelationshipModelReadRepository();
	}

	@Bean RelationshipModelLoader relationshipModelLoader(ModelPermissionEvaluator<RelationshipModel> modelPermissionEvaluator,
		ApplicationEventPublisher eventPublisher, IModelReadRepository<RelationshipModel> relationshipModelReadRepository,
		ModelHeaderJpaRepository headerJpaRepository) {
		return new DefaultRelationshipModelLoader(modelPermissionEvaluator, eventPublisher, relationshipModelReadRepository, headerJpaRepository);
	}

	@Bean DocumentModelServiceFactory documentModelServiceFactory() {
		return documentModelServiceFactory;
	}

	@ConfigurationProperties(DataServicesCoreProperties.PROPERTIES_PREFIX)
	@Bean DataServicesCoreProperties dataServicesCoreProperties() {
		return new DataServicesCoreProperties();
	}

	@Bean HeaderParser headerParser() {
		return new DefaultHeaderParser();
	}

	@Bean ModelService modelService(ResourcePatternResolver resolver, HeaderParser headerParser) {
		return new ModelService() {
			@Override public GenericModel create(@NonNull String modelContent) {
				throw new UnsupportedOperationException();
			}

			@Override public GenericModel update(@NonNull String modelContent) {
				throw new UnsupportedOperationException();
			}

			@Override public boolean delete(@NonNull String modelId) {
				throw new UnsupportedOperationException();
			}

			@SneakyThrows @Override public GenericModel load(@NonNull String modelId) {
				throw new UnsupportedOperationException();
			}

			@Override public Collection<GenericModel> load(@NonNull Collection<String> modelIds) {
				throw new UnsupportedOperationException();
			}

			@SneakyThrows @Override public List<Header> findAllHeadersByType(String type) {
				return Arrays.stream(resolver.getResources("classpath*:/models/**/*.json"))
					.map(r -> {
						try {
							return headerParser.parseJson(r.getContentAsString(StandardCharsets.UTF_8));
						} catch (HeaderParseException | IOException e) {
							throw new RuntimeException(e);
						}
					})
					.filter(h -> Objects.equals(type, h.getModelType()))
					.toList();
			}

			@Override public boolean exists(@NonNull Header header) {
				return false;
			}

			@Override public IModelRepository getSupportingRepository(@NonNull Header header) {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Bean <T extends Model> ModelPermissionEvaluator<T> modelPermissionEvaluator() {
		return new ModelPermissionEvaluator<T>() {
			@Override public void checkModelCreatePermission(Header header) {

			}

			@Override public void checkModelUpdatePermission(Header header) {

			}

			@Override public void checkModelDeletePermission(Header header) {

			}

			@Override public void checkModelReadPermission(String modelId) {

			}

			@Override public void checkModelReadPermission(Header header) {

			}

			@Override public void checkModelReadPermission(T model) {

			}

			@Override public boolean hasModelReadPermission(String modelId) {
				return true;
			}

			@Override public boolean hasModelReadPermission(Header header) {
				return true;
			}

			@Override public boolean hasModelReadPermission(T model) {
				return true;
			}
		};
	}

	@Bean DefaultModelTypeService modelTypeService(ModelService modelService, ModelPermissionEvaluator<Model> modelPermissionEvaluator,
		DataServicesCoreProperties dataServicesCoreProperties, Optional<CacheManager> cacheManager) {
		return new DefaultModelTypeService(modelService, modelPermissionEvaluator, dataServicesCoreProperties, cacheManager);
	}

	@Bean RelationshipUtils relationshipUtils(DefaultModelTypeService modelTypeService) {
		return new RelationshipUtils(modelTypeService);
	}

	@Bean FieldsValidator fieldsValidator(DocumentModelUtils documentModelUtils, DefaultModelTypeService modelTypeService) {
		return new FieldsValidator(documentModelUtils, modelTypeService);
	}

	@Bean LinkAwareValidator linkAwareValidator(DefaultModelTypeService modelTypeService, DataServicesCoreProperties dataServicesCoreProperties,
		RelationshipUtils relationshipUtils) {
		return new LinkAwareValidator(modelTypeService, dataServicesCoreProperties, relationshipUtils);
	}

	@Bean IDocumentModelSerializer documentModelSerializer() {
		return documentModelSerializer;
	}

	@Bean DocumentModelUtils documentModelUtils(DocumentModelServiceFactory documentModelServiceFactory, IDocumentModelSerializer documentModelSerializer) {
		return new DocumentModelUtils(documentModelServiceFactory, documentModelSerializer, headerParser);
	}

	@Bean IDocumentModelService documentModelService() {
		return iDocumentModelService;
	}

	@Bean RelationshipModelSerializer relationshipModelSerializer(ObjectMapper objectMapper) {
		return new DefaultRelationshipModelSerializer(objectMapper);
	}

	@Bean DocumentModelFieldsIndexer documentModelFieldsIndexer(DocumentModelUtils documentModelUtils, IDocumentModelService modelService,
		ModelFieldsJpaRepository modelFieldsJpaRepository, ObjectMapper jsonMapper, LocalizedFieldsJpaRepository localizedFieldsJpaRepository,
		SearchCustomizerRegistry searchCustomizerRegistry) {
		return new DocumentModelFieldsIndexer(documentModelUtils, modelService, modelFieldsJpaRepository, localizedFieldsJpaRepository, searchCustomizerRegistry, jsonMapper);
	}

	@Bean QueryValidator queryValidator(DataServicesCoreProperties dataServicesCoreProperties,
		ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator, LinkAwareValidator linkAwareValidator,
		IModelLoader<RelationshipModel> relationshipModelLoader,
		DocumentModelFieldsIndexer documentModelFieldsIndexer, FieldsValidator fieldsValidator) {
		return new QueryValidator(dataServicesCoreProperties, documentModelPermissionEvaluator, linkAwareValidator, relationshipModelLoader,
			fieldsValidator);
	}

	@Bean QueryRepository queryRepository() {
		return (queryRoot, types, queryContext) -> {throw new UnsupportedOperationException();};
	}

	@Bean QueryAuthorizationService defaultQueryAuthorizationService() {
		return (constraint, documentModel) -> constraint;
	}

	@Bean QueryEnricher queryEnricher(ModelTypeService modelTypeService, DocumentModelUtils documentModelUtils,
		DataServicesCoreProperties dataServicesCoreProperties, QueryAuthorizationService queryAuthorizationService,DocumentModelServiceFactory documentModelServiceFactory) {
		return new DefaultQueryEnricher(modelTypeService, documentModelUtils, dataServicesCoreProperties, queryAuthorizationService, documentModelServiceFactory);
	}

	@Bean QueryContextHelper queryContextHelper(ApplicationContext applicationContext, Map<String, IQueryOperatorValidator> validators) {
		return new QueryContextHelper(applicationContext, validators);
	}

	@Bean DefaultQueryService queryService(IModelLoader<IDocumentModel> documentModelLoader, IModelLoader<RelationshipModel> relationshipModelLoader,
		DocumentModelServiceFactory documentModelServiceFactory, DocumentPermissionEvaluator documentPermissionEvaluator,
		ProjectionProvider projectionProvider, QueryValidator queryValidator, DataServicesCoreProperties dataServicesCoreProperties,
		QueryRepository queryRepository, QueryEnricher queryEnricher, Optional<IQueryResultAuthorization> queryResultAuthorization,
		ApplicationEventPublisher applicationEventPublisher, QueryContextHelper queryContextHelper, IndexedModelFieldCache indexedModelFieldCache) {
		return new DefaultQueryService(documentModelLoader, relationshipModelLoader, documentModelServiceFactory, documentPermissionEvaluator, projectionProvider,
			queryValidator, dataServicesCoreProperties, queryRepository, queryEnricher, queryResultAuthorization, applicationEventPublisher,
			queryContextHelper, indexedModelFieldCache);
	}

	@Bean QueryContextFactory queryContextFactory(IModelLoader<IDocumentModel> documentModelLoader, IModelLoader<RelationshipModel> relationshipModelLoader,
		DefaultQueryService queryService, DocumentModelServiceFactory documentModelServiceFactory, QueryContextHelper queryContextHelper, ModelFieldsJpaRepository modelFieldsJpaRepository, IndexedModelFieldCache indexedModelFieldCache) {
		return new DefaultQueryContextFactory(documentModelLoader, relationshipModelLoader, queryService, documentModelServiceFactory, queryContextHelper, indexedModelFieldCache);
	}

	@Bean DefaultQueryGeneratorContext.QueryGeneratorContextFactory queryGeneratorContextFactory(ObjectMapper objectMapper,
		ApplicationContext applicationContext) {
		return new DefaultQueryGeneratorContext.QueryGeneratorContextFactory(objectMapper, applicationContext);
	}

	@Bean DocumentPermissionEvaluator documentPermissionEvaluator() {
		return new DocumentPermissionEvaluator() {
			@Override public void checkDocumentCreatePermission(DocumentV2 document) {

			}

			@Override public boolean hasDocumentCreatePermission(DocumentV2 document) {
				return true;
			}

			@Override public void checkDocumentPartialUpdatePermission(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef) {

			}

			@Override public void checkDocumentUpdatePermission(DocumentV2 oldDocument, DocumentV2 newDocument, DocumentReference docRef) {

			}

			@Override public void checkDocumentDeletePermission(DataServicesDocument document) {

			}

			@Override public void checkDocumentMultiDeletePermission(Collection<Header> headers) {

			}

			@Override public void checkExportListCDDPermission() {

			}

			@Override public boolean hasExportListCDDPermission() {
				return true;
			}

			@Override public void checkDocumentQueryPermission(String documentModel) {

			}

			@Override public boolean hasDocumentQueryPermission(String documentModel) {
				return true;
			}
		};
	}
}
