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
public class Meta_Felder_relationshipmetamodel {
	private final ArrayList<Feld_t> felder = new ArrayList<Feld_t>();
	private final Map<Integer, Map<Locale, String>> errorTexts4Req = new HashMap<Integer, Map<Locale, String>>();

	private final Map<Integer, IFormatDefinition> metaFormate = new HashMap<>();

	private final Map<Integer, Map<Locale, String>> mehrsprachigeFehlertexte = new HashMap<Integer, Map<Locale, String>>();
	private final Map<Integer, Map<Locale, List<Pair<String, String>>>> enumerationOberflaechenWerte = new HashMap<Integer, Map<Locale, List<Pair<String, String>>>>();
	private final Map<Integer, Map<String, Map<String, String>>> enumerationCategoryValues = new HashMap<Integer, Map<String, Map<String, String>>> ();
    private final Map<Integer, Map<Locale, List<String>>> stringHintLists = new HashMap<Integer, Map<Locale, List<String>>>();

	private final List<Locale> supportedLanguages = Arrays.asList( Locale.of("en") );

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
 			false, // noValueValidation
 			stringHintLists.get(1) // values
 		)
 	);

 	metaFormate.put(2,
 		new FormatDefinitionString(
 			1, //minLaenge
 			100, //maxLaenge
 			1, //minLaengePrintable
 			100, //maxLaengePrintable
 			"[_a-zA-Z][-_.a-zA-Z0-9]*", //muster
 			mehrsprachigeFehlertexte.get(2), //fehlertexte
 			false, //zeilenUmbruch
 			false, // noValueValidation
 			stringHintLists.get(2) // values
 		)
 	);

 	metaFormate.put(3,
 		new FormatDefinitionJaNein(
 			"true", //jaWert
 			"false" //neinWert


 		)
 	);

 	metaFormate.put(4,
 		new FormatDefinitionString(
 			1, //minLaenge
 			85, //maxLaenge
 			1, //minLaengePrintable
 			85, //maxLaengePrintable
 			"[_a-zA-Z][-_.a-zA-Z0-9]*", //muster
 			mehrsprachigeFehlertexte.get(4), //fehlertexte
 			false, //zeilenUmbruch
 			false, // noValueValidation
 			stringHintLists.get(4) // values
 		)
 	);

 	metaFormate.put(5,
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
 			"0", //MinWert
 			"" //MaxWert

 		)
 	);

 	}

 	private void initFelder_0(){
 	felder.add(new Feld_t(0, "id", "/header/id", 1, true, false, metaFormate.get(0), "relationshipmetamodelidValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(0)));
 	felder.add(new Feld_t(1, "modelType_1", "/header/modelType", 1, true, false, metaFormate.get(0), "relationshipmetamodelmodelType_1Validate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(1)));
 	felder.add(new Feld_t(2, "modelVersion", "/header/modelVersion", 1, false, false, metaFormate.get(0), "relationshipmetamodelmodelVersionValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Version").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(2)));
 	felder.add(new Feld_t(3, "code", "/header/locales/code", 1, false, true, metaFormate.get(0), "relationshipmetamodelcodeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Code").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", true).build(), Set.of(), false, null, null, errorTexts4Req.get(3)));
 	felder.add(new Feld_t(4, "locale_2", "/header/labels/locale", 1, false, true, metaFormate.get(0), "relationshipmetamodellocale_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Locale").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", true).build(), Set.of(), false, null, null, errorTexts4Req.get(4)));
 	felder.add(new Feld_t(5, "text_2", "/header/labels/text", 1, false, false, metaFormate.get(1), "relationshipmetamodeltext_2Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Text").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(5)));
 	felder.add(new Feld_t(6, "name", "/header/annotations/name", 1, false, true, metaFormate.get(0), "relationshipmetamodelnameValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Name").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", true).build(), Set.of(), false, null, null, errorTexts4Req.get(6)));
 	felder.add(new Feld_t(7, "value", "/header/annotations/value", 1, false, false, metaFormate.get(0), "relationshipmetamodelvalueValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Value").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(7)));
 	felder.add(new Feld_t(8, "alias", "/header/modelReferences/alias", 1, false, false, metaFormate.get(0), "relationshipmetamodelaliasValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Alias").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(8)));
 	felder.add(new Feld_t(9, "purpose", "/header/modelReferences/purpose", 1, false, false, metaFormate.get(0), "relationshipmetamodelpurposeValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Purpose").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(9)));
 	felder.add(new Feld_t(10, "modelType_0", "/header/modelReferences/modelType", 1, false, true, metaFormate.get(0), "relationshipmetamodelmodelType_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Model Type").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(10)));
 	felder.add(new Feld_t(11, "reference", "/header/modelReferences/reference", 1, false, true, metaFormate.get(0), "relationshipmetamodelreferenceValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Reference").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(11)));
 	felder.add(new Feld_t(12, "locale_1", "/content/labels/locale", 1, false, true, metaFormate.get(0), "relationshipmetamodellocale_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Locale").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", true).build(), Set.of(), false, null, null, errorTexts4Req.get(12)));
 	felder.add(new Feld_t(13, "text_1", "/content/labels/text", 1, false, false, metaFormate.get(1), "relationshipmetamodeltext_1Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Text").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(13)));
 	felder.add(new Feld_t(14, "linkDocumentModel", "/content/linkDocumentModel", 1, false, false, metaFormate.get(2), "relationshipmetamodellinkDocumentModelValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "DocumentModel for link document").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(14)));
 	felder.add(new Feld_t(15, "duplicatesAllowed", "/content/duplicatesAllowed", 1, true, false, metaFormate.get(3), "relationshipmetamodelduplicatesAllowedValidate", new Integer[] { 0}, new Integer[] { 0 }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Duplicates Allowed").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(15)));
 	felder.add(new Feld_t(16, "role", "/content/entityCharacteristics/role", 1, false, true, metaFormate.get(4), "relationshipmetamodelroleValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Role").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(16)));
 	felder.add(new Feld_t(17, "locale_0", "/content/entityCharacteristics/labels/locale", 1, false, false, metaFormate.get(0), "relationshipmetamodellocale_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Locale").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(17)));
 	felder.add(new Feld_t(18, "text_0", "/content/entityCharacteristics/labels/text", 1, false, false, metaFormate.get(0), "relationshipmetamodeltext_0Validate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Label").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(18)));
 	felder.add(new Feld_t(19, "documentModel", "/content/entityCharacteristics/documentModel", 1, false, true, metaFormate.get(2), "relationshipmetamodeldocumentModelValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "DocumentModel").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(19)));
 	felder.add(new Feld_t(20, "ordered", "/content/entityCharacteristics/ordered", 1, false, true, metaFormate.get(3), "relationshipmetamodelorderedValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Ordered").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(20)));
 	felder.add(new Feld_t(21, "unbounded", "/content/entityCharacteristics/linkConstraints/multiplicity/unbounded", 1, false, true, metaFormate.get(3), "relationshipmetamodelunboundedValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Unbounded").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", true).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(21)));
 	felder.add(new Feld_t(22, "upperLimit", "/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit", 1, false, false, metaFormate.get(5), "relationshipmetamodelupperLimitValidate", new Integer[] { }, new Integer[] {  }, new MapBuilder<String, Object>().put("DruckText", new MapBuilder<Locale, String>().put(Locale.of("en"), "Upper Limit").build()).put("GlobalesFeld", false).put("ExplicitlyMandatory", false).put("TransientField", false).put("IndexField", false).build(), Set.of(), false, null, null, errorTexts4Req.get(22)));
 	}

 	private void initFeldFehlertexte_0(){
 	mehrsprachigeFehlertexte.put(2, new HashMap<Locale, String>());
 	mehrsprachigeFehlertexte.get(2).put(Locale.of("en"), "model name must be string of length between 1 and 100 characters and matching the pattern /[_a-zA-Z][-_.a-zA-Z0-9]*/");

 	mehrsprachigeFehlertexte.put(4, new HashMap<Locale, String>());
 	mehrsprachigeFehlertexte.get(4).put(Locale.of("en"), "Use only letters, digits, hyphens, underscores and periods. Furthermore, the name may only start with a letter or underscore.");

 	}

 	private void initEnumerationOberflaechenWerte_0(){
 	}

 	private void initEnumerationCategoryValues_0(){
 	}

 	private void initStringHintLists_0(){
 	}


	public Meta_Felder_relationshipmetamodel() {
		initFeldFehlertexte_0();

	    initFeldtypen_0();

		initFelder_0();

    }

}