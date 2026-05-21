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
public class Meta_Felder_contract {
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
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(5),
 			supportedLanguages,
 			new String[] {"Household", "Travel", "Health", "Liability"},
 			enumerationOberflaechenWerte.get(5),
 			enumerationCategoryValues.get(5),
 			""

 		)
 	);

 	metaFormate.put(6,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(7,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(8,
 		new FormatDefinitionDatum(
 			 "HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(9,
 		new FormatDefinitionDatum(
 			 "yyyy", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(10,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.JAHR_OPTIONAL //TeilbekanntArt
 		)
 	);

 	metaFormate.put(11,
 		new FormatDefinitionDatumBereich(
 			"yyyy-MM-dd", //DatumFormat
 			false, //Zusatzprüfung
 			"/",  //Bereichstrenner
 			Jahresinterpretation.Standard //für Formate TT.MM-TT.MM (bis/von ist Referenzjahr)
 		)
 	);

 	metaFormate.put(12,
 		new FormatDefinitionString(
 			-1, //minLaenge
 			99999, //maxLaenge
 			19, //minLaengePrintable
 			19, //maxLaengePrintable
 			"[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}", //muster
 			mehrsprachigeFehlertexte.get(12), //fehlertexte
 			false, //zeilenUmbruch
 			false, // noValueValidation
 			stringHintLists.get(12) // values
 		)
 	);

 	metaFormate.put(13,
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

 	metaFormate.put(14,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(14),
 			supportedLanguages,
 			new String[] {"dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"},
 			enumerationOberflaechenWerte.get(14),
 			enumerationCategoryValues.get(14),
 			""

 		)
 	);

 	metaFormate.put(15,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(15),
 			supportedLanguages,
 			new String[] {"trivial", "low", "medium", "high", "critical"},
 			enumerationOberflaechenWerte.get(15),
 			enumerationCategoryValues.get(15),
 			""

 		)
 	);

 	metaFormate.put(16,
 		new FormatDefinitionDatum(
 			 "dd.MM.yyyy", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(17,
 		new FormatDefinitionDatum(
 			 "yyyy-MM", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "ContractName", "/ContractRoot/ContractName", 1, false, false, metaFormate.get(0), "contractContractNameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Name").put(new Locale("de"), "Vertragsname").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "ContractDescription", "/ContractRoot/ContractDescription", 1, false, false, metaFormate.get(0), "contractContractDescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Description").put(new Locale("de"), "Vertragsbeschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "LengthOfContract", "/ContractRoot/LengthOfContract", 1, false, false, metaFormate.get(1), "contractLengthOfContractValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Length of contract").put(new Locale("de"), "Vertragsdauer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "ContractValue", "/ContractRoot/ContractValue", 1, false, false, metaFormate.get(2), "contractContractValueValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Contract Value").put(new Locale("de"), "Vertragswert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "Liability", "/ContractRoot/Liability", 1, false, false, metaFormate.get(2), "contractLiabilityValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Amount of liability accepted").put(new Locale("de"), "Höhe der übernommenen Haftung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "CostToCustomer", "/ContractRoot/CostToCustomer", 1, false, false, metaFormate.get(2), "contractCostToCustomerValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Cost to Customer").put(new Locale("de"), "Kosten für den Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "NoOfCoInsuredCustomers", "/ContractRoot/NoOfCoInsuredCustomers", 1, false, false, metaFormate.get(3), "contractNoOfCoInsuredCustomersValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Number of Co-Insured Customers").put(new Locale("de"), "Anzahl der mitversicherten Kunden").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "MaxDiscount", "/ContractRoot/MaxDiscount", 1, false, false, metaFormate.get(0), "contractMaxDiscountValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Highest Discount amongst Co-Insured Policy Holders").put(new Locale("de"), "Höchster Rabatt unter den mitversicherten Versicherungsnehmern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "CostPerCoInsured", "/ContractRoot/CostPerCoInsured", 1, false, false, metaFormate.get(2), "contractCostPerCoInsuredValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Cost per Co-Insured Policy Holder").put(new Locale("de"), "Kosten pro mitversichertem Versicherungsnehmer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "Valid", "/ContractRoot/Valid", 1, false, false, metaFormate.get(4), "contractValidValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Valid").put(new Locale("de"), "Gültig").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "Type", "/ContractRoot/Type", 1, false, false, metaFormate.get(5), "contractTypeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Insurance type").put(new Locale("de"), "Versicherungsart").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "ContractStartDate", "/ContractRoot/ContractDates/ContractStartDate", 1, false, false, metaFormate.get(6), "contractContractStartDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "ContractEndDate", "/ContractRoot/ContractDates/ContractEndDate", 1, false, false, metaFormate.get(7), "contractContractEndDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "ContractEndTime", "/ContractRoot/ContractDates/ContractEndTime", 1, false, false, metaFormate.get(8), "contractContractEndTimeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "ContractReviewDate", "/ContractRoot/ContractDates/ContractReviewDate", 1, false, false, metaFormate.get(9), "contractContractReviewDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "LastPremiumPaidDate", "/ContractRoot/ContractDates/LastPremiumPaidDate", 1, false, false, metaFormate.get(10), "contractLastPremiumPaidDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "CoveragePeriod", "/ContractRoot/ContractDates/CoveragePeriod", 1, false, false, metaFormate.get(11), "contractCoveragePeriodValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "ID_2", "/ContractRoot/ChangeLog/ID", 1, false, false, metaFormate.get(12), "contractID_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(Locale.US, "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "Number", "/ContractRoot/ChangeLog/Number", 1, false, false, metaFormate.get(13), "contractNumberValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Number").put(Locale.US, "Number").put(new Locale("de"), "Nummer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	felder.add(new Feld_t(19, "ChangeTimestamp", "/ContractRoot/ChangeLog/ChangeTimestamp", 1, false, false, metaFormate.get(7), "contractChangeTimestampValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Change Timestamp").put(Locale.US, "Change Timestamp").put(new Locale("de"), "Zeitstempel ändern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(19)));
 	felder.add(new Feld_t(20, "Description", "/ContractRoot/ChangeLog/Description", 1, false, false, metaFormate.get(0), "contractDescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Description").put(Locale.US, "Description").put(new Locale("de"), "Beschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(20)));
 	felder.add(new Feld_t(21, "User", "/ContractRoot/ChangeLog/User", 1, false, false, metaFormate.get(0), "contractUserValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "User").put(Locale.US, "User").put(new Locale("de"), "Benutzer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(21)));
 	felder.add(new Feld_t(22, "Status_2", "/ContractRoot/ChangeLog/Status", 1, false, false, metaFormate.get(14), "contractStatus_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(Locale.US, "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(22)));
 	felder.add(new Feld_t(23, "Priority_2", "/ContractRoot/ChangeLog/Priority", 1, false, false, metaFormate.get(15), "contractPriority_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(Locale.US, "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(23)));
 	felder.add(new Feld_t(24, "ID_0", "/ContractRoot/ChangeLog/Changes/ID", 1, false, false, metaFormate.get(12), "contractID_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(Locale.US, "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(24)));
 	felder.add(new Feld_t(25, "Title", "/ContractRoot/ChangeLog/Changes/Title", 1, false, false, metaFormate.get(0), "contractTitleValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Title").put(Locale.US, "Title").put(new Locale("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(25)));
 	felder.add(new Feld_t(26, "Details_0", "/ContractRoot/ChangeLog/Changes/Details", 1, false, false, metaFormate.get(0), "contractDetails_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Details").put(Locale.US, "Details").put(new Locale("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(26)));
 	felder.add(new Feld_t(27, "Priority_0", "/ContractRoot/ChangeLog/Changes/Priority", 1, false, false, metaFormate.get(15), "contractPriority_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(Locale.US, "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(27)));
 	felder.add(new Feld_t(28, "Status_0", "/ContractRoot/ChangeLog/Changes/Status", 1, false, false, metaFormate.get(14), "contractStatus_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(Locale.US, "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(28)));
 	felder.add(new Feld_t(29, "LastModified", "/ContractRoot/ChangeLog/Changes/LastModified", 1, false, false, metaFormate.get(16), "contractLastModifiedValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Last Modified").put(Locale.US, "Last Modified").put(new Locale("de"), "Zuletzt geändert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(29)));
 	felder.add(new Feld_t(30, "ID_1", "/ContractRoot/ChangeLog/Changes/SubChanges/ID", 1, false, false, metaFormate.get(12), "contractID_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "ID").put(Locale.US, "ID").put(new Locale("de"), "ID").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(30)));
 	felder.add(new Feld_t(31, "TItle", "/ContractRoot/ChangeLog/Changes/SubChanges/TItle", 1, false, false, metaFormate.get(0), "contractTItleValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "TItle").put(Locale.US, "TItle").put(new Locale("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(31)));
 	felder.add(new Feld_t(32, "Details_1", "/ContractRoot/ChangeLog/Changes/SubChanges/Details", 1, false, false, metaFormate.get(0), "contractDetails_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Details").put(Locale.US, "Details").put(new Locale("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(32)));
 	felder.add(new Feld_t(33, "Priority_1", "/ContractRoot/ChangeLog/Changes/SubChanges/Priority", 1, false, false, metaFormate.get(15), "contractPriority_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Priority").put(Locale.US, "Priority").put(new Locale("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(33)));
 	felder.add(new Feld_t(34, "Status_1", "/ContractRoot/ChangeLog/Changes/SubChanges/Status", 1, false, false, metaFormate.get(14), "contractStatus_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Status").put(Locale.US, "Status").put(new Locale("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(34)));
 	felder.add(new Feld_t(35, "CreatedAt", "/ContractRoot/ChangeLog/Changes/SubChanges/CreatedAt", 1, false, false, metaFormate.get(17), "contractCreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Last Modified").put(Locale.US, "Last Modified").put(new Locale("de"), "Zuletzt geändert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(35)));
 	felder.add(new Feld_t(36, "docRef", "/__meta/docRef", 1, false, false, metaFormate.get(0), "contractdocRefValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(36)));
 	felder.add(new Feld_t(37, "modelReference", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractmodelReferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(37)));
 	felder.add(new Feld_t(38, "modelVersion", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractmodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(38)));
 	felder.add(new Feld_t(39, "creator", "/__meta/creator", 1, false, false, metaFormate.get(0), "contractcreatorValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(39)));
 	felder.add(new Feld_t(40, "createdAt", "/__meta/createdAt", 1, false, false, metaFormate.get(6), "contractcreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(40)));
 	felder.add(new Feld_t(41, "modifier", "/__meta/modifier", 1, false, false, metaFormate.get(0), "contractmodifierValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(41)));
 	felder.add(new Feld_t(42, "modifiedAt", "/__meta/modifiedAt", 1, false, false, metaFormate.get(6), "contractmodifiedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(42)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(5, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(12, new HashMap<Locale, String>());
 	mehrsprachigeFehlertexte.get(12).put(new Locale("de"), "Muster wird nicht erfüllt");
 	mehrsprachigeFehlertexte.get(12).put(new Locale("en"), "Pattern is not met");
 	mehrsprachigeFehlertexte.get(12).put(Locale.US, "Pattern is not met");

 	mehrsprachigeFehlertexte.put(14, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(15, new HashMap<Locale, String>());

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	enumerationOberflaechenWerte.put(5, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(5).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "Haushaltsversicherung", "Reiseversicherung", "Krankenversicherung", "Haftpflichtversicherung" }, new String[] { "Household", "Travel", "Health", "Liability" }));
 	enumerationOberflaechenWerte.get(5).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "Household Insurance", "Travel Insurance", "Health Insurance", "Liability Insurance" }, new String[] { "Household", "Travel", "Health", "Liability" }));
 	enumerationOberflaechenWerte.get(5).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "Household Coverage", "Travel Coverage", "Health Coverage", "Liability Coverage" }, new String[] { "Household", "Travel", "Health", "Liability" }));

 	enumerationOberflaechenWerte.put(14, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(14).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "attrape", "entwurf", "uberpruft", "zuruckgesetzt", "genehmigt", "fertig", "bereitgestelt" }, new String[] { "dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed" }));
 	enumerationOberflaechenWerte.get(14).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "imitation", "blueprint", "examined", "returned", "accepted", "finished", "utilized" }, new String[] { "dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed" }));
 	enumerationOberflaechenWerte.get(14).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "imitation", "blueprint", "examined", "returned", "accepted", "finished", "utilized" }, new String[] { "dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed" }));

 	enumerationOberflaechenWerte.put(15, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(15).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "banal", "niedrig", "medium", "hoch", "kritisch" }, new String[] { "trivial", "low", "medium", "high", "critical" }));
 	enumerationOberflaechenWerte.get(15).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "negligible", "small", "intermediate", "large", "demanding" }, new String[] { "trivial", "low", "medium", "high", "critical" }));
 	enumerationOberflaechenWerte.get(15).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "lesser", "low", "medium", "high", "critical" }, new String[] { "trivial", "low", "medium", "high", "critical" }));

 	}

 	private void initEnumerationCategoryValues_0(){
 	enumerationCategoryValues.put(14, new MapBuilder<String, Map<String, String>>().put("dummy", new MapBuilder<String, String>().put("Status", "Status").build()).put("draft", new MapBuilder<String, String>().put("Status", "Status").build()).put("reviewed", new MapBuilder<String, String>().put("Status", "Status").build()).put("reverted", new MapBuilder<String, String>().put("Status", "Status").build()).put("approved", new MapBuilder<String, String>().put("Status", "Status").build()).put("done", new MapBuilder<String, String>().put("Status", "Status").build()).put("deployed", new MapBuilder<String, String>().put("Status", "Status").build()).build());

 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_contract() {
		initFeldFehlertexte_0();

		initEnumerationOberflaechenWerte_0();

		initEnumerationCategoryValues_0();

	    initFeldtypen_0();

		initFelder_0();

    }

}