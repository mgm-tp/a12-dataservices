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
package com.mgmtp.a12.dataservices.documentation.internal.domain;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;

public class EventTriggered extends AbstractLoggedElement {
	@Getter private Object event;

	public EventTriggered(Object event, List<StackTraceElement> callHistory, LoggedEventDirection direction) throws IntrospectionException {
		super(LoggedEventType.EVENT, tidyUpId("event_" + event.getClass().getName()), "**%s**".formatted(event.getClass().getSimpleName()), Instant.now(),
			direction, callHistory);
		this.event = event;
		this.setProperties(Arrays.stream(Introspector.getBeanInfo(event.getClass()).getPropertyDescriptors())
			.filter(m -> Objects.nonNull(m.getName()))
			.collect(Collectors.toMap(FeatureDescriptor::getName, m -> String.valueOf(getValue(m)))));
	}

	@SneakyThrows private Object getValue(PropertyDescriptor m) {
		return m.getReadMethod().invoke(event);
	}

	@Override public AbstractLoggedElement createReturn() throws IntrospectionException {
		return new EventTriggered(event, getCallHistory(), LoggedEventDirection.RETURN);
	}
}
