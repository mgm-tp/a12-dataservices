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

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Vorprüfungen (z.B. Pflichtfeldprüfung).
 *  
 */
public class TVCheckPreliminary_relationshipmetamodel implements ITeilValidierungChecker {

	private R_relationshipmetamodel checkObject = R_relationshipmetamodel.getInstance();

	public TVCheckPreliminary_relationshipmetamodel() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		if(interneFeldNr<=21) { fuehreAus_0(controller, interneFeldNr, indices); }

	}

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [0,21]
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
			case 0: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkid_req(controller, indices);
				controller.logMessung("mvk_vk_vkid_req", indices[0], startMesspunkt);

			break; }

			case 1: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkmodelType_req_1(controller, indices);
				controller.logMessung("mvk_vk_vkmodelType_req_1", indices[0], startMesspunkt);

			break; }

			case 3: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkcode_req(controller, indices);
				controller.logMessung("mvk_vk_vkcode_req", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vklocales_vk_grp_code_unq(controller, indices);
				controller.logMessung("mvk_vk_vklocales_vk_grp_code_unq", indices[0], startMesspunkt);

			break; }

			case 4: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vklabels_vk_grp_locale_unq_1(controller, indices);
				controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_1", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vklocale_req_1(controller, indices);
				controller.logMessung("mvk_vk_vklocale_req_1", indices[0], startMesspunkt);

			break; }

			case 6: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkname_req(controller, indices);
				controller.logMessung("mvk_vk_vkname_req", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkannotations_vk_grp_name_unq(controller, indices);
				controller.logMessung("mvk_vk_vkannotations_vk_grp_name_unq", indices[0], startMesspunkt);

			break; }

			case 10: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkmodelType_req_0(controller, indices);
				controller.logMessung("mvk_vk_vkmodelType_req_0", indices[0], startMesspunkt);

			break; }

			case 11: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkreference_req(controller, indices);
				controller.logMessung("mvk_vk_vkreference_req", indices[0], startMesspunkt);

			break; }

			case 12: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vklabels_vk_grp_locale_unq_0(controller, indices);
				controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_0", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vklocale_req_0(controller, indices);
				controller.logMessung("mvk_vk_vklocale_req_0", indices[0], startMesspunkt);

			break; }

			case 15: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkduplicatesAllowed_req(controller, indices);
				controller.logMessung("mvk_vk_vkduplicatesAllowed_req", indices[0], startMesspunkt);

			break; }

			case 16: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkrole_req(controller, indices);
				controller.logMessung("mvk_vk_vkrole_req", indices[0], startMesspunkt);

			break; }

			case 19: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkdocumentModel_req(controller, indices);
				controller.logMessung("mvk_vk_vkdocumentModel_req", indices[0], startMesspunkt);

			break; }

			case 20: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkordered_req(controller, indices);
				controller.logMessung("mvk_vk_vkordered_req", indices[0], startMesspunkt);

			break; }

			case 21: {
				startMesspunkt = controller.startMesspunkt();
				checkObject.mvk_vk_vkunbounded_req(controller, indices);
				controller.logMessung("mvk_vk_vkunbounded_req", indices[0], startMesspunkt);

			break; }


		default:
			if(interneFeldNr<=21) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new IllegalArgumentException("Die interne Feldnummer "+interneFeldNr+" liegt nicht im Interval [0,21].");
			}
		}
	}

}
