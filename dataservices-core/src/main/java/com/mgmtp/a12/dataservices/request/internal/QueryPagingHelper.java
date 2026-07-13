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
package com.mgmtp.a12.dataservices.request.internal;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.query.DirectFieldOrder;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.Order;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.request.PageSpec;
import com.mgmtp.a12.dataservices.rpc.query.PagedResultSet;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass public class QueryPagingHelper {

	/**
	 * Validate the query paging attributes in order to avoid possible denial of service issues.
	 *
	 * @param query The topology node the paging attributes of which are validated.
	 * @param pageNumberLimit Configurable upper limit of the page number.
	 * @param pageSizeLimit Configurable upper limit of the page size.
	 */
	public static void validatePageRequest(QueryRoot query, int pageNumberLimit, int pageSizeLimit) {
		Paging paging = query.getPaging();

		if (paging == null) {
			throw new InvalidInputException(ExceptionKeys.QUERY_INVALID_PAGING_ERROR_KEY, "Invalid paging: paging must not be null");
		}
		validatePageSpecProperty(pageNumberLimit, paging.pageNumber(), "pageNumber", PageSpec.MIN_PAGE_NUMBER);
		validatePageSpecProperty(pageSizeLimit, paging.pageSize(), "pageSize", PageSpec.MIN_PAGE_SIZE);
	}

	public static @NonNull PageRequest preparePageRequest(Paging paging, List<Order> sort) {
		return PageRequest.of(paging.pageNumber(), paging.pageSize(), makeSort(sort));
	}

	private static void validatePageSpecProperty(int limit, Integer requested, String subject, int minValue) {
		if (requested == null) {
			throw new InvalidInputException(ExceptionKeys.QUERY_INVALID_PAGING_ERROR_KEY, "Invalid paging: %s must not be null".formatted(subject));
		}
		if (requested < minValue) {
			throw new InvalidInputException(ExceptionKeys.QUERY_INVALID_PAGING_ERROR_KEY,
				"Requested value for page attribute %s is invalid, it must be greater or equal to %d.".formatted(subject, minValue));
		}
		if (limit < requested) {
			throw new InvalidInputException(ExceptionKeys.QUERY_PAGE_REQUEST_LIMIT_EXCEEDED_ERROR_KEY,
				"The requested %s is bigger than the allowed limit %d. Change configuration to allow for a higher limit.".formatted(subject, limit));
		}
	}

	private static @NonNull Sort makeSort(List<Order> sort) {
		if (sort == null) {
			return Sort.unsorted();
		}
		// Filter out relationship orders - they are handled directly in SQL generation
		List<Sort.Order> orders = sort.stream()
			.filter(DirectFieldOrder.class::isInstance)
			.map(DirectFieldOrder.class::cast)
			.map(QueryPagingHelper::makeOrder)
			.toList();
		return Sort.by(orders);
	}

	private static @NonNull Sort.Order makeOrder(DirectFieldOrder dfo) {
		if (dfo.field() == null || dfo.direction() == null || dfo.nullHandling() == null || dfo.ignoreCase() == null) {
			log.error("None of the sorting parameters 'field', 'direction', 'nullHandling', and 'ignoreCase' must be null");
			throw new InvalidInputException(ExceptionKeys.QUERY_INVALID_SORTING_ERROR_KEY,
				"None of the sorting parameters 'field', 'direction', 'nullHandling', and 'ignoreCase' must be null");
		}
		Sort.Direction direction = Sort.Direction.valueOf(dfo.direction().name());
		Sort.NullHandling nullHandling = Sort.NullHandling.valueOf(dfo.nullHandling().name());
		return new Sort.Order(direction, dfo.field(), dfo.ignoreCase(), nullHandling);
	}

	public static <T> PagedResultSet<T> pageToResultSet(QueryPage<T> page, boolean isExclude) {

		return PagedResultSet.<T>builder()
			.entries(collectEntries(page.getContent(), isExclude))
			.links(collectLinks(page.getContent()))
			.page(Paging.builder()
				.pageNumber(page.getNumber())
				.pageSize(page.getSize())
				.build())
			.fullSize(page.getTotalElements())
			.otherResults(page.getOtherResults())
			.build();
	}

	private static <T> List<T> collectEntries(List<T> content, boolean isExclude) {
		if (CollectionUtils.isNotEmpty(content) && !(content.getFirst() instanceof DocumentTreeResult)) {
			// If result is not of type `DocumentTreeResult`, add all items to the entries list
			return content;
		} else {
			return content.stream()
				.filter(c -> !isExclude && isPageable(((DocumentTreeResult) c).getType(), isExclude))
				.toList();
		}
	}

	public static boolean isPageable(DocumentTreeNodeType type, boolean isExclude) {
		return (!isExclude && (type == null || DocumentTreeNodeType.ROOT.equals(type))) || (isExclude && DocumentTreeNodeType.CHILD.equals(type));
	}

	private static <T> List<T> collectLinks(List<T> content) {
		return content.stream()
			.filter(DocumentTreeResult.class::isInstance)
			.filter(c -> ((DocumentTreeResult) c).getType() != null && (((DocumentTreeResult) c).getType().equals(DocumentTreeNodeType.CHILD) ||
				((DocumentTreeResult) c).getType().equals(DocumentTreeNodeType.LINK)))
			.toList();
	}

}
