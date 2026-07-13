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
package com.mgmtp.a12.dataservices.autoconfigure;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.mgmtp.a12.dataservices.cdd.internal.CddSkeletonFactory;
import com.mgmtp.a12.dataservices.client.rpc.RequestBuilderFactory;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.common.anonymizing.RuntimeSwitchingAnonymizer;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.ExposePropertiesToActuator;
import com.mgmtp.a12.dataservices.configuration.internal.ConfigurationPropertiesData;
import com.mgmtp.a12.dataservices.configuration.internal.MonitorPropertiesData;
import com.mgmtp.a12.dataservices.configuration.internal.validation.validator.JsonRpcPropertiesValidator;
import com.mgmtp.a12.dataservices.document.IDataServicesDocumentMetadataExtractor;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentMetadataExtractor;
import com.mgmtp.a12.dataservices.document.internal.KernelDocumentSerializer;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.initialization.internal.ModelImportConfiguration;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.dataservices.model.document.internal.ValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.internal.DataServicesDocumentDynamicServiceConfig;
import com.mgmtp.a12.dataservices.model.internal.DataServicesDocumentModelServiceFactory;
import com.mgmtp.a12.dataservices.model.internal.DataServicesModelCodeCache;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.query.internal.marshalling.QuerySubtypeProvider;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.dataservices.relationship.factory.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.internal.DefaultRelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.internal.DefaultRelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.internal.DocumentDeletionListener;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.DefaultRelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.relationship.validation.RelationshipValidationSupport;
import com.mgmtp.a12.dataservices.rpc.internal.marshalling.DataServicesJacksonModule;
import com.mgmtp.a12.dataservices.state.VersionInfo;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentServiceConfig;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;
import com.mgmtp.a12.model.serialization.DefaultJsonSerializationFactory;
import com.mgmtp.a12.model.serialization.DefaultXmlModelTypeIdentifier;
import com.mgmtp.a12.model.serialization.DefaultXmlSerializerFactory;
import com.mgmtp.a12.model.serialization.JsonSerializationFactory;
import com.mgmtp.a12.model.serialization.ModelTypeIdentifier;
import com.mgmtp.a12.model.serialization.XmlSerializerFactory;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Core autoconfiguration for the DataServices module.
 *
 * Registers core factories and services for document models, serialization, anonymization,
 * relationship handling, Jackson customization, and auxiliary infrastructure beans.
 */
@RequiredArgsConstructor
@PropertySource({ "classpath:dataservices-common_jpa.properties",
	"classpath:dataservices-common_quartz.properties", "classpath:dataservices-common_cache.properties",
	"classpath:dataservices-common_liquibase.properties", "classpath:services-version.properties" })
@EnableScheduling @EnableCaching
@Configuration public class DataServicesCoreAutoconfiguration {

	@Value("${groupId}")
	private String groupId;
	@Value("${artifactId}")
	private String artifactId;

	@Value("${a12DataservicesVersion}")
	private String a12DataservicesVersion;

	@Value("${a12ClassicVersion}")
	private String a12ClassicVersion;

	static {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}

	@ExposePropertiesToActuator
	@ConfigurationProperties(DataServicesCoreProperties.PROPERTIES_PREFIX)
	@Bean DataServicesCoreProperties dataServicesCoreProperties() {
		return new DataServicesCoreProperties();
	}

	@Bean ConfigurationPropertiesData configurationPropertiesData(ApplicationContext applicationContext) {
		return new ConfigurationPropertiesData(applicationContext);
	}

	@Bean MonitorPropertiesData monitorPropertiesData(ApplicationContext applicationContext, ConfigurableEnvironment environment) {
		return new MonitorPropertiesData(applicationContext, environment);
	}

	/**
	 * Creates the {@link IDocumentModelService}.
	 *
	 * @param documentModelServiceFactory Factory for document model services.
	 * @return The {@link IDocumentModelService}.
	 */
	@Bean public IDocumentModelService iDocumentModelService(DocumentModelServiceFactory documentModelServiceFactory) {
		return documentModelServiceFactory.createDocumentModelService();
	}

