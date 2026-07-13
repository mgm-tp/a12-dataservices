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
var Meta_Felder_recursivebusinesspartnercdm = (function () {
	function Meta_Felder_recursivebusinesspartnercdm() {
		this._felder = new Array();
		this._metaFormate = new Map();
		this._mehrsprachigeFehlertexte = new Map();
		this._errorTexts4Req = new Map();
		this._enumerationOberflaechenWerte = new Map();
		this._enumerationCategoryValues = new Map();
		this._stringHintLists = new Map();
		this._initFeldFehlertexte();
		this._initEnumerationOberflaechenWerte();
		this._initEnumerationCategoryValues();
		this._initFeldtypen();
		this._initFelder();
	}
	Meta_Felder_recursivebusinesspartnercdm.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_recursivebusinesspartnercdm.prototype._initFeldtypen = function () {
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
		new FormatDefinitionZahl(
			true, // Vorzeichen
			false, // nullErlaubt
			false, // fuehrendeNullenErlaubt
			-1, // Vorkomma
			0, // Nachkomma
			0, // NachkommaBis
			-1, // minLaenge
			1, // minLaengeDisp
			-1, // maxLaenge
			16, // maxLaengeDisp
			false, // Geldbetrag
			"1", // MinWert
			"" // MaxWert

		)
	);
	this._metaFormate.set(
		8,
		new FormatDefinitionZahl(
			true, // Vorzeichen
			true, // nullErlaubt
			false, // fuehrendeNullenErlaubt
			13, // Vorkomma
			2, // Nachkomma
			2, // NachkommaBis
			-1, // minLaenge
			4, // minLaengeDisp
			-1, // maxLaenge
			17, // maxLaengeDisp
			true, // Geldbetrag
			"", // MinWert
			"" // MaxWert

		)
	);
	this._metaFormate.set(
		9,
		new FormatDefinitionJaNein(
			"true", // jaWert
			"false" // neinWert

		)
	);
	this._metaFormate.set(
		10,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(10),
			["Household", "Travel", "Health", "Liability"],
			this._enumerationOberflaechenWerte.get(10),
			this._enumerationCategoryValues.get(10),
			""
		)
	);
	this._metaFormate.set(
		11,
		new FormatDefinitionDatum(
			"yyyy-MM-dd'T'HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		12,
		new FormatDefinitionDatum(
			"HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		13,
		new FormatDefinitionDatum(
			"yyyy", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		14,
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.JAHR_OPTIONAL // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		15,
		new FormatDefinitionDatumBereich(
			"yyyy-MM-dd", // DatumFormat
			false, // Zusatzprüfung
			"/", // Bereichstrenner
			Jahresinterpretation.Standard // für Formate TT.MM-TT.MM (bis/von ist Referenzjahr)
		)
	);
	this._metaFormate.set(
		16,
		new FormatDefinitionString(
			-1, // minLaenge
			99999, // maxLaenge
			19, // minLaengePrintable
			19, // maxLaengePrintable
			"[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}", // muster
			this._mehrsprachigeFehlertexte.get(16), // fehlertexte
			false, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(16)
		)
	);
	this._metaFormate.set(
		17,
		new FormatDefinitionZahl(
			true, // Vorzeichen
			false, // nullErlaubt
			false, // fuehrendeNullenErlaubt
			15, // Vorkomma
			0, // Nachkomma
			0, // NachkommaBis
			-1, // minLaenge
			1, // minLaengeDisp
			-1, // maxLaenge
			16, // maxLaengeDisp
			true, // Geldbetrag
			"", // MinWert
			"" // MaxWert

		)
	);
	this._metaFormate.set(
		18,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(18),
			["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"],
			this._enumerationOberflaechenWerte.get(18),
			this._enumerationCategoryValues.get(18),
			""
		)
	);
	this._metaFormate.set(
		19,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(19),
			["trivial", "low", "medium", "high", "critical"],
			this._enumerationOberflaechenWerte.get(19),
			this._enumerationCategoryValues.get(19),
			""
		)
	);
	this._metaFormate.set(
		20,
		new FormatDefinitionDatum(
			"dd.MM.yyyy", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		21,
		new FormatDefinitionDatum(
			"yyyy-MM", // Datumsformat
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
	Meta_Felder_recursivebusinesspartnercdm.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"original_filename",
			"/BusinessPartnerRoot/Attachment/original_filename",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmoriginal_filenameValidate",
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
			"recursivebusinesspartnercdminternal_filenameValidate",
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
			"recursivebusinesspartnercdmcontentValidate",
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
			"recursivebusinesspartnercdmattachment_idValidate",
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
			"recursivebusinesspartnercdmsizeValidate",
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
			"recursivebusinesspartnercdmmime_typeValidate",
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
			"recursivebusinesspartnercdmcategoryValidate",
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
			"recursivebusinesspartnercdmdescriptionValidate",
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
			"recursivebusinesspartnercdmNameValidate",
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
			"recursivebusinesspartnercdmIndustryValidate",
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
			"recursivebusinesspartnercdmStartOfRelationshipValidate",
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
			"recursivebusinesspartnercdmCustomerDiscountValidate",
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
			"recursivebusinesspartnercdmincomeValidate",
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
			"recursivebusinesspartnercdmtaxValidate",
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
			"recursivebusinesspartnercdmPersonOrEntityValidate",
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
			"ContractName_1",
			"/ContractBusinessPartner/ContractRoot/ContractName",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmContractName_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Contract Name").put("de", "Vertragsname").build())
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
			"ContractDescription_1",
			"/ContractBusinessPartner/ContractRoot/ContractDescription",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmContractDescription_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Contract Description").put("de", "Vertragsbeschreibung").build())
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
			"LengthOfContract_1",
			"/ContractBusinessPartner/ContractRoot/LengthOfContract",
			false,
			false,
			this._metaFormate.get(7),
			"recursivebusinesspartnercdmLengthOfContract_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Length of contract").put("de", "Vertragsdauer").build())
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
			"ContractValue_1",
			"/ContractBusinessPartner/ContractRoot/ContractValue",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmContractValue_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Contract Value").put("de", "Vertragswert").build())
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
			"Liability_1",
			"/ContractBusinessPartner/ContractRoot/Liability",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmLiability_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Amount of liability accepted").put("de", "Höhe der übernommenen Haftung").build())
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
			"CostToCustomer_1",
			"/ContractBusinessPartner/ContractRoot/CostToCustomer",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmCostToCustomer_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Cost to Customer").put("de", "Kosten für den Kunden").build())
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
			"NoOfCoInsuredCustomers_1",
			"/ContractBusinessPartner/ContractRoot/NoOfCoInsuredCustomers",
			false,
			false,
			this._metaFormate.get(2),
			"recursivebusinesspartnercdmNoOfCoInsuredCustomers_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Number of Co-Insured Customers").put("de", "Anzahl der mitversicherten Kunden").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
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
			"MaxDiscount_1",
			"/ContractBusinessPartner/ContractRoot/MaxDiscount",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmMaxDiscount_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Highest Discount amongst Co-Insured Policy Holders").put("de", "Höchster Rabatt unter den mitversicherten Versicherungsnehmern").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(22)
		)
	);
	this._felder.push(
		new Feld_t(
			23,
			"CostPerCoInsured_1",
			"/ContractBusinessPartner/ContractRoot/CostPerCoInsured",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmCostPerCoInsured_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Cost per Co-Insured Policy Holder").put("de", "Kosten pro mitversichertem Versicherungsnehmer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(23)
		)
	);
	this._felder.push(
		new Feld_t(
			24,
			"Valid_1",
			"/ContractBusinessPartner/ContractRoot/Valid",
			false,
			false,
			this._metaFormate.get(9),
			"recursivebusinesspartnercdmValid_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Valid").put("de", "Gültig").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(24)
		)
	);
	this._felder.push(
		new Feld_t(
			25,
			"Type_1",
			"/ContractBusinessPartner/ContractRoot/Type",
			false,
			false,
			this._metaFormate.get(10),
			"recursivebusinesspartnercdmType_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Insurance type").put("de", "Versicherungsart").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(25)
		)
	);
	this._felder.push(
		new Feld_t(
			26,
			"ContractStartDate_1",
			"/ContractBusinessPartner/ContractRoot/ContractDates/ContractStartDate",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmContractStartDate_1Validate",
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
			this._errorTexts4Req.get(26)
		)
	);
	this._felder.push(
		new Feld_t(
			27,
			"ContractEndDate_1",
			"/ContractBusinessPartner/ContractRoot/ContractDates/ContractEndDate",
			false,
			false,
			this._metaFormate.get(4),
			"recursivebusinesspartnercdmContractEndDate_1Validate",
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
			this._errorTexts4Req.get(27)
		)
	);
	this._felder.push(
		new Feld_t(
			28,
			"ContractEndTime_1",
			"/ContractBusinessPartner/ContractRoot/ContractDates/ContractEndTime",
			false,
			false,
			this._metaFormate.get(12),
			"recursivebusinesspartnercdmContractEndTime_1Validate",
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
			this._errorTexts4Req.get(28)
		)
	);
	this._felder.push(
		new Feld_t(
			29,
			"ContractReviewDate_1",
			"/ContractBusinessPartner/ContractRoot/ContractDates/ContractReviewDate",
			false,
			false,
			this._metaFormate.get(13),
			"recursivebusinesspartnercdmContractReviewDate_1Validate",
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
			this._errorTexts4Req.get(29)
		)
	);
	this._felder.push(
		new Feld_t(
			30,
			"LastPremiumPaidDate_1",
			"/ContractBusinessPartner/ContractRoot/ContractDates/LastPremiumPaidDate",
			false,
			false,
			this._metaFormate.get(14),
			"recursivebusinesspartnercdmLastPremiumPaidDate_1Validate",
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
			this._errorTexts4Req.get(30)
		)
	);
	this._felder.push(
		new Feld_t(
			31,
			"CoveragePeriod_1",
			"/ContractBusinessPartner/ContractRoot/ContractDates/CoveragePeriod",
			false,
			false,
			this._metaFormate.get(15),
			"recursivebusinesspartnercdmCoveragePeriod_1Validate",
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
			this._errorTexts4Req.get(31)
		)
	);
	this._felder.push(
		new Feld_t(
			32,
			"ID_5",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/ID",
			false,
			false,
			this._metaFormate.get(16),
			"recursivebusinesspartnercdmID_5Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("de", "ID").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(32)
		)
	);
	this._felder.push(
		new Feld_t(
			33,
			"Number_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Number",
			false,
			false,
			this._metaFormate.get(17),
			"recursivebusinesspartnercdmNumber_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Number").put("de", "Nummer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(33)
		)
	);
	this._felder.push(
		new Feld_t(
			34,
			"ChangeTimestamp_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/ChangeTimestamp",
			false,
			false,
			this._metaFormate.get(4),
			"recursivebusinesspartnercdmChangeTimestamp_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Change Timestamp").put("de", "Zeitstempel ändern").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(34)
		)
	);
	this._felder.push(
		new Feld_t(
			35,
			"Description_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Description",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmDescription_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Description").put("de", "Beschreibung").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(35)
		)
	);
	this._felder.push(
		new Feld_t(
			36,
			"User_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/User",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmUser_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "User").put("de", "Benutzer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(36)
		)
	);
	this._felder.push(
		new Feld_t(
			37,
			"Status_5",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Status",
			false,
			false,
			this._metaFormate.get(18),
			"recursivebusinesspartnercdmStatus_5Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("de", "Status").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(37)
		)
	);
	this._felder.push(
		new Feld_t(
			38,
			"Priority_5",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Priority",
			false,
			false,
			this._metaFormate.get(19),
			"recursivebusinesspartnercdmPriority_5Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("de", "Priorität").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(38)
		)
	);
	this._felder.push(
		new Feld_t(
			39,
			"ID_3",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/ID",
			false,
			false,
			this._metaFormate.get(16),
			"recursivebusinesspartnercdmID_3Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("de", "ID").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(39)
		)
	);
	this._felder.push(
		new Feld_t(
			40,
			"Title_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Title",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmTitle_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Title").put("de", "Titel").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(40)
		)
	);
	this._felder.push(
		new Feld_t(
			41,
			"Details_2",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Details",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmDetails_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Details").put("de", "Details").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(41)
		)
	);
	this._felder.push(
		new Feld_t(
			42,
			"Priority_3",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Priority",
			false,
			false,
			this._metaFormate.get(19),
			"recursivebusinesspartnercdmPriority_3Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("de", "Priorität").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(42)
		)
	);
	this._felder.push(
		new Feld_t(
			43,
			"Status_3",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/Status",
			false,
			false,
			this._metaFormate.get(18),
			"recursivebusinesspartnercdmStatus_3Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("de", "Status").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(43)
		)
	);
	this._felder.push(
		new Feld_t(
			44,
			"LastModified_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/LastModified",
			false,
			false,
			this._metaFormate.get(20),
			"recursivebusinesspartnercdmLastModified_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Last Modified").put("de", "Zuletzt geändert").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(44)
		)
	);
	this._felder.push(
		new Feld_t(
			45,
			"ID_4",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/ID",
			false,
			false,
			this._metaFormate.get(16),
			"recursivebusinesspartnercdmID_4Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("de", "ID").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(45)
		)
	);
	this._felder.push(
		new Feld_t(
			46,
			"TItle_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/TItle",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmTItle_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "TItle").put("de", "Titel").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(46)
		)
	);
	this._felder.push(
		new Feld_t(
			47,
			"Details_3",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/Details",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmDetails_3Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Details").put("de", "Details").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(47)
		)
	);
	this._felder.push(
		new Feld_t(
			48,
			"Priority_4",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/Priority",
			false,
			false,
			this._metaFormate.get(19),
			"recursivebusinesspartnercdmPriority_4Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("de", "Priorität").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(48)
		)
	);
	this._felder.push(
		new Feld_t(
			49,
			"Status_4",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/Status",
			false,
			false,
			this._metaFormate.get(18),
			"recursivebusinesspartnercdmStatus_4Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("de", "Status").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(49)
		)
	);
	this._felder.push(
		new Feld_t(
			50,
			"CreatedAt_1",
			"/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges/CreatedAt",
			false,
			false,
			this._metaFormate.get(21),
			"recursivebusinesspartnercdmCreatedAt_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Last Modified").put("de", "Zuletzt geändert").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(50)
		)
	);
	this._felder.push(
		new Feld_t(
			51,
			"ContractName_0",
			"/ContractBusinessPartner/ContractAmendment/ContractName",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmContractName_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Contract Name").put("de", "Vertragsname").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(51)
		)
	);
	this._felder.push(
		new Feld_t(
			52,
			"ContractDescription_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDescription",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmContractDescription_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Contract Description").put("de", "Vertragsbeschreibung").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(52)
		)
	);
	this._felder.push(
		new Feld_t(
			53,
			"LengthOfContract_0",
			"/ContractBusinessPartner/ContractAmendment/LengthOfContract",
			false,
			false,
			this._metaFormate.get(7),
			"recursivebusinesspartnercdmLengthOfContract_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Length of contract").put("de", "Vertragsdauer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(53)
		)
	);
	this._felder.push(
		new Feld_t(
			54,
			"ContractValue_0",
			"/ContractBusinessPartner/ContractAmendment/ContractValue",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmContractValue_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Contract Value").put("de", "Vertragswert").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(54)
		)
	);
	this._felder.push(
		new Feld_t(
			55,
			"Liability_0",
			"/ContractBusinessPartner/ContractAmendment/Liability",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmLiability_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Amount of liability accepted").put("de", "Höhe der übernommenen Haftung").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(55)
		)
	);
	this._felder.push(
		new Feld_t(
			56,
			"CostToCustomer_0",
			"/ContractBusinessPartner/ContractAmendment/CostToCustomer",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmCostToCustomer_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Cost to Customer").put("de", "Kosten für den Kunden").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(56)
		)
	);
	this._felder.push(
		new Feld_t(
			57,
			"NoOfCoInsuredCustomers_0",
			"/ContractBusinessPartner/ContractAmendment/NoOfCoInsuredCustomers",
			false,
			false,
			this._metaFormate.get(2),
			"recursivebusinesspartnercdmNoOfCoInsuredCustomers_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Number of Co-Insured Customers").put("de", "Anzahl der mitversicherten Kunden").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(57)
		)
	);
	this._felder.push(
		new Feld_t(
			58,
			"MaxDiscount_0",
			"/ContractBusinessPartner/ContractAmendment/MaxDiscount",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmMaxDiscount_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Highest Discount amongst Co-Insured Policy Holders").put("de", "Höchster Rabatt unter den mitversicherten Versicherungsnehmern").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(58)
		)
	);
	this._felder.push(
		new Feld_t(
			59,
			"CostPerCoInsured_0",
			"/ContractBusinessPartner/ContractAmendment/CostPerCoInsured",
			false,
			false,
			this._metaFormate.get(8),
			"recursivebusinesspartnercdmCostPerCoInsured_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Cost per Co-Insured Policy Holder").put("de", "Kosten pro mitversichertem Versicherungsnehmer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(59)
		)
	);
	this._felder.push(
		new Feld_t(
			60,
			"Valid_0",
			"/ContractBusinessPartner/ContractAmendment/Valid",
			false,
			false,
			this._metaFormate.get(9),
			"recursivebusinesspartnercdmValid_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Valid").put("de", "Gültig").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(60)
		)
	);
	this._felder.push(
		new Feld_t(
			61,
			"Type_0",
			"/ContractBusinessPartner/ContractAmendment/Type",
			false,
			false,
			this._metaFormate.get(10),
			"recursivebusinesspartnercdmType_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Insurance type").put("de", "Versicherungsart").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(61)
		)
	);
	this._felder.push(
		new Feld_t(
			62,
			"ContractStartDate_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDates/ContractStartDate",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmContractStartDate_0Validate",
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
			this._errorTexts4Req.get(62)
		)
	);
	this._felder.push(
		new Feld_t(
			63,
			"ContractEndDate_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDates/ContractEndDate",
			false,
			false,
			this._metaFormate.get(4),
			"recursivebusinesspartnercdmContractEndDate_0Validate",
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
			this._errorTexts4Req.get(63)
		)
	);
	this._felder.push(
		new Feld_t(
			64,
			"ContractEndTime_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDates/ContractEndTime",
			false,
			false,
			this._metaFormate.get(12),
			"recursivebusinesspartnercdmContractEndTime_0Validate",
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
			this._errorTexts4Req.get(64)
		)
	);
	this._felder.push(
		new Feld_t(
			65,
			"ContractReviewDate_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDates/ContractReviewDate",
			false,
			false,
			this._metaFormate.get(13),
			"recursivebusinesspartnercdmContractReviewDate_0Validate",
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
			this._errorTexts4Req.get(65)
		)
	);
	this._felder.push(
		new Feld_t(
			66,
			"LastPremiumPaidDate_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDates/LastPremiumPaidDate",
			false,
			false,
			this._metaFormate.get(14),
			"recursivebusinesspartnercdmLastPremiumPaidDate_0Validate",
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
			this._errorTexts4Req.get(66)
		)
	);
	this._felder.push(
		new Feld_t(
			67,
			"CoveragePeriod_0",
			"/ContractBusinessPartner/ContractAmendment/ContractDates/CoveragePeriod",
			false,
			false,
			this._metaFormate.get(15),
			"recursivebusinesspartnercdmCoveragePeriod_0Validate",
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
			this._errorTexts4Req.get(67)
		)
	);
	this._felder.push(
		new Feld_t(
			68,
			"ID_2",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/ID",
			false,
			false,
			this._metaFormate.get(16),
			"recursivebusinesspartnercdmID_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("de", "ID").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(68)
		)
	);
	this._felder.push(
		new Feld_t(
			69,
			"Number_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Number",
			false,
			false,
			this._metaFormate.get(17),
			"recursivebusinesspartnercdmNumber_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Number").put("de", "Nummer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(69)
		)
	);
	this._felder.push(
		new Feld_t(
			70,
			"ChangeTimestamp_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/ChangeTimestamp",
			false,
			false,
			this._metaFormate.get(4),
			"recursivebusinesspartnercdmChangeTimestamp_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Change Timestamp").put("de", "Zeitstempel ändern").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(70)
		)
	);
	this._felder.push(
		new Feld_t(
			71,
			"Description_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Description",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmDescription_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Description").put("de", "Beschreibung").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(71)
		)
	);
	this._felder.push(
		new Feld_t(
			72,
			"User_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/User",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmUser_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "User").put("de", "Benutzer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(72)
		)
	);
	this._felder.push(
		new Feld_t(
			73,
			"Status_2",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Status",
			false,
			false,
			this._metaFormate.get(18),
			"recursivebusinesspartnercdmStatus_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("de", "Status").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(73)
		)
	);
	this._felder.push(
		new Feld_t(
			74,
			"Priority_2",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Priority",
			false,
			false,
			this._metaFormate.get(19),
			"recursivebusinesspartnercdmPriority_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("de", "Priorität").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(74)
		)
	);
	this._felder.push(
		new Feld_t(
			75,
			"ID_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/ID",
			false,
			false,
			this._metaFormate.get(16),
			"recursivebusinesspartnercdmID_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("de", "ID").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(75)
		)
	);
	this._felder.push(
		new Feld_t(
			76,
			"Title_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Title",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmTitle_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Title").put("de", "Titel").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(76)
		)
	);
	this._felder.push(
		new Feld_t(
			77,
			"Details_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Details",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmDetails_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Details").put("de", "Details").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(77)
		)
	);
	this._felder.push(
		new Feld_t(
			78,
			"Priority_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Priority",
			false,
			false,
			this._metaFormate.get(19),
			"recursivebusinesspartnercdmPriority_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("de", "Priorität").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(78)
		)
	);
	this._felder.push(
		new Feld_t(
			79,
			"Status_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/Status",
			false,
			false,
			this._metaFormate.get(18),
			"recursivebusinesspartnercdmStatus_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("de", "Status").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(79)
		)
	);
	this._felder.push(
		new Feld_t(
			80,
			"LastModified_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/LastModified",
			false,
			false,
			this._metaFormate.get(20),
			"recursivebusinesspartnercdmLastModified_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Last Modified").put("de", "Zuletzt geändert").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(80)
		)
	);
	this._felder.push(
		new Feld_t(
			81,
			"ID_1",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/ID",
			false,
			false,
			this._metaFormate.get(16),
			"recursivebusinesspartnercdmID_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("de", "ID").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(81)
		)
	);
	this._felder.push(
		new Feld_t(
			82,
			"TItle_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/TItle",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmTItle_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "TItle").put("de", "Titel").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(82)
		)
	);
	this._felder.push(
		new Feld_t(
			83,
			"Details_1",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/Details",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmDetails_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Details").put("de", "Details").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(83)
		)
	);
	this._felder.push(
		new Feld_t(
			84,
			"Priority_1",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/Priority",
			false,
			false,
			this._metaFormate.get(19),
			"recursivebusinesspartnercdmPriority_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("de", "Priorität").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(84)
		)
	);
	this._felder.push(
		new Feld_t(
			85,
			"Status_1",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/Status",
			false,
			false,
			this._metaFormate.get(18),
			"recursivebusinesspartnercdmStatus_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("de", "Status").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(85)
		)
	);
	this._felder.push(
		new Feld_t(
			86,
			"CreatedAt_0",
			"/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges/CreatedAt",
			false,
			false,
			this._metaFormate.get(21),
			"recursivebusinesspartnercdmCreatedAt_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Last Modified").put("de", "Zuletzt geändert").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(86)
		)
	);
	this._felder.push(
		new Feld_t(
			87,
			"docRef_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmdocRef_0Validate",
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
			this._errorTexts4Req.get(87)
		)
	);
	this._felder.push(
		new Feld_t(
			88,
			"modelReference_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodelReference_0Validate",
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
			this._errorTexts4Req.get(88)
		)
	);
	this._felder.push(
		new Feld_t(
			89,
			"modelVersion_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodelVersion_0Validate",
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
			this._errorTexts4Req.get(89)
		)
	);
	this._felder.push(
		new Feld_t(
			90,
			"creator_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmcreator_0Validate",
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
			this._errorTexts4Req.get(90)
		)
	);
	this._felder.push(
		new Feld_t(
			91,
			"createdAt_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmcreatedAt_0Validate",
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
			this._errorTexts4Req.get(91)
		)
	);
	this._felder.push(
		new Feld_t(
			92,
			"modifier_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodifier_0Validate",
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
			this._errorTexts4Req.get(92)
		)
	);
	this._felder.push(
		new Feld_t(
			93,
			"modifiedAt_0",
			"/ContractBusinessPartner/ContractAmendment/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmmodifiedAt_0Validate",
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
			this._errorTexts4Req.get(93)
		)
	);
	this._felder.push(
		new Feld_t(
			94,
			"docRef_1",
			"/ContractBusinessPartner/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmdocRef_1Validate",
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
			this._errorTexts4Req.get(94)
		)
	);
	this._felder.push(
		new Feld_t(
			95,
			"modelReference_1",
			"/ContractBusinessPartner/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodelReference_1Validate",
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
			this._errorTexts4Req.get(95)
		)
	);
	this._felder.push(
		new Feld_t(
			96,
			"modelVersion_1",
			"/ContractBusinessPartner/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodelVersion_1Validate",
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
			this._errorTexts4Req.get(96)
		)
	);
	this._felder.push(
		new Feld_t(
			97,
			"creator_1",
			"/ContractBusinessPartner/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmcreator_1Validate",
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
			this._errorTexts4Req.get(97)
		)
	);
	this._felder.push(
		new Feld_t(
			98,
			"createdAt_1",
			"/ContractBusinessPartner/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmcreatedAt_1Validate",
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
			this._errorTexts4Req.get(98)
		)
	);
	this._felder.push(
		new Feld_t(
			99,
			"modifier_1",
			"/ContractBusinessPartner/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodifier_1Validate",
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
			this._errorTexts4Req.get(99)
		)
	);
	this._felder.push(
		new Feld_t(
			100,
			"modifiedAt_1",
			"/ContractBusinessPartner/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmmodifiedAt_1Validate",
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
			this._errorTexts4Req.get(100)
		)
	);
	this._felder.push(
		new Feld_t(
			101,
			"docRef_2",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmdocRef_2Validate",
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
			this._errorTexts4Req.get(101)
		)
	);
	this._felder.push(
		new Feld_t(
			102,
			"modelReference_2",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodelReference_2Validate",
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
			this._errorTexts4Req.get(102)
		)
	);
	this._felder.push(
		new Feld_t(
			103,
			"modelVersion_2",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodelVersion_2Validate",
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
			this._errorTexts4Req.get(103)
		)
	);
	this._felder.push(
		new Feld_t(
			104,
			"creator_2",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmcreator_2Validate",
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
			this._errorTexts4Req.get(104)
		)
	);
	this._felder.push(
		new Feld_t(
			105,
			"createdAt_2",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmcreatedAt_2Validate",
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
			this._errorTexts4Req.get(105)
		)
	);
	this._felder.push(
		new Feld_t(
			106,
			"modifier_2",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"recursivebusinesspartnercdmmodifier_2Validate",
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
			this._errorTexts4Req.get(106)
		)
	);
	this._felder.push(
		new Feld_t(
			107,
			"modifiedAt_2",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(11),
			"recursivebusinesspartnercdmmodifiedAt_2Validate",
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
			this._errorTexts4Req.get(107)
		)
	);
	};
	Meta_Felder_recursivebusinesspartnercdm.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(3, new Map());

	this._mehrsprachigeFehlertexte.set(5, new Map());

	this._mehrsprachigeFehlertexte.set(10, new Map());

	this._mehrsprachigeFehlertexte.set(16, new Map());
	this._mehrsprachigeFehlertexte.get(16).set("de", "Muster wird nicht erfüllt");this._mehrsprachigeFehlertexte.get(16).set("en", "Pattern is not met");
	this._mehrsprachigeFehlertexte.set(18, new Map());

	this._mehrsprachigeFehlertexte.set(19, new Map());

	};
	Meta_Felder_recursivebusinesspartnercdm.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(3, new Map());
	this._enumerationOberflaechenWerte.get(3).set("de", MetaFeldUtil.createPairList(["Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(3).set("en", MetaFeldUtil.createPairList(["Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));

	this._enumerationOberflaechenWerte.set(5, new Map());
	this._enumerationOberflaechenWerte.get(5).set("de", MetaFeldUtil.createPairList(["0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(5).set("en", MetaFeldUtil.createPairList(["0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));

	this._enumerationOberflaechenWerte.set(10, new Map());
	this._enumerationOberflaechenWerte.get(10).set("de", MetaFeldUtil.createPairList(["Haushaltsversicherung", "Reiseversicherung", "Krankenversicherung", "Haftpflichtversicherung"], ["Household", "Travel", "Health", "Liability"]));
	this._enumerationOberflaechenWerte.get(10).set("en", MetaFeldUtil.createPairList(["Household Insurance", "Travel Insurance", "Health Insurance", "Liability Insurance"], ["Household", "Travel", "Health", "Liability"]));

	this._enumerationOberflaechenWerte.set(18, new Map());
	this._enumerationOberflaechenWerte.get(18).set("de", MetaFeldUtil.createPairList(["attrape", "entwurf", "uberpruft", "zuruckgesetzt", "genehmigt", "fertig", "bereitgestelt"], ["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"]));
	this._enumerationOberflaechenWerte.get(18).set("en", MetaFeldUtil.createPairList(["imitation", "blueprint", "examined", "returned", "accepted", "finished", "utilized"], ["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"]));

	this._enumerationOberflaechenWerte.set(19, new Map());
	this._enumerationOberflaechenWerte.get(19).set("de", MetaFeldUtil.createPairList(["banal", "niedrig", "medium", "hoch", "kritisch"], ["trivial", "low", "medium", "high", "critical"]));
	this._enumerationOberflaechenWerte.get(19).set("en", MetaFeldUtil.createPairList(["negligible", "small", "intermediate", "large", "demanding"], ["trivial", "low", "medium", "high", "critical"]));

	};
	Meta_Felder_recursivebusinesspartnercdm.prototype._initEnumerationCategoryValues = function () {
	this._enumerationCategoryValues.set(18, new MapBuilder().put("dummy", new MapBuilder().put("Status", "Status").build()).put("draft", new MapBuilder().put("Status", "Status").build()).put("reviewed", new MapBuilder().put("Status", "Status").build()).put("reverted", new MapBuilder().put("Status", "Status").build()).put("approved", new MapBuilder().put("Status", "Status").build()).put("done", new MapBuilder().put("Status", "Status").build()).put("deployed", new MapBuilder().put("Status", "Status").build()).build());

	};


	return Meta_Felder_recursivebusinesspartnercdm;
}());
var Meta_Regel_recursivebusinesspartnercdm = (function () {
	function Meta_Regel_recursivebusinesspartnercdm() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_recursivebusinesspartnercdm.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_recursivebusinesspartnercdm.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_recursivebusinesspartnercdm.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_recursivebusinesspartnercdm.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_recursivebusinesspartnercdm.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_recursivebusinesspartnercdm.ANZAHL_REGELN = 5;

	Meta_Regel_recursivebusinesspartnercdm.prototype._initRegelRefs = function () {
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

	Meta_Regel_recursivebusinesspartnercdm.prototype._initRegelFehlertexte = function () {
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
	Meta_Regel_recursivebusinesspartnercdm.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("AttachmentInternalFilenameRequired", "/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", "ErrorR29", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/BusinessPartnerRoot/Attachment/internal_filename", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentMimeTypeRequired", "/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", "ErrorR30", this._mehrsprachigeFehlertexte.get(1), this._fehlertexteParameterMapping.get(1), "Fehler", this._refIdentifier[1], this._refAuslassungsIdentifier[1], null, "/BusinessPartnerRoot/Attachment/mime_type", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentIdOrContentFilled", "/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", "ErrorR31", this._mehrsprachigeFehlertexte.get(2), this._fehlertexteParameterMapping.get(2), "Fehler", this._refIdentifier[2], this._refAuslassungsIdentifier[2], null, "/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("SizeOfContentFilled", "/BusinessPartnerRoot/Attachment/SizeOfContentFilled", "ErrorR32", this._mehrsprachigeFehlertexte.get(3), this._fehlertexteParameterMapping.get(3), "Fehler", this._refIdentifier[3], this._refAuslassungsIdentifier[3], null, "/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("TaxComputation", "/BusinessPartnerRoot/Employment/TaxComputation", "TaxComputation", this._mehrsprachigeFehlertexte.get(4), this._fehlertexteParameterMapping.get(4), "Fehler", this._refIdentifier[4], this._refAuslassungsIdentifier[4], null, "/BusinessPartnerRoot/Employment/tax", true, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_recursivebusinesspartnercdm;
}());
var Meta_PfFeld_recursivebusinesspartnercdm = (function () {
	function Meta_PfFeld_recursivebusinesspartnercdm(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_recursivebusinesspartnercdm.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		this._addLokalePflichtFeldInfos_0(infos);

		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};

	Meta_PfFeld_recursivebusinesspartnercdm.prototype._addLokalePflichtFeldInfos_0 = function (infos) {
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



	Meta_PfFeld_recursivebusinesspartnercdm.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_recursivebusinesspartnercdm;
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

var Meta_recursivebusinesspartnercdm = (function () {
	function Meta_recursivebusinesspartnercdm() {
		this._metaFelder = new Meta_Felder_recursivebusinesspartnercdm();
		this._metaRegeln = new Meta_Regel_recursivebusinesspartnercdm();
		this._unterstuetzteSprachen = ["en", "de"];
		this._da = new Model_t(
				"29.4.0", // Produkt-Version
				".", // dezimaltrenner
				["en", "de"], // unterstuetzte Sprachen
				MetaDataHelper.createSetOfStrings(), //  Namen der Applikationsbedingungen
				MetaDataHelper.createSetOfStrings(), // Names of custom field types
				// additional Values
				new MapBuilder()
					.put("BaseYear", null)
					.put("TimeZone", "UTC")
					.put("ModelId", "RecursiveBusinessPartnerCDM")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin")
					.put("cdm.queryRoot", "BusinessPartnerSuper").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_recursivebusinesspartnercdm._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_recursivebusinesspartnercdm.getInstance() instead of new.");
		}
		Meta_recursivebusinesspartnercdm._instance = this;
		var container = Meta_recursivebusinesspartnercdm._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_recursivebusinesspartnercdm.LEGAL_CHARACTERS,
			Meta_recursivebusinesspartnercdm.LEGAL_GRAPHEME_TRIE,
			() => E_recursivebusinesspartnercdm.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_recursivebusinesspartnercdm(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_recursivebusinesspartnercdm, "RUNTIME_VERSION", { get: function () { return "31.1"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_recursivebusinesspartnercdm, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_recursivebusinesspartnercdm, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_recursivebusinesspartnercdm, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_recursivebusinesspartnercdm, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_recursivebusinesspartnercdm.getInstance = function () {
		return Meta_recursivebusinesspartnercdm._instance;
	};
	Meta_recursivebusinesspartnercdm.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_recursivebusinesspartnercdm.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_recursivebusinesspartnercdm._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Attachment", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Employment", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner", 100, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "ContractBusinessPartner")
			.put("cdm.sourceRole", "Partner")
			.put("cdm.targetRole", "Contract")
			.put("cdm.targetDocumentModel", "Contract").build()).build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractRoot", 100, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractRoot/ContractDates", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractRoot/ChangeLog", 10000, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractRoot/ChangeLog/Changes", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment", 100, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "ContractAmendment")
			.put("cdm.sourceRole", "Contract")
			.put("cdm.targetRole", "Amendment")
			.put("cdm.targetDocumentModel", "Contract").build()).build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment/ContractDates", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment/ChangeLog", 10000, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/ContractAmendment/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_recursivebusinesspartnercdm.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_recursivebusinesspartnercdm.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_recursivebusinesspartnercdm._constructor = (function () {
		if (Meta_recursivebusinesspartnercdm.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_recursivebusinesspartnercdm.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_recursivebusinesspartnercdm._instance = new Meta_recursivebusinesspartnercdm();
	})();
	return Meta_recursivebusinesspartnercdm;
}());var E_recursivebusinesspartnercdm = (function () {
	function E_recursivebusinesspartnercdm() {
		if (E_recursivebusinesspartnercdm._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_recursivebusinesspartnercdm.getInstance() instead of new.");
		}
		E_recursivebusinesspartnercdm._valueCalcObject = this;
		this._feldData = Meta_recursivebusinesspartnercdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_recursivebusinesspartnercdm.getInstance = function () {
		return this._valueCalcObject;
	};


	E_recursivebusinesspartnercdm.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_recursivebusinesspartnercdm._valueCalcObject = new E_recursivebusinesspartnercdm();
	return E_recursivebusinesspartnercdm;
}());
var C_recursivebusinesspartnercdm = (function () {
	function C_recursivebusinesspartnercdm() {
		if (C_recursivebusinesspartnercdm._checkObject) {
			throw new Error("Instantiation failed: Use C_recursivebusinesspartnercdm.getInstance() instead of new.");
		}
		C_recursivebusinesspartnercdm._checkObject = this;
		this._feldData = Meta_recursivebusinesspartnercdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_recursivebusinesspartnercdm.getInstance = function () {
		return this._checkObject;
	};

	C_recursivebusinesspartnercdm.prototype.calc_tax = function (controller) {
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


	C_recursivebusinesspartnercdm.CALCULATED_FIELD_DEPENDENCIES = new Map([
		["/BusinessPartnerRoot/Employment/tax", new Set(["/BusinessPartnerRoot/Employment/income"])]
	]);

	C_recursivebusinesspartnercdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set([
		"/BusinessPartnerRoot/Employment/income"
	]);

	C_recursivebusinesspartnercdm.CALC_FUNC_NAME_MAP = new Map([
		["/BusinessPartnerRoot/Employment/tax", "calc_tax"]
	]);

	C_recursivebusinesspartnercdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [
		"/BusinessPartnerRoot/Employment/tax"
	];

	C_recursivebusinesspartnercdm.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_recursivebusinesspartnercdm.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_recursivebusinesspartnercdm.prototype.hasCalculation = function(fullFieldName) {
        return C_recursivebusinesspartnercdm.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_recursivebusinesspartnercdm.prototype.getDependencyGraph = function() {
        return C_recursivebusinesspartnercdm.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_recursivebusinesspartnercdm.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_recursivebusinesspartnercdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_recursivebusinesspartnercdm.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_recursivebusinesspartnercdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_recursivebusinesspartnercdm._checkObject = new C_recursivebusinesspartnercdm();
	return C_recursivebusinesspartnercdm;
}());
var R_recursivebusinesspartnercdm = (function () {
	function R_recursivebusinesspartnercdm() {
		this._feldData = Meta_recursivebusinesspartnercdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_recursivebusinesspartnercdm.getInstance = function () {
		return R_recursivebusinesspartnercdm._checkObject;
	};
	R_recursivebusinesspartnercdm.prototype.mvk_AttachmentInternalFilenameRequired = function (controller, indices) {
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
	R_recursivebusinesspartnercdm.prototype.mvk_AttachmentMimeTypeRequired = function (controller, indices) {
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
	R_recursivebusinesspartnercdm.prototype.mvk_AttachmentIdOrContentFilled = function (controller, indices) {
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
	R_recursivebusinesspartnercdm.prototype.mvk_SizeOfContentFilled = function (controller, indices) {
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
	R_recursivebusinesspartnercdm.prototype.mvk_TaxComputation = function (controller, indices) {
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


	R_recursivebusinesspartnercdm.vordruckF__BusinessPartnerRoot = function (controller) {
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
	R_recursivebusinesspartnercdm.vordruckF__BusinessPartnerRoot_lfdNr = function (controller, indices) {
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
	R_recursivebusinesspartnercdm.vordruck__BusinessPartnerRoot = function (controller) {
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
	R_recursivebusinesspartnercdm.vordruck__BusinessPartnerRoot_lfdNr = function (controller, indices) {
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



	R_recursivebusinesspartnercdm.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_recursivebusinesspartnercdm["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_recursivebusinesspartnercdm, controller);
		}
	};

	R_recursivebusinesspartnercdm.prototype.validatePreliminaryRules = function(controller) {
};

	R_recursivebusinesspartnercdm.prototype.validiereVoll = function (controller, modus) {
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
	R_recursivebusinesspartnercdm.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_recursivebusinesspartnercdm();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_recursivebusinesspartnercdm();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_recursivebusinesspartnercdm();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_recursivebusinesspartnercdm();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_recursivebusinesspartnercdm();
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
	R_recursivebusinesspartnercdm.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_recursivebusinesspartnercdm.vordruck__BusinessPartnerRoot(controller);
};
	R_recursivebusinesspartnercdm.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_recursivebusinesspartnercdm.vordruckF__BusinessPartnerRoot(controller);
};
	R_recursivebusinesspartnercdm.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_recursivebusinesspartnercdm.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_recursivebusinesspartnercdm._checkObject = new R_recursivebusinesspartnercdm();
	return R_recursivebusinesspartnercdm;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_recursivebusinesspartnercdm = (function () {
	function TVCheckFehler_recursivebusinesspartnercdm() {
		this._checkObject = R_recursivebusinesspartnercdm.getInstance();
	}
	TVCheckFehler_recursivebusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
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

	TVCheckFehler_recursivebusinesspartnercdm.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
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

	return TVCheckFehler_recursivebusinesspartnercdm;
}());
var TVCheckHinweis_recursivebusinesspartnercdm = (function () {
	function TVCheckHinweis_recursivebusinesspartnercdm() {
		this._checkObject = R_recursivebusinesspartnercdm.getInstance();
	}
	TVCheckHinweis_recursivebusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_recursivebusinesspartnercdm;
}());
var TVCheckInfo_recursivebusinesspartnercdm = (function () {
	function TVCheckInfo_recursivebusinesspartnercdm() {
		this._checkObject = R_recursivebusinesspartnercdm.getInstance();
	}
	TVCheckInfo_recursivebusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_recursivebusinesspartnercdm;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_recursivebusinesspartnercdm = (function () {
	function TVCheckAlle_recursivebusinesspartnercdm() {
		this._tvFehlerChecker = new TVCheckFehler_recursivebusinesspartnercdm();
		this._tvHinweisChecker = new TVCheckHinweis_recursivebusinesspartnercdm();
		this._tvInfoChecker = new TVCheckInfo_recursivebusinesspartnercdm();
	}
	TVCheckAlle_recursivebusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_recursivebusinesspartnercdm;
}());
var TVCheckPreliminary_recursivebusinesspartnercdm = (function () {
	function TVCheckPreliminary_recursivebusinesspartnercdm() {
		this._checkObject = R_recursivebusinesspartnercdm.getInstance();
	}
	TVCheckPreliminary_recursivebusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_recursivebusinesspartnercdm;
}());
var ERValidator_recursivebusinesspartnercdm = (function () {
	function ERValidator_recursivebusinesspartnercdm() {
		if (ERValidator_recursivebusinesspartnercdm._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_recursivebusinesspartnercdm.getInstance() instead of new.");
		}
		ERValidator_recursivebusinesspartnercdm._instance = this;
	}
	ERValidator_recursivebusinesspartnercdm.getInstance = function () {
		return ERValidator_recursivebusinesspartnercdm._instance;
	};
	ERValidator_recursivebusinesspartnercdm.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_recursivebusinesspartnercdm.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_recursivebusinesspartnercdm.getInstance(), modus, ERValidator_recursivebusinesspartnercdm._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_recursivebusinesspartnercdm.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_recursivebusinesspartnercdm.getInstance(), modus, ERValidator_recursivebusinesspartnercdm._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_recursivebusinesspartnercdm.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_recursivebusinesspartnercdm.getInstance(), modus, ERValidator_recursivebusinesspartnercdm._metaData, logger, C_recursivebusinesspartnercdm.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_recursivebusinesspartnercdm._instance = new ERValidator_recursivebusinesspartnercdm();
	ERValidator_recursivebusinesspartnercdm._metaData = Meta_recursivebusinesspartnercdm.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_recursivebusinesspartnercdm._constructor = (function () {
	})();
	return ERValidator_recursivebusinesspartnercdm;
}());

return {
	validator: ERValidator_recursivebusinesspartnercdm,
	meta: Meta_recursivebusinesspartnercdm
};
