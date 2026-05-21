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
package com.mgmtp.a12.kernel.generated.recursivebusinesspartnercdm.internal;

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
public class Meta_Felder_recursivebusinesspartnercdm {
	private final ArrayList<Feld_t> felder = new ArrayList<Feld_t>();
	private final Map<Integer, Map<Locale, String>> errorTexts4Req = new HashMap<Integer, Map<Locale, String>>();

	private final Map<Integer, IFormatDefinition> metaFormate = new HashMap<>();

	private final Map<Integer, Map<Locale, String>> mehrsprachigeFehlertexte = new HashMap<Integer, Map<Locale, String>>();
	private final Map<Integer, Map<Locale, List<Pair<String, String>>>> enumerationOberflaechenWerte = new HashMap<Integer, Map<Locale, List<Pair<String, String>>>>();
	private final Map<Integer, Map<String, Map<String, String>>> enumerationCategoryValues = new HashMap<Integer, Map<String, Map<String, String>>> ();
    private final Map<Integer, Map<Locale, List<String>>> stringHintLists = new HashMap<Integer, Map<Locale, List<String>>>();

	private final List<Locale> supportedLanguages = Arrays.asList( new Locale("en"), new Locale("de") );

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
 		new FormatDefinitionString(
 			-1, //minLaenge
 			99999, //maxLaenge
 			1, //minLaengePrintable
 			99999, //maxLaengePrintable
 			"", //muster
 			mehrsprachigeFehlertexte.get(1), //fehlertexte
 			true, //zeilenUmbruch
 			true, // noValueValidation
 			stringHintLists.get(1) // values
 		)
 	);

 	metaFormate.put(2,
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

 	metaFormate.put(3,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(3),
 			supportedLanguages,
 			new String[] {"IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"},
 			enumerationOberflaechenWerte.get(3),
 			enumerationCategoryValues.get(3),
 			""

 		)
 	);

 	metaFormate.put(4,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(5,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(5),
 			supportedLanguages,
 			new String[] {"100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"},
 			enumerationOberflaechenWerte.get(5),
 			enumerationCategoryValues.get(5),
 			""

 		)
 	);

 	metaFormate.put(6,
 		new FormatDefinitionZahl(
 			true, //Vorzeichen
 			true, //nullErlaubt
 			false, //fuehrendeNullenErlaubt
 			-1, //Vorkomma
 			0, //Nachkomma
 			2, // NachkommaBis
 			-1, //minLaenge
 			1, //minLaengeDisp
 			-1, //maxLaenge
 			17, //maxLaengeDisp
 			false,
 			"", //MinWert
 			"" //MaxWert

 		)
 	);

 	metaFormate.put(7,
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

 	metaFormate.put(8,
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

 	metaFormate.put(9,
 		new FormatDefinitionJaNein(
 			"true", //jaWert
 			"false" //neinWert


 		)
 	);

 	metaFormate.put(10,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(10),
 			supportedLanguages,
 			new String[] {"Household", "Travel", "Health", "Liability"},
 			enumerationOberflaechenWerte.get(10),
 			enumerationCategoryValues.get(10),
 			""

 		)
 	);

 	metaFormate.put(11,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(12,
 		new FormatDefinitionDatum(
 			 "HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(13,
 		new FormatDefinitionDatum(
 			 "yyyy", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(14,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.JAHR_OPTIONAL //TeilbekanntArt
 		)
 	);

 	metaFormate.put(15,
 		new FormatDefinitionDatumBereich(
 			"yyyy-MM-dd", //DatumFormat
 			false, //Zusatzprüfung
 			"/",  //Bereichstrenner
 			Jahresinterpretation.Standard //für Formate TT.MM-TT.MM (bis/von ist Referenzjahr)
 		)
 	);

 	metaFormate.put(16,
 		new FormatDefinitionString(
 			-1, //minLaenge
 			99999, //maxLaenge
 			19, //minLaengePrintable
 			19, //maxLaengePrintable
 			"[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}", //muster
 			mehrsprachigeFehlertexte.get(16), //fehlertexte
 			false, //zeilenUmbruch
 			false, // noValueValidation
 			stringHintLists.get(16) // values
 		)
 	);

 	metaFormate.put(17,
 		new FormatDefinitionZahl(
 			true, //Vorzeichen
 			false, //nullErlaubt
 			false, //fuehrendeNullenErlaubt
 			15, //Vorkomma
 			0, //Nachkomma
 			0, // NachkommaBis
 			-1, //minLaenge
 			1, //minLaengeDisp
 			-1, //maxLaenge
 			16, //maxLaengeDisp
 			true,
 			"", //MinWert
 			"" //MaxWert

 		)
 	);

 	metaFormate.put(18,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(18),
 			supportedLanguages,
 			new String[] {"dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"},
 			enumerationOberflaechenWerte.get(18),
 			enumerationCategoryValues.get(18),
 			""

 		)
 	);

 	metaFormate.put(19,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(19),
 			supportedLanguages,
 			new String[] {"trivial", "low", "medium", "high", "critical"},
 			enumerationOberflaechenWerte.get(19),
 			enumerationCategoryValues.get(19),
 			""

 		)
 	);

 	metaFormate.put(20,
 		new FormatDefinitionDatum(
 			 "dd.MM.yyyy", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(21,
 		new FormatDefinitionDatum(
 			 "yyyy-MM", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "original_filename", "/BusinessPartnerRoot/Attachment/original_filename", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmoriginal_filenameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "internal_filename", "/BusinessPartnerRoot/Attachment/internal_filename", 1, false, true, metaFormate.get(0), "recursivebusinesspartnercdminternal_filenameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "content", "/BusinessPartnerRoot/Attachment/content", 1, false, false, metaFormate.get(1), "recursivebusinesspartnercdmcontentValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "attachment_id", "/BusinessPartnerRoot/Attachment/attachment_id", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmattachment_idValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "size", "/BusinessPartnerRoot/Attachment/size", 1, false, false, metaFormate.get(2), "recursivebusinesspartnercdmsizeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "mime_type", "/BusinessPartnerRoot/Attachment/mime_type", 1, false, true, metaFormate.get(0), "recursivebusinesspartnercdmmime_typeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "category", "/BusinessPartnerRoot/Attachment/category", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmcategoryValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "description", "/BusinessPartnerRoot/Attachment/description", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmdescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "Name", "/BusinessPartnerRoot/Name", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmNameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "Industry", "/BusinessPartnerRoot/Industry", 1, false, false, metaFormate.get(3), "recursivebusinesspartnercdmIndustryValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Industry or business sector").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "StartOfRelationship", "/BusinessPartnerRoot/StartOfRelationship", 1, false, false, metaFormate.get(4), "recursivebusinesspartnercdmStartOfRelationshipValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Start of business relationship").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "CustomerDiscount", "/BusinessPartnerRoot/CustomerDiscount", 1, false, false, metaFormate.get(5), "recursivebusinesspartnercdmCustomerDiscountValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Customer Discount").put(new Locale("de"), "Kundenrabatt").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "income", "/BusinessPartnerRoot/Employment/income", 1, false, false, metaFormate.get(6), "recursivebusinesspartnercdmincomeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Income").put(new Locale("de"), "Einkommen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "tax", "/BusinessPartnerRoot/Employment/tax", 1, false, false, metaFormate.get(6), "recursivebusinesspartnercdmtaxValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "tax").put(new Locale("de"), "Steuer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "PersonOrEntity", "/BusinessPartnerRoot/PersonOrEntity", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmPersonOrEntityValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Natural Person or Legal Entity").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "ContractName_1", "/ContractBusinessPartner/ContractRoot/ContractName", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmContractName_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Name").put(new Locale("de"), "Vertragsname").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "ContractDescription_1", "/ContractBusinessPartner/ContractRoot/ContractDescription", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmContractDescription_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Description").put(new Locale("de"), "Vertragsbeschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "LengthOfContract_1", "/ContractBusinessPartner/ContractRoot/LengthOfContract", 1, false, false, metaFormate.get(7), "recursivebusinesspartnercdmLengthOfContract_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Length of contract").put(new Locale("de"), "Vertragsdauer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "ContractValue_1", "/ContractBusinessPartner/ContractRoot/ContractValue", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmContractValue_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Value").put(new Locale("de"), "Vertragswert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	felder.add(new Feld_t(19, "Liability_1", "/ContractBusinessPartner/ContractRoot/Liability", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmLiability_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Amount of liability accepted").put(new Locale("de"), "Höhe der übernommenen Haftung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(19)));
 	felder.add(new Feld_t(20, "CostToCustomer_1", "/ContractBusinessPartner/ContractRoot/CostToCustomer", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmCostToCustomer_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Cost to Customer").put(new Locale("de"), "Kosten für den Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(20)));
 	felder.add(new Feld_t(21, "NoOfCoInsuredCustomers_1", "/ContractBusinessPartner/ContractRoot/NoOfCoInsuredCustomers", 1, false, false, metaFormate.get(2), "recursivebusinesspartnercdmNoOfCoInsuredCustomers_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Number of Co-Insured Customers").put(new Locale("de"), "Anzahl der mitversicherten Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(21)));
 	felder.add(new Feld_t(22, "MaxDiscount_1", "/ContractBusinessPartner/ContractRoot/MaxDiscount", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmMaxDiscount_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Highest Discount amongst Co-Insured Policy Holders").put(new Locale("de"), "Höchster Rabatt unter den mitversicherten Versicherungsnehmern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(22)));
 	felder.add(new Feld_t(23, "CostPerCoInsured_1", "/ContractBusinessPartner/ContractRoot/CostPerCoInsured", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmCostPerCoInsured_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Cost per Co-Insured Policy Holder").put(new Locale("de"), "Kosten pro mitversichertem Versicherungsnehmer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(23)));
 	felder.add(new Feld_t(24, "Valid_1", "/ContractBusinessPartner/ContractRoot/Valid", 1, false, false, metaFormate.get(9), "recursivebusinesspartnercdmValid_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Valid").put(new Locale("de"), "Gültig").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(24)));
 	felder.add(new Feld_t(25, "Type_1", "/ContractBusinessPartner/ContractRoot/Type", 1, false, false, metaFormate.get(10), "recursivebusinesspartnercdmType_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Insurance type").put(new Locale("de"), "Versicherungsart").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(25)));
 	felder.add(new Feld_t(26, "ContractStartDate_1", "/ContractBusinessPartner/ContractRoot/ContractDates/ContractStartDate", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmContractStartDate_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(26)));
 	felder.add(new Feld_t(27, "ContractEndDate_1", "/ContractBusinessPartner/ContractRoot/ContractDates/ContractEndDate", 1, false, false, metaFormate.get(4), "recursivebusinesspartnercdmContractEndDate_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(27)));
 	felder.add(new Feld_t(28, "ContractEndTime_1", "/ContractBusinessPartner/ContractRoot/ContractDates/ContractEndTime", 1, false, false, metaFormate.get(12), "recursivebusinesspartnercdmContractEndTime_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(28)));
 	felder.add(new Feld_t(29, "ContractReviewDate_1", "/ContractBusinessPartner/ContractRoot/ContractDates/ContractReviewDate", 1, false, false, metaFormate.get(13), "recursivebusinesspartnercdmContractReviewDate_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(29)));
 	felder.add(new Feld_t(30, "LastPremiumPaidDate_1", "/ContractBusinessPartner/ContractRoot/ContractDates/LastPremiumPaidDate", 1, false, false, metaFormate.get(14), "recursivebusinesspartnercdmLastPremiumPaidDate_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(30)));
 	felder.add(new Feld_t(31, "CoveragePeriod_1", "/ContractBusinessPartner/ContractRoot/ContractDates/CoveragePeriod", 1, false, false, metaFormate.get(15), "recursivebusinesspartnercdmCoveragePeriod_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(31)));
 	felder.add(new Feld_t(32, "ID_5", "/ContractBusinessPartner/ContractRoot/ChangeLog/ID", 1, false, false, metaFormate.get(16), "recursivebusinesspartnercdmID_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(32)));
 	felder.add(new Feld_t(33, "Number_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/Number", 1, false, false, metaFormate.get(17), "recursivebusinesspartnercdmNumber_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Number").put(new Locale("de"), "Nummer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(33)));
 	felder.add(new Feld_t(34, "ChangeTimestamp_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/ChangeTimestamp", 1, false, false, metaFormate.get(4), "recursivebusinesspartnercdmChangeTimestamp_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Change Timestamp").put(new Locale("de"), "Zeitstempel ändern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(34)));
 	felder.add(new Feld_t(35, "Description_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/Description", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmDescription_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Description").put(new Locale("de"), "Beschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(35)));
 	felder.add(new Feld_t(36, "User_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/User", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmUser_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "User").put(new Locale("de"), "Benutzer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(36)));
 	felder.add(new Feld_t(37, "Status_5", "/ContractBusinessPartner/ContractRoot/ChangeLog/Status", 1, false, false, metaFormate.get(18), "recursivebusinesspartnercdmStatus_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(37)));
 	felder.add(new Feld_t(38, "Priority_5", "/ContractBusinessPartner/ContractRoot/ChangeLog/Priority", 1, false, false, metaFormate.get(19), "recursivebusinesspartnercdmPriority_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(38)));
 	felder.add(new Feld_t(39, "ID_3", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/ID", 1, false, false, metaFormate.get(16), "recursivebusinesspartnercdmID_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(39)));
 	felder.add(new Feld_t(40, "Title_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Title", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmTitle_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Title").put(new Locale("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(40)));
 	felder.add(new Feld_t(41, "Details_2", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Details", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmDetails_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Details").put(new Locale("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(41)));
 	felder.add(new Feld_t(42, "Priority_3", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Priority", 1, false, false, metaFormate.get(19), "recursivebusinesspartnercdmPriority_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(42)));
 	felder.add(new Feld_t(43, "Status_3", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Status", 1, false, false, metaFormate.get(18), "recursivebusinesspartnercdmStatus_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(43)));
 	felder.add(new Feld_t(44, "LastModified_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/LastModified", 1, false, false, metaFormate.get(20), "recursivebusinesspartnercdmLastModified_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Last Modified").put(new Locale("de"), "Zuletzt geändert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(44)));
 	felder.add(new Feld_t(45, "ID_4", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/ID", 1, false, false, metaFormate.get(16), "recursivebusinesspartnercdmID_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(45)));
 	felder.add(new Feld_t(46, "TItle_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/TItle", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmTItle_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "TItle").put(new Locale("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(46)));
 	felder.add(new Feld_t(47, "Details_3", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/Details", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmDetails_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Details").put(new Locale("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(47)));
 	felder.add(new Feld_t(48, "Priority_4", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/Priority", 1, false, false, metaFormate.get(19), "recursivebusinesspartnercdmPriority_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(48)));
 	felder.add(new Feld_t(49, "Status_4", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/Status", 1, false, false, metaFormate.get(18), "recursivebusinesspartnercdmStatus_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(49)));
 	}
 	private void initFelder_1(){
 	felder.add(new Feld_t(50, "CreatedAt_1", "/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/CreatedAt", 1, false, false, metaFormate.get(21), "recursivebusinesspartnercdmCreatedAt_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Last Modified").put(new Locale("de"), "Zuletzt geändert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(50)));
 	felder.add(new Feld_t(51, "ContractName_0", "/ContractBusinessPartner/ContractAmendment/ContractName", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmContractName_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Name").put(new Locale("de"), "Vertragsname").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(51)));
 	felder.add(new Feld_t(52, "ContractDescription_0", "/ContractBusinessPartner/ContractAmendment/ContractDescription", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmContractDescription_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Description").put(new Locale("de"), "Vertragsbeschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(52)));
 	felder.add(new Feld_t(53, "LengthOfContract_0", "/ContractBusinessPartner/ContractAmendment/LengthOfContract", 1, false, false, metaFormate.get(7), "recursivebusinesspartnercdmLengthOfContract_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Length of contract").put(new Locale("de"), "Vertragsdauer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(53)));
 	felder.add(new Feld_t(54, "ContractValue_0", "/ContractBusinessPartner/ContractAmendment/ContractValue", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmContractValue_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Value").put(new Locale("de"), "Vertragswert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(54)));
 	felder.add(new Feld_t(55, "Liability_0", "/ContractBusinessPartner/ContractAmendment/Liability", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmLiability_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Amount of liability accepted").put(new Locale("de"), "Höhe der übernommenen Haftung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(55)));
 	felder.add(new Feld_t(56, "CostToCustomer_0", "/ContractBusinessPartner/ContractAmendment/CostToCustomer", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmCostToCustomer_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Cost to Customer").put(new Locale("de"), "Kosten für den Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(56)));
 	felder.add(new Feld_t(57, "NoOfCoInsuredCustomers_0", "/ContractBusinessPartner/ContractAmendment/NoOfCoInsuredCustomers", 1, false, false, metaFormate.get(2), "recursivebusinesspartnercdmNoOfCoInsuredCustomers_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Number of Co-Insured Customers").put(new Locale("de"), "Anzahl der mitversicherten Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(57)));
 	felder.add(new Feld_t(58, "MaxDiscount_0", "/ContractBusinessPartner/ContractAmendment/MaxDiscount", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmMaxDiscount_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Highest Discount amongst Co-Insured Policy Holders").put(new Locale("de"), "Höchster Rabatt unter den mitversicherten Versicherungsnehmern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(58)));
 	felder.add(new Feld_t(59, "CostPerCoInsured_0", "/ContractBusinessPartner/ContractAmendment/CostPerCoInsured", 1, false, false, metaFormate.get(8), "recursivebusinesspartnercdmCostPerCoInsured_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Cost per Co-Insured Policy Holder").put(new Locale("de"), "Kosten pro mitversichertem Versicherungsnehmer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(59)));
 	felder.add(new Feld_t(60, "Valid_0", "/ContractBusinessPartner/ContractAmendment/Valid", 1, false, false, metaFormate.get(9), "recursivebusinesspartnercdmValid_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Valid").put(new Locale("de"), "Gültig").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(60)));
 	felder.add(new Feld_t(61, "Type_0", "/ContractBusinessPartner/ContractAmendment/Type", 1, false, false, metaFormate.get(10), "recursivebusinesspartnercdmType_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Insurance type").put(new Locale("de"), "Versicherungsart").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(61)));
 	felder.add(new Feld_t(62, "ContractStartDate_0", "/ContractBusinessPartner/ContractAmendment/ContractDates/ContractStartDate", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmContractStartDate_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(62)));
 	felder.add(new Feld_t(63, "ContractEndDate_0", "/ContractBusinessPartner/ContractAmendment/ContractDates/ContractEndDate", 1, false, false, metaFormate.get(4), "recursivebusinesspartnercdmContractEndDate_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(63)));
 	felder.add(new Feld_t(64, "ContractEndTime_0", "/ContractBusinessPartner/ContractAmendment/ContractDates/ContractEndTime", 1, false, false, metaFormate.get(12), "recursivebusinesspartnercdmContractEndTime_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(64)));
 	felder.add(new Feld_t(65, "ContractReviewDate_0", "/ContractBusinessPartner/ContractAmendment/ContractDates/ContractReviewDate", 1, false, false, metaFormate.get(13), "recursivebusinesspartnercdmContractReviewDate_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(65)));
 	felder.add(new Feld_t(66, "LastPremiumPaidDate_0", "/ContractBusinessPartner/ContractAmendment/ContractDates/LastPremiumPaidDate", 1, false, false, metaFormate.get(14), "recursivebusinesspartnercdmLastPremiumPaidDate_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(66)));
 	felder.add(new Feld_t(67, "CoveragePeriod_0", "/ContractBusinessPartner/ContractAmendment/ContractDates/CoveragePeriod", 1, false, false, metaFormate.get(15), "recursivebusinesspartnercdmCoveragePeriod_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(67)));
 	felder.add(new Feld_t(68, "ID_2", "/ContractBusinessPartner/ContractAmendment/ChangeLog/ID", 1, false, false, metaFormate.get(16), "recursivebusinesspartnercdmID_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(68)));
 	felder.add(new Feld_t(69, "Number_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Number", 1, false, false, metaFormate.get(17), "recursivebusinesspartnercdmNumber_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Number").put(new Locale("de"), "Nummer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(69)));
 	felder.add(new Feld_t(70, "ChangeTimestamp_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/ChangeTimestamp", 1, false, false, metaFormate.get(4), "recursivebusinesspartnercdmChangeTimestamp_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Change Timestamp").put(new Locale("de"), "Zeitstempel ändern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(70)));
 	felder.add(new Feld_t(71, "Description_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Description", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmDescription_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Description").put(new Locale("de"), "Beschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(71)));
 	felder.add(new Feld_t(72, "User_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/User", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmUser_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "User").put(new Locale("de"), "Benutzer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(72)));
 	felder.add(new Feld_t(73, "Status_2", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Status", 1, false, false, metaFormate.get(18), "recursivebusinesspartnercdmStatus_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(73)));
 	felder.add(new Feld_t(74, "Priority_2", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Priority", 1, false, false, metaFormate.get(19), "recursivebusinesspartnercdmPriority_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(74)));
 	felder.add(new Feld_t(75, "ID_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/ID", 1, false, false, metaFormate.get(16), "recursivebusinesspartnercdmID_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(75)));
 	felder.add(new Feld_t(76, "Title_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Title", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmTitle_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Title").put(new Locale("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(76)));
 	felder.add(new Feld_t(77, "Details_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Details", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmDetails_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Details").put(new Locale("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(77)));
 	felder.add(new Feld_t(78, "Priority_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Priority", 1, false, false, metaFormate.get(19), "recursivebusinesspartnercdmPriority_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(78)));
 	felder.add(new Feld_t(79, "Status_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Status", 1, false, false, metaFormate.get(18), "recursivebusinesspartnercdmStatus_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(79)));
 	felder.add(new Feld_t(80, "LastModified_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/LastModified", 1, false, false, metaFormate.get(20), "recursivebusinesspartnercdmLastModified_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Last Modified").put(new Locale("de"), "Zuletzt geändert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(80)));
 	felder.add(new Feld_t(81, "ID_1", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/ID", 1, false, false, metaFormate.get(16), "recursivebusinesspartnercdmID_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(81)));
 	felder.add(new Feld_t(82, "TItle_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/TItle", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmTItle_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "TItle").put(new Locale("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(82)));
 	felder.add(new Feld_t(83, "Details_1", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/Details", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmDetails_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Details").put(new Locale("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(83)));
 	felder.add(new Feld_t(84, "Priority_1", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/Priority", 1, false, false, metaFormate.get(19), "recursivebusinesspartnercdmPriority_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(84)));
 	felder.add(new Feld_t(85, "Status_1", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/Status", 1, false, false, metaFormate.get(18), "recursivebusinesspartnercdmStatus_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(85)));
 	felder.add(new Feld_t(86, "CreatedAt_0", "/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/CreatedAt", 1, false, false, metaFormate.get(21), "recursivebusinesspartnercdmCreatedAt_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Last Modified").put(new Locale("de"), "Zuletzt geändert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(86)));
 	felder.add(new Feld_t(87, "docRef_0", "/ContractBusinessPartner/ContractAmendment/__meta/docRef", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmdocRef_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(87)));
 	felder.add(new Feld_t(88, "modelReference_0", "/ContractBusinessPartner/ContractAmendment/__meta/modelReference", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodelReference_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(88)));
 	felder.add(new Feld_t(89, "modelVersion_0", "/ContractBusinessPartner/ContractAmendment/__meta/modelVersion", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodelVersion_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(89)));
 	felder.add(new Feld_t(90, "creator_0", "/ContractBusinessPartner/ContractAmendment/__meta/creator", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmcreator_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(90)));
 	felder.add(new Feld_t(91, "createdAt_0", "/ContractBusinessPartner/ContractAmendment/__meta/createdAt", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmcreatedAt_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(91)));
 	felder.add(new Feld_t(92, "modifier_0", "/ContractBusinessPartner/ContractAmendment/__meta/modifier", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodifier_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(92)));
 	felder.add(new Feld_t(93, "modifiedAt_0", "/ContractBusinessPartner/ContractAmendment/__meta/modifiedAt", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmmodifiedAt_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(93)));
 	felder.add(new Feld_t(94, "docRef_1", "/ContractBusinessPartner/__meta/docRef", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmdocRef_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(94)));
 	felder.add(new Feld_t(95, "modelReference_1", "/ContractBusinessPartner/__meta/modelReference", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodelReference_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(95)));
 	felder.add(new Feld_t(96, "modelVersion_1", "/ContractBusinessPartner/__meta/modelVersion", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodelVersion_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(96)));
 	felder.add(new Feld_t(97, "creator_1", "/ContractBusinessPartner/__meta/creator", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmcreator_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(97)));
 	felder.add(new Feld_t(98, "createdAt_1", "/ContractBusinessPartner/__meta/createdAt", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmcreatedAt_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(98)));
 	felder.add(new Feld_t(99, "modifier_1", "/ContractBusinessPartner/__meta/modifier", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodifier_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(99)));
 	}
 	private void initFelder_2(){
 	felder.add(new Feld_t(100, "modifiedAt_1", "/ContractBusinessPartner/__meta/modifiedAt", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmmodifiedAt_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(100)));
 	felder.add(new Feld_t(101, "docRef_2", "/__meta/docRef", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmdocRef_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(101)));
 	felder.add(new Feld_t(102, "modelReference_2", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodelReference_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(102)));
 	felder.add(new Feld_t(103, "modelVersion_2", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodelVersion_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(103)));
 	felder.add(new Feld_t(104, "creator_2", "/__meta/creator", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmcreator_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(104)));
 	felder.add(new Feld_t(105, "createdAt_2", "/__meta/createdAt", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmcreatedAt_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(105)));
 	felder.add(new Feld_t(106, "modifier_2", "/__meta/modifier", 1, false, false, metaFormate.get(0), "recursivebusinesspartnercdmmodifier_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(106)));
 	felder.add(new Feld_t(107, "modifiedAt_2", "/__meta/modifiedAt", 1, false, false, metaFormate.get(11), "recursivebusinesspartnercdmmodifiedAt_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(107)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(3, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(5, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(10, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(16, new HashMap<Locale, String>());
 	mehrsprachigeFehlertexte.get(16).put(new Locale("de"), "Muster wird nicht erfüllt");
 	mehrsprachigeFehlertexte.get(16).put(new Locale("en"), "Pattern is not met");

 	mehrsprachigeFehlertexte.put(18, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(19, new HashMap<Locale, String>());

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	enumerationOberflaechenWerte.put(3, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(3).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));
 	enumerationOberflaechenWerte.get(3).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));

 	enumerationOberflaechenWerte.put(5, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(5).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));
 	enumerationOberflaechenWerte.get(5).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));

 	enumerationOberflaechenWerte.put(10, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(10).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "Haushaltsversicherung", "Reiseversicherung", "Krankenversicherung", "Haftpflichtversicherung" }, new String[] { "Household", "Travel", "Health", "Liability" }));
 	enumerationOberflaechenWerte.get(10).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "Household Insurance", "Travel Insurance", "Health Insurance", "Liability Insurance" }, new String[] { "Household", "Travel", "Health", "Liability" }));

 	enumerationOberflaechenWerte.put(18, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(18).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "attrape", "entwurf", "uberpruft", "zuruckgesetzt", "genehmigt", "fertig", "bereitgestelt" }, new String[] { "dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed" }));
 	enumerationOberflaechenWerte.get(18).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "imitation", "blueprint", "examined", "returned", "accepted", "finished", "utilized" }, new String[] { "dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed" }));

 	enumerationOberflaechenWerte.put(19, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(19).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "banal", "niedrig", "medium", "hoch", "kritisch" }, new String[] { "trivial", "low", "medium", "high", "critical" }));
 	enumerationOberflaechenWerte.get(19).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "negligible", "small", "intermediate", "large", "demanding" }, new String[] { "trivial", "low", "medium", "high", "critical" }));

 	}

 	private void initEnumerationCategoryValues_0(){
 	enumerationCategoryValues.put(18, new MapBuilder<String, Map<String, String>>().put("dummy", new MapBuilder<String, String>().put("Status", "Status").build()).put("draft", new MapBuilder<String, String>().put("Status", "Status").build()).put("reviewed", new MapBuilder<String, String>().put("Status", "Status").build()).put("reverted", new MapBuilder<String, String>().put("Status", "Status").build()).put("approved", new MapBuilder<String, String>().put("Status", "Status").build()).put("done", new MapBuilder<String, String>().put("Status", "Status").build()).put("deployed", new MapBuilder<String, String>().put("Status", "Status").build()).build());

 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_recursivebusinesspartnercdm() {
		initFeldFehlertexte_0();

		initEnumerationOberflaechenWerte_0();

		initEnumerationCategoryValues_0();

	    initFeldtypen_0();

		initFelder_0();
		initFelder_1();
		initFelder_2();

    }

}