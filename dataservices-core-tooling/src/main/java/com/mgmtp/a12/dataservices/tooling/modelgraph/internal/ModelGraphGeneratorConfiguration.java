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
package com.mgmtp.a12.dataservices.tooling.modelgraph.internal;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.model.internal.DefaultModelTypeService;
import com.mgmtp.a12.dataservices.relationship.internal.ModelGraphService;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.dataservices.relationship.model.internal.DefaultRelationshipModelSerializer;
import com.mgmtp.a12.dataservices.tooling.modelgraph.internal.model.CoreToolingModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.tooling.modelgraph.internal.model.CoreToolingRelationshipModelLoader;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParser;

@Configuration public class ModelGraphGeneratorConfiguration {
	@Bean public RelationshipModelSerializer relationshipModelSerializer(ObjectMapper objectMapper) {
		return new DefaultRelationshipModelSerializer(objectMapper);
	}

	@Bean public ModelGraphService modelGraphService(ModelService modelService, CoreToolingRelationshipModelLoader relationshipModelLoader,
		RelationshipModelSerializer relationshipModelSerializer, DocumentModelUtils documentModelUtils, ModelTypeService modelTypeService,
		Optional<DefaultModelTypeService> defaultModelTypeServiceOpt, ModelPermissionEvaluator<Model> modelPermissionEvaluator) {
		return new ModelGraphService(modelService, relationshipModelLoader, relationshipModelSerializer,
			documentModelUtils, modelTypeService, defaultModelTypeServiceOpt, modelPermissionEvaluator);
	}

	@Bean public ModelTypeService modelTypeService(ModelService modelService, ModelPermissionEvaluator<Model> modelPermissionEvaluator,
		DataServicesCoreProperties dataServicesCoreProperties) {
		return new DefaultModelTypeService(modelService, modelPermissionEvaluator, dataServicesCoreProperties, null);
	}

	@Bean public ModelPermissionEvaluator<Model> modelPermissionEvaluator() {
		return new CoreToolingModelPermissionEvaluator();
	}

	@Bean public DataServicesCoreProperties dataServicesCoreProperties() {
		return new DataServicesCoreProperties();
	}

	@Bean public HeaderParser headerParser() {
		return new DefaultHeaderParser();
	}

	@Bean public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean public DocumentModelServiceFactory documentModelServiceFactory() {
		return new DocumentModelServiceFactory();
	}

	@Bean public IDocumentModelSerializer documentModelSerializer(DocumentModelServiceFactory documentServiceFactory) {
		return documentServiceFactory.createDocumentModelSerializer();
	}

	@Bean
	public DocumentModelUtils documentModelUtils(DocumentModelServiceFactory documentModelServiceFactory, IDocumentModelSerializer documentModelSerializer,
		HeaderParser headerParser) {

		return new DocumentModelUtils(documentModelServiceFactory, documentModelSerializer, headerParser);
	}
}
