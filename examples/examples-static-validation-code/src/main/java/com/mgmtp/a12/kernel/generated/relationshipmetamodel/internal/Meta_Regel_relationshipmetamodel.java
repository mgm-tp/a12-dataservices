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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Identifier_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Identifier_t.ReferenzTyp;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Regel_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtIdentifierTemplate;

/*
 * Enthält Metadaten über die Regeln.
 *
 */
public class  Meta_Regel_relationshipmetamodel {
	private ArrayList<Regel_t> regeln = new ArrayList<Regel_t>();

	public ArrayList<Regel_t> getRegeln(){
		return regeln;
	}

	private static final int ANZAHL_REGELN = 2;

	//speichert pro Regel die referenzierten Identifier
	private Identifier_t refIdentifier[][] = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
	private Identifier_t refAuslassungsIdentifier[][] = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
	private String refAuslassungsVordrucke[][] = new String[ANZAHL_REGELN][];

	//stores the referenced entities per rule (can contain groups)
	private RtIdentifierTemplate[][] refEntity = new RtIdentifierTemplate[ANZAHL_REGELN][];

	//stores the referenced entities per rule, which could cause omission errors (may contain groups)
	private RtIdentifierTemplate[][] refAuslassungsEntity = new RtIdentifierTemplate[ANZAHL_REGELN][];

	private List<Map<Locale, String>> mehrsprachigeFehlertexte = new ArrayList<Map<Locale, String>>();
	{
		for (int i = 0; i < ANZAHL_REGELN; i++) {
			mehrsprachigeFehlertexte.add(new HashMap<Locale, String>());
		}
	}

	private void initRegelRefs_0(){
	// Init Daten zur Regel '/header/mustHaveAtLeastOneLocale'
	refIdentifier[0] = new Identifier_t[]{
			new Identifier_t( 1, new long[]{1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/header/modelType"),
			new Identifier_t( 3, new long[]{1, IIdentifier.ALL_INDICES, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/header/locales/code")
	};

	refAuslassungsIdentifier[0]= new Identifier_t[]{
			new Identifier_t( 3, new long[]{1, IIdentifier.ALL_INDICES, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/header/locales/code")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule0RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("code").isField(true).usb(-2).idx(1).idx(1, 9999).idx(1).build();
	  	final RtIdentifierTemplate metaRule0RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("modelType_1").isField(true).usb(-2).idx(1).idx(1).build();

	    refEntity[0] = new RtIdentifierTemplate[]{
	    		metaRule0RefsrtIdCon1,
	    		metaRule0RefsrtIdCon2
	    };

	    refAuslassungsEntity[0]= new RtIdentifierTemplate[]{
	    		metaRule0RefsrtIdCon1
	    };
	}
	// Init Daten zur Regel '/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet'
	refIdentifier[1] = new Identifier_t[]{
			new Identifier_t( 21, new long[]{1, IIdentifier.ITERATION, 1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/content/entityCharacteristics/linkConstraints/multiplicity/unbounded"),
			new Identifier_t( 22, new long[]{1, IIdentifier.ITERATION, 1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit")
	};

	refAuslassungsIdentifier[1]= new Identifier_t[]{
			new Identifier_t( 21, new long[]{1, IIdentifier.ITERATION, 1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/content/entityCharacteristics/linkConstraints/multiplicity/unbounded"),
			new Identifier_t( 22, new long[]{1, IIdentifier.ITERATION, 1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule1RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("unbounded").isField(true).usb(-2).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
	  	final RtIdentifierTemplate metaRule1RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("upperLimit").isField(true).usb(-2).idx(1).idx(-1).idx(1).idx(1).idx(1).build();

	    refEntity[1] = new RtIdentifierTemplate[]{
	    		metaRule1RefsrtIdCon1,
	    		metaRule1RefsrtIdCon2
	    };

	    refAuslassungsEntity[1]= new RtIdentifierTemplate[]{
	    		metaRule1RefsrtIdCon1,
	    		metaRule1RefsrtIdCon2
	    };
	}
	}


	private void initRegelFehlertexte_0(){
	// Init Daten zur Regel '/header/mustHaveAtLeastOneLocale'
	mehrsprachigeFehlertexte.get(0).put(Locale.of("en"), "Locales must not be empty");

	// Init Daten zur Regel '/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet'
	mehrsprachigeFehlertexte.get(1).put(Locale.of("en"), "When not unbounded, upperLimit must have a value");

	}


	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	private void initRegeln_0(){
	regeln.add(new Regel_t("mustHaveAtLeastOneLocale", "/header/mustHaveAtLeastOneLocale", "Error rule_e366c", mehrsprachigeFehlertexte.get(0), "Fehler", refIdentifier[0], refAuslassungsIdentifier[0], refEntity[0], refAuslassungsEntity[0], null,"/header/modelType", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	regeln.add(new Regel_t("whenNotUnboundedUpperLimitShouldBeSet", "/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet", "Error rule_3ce1d", mehrsprachigeFehlertexte.get(1), "Fehler", refIdentifier[1], refAuslassungsIdentifier[1], refEntity[1], refAuslassungsEntity[1], null,"/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	}


  	public Meta_Regel_relationshipmetamodel() {
        initRegelRefs_0();

        initRegelFehlertexte_0();

        initRegeln_0();

	}
}