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
package com.mgmtp.a12.dataservices.rpc.internal.marshalling;

import java.util.Collection;
import java.util.Collections;

import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleModule;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.marshalling.DocumentReferenceSerializer;
import com.mgmtp.a12.dataservices.marshalling.internal.DocumentReferenceDeserializer;

/**
 * Jackson 3.x module for Data Services that combines two responsibilities:
 *
 * . *Serialization* — registers a custom serializer and deserializer for {@link DocumentReference}
 *   so that document references are written and read as plain JSON strings.
 * . *Polymorphic subtype resolution* — registers caller-supplied subtypes with the Jackson
 *   subtype resolver so that query constraint trees can be deserialized polymorphically.
 *
 * Under Spring Boot the module is registered as a bean and picked up automatically by
 * `StandardJsonMapperBuilderCustomizer`. When used outside of Spring Boot, construct the module
 * directly and register it on the `ObjectMapper`.
 */
public class DataServicesJacksonModule extends SimpleModule {

	public static final String MODULE_NAME = "DataServicesJacksonModule";

	private final Collection<NamedType> subtypes;

	/**
	 * Creates the module.
	 *
	 * {@link DocumentReference} serialization is always registered regardless of the subtypes
	 * argument. Pass an empty collection when no polymorphic subtype support is needed.
	 *
	 * @param subtypes subtypes to register for polymorphic deserialization; must not be null, may be empty.
	 */
	public DataServicesJacksonModule(Collection<NamedType> subtypes) {
		super(MODULE_NAME);
		this.subtypes = subtypes;
		addSerializer(DocumentReference.class, new DocumentReferenceSerializer());
		addDeserializer(DocumentReference.class, new DocumentReferenceDeserializer());
	}

	/**
	 * Returns an unmodifiable view of the subtypes registered by this module.
	 *
	 * @return subtypes; never null, may be empty.
	 */
	Collection<NamedType> getSubtypes() {
		return Collections.unmodifiableCollection(subtypes);
	}

	/**
	 * Invoked by Jackson during mapper construction. Delegates serializer/deserializer
	 * registration to the parent and additionally registers all subtypes supplied at
	 * construction time.
	 */
	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		if (subtypes != null && !subtypes.isEmpty()) {
			context.registerSubtypes(subtypes.toArray(new NamedType[0]));
		}
	}
}
