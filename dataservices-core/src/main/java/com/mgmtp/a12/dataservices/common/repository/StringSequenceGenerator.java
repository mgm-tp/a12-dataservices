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
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.StandardBasicTypeTemplate;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.BigDecimalJavaType;
import org.hibernate.type.descriptor.jdbc.BigIntJdbcType;

import com.mgmtp.a12.dataservices.document.internal.entity.DocumentEntity;

/**
 * Convert generated sequence to string in order to use it for {@link String} fields.
 * Used in {@link DocumentEntity} annotation
 */
public class StringSequenceGenerator extends SequenceStyleGenerator {

	private static final Type SEQ_TYPE = new StandardBasicTypeTemplate<>(BigIntJdbcType.INSTANCE, BigDecimalJavaType.INSTANCE);

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

	/**
	 * Returns the identifier type used by this generator.
	 *
	 * @return The Hibernate {@link Type} representing the sequence type.
	 */
	@Override public Type getIdentifierType() {
		return SEQ_TYPE;
	}

	/**
	 * Configures the generator to use {@link #SEQ_TYPE} irrespective of requested `type`.
	 *
	 * @param type Ignored; the generator uses {@link #SEQ_TYPE}.
	 * @param params Generator parameters.
	 * @param serviceRegistry The Hibernate service registry.
	 * @throws MappingException If configuration fails.
	 */
	@Override public void configure(final Type type, final Properties params, final ServiceRegistry serviceRegistry)
		throws MappingException {
		super.configure(SEQ_TYPE, params, serviceRegistry);
	}
}
