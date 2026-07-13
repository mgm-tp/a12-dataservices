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
package com.mgmtp.a12.dataservices.relationship.model.internal;

import java.io.Reader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.ModelSerializationException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipVersionValidationException;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModelSerializer;
import com.mgmtp.a12.model.header.DefaultHeaderJsonSerializer;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderJsonSerializer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Serializer that converts generic model to the proper relationship model type. Currently only deserialization is supported because for the serialization
 * there is no use-case currently.
 */

@Slf4j
@RequiredArgsConstructor
public class DefaultRelationshipModelSerializer implements RelationshipModelSerializer {

	public static final String EXCEPTION_MESSAGE = "Relationship model (de)serialization failed";

	private final ObjectMapper objectMapper;

	/**
	 * Deserializes complete model content and sets already de-serialized header to RelationshipModel.header
	 *
	 * @param modelContent complete relationship model JSON content
	 * @param header de-serialized header
	 * @return instance of the {@link RelationshipModel} with both header and content set
	 */
	@Override public RelationshipModel deserialize(@NonNull Reader modelContent, @NonNull Header header) {
		try {
			RelationshipModel relationshipModel = objectMapper.readValue(modelContent, RelationshipModel.class);
			relationshipModel.setHeader(header);
			validateRelationshipModelVersion(relationshipModel);
			return relationshipModel;
		} catch (Exception ex) {
			log.warn("Deserialization of relationship model {} failed", header.getId(), ex);
			throw new ModelSerializationException(ExceptionKeys.RELATIONSHIP_MODEL_SERIALIZATION_ERROR_KEY, EXCEPTION_MESSAGE);
		}
	}

	@Override public String serialize(@NonNull RelationshipModel relationshipModel) {
		return objectMapper.writeValueAsString(new RelationshipModelWithHeader(relationshipModel));
	}

	private static void validateRelationshipModelVersion(RelationshipModel relationshipModel) {
		String modelVersion = relationshipModel.getHeader().getModelVersion();
		if (!RelationshipModel.VERSION.equals(modelVersion)) {
			throw new RelationshipVersionValidationException(
				"Validation of relationship model [%s] failed because it contains invalid version [%s]. Currently valid version is [%s]".formatted(
					relationshipModel.getHeader().getId(),
					modelVersion,
					RelationshipModel.VERSION));
		}
	}

	private static class RelationshipModelWithHeader extends RelationshipModel {

		public RelationshipModelWithHeader(RelationshipModel relationshipModel) {
			super(relationshipModel.getHeader(), relationshipModel.getContent());
		}

		@JsonIgnore(false) @JsonProperty @JsonSerialize(using = HeaderSerializer.class)
		@Override public Header getHeader() {
			return super.getHeader();
		}
	}

	public static class HeaderSerializer extends ValueSerializer<Header> {
		private final HeaderJsonSerializer headerJsonSerializer = new DefaultHeaderJsonSerializer();

		@Override public void serialize(Header value, JsonGenerator gen, SerializationContext ctxt) {
			gen.writeRawValue(headerJsonSerializer.toJsonString(value));
		}
	}
}
