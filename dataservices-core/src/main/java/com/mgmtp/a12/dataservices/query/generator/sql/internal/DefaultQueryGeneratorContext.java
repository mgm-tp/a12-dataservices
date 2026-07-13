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
package com.mgmtp.a12.dataservices.query.generator.sql.internal;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.query.QueryInvalidInputException;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunction;
import com.mgmtp.a12.dataservices.query.internal.marshalling.QuerySubtypeProvider;
import com.mgmtp.a12.dataservices.rpc.internal.marshalling.DataServicesJacksonModule;
import com.mgmtp.a12.dataservices.query.annotation.QueryAggregationFunctionGenerator;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperator;
import com.mgmtp.a12.dataservices.query.annotation.QueryOperatorGenerator;
import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;
import com.mgmtp.a12.dataservices.query.generator.sql.IAggregationFunctionGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.ILogicOperatorGenerator;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.ObjectMapper;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_SQL_GENERATION;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class DefaultQueryGeneratorContext implements QueryGeneratorContext {

	@Getter private final String schema;
	private final String documentTableName;
	private final String jsonColumnName;
	private final String modelNameColumnName;
	@Getter @Setter private int hardLimit;
	@Getter @Setter private int pageOffset;
	@Getter @Setter private int pageLimit;
	@Getter @Setter private String targetDocumentModel;
	@Getter @Setter private String locale;
	@Getter private final Enrichments enrichments;
	@Getter private final AtomicInteger tableCounter = new AtomicInteger();
	@Getter private final AtomicInteger paramCounter = new AtomicInteger();
	@Getter private final Map<String, Object> paramHolder = new HashMap<>();

	private final Deque<String> targetDocRefStack = new ArrayDeque<>();
	private final Deque<String> documentTableStack = new ArrayDeque<>();
	private final Deque<Boolean> recursionStack = new ArrayDeque<>();

	private final Deque<CharSequence> cteAlias = new ArrayDeque<>();
	protected boolean isAggregated;
	protected boolean isGroupingAgg;
	protected int aggregationDefaultPrecision;
	protected String projectionName;
	/**
	 * Exclude selected `ROOT` documents from the projection.
	 */
	@Getter @Setter protected boolean exclude;

	@Override public String getDocumentTableName() {
		return documentTableName == null ? QueryGeneratorConstants.TableNames.DOCUMENT_FIELDS_TABLE_NAME : documentTableName;
	}

	@Override public String getJsonColumnName() {
		return jsonColumnName == null ? QueryGeneratorConstants.ColumnNames.JSON_COLUMN_ALIAS : jsonColumnName;
	}

	@Override public String getModelNameColumnName() {
		return modelNameColumnName == null ? QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_NAME : modelNameColumnName;
	}

	@Override public String getCurrentDocumentTableAlias() {
		return documentTableStack.isEmpty() ? QueryGeneratorConstants.TableNames.TARGET_DOCUMENT_TABLE_ALIAS : documentTableStack.peek();
	}

	@Override public String registerNewDocumentTableAlias(String tableAlias) {
		documentTableStack.push(tableAlias);
		return tableAlias;
	}

	@Override public String registerNewDocumentTableAlias() {
		return registerNewDocumentTableAlias("%s_%04d".formatted(
			QueryGeneratorConstants.TableNames.TARGET_DOCUMENT_TABLE_ALIAS, getTableCounter().addAndGet(1)));
	}

	@Override public String unregisterDocumentTableAlias() {
		return documentTableStack.pop();
	}

	@Override public String getCurrentTargetDocRef() {
		return targetDocRefStack.isEmpty() ? QueryGeneratorConstants.TableNames.TARGET_DOCUMENT_TABLE_ALIAS
			+ QueryGeneratorConstants.DOT_JOINER + QueryGeneratorConstants.ColumnNames.DOC_REF_COLUMN_NAME :
			targetDocRefStack.peek();
	}

	@Override public void registerNewTargetDocRef(String targetDocRef) {
		targetDocRefStack.push(targetDocRef);
	}

	@Override public void unregisterTargetDocRef() {
		targetDocRefStack.pop();
	}

	@Override public boolean registerRecursion(boolean recursion) {
		recursionStack.push(recursion);
		return recursion;
	}

	@Override public boolean getCurrentRecursionState() {
		return !recursionStack.isEmpty() && recursionStack.peek();
	}

	@Override public boolean unregisterRecursion() {
		return recursionStack.pop();
	}

	@Override public CharSequence getCurrentCteAlias() {
		return cteAlias.isEmpty() ? null : cteAlias.peek();
	}

	@Override public CharSequence registerCteAlias(CharSequence recursion) {
		cteAlias.push(recursion);
		return recursion;
	}

	@Override public CharSequence unregisterCteAlias() {
		return cteAlias.pop();
	}

	@RequiredArgsConstructor
	@Component public static class QueryGeneratorContextFactory {

		private final ObjectMapper injectedObjectMapper;
		private final ApplicationContext ctx;
		private final QuerySubtypeProvider querySubtypeProvider;
		private final Map<Class<? extends ILogicOperator>, ILogicOperatorGenerator<? extends ILogicOperator>> sqlConstraintGenerators = new HashMap<>();
		private final Map<Class<? extends IAggregationFunction>, IAggregationFunctionGenerator<? extends IAggregationFunction>> sqlFunctionGenerators =
			new HashMap<>();
		@Setter private String schema;
		@Setter private String jsonColumnName;
		@Setter private String modelNameColumnName;

		@Getter private ObjectMapper objectMapper;

		@PostConstruct
		public void init() {

			// The injected ObjectMapper may not have DataServicesJacksonModule registered when
			// the shared ObjectMapper is customized externally. Add the module when absent to
			// guarantee query operator subtypes are always available for deserialization.
			if (isDataServicesJacksonModuleRegistered(injectedObjectMapper)) {
				this.objectMapper = injectedObjectMapper.rebuild().build();
			} else {
				this.objectMapper = injectedObjectMapper.rebuild()
					.addModule(new DataServicesJacksonModule(querySubtypeProvider.getSubtypes()))
					.build();
			}

			ctx.getBeansOfType(ILogicOperatorGenerator.class)
				.forEach((name, bean) -> Optional.ofNullable(ctx.findAnnotationOnBean(name, QueryOperatorGenerator.class))
					.map(QueryOperatorGenerator::value)
					.stream()
					.flatMap(Arrays::stream)
					.forEach(o -> sqlConstraintGenerators.put(o, bean)));

			ctx.getBeansOfType(IAggregationFunctionGenerator.class)
				.forEach((name, bean) -> Optional.ofNullable(ctx.findAnnotationOnBean(name, QueryAggregationFunctionGenerator.class))
					.map(QueryAggregationFunctionGenerator::value)
					.stream()
					.flatMap(Arrays::stream)
					.forEach(o -> sqlFunctionGenerators.put(o, bean)));
		}

		public QueryGeneratorContext createContext(QueryContext queryContext) {
			return new DefaultQueryGeneratorContext(schema, QueryGeneratorConstants.TableNames.DOCUMENT_SEARCH_TABLE_NAME, jsonColumnName, modelNameColumnName,
				queryContext.getEnrichments()) {
				@Override public <T extends ILogicOperator> ILogicOperatorGenerator<T> getConstraintGenerator(Class<T> operator) {
					ILogicOperatorGenerator<? extends ILogicOperator> generator = sqlConstraintGenerators.computeIfAbsent(operator,
						o -> {
							throw new QueryInvalidInputException(QUERY_SQL_GENERATION, ExceptionKeys.INVALID_QUERY_ERROR_KEY, null)
								.withAnonymityMessage("No SQL generator registered for operator %s.".formatted(
									Optional.ofNullable(o.getAnnotation(QueryOperator.class)).map(QueryOperator::value).orElse("unknown")));
						});
					return (ILogicOperatorGenerator<T>) generator;
				}

				@Override public <T extends IAggregationFunction> IAggregationFunctionGenerator<T> getFunctionGenerator(Class<T> function) {
					IAggregationFunctionGenerator<? extends IAggregationFunction> generator = sqlFunctionGenerators.computeIfAbsent(function,
						o -> {
							throw new QueryInvalidInputException(QUERY_SQL_GENERATION, ExceptionKeys.INVALID_QUERY_ERROR_KEY, null)
								.withAnonymityMessage("No SQL generator registered for function %s.".formatted(
									Optional.ofNullable(o.getAnnotation(QueryAggregationFunction.class)).map(QueryAggregationFunction::value).orElse("unknown")));
						});
					return (IAggregationFunctionGenerator<T>) generator;
				}

				@Override public void setIsAggregated(boolean aggregated) {
					this.isAggregated = aggregated;
				}

				@Override public boolean isAggregated() {
					return this.isAggregated;
				}

				@Override public void setIsGroupingAgg(boolean groupingAgg) {
					this.isGroupingAgg = groupingAgg;
				}

				@Override public boolean isGroupingAgg() {
					return isGroupingAgg;
				}

				@Override public String getProjectionName() {
					return this.projectionName;
				}

				@Override public void setProjectionName(String projectionName) {
					this.projectionName = projectionName;
				}

				@Override public void setAggregationDefaultPrecision(int precision) {
					this.aggregationDefaultPrecision = precision;
				}

				@Override public int getAggregationDefaultPrecision() {
					return this.aggregationDefaultPrecision;
				}
			};
		}
	}

	private static boolean isDataServicesJacksonModuleRegistered(ObjectMapper objectMapper) {
		return objectMapper.registeredModules().stream()
			.anyMatch(m -> DataServicesJacksonModule.MODULE_NAME.equals(m.getModuleName()));
	}
}
