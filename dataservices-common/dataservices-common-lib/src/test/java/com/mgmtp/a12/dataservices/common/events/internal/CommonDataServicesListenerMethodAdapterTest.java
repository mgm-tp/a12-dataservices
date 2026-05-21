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
package com.mgmtp.a12.dataservices.common.events.internal;


import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;

import org.assertj.core.api.Assertions;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class CommonDataServicesListenerMethodAdapterTest {

	@Mock
	ApplicationContext applicationContext;

	@Test
	public void testDefaultId() {
		Method method = ReflectionUtils.findMethod(SampleListener.class, "defaultValue", String.class);
		CommonDataServicesListenerMethodAdapter adapter = createTestInstance(method);

		Assertions.assertThat(adapter.getListenerId()).endsWith("SampleListener.defaultValue(java.lang.String)");
	}

	@Test
	public void testIdSet() {
		Method method = ReflectionUtils.findMethod(SampleListener.class, "idSet", String.class);
		CommonDataServicesListenerMethodAdapter adapter = createTestInstance(method);

		Assertions.assertThat(adapter.getListenerId()).endsWith("identifier");
	}

	@Test
	public void testClassesSet() {
		Method method = ReflectionUtils.findMethod(SampleListener.class, "classesSet");
		assertSupportEventType(method, String.class, true);
		assertSupportEventType(method, Integer.class, false);
	}

	@Test
	public void testValueSet() {
		Method method = ReflectionUtils.findMethod(SampleListener.class, "valueSet", String.class);

		assertSupportEventType(method, String.class, true);
		assertSupportEventType(method, Integer.class, true);
		assertSupportEventType(method, Long.class, false);
	}


	private void assertSupportEventType(Method method, Class<?> type, boolean match) {
		CommonDataServicesListenerMethodAdapter adapter = createTestInstance(method);
		Assertions.assertThat(adapter.supportsEventType(createDefaultEventType(type))).isEqualTo(match);
	}

	private ResolvableType createDefaultEventType(Class<?> payloadType) {
		return ResolvableType.forClassWithGenerics(PayloadApplicationEvent.class, payloadType);
	}

	private CommonDataServicesListenerMethodAdapter createTestInstance(Method m) {
		return new CommonDataServicesListenerMethodAdapter("test", SampleListener.class, m, applicationContext) {
			@Override
			protected Object getTargetBean() {
				return new SampleListener();
			}
		};
	}

	public static class SampleListener {

		@CommonDataServicesEventListener
		public void defaultValue(String data) {
		}

		@CommonDataServicesEventListener(value = { String.class, Integer.class })
		public void valueSet(String data) {
		}

		@CommonDataServicesEventListener(value = { String.class })
		public void classesSet() {
		}

		@CommonDataServicesEventListener(id = "identifier" )
		public void idSet(String data) {
		}
	}
}
