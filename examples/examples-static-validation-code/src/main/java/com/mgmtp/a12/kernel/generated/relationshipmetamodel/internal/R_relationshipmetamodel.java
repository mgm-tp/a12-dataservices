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
package com.mgmtp.a12.kernel.generated.relationshipmetamodel.internal;

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


public class  R_relationshipmetamodel implements IValidatorIntern {
	private static final R_relationshipmetamodel checkObject = new R_relationshipmetamodel();

	private static Meta_relationshipmetamodel feldData = Meta_relationshipmetamodel.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static R_relationshipmetamodel getInstance() {
		return checkObject;
	}

	public void mvk_mustHaveAtLeastOneLocale(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/mustHaveAtLeastOneLocale",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1};
		long[] ende = {-2, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);

		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("code").isField(true).usb(-2).idx(1).idx(1, 9999).idx(1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("modelType_1").isField(true).usb(-2).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.keinKontextAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.keinKontextAngegeben(idx, allRtIdCon1);
					ValidierungsErgebnis tb1 = controller.alleFelderAngegeben(idx, allRtIdCon2);

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("modelType_1", idx, "Error rule_e366c", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("modelType_1", iter.get(), "Error rule_e366c", e);
		}
	}

	public void mvk_whenNotUnboundedUpperLimitShouldBeSet(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1, 1, 1};
		long[] ende = {-2, 1, 2, 1, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("unbounded").isField(true).usb(-2).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("upperLimit").isField(true).usb(-2).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.vergleicheSTRING(BedingungsOperatorHelper.VglOp.EQ, controller.feldWert(idx, rtIdCon1), "false").isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
					ValidierungsErgebnis tb0 = controller.vergleicheSTRING(BedingungsOperatorHelper.VglOp.EQ, controller.feldWert(idx, rtIdCon1), "false");
					ValidierungsErgebnis tb1 = controller.keinFeldAngegeben(idx, allRtIdCon1);

					ValidierungsErgebnis valErg = tb0.combineUND(tb1);

					controller.addValidatorMessage("upperLimit", idx, "Error rule_3ce1d", valErg.getFehlerTyp());

				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("upperLimit", iter.get(), "Error rule_3ce1d", e);
		}
	}

