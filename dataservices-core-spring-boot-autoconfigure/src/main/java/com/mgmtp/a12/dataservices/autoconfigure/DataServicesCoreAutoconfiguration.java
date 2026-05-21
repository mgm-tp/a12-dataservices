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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration;
import com.mgmtp.a12.dataservices.model.document.IValidationCodeProvider;
import com.mgmtp.a12.dataservices.model.document.internal.ValidationCodeGenerator;
import com.mgmtp.a12.dataservices.model.internal.DataServicesDocumentDynamicServiceConfig;
import com.mgmtp.a12.dataservices.model.internal.DataServicesModelCodeCache;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory;
import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.RelationshipMigration;
import com.mgmtp.a12.dataservices.relationship.internal.DefaultRelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.internal.DocumentDeletionListener;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkFactory;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipValidationSupport;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.DefaultRelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.RelationshipLinkRepository;
import com.mgmtp.a12.dataservices.relationship.persistence.internal.jpa.repository.RelationshipLinkJpaRepository;
import com.mgmtp.a12.dataservices.state.VersionInfo;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
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

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

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
	 *
	 * @return A new {@link DocumentModelServiceFactory}.
	 */
	@Bean public DocumentModelServiceFactory documentModelServiceFactory() {
		return new DocumentModelServiceFactory();
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
		IValidationCodeGeneratorConfig validationCodeGeneratorConfig,  IModelLoader<IDocumentModel> documentModelLoader) {
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
	 * Creates the {@link IDocumentFactory}.
	 *
	 * @param documentServiceFactory Factory to create document services.
	 * @return The {@link IDocumentFactory}.
	 */
	@Bean public IDocumentFactory documentFactory(DocumentServiceFactory documentServiceFactory) {
		return documentServiceFactory.createDocumentFactory();
	}

	/**
	 * Creates the {@link IDocumentSerializer}.
	 *
	 * @param documentServiceFactory Factory to create serializers.
	 * @return The {@link IDocumentSerializer}.
	 */
	@Bean public IDocumentSerializer documentSerializer(DocumentServiceFactory documentServiceFactory) {
		return documentServiceFactory.createDocumentSerializer();
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
	 * Provides a service to join document models.
	 *
	 * @return The {@link DocumentModelJoiningService}.
	 */
	@Bean public DocumentModelJoiningService documentModelJoiningService() {
		return new DocumentModelJoiningService();
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
	 * Customizes the Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} for DataServices.
	 *
	 * Enables propagation of the `transient` marker and registers modules, including a serializer
	 * for {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}.
	 *
	 * @param kernelDocumentSerializer Serializer for {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}.
	 * @return The customizer applied to the Jackson builder.
	 */
	@Bean public Jackson2ObjectMapperBuilderCustomizer dataServicesJacksonCustomizer(KernelDocumentSerializer kernelDocumentSerializer) {
		return builder -> {
			builder.featuresToEnable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
			builder.modules(
				new Jdk8Module(),
				new JavaTimeModule(),
				new SimpleModule().addSerializer(DocumentV2.class, kernelDocumentSerializer)
			);
		};
	}

	@Bean RequestBuilderFactory requestBuilderFactory(ObjectMapper objectMapper) {
		return new RequestBuilderFactory(objectMapper);
	}

	/**
	 * Provides bulk importer configuration bound from core properties.
	 *
	 * @param coreProperties Core properties providing initialization import settings.
	 * @return The {@link BulkImporterConfiguration}.
	 */
	@Bean public BulkImporterConfiguration bulkImporterConfiguration(DataServicesCoreProperties coreProperties) {
		BulkImporterConfiguration config = new BulkImporterConfiguration();
		DataServicesCoreProperties.Initialization.Import.Model models = coreProperties.getInitialization().getImport().getModels();
		config.setOverwriteDocumentModels(models.getOverwrite().getDocumentModels().isEnabled());
		config.setOverwriteModels(models.getOverwrite().getModels());
		config.setOverwriteModelsDefault(models.getOverwrite().isEnabled());
		config.setPaths(models.getPath());
		config.setModelTypes(models.getTypesToClear());
		return config;
	}

	/**
	 * Configures FreeMarker template engine for DataServices.
	 *
	 * @return The FreeMarker {@link freemarker.template.Configuration}.
	 * @throws IOException If templates cannot be loaded.
	 * @throws TemplateException If FreeMarker configuration fails.
	 */
	@Bean public freemarker.template.Configuration freemarkerConfiguration() throws IOException, TemplateException {
		FreeMarkerConfigurationFactoryBean factory = new FreeMarkerConfigurationFactoryBean();
		factory.setTemplateLoaderPath("classpath:templates/");
		factory.setPreferFileSystemAccess(false);
		factory.setDefaultEncoding(String.valueOf(StandardCharsets.UTF_8));
		return factory.createConfiguration();
	}

	@SuppressWarnings({"removal"})
	/**
	 * Provides the {@link RelationshipMigration} utility for relationship data migration.
	 *
	 * @param objectMapper ObjectMapper used to (de)serialize migration payloads.
	 * @return The {@link RelationshipMigration}.
	 */
	@Bean public RelationshipMigration relationshipMigration(ObjectMapper objectMapper) {
		return new RelationshipMigration(objectMapper);
	}

	// TODO A12S-6047: Get rid of the @Lazy annotation of `DefaultDocumentService`
	/**
	 * Provides the {@link DefaultRelationshipLinkService}.
	 *
	 * @param repository Repository for relationship links.
	 * @param relationshipLinkFactory Factory creating relationship links.
	 * @param modelLoader Loader for relationship models.
	 * @param documentSupport Support services for document handling.
	 * @param properties Core properties controlling relationship behavior.
	 * @param eventPublisher Publishes relationship events.
	 * @param defaultDocumentService Default document service (lazy).
	 * @return The {@link DefaultRelationshipLinkService}.
	 */
	@Bean public DefaultRelationshipLinkService relationshipLinkService(
		RelationshipLinkRepository repository,
		RelationshipLinkFactory relationshipLinkFactory,
		RelationshipModelLoader modelLoader,
		DocumentSupport documentSupport,
		DataServicesCoreProperties properties,
		ApplicationEventPublisher eventPublisher,
		@Lazy DefaultDocumentService defaultDocumentService
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
		return new RelationshipLinkFactory(relationshipRankService, relationshipValidationSupport);
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
	 * @param modelLoader Loader for relationship models.
	 * @param aggregatedDocumentRepository Repository supporting aggregated documents.
	 * @param modelTypeService Service resolving model types.
	 * @return The {@link RelationshipValidationSupport}.
	 */
	@Bean public RelationshipValidationSupport relationshipValidationSupport(
		IModelLoader<RelationshipModel> modelLoader,
		AggregatedDocumentRepository aggregatedDocumentRepository,
		DefaultModelTypeService modelTypeService
	) {
		return new RelationshipValidationSupport(modelLoader, aggregatedDocumentRepository, modelTypeService);
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

	/**
	 * Provides a factory to inject document model metadata.
	 *
	 * @param documentModelJoiningService Service to join models.
	 * @param iDocumentFactory Factory to create documents.
	 * @param documentModelService Internal document model service.
	 * @return The {@link DocumentModelMetadataInjectorFactory}.
	 */
	@Bean
	public DocumentModelMetadataInjectorFactory modelMetadataHelper(DocumentModelJoiningService documentModelJoiningService, IDocumentFactory iDocumentFactory,
		DocumentModelService documentModelService) {
		return new DocumentModelMetadataInjectorFactory(documentModelJoiningService, iDocumentFactory, documentModelService);
	}

	@Bean Locale defautlLocale() {
		return Locale.US;
	}

	@ConditionalOnMissingBean
	@Bean IDataServicesDocumentMetadataExtractor iDataServicesDocumentMetadataExtractor() {
		return new DefaultDataServicesDocumentMetadataExtractor();
	}

	/**
	 * Provides a factory to build CDD skeletons based on relationship models and link service.
	 *
	 * @param relationshipModelLoader Loader for {@link RelationshipModel}.
	 * @param relationshipLinkService Service providing relationship links.
	 * @return The {@link CddSkeletonFactory}.
	 */
	@Bean public CddSkeletonFactory cddSkeletonFactory(IModelLoader<RelationshipModel> relationshipModelLoader, RelationshipLinkService relationshipLinkService) {
		return new CddSkeletonFactory(relationshipLinkService, relationshipModelLoader);
	}

}
