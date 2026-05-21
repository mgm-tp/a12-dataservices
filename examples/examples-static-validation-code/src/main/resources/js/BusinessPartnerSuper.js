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
var Meta_Felder_businesspartnersuper = (function () {
	function Meta_Felder_businesspartnersuper() {
		this._felder = new Array();
		this._metaFormate = new Map();
		this._mehrsprachigeFehlertexte = new Map();
		this._errorTexts4Req = new Map();
		this._enumerationOberflaechenWerte = new Map();
		this._enumerationCategoryValues = new Map();
		this._stringHintLists = new Map();
		this._initFeldFehlertexte();
		this._initEnumerationOberflaechenWerte();
		this._initFeldtypen();
		this._initFelder();
	}
	Meta_Felder_businesspartnersuper.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_businesspartnersuper.prototype._initFeldtypen = function () {
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
			true, // noValueValidation
			this._stringHintLists.get(1)
		)
	);
	this._metaFormate.set(
		2,
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
			"", // MinWert
			"" // MaxWert

		)
	);
	this._metaFormate.set(
		3,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(3),
			["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"],
			this._enumerationOberflaechenWerte.get(3),
			this._enumerationCategoryValues.get(3),
			""
		)
	);
	this._metaFormate.set(
		4,
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		5,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(5),
			["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"],
			this._enumerationOberflaechenWerte.get(5),
			this._enumerationCategoryValues.get(5),
			""
		)
	);
	this._metaFormate.set(
		6,
		new FormatDefinitionZahl(
			true, // Vorzeichen
			true, // nullErlaubt
			false, // fuehrendeNullenErlaubt
			-1, // Vorkomma
			0, // Nachkomma
			2, // NachkommaBis
			-1, // minLaenge
			1, // minLaengeDisp
			-1, // maxLaenge
			17, // maxLaengeDisp
			false, // Geldbetrag
			"", // MinWert
			"" // MaxWert

		)
	);
	this._metaFormate.set(
		7,
		new FormatDefinitionDatum(
			"yyyy-MM-dd'T'HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	};

	/*
	 * Die übergebenen Parameter bedeuten (in aufsteigender Reihenfolge):
	 * interne Nummer, eindeutiger name, voller Pfadname, pflichtFeld, mandatoryForRepeatableGroup, Format,
	 * Formatdefinition, javaScriptName, vordruckPflichtfeld (lfd. Nummern), pflichtfeld
	 * (lfd. Nummern), zusätzliche Schlüssel-Werte Paare, berechnungsUebertragServer
	 */
	Meta_Felder_businesspartnersuper.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"original_filename",
			"/BusinessPartnerRoot/Attachment/original_filename",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersuperoriginal_filenameValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"internal_filename",
			"/BusinessPartnerRoot/Attachment/internal_filename",
			false,
			true,
			this._metaFormate.get(0),
			"businesspartnersuperinternal_filenameValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"content",
			"/BusinessPartnerRoot/Attachment/content",
			false,
			false,
			this._metaFormate.get(1),
			"businesspartnersupercontentValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
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
			"attachment_id",
			"/BusinessPartnerRoot/Attachment/attachment_id",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersuperattachment_idValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(3)
		)
	);
	this._felder.push(
		new Feld_t(
			4,
			"size",
			"/BusinessPartnerRoot/Attachment/size",
			false,
			false,
			this._metaFormate.get(2),
			"businesspartnersupersizeValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(4)
		)
	);
	this._felder.push(
		new Feld_t(
			5,
			"mime_type",
			"/BusinessPartnerRoot/Attachment/mime_type",
			false,
			true,
			this._metaFormate.get(0),
			"businesspartnersupermime_typeValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
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
			"category",
			"/BusinessPartnerRoot/Attachment/category",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersupercategoryValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(6)
		)
	);
	this._felder.push(
		new Feld_t(
			7,
			"description",
			"/BusinessPartnerRoot/Attachment/description",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersuperdescriptionValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
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
			"Name",
			"/BusinessPartnerRoot/Name",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersuperNameValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Name").build())
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
			"Industry",
			"/BusinessPartnerRoot/Industry",
			false,
			false,
			this._metaFormate.get(3),
			"businesspartnersuperIndustryValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Industry or business sector").build())
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
			"StartOfRelationship",
			"/BusinessPartnerRoot/StartOfRelationship",
			false,
			false,
			this._metaFormate.get(4),
			"businesspartnersuperStartOfRelationshipValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Start of business relationship").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"CustomerDiscount",
			"/BusinessPartnerRoot/CustomerDiscount",
			false,
			false,
			this._metaFormate.get(5),
			"businesspartnersuperCustomerDiscountValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Customer Discount").put("de", "Kundenrabatt").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"income",
			"/BusinessPartnerRoot/Employment/income",
			false,
			false,
			this._metaFormate.get(6),
			"businesspartnersuperincomeValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Income").put("de", "Einkommen").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(12)
		)
	);
	this._felder.push(
		new Feld_t(
			13,
			"tax",
			"/BusinessPartnerRoot/Employment/tax",
			false,
			false,
			this._metaFormate.get(6),
			"businesspartnersupertaxValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "tax").put("de", "Steuer").build())
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
			"PersonOrEntity",
			"/BusinessPartnerRoot/PersonOrEntity",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersuperPersonOrEntityValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Natural Person or Legal Entity").build())
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
			"docRef",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersuperdocRefValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Document Reference").put("de", "Dokumentreferenz").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"modelReference",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersupermodelReferenceValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Document Model Reference").put("de", "Document Model-Referenz").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"modelVersion",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersupermodelVersionValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Model Version").put("de", "Model-Version").build())
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
			"creator",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersupercreatorValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Creator").put("de", "Erstellt von").build())
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
			"createdAt",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(7),
			"businesspartnersupercreatedAtValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Created At").put("de", "Erstellt am").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"modifier",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnersupermodifierValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Modifier").put("de", "Bearbeitet von").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"modifiedAt",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(7),
			"businesspartnersupermodifiedAtValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Modified At").put("de", "Bearbeitet am").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(21)
		)
	);
	};
	Meta_Felder_businesspartnersuper.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(3, new Map());

	this._mehrsprachigeFehlertexte.set(5, new Map());

	};
	Meta_Felder_businesspartnersuper.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(3, new Map());
	this._enumerationOberflaechenWerte.get(3).set("de", MetaFeldUtil.createPairList(["Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(3).set("en", MetaFeldUtil.createPairList(["Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(3).set("en_US", MetaFeldUtil.createPairList(["Technology of Information", "Investment", "Bookkeeping", "HealthPrevention", "Liability", "Constitution", "Business"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));

	this._enumerationOberflaechenWerte.set(5, new Map());
	this._enumerationOberflaechenWerte.get(5).set("de", MetaFeldUtil.createPairList(["0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(5).set("en", MetaFeldUtil.createPairList(["0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(5).set("en_US", MetaFeldUtil.createPairList(["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));

	};


	return Meta_Felder_businesspartnersuper;
}());
var Meta_Regel_businesspartnersuper = (function () {
	function Meta_Regel_businesspartnersuper() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_businesspartnersuper.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_businesspartnersuper.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_businesspartnersuper.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_businesspartnersuper.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_businesspartnersuper.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_businesspartnersuper.ANZAHL_REGELN = 5;

	Meta_Regel_businesspartnersuper.prototype._initRegelRefs = function () {
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	this._refIdentifier[0] = [
			new Identifier_t(0, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(1, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(2, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(3, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(4, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(5, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(6, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(7, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[0]= [
			new Identifier_t(1, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	this._refIdentifier[1] = [
			new Identifier_t(0, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(1, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(2, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(3, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(4, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(5, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(6, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(7, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[1]= [
			new Identifier_t(5, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	this._refIdentifier[2] = [
			new Identifier_t(0, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(1, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(2, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(3, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(4, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(5, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(6, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(7, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[2]= [
			new Identifier_t(2, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(3, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	this._refIdentifier[3] = [
			new Identifier_t(2, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(4, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[3]= [
			new Identifier_t(4, [1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/Employment/TaxComputation'
	this._refIdentifier[4] = [
			new Identifier_t(12, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(13, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[4]= [
			new Identifier_t(12, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(13, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	};

	Meta_Regel_businesspartnersuper.prototype._initRegelFehlertexte = function () {
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	this._mehrsprachigeFehlertexte.get(0).set("de", "Internal Error: Field $internal_filename$ of customType attachment is not filled.");this._mehrsprachigeFehlertexte.get(0).set("en", "Internal Error: Field $internal_filename$ of customType attachment is not filled.");
	this._fehlertexteParameterMapping.set(0, new MapBuilder().put("internal_filename", "internal_filename@1@e@1").build());
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	this._mehrsprachigeFehlertexte.get(1).set("de", "Internal Error: Field $mime_type$ of customType attachment is not filled.");this._mehrsprachigeFehlertexte.get(1).set("en", "Internal Error: Field $mime_type$ of customType attachment is not filled.");
	this._fehlertexteParameterMapping.set(1, new MapBuilder().put("mime_type", "mime_type@1@e@1").build());
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	this._mehrsprachigeFehlertexte.get(2).set("de", "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");this._mehrsprachigeFehlertexte.get(2).set("en", "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");
	this._fehlertexteParameterMapping.set(2, new MapBuilder().build());
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	this._mehrsprachigeFehlertexte.get(3).set("de", "Internal Error: If the content is filled, the size must be also filled.");this._mehrsprachigeFehlertexte.get(3).set("en", "Internal Error: If the content is filled, the size must be also filled.");
	this._fehlertexteParameterMapping.set(3, new MapBuilder().build());
	// Init Daten zur Regel '/BusinessPartnerRoot/Employment/TaxComputation'
	this._mehrsprachigeFehlertexte.get(4).set("de", "Berechnungsfehler für TaxComputation");this._mehrsprachigeFehlertexte.get(4).set("en", "Computation error for TaxComputation");
	this._fehlertexteParameterMapping.set(4, new MapBuilder().build());
	};

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	Meta_Regel_businesspartnersuper.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("AttachmentInternalFilenameRequired", "/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", "ErrorR29", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/BusinessPartnerRoot/Attachment/internal_filename", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentMimeTypeRequired", "/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", "ErrorR30", this._mehrsprachigeFehlertexte.get(1), this._fehlertexteParameterMapping.get(1), "Fehler", this._refIdentifier[1], this._refAuslassungsIdentifier[1], null, "/BusinessPartnerRoot/Attachment/mime_type", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentIdOrContentFilled", "/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", "ErrorR31", this._mehrsprachigeFehlertexte.get(2), this._fehlertexteParameterMapping.get(2), "Fehler", this._refIdentifier[2], this._refAuslassungsIdentifier[2], null, "/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("SizeOfContentFilled", "/BusinessPartnerRoot/Attachment/SizeOfContentFilled", "ErrorR32", this._mehrsprachigeFehlertexte.get(3), this._fehlertexteParameterMapping.get(3), "Fehler", this._refIdentifier[3], this._refAuslassungsIdentifier[3], null, "/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("TaxComputation", "/BusinessPartnerRoot/Employment/TaxComputation", "TaxComputation", this._mehrsprachigeFehlertexte.get(4), this._fehlertexteParameterMapping.get(4), "Fehler", this._refIdentifier[4], this._refAuslassungsIdentifier[4], null, "/BusinessPartnerRoot/Employment/tax", true, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_businesspartnersuper;
}());
var Meta_PfFeld_businesspartnersuper = (function () {
	function Meta_PfFeld_businesspartnersuper(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_businesspartnersuper.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		this._addLokalePflichtFeldInfos_0(infos);

		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};

	Meta_PfFeld_businesspartnersuper.prototype._addLokalePflichtFeldInfos_0 = function (infos) {
		// /BusinessPartnerRoot/Attachment/SizeOfContentFilled&/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired&/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired
		if (infos != null) {
			infos[0] = [
					"content[1,-1,1];size[1,-1,1]",
					"internal_filename[1,-1,1];mime_type[1,-1,1];size[1,-1,1]"
			];

		}
		// /BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired&/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired
		if (infos != null) {
			infos[1] = [
					"attachment_id[1,-1,1];category[1,-1,1];content[1,-1,1];description[1,-1,1];internal_filename[1,-1,1];mime_type[1,-1,1];original_filename[1,-1,1];size[1,-1,1]",
					"internal_filename[1,-1,1];mime_type[1,-1,1]"
			];

		}

	};



	Meta_PfFeld_businesspartnersuper.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_businesspartnersuper;
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

var Meta_businesspartnersuper = (function () {
	function Meta_businesspartnersuper() {
		this._metaFelder = new Meta_Felder_businesspartnersuper();
		this._metaRegeln = new Meta_Regel_businesspartnersuper();
		this._unterstuetzteSprachen = ["en", "en_US", "de"];
		this._da = new Model_t(
				"28.4.0", // Produkt-Version
				".", // dezimaltrenner
				["en", "en_US", "de"], // unterstuetzte Sprachen
				MetaDataHelper.createSetOfStrings(), //  Namen der Applikationsbedingungen
				MetaDataHelper.createSetOfStrings(), // Names of custom field types
				// additional Values
				new MapBuilder()
					.put("BaseYear", null)
					.put("TimeZone", "UTC")
					.put("ModelId", "BusinessPartnerSuper")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest,ModelRead")
					.put("abstract", "true")
					.put("subTypes", "BusinessPartner,BusinessPartnerLTD").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_businesspartnersuper._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_businesspartnersuper.getInstance() instead of new.");
		}
		Meta_businesspartnersuper._instance = this;
		var container = Meta_businesspartnersuper._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_businesspartnersuper.LEGAL_CHARACTERS,
			Meta_businesspartnersuper.LEGAL_GRAPHEME_TRIE,
			() => E_businesspartnersuper.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_businesspartnersuper(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_businesspartnersuper, "RUNTIME_VERSION", { get: function () { return "30.8"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartnersuper, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartnersuper, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartnersuper, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartnersuper, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_businesspartnersuper.getInstance = function () {
		return Meta_businesspartnersuper._instance;
	};
	Meta_businesspartnersuper.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_businesspartnersuper.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_businesspartnersuper._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Attachment", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Employment", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_businesspartnersuper.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_businesspartnersuper.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_businesspartnersuper._constructor = (function () {
		if (Meta_businesspartnersuper.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_businesspartnersuper.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_businesspartnersuper._instance = new Meta_businesspartnersuper();
	})();
	return Meta_businesspartnersuper;
}());var E_businesspartnersuper = (function () {
	function E_businesspartnersuper() {
		if (E_businesspartnersuper._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_businesspartnersuper.getInstance() instead of new.");
		}
		E_businesspartnersuper._valueCalcObject = this;
		this._feldData = Meta_businesspartnersuper.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_businesspartnersuper.getInstance = function () {
		return this._valueCalcObject;
	};


	E_businesspartnersuper.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_businesspartnersuper._valueCalcObject = new E_businesspartnersuper();
	return E_businesspartnersuper;
}());
var C_businesspartnersuper = (function () {
	function C_businesspartnersuper() {
		if (C_businesspartnersuper._checkObject) {
			throw new Error("Instantiation failed: Use C_businesspartnersuper.getInstance() instead of new.");
		}
		C_businesspartnersuper._checkObject = this;
		this._feldData = Meta_businesspartnersuper.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_businesspartnersuper.getInstance = function () {
		return this._checkObject;
	};

	C_businesspartnersuper.prototype.calc_tax = function (controller) {
		// Berechnung für das Feld '/BusinessPartnerRoot/Employment/tax'
		var start = [1, 1, 1];
		var ende = [1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income").isField(true).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/BusinessPartnerRoot/Employment/TaxComputation'
					if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert(controller.runden(controller.feldWertAlsZahl(idx, rtIdCon2).multiply(controller.makeDecimal("0.20")), 2), id);
						continue;
					}
				} catch (e) {
					var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
					controller.markiereAlsFehlerhaftBerechnet(id);
					continue;
				}

			}
		}
		catch (e) {
			throw new ValidatorException(e.message, e);
		}
	};


	C_businesspartnersuper.CALCULATED_FIELD_DEPENDENCIES = new Map([
		["/BusinessPartnerRoot/Employment/tax", new Set(["/BusinessPartnerRoot/Employment/income"])]
	]);

	C_businesspartnersuper.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set([
		"/BusinessPartnerRoot/Employment/income"
	]);

	C_businesspartnersuper.CALC_FUNC_NAME_MAP = new Map([
		["/BusinessPartnerRoot/Employment/tax", "calc_tax"]
	]);

	C_businesspartnersuper.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [
		"/BusinessPartnerRoot/Employment/tax"
	];

	C_businesspartnersuper.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_businesspartnersuper.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_businesspartnersuper.prototype.hasCalculation = function(fullFieldName) {
        return C_businesspartnersuper.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_businesspartnersuper.prototype.getDependencyGraph = function() {
        return C_businesspartnersuper.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_businesspartnersuper.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_businesspartnersuper.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_businesspartnersuper.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_businesspartnersuper.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_businesspartnersuper._checkObject = new C_businesspartnersuper();
	return C_businesspartnersuper;
}());
var R_businesspartnersuper = (function () {
	function R_businesspartnersuper() {
		this._feldData = Meta_businesspartnersuper.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_businesspartnersuper.getInstance = function () {
		return R_businesspartnersuper._checkObject;
	};
	R_businesspartnersuper.prototype.mvk_AttachmentInternalFilenameRequired = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("internal_filename").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						var tb0 = controller.alleKontexteAngegeben(idx, allRtIdCon1);
						var tb1 = controller.keinFeldAngegeben(idx, allRtIdCon2);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("internal_filename", idx, "ErrorR29", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("internal_filename", iter.get(), "ErrorR29", e);
			}
		}
	};
	R_businesspartnersuper.prototype.mvk_AttachmentMimeTypeRequired = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("mime_type").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						var tb0 = controller.alleKontexteAngegeben(idx, allRtIdCon1);
						var tb1 = controller.keinFeldAngegeben(idx, allRtIdCon2);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("mime_type", idx, "ErrorR30", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("mime_type", iter.get(), "ErrorR30", e);
			}
		}
	};
	R_businesspartnersuper.prototype.mvk_AttachmentIdOrContentFilled = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("attachment_id").isField(true).idx(1).idx(-1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("content").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).add(rtIdCon3).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleKontexteAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.nichtGenauEinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						var tb0 = controller.alleKontexteAngegeben(idx, allRtIdCon1);
						var tb1 = controller.nichtGenauEinFeldAngegeben(idx, allRtIdCon2);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("content", idx, "ErrorR31", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("content", iter.get(), "ErrorR31", e);
			}
		}
	};
	R_businesspartnersuper.prototype.mvk_SizeOfContentFilled = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Attachment/SizeOfContentFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("content").isField(true).idx(1).idx(-1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("size").isField(true).idx(1).idx(-1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.keinFeldAngegeben(idx, allRtIdCon2);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("content", idx, "ErrorR32", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("content", iter.get(), "ErrorR32", e);
			}
		}
	};
	R_businesspartnersuper.prototype.mvk_TaxComputation = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Employment/TaxComputation", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income").isField(true).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.vergleiche(VglOp.NE, controller.feldWertAlsZahl(idx, rtIdCon1), controller.runden(controller.feldWertAlsZahl(idx, rtIdCon2).multiply(controller.makeDecimal("0.20")), 2)).isKnownAndTrue()) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.alleFelderAngegeben(idx, allRtIdCon2);
						var tb2 = controller.vergleiche(VglOp.NE, controller.feldWertAlsZahl(idx, rtIdCon1), controller.runden(controller.feldWertAlsZahl(idx, rtIdCon2).multiply(controller.makeDecimal("0.20")), 2));
						var valErg = tb0.combineUND(tb1).combineUND(tb2);
						controller.addValidatorMessage("tax", idx, "TaxComputation", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("tax", iter.get(), "TaxComputation", e);
			}
		}
	};


	R_businesspartnersuper.vordruckF__BusinessPartnerRoot = function (controller) {
		/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruckF__BusinessPartnerRoot_lfdNr(controller, indices);
				}
			}
		}
	};
	R_businesspartnersuper.vordruckF__BusinessPartnerRoot_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentInternalFilenameRequired(controller, indices);
		controller.logMessung("mvk_AttachmentInternalFilenameRequired", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentMimeTypeRequired(controller, indices);
		controller.logMessung("mvk_AttachmentMimeTypeRequired", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentIdOrContentFilled(controller, indices);
		controller.logMessung("mvk_AttachmentIdOrContentFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_SizeOfContentFilled(controller, indices);
		controller.logMessung("mvk_SizeOfContentFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_TaxComputation(controller, indices);
		controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);
	};
	R_businesspartnersuper.vordruck__BusinessPartnerRoot = function (controller) {
		/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("BusinessPartnerRoot", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruck__BusinessPartnerRoot_lfdNr(controller, indices);
				}
			}
		}
	};
	R_businesspartnersuper.vordruck__BusinessPartnerRoot_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck BusinessPartnerRoot für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentInternalFilenameRequired(controller, indices);
		controller.logMessung("mvk_AttachmentInternalFilenameRequired", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentMimeTypeRequired(controller, indices);
		controller.logMessung("mvk_AttachmentMimeTypeRequired", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentIdOrContentFilled(controller, indices);
		controller.logMessung("mvk_AttachmentIdOrContentFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_SizeOfContentFilled(controller, indices);
		controller.logMessung("mvk_SizeOfContentFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_TaxComputation(controller, indices);
		controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);
	};



	R_businesspartnersuper.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_businesspartnersuper["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_businesspartnersuper, controller);
		}
	};

	R_businesspartnersuper.prototype.validatePreliminaryRules = function(controller) {
};

	R_businesspartnersuper.prototype.validiereVoll = function (controller, modus) {
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
	R_businesspartnersuper.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_businesspartnersuper();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_businesspartnersuper();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_businesspartnersuper();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_businesspartnersuper();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_businesspartnersuper();
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
	R_businesspartnersuper.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_businesspartnersuper.vordruck__BusinessPartnerRoot(controller);
};
	R_businesspartnersuper.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_businesspartnersuper.vordruckF__BusinessPartnerRoot(controller);
};
	R_businesspartnersuper.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_businesspartnersuper.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_businesspartnersuper._checkObject = new R_businesspartnersuper();
	return R_businesspartnersuper;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_businesspartnersuper = (function () {
	function TVCheckFehler_businesspartnersuper() {
		this._checkObject = R_businesspartnersuper.getInstance();
	}
	TVCheckFehler_businesspartnersuper.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 13) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [1,13]
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

	TVCheckFehler_businesspartnersuper.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
		var startMesspunkt = -1;
		switch (interneFeldNr) {
			case 1:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentInternalFilenameRequired(controller, indices);
				controller.logMessung("mvk_AttachmentInternalFilenameRequired", indices[0], startMesspunkt);
				break;
			case 2:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentIdOrContentFilled(controller, indices);
				controller.logMessung("mvk_AttachmentIdOrContentFilled", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_SizeOfContentFilled(controller, indices);
				controller.logMessung("mvk_SizeOfContentFilled", indices[0], startMesspunkt);
				break;
			case 5:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentMimeTypeRequired(controller, indices);
				controller.logMessung("mvk_AttachmentMimeTypeRequired", indices[0], startMesspunkt);
				break;
			case 13:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_TaxComputation(controller, indices);
				controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 13) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [1,13].");
			}
			break;
		}
	};

	return TVCheckFehler_businesspartnersuper;
}());
var TVCheckHinweis_businesspartnersuper = (function () {
	function TVCheckHinweis_businesspartnersuper() {
		this._checkObject = R_businesspartnersuper.getInstance();
	}
	TVCheckHinweis_businesspartnersuper.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_businesspartnersuper;
}());
var TVCheckInfo_businesspartnersuper = (function () {
	function TVCheckInfo_businesspartnersuper() {
		this._checkObject = R_businesspartnersuper.getInstance();
	}
	TVCheckInfo_businesspartnersuper.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_businesspartnersuper;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_businesspartnersuper = (function () {
	function TVCheckAlle_businesspartnersuper() {
		this._tvFehlerChecker = new TVCheckFehler_businesspartnersuper();
		this._tvHinweisChecker = new TVCheckHinweis_businesspartnersuper();
		this._tvInfoChecker = new TVCheckInfo_businesspartnersuper();
	}
	TVCheckAlle_businesspartnersuper.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_businesspartnersuper;
}());
var TVCheckPreliminary_businesspartnersuper = (function () {
	function TVCheckPreliminary_businesspartnersuper() {
		this._checkObject = R_businesspartnersuper.getInstance();
	}
	TVCheckPreliminary_businesspartnersuper.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_businesspartnersuper;
}());
var ERValidator_businesspartnersuper = (function () {
	function ERValidator_businesspartnersuper() {
		if (ERValidator_businesspartnersuper._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_businesspartnersuper.getInstance() instead of new.");
		}
		ERValidator_businesspartnersuper._instance = this;
	}
	ERValidator_businesspartnersuper.getInstance = function () {
		return ERValidator_businesspartnersuper._instance;
	};
	ERValidator_businesspartnersuper.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_businesspartnersuper.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_businesspartnersuper.getInstance(), modus, ERValidator_businesspartnersuper._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_businesspartnersuper.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_businesspartnersuper.getInstance(), modus, ERValidator_businesspartnersuper._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_businesspartnersuper.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_businesspartnersuper.getInstance(), modus, ERValidator_businesspartnersuper._metaData, logger, C_businesspartnersuper.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_businesspartnersuper._instance = new ERValidator_businesspartnersuper();
	ERValidator_businesspartnersuper._metaData = Meta_businesspartnersuper.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_businesspartnersuper._constructor = (function () {
	})();
	return ERValidator_businesspartnersuper;
}());

return {
	validator: ERValidator_businesspartnersuper,
	meta: Meta_businesspartnersuper
};
