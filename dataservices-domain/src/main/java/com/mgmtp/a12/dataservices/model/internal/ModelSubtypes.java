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
package com.mgmtp.a12.dataservices.model.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data @EqualsAndHashCode(of = "modelName")
@RequiredArgsConstructor
public class ModelSubtypes implements Iterable<ModelSubtypes> {

	private final String modelName;
	private final Set<ModelSubtypes> directSubtypes = new HashSet<>();

	public ModelSubtypes(String modelName, Set<ModelSubtypes> subTypes) {
		this.modelName = modelName;
		this.directSubtypes.addAll(subTypes);
	}

	@Override public @NonNull Iterator<ModelSubtypes> iterator() {
		return new Iterator<>() {
			private final Deque<Iterator<ModelSubtypes>> stack = new ArrayDeque<>();

			{
				this.stack.push(directSubtypes.iterator());
			}

			@Override public boolean hasNext() {
				if (stack.isEmpty()) {
					return false;
				} else if (stack.peek().hasNext()) {
					return true;
				} else {
					stack.pop();
					return hasNext();
				}
			}

			@Override public ModelSubtypes next() {
				if (stack.isEmpty()) {
					throw new NoSuchElementException();
				}
				Iterator<ModelSubtypes> currentIterator = stack.peek();
				if (currentIterator.hasNext()) {
					ModelSubtypes item = currentIterator.next();
					if (CollectionUtils.isNotEmpty(item.getDirectSubtypes())) {
						stack.push(item.getDirectSubtypes().iterator());
					}
					return item;
				} else {
					stack.pop();
					return next();
				}
			}
		};
	}
}
