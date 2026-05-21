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
package com.mgmtp.a12.dataservices.client.cli.internal.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.ApplicationArguments;

import com.mgmtp.a12.dataservices.client.cli.internal.IApplicationOutput;
import com.mgmtp.a12.dataservices.client.exception.A12ClientException;
import com.mgmtp.a12.dataservices.client.exception.RestErrorDetail;
import com.mgmtp.a12.uaa.client.rest.UAARestClientException;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRPCCommand<T> implements ICommandProcessor<T> {
	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_FAILURE = 1;
	public static final int EXIT_INVALID_ARGS = 2;

	protected final IApplicationOutput applicationOutput;

	protected AbstractRPCCommand(@NonNull IApplicationOutput applicationOutput) {
		this.applicationOutput = applicationOutput;
	}

	protected abstract ICommandProcessor.CommandResponse<T> executeRemoteCommand(T args);

	@SneakyThrows
	@Override public ICommandProcessor.CommandResponse<T> execute(T args) {
		PrintWriter output = new PrintWriter(applicationOutput.getDataOutput());
		try {
			return executeRemoteCommand(args);
		} catch (UAARestClientException e) {
			log.error(e.getMessage(), e);
			applicationOutput.getDialogOutput()
				.printf("Failed to communicate with server: %s%nCheck that you have configured server connection and the server is running",
					getCause(e).getMessage());
			output.println(e.getStatus());
			applicationOutput.flush();
			return new ICommandProcessor.CommandResponse<>(1, this);
		}
	}

	private static Throwable getCause(Throwable e) {
		if (e.getCause() == null || e.getCause() == e) {
			return e;
		} else {
			return getCause(e.getCause());
		}
	}

	static void printA12ClientException(PrintWriter output, Exception e) {
		if (e instanceof A12ClientException a12ClientException) {
			Optional.ofNullable((a12ClientException).getErrorDetail())
				.filter(RestErrorDetail.class::isInstance)
				.map(RestErrorDetail.class::cast)
				.map(RestErrorDetail::getResponse)
				.ifPresent(output::println);
		}
	}

	protected static Pair<Integer, Integer> findCommand(ApplicationArguments args, String command) {
		return findCommand(args.getNonOptionArgs(), command.split("\\s+"));
	}

	private static Pair<Integer, Integer> findCommand(List<String> args, String... command) {
		int firstIndex = args.indexOf(command[0]);
		if (firstIndex > -1) {
			Pair<Integer, Integer> range = ImmutablePair.of(firstIndex, firstIndex + command.length);
			if (Arrays.deepEquals(command, Arrays.copyOfRange(args.toArray(String[]::new), range.getLeft(), range.getRight()))) {
				return range;
			} else if (args.size() > firstIndex + 2) {
				return findCommand(args.subList(firstIndex + 1, args.size()), command);
			}
		}
		return null;
	}
}
