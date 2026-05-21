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
package com.mgmtp.a12.kernel.generated.anonymizedbusinesspartnercdm.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.ArrayUtils;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.ListBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.LinkedHashMapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.EbenenIterator;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.FilterPredicate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.LongArray;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.CustomConditionException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;


public class  R_anonymizedbusinesspartnercdm implements IValidatorIntern {
	private static final R_anonymizedbusinesspartnercdm checkObject = new R_anonymizedbusinesspartnercdm();

	private static Meta_anonymizedbusinesspartnercdm feldData = Meta_anonymizedbusinesspartnercdm.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static R_anonymizedbusinesspartnercdm getInstance() {
		return checkObject;
	}




	private final Map<String, Consumer<MainValidatorController>> uniqueFieldNameToPreliminaryValidationFunc = Map.ofEntries(
	);

	public void validatePreliminaryRulesForField(MainValidatorController controller, String uniqueFieldName) {
        var validationFunc = uniqueFieldNameToPreliminaryValidationFunc.get(uniqueFieldName);
        if (validationFunc != null) {
            validationFunc.accept(controller);
        }
	}

	public void validatePreliminaryRules(MainValidatorController controller) {
	}

	public void validiereVoll(MainValidatorController controller, ValidationMode modus) {
		switch (modus.getRuleType()) {
			case ALL:
				alleRegeln(controller);
				break;
			case INFO:
                alleInfoRegeln(controller);
                break;
            case HINT:
				alleHinweisRegeln(controller);
				break;
			case ERROR:
				alleFehlerRegeln(controller);
				break;
			default:
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
	}	

	public void validiereTeil(MainValidatorController controller, ValidationMode modus, Map<String, RtInternalIdentifier> pruefungsFelderMap) {
		ITeilValidierungChecker checker = null;
		switch (modus.getRuleType()) {
			case ALL:
				checker = new TVCheckAlle_anonymizedbusinesspartnercdm();
				break;
			case INFO:
                checker = new TVCheckInfo_anonymizedbusinesspartnercdm();
                break;
            case HINT:
				checker = new TVCheckHinweis_anonymizedbusinesspartnercdm();
				break;
			case ERROR: 
				checker = new TVCheckFehler_anonymizedbusinesspartnercdm();
				break;
			default: 
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}

		Collection<RtInternalIdentifier> felder = pruefungsFelderMap.values();
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			ITeilValidierungChecker preChecker = new TVCheckPreliminary_anonymizedbusinesspartnercdm();

			for (RtInternalIdentifier id : felder) {
				int interneFeldNr = metaDataValidierung.getInterneFeldNr(id.getName());
				preChecker.fuehreAus(controller, interneFeldNr, id.getIndices());
			}
		}

		for (RtInternalIdentifier id : felder) {
			int interneFeldNr = metaDataValidierung.getInterneFeldNr(id.getName());
			checker.fuehreAus(controller, interneFeldNr, id.getIndices());
		}
	}

	private void alleRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Regeln 
	}

	private void alleFehlerRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Fehler-Regeln 
	}

	private void alleHinweisRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Hinweis-Regeln 
	}

	private void alleInfoRegeln(MainValidatorController controller) 
                    throws ValidatorException { 
        //Aufruf aller Info-Regeln 
    }
}


