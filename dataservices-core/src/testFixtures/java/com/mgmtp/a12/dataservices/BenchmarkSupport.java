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
package com.mgmtp.a12.dataservices;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 2)
@Warmup(iterations = 2)
@Measurement(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkSupport {

	@Benchmark
	public void testTraditionalForLoop(BenchmarkState state, Blackhole blackhole) {
		long sum = sumArray(state.array);
		blackhole.consume(sum);
	}

	@Benchmark
	public void testStream(BenchmarkState state, Blackhole blackhole) {
		long sum = sumArrayStream(state.array);
		blackhole.consume(sum);
	}

	@State(Scope.Thread)
	public static class BenchmarkState {
		int[] array;

		@Setup
		public void prepare() {
			array = new int[100000000];
			for (int i = 0; i < array.length; i++) {
				array[i] = i;
			}
		}
	}

	private long sumArray(int[] array) {
		long sum = 0;
		for (int i : array) {
			sum += i;
		}
		return sum;
	}

	private long sumArrayStream(int[] array) {
		return Arrays.stream(array).parallel().sum();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder()
			.jvmArgsAppend("-Djmh.separateClasspathJAR=true")
			.parent(new CommandLineOptions(args))
			.build();

		new Runner(opt).run();
	}
}