	/**
	 * Provides the {@link DocumentModelServiceFactory}.
	 * The actual instance is a `DataServicesDocumentModelServiceFactory`, which extends
	 * `DocumentModelServiceFactory` solely to add `@Cacheable` behavior to
	 * `createDocumentModelSearchService`.
	 *
	 * @return A `DataServicesDocumentModelServiceFactory` instance typed as {@link DocumentModelServiceFactory}.
	 * @see DataServicesDocumentModelServiceFactory
	 */
	@Bean public DocumentModelServiceFactory documentModelServiceFactory() {
		return new DataServicesDocumentModelServiceFactory();
	}

	/**
	 * Creates the {@link IDocumentModelSerializer}.
	 *
	 * @param documentServiceFactory Factory to create the serializer.
	 * @return The {@link IDocumentModelSerializer}.
	 */
	@Bean public IDocumentModelSerializer documentModelSerializer(DocumentModelServiceFactory documentServiceFactory) {
		return documentServiceFactory.createDocumentModelSerializer();
	}

	/**
	 * Provides the {@link DocumentRtServiceFactory}.
	 *
	 * @param documentModelResolver Resolver for document models.
	 * @return The {@link DocumentRtServiceFactory}.
	 */
	@Bean public DocumentRtServiceFactory documentRtServiceFactory(IDocumentModelResolver documentModelResolver) {
		return new DocumentRtServiceFactory(documentModelResolver);
	}

	/**
	 * Provides a cache for generated DataServices model code.
	 *
	 * @return The {@link DataServicesModelCodeCache}.
	 */
	@ConditionalOnMissingBean
	@Bean public DataServicesModelCodeCache dataServicesModelCodeCache() {
		return new DataServicesModelCodeCache();
	}

	/**
	 * Provides dynamic document service configuration backed by the model code cache.
	 *
	 * @param modelCodeCache Cache storing generated model code.
	 * @return The {@link IDocumentServiceConfig}.
	 */
	@ConditionalOnMissingBean(IDocumentServiceConfig.class)
	@Bean public IDocumentServiceConfig dataServicesDocumentDynamicServiceConfig(DataServicesModelCodeCache modelCodeCache) {
		return new DataServicesDocumentDynamicServiceConfig(modelCodeCache);
	}

	/**
	 * Provides the validation code loader which generates validation code for models.
	 *
	 * @param iDocumentModelService Service exposing document models.
	 * @param validationCodeGeneratorConfig Configuration for code generation.
	 * @param documentModelLoader Loader for models from persistence.
	 * @return The {@link IValidationCodeProvider}.
	 */
	@ConditionalOnMissingBean(IValidationCodeProvider.class)
	@Bean public IValidationCodeProvider validationCodeLoader(IDocumentModelService iDocumentModelService,
		IValidationCodeGeneratorConfig validationCodeGeneratorConfig, IModelLoader<IDocumentModel> documentModelLoader) {
		return new ValidationCodeGenerator(iDocumentModelService, validationCodeGeneratorConfig, documentModelLoader);
	}

	/**
	 * Creates the {@link IDocumentRtService} from the given factory and service config.
	 *
	 * @param documentRtServiceFactory Factory that builds RT services.
	 * @param documentServiceConfig Dynamic service configuration.
	 * @return The {@link IDocumentRtService}.
	 */
	@Bean public IDocumentRtService iDocumentRtService(DocumentRtServiceFactory documentRtServiceFactory,
		IDocumentServiceConfig documentServiceConfig) {
		return documentRtServiceFactory.createDocumentRtService(documentServiceConfig);
	}

	/**
	 * Exposes configuration for validation code generation.
	 *
	 * @return The {@link IValidationCodeGeneratorConfig}.
	 */
	@Bean public IValidationCodeGeneratorConfig validationCodeGeneratorConfig() {
		return () -> IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVASCRIPT;
	}

