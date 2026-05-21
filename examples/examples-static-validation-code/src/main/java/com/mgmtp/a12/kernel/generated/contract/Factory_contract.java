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
package com.mgmtp.a12.kernel.generated.contract;

import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;

public class Factory_contract implements IGeneratedCodeAccessFactory {

    private static final Factory_contract INSTANCE = new Factory_contract();

    private final com.mgmtp.a12.kernel.generated.contract.internal.Factory_contract internalFactory =
        com.mgmtp.a12.kernel.generated.contract.internal.Factory_contract.getInstance();

    public static Factory_contract getInstance() {
        return INSTANCE;
    }

    private Factory_contract() { }

	public IMetaModel getMetaObject() {
		return internalFactory.getMetaObject();
	}

	public IValidator getValidator() {
		return internalFactory.getValidator();
	}

	public ICalculator getCalculator() {
		return internalFactory.getCalculator();
	}

}
