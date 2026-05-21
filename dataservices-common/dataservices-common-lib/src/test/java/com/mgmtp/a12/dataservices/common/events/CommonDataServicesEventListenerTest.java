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
package com.mgmtp.a12.dataservices.common.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.common.events.internal.CommonDataServicesEventListenerFactory;
import com.mgmtp.a12.dataservices.common.events.internal.CommonDataServicesEventPublisher;

public class CommonDataServicesEventListenerTest {

	public static final String TEST_EVENT_NAME = "test";
	public static final String NOT_TEST_EVENT_NAME = "not_test";

	@Test public void testEventHasTheSameContext() {
		GenericApplicationContext context = createContext(TestListener.class);

		getPublisher(context).publishEvent(TEST_EVENT_NAME);

		getCollector(context).assertTotalEventsCount(1);
		getCollector(context).assertEvents(TEST_EVENT_NAME);
	}

	@Test public void testEventHasDifferentContext() {
		GenericApplicationContext context1 = createContext(TestListener.class);
		GenericApplicationContext context2 = createContext(TestListener.class);
		context1.setParent(context2);

		getPublisher(context1).publishEvent(TEST_EVENT_NAME);

		getCollector(context1).assertTotalEventsCount(1);
		getCollector(context1).assertEvents(TEST_EVENT_NAME);
		getCollector(context2).assertTotalEventsCount(0);
	}

	@Test public void testSetCondition() {
		GenericApplicationContext context = createContext(SetConditionTestListener.class);

		// Assert if the condition does not match
		getPublisher(context).publishEvent(NOT_TEST_EVENT_NAME);
		getCollector(context).assertTotalEventsCount(0);

		// Assert if the condition matches
		getPublisher(context).publishEvent(TEST_EVENT_NAME);
		getCollector(context).assertTotalEventsCount(1);
		getCollector(context).assertEvents(TEST_EVENT_NAME);
	}

	private EventCollector getCollector(ApplicationContext applicationContext) {
		return applicationContext.getBean(EventCollector.class);
	}

	private ApplicationEventPublisher getPublisher(ApplicationContext applicationContext) {
		return applicationContext.getBean(ApplicationEventPublisher.class);
	}

	private GenericApplicationContext createContext(Class<?>... classes) {
		List<Class<?>> allClasses = new ArrayList<>();
		allClasses.add(BaseConfiguration.class);
		allClasses.addAll(Arrays.asList(classes));
		return new AnnotationConfigApplicationContext(allClasses.toArray(new Class<?>[0]));
	}

	@Configuration static class BaseConfiguration {
		@Bean public CommonDataServicesEventListenerFactory CommonDataServicesListenerFactory() {
			return new CommonDataServicesEventListenerFactory();
		}

		@Bean public EventCollector eventCollector() {
			return new EventCollector();
		}

		@Primary @Bean public ApplicationEventPublisher CommonDataServicesEventPublisher() {
			return new CommonDataServicesEventPublisher();
		}
	}

	@Component static class BaseTestListener {
		@Autowired private EventCollector eventCollector;

		public void handleEvent(String data) {
			this.eventCollector.addEvent(data);
		}
	}

	@Component static class TestListener extends BaseTestListener {
		@CommonDataServicesEventListener public void handleEventHasTheSameContext(String data) {
			handleEvent(data);
		}
	}

	@Component static class SetConditionTestListener extends BaseTestListener {
		@CommonDataServicesEventListener(condition = "'" + TEST_EVENT_NAME + "'.equals(#data)")
		public void handleSetConditionTestListener(String data) {
			handleEvent(data);
		}
	}

	static class EventCollector {
		private final List<Object> events = new LinkedList<>();

		public void addEvent(Object event) {
			this.events.add(event);
		}

		public List<Object> getEvents() {
			return this.events;
		}

		public void assertEvents(Object... expected) {
			List<Object> actual = getEvents();
			Assertions.assertThat(actual).hasSize(expected.length);
			for (int i = 0; i < expected.length; i++) {
				Assertions.assertThat(actual.get(i)).isEqualTo(expected[i]);
			}
		}

		public void assertTotalEventsCount(int number) {
			Assertions.assertThat(getEvents()).hasSize(number);
		}
	}

}
