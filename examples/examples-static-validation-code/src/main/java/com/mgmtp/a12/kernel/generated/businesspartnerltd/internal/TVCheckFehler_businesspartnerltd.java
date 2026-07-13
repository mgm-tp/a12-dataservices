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
package com.mgmtp.a12.kernel.generated.businesspartnerltd.internal;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *  
 */
public class TVCheckFehler_businesspartnerltd implements ITeilValidierungChecker {

	private R_businesspartnerltd checkObject = R_businesspartnerltd.getInstance();

	public TVCheckFehler_businesspartnerltd() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		if(interneFeldNr<=14) { fuehreAus_0(controller, interneFeldNr, indices); }

	}

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [1,14]
	* liegt.
	* 
	* In der Methode werden allerdings nur interne Nummern von Feldern
	* betrachtet, die Aktionsfelder einer Methode sind. Es müssen somit nicht alle
	* Felder des Intervalls in der Switch-Anweisung berücksichtigt werden.
	* 
	* @param controller
	* @param interneFeldNr
	* @param indices
	*/
	private void fuehreAus_0(MainValidatorController controller, int interneFeldNr, long[] indices) {
		long startMesspunkt = -1;
		switch (interneFeldNr) {
			case 1: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_AttachmentInternalFilenameRequired(controller, indices);
				controller.logMessung("mvk_AttachmentInternalFilenameRequired", indices[0], startMesspunkt);

			break; }

			case 2: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_AttachmentIdOrContentFilled(controller, indices);
				controller.logMessung("mvk_AttachmentIdOrContentFilled", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_SizeOfContentFilled(controller, indices);
				controller.logMessung("mvk_SizeOfContentFilled", indices[0], startMesspunkt);

			break; }

			case 5: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_AttachmentMimeTypeRequired(controller, indices);
				controller.logMessung("mvk_AttachmentMimeTypeRequired", indices[0], startMesspunkt);

			break; }

			case 8: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_NameFilled(controller, indices);
				controller.logMessung("mvk_NameFilled", indices[0], startMesspunkt);

			break; }

			case 9: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_IndustryFilled(controller, indices);
				controller.logMessung("mvk_IndustryFilled", indices[0], startMesspunkt);

			break; }

			case 12: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_TaxComputation(controller, indices);
				controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);

			break; }

			case 13: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_CustomerDiscountFilled(controller, indices);
				controller.logMessung("mvk_CustomerDiscountFilled", indices[0], startMesspunkt);

			break; }

			case 14: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_EntityComp(controller, indices);
				controller.logMessung("mvk_EntityComp", indices[0], startMesspunkt);

			break; }


		default:
			if(interneFeldNr<=14) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new IllegalArgumentException("Die interne Feldnummer "+interneFeldNr+" liegt nicht im Interval [1,14].");
			}
		}
	}

}
