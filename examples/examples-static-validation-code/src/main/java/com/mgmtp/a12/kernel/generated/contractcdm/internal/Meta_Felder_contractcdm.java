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
package com.mgmtp.a12.kernel.generated.contractcdm.internal;

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
public class Meta_Felder_contractcdm {
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

 	metaFormate.put(2,
 		new FormatDefinitionJaNein(
 			"true", //jaWert
 			"false" //neinWert


 		)
 	);

 	metaFormate.put(3,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
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
 		new FormatDefinitionDatum(
 			 "HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(6,
 		new FormatDefinitionDatum(
 			 "yyyy", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(7,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.JAHR_OPTIONAL //TeilbekanntArt
 		)
 	);

 	metaFormate.put(8,
 		new FormatDefinitionDatumBereich(
 			"yyyy-MM-dd", //DatumFormat
 			false, //Zusatzprüfung
 			"/",  //Bereichstrenner
 			InterpretationOfYear.STANDARD //für Formate TT.MM-TT.MM (bis/von ist Referenzjahr)
 		)
 	);

 	metaFormate.put(9,
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

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "ContractName", "/ContractRoot/ContractName", 1, false, false, metaFormate.get(0), "contractcdmContractNameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Contract Name").put(Locale.of("de"), "Vertragsname").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "ContractValue", "/ContractRoot/ContractValue", 1, false, false, metaFormate.get(1), "contractcdmContractValueValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Contract Value").put(Locale.of("de"), "Vertragswert").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "Valid", "/ContractRoot/Valid", 1, false, false, metaFormate.get(2), "contractcdmValidValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Valid").put(Locale.of("de"), "Gültig").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "Type", "/ContractRoot/Type", 1, false, false, metaFormate.get(0), "contractcdmTypeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Insurance type").put(Locale.of("de"), "Versicherungsart").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "ContractStartDate", "/ContractRoot/ContractDates/ContractStartDate", 1, false, false, metaFormate.get(3), "contractcdmContractStartDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "ContractEndDate", "/ContractRoot/ContractDates/ContractEndDate", 1, false, false, metaFormate.get(4), "contractcdmContractEndDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "ContractEndTime", "/ContractRoot/ContractDates/ContractEndTime", 1, false, false, metaFormate.get(5), "contractcdmContractEndTimeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "ContractReviewDate", "/ContractRoot/ContractDates/ContractReviewDate", 1, false, false, metaFormate.get(6), "contractcdmContractReviewDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "LastPremiumPaidDate", "/ContractRoot/ContractDates/LastPremiumPaidDate", 1, false, false, metaFormate.get(7), "contractcdmLastPremiumPaidDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "CoveragePeriod", "/ContractRoot/ContractDates/CoveragePeriod", 1, false, false, metaFormate.get(8), "contractcdmCoveragePeriodValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "Number", "/ContractRoot/ChangeLog/Number", 1, false, false, metaFormate.get(9), "contractcdmNumberValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Number").put(Locale.US, "Number").put(Locale.of("de"), "Nummer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "ChangeTimestamp", "/ContractRoot/ChangeLog/ChangeTimestamp", 1, false, false, metaFormate.get(4), "contractcdmChangeTimestampValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Change Timestamp").put(Locale.US, "Change Timestamp").put(Locale.of("de"), "Zeitstempel ändern").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "Description", "/ContractRoot/ChangeLog/Description", 1, false, false, metaFormate.get(0), "contractcdmDescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Description").put(Locale.US, "Description").put(Locale.of("de"), "Beschreibung").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "User", "/ContractRoot/ChangeLog/User", 1, false, false, metaFormate.get(0), "contractcdmUserValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "User").put(Locale.US, "User").put(Locale.of("de"), "Benutzer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "Status_1", "/ContractRoot/ChangeLog/Status", 1, false, false, metaFormate.get(0), "contractcdmStatus_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Status").put(Locale.US, "Status").put(Locale.of("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "Priority_1", "/ContractRoot/ChangeLog/Priority", 1, false, false, metaFormate.get(0), "contractcdmPriority_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Priority").put(Locale.US, "Priority").put(Locale.of("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "Title", "/ContractRoot/ChangeLog/Changes/Title", 1, false, false, metaFormate.get(0), "contractcdmTitleValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Title").put(Locale.US, "Title").put(Locale.of("de"), "Titel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "Details", "/ContractRoot/ChangeLog/Changes/Details", 1, false, false, metaFormate.get(0), "contractcdmDetailsValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Details").put(Locale.US, "Details").put(Locale.of("de"), "Details").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "Priority_0", "/ContractRoot/ChangeLog/Changes/Priority", 1, false, false, metaFormate.get(0), "contractcdmPriority_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Priority").put(Locale.US, "Priority").put(Locale.of("de"), "Priorität").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	felder.add(new Feld_t(19, "Status_0", "/ContractRoot/ChangeLog/Changes/Status", 1, false, false, metaFormate.get(0), "contractcdmStatus_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Status").put(Locale.US, "Status").put(Locale.of("de"), "Status").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(19)));
 	felder.add(new Feld_t(20, "Name_0", "/ContractBusinessPartner/BusinessPartnerRoot/Name", 1, false, false, metaFormate.get(0), "contractcdmName_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name").put(Locale.of("de"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(20)));
 	felder.add(new Feld_t(21, "Industry_0", "/ContractBusinessPartner/BusinessPartnerRoot/Industry", 1, false, false, metaFormate.get(0), "contractcdmIndustry_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Industry").put(Locale.of("de"), "Industrie").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(21)));
 	felder.add(new Feld_t(22, "Country_0", "/ContractBusinessPartner/PartnerAddresses/AddressRoot/Country", 1, false, false, metaFormate.get(0), "contractcdmCountry_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Country").put(Locale.of("de"), "Land").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(22)));
 	felder.add(new Feld_t(23, "AddressType_0", "/ContractBusinessPartner/PartnerAddresses/AddressRoot/AddressType", 1, false, false, metaFormate.get(0), "contractcdmAddressType_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Address type").put(Locale.of("de"), "Adresstyp").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(23)));
 	felder.add(new Feld_t(24, "docRef_0", "/ContractBusinessPartner/PartnerAddresses/__meta/docRef", 1, false, false, metaFormate.get(0), "contractcdmdocRef_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(24)));
 	felder.add(new Feld_t(25, "modelReference_0", "/ContractBusinessPartner/PartnerAddresses/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractcdmmodelReference_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(25)));
 	felder.add(new Feld_t(26, "modelVersion_0", "/ContractBusinessPartner/PartnerAddresses/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractcdmmodelVersion_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(26)));
 	felder.add(new Feld_t(27, "creator_0", "/ContractBusinessPartner/PartnerAddresses/__meta/creator", 1, false, false, metaFormate.get(0), "contractcdmcreator_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(27)));
 	felder.add(new Feld_t(28, "createdAt_0", "/ContractBusinessPartner/PartnerAddresses/__meta/createdAt", 1, false, false, metaFormate.get(3), "contractcdmcreatedAt_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(28)));
 	felder.add(new Feld_t(29, "modifier_0", "/ContractBusinessPartner/PartnerAddresses/__meta/modifier", 1, false, false, metaFormate.get(0), "contractcdmmodifier_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(29)));
 	felder.add(new Feld_t(30, "modifiedAt_0", "/ContractBusinessPartner/PartnerAddresses/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "contractcdmmodifiedAt_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(30)));
 	felder.add(new Feld_t(31, "Country_1", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Country", 1, false, false, metaFormate.get(0), "contractcdmCountry_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Country").put(Locale.of("de"), "Land").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(31)));
 	felder.add(new Feld_t(32, "AddressType_1", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/AddressType", 1, false, false, metaFormate.get(0), "contractcdmAddressType_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Address type").put(Locale.of("de"), "Adresstyp").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(32)));
 	felder.add(new Feld_t(33, "docRef_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/docRef", 1, false, false, metaFormate.get(0), "contractcdmdocRef_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(33)));
 	felder.add(new Feld_t(34, "modelReference_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractcdmmodelReference_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(34)));
 	felder.add(new Feld_t(35, "modelVersion_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractcdmmodelVersion_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(35)));
 	felder.add(new Feld_t(36, "creator_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/creator", 1, false, false, metaFormate.get(0), "contractcdmcreator_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(36)));
 	felder.add(new Feld_t(37, "createdAt_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/createdAt", 1, false, false, metaFormate.get(3), "contractcdmcreatedAt_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(37)));
 	felder.add(new Feld_t(38, "modifier_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modifier", 1, false, false, metaFormate.get(0), "contractcdmmodifier_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(38)));
 	felder.add(new Feld_t(39, "modifiedAt_1", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "contractcdmmodifiedAt_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(39)));
 	felder.add(new Feld_t(40, "docRef_2", "/ContractBusinessPartner/__meta/docRef", 1, false, false, metaFormate.get(0), "contractcdmdocRef_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(40)));
 	felder.add(new Feld_t(41, "modelReference_2", "/ContractBusinessPartner/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractcdmmodelReference_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(41)));
 	felder.add(new Feld_t(42, "modelVersion_2", "/ContractBusinessPartner/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractcdmmodelVersion_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(42)));
 	felder.add(new Feld_t(43, "creator_2", "/ContractBusinessPartner/__meta/creator", 1, false, false, metaFormate.get(0), "contractcdmcreator_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(43)));
 	felder.add(new Feld_t(44, "createdAt_2", "/ContractBusinessPartner/__meta/createdAt", 1, false, false, metaFormate.get(3), "contractcdmcreatedAt_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(44)));
 	felder.add(new Feld_t(45, "modifier_2", "/ContractBusinessPartner/__meta/modifier", 1, false, false, metaFormate.get(0), "contractcdmmodifier_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(45)));
 	felder.add(new Feld_t(46, "modifiedAt_2", "/ContractBusinessPartner/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "contractcdmmodifiedAt_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(46)));
 	felder.add(new Feld_t(47, "Name_1", "/ContractCoInsuredPartner/BusinessPartnerRoot/Name", 1, false, false, metaFormate.get(0), "contractcdmName_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(47)));
 	felder.add(new Feld_t(48, "Industry_1", "/ContractCoInsuredPartner/BusinessPartnerRoot/Industry", 1, false, false, metaFormate.get(0), "contractcdmIndustry_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Industry").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(48)));
 	felder.add(new Feld_t(49, "Name_2", "/ContractCoInsuredPartner/relationship/CoInsuredRoot/Name", 1, false, false, metaFormate.get(0), "contractcdmName_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name of Policy Holder").put(Locale.US, "Name of Policy Holder").put(Locale.of("de"), "Name des Versicherungsnehmers").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(49)));
 	}
 	private void initFelder_1(){
 	felder.add(new Feld_t(50, "docRef_4", "/ContractCoInsuredPartner/relationship/__meta/docRef", 1, false, false, metaFormate.get(0), "contractcdmdocRef_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(50)));
 	felder.add(new Feld_t(51, "modelReference_4", "/ContractCoInsuredPartner/relationship/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractcdmmodelReference_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(51)));
 	felder.add(new Feld_t(52, "modelVersion_4", "/ContractCoInsuredPartner/relationship/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractcdmmodelVersion_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(52)));
 	felder.add(new Feld_t(53, "creator_4", "/ContractCoInsuredPartner/relationship/__meta/creator", 1, false, false, metaFormate.get(0), "contractcdmcreator_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(53)));
 	felder.add(new Feld_t(54, "createdAt_4", "/ContractCoInsuredPartner/relationship/__meta/createdAt", 1, false, false, metaFormate.get(3), "contractcdmcreatedAt_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(54)));
 	felder.add(new Feld_t(55, "modifier_4", "/ContractCoInsuredPartner/relationship/__meta/modifier", 1, false, false, metaFormate.get(0), "contractcdmmodifier_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(55)));
 	felder.add(new Feld_t(56, "modifiedAt_4", "/ContractCoInsuredPartner/relationship/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "contractcdmmodifiedAt_4Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(56)));
 	felder.add(new Feld_t(57, "docRef_3", "/ContractCoInsuredPartner/__meta/docRef", 1, false, false, metaFormate.get(0), "contractcdmdocRef_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(57)));
 	felder.add(new Feld_t(58, "modelReference_3", "/ContractCoInsuredPartner/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractcdmmodelReference_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(58)));
 	felder.add(new Feld_t(59, "modelVersion_3", "/ContractCoInsuredPartner/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractcdmmodelVersion_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(59)));
 	felder.add(new Feld_t(60, "creator_3", "/ContractCoInsuredPartner/__meta/creator", 1, false, false, metaFormate.get(0), "contractcdmcreator_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(60)));
 	felder.add(new Feld_t(61, "createdAt_3", "/ContractCoInsuredPartner/__meta/createdAt", 1, false, false, metaFormate.get(3), "contractcdmcreatedAt_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(61)));
 	felder.add(new Feld_t(62, "modifier_3", "/ContractCoInsuredPartner/__meta/modifier", 1, false, false, metaFormate.get(0), "contractcdmmodifier_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(62)));
 	felder.add(new Feld_t(63, "modifiedAt_3", "/ContractCoInsuredPartner/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "contractcdmmodifiedAt_3Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(63)));
 	felder.add(new Feld_t(64, "docRef_5", "/__meta/docRef", 1, false, false, metaFormate.get(0), "contractcdmdocRef_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(64)));
 	felder.add(new Feld_t(65, "modelReference_5", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "contractcdmmodelReference_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(65)));
 	felder.add(new Feld_t(66, "modelVersion_5", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "contractcdmmodelVersion_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(66)));
 	felder.add(new Feld_t(67, "creator_5", "/__meta/creator", 1, false, false, metaFormate.get(0), "contractcdmcreator_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(67)));
 	felder.add(new Feld_t(68, "createdAt_5", "/__meta/createdAt", 1, false, false, metaFormate.get(3), "contractcdmcreatedAt_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(68)));
 	felder.add(new Feld_t(69, "modifier_5", "/__meta/modifier", 1, false, false, metaFormate.get(0), "contractcdmmodifier_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(69)));
 	felder.add(new Feld_t(70, "modifiedAt_5", "/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "contractcdmmodifiedAt_5Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(70)));
 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_contractcdm() {
	    initFeldtypen_0();

		initFelder_0();
		initFelder_1();

    }

}