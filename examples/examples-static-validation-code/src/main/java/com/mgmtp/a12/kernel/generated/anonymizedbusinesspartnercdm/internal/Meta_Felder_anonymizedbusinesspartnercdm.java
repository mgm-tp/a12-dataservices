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
public class Meta_Felder_anonymizedbusinesspartnercdm {
	private final ArrayList<Feld_t> felder = new ArrayList<Feld_t>();
	private final Map<Integer, Map<Locale, String>> errorTexts4Req = new HashMap<Integer, Map<Locale, String>>();

	private final Map<Integer, IFormatDefinition> metaFormate = new HashMap<>();

	private final Map<Integer, Map<Locale, String>> mehrsprachigeFehlertexte = new HashMap<Integer, Map<Locale, String>>();
	private final Map<Integer, Map<Locale, List<Pair<String, String>>>> enumerationOberflaechenWerte = new HashMap<Integer, Map<Locale, List<Pair<String, String>>>>();
	private final Map<Integer, Map<String, Map<String, String>>> enumerationCategoryValues = new HashMap<Integer, Map<String, Map<String, String>>> ();
    private final Map<Integer, Map<Locale, List<String>>> stringHintLists = new HashMap<Integer, Map<Locale, List<String>>>();

	private final List<Locale> supportedLanguages = Arrays.asList( new Locale("en"), Locale.US );

	public ArrayList<Feld_t> getFelder(){
		return felder;
	}

 	private void initFeldtypen_0(){
 	metaFormate.put(0,
 		new FormatDefinitionString(
 			-1, //minLaenge
 			99999, //maxLaenge
 			1, //minLaengePrintable
 			99999, //maxLaengePrintable
 			"", //muster
 			mehrsprachigeFehlertexte.get(0), //fehlertexte
 			false, //zeilenUmbruch
 			false, // noValueValidation
 			stringHintLists.get(0) // values
 		)
 	);

 	metaFormate.put(1,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(2,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(2),
 			supportedLanguages,
 			new String[] {"100%", "90%", "80%", "50%"},
 			enumerationOberflaechenWerte.get(2),
 			enumerationCategoryValues.get(2),
 			""

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
 	felder.add(new Feld_t(0, "Industry", "/BusinessPartnerRoot/Industry", 1, false, false, metaFormate.get(0), "anonymizedbusinesspartnercdmIndustryValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Industry or business sector").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "StartOfRelationship", "/BusinessPartnerRoot/StartOfRelationship", 1, false, false, metaFormate.get(1), "anonymizedbusinesspartnercdmStartOfRelationshipValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Start of business relationship").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "CustomerDiscount", "/BusinessPartnerRoot/CustomerDiscount", 1, false, false, metaFormate.get(2), "anonymizedbusinesspartnercdmCustomerDiscountValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Customer Discount").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "docRef", "/__meta/docRef", 1, false, false, metaFormate.get(0), "anonymizedbusinesspartnercdmdocRefValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "modelReference", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "anonymizedbusinesspartnercdmmodelReferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "modelVersion", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "anonymizedbusinesspartnercdmmodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "creator", "/__meta/creator", 1, false, false, metaFormate.get(0), "anonymizedbusinesspartnercdmcreatorValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "createdAt", "/__meta/createdAt", 1, false, false, metaFormate.get(3), "anonymizedbusinesspartnercdmcreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "modifier", "/__meta/modifier", 1, false, false, metaFormate.get(0), "anonymizedbusinesspartnercdmmodifierValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "modifiedAt", "/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "anonymizedbusinesspartnercdmmodifiedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(2, new HashMap<Locale, String>());

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	enumerationOberflaechenWerte.put(2, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(2).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "no discount", "10% discount", "20% discount", "50% discount" }, new String[] { "100%", "90%", "80%", "50%" }));
 	enumerationOberflaechenWerte.get(2).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "100%", "90%", "80%", "50%" }, new String[] { "100%", "90%", "80%", "50%" }));

 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_anonymizedbusinesspartnercdm() {
		initFeldFehlertexte_0();

		initEnumerationOberflaechenWerte_0();

	    initFeldtypen_0();

		initFelder_0();

    }

}