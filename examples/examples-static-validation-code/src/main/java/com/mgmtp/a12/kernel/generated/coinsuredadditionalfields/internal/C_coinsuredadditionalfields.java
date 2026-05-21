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
package com.mgmtp.a12.kernel.generated.coinsuredadditionalfields.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.CalculationCache.CalculationFieldException;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.*;

public class C_coinsuredadditionalfields implements ICalculatorIntern {
	private static final C_coinsuredadditionalfields checkObject = new C_coinsuredadditionalfields();

	private static Meta_coinsuredadditionalfields feldData = Meta_coinsuredadditionalfields.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();
	private static Set<String> expandedOperandFieldsOfCalculations = Set.of();

	public static C_coinsuredadditionalfields getInstance() {
		return checkObject;
	}

	public void calc_ID(final CalculationController controller)
			throws ValidatorException {	
		/* Berechnung für das Feld '/CoInsuredRoot/ID'
		 */
		controller.initFuerBerechnung("/CoInsuredRoot/ID");

		long[] start = {-2, 1, 1};
		long[] ende = {-2, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("ID").isField(true).usb(-2).idx(1).idx(1).build();

		try {
			for (IterationState idx : iter) {
				try { // Berechnungsalternative aus Regel '/CoInsuredRoot/AddID'
					if (true) {
						RtInternalIdentifier id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("0000-0000-0000-0000", id);
						continue;
					}
				} catch (CalculationFieldException e) {
					// Berechnung wird unterbrochen, wenn der Wert eines Operators, der selbst berechnet wurde, fehlerhaft ist
					// Wird in CalculationCache geworfen. 
					RtInternalIdentifier id = RuntimeController.makeIdentifier(idx, rtIdCon1);
					controller.markiereAlsFehlerhaftBerechnet(id);
					continue;
				} catch (Exception e) {
					RtInternalIdentifier id = RuntimeController.makeIdentifier(idx, rtIdCon1);
					controller.markiereAlsFehlerhaftBerechnet(id);
					controller.addBerechnungsFehler(id);
					continue;
				}

			}
		} catch (Exception e) {
			throw new ValidatorException(e.getMessage(), e);
		}
	}


	public Set<String> getExpandedOperandFieldsOfCalculations() {
		 return expandedOperandFieldsOfCalculations;
	}

	public void calculate(CalculationController controller) {
		long startMesspunkt;
		startMesspunkt = controller.startMesspunkt();
		calc_ID(controller);
		controller.logMessung("calc_ID", startMesspunkt);

	}
}
