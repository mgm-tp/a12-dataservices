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
var Meta_Felder_businesspartner = (function () {
	function Meta_Felder_businesspartner() {
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
	Meta_Felder_businesspartner.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_businesspartner.prototype._initFeldtypen = function () {
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
		new FormatDefinitionDatum(
			"yyyy-MM-dd'T'HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		4,
		new FormatDefinitionDatum(
			"yyyy-MM", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
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
		6,
		new FormatDefinitionJaNein(
			"true", // jaWert
			"false" // neinWert

		)
	);
	this._metaFormate.set(
		7,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(7),
			["0%", "10%", "20%", "30%", "40%", "50%"],
			this._enumerationOberflaechenWerte.get(7),
			this._enumerationCategoryValues.get(7),
			""
		)
	);
	this._metaFormate.set(
		8,
		new FormatDefinitionDatumBereich(
			"yyyy-MM-dd", // DatumFormat
			false, // Zusatzprüfung
			"/", // Bereichstrenner
			Jahresinterpretation.Standard // für Formate TT.MM-TT.MM (bis/von ist Referenzjahr)
		)
	);
	this._metaFormate.set(
		9,
		new FormatDefinitionZahl(
			true, // Vorzeichen
			true, // nullErlaubt
			true, // fuehrendeNullenErlaubt
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
		10,
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.JAHR_OPTIONAL // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		11,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(11),
			["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"],
			this._enumerationOberflaechenWerte.get(11),
			this._enumerationCategoryValues.get(11),
			""
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
			"MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		14,
		new FormatDefinitionJa(
			"true"

		)
	);
	this._metaFormate.set(
		15,
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		16,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(16),
			["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"],
			this._enumerationOberflaechenWerte.get(16),
			this._enumerationCategoryValues.get(16),
			""
		)
	);
	};

	/*
	 * Die übergebenen Parameter bedeuten (in aufsteigender Reihenfolge):
	 * interne Nummer, eindeutiger name, voller Pfadname, pflichtFeld, mandatoryForRepeatableGroup, Format,
	 * Formatdefinition, javaScriptName, vordruckPflichtfeld (lfd. Nummern), pflichtfeld
	 * (lfd. Nummern), zusätzliche Schlüssel-Werte Paare, berechnungsUebertragServer
	 */
	Meta_Felder_businesspartner.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"original_filename",
			"/BusinessPartnerRoot/Attachment/original_filename",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartneroriginal_filenameValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false)
				.put("Annotation", new MapBuilder()
				.put("enable_case_insensitive_search", "true").build()).build(),
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
			"businesspartnerinternal_filenameValidate",
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
			"businesspartnercontentValidate",
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
			"businesspartnerattachment_idValidate",
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
			"businesspartnersizeValidate",
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
			"businesspartnermime_typeValidate",
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
			"businesspartnercategoryValidate",
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
			"businesspartnerdescriptionValidate",
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
			"signingDateTime",
			"/BusinessPartnerRoot/Employment/signingDateTime",
			false,
			false,
			this._metaFormate.get(3),
			"businesspartnersigningDateTimeValidate",
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
			this._errorTexts4Req.get(8)
		)
	);
	this._felder.push(
		new Feld_t(
			9,
			"employedSince",
			"/BusinessPartnerRoot/Employment/employedSince",
			false,
			false,
			this._metaFormate.get(4),
			"businesspartneremployedSinceValidate",
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
			this._errorTexts4Req.get(9)
		)
	);
	this._felder.push(
		new Feld_t(
			10,
			"employedTill",
			"/BusinessPartnerRoot/Employment/employedTill",
			false,
			false,
			this._metaFormate.get(4),
			"businesspartneremployedTillValidate",
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
			this._errorTexts4Req.get(10)
		)
	);
	this._felder.push(
		new Feld_t(
			11,
			"income_0",
			"/BusinessPartnerRoot/Employment/income",
			false,
			false,
			this._metaFormate.get(5),
			"businesspartnerincome_0Validate",
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
			this._errorTexts4Req.get(11)
		)
	);
	this._felder.push(
		new Feld_t(
			12,
			"tax",
			"/BusinessPartnerRoot/Employment/tax",
			false,
			false,
			this._metaFormate.get(5),
			"businesspartnertaxValidate",
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
			this._errorTexts4Req.get(12)
		)
	);
	this._felder.push(
		new Feld_t(
			13,
			"role_0",
			"/BusinessPartnerRoot/Employment/role",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnerrole_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Role").put("de", "Rolle").build())
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
			"currentlyEmployed",
			"/BusinessPartnerRoot/Employment/currentlyEmployed",
			false,
			false,
			this._metaFormate.get(6),
			"businesspartnercurrentlyEmployedValidate",
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
			this._errorTexts4Req.get(14)
		)
	);
	this._felder.push(
		new Feld_t(
			15,
			"Discount",
			"/BusinessPartnerRoot/Offer/Discount",
			false,
			false,
			this._metaFormate.get(7),
			"businesspartnerDiscountValidate",
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
			this._errorTexts4Req.get(15)
		)
	);
	this._felder.push(
		new Feld_t(
			16,
			"ValidityPeriod",
			"/BusinessPartnerRoot/Offer/ValidityPeriod",
			false,
			false,
			this._metaFormate.get(8),
			"businesspartnerValidityPeriodValidate",
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
			this._errorTexts4Req.get(16)
		)
	);
	this._felder.push(
		new Feld_t(
			17,
			"accountNumber",
			"/BusinessPartnerRoot/accountNumber",
			false,
			false,
			this._metaFormate.get(9),
			"businesspartneraccountNumberValidate",
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
			this._errorTexts4Req.get(17)
		)
	);
	this._felder.push(
		new Feld_t(
			18,
			"birthday",
			"/BusinessPartnerRoot/birthday",
			false,
			false,
			this._metaFormate.get(10),
			"businesspartnerbirthdayValidate",
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
			this._errorTexts4Req.get(18)
		)
	);
	this._felder.push(
		new Feld_t(
			19,
			"Name",
			"/BusinessPartnerRoot/Name",
			true,
			false,
			this._metaFormate.get(0),
			"businesspartnerNameValidate",
			[0],
			[0],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Name").put("de", "Name").build())
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
			"Industry",
			"/BusinessPartnerRoot/Industry",
			true,
			false,
			this._metaFormate.get(11),
			"businesspartnerIndustryValidate",
			[0],
			[0],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Industry or business sector").build())
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
			"role_1",
			"/BusinessPartnerRoot/role",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnerrole_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Role").put("de", "Rolle").build())
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
			"income_1",
			"/BusinessPartnerRoot/income",
			false,
			false,
			this._metaFormate.get(2),
			"businesspartnerincome_1Validate",
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
			this._errorTexts4Req.get(22)
		)
	);
	this._felder.push(
		new Feld_t(
			23,
			"EndOfRelationship",
			"/BusinessPartnerRoot/EndOfRelationship",
			false,
			false,
			this._metaFormate.get(3),
			"businesspartnerEndOfRelationshipValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "End of business relationship").build())
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
			"TimeOfContractSignature",
			"/BusinessPartnerRoot/TimeOfContractSignature",
			false,
			false,
			this._metaFormate.get(12),
			"businesspartnerTimeOfContractSignatureValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Exact time of contract signature").build())
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
			"ApproximateOfferDate",
			"/BusinessPartnerRoot/ApproximateOfferDate",
			false,
			false,
			this._metaFormate.get(13),
			"businesspartnerApproximateOfferDateValidate",
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
			this._errorTexts4Req.get(25)
		)
	);
	this._felder.push(
		new Feld_t(
			26,
			"PremiumPartner",
			"/BusinessPartnerRoot/PremiumPartner",
			false,
			false,
			this._metaFormate.get(14),
			"businesspartnerPremiumPartnerValidate",
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
			"StartOfRelationship",
			"/BusinessPartnerRoot/StartOfRelationship",
			false,
			false,
			this._metaFormate.get(15),
			"businesspartnerStartOfRelationshipValidate",
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
			this._errorTexts4Req.get(27)
		)
	);
	this._felder.push(
		new Feld_t(
			28,
			"CustomerDiscount",
			"/BusinessPartnerRoot/CustomerDiscount",
			true,
			false,
			this._metaFormate.get(16),
			"businesspartnerCustomerDiscountValidate",
			[0],
			[0],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Customer Discount").put("de", "Kundenrabatt").build())
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
			"PersonOrEntity",
			"/BusinessPartnerRoot/PersonOrEntity",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnerPersonOrEntityValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Natural Person or Legal Entity").put("de", "Natürliche Person oder juristische Person").build())
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
			"Company",
			"/BusinessPartnerRoot/SubtypeGroup/Company",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnerCompanyValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Company").put("de", "Unternehmen").build())
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
			"Notes",
			"/BusinessPartnerRoot/Notes",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnerNotesValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Notes").put("de", "Notizen").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false)
				.put("Annotation", new MapBuilder()
				.put("indexed", "false").build()).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(31)
		)
	);
	this._felder.push(
		new Feld_t(
			32,
			"docRef",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnerdocRefValidate",
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
			this._errorTexts4Req.get(32)
		)
	);
	this._felder.push(
		new Feld_t(
			33,
			"modelReference",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnermodelReferenceValidate",
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
			this._errorTexts4Req.get(33)
		)
	);
	this._felder.push(
		new Feld_t(
			34,
			"modelVersion",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnermodelVersionValidate",
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
			this._errorTexts4Req.get(34)
		)
	);
	this._felder.push(
		new Feld_t(
			35,
			"creator",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnercreatorValidate",
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
			this._errorTexts4Req.get(35)
		)
	);
	this._felder.push(
		new Feld_t(
			36,
			"createdAt",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
			"businesspartnercreatedAtValidate",
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
			this._errorTexts4Req.get(36)
		)
	);
	this._felder.push(
		new Feld_t(
			37,
			"modifier",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"businesspartnermodifierValidate",
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
			this._errorTexts4Req.get(37)
		)
	);
	this._felder.push(
		new Feld_t(
			38,
			"modifiedAt",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
			"businesspartnermodifiedAtValidate",
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
			this._errorTexts4Req.get(38)
		)
	);
	};
	Meta_Felder_businesspartner.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(7, new Map());

	this._mehrsprachigeFehlertexte.set(11, new Map());

	this._mehrsprachigeFehlertexte.set(16, new Map());

	};
	Meta_Felder_businesspartner.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(7, new Map());
	this._enumerationOberflaechenWerte.get(7).set("de", MetaFeldUtil.createPairList(["0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat"], ["0%", "10%", "20%", "30%", "40%", "50%"]));
	this._enumerationOberflaechenWerte.get(7).set("en", MetaFeldUtil.createPairList(["0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount"], ["0%", "10%", "20%", "30%", "40%", "50%"]));
	this._enumerationOberflaechenWerte.get(7).set("en_US", MetaFeldUtil.createPairList(["0%", "10%", "20%", "30%", "40%", "50%"], ["0%", "10%", "20%", "30%", "40%", "50%"]));

	this._enumerationOberflaechenWerte.set(11, new Map());
	this._enumerationOberflaechenWerte.get(11).set("de", MetaFeldUtil.createPairList(["Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(11).set("en", MetaFeldUtil.createPairList(["Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(11).set("en_US", MetaFeldUtil.createPairList(["Technology of Information", "Investment", "Bookkeeping", "HealthPrevention", "Liability", "Constitution", "Business"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));

	this._enumerationOberflaechenWerte.set(16, new Map());
	this._enumerationOberflaechenWerte.get(16).set("de", MetaFeldUtil.createPairList(["0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(16).set("en", MetaFeldUtil.createPairList(["0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(16).set("en_US", MetaFeldUtil.createPairList(["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));

	};


	return Meta_Felder_businesspartner;
}());
var Meta_Regel_businesspartner = (function () {
	function Meta_Regel_businesspartner() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_businesspartner.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_businesspartner.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_businesspartner.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_businesspartner.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_businesspartner.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_businesspartner.ANZAHL_REGELN = 9;

	Meta_Regel_businesspartner.prototype._initRegelRefs = function () {
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
			new Identifier_t(11, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(12, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[4]= [
			new Identifier_t(11, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(12, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/NameFilled'
	this._refIdentifier[5] = [
			new Identifier_t(19, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[5]= [
			new Identifier_t(19, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/IndustryFilled'
	this._refIdentifier[6] = [
			new Identifier_t(20, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[6]= [
			new Identifier_t(20, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/CustomerDiscountFilled'
	this._refIdentifier[7] = [
			new Identifier_t(28, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[7]= [
			new Identifier_t(28, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/BusinessPartnerRoot/SubtypeGroup/PersonComp'
	this._refIdentifier[8] = [
			new Identifier_t(19, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(20, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(27, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(28, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(29, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[8]= [
			new Identifier_t(29, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	};

	Meta_Regel_businesspartner.prototype._initRegelFehlertexte = function () {
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
	// Init Daten zur Regel '/BusinessPartnerRoot/NameFilled'
	this._mehrsprachigeFehlertexte.get(5).set("de", "Interner Fehler: Name muss ausgefüllt sein.");this._mehrsprachigeFehlertexte.get(5).set("en", "Internal Error: Name must be filled.");
	this._fehlertexteParameterMapping.set(5, new MapBuilder().build());
	// Init Daten zur Regel '/BusinessPartnerRoot/IndustryFilled'
	this._mehrsprachigeFehlertexte.get(6).set("de", "Interner Fehler: Industry muss ausgefüllt sein.");this._mehrsprachigeFehlertexte.get(6).set("en", "Internal Error: Industry must be filled.");
	this._fehlertexteParameterMapping.set(6, new MapBuilder().build());
	// Init Daten zur Regel '/BusinessPartnerRoot/CustomerDiscountFilled'
	this._mehrsprachigeFehlertexte.get(7).set("de", "Interner Fehler: CustomerDiscount muss ausgefüllt sein.");this._mehrsprachigeFehlertexte.get(7).set("en", "Internal Error: CustomerDiscount must be filled.");
	this._fehlertexteParameterMapping.set(7, new MapBuilder().build());
	// Init Daten zur Regel '/BusinessPartnerRoot/SubtypeGroup/PersonComp'
	this._mehrsprachigeFehlertexte.get(8).set("de", "fehlertext zur Berechnung von PersonComp");this._mehrsprachigeFehlertexte.get(8).set("en", "error text for computation of PersonComp");
	this._fehlertexteParameterMapping.set(8, new MapBuilder().build());
	};

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	Meta_Regel_businesspartner.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("AttachmentInternalFilenameRequired", "/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", "ErrorR29", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/BusinessPartnerRoot/Attachment/internal_filename", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentMimeTypeRequired", "/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", "ErrorR30", this._mehrsprachigeFehlertexte.get(1), this._fehlertexteParameterMapping.get(1), "Fehler", this._refIdentifier[1], this._refAuslassungsIdentifier[1], null, "/BusinessPartnerRoot/Attachment/mime_type", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentIdOrContentFilled", "/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", "ErrorR31", this._mehrsprachigeFehlertexte.get(2), this._fehlertexteParameterMapping.get(2), "Fehler", this._refIdentifier[2], this._refAuslassungsIdentifier[2], null, "/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("SizeOfContentFilled", "/BusinessPartnerRoot/Attachment/SizeOfContentFilled", "ErrorR32", this._mehrsprachigeFehlertexte.get(3), this._fehlertexteParameterMapping.get(3), "Fehler", this._refIdentifier[3], this._refAuslassungsIdentifier[3], null, "/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("TaxComputation", "/BusinessPartnerRoot/Employment/TaxComputation", "TaxComputation", this._mehrsprachigeFehlertexte.get(4), this._fehlertexteParameterMapping.get(4), "Fehler", this._refIdentifier[4], this._refAuslassungsIdentifier[4], null, "/BusinessPartnerRoot/Employment/tax", true, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("NameFilled", "/BusinessPartnerRoot/NameFilled", "ErrorR33", this._mehrsprachigeFehlertexte.get(5), this._fehlertexteParameterMapping.get(5), "Fehler", this._refIdentifier[5], this._refAuslassungsIdentifier[5], null, "/BusinessPartnerRoot/Name", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("IndustryFilled", "/BusinessPartnerRoot/IndustryFilled", "ErrorR34", this._mehrsprachigeFehlertexte.get(6), this._fehlertexteParameterMapping.get(6), "Fehler", this._refIdentifier[6], this._refAuslassungsIdentifier[6], null, "/BusinessPartnerRoot/Industry", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("CustomerDiscountFilled", "/BusinessPartnerRoot/CustomerDiscountFilled", "ErrorR35", this._mehrsprachigeFehlertexte.get(7), this._fehlertexteParameterMapping.get(7), "Fehler", this._refIdentifier[7], this._refAuslassungsIdentifier[7], null, "/BusinessPartnerRoot/CustomerDiscount", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("PersonComp", "/BusinessPartnerRoot/SubtypeGroup/PersonComp", "PersonComp", this._mehrsprachigeFehlertexte.get(8), this._fehlertexteParameterMapping.get(8), "Fehler", this._refIdentifier[8], this._refAuslassungsIdentifier[8], null, "/BusinessPartnerRoot/PersonOrEntity", true, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_businesspartner;
}());
var Meta_PfFeld_businesspartner = (function () {
	function Meta_PfFeld_businesspartner(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_businesspartner.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		this._addLokalePflichtFeldInfos_0(infos);

		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};

	Meta_PfFeld_businesspartner.prototype._addLokalePflichtFeldInfos_0 = function (infos) {
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



	Meta_PfFeld_businesspartner.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_businesspartner;
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

var Meta_businesspartner = (function () {
	function Meta_businesspartner() {
		this._metaFelder = new Meta_Felder_businesspartner();
		this._metaRegeln = new Meta_Regel_businesspartner();
		this._unterstuetzteSprachen = ["en", "en_US", "de"];
		this._da = new Model_t(
				"29.4.0", // Produkt-Version
				".", // dezimaltrenner
				["en", "en_US", "de"], // unterstuetzte Sprachen
				MetaDataHelper.createSetOfStrings(), //  Namen der Applikationsbedingungen
				MetaDataHelper.createSetOfStrings(), // Names of custom field types
				// additional Values
				new MapBuilder()
					.put("BaseYear", null)
					.put("TimeZone", "UTC")
					.put("ModelId", "BusinessPartner")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest,ModelRead").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_businesspartner._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_businesspartner.getInstance() instead of new.");
		}
		Meta_businesspartner._instance = this;
		var container = Meta_businesspartner._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_businesspartner.LEGAL_CHARACTERS,
			Meta_businesspartner.LEGAL_GRAPHEME_TRIE,
			() => E_businesspartner.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_businesspartner(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_businesspartner, "RUNTIME_VERSION", { get: function () { return "31.1"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartner, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartner, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartner, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_businesspartner, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_businesspartner.getInstance = function () {
		return Meta_businesspartner._instance;
	};
	Meta_businesspartner.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_businesspartner.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_businesspartner._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Attachment", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Employment", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/Offer", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/BusinessPartnerRoot/SubtypeGroup", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_businesspartner.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_businesspartner.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_businesspartner._constructor = (function () {
		if (Meta_businesspartner.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_businesspartner.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_businesspartner._instance = new Meta_businesspartner();
	})();
	return Meta_businesspartner;
}());var E_businesspartner = (function () {
	function E_businesspartner() {
		if (E_businesspartner._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_businesspartner.getInstance() instead of new.");
		}
		E_businesspartner._valueCalcObject = this;
		this._feldData = Meta_businesspartner.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_businesspartner.getInstance = function () {
		return this._valueCalcObject;
	};


	E_businesspartner.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_businesspartner._valueCalcObject = new E_businesspartner();
	return E_businesspartner;
}());
var C_businesspartner = (function () {
	function C_businesspartner() {
		if (C_businesspartner._checkObject) {
			throw new Error("Instantiation failed: Use C_businesspartner.getInstance() instead of new.");
		}
		C_businesspartner._checkObject = this;
		this._feldData = Meta_businesspartner.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_businesspartner.getInstance = function () {
		return this._checkObject;
	};

	C_businesspartner.prototype.calc_tax = function (controller) {
		// Berechnung für das Feld '/BusinessPartnerRoot/Employment/tax'
		var start = [1, 1, 1];
		var ende = [1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income_0").isField(true).idx(1).idx(1).idx(1).build();
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
	C_businesspartner.prototype.calc_PersonOrEntity = function (controller) {
		// Berechnung für das Feld '/BusinessPartnerRoot/PersonOrEntity'
		var start = [1, 1];
		var ende = [1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("PersonOrEntity").isField(true).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("Name").isField(true).idx(1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("Industry").isField(true).idx(1).idx(1).build();
		var rtIdCon4 = RtIdentifierTemplate.builder().unqNm("StartOfRelationship").isField(true).idx(1).idx(1).build();
		var rtIdCon5 = RtIdentifierTemplate.builder().unqNm("CustomerDiscount").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).add(rtIdCon3).add(rtIdCon4).add(rtIdCon5).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/BusinessPartnerRoot/SubtypeGroup/PersonComp'
					if (controller.mindestensEinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("Natural Person", id);
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


	C_businesspartner.CALCULATED_FIELD_DEPENDENCIES = new Map([
		["/BusinessPartnerRoot/Employment/tax", new Set(["/BusinessPartnerRoot/Employment/income"])],
		["/BusinessPartnerRoot/PersonOrEntity", new Set(["/BusinessPartnerRoot/Industry", "/BusinessPartnerRoot/StartOfRelationship", "/BusinessPartnerRoot/Name", "/BusinessPartnerRoot/CustomerDiscount"])]
	]);

	C_businesspartner.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set([
		"/BusinessPartnerRoot/CustomerDiscount",
		"/BusinessPartnerRoot/Employment/income",
		"/BusinessPartnerRoot/Industry",
		"/BusinessPartnerRoot/Name",
		"/BusinessPartnerRoot/StartOfRelationship"
	]);

	C_businesspartner.CALC_FUNC_NAME_MAP = new Map([
		["/BusinessPartnerRoot/Employment/tax", "calc_tax"],
		["/BusinessPartnerRoot/PersonOrEntity", "calc_PersonOrEntity"]
	]);

	C_businesspartner.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [
		"/BusinessPartnerRoot/Employment/tax",
		"/BusinessPartnerRoot/PersonOrEntity"
	];

	C_businesspartner.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_businesspartner.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_businesspartner.prototype.hasCalculation = function(fullFieldName) {
        return C_businesspartner.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_businesspartner.prototype.getDependencyGraph = function() {
        return C_businesspartner.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_businesspartner.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_businesspartner.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_businesspartner.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_businesspartner.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_businesspartner._checkObject = new C_businesspartner();
	return C_businesspartner;
}());
var R_businesspartner = (function () {
	function R_businesspartner() {
		this._feldData = Meta_businesspartner.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_businesspartner.getInstance = function () {
		return R_businesspartner._checkObject;
	};
	R_businesspartner.prototype.mvk_AttachmentInternalFilenameRequired = function (controller, indices) {
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
	R_businesspartner.prototype.mvk_AttachmentMimeTypeRequired = function (controller, indices) {
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
	R_businesspartner.prototype.mvk_AttachmentIdOrContentFilled = function (controller, indices) {
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
	R_businesspartner.prototype.mvk_SizeOfContentFilled = function (controller, indices) {
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
	R_businesspartner.prototype.mvk_TaxComputation = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/Employment/TaxComputation", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income_0").isField(true).idx(1).idx(1).idx(1).build();
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
	R_businesspartner.prototype.mvk_NameFilled = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/NameFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("Name").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var tb0 = controller.keinFeldAngegeben(idx, allRtIdCon1);
						var valErg = tb0;
						controller.addValidatorMessage("Name", idx, "ErrorR33", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("Name", iter.get(), "ErrorR33", e);
			}
		}
	};
	R_businesspartner.prototype.mvk_IndustryFilled = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/IndustryFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("Industry").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var tb0 = controller.keinFeldAngegeben(idx, allRtIdCon1);
						var valErg = tb0;
						controller.addValidatorMessage("Industry", idx, "ErrorR34", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("Industry", iter.get(), "ErrorR34", e);
			}
		}
	};
	R_businesspartner.prototype.mvk_CustomerDiscountFilled = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/CustomerDiscountFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("CustomerDiscount").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var tb0 = controller.keinFeldAngegeben(idx, allRtIdCon1);
						var valErg = tb0;
						controller.addValidatorMessage("CustomerDiscount", idx, "ErrorR35", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("CustomerDiscount", iter.get(), "ErrorR35", e);
			}
		}
	};
	R_businesspartner.prototype.mvk_PersonComp = function (controller, indices) {
		controller.initFuerRegelpruefung("/BusinessPartnerRoot/SubtypeGroup/PersonComp", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("PersonOrEntity").isField(true).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("Name").isField(true).idx(1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("Industry").isField(true).idx(1).idx(1).build();
		var rtIdCon4 = RtIdentifierTemplate.builder().unqNm("StartOfRelationship").isField(true).idx(1).idx(1).build();
		var rtIdCon5 = RtIdentifierTemplate.builder().unqNm("CustomerDiscount").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).add(rtIdCon3).add(rtIdCon4).add(rtIdCon5).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.mindestensEinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "Natural Person").isKnownAndTrue()) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.mindestensEinFeldAngegeben(idx, allRtIdCon2);
						var tb2 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "Natural Person");
						var valErg = tb0.combineUND(tb1).combineUND(tb2);
						controller.addValidatorMessage("PersonOrEntity", idx, "PersonComp", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("PersonOrEntity", iter.get(), "PersonComp", e);
			}
		}
	};


	R_businesspartner.vordruckF__global = function (controller) {
	/* Aufruf der Regeln fuer Vordruck global für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_NameFilled(controller, indices);
		controller.logMessung("mvk_NameFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_IndustryFilled(controller, indices);
		controller.logMessung("mvk_IndustryFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_CustomerDiscountFilled(controller, indices);
		controller.logMessung("mvk_CustomerDiscountFilled", indices[0], startMesspunkt);
	};
	R_businesspartner.vordruckF__BusinessPartnerRoot = function (controller) {
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
	R_businesspartner.vordruckF__BusinessPartnerRoot_lfdNr = function (controller, indices) {
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
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_PersonComp(controller, indices);
		controller.logMessung("mvk_PersonComp", indices[0], startMesspunkt);
	};
	R_businesspartner.vordruck__global = function (controller) {
	/* Aufruf der Regeln fuer Vordruck global für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_NameFilled(controller, indices);
		controller.logMessung("mvk_NameFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_IndustryFilled(controller, indices);
		controller.logMessung("mvk_IndustryFilled", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_CustomerDiscountFilled(controller, indices);
		controller.logMessung("mvk_CustomerDiscountFilled", indices[0], startMesspunkt);
	};
	R_businesspartner.vordruck__BusinessPartnerRoot = function (controller) {
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
	R_businesspartner.vordruck__BusinessPartnerRoot_lfdNr = function (controller, indices) {
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
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_PersonComp(controller, indices);
		controller.logMessung("mvk_PersonComp", indices[0], startMesspunkt);
	};



	R_businesspartner.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_businesspartner["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_businesspartner, controller);
		}
	};

	R_businesspartner.prototype.validatePreliminaryRules = function(controller) {
};

	R_businesspartner.prototype.validiereVoll = function (controller, modus) {
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
	R_businesspartner.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_businesspartner();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_businesspartner();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_businesspartner();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_businesspartner();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_businesspartner();
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
	R_businesspartner.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_businesspartner.vordruck__global(controller);
		R_businesspartner.vordruck__BusinessPartnerRoot(controller);
};
	R_businesspartner.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_businesspartner.vordruckF__global(controller);
		R_businesspartner.vordruckF__BusinessPartnerRoot(controller);
};
	R_businesspartner.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_businesspartner.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_businesspartner._checkObject = new R_businesspartner();
	return R_businesspartner;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_businesspartner = (function () {
	function TVCheckFehler_businesspartner() {
		this._checkObject = R_businesspartner.getInstance();
	}
	TVCheckFehler_businesspartner.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 29) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [1,29]
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

	TVCheckFehler_businesspartner.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
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
			case 12:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_TaxComputation(controller, indices);
				controller.logMessung("mvk_TaxComputation", indices[0], startMesspunkt);
				break;
			case 19:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_NameFilled(controller, indices);
				controller.logMessung("mvk_NameFilled", indices[0], startMesspunkt);
				break;
			case 20:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_IndustryFilled(controller, indices);
				controller.logMessung("mvk_IndustryFilled", indices[0], startMesspunkt);
				break;
			case 28:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_CustomerDiscountFilled(controller, indices);
				controller.logMessung("mvk_CustomerDiscountFilled", indices[0], startMesspunkt);
				break;
			case 29:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_PersonComp(controller, indices);
				controller.logMessung("mvk_PersonComp", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 29) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [1,29].");
			}
			break;
		}
	};

	return TVCheckFehler_businesspartner;
}());
var TVCheckHinweis_businesspartner = (function () {
	function TVCheckHinweis_businesspartner() {
		this._checkObject = R_businesspartner.getInstance();
	}
	TVCheckHinweis_businesspartner.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_businesspartner;
}());
var TVCheckInfo_businesspartner = (function () {
	function TVCheckInfo_businesspartner() {
		this._checkObject = R_businesspartner.getInstance();
	}
	TVCheckInfo_businesspartner.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_businesspartner;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_businesspartner = (function () {
	function TVCheckAlle_businesspartner() {
		this._tvFehlerChecker = new TVCheckFehler_businesspartner();
		this._tvHinweisChecker = new TVCheckHinweis_businesspartner();
		this._tvInfoChecker = new TVCheckInfo_businesspartner();
	}
	TVCheckAlle_businesspartner.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_businesspartner;
}());
var TVCheckPreliminary_businesspartner = (function () {
	function TVCheckPreliminary_businesspartner() {
		this._checkObject = R_businesspartner.getInstance();
	}
	TVCheckPreliminary_businesspartner.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_businesspartner;
}());
var ERValidator_businesspartner = (function () {
	function ERValidator_businesspartner() {
		if (ERValidator_businesspartner._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_businesspartner.getInstance() instead of new.");
		}
		ERValidator_businesspartner._instance = this;
	}
	ERValidator_businesspartner.getInstance = function () {
		return ERValidator_businesspartner._instance;
	};
	ERValidator_businesspartner.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_businesspartner.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_businesspartner.getInstance(), modus, ERValidator_businesspartner._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_businesspartner.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_businesspartner.getInstance(), modus, ERValidator_businesspartner._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_businesspartner.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_businesspartner.getInstance(), modus, ERValidator_businesspartner._metaData, logger, C_businesspartner.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_businesspartner._instance = new ERValidator_businesspartner();
	ERValidator_businesspartner._metaData = Meta_businesspartner.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_businesspartner._constructor = (function () {
	})();
	return ERValidator_businesspartner;
}());

return {
	validator: ERValidator_businesspartner,
	meta: Meta_businesspartner
};