	public void mvk_vk_vkid_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/vk_vkid_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1};
		long[] ende = {-2, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);

		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("id").isField(true).usb(-2).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			for (IterationState idx : iter) {
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
					controller.preliminaryError("id", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("id", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkmodelType_req_1(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/vk_vkmodelType_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1};
		long[] ende = {-2, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);

		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("modelType_1").isField(true).usb(-2).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			for (IterationState idx : iter) {
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
					controller.preliminaryError("modelType_1", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("modelType_1", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkcode_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/locales/vk_vkcode_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/locales").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("code").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("code", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("code", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkname_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/annotations/vk_vkname_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/annotations").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("name").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("name", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("name", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkmodelType_req_0(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/modelReferences/vk_vkmodelType_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/modelReferences").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("modelType_0").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("modelType_0", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("modelType_0", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkreference_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/modelReferences/vk_vkreference_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/modelReferences").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("reference").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("reference", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("reference", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkduplicatesAllowed_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/vk_vkduplicatesAllowed_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1};
		long[] ende = {-2, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);

		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("duplicatesAllowed").isField(true).usb(-2).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			for (IterationState idx : iter) {
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
					controller.preliminaryError("duplicatesAllowed", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("duplicatesAllowed", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkrole_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/entityCharacteristics/vk_vkrole_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, 2, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("role").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("role", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("role", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkdocumentModel_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/entityCharacteristics/vk_vkdocumentModel_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, 2, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("documentModel").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("documentModel", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("documentModel", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkordered_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/entityCharacteristics/vk_vkordered_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, 2, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("ordered").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("ordered", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("ordered", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vkunbounded_req(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/entityCharacteristics/linkConstraints/multiplicity/vk_vkunbounded_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1, 1, 1};
		long[] ende = {-2, 1, 2, 1, 1, 1};
		EbenenIterator iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("unbounded").isField(true).usb(-2).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("unbounded", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("unbounded", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vklocales_vk_grp_code_unq(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/vk_vklocales_vk_grp_code_unq",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("code").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			RepetitionNotUniqueErrorCache repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			for (IterationState idx : iter) {
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 2)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, new int[] {2}, repNotUnqErrorCache, iter.getSemanticIndexLevel()).isKnownAndTrue()) {
					controller.preliminaryError("code", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("code", iter.get(), "uniqueField", e);
		}
	}

	public void mvk_vk_vklabels_vk_grp_locale_unq_1(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/vk_vklabels_vk_grp_locale_unq",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("locale_2").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			RepetitionNotUniqueErrorCache repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			for (IterationState idx : iter) {
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 2)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, new int[] {2}, repNotUnqErrorCache, iter.getSemanticIndexLevel()).isKnownAndTrue()) {
					controller.preliminaryError("locale_2", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("locale_2", iter.get(), "uniqueField", e);
		}
	}

	public void mvk_vk_vkannotations_vk_grp_name_unq(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/vk_vkannotations_vk_grp_name_unq",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("name").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			RepetitionNotUniqueErrorCache repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			for (IterationState idx : iter) {
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 2)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, new int[] {2}, repNotUnqErrorCache, iter.getSemanticIndexLevel()).isKnownAndTrue()) {
					controller.preliminaryError("name", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("name", iter.get(), "uniqueField", e);
		}
	}

	public void mvk_vk_vklabels_vk_grp_locale_unq_0(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/vk_vklabels_vk_grp_locale_unq",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("locale_1").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();

		try {
			RepetitionNotUniqueErrorCache repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			for (IterationState idx : iter) {
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 2)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, new int[] {2}, repNotUnqErrorCache, iter.getSemanticIndexLevel()).isKnownAndTrue()) {
					controller.preliminaryError("locale_1", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("locale_1", iter.get(), "uniqueField", e);
		}
	}

	public void mvk_vk_vklocale_req_1(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/header/labels/vk_vklocale_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/labels").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("locale_2").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("locale_2", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("locale_2", iter.get(), "mandatoryField", e);
		}
	}

	public void mvk_vk_vklocale_req_0(final MainValidatorController controller, long[] indices)
			throws ValidatorException {	
		controller.initFuerRegelpruefung("/content/labels/vk_vklocale_req",MainValidatorController.PruefErgebnisTyp.Fehler, false);

		long[] start = {-2, 1, 1, 1};
		long[] ende = {-2, 1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1};
		EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}


		final RtIdentifierTemplate rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/labels").isField(false).usb(-2).idx(1).idx(-1).build();
		final RtIdentifierTemplate rtIdCon2 = RtIdentifierTemplate.builder().unqNm("locale_1").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
		List<RtIdentifierTemplate> allRtIdCon1 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon1).build();
		List<RtIdentifierTemplate> allRtIdCon2 = new ListBuilder<RtIdentifierTemplate>().add(rtIdCon2).build();

		try {
			for (IterationState idx : iter) {
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
					controller.preliminaryError("locale_1", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				} 
			}    
		} catch (CustomConditionException cce) {
		    throw cce;
		} catch (Exception e) {
			controller.addValidatorMessage("locale_1", iter.get(), "mandatoryField", e);
		}
	}



	public void vordruckF__header(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck header
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("header", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("header", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruckF__header_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruckF__header_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck header für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_mustHaveAtLeastOneLocale(controller, indices);
	    controller.logMessung("mvk_mustHaveAtLeastOneLocale", indices[0], startMesspunkt);

	}
	public void vordruckF__content(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck content
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("content", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("content", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruckF__content_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruckF__content_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck content für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_whenNotUnboundedUpperLimitShouldBeSet(controller, indices);
	    controller.logMessung("mvk_whenNotUnboundedUpperLimitShouldBeSet", indices[0], startMesspunkt);

	}
	public void vordruck__header(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck header
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("header", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("header", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruck__header_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruck__header_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck header für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_mustHaveAtLeastOneLocale(controller, indices);
	    controller.logMessung("mvk_mustHaveAtLeastOneLocale", indices[0], startMesspunkt);

	}
	public void vordruck__content(MainValidatorController controller)  throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck content
	    H: nur Hinweise
	    F: nur Fehler
	    kein Zusatz: fuer alle
	  */
		if (controller.mindestensEinVordruckAngegeben("content", 0, 0).isKnownAndTrue()) {
		    long maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
	 	    for (int l = 1; l <= maxLfdNummer; l++) {
	 	        if (controller.mindestensEinVordruckAngegeben("content", l, 0).isKnownAndTrue()) {
	 	        	long[] indices = {l};
	 	            vordruck__content_lfdNr(controller, indices);
	 	        }
		  	}
		}
	}

	public void vordruck__content_lfdNr(MainValidatorController controller, long[] indices)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck content für eine spezifische lfdNr.
	  */
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_whenNotUnboundedUpperLimitShouldBeSet(controller, indices);
	    controller.logMessung("mvk_whenNotUnboundedUpperLimitShouldBeSet", indices[0], startMesspunkt);

	}
	public void vordruckP__global(MainValidatorController controller)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck global für eine spezifische lfdNr.
	  */
		long[] indices = {0};
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkid_req(controller, indices);
	    controller.logMessung("mvk_vk_vkid_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkmodelType_req_1(controller, indices);
	    controller.logMessung("mvk_vk_vkmodelType_req_1", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkduplicatesAllowed_req(controller, indices);
	    controller.logMessung("mvk_vk_vkduplicatesAllowed_req", indices[0], startMesspunkt);

	}
	public void vordruckP__header(MainValidatorController controller)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck header für eine spezifische lfdNr.
	  */
		long[] indices = {0};
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkcode_req(controller, indices);
	    controller.logMessung("mvk_vk_vkcode_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkname_req(controller, indices);
	    controller.logMessung("mvk_vk_vkname_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkmodelType_req_0(controller, indices);
	    controller.logMessung("mvk_vk_vkmodelType_req_0", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkreference_req(controller, indices);
	    controller.logMessung("mvk_vk_vkreference_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vklocales_vk_grp_code_unq(controller, indices);
	    controller.logMessung("mvk_vk_vklocales_vk_grp_code_unq", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vklabels_vk_grp_locale_unq_1(controller, indices);
	    controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_1", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkannotations_vk_grp_name_unq(controller, indices);
	    controller.logMessung("mvk_vk_vkannotations_vk_grp_name_unq", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vklocale_req_1(controller, indices);
	    controller.logMessung("mvk_vk_vklocale_req_1", indices[0], startMesspunkt);

	}
	public void vordruckP__content(MainValidatorController controller)
	                                   throws ValidatorException {
	/* Aufruf der Regeln fuer Vordruck content für eine spezifische lfdNr.
	  */
		long[] indices = {0};
		long startMesspunkt = -1;
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkrole_req(controller, indices);
	    controller.logMessung("mvk_vk_vkrole_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkdocumentModel_req(controller, indices);
	    controller.logMessung("mvk_vk_vkdocumentModel_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkordered_req(controller, indices);
	    controller.logMessung("mvk_vk_vkordered_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vkunbounded_req(controller, indices);
	    controller.logMessung("mvk_vk_vkunbounded_req", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vklabels_vk_grp_locale_unq_0(controller, indices);
	    controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_0", indices[0], startMesspunkt);
	    startMesspunkt = controller.startMesspunkt();
	    checkObject.mvk_vk_vklocale_req_0(controller, indices);
	    controller.logMessung("mvk_vk_vklocale_req_0", indices[0], startMesspunkt);

	}


	private void validatePreliminaryRulesForField__code(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkcode_req(controller, indices);
		controller.logMessung("mvk_vk_vkcode_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vklocales_vk_grp_code_unq(controller, indices);
		controller.logMessung("mvk_vk_vklocales_vk_grp_code_unq", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__documentModel(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkdocumentModel_req(controller, indices);
		controller.logMessung("mvk_vk_vkdocumentModel_req", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__duplicatesAllowed(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkduplicatesAllowed_req(controller, indices);
		controller.logMessung("mvk_vk_vkduplicatesAllowed_req", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__id(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkid_req(controller, indices);
		controller.logMessung("mvk_vk_vkid_req", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__locale_1(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vklabels_vk_grp_locale_unq_0(controller, indices);
		controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vklocale_req_0(controller, indices);
		controller.logMessung("mvk_vk_vklocale_req_0", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__locale_2(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vklabels_vk_grp_locale_unq_1(controller, indices);
		controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vklocale_req_1(controller, indices);
		controller.logMessung("mvk_vk_vklocale_req_1", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__modelType_0(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkmodelType_req_0(controller, indices);
		controller.logMessung("mvk_vk_vkmodelType_req_0", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__modelType_1(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkmodelType_req_1(controller, indices);
		controller.logMessung("mvk_vk_vkmodelType_req_1", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__name(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkname_req(controller, indices);
		controller.logMessung("mvk_vk_vkname_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkannotations_vk_grp_name_unq(controller, indices);
		controller.logMessung("mvk_vk_vkannotations_vk_grp_name_unq", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__ordered(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkordered_req(controller, indices);
		controller.logMessung("mvk_vk_vkordered_req", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__reference(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkreference_req(controller, indices);
		controller.logMessung("mvk_vk_vkreference_req", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__role(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkrole_req(controller, indices);
		controller.logMessung("mvk_vk_vkrole_req", indices[0], startMesspunkt);
	}
	private void validatePreliminaryRulesForField__unbounded(MainValidatorController controller) throws ValidatorException {
		long[] indices = {0};
	    long startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		checkObject.mvk_vk_vkunbounded_req(controller, indices);
		controller.logMessung("mvk_vk_vkunbounded_req", indices[0], startMesspunkt);
	}

	private final Map<String, Consumer<MainValidatorController>> uniqueFieldNameToPreliminaryValidationFunc = Map.ofEntries(
	    Map.entry("code", this::validatePreliminaryRulesForField__code),
	    Map.entry("documentModel", this::validatePreliminaryRulesForField__documentModel),
	    Map.entry("duplicatesAllowed", this::validatePreliminaryRulesForField__duplicatesAllowed),
	    Map.entry("id", this::validatePreliminaryRulesForField__id),
	    Map.entry("locale_1", this::validatePreliminaryRulesForField__locale_1),
	    Map.entry("locale_2", this::validatePreliminaryRulesForField__locale_2),
	    Map.entry("modelType_0", this::validatePreliminaryRulesForField__modelType_0),
	    Map.entry("modelType_1", this::validatePreliminaryRulesForField__modelType_1),
	    Map.entry("name", this::validatePreliminaryRulesForField__name),
	    Map.entry("ordered", this::validatePreliminaryRulesForField__ordered),
	    Map.entry("reference", this::validatePreliminaryRulesForField__reference),
	    Map.entry("role", this::validatePreliminaryRulesForField__role),
	    Map.entry("unbounded", this::validatePreliminaryRulesForField__unbounded)
	);

	public void validatePreliminaryRulesForField(MainValidatorController controller, String uniqueFieldName) {
        var validationFunc = uniqueFieldNameToPreliminaryValidationFunc.get(uniqueFieldName);
        if (validationFunc != null) {
            validationFunc.accept(controller);
        }
	}

	public void validatePreliminaryRules(MainValidatorController controller) {
		vordruckP__global(controller);
		vordruckP__header(controller);
		vordruckP__content(controller);

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
				checker = new TVCheckAlle_relationshipmetamodel();
				break;
			case INFO:
                checker = new TVCheckInfo_relationshipmetamodel();
                break;
            case HINT:
				checker = new TVCheckHinweis_relationshipmetamodel();
				break;
			case ERROR: 
				checker = new TVCheckFehler_relationshipmetamodel();
				break;
			default: 
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}

		Collection<RtInternalIdentifier> felder = pruefungsFelderMap.values();
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			ITeilValidierungChecker preChecker = new TVCheckPreliminary_relationshipmetamodel();

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
		vordruck__header(controller);
		vordruck__content(controller);

	}

	private void alleFehlerRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Fehler-Regeln 
		vordruckF__header(controller);
		vordruckF__content(controller);

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


