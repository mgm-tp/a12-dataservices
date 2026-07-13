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
package com.mgmtp.a12.dataservices.modelgraph.fs.impl;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedDocumentModelLoader;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedModelService;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedModelTypeService;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.FileBasedRelationshipModelLoader;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.internal.model.NoOpModelPermissionEvaluator;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;

/**
 * All created beans have @ConditionalOnMissingBean to not create conflicts with the applications where this config might be
 * embedded. DS beans are not likely to create such conflicts but ObjectMapper, Header,... sure can create such problems.
 */
@Configuration public class ModelGraphGeneratorConfiguration {

	@Bean @ConditionalOnMissingBean public RelationshipModelSerializer relationshipModelSerializer(ObjectMapper objectMapper) {
		return new DefaultRelationshipModelSerializer(objectMapper);
	}

	@Bean @ConditionalOnMissingBean public ModelGraphService modelGraphService(ModelService modelService, RelationshipModelLoader relationshipModelLoader,
		RelationshipModelSerializer relationshipModelSerializer, ModelTypeService modelTypeService,
		ModelPermissionEvaluator<Model> modelPermissionEvaluator) {
		return new ModelGraphService(modelService, relationshipModelLoader, relationshipModelSerializer,
			modelTypeService, Optional.of(modelTypeService), modelPermissionEvaluator);
	}

	@Bean @ConditionalOnMissingBean public ModelTypeService fileBasedModelTypeService(ModelService modelService,
		ModelPermissionEvaluator<Model> modelPermissionEvaluator) {
		return new FileBasedModelTypeService(modelService, modelPermissionEvaluator);
	}

	@Bean @ConditionalOnMissingBean public ModelPermissionEvaluator<Model> modelPermissionEvaluator() {
		return new NoOpModelPermissionEvaluator();
	}

	@Bean @ConditionalOnMissingBean public ModelService fileBasedModelService(HeaderParser headerParser) {
		return new FileBasedModelService(headerParser);
	}

	@Bean @ConditionalOnMissingBean public RelationshipModelLoader fileBasedRelationshipModelLoader(ModelService modelService,
		RelationshipModelSerializer relationshipModelSerializer) {
		return new FileBasedRelationshipModelLoader(modelService, relationshipModelSerializer);
	}

	@Bean @ConditionalOnMissingBean public DocumentModelLoader fileBasedDocumentModelLoader(ModelService modelService,
		IDocumentModelSerializer documentModelSerializer) {
		return new FileBasedDocumentModelLoader(modelService, documentModelSerializer);
	}

	@Bean @ConditionalOnMissingBean public HeaderParser headerParser() {
		return new DefaultHeaderParser();
	}

	@Bean @ConditionalOnMissingBean public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean @ConditionalOnMissingBean public DocumentModelServiceFactory documentModelServiceFactory() {
		return new DocumentModelServiceFactory();
	}

	@Bean @ConditionalOnMissingBean public IDocumentModelSerializer documentModelSerializer(DocumentModelServiceFactory documentServiceFactory) {
		return documentServiceFactory.createDocumentModelSerializer();
	}
}
