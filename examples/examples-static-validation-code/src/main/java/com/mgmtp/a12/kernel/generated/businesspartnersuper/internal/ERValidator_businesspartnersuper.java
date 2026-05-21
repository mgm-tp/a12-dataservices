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
package com.mgmtp.a12.kernel.generated.businesspartnersuper.internal;

import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.CalculationCommand;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.ValidationCommand;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;

public class ERValidator_businesspartnersuper
	implements IValidator, ICalculator {
	// if you change the order of these two lines consider the order of (static) initializer execution carefully
	private static final IMetaDataValidierung metaData = Meta_businesspartnersuper.getInstance().getMetaDataValidierung();
	private static final ERValidator_businesspartnersuper instance = new ERValidator_businesspartnersuper();

	private ERValidator_businesspartnersuper () {
	}

	public static ERValidator_businesspartnersuper getInstance() {
		return instance;
	}

	private void pruefeParams(
			IData daten,
			IResult ergebnis,
			ValidationMode modus) {
		if (daten == null) {
			throw new IllegalArgumentException("Übergebene Daten dürfen nicht null sein");
		}
		if (ergebnis == null) {
			throw new IllegalArgumentException("Übergebenes Ergebnis darf nicht null sein");
		}
		if (modus != null && !Meta_businesspartnersuper.getInstance().isSpracheUnterstuetzt(modus.getLanguage())) {
			throw new IllegalArgumentException("Die im ValidierungsModus übergebene Sprache " + modus.getLanguage() + " wird nicht unterstützt.");
		}
	} 

    /**
     * @deprecated since version 30.6.0. Will be removed with A12K-3589.
     */
    @Deprecated(since = "30.6.0")
    @SuppressWarnings("removal")
	public void validatePartlyWith3ValueLogic(
			IData daten, 
			Set<IIdentifier> relevantEntities,
			Map<String, ICustomCondition> applBedingungen,
			IResult ergebnis,
			ValidationMode modus) throws ValidatorException {
		pruefeParams(daten, ergebnis, modus);
		ValidationCommand c = new ValidationCommand(R_businesspartnersuper.getInstance(), modus, metaData, daten);
		c.processPartlyValidation(daten, relevantEntities, applBedingungen, ergebnis);
	}

	public void validatePartlyWith3ValueLogic(
			IData daten,
			Set<IIdentifier> relevantEntities,
			IResult ergebnis,
			ValidationMode modus) throws ValidatorException {
		pruefeParams(daten, ergebnis, modus);
		ValidationCommand c = new ValidationCommand(R_businesspartnersuper.getInstance(), modus, metaData, daten);
		c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}


    /**
     * @deprecated since version 30.6.0. Will be removed with A12K-3589.
     */
    @Deprecated(since = "30.6.0")
    @SuppressWarnings("removal")
	public void validateAll(IData daten,
			Map<String, ICustomCondition> applBedingungen,
			IResult ergebnis,
			ValidationMode modus) throws ValidatorException {
		validateAllWithDefaults(daten, applBedingungen, ergebnis, modus, null);
	}

	public void validateAll(IData daten,
			IResult ergebnis,
			ValidationMode modus) throws ValidatorException {
		pruefeParams(daten, ergebnis, modus);
		ValidationCommand c = new ValidationCommand(R_businesspartnersuper.getInstance(), modus, metaData, daten);
		c.processFullValidation(daten, ergebnis, null);
	}

	/**
	 * This method is not contained in any interface, because it is not supposed to be used by applications. 
	 * It is used internally for 'on-the-fly testing and by validateAll.
     * Will be removed with A12K-3593.
 	 */
	public void validateAllWithDefaults(IData daten,
			Map<String, ICustomCondition> applBedingungen,
			IResult ergebnis,
			ValidationMode modus, 
			Set<IIdentifier> defaultValues) throws ValidatorException {
		pruefeParams(daten, ergebnis, modus);
		ValidationCommand c = new ValidationCommand(R_businesspartnersuper.getInstance(), modus, metaData, daten);
		c.processFullValidation(daten, applBedingungen, ergebnis, defaultValues);
	}

	public ICalculationResult calculateAll(IData daten, ValidationMode modus, IResult ergebnis) {
		pruefeParams(daten, ergebnis, modus);
		CalculationCommand c = new CalculationCommand(R_businesspartnersuper.getInstance(), modus, metaData, daten, C_businesspartnersuper.getInstance());
		ICalculationResult result = c.processCalc(daten, ergebnis);
		return result;
	}
}
