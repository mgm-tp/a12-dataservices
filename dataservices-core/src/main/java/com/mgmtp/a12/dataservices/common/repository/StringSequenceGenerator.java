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
package com.mgmtp.a12.dataservices.common.repository;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.HibernateException;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import com.mgmtp.a12.dataservices.document.internal.entity.DocumentEntity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Convert generated sequence to string in order to use it for {@link String} fields.
 * Used in {@link DocumentEntity} annotation
 */
public class StringSequenceGenerator extends SequenceStyleGenerator {

	public static final String GENERATOR_NAME = "string-sequence-generator";

	/**
	 * Annotation to configure the string sequence generator.
	 */
	@IdGeneratorType(StringSequenceGenerator.class)
	@Target({FIELD, METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Sequence {
		/**
		 * The name of the database sequence.
		 */
		String name();

		/**
		 * The increment size for the sequence.
		 */
		int incrementSize() default 1;
	}

	/**
	 * Generates a sequence value and converts it to a {@link String}.
	 *
	 * @param session The Hibernate session contract.
	 * @param object The entity instance for which the identifier is generated.
	 * @return The generated identifier as a {@link String}.
	 * @throws HibernateException If identifier generation fails.
	 */
	@Override public Serializable generate(final SharedSessionContractImplementor session, final Object object) throws HibernateException {
		return super.generate(session, object).toString();
	}
}
