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


public class  R_businesspartnersuper implements IValidatorIntern {
	private static final R_businesspartnersuper checkObject = new R_businesspartnersuper();

	private static Meta_businesspartnersuper feldData = Meta_businesspartnersuper.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static R_businesspartnersuper getInstance() {
		return checkObject;
	}

	public void mvk_AttachmentInternalFilenameRequired(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("internal_filename").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.alleKontexteAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.keinFeldAngegeben(idx, allRtIdCon2);

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("internal_filename", idx, "ErrorR29", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("internal_filename", iter.get(), "ErrorR29", e);
		}
	}

	public void mvk_AttachmentMimeTypeRequired(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("mime_type").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.alleKontexteAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.keinFeldAngegeben(idx, allRtIdCon2);

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("mime_type", idx, "ErrorR30", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("mime_type", iter.get(), "ErrorR30", e);
		}
	}

	public void mvk_AttachmentIdOrContentFilled(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("attachment_id").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		final RtIdentifierTemplate rtIdCon3 = RtIdentifierTemplate.builder().unqNm("content").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).add(rtIdCon3).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.nichtGenauEinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.alleKontexteAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.nichtGenauEinFeldAngegeben(idx, allRtIdCon2);

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("content", idx, "ErrorR31", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("content", iter.get(), "ErrorR31", e);
		}
	}

	public void mvk_SizeOfContentFilled(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/SizeOfContentFilled",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("content").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("size").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.keinFeldAngegeben(idx, allRtIdCon2);

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("content", idx, "ErrorR32", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("content", iter.get(), "ErrorR32", e);
		}
	}

	public void mvk_TaxComputation(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Employment/TaxComputation",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);

		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax").isField(true).usb(-2).idx(1).idx(1).idx(1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income").isField(true).usb(-2).idx(1).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.vergleiche(BedingungsOperatorHelper.VglOp.NE, controller.feldWertAlsZahl(idx, rtIdCon1), controller.runden(controller.feldWertAlsZahl(idx, rtIdCon2).multiply(controller.makeDecimal("0.20")), 2)).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.alleFelderAngegeben(idx, allRtIdCon2);
					ValidierungsErgebnis tb2 = controller.vergleiche(BedingungsOperatorHelper.VglOp.NE, controller.feldWertAlsZahl(idx, rtIdCon1), controller.runden(controller.feldWertAlsZahl(idx, rtIdCon2).multiply(controller.makeDecimal("0.20")), 2));

					ValidierungsErgebnis valErg = tb0.combineUND(tb1).combineUND(tb2);

					controller.addValidatorMessage("tax", idx, "TaxComputation", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("tax", iter.get(), "TaxComputation", e);
		}
	}



	public void vordruckF__BusinessPartnerRoot(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruckF__BusinessPartnerRoot_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruckF__BusinessPartnerRoot_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AttachmentInternalFilenameRequired(controller, indices);
	    controller.logMessung("mvk_AttachmentInternalFilenameRequired", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AttachmentMimeTypeRequired(controller, indices);
	    controller.logMessung("mvk_AttachmentMimeTypeRequired", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AttachmentIdOrContentFilled(controller, indices);
	    controller.logMessung("mvk_AttachmentIdOrContentFilled", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_SizeOfContentFilled(controller, indices);
	    controller.logMessung("mvk_SizeOfContentFilled", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_TaxComputation(controller, indices);
	    controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);

	}
	public void vordruck__BusinessPartnerRoot(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruck__BusinessPartnerRoot_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruck__BusinessPartnerRoot_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AttachmentInternalFilenameRequired(controller, indices);
	    controller.logMessung("mvk_AttachmentInternalFilenameRequired", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AttachmentMimeTypeRequired(controller, indices);
	    controller.logMessung("mvk_AttachmentMimeTypeRequired", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_AttachmentIdOrContentFilled(controller, indices);
	    controller.logMessung("mvk_AttachmentIdOrContentFilled", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_SizeOfContentFilled(controller, indices);
	    controller.logMessung("mvk_SizeOfContentFilled", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_TaxComputation(controller, indices);
	    controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);

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
				checker = new TVCheckAlle_businesspartnersuper();
				break;
			case INFO:
                checker = new TVCheckInfo_businesspartnersuper();
                break;
            case HINT:
				checker = new TVCheckHinweis_businesspartnersuper();
				break;
			case ERROR: 
				checker = new TVCheckFehler_businesspartnersuper();
				break;
			default: 
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}

		Collection<RtInternalIdentifier> felder = pruefungsFelderMap.values();
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			ITeilValidierungChecker preChecker = new TVCheckPreliminary_businesspartnersuper();

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
		vordruck__BusinessPartnerRoot(controller);

	}

	private void alleFehlerRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Fehler-Regeln 
		vordruckF__BusinessPartnerRoot(controller);

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


