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
package com.mgmtp.a12.dataservices.internal;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrentStopWatch {

	private final ThreadLocal<Map<String, StopWatch>> timings = ThreadLocal.withInitial(HashMap::new);
	private final ThreadLocal<ArrayDeque<String>> parents = ThreadLocal.withInitial(ArrayDeque::new);

	public void reset() {
		timings.remove();
		parents.remove();
	}

	public StopWatch getStarted(String name) {

		Deque<String> parentsLocal = parents.get();
		Map<String, StopWatch> timingsLocal = timings.get();

		while (!parentsLocal.isEmpty()) {
			String parent = parentsLocal.peek();
			if (isParentInvalid(timingsLocal.get(parent))) {
				parentsLocal.remove(parent);
			} else {
				name = "%s > %s".formatted(parent, name);
				break;
			}
		}
		return timingsLocal.compute(name, (k, v) -> {
			if (v == null) {
				parentsLocal.push(k);
				return StopWatch.createStarted();
			} else if (v.isSuspended()) {
				parentsLocal.remove(k);
				parentsLocal.push(k);
				v.resume();
				return v;
			} else {
				throw new IllegalStateException("StopWatch with name %s is already running!");
			}
		});
	}

	private static boolean isParentInvalid(StopWatch parentStopWatch) {
		return parentStopWatch == null || !parentStopWatch.isStarted() || parentStopWatch.isSuspended() || parentStopWatch.isStopped();
	}

	@Override public String toString() {
		try {
			Map<String, StopWatch> timingsLocal = timings.get();

			long totalTime = timingsLocal.values().stream()
				.filter(StopWatch::isStopped)
				.map(v -> Pair.of(v.getStartInstant(), v.getStopInstant()))
				.reduce((a, b) -> Pair.of(
					a.getLeft().isBefore(b.getLeft()) ? a.getLeft() : b.getLeft(),
					b.getRight().isAfter(a.getRight()) ? b.getRight() : a.getRight()
				))
				.map(p -> Duration.between(p.getLeft(), p.getRight()))
				.map(Duration::toNanos)
				.orElse(0L);
			long percent = totalTime / 100;

			String format = timingsLocal.keySet().stream()
				.mapToInt(String::length)
				.max()
				.stream()
				.mapToObj(v -> "%-" + v + "s: %15s (%d%%)")
				.findAny()
				.orElse("%s: %15s (%d%%)");

			Comparator<? super Map.Entry<String, StopWatch>> ordering = (Comparator<Map.Entry<String, StopWatch>>) (o1, o2) -> {
				int r = Objects.compare(o1, o2, Comparator.comparing(v -> v.getValue().getStartInstant()));
				if (r != 0) {
					return r;
				}
				return Objects.compare(o1, o2, Comparator.comparingInt(x -> x.getKey().length()));
			};
			return timingsLocal.entrySet().stream()
				.filter(v -> v.getValue().isStopped() || v.getValue().isSuspended())
				.sorted(ordering)
				.map(e -> format.formatted(e.getKey(), e.getValue().formatTime(), e.getValue().getNanoTime() / percent))
				.collect(Collectors.joining("\n"));
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
