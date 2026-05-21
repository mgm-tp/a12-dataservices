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
package com.mgmtp.a12.kernel.generated.address.internal;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *  
 */
public class TVCheckFehler_address implements ITeilValidierungChecker {

	private R_address checkObject = R_address.getInstance();

	public TVCheckFehler_address() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		if(interneFeldNr<=6) { fuehreAus_0(controller, interneFeldNr, indices); }

	}

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [6,6]
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
			case 6: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_MustContainCountry(controller, indices);
				controller.logMessung("mvk_MustContainCountry", indices[0], startMesspunkt);

			break; }


		default:
			if(interneFeldNr<=6) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new IllegalArgumentException("Die interne Feldnummer "+interneFeldNr+" liegt nicht im Interval [6,6].");
			}
		}
	}

}
