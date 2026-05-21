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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.FormatDefinitionDatumBereich.Jahresinterpretation;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.IFormatDefinition.FeldFormatEnumType;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MetaFeldUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatumTeilbekanntArt;

/*
 * Enthält Metadaten über Felder.
 *
 */
public class Meta_Felder_address {
	private final ArrayList<Feld_t> felder = new ArrayList<Feld_t>();
	private final Map<Integer, Map<Locale, String>> errorTexts4Req = new HashMap<Integer, Map<Locale, String>>();

	private final Map<Integer, IFormatDefinition> metaFormate = new HashMap<>();

	private final Map<Integer, Map<Locale, String>> mehrsprachigeFehlertexte = new HashMap<Integer, Map<Locale, String>>();
	private final Map<Integer, Map<Locale, List<Pair<String, String>>>> enumerationOberflaechenWerte = new HashMap<Integer, Map<Locale, List<Pair<String, String>>>>();
	private final Map<Integer, Map<String, Map<String, String>>> enumerationCategoryValues = new HashMap<Integer, Map<String, Map<String, String>>> ();
    private final Map<Integer, Map<Locale, List<String>>> stringHintLists = new HashMap<Integer, Map<Locale, List<String>>>();

	private final List<Locale> supportedLanguages = Arrays.asList( new Locale("en"), Locale.US, new Locale("de") );

	public ArrayList<Feld_t> getFelder(){
		return felder;
	}

 	private void initFeldtypen_0(){
 	metaFormate.put(0,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(0),
 			supportedLanguages,
 			new String[] {"Residential", "Commercial", "Other"},
 			enumerationOberflaechenWerte.get(0),
 			enumerationCategoryValues.get(0),
 			""

 		)
 	);

 	metaFormate.put(1,
 		new FormatDefinitionString(
 			-1, //minLaenge
 			99999, //maxLaenge
 			1, //minLaengePrintable
 			99999, //maxLaengePrintable
 			"", //muster
 			mehrsprachigeFehlertexte.get(1), //fehlertexte
 			false, //zeilenUmbruch
 			false, // noValueValidation
 			stringHintLists.get(1) // values
 		)
 	);

 	metaFormate.put(2,
 		new FormatDefinitionJa(
 			"true"

 		)
 	);

 	metaFormate.put(3,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "AddressType", "/AddressRoot/AddressType", 1, false, false, metaFormate.get(0), "addressAddressTypeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Address type").put(new Locale("de"), "Adresstyp").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "Location", "/AddressRoot/Location", 1, false, false, metaFormate.get(1), "addressLocationValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Location").put(new Locale("de"), "Standort").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "Street", "/AddressRoot/Street", 1, false, false, metaFormate.get(1), "addressStreetValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Street").put(new Locale("de"), "Straßenadresse").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "HouseNumber", "/AddressRoot/HouseNumber", 1, false, false, metaFormate.get(1), "addressHouseNumberValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "House number").put(new Locale("de"), "Hausnummer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "City", "/AddressRoot/City", 1, false, false, metaFormate.get(1), "addressCityValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "City").put(new Locale("de"), "Stadt").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "PostCode", "/AddressRoot/PostCode", 1, false, false, metaFormate.get(1), "addressPostCodeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Post Code").put(new Locale("de"), "Postleitzahl").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "Country", "/AddressRoot/Country", 1, false, false, metaFormate.get(1), "addressCountryValidate", new Integer[] { 0}, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Country").put(new Locale("de"), "Land").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "PostalAddress", "/AddressRoot/PostalAddress", 1, false, false, metaFormate.get(2), "addressPostalAddressValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "This is the postal address").put(new Locale("de"), "Dies ist die Postanschrift").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "docRef", "/__meta/docRef", 1, false, false, metaFormate.get(1), "addressdocRefValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "modelReference", "/__meta/modelReference", 1, false, false, metaFormate.get(1), "addressmodelReferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "modelVersion", "/__meta/modelVersion", 1, false, false, metaFormate.get(1), "addressmodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "creator", "/__meta/creator", 1, false, false, metaFormate.get(1), "addresscreatorValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "createdAt", "/__meta/createdAt", 1, false, false, metaFormate.get(3), "addresscreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "modifier", "/__meta/modifier", 1, false, false, metaFormate.get(1), "addressmodifierValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "modifiedAt", "/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "addressmodifiedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(0, new HashMap<Locale, String>());

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	enumerationOberflaechenWerte.put(0, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(0).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "Wohnen", "Kommerziell", "Andere" }, new String[] { "Residential", "Commercial", "Other" }));
 	enumerationOberflaechenWerte.get(0).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "Residential", "Commercial", "Other" }, new String[] { "Residential", "Commercial", "Other" }));
 	enumerationOberflaechenWerte.get(0).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "Urban", "Retail", "Another" }, new String[] { "Residential", "Commercial", "Other" }));

 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_address() {
		initFeldFehlertexte_0();

		initEnumerationOberflaechenWerte_0();

	    initFeldtypen_0();

		initFelder_0();

    }

}