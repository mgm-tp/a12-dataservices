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
package com.mgmtp.a12.dataservices.query.projection;

import java.lang.reflect.ParameterizedType;

import org.springframework.data.domain.Page;

import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryContext;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.annotation.QueryProjection;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;

import lombok.NonNull;

/**
 * Projection implementation can preprocess the input query to fetch additional data or to split the querying to multiple phases to properly page, for example.
 * You can define your custom return type, which is independent of `DocumentTreeResult`.
 * So in the postprocess phase you can transform results of type `DocumentTreeResult`
 * to results of different size and different type, for example, by construction real trees from the document graph.
 * It's primarily used to construct CDDs on the fly from the list of CRDs and its linked documents.
 *
 * @param <T> the projected item type returned by this projection.
 */
public interface IQueryProjection<T> {

	/**
	 * Return identifier of the projection. Projection is selected based on this ID by specifying this ID in the {@link QueryRoot#getProjectionName()}.
	 *
	 * @return unique ID of the projection implementation.
	 */
	@NonNull default String getId() {
		return this.getClass().getAnnotation(QueryProjection.class).value();
	}

	/**
	 * Modify the input query to achieve a query to fit projection needs.
	 * You can strip links, for example, to achieve proper paging and then load just links in the {@link #postprocess(QueryRoot, Page, QueryContext)} method.
	 *
	 * @param originalQuery the original input query.
	 * @param context of the originalQuery
	 * @return modified query to fit projection's needs.
	 */
	@NonNull QueryRoot preprocess(@NonNull QueryRoot originalQuery, QueryContext context);

	/**
	 * Postprocess {@link Page} of {@link DocumentTreeResult}s to return items of structure the projection is intended to return.
	 * You can load links, for example, and join with the root document to construct CDD.
	 *
	 * @param originalQuery the original input query.
	 * @param queryResult items returned by the query after postprocessing.
	 * @param context of the originalQuery
	 * @return {@link QueryPage} of items this projection should project to.
	 */
	@NonNull QueryPage<T> postprocess(@NonNull QueryRoot originalQuery, @NonNull Page<DocumentTreeResult> queryResult, QueryContext context);

	/**
	 * @return the generic type of the projection.
	 */
	default Class<T> getType() {
		ParameterizedType type = (ParameterizedType) getClass().getGenericInterfaces()[0];
		return (Class<T>) type.getActualTypeArguments()[0];
	}
}

