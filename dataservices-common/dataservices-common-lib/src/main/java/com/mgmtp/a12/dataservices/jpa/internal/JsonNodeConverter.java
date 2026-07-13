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
package com.mgmtp.a12.dataservices.jpa.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.util.ByteArrayBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// TODO A12S-6689: Drop this class as soon as Hibenrate supports Jackson 3
/**
 * Auto-apply converter wrapper that delegates to the Spring-managed JsonNodeConverter bean.
 * This allows the converter to use injected dependencies while still being auto-applied by Hibernate.
 */
@Converter(autoApply = true) public class JsonNodeConverter implements AttributeConverter<JsonNode, com.fasterxml.jackson.databind.JsonNode> {

	@Override
	public com.fasterxml.jackson.databind.JsonNode convertToDatabaseColumn(JsonNode attribute) {
		return JsonNodeConverterBean.getInstance().convertToDatabaseColumn(attribute);
	}

	@Override
	public JsonNode convertToEntityAttribute(com.fasterxml.jackson.databind.JsonNode dbData) {
		return JsonNodeConverterBean.getInstance().convertToEntityAttribute(dbData);
	}

	@Component public static class JsonNodeConverterBean {

		private static final com.fasterxml.jackson.databind.ObjectMapper JACKSON2_MAPPER =
			JsonMapper.builder().addModules(new Jdk8Module(), new JavaTimeModule()).build();

		private static JsonNodeConverterBean instance;

		private final ObjectMapper objectMapper;

		public JsonNodeConverterBean(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
			instance = this;
		}

		static JsonNodeConverterBean getInstance() {
			if (instance == null) {
				throw new IllegalStateException("JsonNodeConverterBean not initialized by Spring");
			}
			return instance;
		}

		public com.fasterxml.jackson.databind.JsonNode convertToDatabaseColumn(JsonNode attribute) {
			if (attribute == null) {
				return null;
			}
			try {
				return jackson3ToJackson2(attribute);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to convert JsonNode to Jackson 2 format", e);
			}
		}

		public JsonNode convertToEntityAttribute(com.fasterxml.jackson.databind.JsonNode dbData) {
			if (dbData == null || dbData.isEmpty()) {
				return null;
			}
			try {
				return jackson2ToJackson3(dbData);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to convert Jackson 2 JsonNode to Jackson 3 format", e);
			}
		}

		private JsonNode jackson2ToJackson3(com.fasterxml.jackson.databind.JsonNode v2Node) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (com.fasterxml.jackson.core.JsonGenerator gen = JACKSON2_MAPPER.createGenerator(out)) {
				gen.writeTree(v2Node);
			}
			return objectMapper.readTree(out.toByteArray());
		}

		private com.fasterxml.jackson.databind.JsonNode jackson3ToJackson2(JsonNode v3Node) throws IOException {
			ByteArrayBuilder buffer = new ByteArrayBuilder();
			try (tools.jackson.core.JsonGenerator gen = objectMapper.createGenerator(buffer)) {
				gen.writeTree(v3Node);
			}
			return JACKSON2_MAPPER.readTree(buffer.toByteArray());
		}
	}
}