	/**
	 * Provides the {@link DocumentServiceFactory}.
	 *
	 * @param documentModelResolver Resolver for document models.
	 * @return The {@link DocumentServiceFactory}.
	 */
	@Bean public DocumentServiceFactory documentServiceFactory(IDocumentModelResolver documentModelResolver) {
		return new DocumentServiceFactory(documentModelResolver);
	}

	/**
	 * Creates the {@link IDocumentV2Serializer}.
	 *
	 * @param documentServiceFactory Factory to create serializers.
	 * @return The {@link IDocumentV2Serializer}.
	 */
	@Bean public IDocumentV2Serializer documentV2Serializer(DocumentServiceFactory documentServiceFactory) {
		return documentServiceFactory.createDocumentV2Serializer();
	}


	/**
	 * Provides internal document model service implementation.
	 *
	 * @return The {@link DocumentModelService}.
	 */
	@Bean public DocumentModelService documentModelService() {
		return new DocumentModelService();
	}

	/**
	 * Provides the XML model type identifier.
	 *
	 * @return The {@link ModelTypeIdentifier}.
	 */
	// TODO A12S-6447: Remove if not used anymore
	@Bean public ModelTypeIdentifier xmlModelTypeIdentifier() {
		return new DefaultXmlModelTypeIdentifier();
	}

	/**
	 * Provides the XML serializer factory.
	 *
	 * @return The {@link XmlSerializerFactory}.
	 */
	@Bean public XmlSerializerFactory xmlSerializerFactory() {
		return new DefaultXmlSerializerFactory();
	}

	/**
	 * Provides the JSON serializer factory.
	 *
	 * @return The {@link JsonSerializationFactory}.
	 */
	@Bean public JsonSerializationFactory jsonSerializerFactory() {
		return new DefaultJsonSerializationFactory();
	}

	/**
	 * Provides an anonymizer that can be switched at runtime based on properties.
	 *
	 * @param coreProperties Core properties controlling anonymization.
	 * @return The {@link Anonymizer}.
	 */
	@Bean public Anonymizer anonymizer(DataServicesCoreProperties coreProperties) {
		return new RuntimeSwitchingAnonymizer(coreProperties.getLogging().getAnonymization().isEnabled());
	}

	/**
	 * Exposes version information for the DataServices module.
	 *
	 * @return The {@link VersionInfo}.
	 */
	@Bean public VersionInfo versionInfo() {
		return new VersionInfo(groupId, artifactId, a12DataservicesVersion, a12ClassicVersion);
	}

	/**
	 * Provides the header parser used by DataServices.
	 *
	 * @return The {@link HeaderParser}.
	 */
	@Bean public HeaderParser headerParser() {
		return new DefaultHeaderParser();
	}

	/**
	 * Provides the {@link RelationshipModelSerializer} used to serialize and deserialize relationship models.
	 *
	 * @param objectMapper The Jackson {@link ObjectMapper} for JSON processing.
	 * @return The {@link RelationshipModelSerializer}.
	 */
	@ConditionalOnMissingBean
	@Bean public RelationshipModelSerializer relationshipModelSerializer(ObjectMapper objectMapper) {
		return new DefaultRelationshipModelSerializer(objectMapper);
	}

	/**
	 * Scans the application classpath for classes annotated with `@QueryOperator` and
	 * `@QueryAggregationFunction` and exposes them as a {@link QuerySubtypeProvider} bean.
	 *
	 * @param applicationContext Spring application context used to derive the scan scope.
	 * @return the provider; never null.
	 */
	@Bean
	public QuerySubtypeProvider querySubtypeProvider(ApplicationContext applicationContext) {
		return new QuerySubtypeProvider(GenericUtils.getApplicationReflections(applicationContext));
	}

