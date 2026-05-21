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
package com.mgmtp.a12.dataservices.query.generator.sql;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mgmtp.a12.dataservices.query.constraint.ILogicOperator;
import com.mgmtp.a12.dataservices.query.enrichement.Enrichments;
import com.mgmtp.a12.dataservices.query.fields.aggregation.IAggregationFunction;

/**
 * Context of the query's current processing state. Is passed to custom generators, so it has information needed for rendering.
 * Single instance shouldn't be accessed by multiple threads.
 */
public interface QueryGeneratorContext {
	<T extends ILogicOperator> ILogicOperatorGenerator<T> getConstraintGenerator(Class<T> operator);

	<T extends IAggregationFunction> IAggregationFunctionGenerator<T> getFunctionGenerator(Class<T> function);

	String getDocumentTableName();

	String getSchema();

	String getTargetDocumentModel();

	void setTargetDocumentModel(String documentModel);

	int getHardLimit();

	void setHardLimit(int limit);

	void setPageLimit(int limit);

	int getPageLimit();

	void setPageOffset(int limit);

	int getPageOffset();

	void setLocale(String locale);

	String getLocale();

	AtomicInteger getTableCounter();

	AtomicInteger getParamCounter();

	/**
	 * Retrieves the parameter holder containing parameters for the current query generation context.
	 * This is used for `jakarta.persistence.Query#setParameter(java.lang.String, java.lang.Object)` to keep unique parameter names.
	 *
	 * @return a map where the keys are parameter names and the values are parameter objects.
	 */
	Map<String, Object> getParamHolder();

	String getCurrentDocumentTableAlias();

	String registerNewDocumentTableAlias(String tableAlias);

	String registerNewDocumentTableAlias();

	String unregisterDocumentTableAlias();

	String getCurrentTargetDocRef();

	void registerNewTargetDocRef(String targetDocRef);

	void unregisterTargetDocRef();

	String getModelNameColumnName();

	boolean registerRecursion(boolean recursion);

	boolean getCurrentRecursionState();

	boolean unregisterRecursion();

	CharSequence getCurrentCteAlias();

	CharSequence registerCteAlias(CharSequence recursion);

	CharSequence unregisterCteAlias();

	CharSequence getJsonColumnName();

	void setIsAggregated(boolean aggregated);

	boolean isAggregated();

	void setIsGroupingAgg(boolean groupingAgg);

	boolean isGroupingAgg();

	Enrichments getEnrichments();

	boolean isExclude();

	void setExclude(boolean isExclude);

	void setProjectionName(String projectionName);

	String getProjectionName();

	void setAggregationDefaultPrecision(int precision);

	int getAggregationDefaultPrecision();
}

