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
package com.mgmtp.a12.dataservices.attachment;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testng.annotations.Factory;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;

import static org.reflections.scanners.Scanners.SubTypes;

public class TestFactory {

	private static String currentAttachmentRepoType = "FS";

	@Factory(enabled = false) public static Object[] makeAttachmentTestDbAndFsVariants() {
		Reflections reflections = new Reflections(AbstractAttachmentTT.class.getPackage().getName());
		return reflections.get(SubTypes.of(AbstractAttachmentTT.class).asClass()).stream()
			.filter(c -> !Modifier.isAbstract(c.getModifiers()))
			.filter(c -> !Modifier.isInterface(c.getModifiers()))
			.map(clazz -> new Object[] {
				makeTestInstance((Class<? extends AbstractAttachmentTT>) clazz, "DB"),
				makeTestInstance((Class<? extends AbstractAttachmentTT>) clazz, "FS")
			})
			.flatMap(Stream::of)
			.toArray(Object[]::new);
	}

	@DynamicPropertySource static void attachmentProperties(DynamicPropertyRegistry registry) {
		registry.add("mgmtp.a12.dataservices.attachments.ext.defaultAttachmentStorage=", () -> currentAttachmentRepoType);
	}

	@SneakyThrows
	private static <T extends AbstractAttachmentTT> T makeTestInstance(Class<T> testClass, String storageType) {
		currentAttachmentRepoType = storageType;
		try (DynamicType.Unloaded<T> unloadedDynamicType = new ByteBuddy()
			.subclass(testClass)
			.name(testClass.getCanonicalName().replaceAll("TT$", "IT"))
			.make()) {
			return unloadedDynamicType
				.load(testClass.getClassLoader())
				.getLoaded()
				.getDeclaredConstructor(String.class)
				.newInstance(storageType);
		}
	}

}
