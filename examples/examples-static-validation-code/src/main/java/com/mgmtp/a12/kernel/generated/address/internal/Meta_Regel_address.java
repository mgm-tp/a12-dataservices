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
public class  Meta_Regel_address {
	private ArrayList<Regel_t> regeln = new ArrayList<Regel_t>();

	public ArrayList<Regel_t> getRegeln(){
		return regeln;
	}

	private static final int ANZAHL_REGELN = 1;

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
	// Init Daten zur Regel '/AddressRoot/MustContainCountry'
	refIdentifier[0] = new Identifier_t[]{
			new Identifier_t( 6, new long[]{1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/AddressRoot/Country")
	};

	refAuslassungsIdentifier[0]= new Identifier_t[]{
			new Identifier_t( 6, new long[]{1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/AddressRoot/Country")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule0RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("Country").isField(true).usb(-2).idx(1).idx(1).build();
	  	final RtIdentifierTemplate metaRule0RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("/AddressRoot").isField(false).usb(-2).idx(1).build();

	    refEntity[0] = new RtIdentifierTemplate[]{
	    		metaRule0RefsrtIdCon1,
	    		metaRule0RefsrtIdCon2
	    };

	    refAuslassungsEntity[0]= new RtIdentifierTemplate[]{
	    		metaRule0RefsrtIdCon1
	    };
	}
	}


	private void initRegelFehlertexte_0(){
	// Init Daten zur Regel '/AddressRoot/MustContainCountry'
	mehrsprachigeFehlertexte.get(0).put(Locale.of("de"), "Kein Land angegeben");
	mehrsprachigeFehlertexte.get(0).put(Locale.of("en"), "No country provided");

	}


	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	private void initRegeln_0(){
	regeln.add(new Regel_t("MustContainCountry", "/AddressRoot/MustContainCountry", "Error rule_7c66e", mehrsprachigeFehlertexte.get(0), "Fehler", refIdentifier[0], refAuslassungsIdentifier[0], refEntity[0], refAuslassungsEntity[0], null,"/AddressRoot/Country", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	}


  	public Meta_Regel_address() {
        initRegelRefs_0();

        initRegelFehlertexte_0();

        initRegeln_0();

	}
}