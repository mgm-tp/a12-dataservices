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
package com.mgmtp.a12.dataservices.rpc.internal;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;
import com.mgmtp.a12.dataservices.utils.internal.JsonUtils;

public class JsonNodeSpelProcessor {

	public static final Pattern PATTERN_FOR_REPLACEMENT_WITH_OBJECT = Pattern.compile("^#\\{(#[0-9a-zA-Z._]*)}$");
	private final ExpressionParser expressionParser = new SpelExpressionParser();
	private final JsonUtils jsonUtils;

	public JsonNodeSpelProcessor(JsonUtils jsonUtils) {
		this.jsonUtils = jsonUtils;
	}

	/**
	 * This method doesn't modify the json node on input, but returns copy of it with SpEL expressions evaluated.
	 *
	 * @param jsonNode with SpEL expressions
	 * @return JSON node with SpEL evaluated
	 */
	public JsonNode evaluateSpel(JsonNode jsonNode) {
		return jsonUtils.copyNode(jsonNode, JsonNode::isTextual, sourceNode -> new TextNode(evaluateSpelInternal(sourceNode.textValue())));
	}

	private String evaluateSpelInternal(String jsonNode) {
		Matcher matcher = PATTERN_FOR_REPLACEMENT_WITH_OBJECT.matcher(jsonNode);
		if (matcher.matches()) {
			try {
				EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
				OperationContextHolder.get().forEach(context::setVariable);
				return Optional.of(expressionParser.parseExpression(matcher.group(1)))
					.map(expression -> expression.getValue(context, OperationContextHolder.get()))
					.map(Object::toString)
					.orElseThrow(() -> new InvalidInputException(ExceptionKeys.CONVERT_JSON_ERROR_KEY, "Parameter deserialization error occurred!"));
			} catch (EvaluationException e) {
				throw new InvalidInputException(ExceptionKeys.CONVERT_JSON_ERROR_KEY, "SpEL evaluation error occurred!", e);
			}
		} else {
			return jsonNode;
		}
	}

}
