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
package com.mgmtp.a12.kernel.generated.businesspartnerltd.internal;

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
public class Meta_Felder_businesspartnerltd {
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
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(6),
 			supportedLanguages,
 			new String[] {"100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"},
 			enumerationOberflaechenWerte.get(6),
 			enumerationCategoryValues.get(6),
 			""

 		)
 	);

 	metaFormate.put(7,
 		new FormatDefinitionString(
 			mehrsprachigeFehlertexte.get(7),
 			supportedLanguages,
 			new String[] {"GmbH", "AG", "KG", "PLC", "LLC"},
 			enumerationOberflaechenWerte.get(7),
 			enumerationCategoryValues.get(7),
 			""

 		)
 	);

 	metaFormate.put(8,
 		new FormatDefinitionDatum(
 			 "yyyy-MM-dd'T'HH:mm:ss", //Datumsformat
 			 false, //Zusatzprüfung
 			 DatumTeilbekanntArt.BEKANNT //TeilbekanntArt
 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "original_filename", "/BusinessPartnerRoot/Attachment/original_filename", 1, false, false, metaFormate.get(0), "businesspartnerltdoriginal_filenameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "internal_filename", "/BusinessPartnerRoot/Attachment/internal_filename", 1, false, true, metaFormate.get(0), "businesspartnerltdinternal_filenameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "content", "/BusinessPartnerRoot/Attachment/content", 1, false, false, metaFormate.get(1), "businesspartnerltdcontentValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "attachment_id", "/BusinessPartnerRoot/Attachment/attachment_id", 1, false, false, metaFormate.get(0), "businesspartnerltdattachment_idValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "size", "/BusinessPartnerRoot/Attachment/size", 1, false, false, metaFormate.get(2), "businesspartnerltdsizeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "mime_type", "/BusinessPartnerRoot/Attachment/mime_type", 1, false, true, metaFormate.get(0), "businesspartnerltdmime_typeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "category", "/BusinessPartnerRoot/Attachment/category", 1, false, false, metaFormate.get(0), "businesspartnerltdcategoryValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "description", "/BusinessPartnerRoot/Attachment/description", 1, false, false, metaFormate.get(0), "businesspartnerltddescriptionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "Name", "/BusinessPartnerRoot/Name", 1, true, false, metaFormate.get(0), "businesspartnerltdNameValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Name").put(new Locale("de"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "Industry", "/BusinessPartnerRoot/Industry", 1, true, false, metaFormate.get(3), "businesspartnerltdIndustryValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Industry or business sector").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "StartOfRelationship", "/BusinessPartnerRoot/StartOfRelationship", 1, false, false, metaFormate.get(4), "businesspartnerltdStartOfRelationshipValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Start of business relationship").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "income", "/BusinessPartnerRoot/Employment/income", 1, false, false, metaFormate.get(5), "businesspartnerltdincomeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Income").put(new Locale("de"), "Einkommen").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "tax", "/BusinessPartnerRoot/Employment/tax", 1, false, false, metaFormate.get(5), "businesspartnerltdtaxValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "tax").put(new Locale("de"), "Steuer").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "CustomerDiscount", "/BusinessPartnerRoot/CustomerDiscount", 1, true, false, metaFormate.get(6), "businesspartnerltdCustomerDiscountValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Customer Discount").put(new Locale("de"), "Kundenrabatt").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "PersonOrEntity", "/BusinessPartnerRoot/PersonOrEntity", 1, false, false, metaFormate.get(0), "businesspartnerltdPersonOrEntityValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Natural Person or Legal Entity").put(new Locale("de"), "Natürliche Person oder juristische Person").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "Type", "/BusinessPartnerRoot/SubtypeGroup/Type", 1, false, false, metaFormate.get(7), "businesspartnerltdTypeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Type of Legal Entity").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "docRef", "/__meta/docRef", 1, false, false, metaFormate.get(0), "businesspartnerltddocRefValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Reference").put(new Locale("de"), "Dokumentreferenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "modelReference", "/__meta/modelReference", 1, false, false, metaFormate.get(0), "businesspartnerltdmodelReferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Document Model Reference").put(new Locale("de"), "Document Model-Referenz").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "modelVersion", "/__meta/modelVersion", 1, false, false, metaFormate.get(0), "businesspartnerltdmodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Model Version").put(new Locale("de"), "Model-Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	felder.add(new Feld_t(19, "creator", "/__meta/creator", 1, false, false, metaFormate.get(0), "businesspartnerltdcreatorValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Creator").put(new Locale("de"), "Erstellt von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(19)));
 	felder.add(new Feld_t(20, "createdAt", "/__meta/createdAt", 1, false, false, metaFormate.get(8), "businesspartnerltdcreatedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Created At").put(new Locale("de"), "Erstellt am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(20)));
 	felder.add(new Feld_t(21, "modifier", "/__meta/modifier", 1, false, false, metaFormate.get(0), "businesspartnerltdmodifierValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modifier").put(new Locale("de"), "Bearbeitet von").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(21)));
 	felder.add(new Feld_t(22, "modifiedAt", "/__meta/modifiedAt", 1, false, false, metaFormate.get(8), "businesspartnerltdmodifiedAtValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(new Locale("en"), "Modified At").put(new Locale("de"), "Bearbeitet am").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(22)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(3, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(6, new HashMap<Locale, String>());

 	mehrsprachigeFehlertexte.put(7, new HashMap<Locale, String>());

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	enumerationOberflaechenWerte.put(3, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(3).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));
 	enumerationOberflaechenWerte.get(3).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));
 	enumerationOberflaechenWerte.get(3).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "Technology of Information", "Investment", "Bookkeeping", "HealthPrevention", "Liability", "Constitution", "Business" }, new String[] { "IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce" }));

 	enumerationOberflaechenWerte.put(6, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(6).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));
 	enumerationOberflaechenWerte.get(6).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));
 	enumerationOberflaechenWerte.get(6).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }, new String[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%" }));

 	enumerationOberflaechenWerte.put(7, new HashMap<Locale, List<Pair<String, String>>>());
 	enumerationOberflaechenWerte.get(7).put(new Locale("de"), MetaFeldUtil.createPairList(new String[] { "GmbH", "AG", "KG", "PLC", "LLC" }, new String[] { "GmbH", "AG", "KG", "PLC", "LLC" }));
 	enumerationOberflaechenWerte.get(7).put(new Locale("en"), MetaFeldUtil.createPairList(new String[] { "GmbH", "AG", "KG", "PLC", "LLC" }, new String[] { "GmbH", "AG", "KG", "PLC", "LLC" }));
 	enumerationOberflaechenWerte.get(7).put(Locale.US, MetaFeldUtil.createPairList(new String[] { "GmbH", "AG", "KG", "PLC", "LLC" }, new String[] { "GmbH", "AG", "KG", "PLC", "LLC" }));

 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_businesspartnerltd() {
		initFeldFehlertexte_0();

		initEnumerationOberflaechenWerte_0();

	    initFeldtypen_0();

		initFelder_0();

    }

}