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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonNodeSpelProcessor {

	public static final Pattern PATTERN_FOR_REPLACEMENT_WITH_OBJECT = Pattern.compile("^#\\{(#[0-9a-zA-Z._]*)}$");
	private final ExpressionParser expressionParser = new SpelExpressionParser();

	private final ObjectMapper objectMapper;

	/**
	 * This method doesn't modify the json node on input, but returns copy of it with SpEL expressions evaluated.
	 *
	 * @param jsonNode with SpEL expressions
	 * @return JSON node with SpEL evaluated
	 */
	public JsonNode evaluateSpel(JsonNode jsonNode) {
		return copyNode(jsonNode, JsonNode::isTextual, sourceNode -> StringNode.valueOf(evaluateSpelInternal(sourceNode.textValue())));
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

	public JsonNode copyNode(JsonNode sourceNode, Predicate<? super JsonNode> condition, Function<? super JsonNode, ? extends JsonNode> convertor) {
		if (condition.test(sourceNode)) {
			return convertor.apply(sourceNode);
		} else if (sourceNode.isContainer()) {
			if (sourceNode.isObject()) {
				return cloneObjectNode(sourceNode, condition, convertor);
			} else if (sourceNode.isArray()) {
				return cloneArrayNode(sourceNode, condition, convertor);
			} else {
				throw new IllegalStateException(String.format("Unexpected container node type %s", sourceNode.getNodeType()));
			}
		} else if (sourceNode.isValueNode()) {
			return sourceNode.deepCopy();
		} else {
			throw new IllegalStateException(String.format("Unexpected node type %s", sourceNode.getNodeType()));
		}
	}

	private ArrayNode cloneArrayNode(JsonNode sourceNode, Predicate<? super JsonNode> condition, Function<? super JsonNode, ? extends JsonNode> convertor) {
		ArrayNode targetNode = objectMapper.createArrayNode();
		sourceNode.forEach(e -> targetNode.add(copyNode(e, condition, convertor)));
		return targetNode;
	}

	private ObjectNode cloneObjectNode(JsonNode sourceNode, Predicate<? super JsonNode> condition, Function<? super JsonNode, ? extends JsonNode> convertor) {
		ObjectNode targetNode = objectMapper.createObjectNode();
		sourceNode.properties().forEach(e -> targetNode.set(e.getKey(), copyNode(e.getValue(), condition, convertor)));
		return targetNode;
	}
}
