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
package com.mgmtp.a12.kernel.generated.contract.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition.FeldFormatEnumType;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.util.MetaFeldUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.DatumTeilbekanntArt;

import com.mgmtp.a12.utils.conversion.InterpretationOfYear;

/*
 * Enthält Metadaten über Felder.
 *
 */
public class Meta_Felder_contract {
	private final ArrayList<Feld_t> felder = new ArrayList<Feld_t>();
	private final Map<Integer, Map<Locale, String>> errorTexts4Req = new HashMap<Integer, Map<Locale, String>>();

	private final Map<Integer, IFormatDefinition> metaFormate = new HashMap<>();

	private final Map<Integer, Map<Locale, String>> mehrsprachigeFehlertexte = new HashMap<Integer, Map<Locale, String>>();
	private final Map<Integer, Map<Locale, List<Pair<String, String>>>> enumerationOberflaechenWerte = new HashMap<Integer, Map<Locale, List<Pair<String, String>>>>();
	private final Map<Integer, Map<String, Map<String, String>>> enumerationCategoryValues = new HashMap<Integer, Map<String, Map<String, String>>> ();
    private final Map<Integer, Map<Locale, List<String>>> stringHintLists = new HashMap<Integer, Map<Locale, List<String>>>();

	private final List<Locale> supportedLanguages = Arrays.asList( Locale.of("en"), Locale.US, Locale.of("de") );

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
 		new FormatDefinitionZahl(
 			true, //Vorzeichen
 			false, //nullErlaubt
 			false, //fuehrendeNullenErlaubt
 			-1, //Vorkomma
 			0, //Nachkomma
 			0, // NachkommaBis
 			-1, //minLaenge
 			1, //minLaengeDisp
 			-1, //maxLaenge
 			16, //maxLaengeDisp
 			false,
 			"1", //MinWert
 			"" //MaxWert

 		)
 	);

 	metaFormate.put(2,
 		new FormatDefinitionZahl(
 			true, //Vorzeichen
 			true, //nullErlaubt
 			false, //fuehrendeNullenErlaubt
 			13, //Vorkomma
 			2, //Nachkomma
 			2, // NachkommaBis
 			-1, //minLaenge
 			4, //minLaengeDisp
 			-1, //maxLaenge
 			17, //maxLaengeDisp
 			true,
 			"", //MinWert
 			"" //MaxWert

 		)
 	);

 	metaFormate.put(3,
 		new FormatDefinitionZahl(
 			true, //Vorzeichen
 			true, //nullErlaubt
 			false, //fuehrendeNullenErlaubt
 			-1, //Vorkomma
 			0, //Nachkomma
 			0, // NachkommaBis
 			-1, //minLaenge
 			1, //minLaengeDisp
 			-1, //maxLaenge
 			16, //maxLaengeDisp
 			false,
 			"", //MinWert
 			"" //MaxWert

 		)
 	);

 	metaFormate.put(4,
 		new FormatDefinitionJaNein(
 			"true", //jaWert
 			"false" //neinWert


 		)
 	);

 	metaFormate.put(5,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "ContractName", "/ContractRoot/ContractName", 1, false, false, metaFormate.get(0), "contractContractNameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name of Contract").put(Locale.of("de"), "Name des Vertrags").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "ContractDescription", "/ContractRoot/ContractDescription", 1, false, false, metaFormate.get(0), "contractContractDescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Contract Description").put(Locale.of("de"), "Vertragsbeschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "LengthOfContract", "/ContractRoot/LengthOfContract", 1, false, false, metaFormate.get(1), "contractLengthOfContractValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Length of contract").put(Locale.of("de"), "Vertragsdauer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "ContractValue", "/ContractRoot/ContractValue", 1, false, false, metaFormate.get(2), "contractContractValueValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Contract Value").put(Locale.of("de"), "Vertragswert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "Liability", "/ContractRoot/Liability", 1, false, false, metaFormate.get(2), "contractLiabilityValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Amount of liability accepted").put(Locale.of("de"), "Höhe der übernommenen Haftung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "CostToCustomer", "/ContractRoot/CostToCustomer", 1, false, false, metaFormate.get(2), "contractCostToCustomerValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Cost to Customer").put(Locale.of("de"), "Kosten für den Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "NoOfCoInsuredCustomers", "/ContractRoot/NoOfCoInsuredCustomers", 1, false, false, metaFormate.get(3), "contractNoOfCoInsuredCustomersValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Number of Co-Insured Customers").put(Locale.of("de"), "Anzahl der mitversicherten Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "MaxDiscount", "/ContractRoot/MaxDiscount", 1, false, false, metaFormate.get(0), "contractMaxDiscountValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Highest Discount amongst Co-Insured Policy Holders").put(Locale.of("de"), "Höchster Rabatt unter den mitversicherten Versicherungsnehmern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "CostPerCoInsured", "/ContractRoot/CostPerCoInsured", 1, false, false, metaFormate.get(2), "contractCostPerCoInsuredValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Cost per Co-Insured Policy Holder").put(Locale.of("de"), "Kosten pro mitversichertem Versicherungsnehmer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "Valid", "/ContractRoot/Valid", 1, false, false, metaFormate.get(4), "contractValidValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Valid").put(Locale.of("de"), "Gültig").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "Make", "/ContractRoot/Car/Make", 1, false, false, metaFormate.get(0), "contractMakeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Car make").put(Locale.of("de"), "Auto machen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "Model", "/ContractRoot/Car/Model", 1, false, false, metaFormate.get(0), "contractModelValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Car model").put(Locale.of("de"), "Auto Model").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "docRef", "/__meta/docRef", 1, false, false, metaFormate.get(0), "contractdocRefValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "modelReference", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractmodelReferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "modelVersion", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractmodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "creator", "/__meta/creator", 1, false, false, metaFormate.get(0), "contractcreatorValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "createdAt", "/__meta/createdAt", 1, false, false, metaFormate.get(5), "contractcreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "modifier", "/__meta/modifier", 1, false, false, metaFormate.get(0), "contractmodifierValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "modifiedAt", "/__meta/modifiedAt", 1, false, false, metaFormate.get(5), "contractmodifiedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_contract() {
	    initFeldtypen_0();

		initFelder_0();

    }

}