	/**
	 * Provides the Jackson module that registers DataServices-specific serializers, deserializers,
	 * and subtypes for query operators and aggregation functions.
	 *
	 * @param querySubtypeProvider provider of discovered query operator and aggregation function subtypes.
	 * @return The {@link DataServicesJacksonModule}.
	 */
	@ConditionalOnMissingBean
	@Bean
	public DataServicesJacksonModule dataServicesJacksonModule(QuerySubtypeProvider querySubtypeProvider) {
		return new DataServicesJacksonModule(querySubtypeProvider.getSubtypes());
	}

	/**
	 * Customizes the Jackson {@link tools.jackson.databind.ObjectMapper} for DataServices.
	 *
	 * Configures field-based serialization (ignoring getters/setters/constructors) so that
	 * Lombok-generated classes with `@Builder` and `@NonNull` fields work correctly.
	 * Also enables propagation of the `transient` marker and registers DataServices modules.
	 *
	 * @param kernelDocumentSerializer Serializer for {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}.
	 * @param dataServicesJacksonModule Module with DataServices serializers and subtypes.
	 * @return The customizer applied to the Jackson builder.
	 */
	@Bean
	public JsonMapperBuilderCustomizer jackson3Customizer(
		KernelDocumentSerializer kernelDocumentSerializer,
		DataServicesJacksonModule dataServicesJacksonModule) {
		return builder -> {
			builder.enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
				.changeDefaultVisibility(vc -> vc
					.withFieldVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
					.withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
					.withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
					.withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
				.addModule(new SimpleModule().addSerializer(DocumentV2.class, kernelDocumentSerializer))
				.addModule(dataServicesJacksonModule);
		};
	}

	@Bean RequestBuilderFactory requestBuilderFactory(ObjectMapper objectMapper) {
		return new RequestBuilderFactory(objectMapper);
	}

	/**
	 * Provides model import configuration bound from core properties.
	 *
	 * @param coreProperties Core properties providing initialization import settings.
	 * @return The {@link ModelImportConfiguration}.
	 */
	@Bean public ModelImportConfiguration modelImportConfiguration(DataServicesCoreProperties coreProperties) {
		DataServicesCoreProperties.Initialization.Import.Model models = coreProperties.getInitialization().getImport().getModels();
		ModelImportConfiguration config = ModelImportConfiguration.builder()
			.overwriteDocumentModels(models.getOverwrite().getDocumentModels().isEnabled())
			.overwriteModelsDefault(models.getOverwrite().isEnabled())
			.paths(models.getPath())
			.modelTypes(models.getTypesToClear())
			.build();
		config.setOverwriteModels(models.getOverwrite().getModels());
		return config;
	}



	/**
	 * Provides the {@link DefaultRelationshipLinkService}.
	 *
	 * @param repository Repository for relationship links.
	 * @param relationshipLinkFactory Factory creating relationship links.
	 * @param modelLoader Loader for relationship models.
	 * @param documentSupport Support services for document handling.
	 * @param properties Core properties controlling relationship behavior.
	 * @param eventPublisher Publishes relationship events.
	 * @param defaultDocumentService Default document service.
	 * @return The {@link DefaultRelationshipLinkService}.
	 */
	@Bean public DefaultRelationshipLinkService relationshipLinkService(
		RelationshipLinkRepository repository,
		RelationshipLinkFactory relationshipLinkFactory,
		RelationshipModelLoader modelLoader,
		DocumentSupport documentSupport,
		DataServicesCoreProperties properties,
		ApplicationEventPublisher eventPublisher,
		DefaultDocumentService defaultDocumentService
	) {
		return new DefaultRelationshipLinkService(
			repository,
			relationshipLinkFactory,
			modelLoader,
			documentSupport,
			properties,
			eventPublisher,
			defaultDocumentService);
	}

	/**
	 * Registers a listener that reacts to document deletion in order to update relationships.
	 *
	 * @param relationshipLinkService Service managing relationship links.
	 * @param relationshipLinkJpaRepository JPA repository for relationship links.
	 * @return The {@link DocumentDeletionListener}.
	 */
	@Bean public DocumentDeletionListener documentDeletionListener(DefaultRelationshipLinkService relationshipLinkService,
		RelationshipLinkJpaRepository relationshipLinkJpaRepository) {
		return new DocumentDeletionListener(relationshipLinkService, relationshipLinkJpaRepository, dataServicesCoreProperties());
	}

	/**
	 * Provides the {@link RelationshipLinkFactory}.
	 *
	 * @param relationshipRankService Service calculating relationship ranks.
	 * @param relationshipValidationSupport Support for relationship validation.
	 * @return The {@link RelationshipLinkFactory}.
	 */
	@Bean public RelationshipLinkFactory relationshipLinkFactory(
		RelationshipRankService relationshipRankService,
		RelationshipValidationSupport relationshipValidationSupport
	) {
		return new DefaultRelationshipLinkFactory(relationshipRankService, relationshipValidationSupport);
	}

	/**
	 * Provides the {@link RelationshipRankService}.
	 *
	 * @param relationshipLinkRepository Repository for relationship links.
	 * @return The {@link RelationshipRankService}.
	 */
	@Bean public RelationshipRankService relationshipRankService(DefaultRelationshipLinkRepository relationshipLinkRepository) {
		return new RelationshipRankService(relationshipLinkRepository);
	}

	/**
	 * Provides support for relationship validation.
	 *
	 * Creates the default `RelationshipValidationSupport` bean only when no custom bean of this
	 * type is present in the application context. Customer projects may provide their own
	 * implementation to override the default validation behavior (for example, to skip validation
	 * during bulk import).
	 *
	 * @param modelLoader Loader for relationship models.
	 * @param aggregatedDocumentRepository Repository supporting aggregated documents.
	 * @param modelTypeService Service resolving model types.
	 * @return The {@link RelationshipValidationSupport}.
	 */
	@ConditionalOnMissingBean(RelationshipValidationSupport.class)
	@Bean public RelationshipValidationSupport relationshipValidationSupport(
		IModelLoader<RelationshipModel> modelLoader,
		AggregatedDocumentRepository aggregatedDocumentRepository,
		DefaultModelTypeService modelTypeService
	) {
		return new com.mgmtp.a12.dataservices.relationship.internal.RelationshipValidationSupport(
			modelLoader, aggregatedDocumentRepository, modelTypeService);
	}

	/**
	 * Validator for JSON-RPC properties exposed as a static bean.
	 *
	 * @return The {@link JsonRpcPropertiesValidator}.
	 */
	@Bean(name = "jsonRpcPropertiesValidator")
	public static JsonRpcPropertiesValidator configurationPropertiesValidator() {
		return new JsonRpcPropertiesValidator();
	}


	@Bean Locale defautlLocale() {
		return Locale.US;
	}

	@ConditionalOnMissingBean
	@Bean IDataServicesDocumentMetadataExtractor iDataServicesDocumentMetadataExtractor() {
		return new DefaultDataServicesDocumentMetadataExtractor();
	}

	/**
	 * Provides a factory to build CDD skeletons based on relationship models and the link repository.
	 *
	 * @param relationshipModelLoader      Loader for {@link RelationshipModel}.
	 * @param relationshipLinkRepository   Repository for reading relationship links.
	 * @param dataServicesCoreProperties   Core configuration properties (used for page-size cap).
	 * @return The {@link CddSkeletonFactory}.
	 */
	@Bean public CddSkeletonFactory cddSkeletonFactory(IModelLoader<RelationshipModel> relationshipModelLoader,
		RelationshipLinkRepository relationshipLinkRepository,
		DataServicesCoreProperties dataServicesCoreProperties) {
		return new CddSkeletonFactory(relationshipLinkRepository, relationshipModelLoader, dataServicesCoreProperties);
	}

}
