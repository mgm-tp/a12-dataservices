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
package com.mgmtp.a12.dataservices.query.enrichment.internal;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.dataservices.model.ModelTypeService;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.enrichement.FieldDescriptor;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_ENRICHMENT;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.getEffectiveFieldType;

/**
 * Helper that populates `FieldDescriptor#fieldType` and `FieldDescriptor#enumerationType` for the
 * field paths of a target document model and its subtypes.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class FieldTypeEnrichmentHelper {

	/**
	 * Populates `FieldDescriptor#fieldType` and `FieldDescriptor#enumerationType` for every field
	 * path of `targetDocumentModelName` and its subtypes. The set is computed because operators
	 * might use fields from any of these models due to heterogeneity.
	 */
	public static void enrichFieldTypes(String targetDocumentModelName, QueryContext context, ModelTypeService modelTypeService, DocumentModelUtils documentModelUtils,
		DocumentModelServiceFactory documentModelServiceFactory) {

		Set<String> allApplicableModelNames = new LinkedHashSet<>(modelTypeService.findAllSubtypes(targetDocumentModelName));
		allApplicableModelNames.add(targetDocumentModelName);

		allApplicableModelNames
			.forEach(modelName -> {
				IDocumentModel documentModel = context.getDocumentModel(modelName);
				IDocumentModelSearchService documentModelSearchService = documentModelServiceFactory.createDocumentModelSearchService(documentModel);
				documentModelUtils.getAllFieldPaths(documentModel)
					.forEach(path -> {
							FieldDescriptor fieldDescriptor = context.getEnrichments().getFieldDescriptor(path);
							if (fieldDescriptor.getFieldType() == null) {
								extractFieldType(documentModelSearchService, path)
									.ifPresent(type -> {
										fieldDescriptor.setFieldType(type);
										fieldDescriptor.setEnumerationType(QueryGeneratorConstants.FieldTypes.ENUMERATION_FIELD_TYPE.equals(type));
									});
							}
						}
					);
			});
	}

	private static Optional<String> extractFieldType(IDocumentModelSearchService documentModelSearchService, String path) {
		return DocumentModelUtils.findField(documentModelSearchService, path)
			.map(field -> getEffectiveFieldType(field, QUERY_ENRICHMENT))
			.map(QueryTopologyHelper::fieldTypeAsString);
	}
}
