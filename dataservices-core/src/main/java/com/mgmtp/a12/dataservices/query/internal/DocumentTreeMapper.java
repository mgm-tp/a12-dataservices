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
package com.mgmtp.a12.dataservices.query.internal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.DocumentTreeEntity;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class DocumentTreeMapper {

	public static final String MISSING_VALUE = "''";
	public static final String ID_PREFIX = "id.";
	public static final String DOC_REF = "docRef";
	public static final String RELATIONSHIP_MODEL = "relationshipModel";
	public static final String SOURCE_ROLE = "sourceRole";
	public static final String SOURCE_DOC_REF = "sourceDocRef";
	public static final String TARGET_ROLE = "targetRole";
	public static final String TARGET_DOC_REF = "targetDocRef";
	public static final String LINK_ID = "linkId";
	public static final String TYPE = "type";
	public static final String BACK_REFERENCE = "backReference";
	public static final String INTERNAL_ID = "internalId";
	public static final String DEPTH = "depth";
	public static final String FIELDS_PROJECTION = "fieldsProjection";
	public static final String JAVA_PREFIX = "java(";
	public static final String JAVA_SUFFIX = ")";
	public static final String MISSING_VALUE_EQUALS = "!\"" + MISSING_VALUE + "\".equals(";
	public static final String MISSING_VALUE_SUFFIX = ")";

	@Autowired
	protected ObjectMapper objectMapper;

	@Mapping(target = "document", expression = "java(contentToJsonNode(input.getContent()))")
	@Mapping(source = ID_PREFIX + DOC_REF, target = DOC_REF)
	@Mapping(source = ID_PREFIX + BACK_REFERENCE, target = BACK_REFERENCE, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + BACK_REFERENCE
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + INTERNAL_ID, target = INTERNAL_ID, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + INTERNAL_ID
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + LINK_ID, target = LINK_ID, conditionExpression = "java(linkId != null && !\"" + QueryGeneratorConstants.EMPTY_STRING
		+ "\".equals(linkId))")
	@Mapping(source = ID_PREFIX + RELATIONSHIP_MODEL, target = RELATIONSHIP_MODEL, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + RELATIONSHIP_MODEL
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + TYPE, target = TYPE)
	@Mapping(source = ID_PREFIX + SOURCE_DOC_REF, target = SOURCE_DOC_REF, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + SOURCE_DOC_REF
		+ MISSING_VALUE_SUFFIX + " && sourceDocRef.isValid()" + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + SOURCE_ROLE, target = SOURCE_ROLE, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + SOURCE_ROLE
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + TARGET_DOC_REF, target = TARGET_DOC_REF, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + TARGET_DOC_REF
		+ MISSING_VALUE_SUFFIX + " && targetDocRef.isValid()" + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + TARGET_ROLE, target = TARGET_ROLE, conditionExpression = JAVA_PREFIX + MISSING_VALUE_EQUALS + TARGET_ROLE
		+ MISSING_VALUE_SUFFIX + JAVA_SUFFIX)
	@Mapping(source = ID_PREFIX + DEPTH, target = DEPTH)
	@Mapping(source = ID_PREFIX + FIELDS_PROJECTION, target = FIELDS_PROJECTION)

	public abstract DocumentTreeResult mapToDocumentTreeResult(DocumentTreeEntity input);

	protected JsonNode contentToJsonNode(String content) {
		return content == null ? objectMapper.createObjectNode() : objectMapper.readTree(content);
	}
}
