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
package com.mgmtp.a12.kernel.generated.businesspartner.internal;

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
public class Meta_Felder_businesspartner {
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
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(4,
 		new FormatDefinitionDatum(
 			 "yyyy-MM", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(5,
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

 	metaFormate.put(6,
 		new FormatDefinitionJaNein(
 			"true", //jaWert
 			"false" //neinWert


 		)
 	);

 	metaFormate.put(7,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(7),
 			supportedLanguages,
 			new String[] {"0%", "10%", "20%", "30%", "40%", "50%"},
 			enumerationOberflaechenWerte.get(7),
 			enumerationCategoryValues.get(7),
 			""

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
 			true, //nullErlaubt
 			true, //fuehrendeNullenErlaubt
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

 	metaFormate.put(10,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.JAHR_OPTIONAL //TeilbekanntArt
 		)
 	);

 	metaFormate.put(11,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(11),
 			supportedLanguages,
 			new String[] {"IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"},
 			enumerationOberflaechenWerte.get(11),
 			enumerationCategoryValues.get(11),
 			""

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
 			 "MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(14,
 		new FormatDefinitionJa(
 			"true"

 		)
 	);

 	metaFormate.put(15,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	metaFormate.put(16,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(16),
 			supportedLanguages,
 			new String[] {"100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"},
 			enumerationOberflaechenWerte.get(16),
 			enumerationCategoryValues.get(16),
 			""

 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "original_filename", "/BusinessPartnerRoot/Attachment/original_filename", 1, false, false, metaFormate.get(0), "businesspartneroriginal_filenameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).put("Annotation", new MapBuilder<String, Object>().put("enable_case_insensitive_search", "true").build()).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "internal_filename", "/BusinessPartnerRoot/Attachment/internal_filename", 1, false, true, metaFormate.get(0), "businesspartnerinternal_filenameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "content", "/BusinessPartnerRoot/Attachment/content", 1, false, false, metaFormate.get(1), "businesspartnercontentValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "attachment_id", "/BusinessPartnerRoot/Attachment/attachment_id", 1, false, false, metaFormate.get(0), "businesspartnerattachment_idValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "size", "/BusinessPartnerRoot/Attachment/size", 1, false, false, metaFormate.get(2), "businesspartnersizeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "mime_type", "/BusinessPartnerRoot/Attachment/mime_type", 1, false, true, metaFormate.get(0), "businesspartnermime_typeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "category", "/BusinessPartnerRoot/Attachment/category", 1, false, false, metaFormate.get(0), "businesspartnercategoryValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "description", "/BusinessPartnerRoot/Attachment/description", 1, false, false, metaFormate.get(0), "businesspartnerdescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "signingDateTime", "/BusinessPartnerRoot/Employment/signingDateTime", 1, false, false, metaFormate.get(3), "businesspartnersigningDateTimeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "employedSince", "/BusinessPartnerRoot/Employment/employedSince", 1, false, false, metaFormate.get(4), "businesspartneremployedSinceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "employedTill", "/BusinessPartnerRoot/Employment/employedTill", 1, false, false, metaFormate.get(4), "businesspartneremployedTillValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "income_0", "/BusinessPartnerRoot/Employment/income", 1, false, false, metaFormate.get(5), "businesspartnerincome_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Income").put(Locale.of("de"), "Einkommen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "tax", "/BusinessPartnerRoot/Employment/tax", 1, false, false, metaFormate.get(5), "businesspartnertaxValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "tax").put(Locale.of("de"), "Steuer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "role_0", "/BusinessPartnerRoot/Employment/role", 1, false, false, metaFormate.get(0), "businesspartnerrole_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Role").put(Locale.of("de"), "Rolle").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "currentlyEmployed", "/BusinessPartnerRoot/Employment/currentlyEmployed", 1, false, false, metaFormate.get(6), "businesspartnercurrentlyEmployedValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "Discount", "/BusinessPartnerRoot/Offer/Discount", 1, false, false, metaFormate.get(7), "businesspartnerDiscountValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Customer Discount").put(Locale.of("de"), "Kundenrabatt").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "ValidityPeriod", "/BusinessPartnerRoot/Offer/ValidityPeriod", 1, false, false, metaFormate.get(8), "businesspartnerValidityPeriodValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "accountNumber", "/BusinessPartnerRoot/accountNumber", 1, false, false, metaFormate.get(9), "businesspartneraccountNumberValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "birthday", "/BusinessPartnerRoot/birthday", 1, false, false, metaFormate.get(10), "businesspartnerbirthdayValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	felder.add(new Feld_t(19, "Name", "/BusinessPartnerRoot/Name", 1, true, false, metaFormate.get(0), "businesspartnerNameValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name").put(Locale.of("de"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(19)));
 	felder.add(new Feld_t(20, "Industry", "/BusinessPartnerRoot/Industry", 1, true, false, metaFormate.get(11), "businesspartnerIndustryValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Industry or business sector").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(20)));
 	felder.add(new Feld_t(21, "role_1", "/BusinessPartnerRoot/role", 1, false, false, metaFormate.get(0), "businesspartnerrole_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Role").put(Locale.of("de"), "Rolle").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(21)));
 	felder.add(new Feld_t(22, "income_1", "/BusinessPartnerRoot/income", 1, false, false, metaFormate.get(2), "businesspartnerincome_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Income").put(Locale.of("de"), "Einkommen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(22)));
 	felder.add(new Feld_t(23, "EndOfRelationship", "/BusinessPartnerRoot/EndOfRelationship", 1, false, false, metaFormate.get(3), "businesspartnerEndOfRelationshipValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "End of business relationship").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(23)));
 	felder.add(new Feld_t(24, "TimeOfContractSignature", "/BusinessPartnerRoot/TimeOfContractSignature", 1, false, false, metaFormate.get(12), "businesspartnerTimeOfContractSignatureValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Exact time of contract signature").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(24)));
 	felder.add(new Feld_t(25, "ApproximateOfferDate", "/BusinessPartnerRoot/ApproximateOfferDate", 1, false, false, metaFormate.get(13), "businesspartnerApproximateOfferDateValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(25)));
 	felder.add(new Feld_t(26, "PremiumPartner", "/BusinessPartnerRoot/PremiumPartner", 1, false, false, metaFormate.get(14), "businesspartnerPremiumPartnerValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(26)));
 	felder.add(new Feld_t(27, "StartOfRelationship", "/BusinessPartnerRoot/StartOfRelationship", 1, false, false, metaFormate.get(15), "businesspartnerStartOfRelationshipValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Start of business relationship").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(27)));
 	felder.add(new Feld_t(28, "CustomerDiscount", "/BusinessPartnerRoot/CustomerDiscount", 1, true, false, metaFormate.get(16), "businesspartnerCustomerDiscountValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Customer Discount").put(Locale.of("de"), "Kundenrabatt").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(28)));
 	felder.add(new Feld_t(29, "PersonOrEntity", "/BusinessPartnerRoot/PersonOrEntity", 1, false, false, metaFormate.get(0), "businesspartnerPersonOrEntityValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Natural Person or Legal Entity").put(Locale.of("de"), "Natürliche Person oder juristische Person").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(29)));
 	felder.add(new Feld_t(30, "Company", "/BusinessPartnerRoot/SubtypeGroup/Company", 1, false, false, metaFormate.get(0), "businesspartnerCompanyValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Company").put(Locale.of("de"), "Unternehmen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(30)));
 	felder.add(new Feld_t(31, "Notes", "/BusinessPartnerRoot/Notes", 1, false, false, metaFormate.get(0), "businesspartnerNotesValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Notes").put(Locale.of("de"), "Notizen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).put("Annotation", new MapBuilder<String, Object>().put("indexed", "false").build()).build(), Set.of(), false, null, null, errorTexts4Req.get(31)));
 	felder.add(new Feld_t(32, "docRef", "/__meta/docRef", 1, false, false, metaFormate.get(0), "businesspartnerdocRefValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Reference").put(Locale.of("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(32)));
 	felder.add(new Feld_t(33, "modelReference", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "businesspartnermodelReferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Document Model Reference").put(Locale.of("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(33)));
 	felder.add(new Feld_t(34, "modelVersion", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "businesspartnermodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").put(Locale.of("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(34)));
 	felder.add(new Feld_t(35, "creator", "/__meta/creator", 1, false, false, metaFormate.get(0), "businesspartnercreatorValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Creator").put(Locale.of("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(35)));
 	felder.add(new Feld_t(36, "createdAt", "/__meta/createdAt", 1, false, false, metaFormate.get(3), "businesspartnercreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Created At").put(Locale.of("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(36)));
 	felder.add(new Feld_t(37, "modifier", "/__meta/modifier", 1, false, false, metaFormate.get(0), "businesspartnermodifierValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modifier").put(Locale.of("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(37)));
 	felder.add(new Feld_t(38, "modifiedAt", "/__meta/modifiedAt", 1, false, false, metaFormate.get(3), "businesspartnermodifiedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Modified At").put(Locale.of("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(38)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(7, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(11, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(16, new HashMap<Locale, String>());

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	enumerationOberflaechenWerte.put(7, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(7).put(Locale.of("de"), MetaFeldUtil.createPairList(new String[] { "0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat" }, new String[] { "0%", "10%", "20%", "30%", "40%", "50%" }));
 	enumerationOberflaechenWerte.get(7).put(Locale.of("en"), MetaFeldUtil.createPairList(new String[] { "0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount" }, new String[] { "0%", "10%", "20%", "30%", "40%", "50%" }));
 	enumerationOberflaechenWerte.get(7).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "0%", "10%", "20%", "30%", "40%", "50%" }, new String[] { "0%", "10%", "20%", "30%", "40%", "50%" }));

 	enumerationOberflaechenWerte.put(11, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(11).put(Locale.of("de"), MetaFeldUtil.createPairList(new String[] { "Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));
 	enumerationOberflaechenWerte.get(11).put(Locale.of("en"), MetaFeldUtil.createPairList(new String[] { "Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));
 	enumerationOberflaechenWerte.get(11).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "Technology of Information", "Investment", "Bookkeeping", "HealthPrevention", "Liability", "Constitution", "Business" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));

 	enumerationOberflaechenWerte.put(16, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(16).put(Locale.of("de"), MetaFeldUtil.createPairList(new String[] { "0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));
 	enumerationOberflaechenWerte.get(16).put(Locale.of("en"), MetaFeldUtil.createPairList(new String[] { "0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));
 	enumerationOberflaechenWerte.get(16).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));

 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_businesspartner() {
		initFeldFehlertexte_0();

		initEnumerationOberflaechenWerte_0();

	    initFeldtypen_0();

		initFelder_0();

    }

}