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
package com.mgmtp.a12.dataservices;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import tools.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelationshipModelResolver {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final DefaultHeaderParser headerParser = new DefaultHeaderParser();
	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	public RelationshipModel getRelationshipModelById(String id) throws IOException {
		String path = "/models/relationship/%s.json".formatted(id);
		Resource[] res = resourcePatternResolver.getResources(path);
		if (ArrayUtils.isEmpty(res)) {
			log.warn("Model %s not found at path %s.".formatted(id, path));
			return null;
		}
		String modelJson = res[0].getContentAsString(StandardCharsets.UTF_8);
		try (Reader r = new StringReader(modelJson)) {
			RelationshipModel relationshipModel = objectMapper.readValue(r, RelationshipModel.class);
			relationshipModel.setHeader(headerParser.parseJson(modelJson));
			return relationshipModel;
		} catch (IOException | NullPointerException | HeaderParseException e) {
			throw new NotFoundException("Model %s not found".formatted(id), e);
		}
	}
}
