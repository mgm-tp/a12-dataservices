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
package com.mgmtp.a12.dataservices.schema;

import java.util.Map;
import java.util.Objects;

import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.internal.DefaultSchemaFilterProvider;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;
import org.hibernate.tool.schema.spi.SchemaValidator;

public class WarnSchemaValidatorManagementTool extends HibernateSchemaManagementTool {

	@Override
	public SchemaValidator getSchemaValidator(Map options) {
		return new WarnSchemaValidator(this, getSchemaFilterProvider(options).getValidateFilter());
	}

	private SchemaFilterProvider getSchemaFilterProvider(Map options) {
		final Object configuredOption = (options == null)
			? null
			: options.get( AvailableSettings.HBM2DDL_FILTER_PROVIDER );
		return Objects.requireNonNull(getServiceRegistry().getService(StrategySelector.class)).resolveDefaultableStrategy(
			SchemaFilterProvider.class,
			configuredOption,
			DefaultSchemaFilterProvider.INSTANCE
		);
	}
}
