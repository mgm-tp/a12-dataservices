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
var Pair = ValidationRuntimeApi.Pair;
var MapBuilder = ValidationRuntime.MapBuilder;
var Feld_t = ValidationRuntime.Feld_t;
var IFormatDefinition = ValidationRuntime.IFormatDefinition;
var FeldFormatEnumType = ValidationRuntime.FeldFormatEnumType;
var FormatDefinitionString = ValidationRuntime.FormatDefinitionString;
var FormatDefinitionJa = ValidationRuntime.FormatDefinitionJa;
var FormatDefinitionJaNein = ValidationRuntime.FormatDefinitionJaNein;
var FormatDefinitionZahl = ValidationRuntime.FormatDefinitionZahl;
var FormatDefinitionDatum = ValidationRuntime.FormatDefinitionDatum;
var FormatDefinitionDatumBereich = ValidationRuntime.FormatDefinitionDatumBereich;
var Jahresinterpretation = ValidationRuntime.Jahresinterpretation;
var FormatDefinitionVordefiniert = ValidationRuntime.FormatDefinitionVordefiniert;
var DatumTeilbekanntArt = ValidationRuntime.DatumTeilbekanntArt;
var MetaFeldUtil = ValidationRuntime.MetaFeldUtil;
var IIdentifier = ValidationRuntimeApi.IIdentifier;
var Regel_t = ValidationRuntime.Regel_t;
var Identifier_t = ValidationRuntime.Identifier_t;
var ReferenzTyp = ValidationRuntime.ReferenzTyp;
var MetaLokalePflichtInfo = ValidationRuntime.MetaLokalePflichtInfo;
var MetaModelImpl = ValidationRuntime.MetaModelImpl;
var IMetaKeysInternal = ValidationRuntime.IMetaKeysInternal;
var Constants = ValidationRuntime.Constants;
var Container_t = ValidationRuntime.Container_t;
var MetaIdentifier = ValidationRuntime.MetaIdentifier;
var MetaDataHelper = ValidationRuntime.MetaDataHelper;
var Model_t = ValidationRuntime.Model_t;
var HashSet = ValidationRuntimeApi.HashSet;
var TrieNode = ValidationRuntime.TrieNode;var RuntimeController = ValidationRuntime.RuntimeController;
var ArraySet = ValidationRuntimeApi.ArraySet;
var RuleType = ValidationRuntimeApi.RuleType;
var IResult = ValidationRuntimeApi.IResult;
var ErrorType = ValidationRuntimeApi.ErrorType;
var RuntimeFormalErrorEnum = ValidationRuntimeApiPublic.RuntimeFormalErrorEnum;
var MainValidatorController = ValidationRuntime.MainValidatorController;
var PruefErgebnisTyp = ValidationRuntime.PruefErgebnisTyp;
var RtIdentifierTemplate = ValidationRuntime.RtIdentifierTemplate;
var RtInternalIdentifier = ValidationRuntime.RtInternalIdentifier;
var EbenenIterator = ValidationRuntime.EbenenIterator;
var FilterPredicate = ValidationRuntime.FilterPredicate;
var ArrayUtils = ValidationRuntime.ArrayUtils;
var BedingungsOperatorHelper = ValidationRuntime.BedingungsOperatorHelper;
var VglOp = ValidationRuntime.VglOp;
var IFormaleFehlerConstants = ValidationRuntime.IFormaleFehlerConstants;
var ListBuilder = ValidationRuntime.ListBuilder;
var VkString = ValidationRuntime.VkString;
var RepetitionNotUniqueErrorCache = ValidationRuntime.RepetitionNotUniqueErrorCache;
var ValidationCommand = ValidationRuntime.ValidationCommand;
var CalculationCommand = ValidationRuntime.CalculationCommand;
var IterationState = ValidationRuntime.IterationState;
var ValidatorException = ValidationRuntimeApi.ValidatorException;
var CustomConditionException = ValidationRuntimeApi.CustomConditionException;
var Meta_Felder_relationshipmetamodel = (function () {
	function Meta_Felder_relationshipmetamodel() {
		this._felder = new Array();
		this._metaFormate = new Map();
		this._mehrsprachigeFehlertexte = new Map();
		this._errorTexts4Req = new Map();
		this._enumerationOberflaechenWerte = new Map();
		this._enumerationCategoryValues = new Map();
		this._stringHintLists = new Map();
		this._initFeldFehlertexte();
		this._initFeldtypen();
		this._initFelder();
	}
	Meta_Felder_relationshipmetamodel.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_relationshipmetamodel.prototype._initFeldtypen = function () {
	this._metaFormate.set(
		0,
		new FormatDefinitionString(
			-1, // minLaenge
			99999, // maxLaenge
			1, // minLaengePrintable
			99999, // maxLaengePrintable
			"", // muster
			this._mehrsprachigeFehlertexte.get(0), // fehlertexte
			false, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(0)
		)
	);
	this._metaFormate.set(
		1,
		new FormatDefinitionString(
			-1, // minLaenge
			99999, // maxLaenge
			1, // minLaengePrintable
			99999, // maxLaengePrintable
			"", // muster
			this._mehrsprachigeFehlertexte.get(1), // fehlertexte
			true, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(1)
		)
	);
	this._metaFormate.set(
		2,
		new FormatDefinitionString(
			1, // minLaenge
			100, // maxLaenge
			1, // minLaengePrintable
			100, // maxLaengePrintable
			"[_a-zA-Z][-_.a-zA-Z0-9]*", // muster
			this._mehrsprachigeFehlertexte.get(2), // fehlertexte
			false, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(2)
		)
	);
	this._metaFormate.set(
		3,
		new FormatDefinitionJaNein(
			"true", // jaWert
			"false" // neinWert

		)
	);
	this._metaFormate.set(
		4,
		new FormatDefinitionString(
			1, // minLaenge
			85, // maxLaenge
			1, // minLaengePrintable
			85, // maxLaengePrintable
			"[_a-zA-Z][-_.a-zA-Z0-9]*", // muster
			this._mehrsprachigeFehlertexte.get(4), // fehlertexte
			false, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(4)
		)
	);
	this._metaFormate.set(
		5,
		new FormatDefinitionZahl(
			true, // Vorzeichen
			true, // nullErlaubt
			false, // fuehrendeNullenErlaubt
			-1, // Vorkomma
			0, // Nachkomma
			0, // NachkommaBis
			-1, // minLaenge
			1, // minLaengeDisp
			-1, // maxLaenge
			16, // maxLaengeDisp
			false, // Geldbetrag
			"0", // MinWert
			"" // MaxWert

		)
	);
	};

	/*
	 * Die übergebenen Parameter bedeuten (in aufsteigender Reihenfolge):
	 * interne Nummer, eindeutiger name, voller Pfadname, pflichtFeld, mandatoryForRepeatableGroup, Format,
	 * Formatdefinition, javaScriptName, vordruckPflichtfeld (lfd. Nummern), pflichtfeld
	 * (lfd. Nummern), zusätzliche Schlüssel-Werte Paare, berechnungsUebertragServer
	 */
	Meta_Felder_relationshipmetamodel.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"id",
			"/header/id",
			true,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodelidValidate",
			[0],
			[0],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Name").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(0)
		)
	);
	this._felder.push(
		new Feld_t(
			1,
			"modelType_1",
			"/header/modelType",
			true,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodelmodelType_1Validate",
			[0],
			[0],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Model").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(1)
		)
	);
	this._felder.push(
		new Feld_t(
			2,
			"modelVersion",
			"/header/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodelmodelVersionValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Model Version").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(2)
		)
	);
	this._felder.push(
		new Feld_t(
			3,
			"code",
			"/header/locales/code",
			false,
			true,
			this._metaFormate.get(0),
			"relationshipmetamodelcodeValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Code").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", true).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(3)
		)
	);
	this._felder.push(
		new Feld_t(
			4,
			"locale_2",
			"/header/labels/locale",
			false,
			true,
			this._metaFormate.get(0),
			"relationshipmetamodellocale_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Locale").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", true).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(4)
		)
	);
	this._felder.push(
		new Feld_t(
			5,
			"text_2",
			"/header/labels/text",
			false,
			false,
			this._metaFormate.get(1),
			"relationshipmetamodeltext_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Text").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(5)
		)
	);
	this._felder.push(
		new Feld_t(
			6,
			"name",
			"/header/annotations/name",
			false,
			true,
			this._metaFormate.get(0),
			"relationshipmetamodelnameValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Name").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", true).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(6)
		)
	);
	this._felder.push(
		new Feld_t(
			7,
			"value",
			"/header/annotations/value",
			false,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodelvalueValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Value").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(7)
		)
	);
	this._felder.push(
		new Feld_t(
			8,
			"alias",
			"/header/modelReferences/alias",
			false,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodelaliasValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Alias").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(8)
		)
	);
	this._felder.push(
		new Feld_t(
			9,
			"purpose",
			"/header/modelReferences/purpose",
			false,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodelpurposeValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Purpose").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(9)
		)
	);
	this._felder.push(
		new Feld_t(
			10,
			"modelType_0",
			"/header/modelReferences/modelType",
			false,
			true,
			this._metaFormate.get(0),
			"relationshipmetamodelmodelType_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Model Type").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(10)
		)
	);
	this._felder.push(
		new Feld_t(
			11,
			"reference",
			"/header/modelReferences/reference",
			false,
			true,
			this._metaFormate.get(0),
			"relationshipmetamodelreferenceValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Reference").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(11)
		)
	);
	this._felder.push(
		new Feld_t(
			12,
			"locale_1",
			"/content/labels/locale",
			false,
			true,
			this._metaFormate.get(0),
			"relationshipmetamodellocale_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Locale").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", true).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(12)
		)
	);
	this._felder.push(
		new Feld_t(
			13,
			"text_1",
			"/content/labels/text",
			false,
			false,
			this._metaFormate.get(1),
			"relationshipmetamodeltext_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Text").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(13)
		)
	);
	this._felder.push(
		new Feld_t(
			14,
			"linkDocumentModel",
			"/content/linkDocumentModel",
			false,
			false,
			this._metaFormate.get(2),
			"relationshipmetamodellinkDocumentModelValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "DocumentModel for link document").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(14)
		)
	);
	this._felder.push(
		new Feld_t(
			15,
			"duplicatesAllowed",
			"/content/duplicatesAllowed",
			true,
			false,
			this._metaFormate.get(3),
			"relationshipmetamodelduplicatesAllowedValidate",
			[0],
			[0],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Duplicates Allowed").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(15)
		)
	);
	this._felder.push(
		new Feld_t(
			16,
			"role",
			"/content/entityCharacteristics/role",
			false,
			true,
			this._metaFormate.get(4),
			"relationshipmetamodelroleValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Role").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(16)
		)
	);
	this._felder.push(
		new Feld_t(
			17,
			"locale_0",
			"/content/entityCharacteristics/labels/locale",
			false,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodellocale_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Locale").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(17)
		)
	);
	this._felder.push(
		new Feld_t(
			18,
			"text_0",
			"/content/entityCharacteristics/labels/text",
			false,
			false,
			this._metaFormate.get(0),
			"relationshipmetamodeltext_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Label").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(18)
		)
	);
	this._felder.push(
		new Feld_t(
			19,
			"documentModel",
			"/content/entityCharacteristics/documentModel",
			false,
			true,
			this._metaFormate.get(2),
			"relationshipmetamodeldocumentModelValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "DocumentModel").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(19)
		)
	);
	this._felder.push(
		new Feld_t(
			20,
			"ordered",
			"/content/entityCharacteristics/ordered",
			false,
			true,
			this._metaFormate.get(3),
			"relationshipmetamodelorderedValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Ordered").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(20)
		)
	);
	this._felder.push(
		new Feld_t(
			21,
			"unbounded",
			"/content/entityCharacteristics/linkConstraints/multiplicity/unbounded",
			false,
			true,
			this._metaFormate.get(3),
			"relationshipmetamodelunboundedValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Unbounded").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", true)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(21)
		)
	);
	this._felder.push(
		new Feld_t(
			22,
			"upperLimit",
			"/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit",
			false,
			false,
			this._metaFormate.get(5),
			"relationshipmetamodelupperLimitValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Upper Limit").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(22)
		)
	);
	};
	Meta_Felder_relationshipmetamodel.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(2, new Map());
	this._mehrsprachigeFehlertexte.get(2).set("en", "model name must be string of length between 1 and 100 characters and matching the pattern /[_a-zA-Z][-_.a-zA-Z0-9]*/");
	this._mehrsprachigeFehlertexte.set(4, new Map());
	this._mehrsprachigeFehlertexte.get(4).set("en", "Use only letters, digits, hyphens, underscores and periods. Furthermore, the name may only start with a letter or underscore.");
	};


	return Meta_Felder_relationshipmetamodel;
}());
var Meta_Regel_relationshipmetamodel = (function () {
	function Meta_Regel_relationshipmetamodel() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_relationshipmetamodel.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_relationshipmetamodel.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_relationshipmetamodel.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_relationshipmetamodel.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_relationshipmetamodel.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_relationshipmetamodel.ANZAHL_REGELN = 2;

	Meta_Regel_relationshipmetamodel.prototype._initRegelRefs = function () {
	// Init Daten zur Regel '/header/mustHaveAtLeastOneLocale'
	this._refIdentifier[0] = [
			new Identifier_t(1, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(3, [1, IIdentifier.ALL_INDICES, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[0]= [
			new Identifier_t(3, [1, IIdentifier.ALL_INDICES, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet'
	this._refIdentifier[1] = [
			new Identifier_t(21, [1, IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(22, [1, IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[1]= [
			new Identifier_t(21, [1, IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(22, [1, IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	};

	Meta_Regel_relationshipmetamodel.prototype._initRegelFehlertexte = function () {
	// Init Daten zur Regel '/header/mustHaveAtLeastOneLocale'
	this._mehrsprachigeFehlertexte.get(0).set("en", "Locales must not be empty");
	this._fehlertexteParameterMapping.set(0, new MapBuilder().build());
	// Init Daten zur Regel '/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet'
	this._mehrsprachigeFehlertexte.get(1).set("en", "When not unbounded, upperLimit must have a value");
	this._fehlertexteParameterMapping.set(1, new MapBuilder().build());
	};

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	Meta_Regel_relationshipmetamodel.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("mustHaveAtLeastOneLocale", "/header/mustHaveAtLeastOneLocale", "Error rule_e366c", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/header/modelType", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("whenNotUnboundedUpperLimitShouldBeSet", "/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet", "Error rule_3ce1d", this._mehrsprachigeFehlertexte.get(1), this._fehlertexteParameterMapping.get(1), "Fehler", this._refIdentifier[1], this._refAuslassungsIdentifier[1], null, "/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit", false, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_relationshipmetamodel;
}());
var Meta_PfFeld_relationshipmetamodel = (function () {
	function Meta_PfFeld_relationshipmetamodel(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_relationshipmetamodel.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		this._addLokalePflichtFeldInfos_0(infos);

		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};

	Meta_PfFeld_relationshipmetamodel.prototype._addLokalePflichtFeldInfos_0 = function (infos) {
		// /header/locales/vk_vkcode_req
		if (infos != null) {
			infos[0] = [
					"code[1,-1,1]",
					"code[1,-1,1]"
			];

		}
		// /content/labels/vk_vklocale_req
		if (infos != null) {
			infos[1] = [
					"locale_1[1,-1,1];text_1[1,-1,1]",
					"locale_1[1,-1,1]"
			];

		}
		// /header/annotations/vk_vkname_req
		if (infos != null) {
			infos[2] = [
					"name[1,-1,1];value[1,-1,1]",
					"name[1,-1,1]"
			];

		}
		// /header/labels/vk_vklocale_req
		if (infos != null) {
			infos[3] = [
					"locale_2[1,-1,1];text_2[1,-1,1]",
					"locale_2[1,-1,1]"
			];

		}
		// /header/modelReferences/vk_vkmodelType_req&/header/modelReferences/vk_vkreference_req
		if (infos != null) {
			infos[4] = [
					"alias[1,-1,1];modelType_0[1,-1,1];purpose[1,-1,1];reference[1,-1,1]",
					"modelType_0[1,-1,1];reference[1,-1,1]"
			];

		}
		// /content/entityCharacteristics/linkConstraints/multiplicity/vk_vkunbounded_req&/content/entityCharacteristics/vk_vkdocumentModel_req&/content/entityCharacteristics/vk_vkordered_req&/content/entityCharacteristics/vk_vkrole_req
		if (infos != null) {
			infos[5] = [
					"documentModel[1,-1,1];locale_0[1,-1,0,1];ordered[1,-1,1];role[1,-1,1];text_0[1,-1,0,1];unbounded[1,-1,1,1,1];upperLimit[1,-1,1,1,1]",
					"documentModel[1,-1,1];ordered[1,-1,1];role[1,-1,1];unbounded[1,-1,1,1,1]"
			];

		}

	};



	Meta_PfFeld_relationshipmetamodel.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_relationshipmetamodel;
}());
var getMetaFSMenge = function (beschreibung) {
		var result = new HashSet();
		var felder = beschreibung.split(";");
		for (var i = 0; i < felder.length; i++) {
			if (felder[i].length > 0) {
				result.add(MetaIdentifier.parse(felder[i]));
			}
		}
		return result;
};

var Meta_relationshipmetamodel = (function () {
	function Meta_relationshipmetamodel() {
		this._metaFelder = new Meta_Felder_relationshipmetamodel();
		this._metaRegeln = new Meta_Regel_relationshipmetamodel();
		this._unterstuetzteSprachen = ["en"];
		this._da = new Model_t(
				"29.4.0", // Produkt-Version
				".", // dezimaltrenner
				["en"], // unterstuetzte Sprachen
				MetaDataHelper.createSetOfStrings(), //  Namen der Applikationsbedingungen
				MetaDataHelper.createSetOfStrings(), // Names of custom field types
				// additional Values
				new MapBuilder()
					.put("BaseYear", null)
					.put("TimeZone", "UTC")
					.put("ModelId", "RelationshipMetaModel")
					.put("Annotation", new MapBuilder()
					.put("roles", "guest,admin,systemAdmin").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_relationshipmetamodel._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_relationshipmetamodel.getInstance() instead of new.");
		}
		Meta_relationshipmetamodel._instance = this;
		var container = Meta_relationshipmetamodel._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_relationshipmetamodel.LEGAL_CHARACTERS,
			Meta_relationshipmetamodel.LEGAL_GRAPHEME_TRIE,
			() => E_relationshipmetamodel.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_relationshipmetamodel(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_relationshipmetamodel, "RUNTIME_VERSION", { get: function () { return "31.1"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_relationshipmetamodel, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_relationshipmetamodel, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_relationshipmetamodel, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_relationshipmetamodel, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_relationshipmetamodel.getInstance = function () {
		return Meta_relationshipmetamodel._instance;
	};
	Meta_relationshipmetamodel.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_relationshipmetamodel.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_relationshipmetamodel._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/header", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/header/locales", 9999, ["code" ], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/header/labels", 9999, ["locale" ], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/header/annotations", 9999, ["name" ], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/header/modelReferences", 9999, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/content", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/content/labels", 9999, ["locale" ], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/content/entityCharacteristics", 2, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/content/entityCharacteristics/labels", 999, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/content/entityCharacteristics/linkConstraints", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/content/entityCharacteristics/linkConstraints/multiplicity", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_relationshipmetamodel.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_relationshipmetamodel.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_relationshipmetamodel._constructor = (function () {
		if (Meta_relationshipmetamodel.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_relationshipmetamodel.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_relationshipmetamodel._instance = new Meta_relationshipmetamodel();
	})();
	return Meta_relationshipmetamodel;
}());var E_relationshipmetamodel = (function () {
	function E_relationshipmetamodel() {
		if (E_relationshipmetamodel._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_relationshipmetamodel.getInstance() instead of new.");
		}
		E_relationshipmetamodel._valueCalcObject = this;
		this._feldData = Meta_relationshipmetamodel.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_relationshipmetamodel.getInstance = function () {
		return this._valueCalcObject;
	};


	E_relationshipmetamodel.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_relationshipmetamodel._valueCalcObject = new E_relationshipmetamodel();
	return E_relationshipmetamodel;
}());
var C_relationshipmetamodel = (function () {
	function C_relationshipmetamodel() {
		if (C_relationshipmetamodel._checkObject) {
			throw new Error("Instantiation failed: Use C_relationshipmetamodel.getInstance() instead of new.");
		}
		C_relationshipmetamodel._checkObject = this;
		this._feldData = Meta_relationshipmetamodel.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_relationshipmetamodel.getInstance = function () {
		return this._checkObject;
	};


	C_relationshipmetamodel.CALCULATED_FIELD_DEPENDENCIES = new Map();

	C_relationshipmetamodel.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set();

	C_relationshipmetamodel.CALC_FUNC_NAME_MAP = new Map();

	C_relationshipmetamodel.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [];

	C_relationshipmetamodel.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_relationshipmetamodel.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_relationshipmetamodel.prototype.hasCalculation = function(fullFieldName) {
        return C_relationshipmetamodel.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_relationshipmetamodel.prototype.getDependencyGraph = function() {
        return C_relationshipmetamodel.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_relationshipmetamodel.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_relationshipmetamodel.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_relationshipmetamodel.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_relationshipmetamodel.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_relationshipmetamodel._checkObject = new C_relationshipmetamodel();
	return C_relationshipmetamodel;
}());
var R_relationshipmetamodel = (function () {
	function R_relationshipmetamodel() {
		this._feldData = Meta_relationshipmetamodel.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_relationshipmetamodel.getInstance = function () {
		return R_relationshipmetamodel._checkObject;
	};
	R_relationshipmetamodel.prototype.mvk_mustHaveAtLeastOneLocale = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/mustHaveAtLeastOneLocale", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("code").isField(true).idx(1).idx(1, 9999).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("modelType_1").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinKontextAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						var tb0 = controller.keinKontextAngegeben(idx, allRtIdCon1);
						var tb1 = controller.alleFelderAngegeben(idx, allRtIdCon2);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("modelType_1", idx, "Error rule_e366c", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("modelType_1", iter.get(), "Error rule_e366c", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_whenNotUnboundedUpperLimitShouldBeSet = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/entityCharacteristics/linkConstraints/multiplicity/whenNotUnboundedUpperLimitShouldBeSet", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1, 1];
		var ende = [1, 2, 1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("unbounded").isField(true).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("upperLimit").isField(true).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.vergleicheSTRING(VglOp.EQ, controller.feldWert(idx, rtIdCon1), "false").isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var tb0 = controller.vergleicheSTRING(VglOp.EQ, controller.feldWert(idx, rtIdCon1), "false");
						var tb1 = controller.keinFeldAngegeben(idx, allRtIdCon1);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("upperLimit", idx, "Error rule_3ce1d", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("upperLimit", iter.get(), "Error rule_3ce1d", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkid_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/vk_vkid_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("id").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						controller.preliminaryError("id", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("id", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkmodelType_req_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/vk_vkmodelType_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("modelType_1").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						controller.preliminaryError("modelType_1", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("modelType_1", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkcode_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/locales/vk_vkcode_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/locales").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("code").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("code", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("code", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkname_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/annotations/vk_vkname_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/annotations").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("name").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("name", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("name", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkmodelType_req_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/modelReferences/vk_vkmodelType_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/modelReferences").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("modelType_0").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("modelType_0", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("modelType_0", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkreference_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/modelReferences/vk_vkreference_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/modelReferences").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("reference").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("reference", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("reference", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkduplicatesAllowed_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/vk_vkduplicatesAllowed_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("duplicatesAllowed").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						controller.preliminaryError("duplicatesAllowed", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("duplicatesAllowed", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkrole_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/entityCharacteristics/vk_vkrole_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 2, 1];

		var iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("role").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("role", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("role", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkdocumentModel_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/entityCharacteristics/vk_vkdocumentModel_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 2, 1];

		var iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("documentModel").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("documentModel", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("documentModel", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkordered_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/entityCharacteristics/vk_vkordered_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 2, 1];

		var iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("ordered").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("ordered", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("ordered", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkunbounded_req = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/entityCharacteristics/linkConstraints/multiplicity/vk_vkunbounded_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1, 1];
		var ende = [1, 2, 1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/entityCharacteristics").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("unbounded").isField(true).idx(1).idx(-1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("unbounded", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("unbounded", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vklocales_vk_grp_code_unq = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/vk_vklocales_vk_grp_code_unq", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("code").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 1)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, [1], repNotUnqErrorCache, iter.semanticIndexLevel) .isKnownAndTrue()) {
						controller.preliminaryError("code", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("code", iter.get(), "uniqueField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vklabels_vk_grp_locale_unq_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/vk_vklabels_vk_grp_locale_unq", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("locale_2").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 1)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, [1], repNotUnqErrorCache, iter.semanticIndexLevel) .isKnownAndTrue()) {
						controller.preliminaryError("locale_2", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("locale_2", iter.get(), "uniqueField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vkannotations_vk_grp_name_unq = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/vk_vkannotations_vk_grp_name_unq", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("name").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 1)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, [1], repNotUnqErrorCache, iter.semanticIndexLevel) .isKnownAndTrue()) {
						controller.preliminaryError("name", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("name", iter.get(), "uniqueField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vklabels_vk_grp_locale_unq_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/vk_vklabels_vk_grp_locale_unq", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("locale_1").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var repNotUnqErrorCache = new RepetitionNotUniqueErrorCache();

			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (ArrayUtils.sindGleichAbPosition(idx.getIndexes(), start, 1)) {
					//Die Iteration über die Zeile beginnt neu - setze die Mengen neu.
					repNotUnqErrorCache.clear();
				}

				if (controller.wiederholungNichtEindeutig(idx, allRtIdCon1, [1], repNotUnqErrorCache, iter.semanticIndexLevel) .isKnownAndTrue()) {
						controller.preliminaryError("locale_1", idx, IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER, ErrorType.VALUE_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("locale_1", iter.get(), "uniqueField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vklocale_req_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/header/labels/vk_vklocale_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/header/labels").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("locale_2").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("locale_2", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("locale_2", iter.get(), "mandatoryField", e);
			}
		}
	};
	R_relationshipmetamodel.prototype.mvk_vk_vklocale_req_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/content/labels/vk_vklocale_req", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 9999), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/content/labels").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("locale_1").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						controller.preliminaryError("locale_1", idx, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER, ErrorType.OMISSION_ERROR);
				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("locale_1", iter.get(), "mandatoryField", e);
			}
		}
	};


	R_relationshipmetamodel.vordruckF__header = function (controller) {
		/* Aufruf der Regeln fuer Vordruck header
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("header", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("header", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruckF__header_lfdNr(controller, indices);
				}
			}
		}
	};
	R_relationshipmetamodel.vordruckF__header_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck header für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_mustHaveAtLeastOneLocale(controller, indices);
		controller.logMessung("mvk_mustHaveAtLeastOneLocale", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.vordruckF__content = function (controller) {
		/* Aufruf der Regeln fuer Vordruck content
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("content", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("content", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruckF__content_lfdNr(controller, indices);
				}
			}
		}
	};
	R_relationshipmetamodel.vordruckF__content_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck content für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_whenNotUnboundedUpperLimitShouldBeSet(controller, indices);
		controller.logMessung("mvk_whenNotUnboundedUpperLimitShouldBeSet", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.vordruck__header = function (controller) {
		/* Aufruf der Regeln fuer Vordruck header
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("header", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("header", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruck__header_lfdNr(controller, indices);
				}
			}
		}
	};
	R_relationshipmetamodel.vordruck__header_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck header für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_mustHaveAtLeastOneLocale(controller, indices);
		controller.logMessung("mvk_mustHaveAtLeastOneLocale", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.vordruck__content = function (controller) {
		/* Aufruf der Regeln fuer Vordruck content
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("content", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("content", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruck__content_lfdNr(controller, indices);
				}
			}
		}
	};
	R_relationshipmetamodel.vordruck__content_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck content für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_whenNotUnboundedUpperLimitShouldBeSet(controller, indices);
		controller.logMessung("mvk_whenNotUnboundedUpperLimitShouldBeSet", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.vordruckP__global = function (controller) {
	/* Aufruf der Regeln fuer Vordruck global für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkid_req(controller, indices);
		controller.logMessung("mvk_vk_vkid_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkmodelType_req_1(controller, indices);
		controller.logMessung("mvk_vk_vkmodelType_req_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkduplicatesAllowed_req(controller, indices);
		controller.logMessung("mvk_vk_vkduplicatesAllowed_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.vordruckP__header = function (controller) {
	/* Aufruf der Regeln fuer Vordruck header für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkcode_req(controller, indices);
		controller.logMessung("mvk_vk_vkcode_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkname_req(controller, indices);
		controller.logMessung("mvk_vk_vkname_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkmodelType_req_0(controller, indices);
		controller.logMessung("mvk_vk_vkmodelType_req_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkreference_req(controller, indices);
		controller.logMessung("mvk_vk_vkreference_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklocales_vk_grp_code_unq(controller, indices);
		controller.logMessung("mvk_vk_vklocales_vk_grp_code_unq", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklabels_vk_grp_locale_unq_1(controller, indices);
		controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkannotations_vk_grp_name_unq(controller, indices);
		controller.logMessung("mvk_vk_vkannotations_vk_grp_name_unq", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklocale_req_1(controller, indices);
		controller.logMessung("mvk_vk_vklocale_req_1", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.vordruckP__content = function (controller) {
	/* Aufruf der Regeln fuer Vordruck content für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkrole_req(controller, indices);
		controller.logMessung("mvk_vk_vkrole_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkdocumentModel_req(controller, indices);
		controller.logMessung("mvk_vk_vkdocumentModel_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkordered_req(controller, indices);
		controller.logMessung("mvk_vk_vkordered_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkunbounded_req(controller, indices);
		controller.logMessung("mvk_vk_vkunbounded_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklabels_vk_grp_locale_unq_0(controller, indices);
		controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklocale_req_0(controller, indices);
		controller.logMessung("mvk_vk_vklocale_req_0", indices[0], startMesspunkt);
	};


	R_relationshipmetamodel.validatePreliminaryRulesForField__code = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkcode_req(controller, indices);
		controller.logMessung("mvk_vk_vkcode_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklocales_vk_grp_code_unq(controller, indices);
		controller.logMessung("mvk_vk_vklocales_vk_grp_code_unq", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__documentModel = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkdocumentModel_req(controller, indices);
		controller.logMessung("mvk_vk_vkdocumentModel_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__duplicatesAllowed = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkduplicatesAllowed_req(controller, indices);
		controller.logMessung("mvk_vk_vkduplicatesAllowed_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__id = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkid_req(controller, indices);
		controller.logMessung("mvk_vk_vkid_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__locale_1 = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklabels_vk_grp_locale_unq_0(controller, indices);
		controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklocale_req_0(controller, indices);
		controller.logMessung("mvk_vk_vklocale_req_0", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__locale_2 = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklabels_vk_grp_locale_unq_1(controller, indices);
		controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vklocale_req_1(controller, indices);
		controller.logMessung("mvk_vk_vklocale_req_1", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__modelType_0 = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkmodelType_req_0(controller, indices);
		controller.logMessung("mvk_vk_vkmodelType_req_0", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__modelType_1 = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkmodelType_req_1(controller, indices);
		controller.logMessung("mvk_vk_vkmodelType_req_1", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__name = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkname_req(controller, indices);
		controller.logMessung("mvk_vk_vkname_req", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkannotations_vk_grp_name_unq(controller, indices);
		controller.logMessung("mvk_vk_vkannotations_vk_grp_name_unq", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__ordered = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkordered_req(controller, indices);
		controller.logMessung("mvk_vk_vkordered_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__reference = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkreference_req(controller, indices);
		controller.logMessung("mvk_vk_vkreference_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__role = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkrole_req(controller, indices);
		controller.logMessung("mvk_vk_vkrole_req", indices[0], startMesspunkt);
	};
	R_relationshipmetamodel.validatePreliminaryRulesForField__unbounded = function (controller) {
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_vk_vkunbounded_req(controller, indices);
		controller.logMessung("mvk_vk_vkunbounded_req", indices[0], startMesspunkt);
	};

	R_relationshipmetamodel.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_relationshipmetamodel["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_relationshipmetamodel, controller);
		}
	};

	R_relationshipmetamodel.prototype.validatePreliminaryRules = function(controller) {
		R_relationshipmetamodel.vordruckP__global(controller);
		R_relationshipmetamodel.vordruckP__header(controller);
		R_relationshipmetamodel.vordruckP__content(controller);
};

	R_relationshipmetamodel.prototype.validiereVoll = function (controller, modus) {
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				this.alleRegeln(controller);
				break;
			case RuleType.INFO:
                this.alleInfoRegeln(controller);
                break;
            case RuleType.HINT:
				this.alleHinweisRegeln(controller);
				break;
			case RuleType.ERROR:
				this.alleFehlerRegeln(controller);
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
	};
	R_relationshipmetamodel.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_relationshipmetamodel();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_relationshipmetamodel();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_relationshipmetamodel();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_relationshipmetamodel();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_relationshipmetamodel();
			felder.forEach(function (id) {
				var interneFeldNr = _this._metaDataValidierung.getInterneFeldNr(id.getName());
				preChecker.fuehreAus(controller, interneFeldNr, id.getIndices());
			});
		}
		felder.forEach(function (id) {
			var interneFeldNr = _this._metaDataValidierung.getInterneFeldNr(id.getName());
			checker.fuehreAus(controller, interneFeldNr, id.getIndices());
		});
	};
	R_relationshipmetamodel.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_relationshipmetamodel.vordruck__header(controller);
		R_relationshipmetamodel.vordruck__content(controller);
};
	R_relationshipmetamodel.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_relationshipmetamodel.vordruckF__header(controller);
		R_relationshipmetamodel.vordruckF__content(controller);
};
	R_relationshipmetamodel.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_relationshipmetamodel.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_relationshipmetamodel._checkObject = new R_relationshipmetamodel();
	return R_relationshipmetamodel;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_relationshipmetamodel = (function () {
	function TVCheckFehler_relationshipmetamodel() {
		this._checkObject = R_relationshipmetamodel.getInstance();
	}
	TVCheckFehler_relationshipmetamodel.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 22) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [1,22]
	* liegt.
	*
	* In der Methode werden allerdings nur interne Nummern von Feldern
	* betrachtet, die Aktionsfelder einer Methode sind. Es müssen somit nicht alle
	* Felder des Intervalls in der Switch-Anweisung berücksichtigt werden.
	*
	* @param controller
	* @param interneFeldNr
	* @param indices
	*/

	TVCheckFehler_relationshipmetamodel.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
		var startMesspunkt = -1;
		switch (interneFeldNr) {
			case 1:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_mustHaveAtLeastOneLocale(controller, indices);
				controller.logMessung("mvk_mustHaveAtLeastOneLocale", indices[0], startMesspunkt);
				break;
			case 22:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_whenNotUnboundedUpperLimitShouldBeSet(controller, indices);
				controller.logMessung("mvk_whenNotUnboundedUpperLimitShouldBeSet", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 22) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [1,22].");
			}
			break;
		}
	};

	return TVCheckFehler_relationshipmetamodel;
}());
var TVCheckHinweis_relationshipmetamodel = (function () {
	function TVCheckHinweis_relationshipmetamodel() {
		this._checkObject = R_relationshipmetamodel.getInstance();
	}
	TVCheckHinweis_relationshipmetamodel.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_relationshipmetamodel;
}());
var TVCheckInfo_relationshipmetamodel = (function () {
	function TVCheckInfo_relationshipmetamodel() {
		this._checkObject = R_relationshipmetamodel.getInstance();
	}
	TVCheckInfo_relationshipmetamodel.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_relationshipmetamodel;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_relationshipmetamodel = (function () {
	function TVCheckAlle_relationshipmetamodel() {
		this._tvFehlerChecker = new TVCheckFehler_relationshipmetamodel();
		this._tvHinweisChecker = new TVCheckHinweis_relationshipmetamodel();
		this._tvInfoChecker = new TVCheckInfo_relationshipmetamodel();
	}
	TVCheckAlle_relationshipmetamodel.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_relationshipmetamodel;
}());
var TVCheckPreliminary_relationshipmetamodel = (function () {
	function TVCheckPreliminary_relationshipmetamodel() {
		this._checkObject = R_relationshipmetamodel.getInstance();
	}
	TVCheckPreliminary_relationshipmetamodel.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 21) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};
	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [0,21]
	* liegt.
	*
	* In der Methode werden allerdings nur interne Nummern von Feldern
	* betrachtet, die Aktionsfelder einer Methode sind. Es müssen somit nicht alle
	* Felder des Intervalls in der Switch-Anweisung berücksichtigt werden.
	*
	* @param controller
	* @param interneFeldNr
	* @param indices
	*/

	TVCheckPreliminary_relationshipmetamodel.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
		var startMesspunkt = -1;
		switch (interneFeldNr) {
			case 0:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkid_req(controller, indices);
				controller.logMessung("mvk_vk_vkid_req", indices[0], startMesspunkt);
				break;
			case 1:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkmodelType_req_1(controller, indices);
				controller.logMessung("mvk_vk_vkmodelType_req_1", indices[0], startMesspunkt);
				break;
			case 3:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkcode_req(controller, indices);
				controller.logMessung("mvk_vk_vkcode_req", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vklocales_vk_grp_code_unq(controller, indices);
				controller.logMessung("mvk_vk_vklocales_vk_grp_code_unq", indices[0], startMesspunkt);
				break;
			case 4:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vklabels_vk_grp_locale_unq_1(controller, indices);
				controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_1", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vklocale_req_1(controller, indices);
				controller.logMessung("mvk_vk_vklocale_req_1", indices[0], startMesspunkt);
				break;
			case 6:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkname_req(controller, indices);
				controller.logMessung("mvk_vk_vkname_req", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkannotations_vk_grp_name_unq(controller, indices);
				controller.logMessung("mvk_vk_vkannotations_vk_grp_name_unq", indices[0], startMesspunkt);
				break;
			case 10:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkmodelType_req_0(controller, indices);
				controller.logMessung("mvk_vk_vkmodelType_req_0", indices[0], startMesspunkt);
				break;
			case 11:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkreference_req(controller, indices);
				controller.logMessung("mvk_vk_vkreference_req", indices[0], startMesspunkt);
				break;
			case 12:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vklabels_vk_grp_locale_unq_0(controller, indices);
				controller.logMessung("mvk_vk_vklabels_vk_grp_locale_unq_0", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vklocale_req_0(controller, indices);
				controller.logMessung("mvk_vk_vklocale_req_0", indices[0], startMesspunkt);
				break;
			case 15:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkduplicatesAllowed_req(controller, indices);
				controller.logMessung("mvk_vk_vkduplicatesAllowed_req", indices[0], startMesspunkt);
				break;
			case 16:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkrole_req(controller, indices);
				controller.logMessung("mvk_vk_vkrole_req", indices[0], startMesspunkt);
				break;
			case 19:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkdocumentModel_req(controller, indices);
				controller.logMessung("mvk_vk_vkdocumentModel_req", indices[0], startMesspunkt);
				break;
			case 20:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkordered_req(controller, indices);
				controller.logMessung("mvk_vk_vkordered_req", indices[0], startMesspunkt);
				break;
			case 21:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_vk_vkunbounded_req(controller, indices);
				controller.logMessung("mvk_vk_vkunbounded_req", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 21) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [0,21].");
			}
			break;
		}
	};
	return TVCheckPreliminary_relationshipmetamodel;
}());
var ERValidator_relationshipmetamodel = (function () {
	function ERValidator_relationshipmetamodel() {
		if (ERValidator_relationshipmetamodel._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_relationshipmetamodel.getInstance() instead of new.");
		}
		ERValidator_relationshipmetamodel._instance = this;
	}
	ERValidator_relationshipmetamodel.getInstance = function () {
		return ERValidator_relationshipmetamodel._instance;
	};
	ERValidator_relationshipmetamodel.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
		if (!daten) {
			throw new Error("Übergebene Daten dürfen nicht null sein");
		}
		if (!logger) {
			throw new Error("Übergebener Logger darf nicht null sein");
		}
		if (!ergebnis) {
			throw new Error("Übergebenes Ergebnis darf nicht null sein");
		}
	};
	ERValidator_relationshipmetamodel.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_relationshipmetamodel.getInstance(), modus, ERValidator_relationshipmetamodel._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_relationshipmetamodel.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_relationshipmetamodel.getInstance(), modus, ERValidator_relationshipmetamodel._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_relationshipmetamodel.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_relationshipmetamodel.getInstance(), modus, ERValidator_relationshipmetamodel._metaData, logger, C_relationshipmetamodel.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_relationshipmetamodel._instance = new ERValidator_relationshipmetamodel();
	ERValidator_relationshipmetamodel._metaData = Meta_relationshipmetamodel.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_relationshipmetamodel._constructor = (function () {
	})();
	return ERValidator_relationshipmetamodel;
}());

return {
	validator: ERValidator_relationshipmetamodel,
	meta: Meta_relationshipmetamodel
};
