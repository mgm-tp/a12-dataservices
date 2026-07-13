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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.*;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.CustomConditionException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;


public class  R_coinsuredadditionalfields implements IValidatorIntern {
	private static final R_coinsuredadditionalfields checkObject = new R_coinsuredadditionalfields();

	private static Meta_coinsuredadditionalfields feldData = Meta_coinsuredadditionalfields.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static R_coinsuredadditionalfields getInstance() {
		return checkObject;
	}

	public void mvk_AddID(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/CoInsuredRoot/AddID",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1};
		long[] ende = {-2, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);

		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("ID").isField(true).usb(-2).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.vergleicheSTRING(BedingungsOperatorHelper.VglOp.NE, controller.feldWert(idx, rtIdCon1), "0000-0000-0000-0000").isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.vergleicheSTRING(BedingungsOperatorHelper.VglOp.NE, controller.feldWert(idx, rtIdCon1), "0000-0000-0000-0000");

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("ID", idx, "AddID", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("ID", iter.get(), "AddID", e);
		}
	}



	public void vordruckF__CoInsuredRoot(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck CoInsuredRoot
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruckF__CoInsuredRoot_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruckF__CoInsuredRoot_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck CoInsuredRoot für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AddID(controller, indices);
	    controller.logMessung("mvk_AddID", indices[0], startMesspunkt);

	}
	public void vordruck__CoInsuredRoot(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck CoInsuredRoot
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruck__CoInsuredRoot_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruck__CoInsuredRoot_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck CoInsuredRoot für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AddID(controller, indices);
	    controller.logMessung("mvk_AddID", indices[0], startMesspunkt);

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
				checker = new TVCheckAlle_coinsuredadditionalfields();
				break;
			case INFO:
                checker = new TVCheckInfo_coinsuredadditionalfields();
                break;
            case HINT:
				checker = new TVCheckHinweis_coinsuredadditionalfields();
				break;
			case ERROR: 
				checker = new TVCheckFehler_coinsuredadditionalfields();
				break;
			default: 
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}

		Collection<RtInternalIdentifier> felder = pruefungsFelderMap.values();
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			ITeilValidierungChecker preChecker = new TVCheckPreliminary_coinsuredadditionalfields();

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
		vordruck__CoInsuredRoot(controller);

	}

	private void alleFehlerRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Fehler-Regeln 
		vordruckF__CoInsuredRoot(controller);

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


