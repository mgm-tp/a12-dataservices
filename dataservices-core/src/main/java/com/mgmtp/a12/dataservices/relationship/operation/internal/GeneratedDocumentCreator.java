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
package com.mgmtp.a12.dataservices.relationship.operation.internal;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;

/**
 * 	For Relationship features some Documents need to be generated. Generated documents are not stored in the persistent store but are generated in the runtime.
 */
@RequiredArgsConstructor
@Component public class GeneratedDocumentCreator {

	public static final String RELATIONSHIP_LINK_DOCUMENT_TEMPLATE_PROP = "relationshipDoc";

	private final Configuration freemarkerConfiguration;

	public String createDocument(String targetDocJson, String additionalModelDoc) {
		return resolveDocumentTemplate(prepareDocsTemplateInput(targetDocJson, additionalModelDoc));
	}

	private Map<String, Object> prepareDocsTemplateInput(String targetDoc, String relationshipDoc) {
		Map<String, Object> input = new HashMap<>();
		input.put("targetDoc", targetDoc);
		if (StringUtils.isNotBlank(relationshipDoc)) {
			input.put(RELATIONSHIP_LINK_DOCUMENT_TEMPLATE_PROP, relationshipDoc);
		}

		return input;
	}

	private String resolveDocumentTemplate(final Map<String, Object> model) {
		try {
			Writer content = new StringWriter();
			Template template = freemarkerConfiguration.getTemplate(resolveTemplateFileName(model.containsKey(RELATIONSHIP_LINK_DOCUMENT_TEMPLATE_PROP)), null,
				String.valueOf(StandardCharsets.UTF_8));
			template.process(model, content);
			return content.toString();
		} catch (final Exception e) {
			throw new InvalidInputException(e.getMessage(), e).withAnonymityMessage("Resolve document template failed.");
		}
	}

	private String resolveTemplateFileName(boolean hasRelationshipDoc) {
		return hasRelationshipDoc ? "relationship_target_doc_template.ftl" : "relationship_target_doc_template_without_link_model.ftl";
	}
}
