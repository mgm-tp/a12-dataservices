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
package com.mgmtp.a12.dataservices.relationship;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.mgmtp.a12.dataservices.rpc.query.PageSpec;

import lombok.NonNull;

/**
 * `PageRequest` that accepts result `offset` as construction parameter.
 * The `pageNumber` is calculated from `offset` via `offset` / `pageSize`.
 */
public class OffsetBasedPageRequest extends PageRequest {

	private final long offset;

	protected OffsetBasedPageRequest(long offset, int size, Sort sort) {
		super((int) (offset / size), size, sort);
		if (offset < 0) {
			throw new IllegalArgumentException("Offset index must not be less than zero!");
		}
		this.offset = offset;
	}

	/**
	 * Creates a page request from a zero-based result offset.
	 *
	 * @param offset Zero-based offset into the result set; must be >= 0.
	 * @param limit Page size (maximum number of items per page); must be > 0.
	 * @param sort Sort definition to apply; use {@link Sort#unsorted()} for none.
	 * @return A new {@link OffsetBasedPageRequest} covering the given range.
	 */
	public static OffsetBasedPageRequest ofOffset(int offset, int limit, Sort sort) {
		return new OffsetBasedPageRequest(offset, limit, sort);
	}

	@Override public int getPageNumber() {
		return (int) (offset / getPageSize());
	}

	@Override public long getOffset() {
		return offset;
	}

	@Override public boolean hasPrevious() {
		return offset > getPageSize();
	}

	@Override public @NonNull OffsetBasedPageRequest next() {
		return new OffsetBasedPageRequest(offset + getPageSize(), getPageSize(), getSort());
	}

	@Override public @NonNull OffsetBasedPageRequest previous() {
		return getOffset() > getPageSize() ? this : new OffsetBasedPageRequest(getOffset() - getPageSize(), getPageSize(), getSort());
	}

	@Override public @NonNull OffsetBasedPageRequest first() {
		return new OffsetBasedPageRequest(0, getPageSize(), getSort());
	}

	@Override public @NonNull OffsetBasedPageRequest withPage(int pageNumber) {
		return new OffsetBasedPageRequest((long) pageNumber * getPageSize(), getPageSize(), getSort());
	}

	@Override public @NonNull OffsetBasedPageRequest withSort(@NonNull Sort sort) {
		return new OffsetBasedPageRequest(getOffset(), getPageSize(), sort);
	}

	@Override public @NonNull OffsetBasedPageRequest withSort(@NonNull Sort.Direction direction, String @NonNull ... properties) {
		return new OffsetBasedPageRequest(getOffset(), getPageSize(), Sort.by(direction, properties));
	}

	/**
	 * Creates an unpaged request with default maximum limit and no sorting.
	 *
	 * @return An {@link OffsetBasedPageRequest} with offset {@link PageSpec#NO_OFFSET} and default limit.
	 */
	public static OffsetBasedPageRequest unpaged() {
		return new OffsetBasedPageRequest(PageSpec.NO_OFFSET, PageSpec.MAX_RESULTS.getLimit(), Sort.unsorted());
	}

	/**
	 * Creates an unpaged request with default maximum limit and the given sorting.
	 *
	 * @param sort Sort definition to apply; must not be null.
	 * @return An {@link OffsetBasedPageRequest} with default limit and the given sort.
	 */
	public static OffsetBasedPageRequest unpaged(Sort sort) {
		return new OffsetBasedPageRequest(PageSpec.NO_OFFSET, PageSpec.MAX_RESULTS.getLimit(), sort);
	}
}

