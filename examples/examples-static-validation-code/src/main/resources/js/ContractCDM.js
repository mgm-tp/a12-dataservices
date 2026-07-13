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
var Meta_Felder_contractcdm = (function () {
	function Meta_Felder_contractcdm() {
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
	Meta_Felder_contractcdm.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_contractcdm.prototype._initFeldtypen = function () {
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
		2,
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
		3,
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
		4,
		new FormatDefinitionJaNein(
			"true", // jaWert
			"false" // neinWert

		)
	);
	this._metaFormate.set(
		5,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(5),
			["Household", "Travel", "Health", "Liability"],
			this._enumerationOberflaechenWerte.get(5),
			this._enumerationCategoryValues.get(5),
			""
		)
	);
	this._metaFormate.set(
		6,
		new FormatDefinitionDatum(
			"yyyy-MM-dd'T'HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		7,
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		8,
		new FormatDefinitionDatum(
			"HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		9,
		new FormatDefinitionDatum(
			"yyyy", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
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
		new FormatDefinitionDatumBereich(
			"yyyy-MM-dd", // DatumFormat
			false, // Zusatzprüfung
			"/", // Bereichstrenner
			Jahresinterpretation.Standard // für Formate TT.MM-TT.MM (bis/von ist Referenzjahr)
		)
	);
	this._metaFormate.set(
		12,
		new FormatDefinitionString(
			-1, // minLaenge
			99999, // maxLaenge
			19, // minLaengePrintable
			19, // maxLaengePrintable
			"[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}", // muster
			this._mehrsprachigeFehlertexte.get(12), // fehlertexte
			false, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(12)
		)
	);
	this._metaFormate.set(
		13,
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
		14,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(14),
			["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"],
			this._enumerationOberflaechenWerte.get(14),
			this._enumerationCategoryValues.get(14),
			""
		)
	);
	this._metaFormate.set(
		15,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(15),
			["trivial", "low", "medium", "high", "critical"],
			this._enumerationOberflaechenWerte.get(15),
			this._enumerationCategoryValues.get(15),
			""
		)
	);
	this._metaFormate.set(
		16,
		new FormatDefinitionDatum(
			"dd.MM.yyyy", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		17,
		new FormatDefinitionDatum(
			"yyyy-MM", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		18,
		new FormatDefinitionString(
			-1, // minLaenge
			99999, // maxLaenge
			1, // minLaengePrintable
			99999, // maxLaengePrintable
			"", // muster
			this._mehrsprachigeFehlertexte.get(18), // fehlertexte
			true, // zeilenUmbruch
			true, // noValueValidation
			this._stringHintLists.get(18)
		)
	);
	this._metaFormate.set(
		19,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(19),
			["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"],
			this._enumerationOberflaechenWerte.get(19),
			this._enumerationCategoryValues.get(19),
			""
		)
	);
	this._metaFormate.set(
		20,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(20),
			["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"],
			this._enumerationOberflaechenWerte.get(20),
			this._enumerationCategoryValues.get(20),
			""
		)
	);
	this._metaFormate.set(
		21,
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
		22,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(22),
			["Residential", "Commercial", "Other"],
			this._enumerationOberflaechenWerte.get(22),
			this._enumerationCategoryValues.get(22),
			""
		)
	);
	this._metaFormate.set(
		23,
		new FormatDefinitionJa(
			"true"

		)
	);
	this._metaFormate.set(
		24,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(24),
			["EXP", "CON", "LAW"],
			this._enumerationOberflaechenWerte.get(24),
			this._enumerationCategoryValues.get(24),
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
	Meta_Felder_contractcdm.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"ContractName",
			"/ContractRoot/ContractName",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmContractNameValidate",
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
			this._errorTexts4Req.get(0)
		)
	);
	this._felder.push(
		new Feld_t(
			1,
			"ContractDescription",
			"/ContractRoot/ContractDescription",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmContractDescriptionValidate",
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
			this._errorTexts4Req.get(1)
		)
	);
	this._felder.push(
		new Feld_t(
			2,
			"LengthOfContract",
			"/ContractRoot/LengthOfContract",
			false,
			false,
			this._metaFormate.get(1),
			"contractcdmLengthOfContractValidate",
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
			this._errorTexts4Req.get(2)
		)
	);
	this._felder.push(
		new Feld_t(
			3,
			"ContractValue",
			"/ContractRoot/ContractValue",
			false,
			false,
			this._metaFormate.get(2),
			"contractcdmContractValueValidate",
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
			this._errorTexts4Req.get(3)
		)
	);
	this._felder.push(
		new Feld_t(
			4,
			"Liability",
			"/ContractRoot/Liability",
			false,
			false,
			this._metaFormate.get(2),
			"contractcdmLiabilityValidate",
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
			this._errorTexts4Req.get(4)
		)
	);
	this._felder.push(
		new Feld_t(
			5,
			"CostToCustomer",
			"/ContractRoot/CostToCustomer",
			false,
			false,
			this._metaFormate.get(2),
			"contractcdmCostToCustomerValidate",
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
			this._errorTexts4Req.get(5)
		)
	);
	this._felder.push(
		new Feld_t(
			6,
			"NoOfCoInsuredCustomers",
			"/ContractRoot/NoOfCoInsuredCustomers",
			false,
			false,
			this._metaFormate.get(3),
			"contractcdmNoOfCoInsuredCustomersValidate",
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
			this._errorTexts4Req.get(6)
		)
	);
	this._felder.push(
		new Feld_t(
			7,
			"MaxDiscount",
			"/ContractRoot/MaxDiscount",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmMaxDiscountValidate",
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
			this._errorTexts4Req.get(7)
		)
	);
	this._felder.push(
		new Feld_t(
			8,
			"CostPerCoInsured",
			"/ContractRoot/CostPerCoInsured",
			false,
			false,
			this._metaFormate.get(2),
			"contractcdmCostPerCoInsuredValidate",
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
			this._errorTexts4Req.get(8)
		)
	);
	this._felder.push(
		new Feld_t(
			9,
			"Valid",
			"/ContractRoot/Valid",
			false,
			false,
			this._metaFormate.get(4),
			"contractcdmValidValidate",
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
			this._errorTexts4Req.get(9)
		)
	);
	this._felder.push(
		new Feld_t(
			10,
			"Type",
			"/ContractRoot/Type",
			false,
			false,
			this._metaFormate.get(5),
			"contractcdmTypeValidate",
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
			this._errorTexts4Req.get(10)
		)
	);
	this._felder.push(
		new Feld_t(
			11,
			"ContractStartDate",
			"/ContractRoot/ContractDates/ContractStartDate",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmContractStartDateValidate",
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
			this._errorTexts4Req.get(11)
		)
	);
	this._felder.push(
		new Feld_t(
			12,
			"ContractEndDate",
			"/ContractRoot/ContractDates/ContractEndDate",
			false,
			false,
			this._metaFormate.get(7),
			"contractcdmContractEndDateValidate",
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
			this._errorTexts4Req.get(12)
		)
	);
	this._felder.push(
		new Feld_t(
			13,
			"ContractEndTime",
			"/ContractRoot/ContractDates/ContractEndTime",
			false,
			false,
			this._metaFormate.get(8),
			"contractcdmContractEndTimeValidate",
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
			this._errorTexts4Req.get(13)
		)
	);
	this._felder.push(
		new Feld_t(
			14,
			"ContractReviewDate",
			"/ContractRoot/ContractDates/ContractReviewDate",
			false,
			false,
			this._metaFormate.get(9),
			"contractcdmContractReviewDateValidate",
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
			"LastPremiumPaidDate",
			"/ContractRoot/ContractDates/LastPremiumPaidDate",
			false,
			false,
			this._metaFormate.get(10),
			"contractcdmLastPremiumPaidDateValidate",
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
			this._errorTexts4Req.get(15)
		)
	);
	this._felder.push(
		new Feld_t(
			16,
			"CoveragePeriod",
			"/ContractRoot/ContractDates/CoveragePeriod",
			false,
			false,
			this._metaFormate.get(11),
			"contractcdmCoveragePeriodValidate",
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
			"ID_3",
			"/ContractRoot/ChangeLog/ID",
			false,
			false,
			this._metaFormate.get(12),
			"contractcdmID_3Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("en_US", "ID").put("de", "ID").build())
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
			"Number",
			"/ContractRoot/ChangeLog/Number",
			false,
			false,
			this._metaFormate.get(13),
			"contractcdmNumberValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Number").put("en_US", "Number").put("de", "Nummer").build())
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
			"ChangeTimestamp",
			"/ContractRoot/ChangeLog/ChangeTimestamp",
			false,
			false,
			this._metaFormate.get(7),
			"contractcdmChangeTimestampValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Change Timestamp").put("en_US", "Change Timestamp").put("de", "Zeitstempel ändern").build())
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
			"Description",
			"/ContractRoot/ChangeLog/Description",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmDescriptionValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Description").put("en_US", "Description").put("de", "Beschreibung").build())
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
			"User",
			"/ContractRoot/ChangeLog/User",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmUserValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "User").put("en_US", "User").put("de", "Benutzer").build())
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
			"Status_2",
			"/ContractRoot/ChangeLog/Status",
			false,
			false,
			this._metaFormate.get(14),
			"contractcdmStatus_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("en_US", "Status").put("de", "Status").build())
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
			"Priority_2",
			"/ContractRoot/ChangeLog/Priority",
			false,
			false,
			this._metaFormate.get(15),
			"contractcdmPriority_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("en_US", "Priority").put("de", "Priorität").build())
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
			"ID_1",
			"/ContractRoot/ChangeLog/Changes/ID",
			false,
			false,
			this._metaFormate.get(12),
			"contractcdmID_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("en_US", "ID").put("de", "ID").build())
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
			"Title",
			"/ContractRoot/ChangeLog/Changes/Title",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmTitleValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Title").put("en_US", "Title").put("de", "Titel").build())
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
			"Details_0",
			"/ContractRoot/ChangeLog/Changes/Details",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmDetails_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Details").put("en_US", "Details").put("de", "Details").build())
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
			"Priority_0",
			"/ContractRoot/ChangeLog/Changes/Priority",
			false,
			false,
			this._metaFormate.get(15),
			"contractcdmPriority_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("en_US", "Priority").put("de", "Priorität").build())
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
			"Status_0",
			"/ContractRoot/ChangeLog/Changes/Status",
			false,
			false,
			this._metaFormate.get(14),
			"contractcdmStatus_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("en_US", "Status").put("de", "Status").build())
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
			"LastModified",
			"/ContractRoot/ChangeLog/Changes/LastModified",
			false,
			false,
			this._metaFormate.get(16),
			"contractcdmLastModifiedValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Last Modified").put("en_US", "Last Modified").put("de", "Zuletzt geändert").build())
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
			"ID_2",
			"/ContractRoot/ChangeLog/Changes/SubChanges/ID",
			false,
			false,
			this._metaFormate.get(12),
			"contractcdmID_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "ID").put("en_US", "ID").put("de", "ID").build())
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
			"TItle",
			"/ContractRoot/ChangeLog/Changes/SubChanges/TItle",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmTItleValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "TItle").put("en_US", "TItle").put("de", "Titel").build())
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
			"Details_1",
			"/ContractRoot/ChangeLog/Changes/SubChanges/Details",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmDetails_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Details").put("en_US", "Details").put("de", "Details").build())
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
			"Priority_1",
			"/ContractRoot/ChangeLog/Changes/SubChanges/Priority",
			false,
			false,
			this._metaFormate.get(15),
			"contractcdmPriority_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Priority").put("en_US", "Priority").put("de", "Priorität").build())
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
			"Status_1",
			"/ContractRoot/ChangeLog/Changes/SubChanges/Status",
			false,
			false,
			this._metaFormate.get(14),
			"contractcdmStatus_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Status").put("en_US", "Status").put("de", "Status").build())
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
			"CreatedAt",
			"/ContractRoot/ChangeLog/Changes/SubChanges/CreatedAt",
			false,
			false,
			this._metaFormate.get(17),
			"contractcdmCreatedAtValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Last Modified").put("en_US", "Last Modified").put("de", "Zuletzt geändert").build())
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
			"original_filename_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/original_filename",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmoriginal_filename_0Validate",
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
			this._errorTexts4Req.get(36)
		)
	);
	this._felder.push(
		new Feld_t(
			37,
			"internal_filename_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/internal_filename",
			false,
			true,
			this._metaFormate.get(0),
			"contractcdminternal_filename_0Validate",
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
			this._errorTexts4Req.get(37)
		)
	);
	this._felder.push(
		new Feld_t(
			38,
			"content_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/content",
			false,
			false,
			this._metaFormate.get(18),
			"contractcdmcontent_0Validate",
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
			this._errorTexts4Req.get(38)
		)
	);
	this._felder.push(
		new Feld_t(
			39,
			"attachment_id_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/attachment_id",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmattachment_id_0Validate",
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
			this._errorTexts4Req.get(39)
		)
	);
	this._felder.push(
		new Feld_t(
			40,
			"size_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/size",
			false,
			false,
			this._metaFormate.get(3),
			"contractcdmsize_0Validate",
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
			this._errorTexts4Req.get(40)
		)
	);
	this._felder.push(
		new Feld_t(
			41,
			"mime_type_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/mime_type",
			false,
			true,
			this._metaFormate.get(0),
			"contractcdmmime_type_0Validate",
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
			this._errorTexts4Req.get(41)
		)
	);
	this._felder.push(
		new Feld_t(
			42,
			"category_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/category",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcategory_0Validate",
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
			this._errorTexts4Req.get(42)
		)
	);
	this._felder.push(
		new Feld_t(
			43,
			"description_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Attachment/description",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdescription_0Validate",
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
			this._errorTexts4Req.get(43)
		)
	);
	this._felder.push(
		new Feld_t(
			44,
			"Name_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Name",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmName_0Validate",
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
			this._errorTexts4Req.get(44)
		)
	);
	this._felder.push(
		new Feld_t(
			45,
			"Industry_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Industry",
			false,
			false,
			this._metaFormate.get(19),
			"contractcdmIndustry_0Validate",
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
			this._errorTexts4Req.get(45)
		)
	);
	this._felder.push(
		new Feld_t(
			46,
			"StartOfRelationship_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/StartOfRelationship",
			false,
			false,
			this._metaFormate.get(7),
			"contractcdmStartOfRelationship_0Validate",
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
			this._errorTexts4Req.get(46)
		)
	);
	this._felder.push(
		new Feld_t(
			47,
			"CustomerDiscount_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/CustomerDiscount",
			false,
			false,
			this._metaFormate.get(20),
			"contractcdmCustomerDiscount_0Validate",
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
			this._errorTexts4Req.get(47)
		)
	);
	this._felder.push(
		new Feld_t(
			48,
			"income_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Employment/income",
			false,
			false,
			this._metaFormate.get(21),
			"contractcdmincome_0Validate",
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
			this._errorTexts4Req.get(48)
		)
	);
	this._felder.push(
		new Feld_t(
			49,
			"tax_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Employment/tax",
			false,
			false,
			this._metaFormate.get(21),
			"contractcdmtax_0Validate",
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
			this._errorTexts4Req.get(49)
		)
	);
	this._felder.push(
		new Feld_t(
			50,
			"PersonOrEntity_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/PersonOrEntity",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmPersonOrEntity_0Validate",
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
			this._errorTexts4Req.get(50)
		)
	);
	this._felder.push(
		new Feld_t(
			51,
			"t_docRef_1",
			"/ContractBusinessPartner/t_docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmt_docRef_1Validate",
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
			this._errorTexts4Req.get(51)
		)
	);
	this._felder.push(
		new Feld_t(
			52,
			"AddressType_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/AddressType",
			false,
			false,
			this._metaFormate.get(22),
			"contractcdmAddressType_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Address type").put("de", "Adresstyp").build())
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
			"Location_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/Location",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmLocation_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Location").put("de", "Standort").build())
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
			"Street_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/Street",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmStreet_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Street").put("de", "Straßenadresse").build())
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
			"HouseNumber_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/HouseNumber",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmHouseNumber_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "House number").put("de", "Hausnummer").build())
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
			"City_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/City",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmCity_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "City").put("de", "Stadt").build())
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
			"PostCode_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/PostCode",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmPostCode_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Post Code").put("de", "Postleitzahl").build())
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
			"Country_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/Country",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmCountry_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Country").put("de", "Land").build())
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
			"PostalAddress_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/PostalAddress",
			false,
			false,
			this._metaFormate.get(23),
			"contractcdmPostalAddress_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "This is the postal address").put("de", "Dies ist die Postanschrift").build())
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
			"t_docRef_0",
			"/ContractBusinessPartner/PartnerAddresses/t_docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmt_docRef_0Validate",
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
			this._errorTexts4Req.get(60)
		)
	);
	this._felder.push(
		new Feld_t(
			61,
			"docRef_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdocRef_0Validate",
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
			this._errorTexts4Req.get(61)
		)
	);
	this._felder.push(
		new Feld_t(
			62,
			"modelReference_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelReference_0Validate",
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
			this._errorTexts4Req.get(62)
		)
	);
	this._felder.push(
		new Feld_t(
			63,
			"modelVersion_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelVersion_0Validate",
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
			this._errorTexts4Req.get(63)
		)
	);
	this._felder.push(
		new Feld_t(
			64,
			"creator_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcreator_0Validate",
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
			this._errorTexts4Req.get(64)
		)
	);
	this._felder.push(
		new Feld_t(
			65,
			"createdAt_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmcreatedAt_0Validate",
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
			this._errorTexts4Req.get(65)
		)
	);
	this._felder.push(
		new Feld_t(
			66,
			"modifier_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodifier_0Validate",
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
			this._errorTexts4Req.get(66)
		)
	);
	this._felder.push(
		new Feld_t(
			67,
			"modifiedAt_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmmodifiedAt_0Validate",
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
			this._errorTexts4Req.get(67)
		)
	);
	this._felder.push(
		new Feld_t(
			68,
			"AddressType_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/AddressType",
			false,
			false,
			this._metaFormate.get(22),
			"contractcdmAddressType_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Address type").put("de", "Adresstyp").build())
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
			"Location_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Location",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmLocation_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Location").put("de", "Standort").build())
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
			"Street_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Street",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmStreet_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Street").put("de", "Straßenadresse").build())
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
			"HouseNumber_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/HouseNumber",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmHouseNumber_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "House number").put("de", "Hausnummer").build())
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
			"City_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/City",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmCity_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "City").put("de", "Stadt").build())
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
			"PostCode_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostCode",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmPostCode_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Post Code").put("de", "Postleitzahl").build())
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
			"Country_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Country",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmCountry_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Country").put("de", "Land").build())
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
			"PostalAddress_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostalAddress",
			false,
			false,
			this._metaFormate.get(23),
			"contractcdmPostalAddress_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "This is the postal address").put("de", "Dies ist die Postanschrift").build())
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
			"HasPostalAddress",
			"/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress",
			false,
			false,
			this._metaFormate.get(4),
			"contractcdmHasPostalAddressValidate",
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
			this._errorTexts4Req.get(76)
		)
	);
	this._felder.push(
		new Feld_t(
			77,
			"HasBusinessPartner",
			"/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner",
			false,
			false,
			this._metaFormate.get(4),
			"contractcdmHasBusinessPartnerValidate",
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
			this._errorTexts4Req.get(77)
		)
	);
	this._felder.push(
		new Feld_t(
			78,
			"docRef_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdocRef_1Validate",
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
			this._errorTexts4Req.get(78)
		)
	);
	this._felder.push(
		new Feld_t(
			79,
			"modelReference_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelReference_1Validate",
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
			this._errorTexts4Req.get(79)
		)
	);
	this._felder.push(
		new Feld_t(
			80,
			"modelVersion_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelVersion_1Validate",
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
			this._errorTexts4Req.get(80)
		)
	);
	this._felder.push(
		new Feld_t(
			81,
			"creator_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcreator_1Validate",
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
			this._errorTexts4Req.get(81)
		)
	);
	this._felder.push(
		new Feld_t(
			82,
			"createdAt_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmcreatedAt_1Validate",
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
			this._errorTexts4Req.get(82)
		)
	);
	this._felder.push(
		new Feld_t(
			83,
			"modifier_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodifier_1Validate",
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
			this._errorTexts4Req.get(83)
		)
	);
	this._felder.push(
		new Feld_t(
			84,
			"modifiedAt_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmmodifiedAt_1Validate",
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
			this._errorTexts4Req.get(84)
		)
	);
	this._felder.push(
		new Feld_t(
			85,
			"docRef_2",
			"/ContractBusinessPartner/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdocRef_2Validate",
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
			this._errorTexts4Req.get(85)
		)
	);
	this._felder.push(
		new Feld_t(
			86,
			"modelReference_2",
			"/ContractBusinessPartner/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelReference_2Validate",
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
			this._errorTexts4Req.get(86)
		)
	);
	this._felder.push(
		new Feld_t(
			87,
			"modelVersion_2",
			"/ContractBusinessPartner/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelVersion_2Validate",
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
			this._errorTexts4Req.get(87)
		)
	);
	this._felder.push(
		new Feld_t(
			88,
			"creator_2",
			"/ContractBusinessPartner/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcreator_2Validate",
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
			this._errorTexts4Req.get(88)
		)
	);
	this._felder.push(
		new Feld_t(
			89,
			"createdAt_2",
			"/ContractBusinessPartner/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmcreatedAt_2Validate",
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
			this._errorTexts4Req.get(89)
		)
	);
	this._felder.push(
		new Feld_t(
			90,
			"modifier_2",
			"/ContractBusinessPartner/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodifier_2Validate",
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
			this._errorTexts4Req.get(90)
		)
	);
	this._felder.push(
		new Feld_t(
			91,
			"modifiedAt_2",
			"/ContractBusinessPartner/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmmodifiedAt_2Validate",
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
			this._errorTexts4Req.get(91)
		)
	);
	this._felder.push(
		new Feld_t(
			92,
			"original_filename_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/original_filename",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmoriginal_filename_1Validate",
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
			this._errorTexts4Req.get(92)
		)
	);
	this._felder.push(
		new Feld_t(
			93,
			"internal_filename_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/internal_filename",
			false,
			true,
			this._metaFormate.get(0),
			"contractcdminternal_filename_1Validate",
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
			this._errorTexts4Req.get(93)
		)
	);
	this._felder.push(
		new Feld_t(
			94,
			"content_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/content",
			false,
			false,
			this._metaFormate.get(18),
			"contractcdmcontent_1Validate",
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
			this._errorTexts4Req.get(94)
		)
	);
	this._felder.push(
		new Feld_t(
			95,
			"attachment_id_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/attachment_id",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmattachment_id_1Validate",
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
			this._errorTexts4Req.get(95)
		)
	);
	this._felder.push(
		new Feld_t(
			96,
			"size_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/size",
			false,
			false,
			this._metaFormate.get(3),
			"contractcdmsize_1Validate",
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
			this._errorTexts4Req.get(96)
		)
	);
	this._felder.push(
		new Feld_t(
			97,
			"mime_type_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/mime_type",
			false,
			true,
			this._metaFormate.get(0),
			"contractcdmmime_type_1Validate",
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
			this._errorTexts4Req.get(97)
		)
	);
	this._felder.push(
		new Feld_t(
			98,
			"category_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/category",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcategory_1Validate",
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
			this._errorTexts4Req.get(98)
		)
	);
	this._felder.push(
		new Feld_t(
			99,
			"description_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/description",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdescription_1Validate",
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
			this._errorTexts4Req.get(99)
		)
	);
	this._felder.push(
		new Feld_t(
			100,
			"Name_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Name",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmName_1Validate",
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
			this._errorTexts4Req.get(100)
		)
	);
	this._felder.push(
		new Feld_t(
			101,
			"Industry_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Industry",
			false,
			false,
			this._metaFormate.get(19),
			"contractcdmIndustry_1Validate",
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
			this._errorTexts4Req.get(101)
		)
	);
	this._felder.push(
		new Feld_t(
			102,
			"StartOfRelationship_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/StartOfRelationship",
			false,
			false,
			this._metaFormate.get(7),
			"contractcdmStartOfRelationship_1Validate",
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
			this._errorTexts4Req.get(102)
		)
	);
	this._felder.push(
		new Feld_t(
			103,
			"CustomerDiscount_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/CustomerDiscount",
			false,
			false,
			this._metaFormate.get(20),
			"contractcdmCustomerDiscount_1Validate",
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
			this._errorTexts4Req.get(103)
		)
	);
	this._felder.push(
		new Feld_t(
			104,
			"income_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/income",
			false,
			false,
			this._metaFormate.get(21),
			"contractcdmincome_1Validate",
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
			this._errorTexts4Req.get(104)
		)
	);
	this._felder.push(
		new Feld_t(
			105,
			"tax_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax",
			false,
			false,
			this._metaFormate.get(21),
			"contractcdmtax_1Validate",
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
			this._errorTexts4Req.get(105)
		)
	);
	this._felder.push(
		new Feld_t(
			106,
			"PersonOrEntity_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/PersonOrEntity",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmPersonOrEntity_1Validate",
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
			this._errorTexts4Req.get(106)
		)
	);
	this._felder.push(
		new Feld_t(
			107,
			"Name_2",
			"/ContractCoInsuredPartner/relationship/CoInsuredRoot/Name",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmName_2Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Name of Policy Holder").put("en_US", "Name of Policy Holder").put("de", "Name des Versicherungsnehmers").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(107)
		)
	);
	this._felder.push(
		new Feld_t(
			108,
			"ID_0",
			"/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmID_0Validate",
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
			this._errorTexts4Req.get(108)
		)
	);
	this._felder.push(
		new Feld_t(
			109,
			"Role",
			"/ContractCoInsuredPartner/relationship/CoInsuredRoot/Role",
			false,
			false,
			this._metaFormate.get(24),
			"contractcdmRoleValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Role of co-insured partner").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(109)
		)
	);
	this._felder.push(
		new Feld_t(
			110,
			"docRef_4",
			"/ContractCoInsuredPartner/relationship/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdocRef_4Validate",
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
			this._errorTexts4Req.get(110)
		)
	);
	this._felder.push(
		new Feld_t(
			111,
			"modelReference_4",
			"/ContractCoInsuredPartner/relationship/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelReference_4Validate",
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
			this._errorTexts4Req.get(111)
		)
	);
	this._felder.push(
		new Feld_t(
			112,
			"modelVersion_4",
			"/ContractCoInsuredPartner/relationship/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelVersion_4Validate",
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
			this._errorTexts4Req.get(112)
		)
	);
	this._felder.push(
		new Feld_t(
			113,
			"creator_4",
			"/ContractCoInsuredPartner/relationship/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcreator_4Validate",
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
			this._errorTexts4Req.get(113)
		)
	);
	this._felder.push(
		new Feld_t(
			114,
			"createdAt_4",
			"/ContractCoInsuredPartner/relationship/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmcreatedAt_4Validate",
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
			this._errorTexts4Req.get(114)
		)
	);
	this._felder.push(
		new Feld_t(
			115,
			"modifier_4",
			"/ContractCoInsuredPartner/relationship/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodifier_4Validate",
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
			this._errorTexts4Req.get(115)
		)
	);
	this._felder.push(
		new Feld_t(
			116,
			"modifiedAt_4",
			"/ContractCoInsuredPartner/relationship/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmmodifiedAt_4Validate",
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
			this._errorTexts4Req.get(116)
		)
	);
	this._felder.push(
		new Feld_t(
			117,
			"docRef_3",
			"/ContractCoInsuredPartner/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdocRef_3Validate",
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
			this._errorTexts4Req.get(117)
		)
	);
	this._felder.push(
		new Feld_t(
			118,
			"modelReference_3",
			"/ContractCoInsuredPartner/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelReference_3Validate",
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
			this._errorTexts4Req.get(118)
		)
	);
	this._felder.push(
		new Feld_t(
			119,
			"modelVersion_3",
			"/ContractCoInsuredPartner/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelVersion_3Validate",
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
			this._errorTexts4Req.get(119)
		)
	);
	this._felder.push(
		new Feld_t(
			120,
			"creator_3",
			"/ContractCoInsuredPartner/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcreator_3Validate",
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
			this._errorTexts4Req.get(120)
		)
	);
	this._felder.push(
		new Feld_t(
			121,
			"createdAt_3",
			"/ContractCoInsuredPartner/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmcreatedAt_3Validate",
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
			this._errorTexts4Req.get(121)
		)
	);
	this._felder.push(
		new Feld_t(
			122,
			"modifier_3",
			"/ContractCoInsuredPartner/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodifier_3Validate",
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
			this._errorTexts4Req.get(122)
		)
	);
	this._felder.push(
		new Feld_t(
			123,
			"modifiedAt_3",
			"/ContractCoInsuredPartner/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmmodifiedAt_3Validate",
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
			this._errorTexts4Req.get(123)
		)
	);
	this._felder.push(
		new Feld_t(
			124,
			"CostToCustomerCDM",
			"/CalculatedFields/CostToCustomerCDM",
			false,
			false,
			this._metaFormate.get(2),
			"contractcdmCostToCustomerCDMValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Cost to Customer").build())
				.put("GlobalesFeld", false)
				.put("ExplicitlyMandatory", false)
				.put("TransientField", false)
				.put("IndexField", false).build(),
			new Set(),
			false,
			this._errorTexts4Req.get(124)
		)
	);
	this._felder.push(
		new Feld_t(
			125,
			"docRef_5",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmdocRef_5Validate",
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
			this._errorTexts4Req.get(125)
		)
	);
	this._felder.push(
		new Feld_t(
			126,
			"modelReference_5",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelReference_5Validate",
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
			this._errorTexts4Req.get(126)
		)
	);
	this._felder.push(
		new Feld_t(
			127,
			"modelVersion_5",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodelVersion_5Validate",
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
			this._errorTexts4Req.get(127)
		)
	);
	this._felder.push(
		new Feld_t(
			128,
			"creator_5",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmcreator_5Validate",
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
			this._errorTexts4Req.get(128)
		)
	);
	this._felder.push(
		new Feld_t(
			129,
			"createdAt_5",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmcreatedAt_5Validate",
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
			this._errorTexts4Req.get(129)
		)
	);
	this._felder.push(
		new Feld_t(
			130,
			"modifier_5",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmmodifier_5Validate",
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
			this._errorTexts4Req.get(130)
		)
	);
	this._felder.push(
		new Feld_t(
			131,
			"modifiedAt_5",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcdmmodifiedAt_5Validate",
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
			this._errorTexts4Req.get(131)
		)
	);
	};
	Meta_Felder_contractcdm.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(5, new Map());

	this._mehrsprachigeFehlertexte.set(12, new Map());
	this._mehrsprachigeFehlertexte.get(12).set("de", "Muster wird nicht erfüllt");this._mehrsprachigeFehlertexte.get(12).set("en", "Pattern is not met");this._mehrsprachigeFehlertexte.get(12).set("en_US", "Pattern is not met");
	this._mehrsprachigeFehlertexte.set(14, new Map());

	this._mehrsprachigeFehlertexte.set(15, new Map());

	this._mehrsprachigeFehlertexte.set(19, new Map());

	this._mehrsprachigeFehlertexte.set(20, new Map());

	this._mehrsprachigeFehlertexte.set(22, new Map());

	this._mehrsprachigeFehlertexte.set(24, new Map());

	};
	Meta_Felder_contractcdm.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(5, new Map());
	this._enumerationOberflaechenWerte.get(5).set("de", MetaFeldUtil.createPairList(["Haushaltsversicherung", "Reiseversicherung", "Krankenversicherung", "Haftpflichtversicherung"], ["Household", "Travel", "Health", "Liability"]));
	this._enumerationOberflaechenWerte.get(5).set("en", MetaFeldUtil.createPairList(["Household Insurance", "Travel Insurance", "Health Insurance", "Liability Insurance"], ["Household", "Travel", "Health", "Liability"]));
	this._enumerationOberflaechenWerte.get(5).set("en_US", MetaFeldUtil.createPairList(["Household Coverage", "Travel Coverage", "Health Coverage", "Liability Coverage"], ["Household", "Travel", "Health", "Liability"]));

	this._enumerationOberflaechenWerte.set(14, new Map());
	this._enumerationOberflaechenWerte.get(14).set("de", MetaFeldUtil.createPairList(["attrape", "entwurf", "uberpruft", "zuruckgesetzt", "genehmigt", "fertig", "bereitgestelt"], ["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"]));
	this._enumerationOberflaechenWerte.get(14).set("en", MetaFeldUtil.createPairList(["imitation", "blueprint", "examined", "returned", "accepted", "finished", "utilized"], ["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"]));
	this._enumerationOberflaechenWerte.get(14).set("en_US", MetaFeldUtil.createPairList(["imitation", "blueprint", "examined", "returned", "accepted", "finished", "utilized"], ["dummy", "draft", "reviewed", "reverted", "approved", "done", "deployed"]));

	this._enumerationOberflaechenWerte.set(15, new Map());
	this._enumerationOberflaechenWerte.get(15).set("de", MetaFeldUtil.createPairList(["banal", "niedrig", "medium", "hoch", "kritisch"], ["trivial", "low", "medium", "high", "critical"]));
	this._enumerationOberflaechenWerte.get(15).set("en", MetaFeldUtil.createPairList(["negligible", "small", "intermediate", "large", "demanding"], ["trivial", "low", "medium", "high", "critical"]));
	this._enumerationOberflaechenWerte.get(15).set("en_US", MetaFeldUtil.createPairList(["lesser", "low", "medium", "high", "critical"], ["trivial", "low", "medium", "high", "critical"]));

	this._enumerationOberflaechenWerte.set(19, new Map());
	this._enumerationOberflaechenWerte.get(19).set("de", MetaFeldUtil.createPairList(["Informationstechnologie", "Bankwesen", "Buchhaltung", "Gesundheitswesen", "Versicherung", "Gesetzlich", "Handel"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(19).set("en", MetaFeldUtil.createPairList(["Information Technology", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));
	this._enumerationOberflaechenWerte.get(19).set("en_US", MetaFeldUtil.createPairList(["Technology of Information", "Investment", "Bookkeeping", "HealthPrevention", "Liability", "Constitution", "Business"], ["IT", "Banking", "Accountancy", "Healthcare", "Risk", "Legal", "Commerce"]));

	this._enumerationOberflaechenWerte.set(20, new Map());
	this._enumerationOberflaechenWerte.get(20).set("de", MetaFeldUtil.createPairList(["0%_Rabbat", "10%_Rabbat", "20%_Rabbat", "30%_Rabbat", "40%_Rabbat", "50%_Rabbat", "60%_Rabbat", "70%_Rabbat", "80%_Rabbat", "90%_Rabbat"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(20).set("en", MetaFeldUtil.createPairList(["0%_Discount", "10%_Discount", "20%_Discount", "30%_Discount", "40%_Discount", "50%_Discount", "60%_Discount", "70%_Discount", "80%_Discount", "90%_Discount"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));
	this._enumerationOberflaechenWerte.get(20).set("en_US", MetaFeldUtil.createPairList(["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"], ["100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%"]));

	this._enumerationOberflaechenWerte.set(22, new Map());
	this._enumerationOberflaechenWerte.get(22).set("de", MetaFeldUtil.createPairList(["Wohnen", "Kommerziell", "Andere"], ["Residential", "Commercial", "Other"]));
	this._enumerationOberflaechenWerte.get(22).set("en", MetaFeldUtil.createPairList(["Residential", "Commercial", "Other"], ["Residential", "Commercial", "Other"]));
	this._enumerationOberflaechenWerte.get(22).set("en_US", MetaFeldUtil.createPairList(["Urban", "Retail", "Another"], ["Residential", "Commercial", "Other"]));

	this._enumerationOberflaechenWerte.set(24, new Map());
	this._enumerationOberflaechenWerte.get(24).set("de", MetaFeldUtil.createPairList(["Experte", "Berater", "Anwalt"], ["EXP", "CON", "LAW"]));
	this._enumerationOberflaechenWerte.get(24).set("en", MetaFeldUtil.createPairList(["Expert", "Consultant", "Barrister"], ["EXP", "CON", "LAW"]));
	this._enumerationOberflaechenWerte.get(24).set("en_US", MetaFeldUtil.createPairList(["Third Party Expert", "Chief Consultant Officer", "Attorney"], ["EXP", "CON", "LAW"]));

	};
	Meta_Felder_contractcdm.prototype._initEnumerationCategoryValues = function () {
	this._enumerationCategoryValues.set(14, new MapBuilder().put("dummy", new MapBuilder().put("Status", "Status").build()).put("draft", new MapBuilder().put("Status", "Status").build()).put("reviewed", new MapBuilder().put("Status", "Status").build()).put("reverted", new MapBuilder().put("Status", "Status").build()).put("approved", new MapBuilder().put("Status", "Status").build()).put("done", new MapBuilder().put("Status", "Status").build()).put("deployed", new MapBuilder().put("Status", "Status").build()).build());

	};


	return Meta_Felder_contractcdm;
}());
var Meta_Regel_contractcdm = (function () {
	function Meta_Regel_contractcdm() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_contractcdm.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_contractcdm.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_contractcdm.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_contractcdm.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_contractcdm.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_contractcdm.ANZAHL_REGELN = 15;

	Meta_Regel_contractcdm.prototype._initRegelRefs = function () {
	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	this._refIdentifier[0] = [
			new Identifier_t(36, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(37, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(38, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(39, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(40, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(41, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(42, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(43, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[0]= [
			new Identifier_t(37, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	this._refIdentifier[1] = [
			new Identifier_t(36, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(37, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(38, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(39, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(40, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(41, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(42, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(43, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[1]= [
			new Identifier_t(41, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	this._refIdentifier[2] = [
			new Identifier_t(36, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(37, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(38, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(39, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(40, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(41, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(42, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(43, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[2]= [
			new Identifier_t(38, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(39, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	this._refIdentifier[3] = [
			new Identifier_t(38, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(40, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[3]= [
			new Identifier_t(40, [1, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Employment/TaxComputation'
	this._refIdentifier[4] = [
			new Identifier_t(48, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(49, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[4]= [
			new Identifier_t(48, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(49, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/PartnerAddresses/AddressRoot/MustContainCountry'
	this._refIdentifier[5] = [
			new Identifier_t(52, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(53, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(54, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(55, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(56, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(57, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(58, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(59, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[5]= [
			new Identifier_t(58, [1, IIdentifier.ITERATION, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/MustContainCountry'
	this._refIdentifier[6] = [
			new Identifier_t(68, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(69, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(70, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(71, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(72, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(73, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(74, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(75, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[6]= [
			new Identifier_t(74, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddressComp'
	this._refIdentifier[7] = [
			new Identifier_t(68, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(69, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(70, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(71, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(72, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(73, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(74, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(75, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(76, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(77, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(78, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(79, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(80, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(81, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(82, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(83, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(84, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[7]= [
			new Identifier_t(76, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(78, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractBusinessPartner/PartnerPostalAddress/HasbusinessPartnerComp'
	this._refIdentifier[8] = [
			new Identifier_t(51, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(68, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(69, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(70, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(71, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(72, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(73, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(74, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(75, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(76, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(77, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(78, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(79, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(80, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(81, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(82, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(83, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(84, [1, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[8]= [
			new Identifier_t(51, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(77, [1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	this._refIdentifier[9] = [
			new Identifier_t(92, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(93, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(94, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(95, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(96, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(97, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(98, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(99, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[9]= [
			new Identifier_t(93, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	this._refIdentifier[10] = [
			new Identifier_t(92, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(93, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(94, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(95, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(96, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(97, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(98, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(99, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[10]= [
			new Identifier_t(97, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	this._refIdentifier[11] = [
			new Identifier_t(92, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(93, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(94, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(95, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(96, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(97, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(98, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(99, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[11]= [
			new Identifier_t(94, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(95, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	this._refIdentifier[12] = [
			new Identifier_t(94, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(96, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[12]= [
			new Identifier_t(96, [IIdentifier.ITERATION, 1, IIdentifier.ITERATION, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/TaxComputation'
	this._refIdentifier[13] = [
			new Identifier_t(104, [IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(105, [IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[13]= [
			new Identifier_t(104, [IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null),

			new Identifier_t(105, [IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	// Init Daten zur Regel '/ContractCoInsuredPartner/relationship/CoInsuredRoot/AddID'
	this._refIdentifier[14] = [
			new Identifier_t(108, [IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[14]= [
			new Identifier_t(108, [IIdentifier.ITERATION, 1, 1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	};

	Meta_Regel_contractcdm.prototype._initRegelFehlertexte = function () {
	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	this._mehrsprachigeFehlertexte.get(0).set("de", "Internal Error: Field $internal_filename$ of customType attachment is not filled.");this._mehrsprachigeFehlertexte.get(0).set("en", "Internal Error: Field $internal_filename$ of customType attachment is not filled.");
	this._fehlertexteParameterMapping.set(0, new MapBuilder().put("internal_filename", "internal_filename_0@1@1@e@1").build());
	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	this._mehrsprachigeFehlertexte.get(1).set("de", "Internal Error: Field $mime_type$ of customType attachment is not filled.");this._mehrsprachigeFehlertexte.get(1).set("en", "Internal Error: Field $mime_type$ of customType attachment is not filled.");
	this._fehlertexteParameterMapping.set(1, new MapBuilder().put("mime_type", "mime_type_0@1@1@e@1").build());
	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	this._mehrsprachigeFehlertexte.get(2).set("de", "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");this._mehrsprachigeFehlertexte.get(2).set("en", "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");
	this._fehlertexteParameterMapping.set(2, new MapBuilder().build());
	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	this._mehrsprachigeFehlertexte.get(3).set("de", "Internal Error: If the content is filled, the size must be also filled.");this._mehrsprachigeFehlertexte.get(3).set("en", "Internal Error: If the content is filled, the size must be also filled.");
	this._fehlertexteParameterMapping.set(3, new MapBuilder().build());
	// Init Daten zur Regel '/ContractBusinessPartner/BusinessPartnerRoot/Employment/TaxComputation'
	this._mehrsprachigeFehlertexte.get(4).set("de", "Berechnungsfehler für TaxComputation");this._mehrsprachigeFehlertexte.get(4).set("en", "Computation error for TaxComputation");
	this._fehlertexteParameterMapping.set(4, new MapBuilder().build());
	// Init Daten zur Regel '/ContractBusinessPartner/PartnerAddresses/AddressRoot/MustContainCountry'
	this._mehrsprachigeFehlertexte.get(5).set("de", "Kein Land angegeben");this._mehrsprachigeFehlertexte.get(5).set("en", "No country provided");
	this._fehlertexteParameterMapping.set(5, new MapBuilder().build());
	// Init Daten zur Regel '/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/MustContainCountry'
	this._mehrsprachigeFehlertexte.get(6).set("de", "Kein Land angegeben");this._mehrsprachigeFehlertexte.get(6).set("en", "No country provided");
	this._fehlertexteParameterMapping.set(6, new MapBuilder().build());
	// Init Daten zur Regel '/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddressComp'
	this._mehrsprachigeFehlertexte.get(7).set("de", "Fehlertext für die Berechnung von HasPostalAddressComp");this._mehrsprachigeFehlertexte.get(7).set("en", "error text for computation of HasPostalAddressComp");
	this._fehlertexteParameterMapping.set(7, new MapBuilder().build());
	// Init Daten zur Regel '/ContractBusinessPartner/PartnerPostalAddress/HasbusinessPartnerComp'
	this._mehrsprachigeFehlertexte.get(8).set("de", "Fehlertext zur Berechnung von HasbusinessPartnerComp");this._mehrsprachigeFehlertexte.get(8).set("en", "error text for computation of HasbusinessPartnerComp");
	this._fehlertexteParameterMapping.set(8, new MapBuilder().build());
	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	this._mehrsprachigeFehlertexte.get(9).set("de", "Internal Error: Field $internal_filename$ of customType attachment is not filled.");this._mehrsprachigeFehlertexte.get(9).set("en", "Internal Error: Field $internal_filename$ of customType attachment is not filled.");
	this._fehlertexteParameterMapping.set(9, new MapBuilder().put("internal_filename", "internal_filename_1@e@1@e@1").build());
	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	this._mehrsprachigeFehlertexte.get(10).set("de", "Internal Error: Field $mime_type$ of customType attachment is not filled.");this._mehrsprachigeFehlertexte.get(10).set("en", "Internal Error: Field $mime_type$ of customType attachment is not filled.");
	this._fehlertexteParameterMapping.set(10, new MapBuilder().put("mime_type", "mime_type_1@e@1@e@1").build());
	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	this._mehrsprachigeFehlertexte.get(11).set("de", "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");this._mehrsprachigeFehlertexte.get(11).set("en", "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");
	this._fehlertexteParameterMapping.set(11, new MapBuilder().build());
	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	this._mehrsprachigeFehlertexte.get(12).set("de", "Internal Error: If the content is filled, the size must be also filled.");this._mehrsprachigeFehlertexte.get(12).set("en", "Internal Error: If the content is filled, the size must be also filled.");
	this._fehlertexteParameterMapping.set(12, new MapBuilder().build());
	// Init Daten zur Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/TaxComputation'
	this._mehrsprachigeFehlertexte.get(13).set("de", "Berechnungsfehler für TaxComputation");this._mehrsprachigeFehlertexte.get(13).set("en", "Computation error for TaxComputation");
	this._fehlertexteParameterMapping.set(13, new MapBuilder().build());
	// Init Daten zur Regel '/ContractCoInsuredPartner/relationship/CoInsuredRoot/AddID'
	this._mehrsprachigeFehlertexte.get(14).set("de", "error text for computation of AddID");this._mehrsprachigeFehlertexte.get(14).set("en", "error text for computation of AddID");this._mehrsprachigeFehlertexte.get(14).set("en_US", "error text for computation of AddID");
	this._fehlertexteParameterMapping.set(14, new MapBuilder().build());
	};

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	Meta_Regel_contractcdm.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("AttachmentInternalFilenameRequired", "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", "ErrorR29", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/internal_filename", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentMimeTypeRequired", "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", "ErrorR30", this._mehrsprachigeFehlertexte.get(1), this._fehlertexteParameterMapping.get(1), "Fehler", this._refIdentifier[1], this._refAuslassungsIdentifier[1], null, "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/mime_type", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentIdOrContentFilled", "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", "ErrorR31", this._mehrsprachigeFehlertexte.get(2), this._fehlertexteParameterMapping.get(2), "Fehler", this._refIdentifier[2], this._refAuslassungsIdentifier[2], null, "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("SizeOfContentFilled", "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled", "ErrorR32", this._mehrsprachigeFehlertexte.get(3), this._fehlertexteParameterMapping.get(3), "Fehler", this._refIdentifier[3], this._refAuslassungsIdentifier[3], null, "/ContractBusinessPartner/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("TaxComputation", "/ContractBusinessPartner/BusinessPartnerRoot/Employment/TaxComputation", "TaxComputation", this._mehrsprachigeFehlertexte.get(4), this._fehlertexteParameterMapping.get(4), "Fehler", this._refIdentifier[4], this._refAuslassungsIdentifier[4], null, "/ContractBusinessPartner/BusinessPartnerRoot/Employment/tax", true, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("MustContainCountry", "/ContractBusinessPartner/PartnerAddresses/AddressRoot/MustContainCountry", "Error rule_7c66e", this._mehrsprachigeFehlertexte.get(5), this._fehlertexteParameterMapping.get(5), "Fehler", this._refIdentifier[5], this._refAuslassungsIdentifier[5], null, "/ContractBusinessPartner/PartnerAddresses/AddressRoot/Country", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("MustContainCountry", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/MustContainCountry", "Error rule_7c66e", this._mehrsprachigeFehlertexte.get(6), this._fehlertexteParameterMapping.get(6), "Fehler", this._refIdentifier[6], this._refAuslassungsIdentifier[6], null, "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Country", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("HasPostalAddressComp", "/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddressComp", "HasPostalAddressComp", this._mehrsprachigeFehlertexte.get(7), this._fehlertexteParameterMapping.get(7), "Fehler", this._refIdentifier[7], this._refAuslassungsIdentifier[7], null, "/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress", true, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("HasbusinessPartnerComp", "/ContractBusinessPartner/PartnerPostalAddress/HasbusinessPartnerComp", "HasbusinessPartnerComp", this._mehrsprachigeFehlertexte.get(8), this._fehlertexteParameterMapping.get(8), "Fehler", this._refIdentifier[8], this._refAuslassungsIdentifier[8], null, "/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner", true, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentInternalFilenameRequired", "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", "ErrorR29", this._mehrsprachigeFehlertexte.get(9), this._fehlertexteParameterMapping.get(9), "Fehler", this._refIdentifier[9], this._refAuslassungsIdentifier[9], null, "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/internal_filename", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentMimeTypeRequired", "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", "ErrorR30", this._mehrsprachigeFehlertexte.get(10), this._fehlertexteParameterMapping.get(10), "Fehler", this._refIdentifier[10], this._refAuslassungsIdentifier[10], null, "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/mime_type", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AttachmentIdOrContentFilled", "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", "ErrorR31", this._mehrsprachigeFehlertexte.get(11), this._fehlertexteParameterMapping.get(11), "Fehler", this._refIdentifier[11], this._refAuslassungsIdentifier[11], null, "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("SizeOfContentFilled", "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled", "ErrorR32", this._mehrsprachigeFehlertexte.get(12), this._fehlertexteParameterMapping.get(12), "Fehler", this._refIdentifier[12], this._refAuslassungsIdentifier[12], null, "/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("TaxComputation", "/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/TaxComputation", "TaxComputation", this._mehrsprachigeFehlertexte.get(13), this._fehlertexteParameterMapping.get(13), "Fehler", this._refIdentifier[13], this._refAuslassungsIdentifier[13], null, "/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax", true, false, new MapBuilder().build(), new Set()));

	this._regeln.push(new Regel_t("AddID", "/ContractCoInsuredPartner/relationship/CoInsuredRoot/AddID", "AddID", this._mehrsprachigeFehlertexte.get(14), this._fehlertexteParameterMapping.get(14), "Fehler", this._refIdentifier[14], this._refAuslassungsIdentifier[14], null, "/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID", true, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_contractcdm;
}());
var Meta_PfFeld_contractcdm = (function () {
	function Meta_PfFeld_contractcdm(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_contractcdm.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		this._addLokalePflichtFeldInfos_0(infos);

		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};

	Meta_PfFeld_contractcdm.prototype._addLokalePflichtFeldInfos_0 = function (infos) {
		// /ContractBusinessPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled&/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired&/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired
		if (infos != null) {
			infos[0] = [
					"content_0[1,1,-1,1];size_0[1,1,-1,1]",
					"internal_filename_0[1,1,-1,1];mime_type_0[1,1,-1,1];size_0[1,1,-1,1]"
			];

		}
		// /ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled&/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired&/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired
		if (infos != null) {
			infos[1] = [
					"content_1[-1,1,-1,1];size_1[-1,1,-1,1]",
					"internal_filename_1[-1,1,-1,1];mime_type_1[-1,1,-1,1];size_1[-1,1,-1,1]"
			];

		}
		// /ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired&/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired
		if (infos != null) {
			infos[2] = [
					"attachment_id_0[1,1,-1,1];category_0[1,1,-1,1];content_0[1,1,-1,1];description_0[1,1,-1,1];internal_filename_0[1,1,-1,1];mime_type_0[1,1,-1,1];original_filename_0[1,1,-1,1];size_0[1,1,-1,1]",
					"internal_filename_0[1,1,-1,1];mime_type_0[1,1,-1,1]"
			];

		}
		// /ContractBusinessPartner/PartnerAddresses/AddressRoot/MustContainCountry
		if (infos != null) {
			infos[3] = [
					"AddressType_0[1,-1,1,1];City_0[1,-1,1,1];Country_0[1,-1,1,1];HouseNumber_0[1,-1,1,1];Location_0[1,-1,1,1];PostCode_0[1,-1,1,1];PostalAddress_0[1,-1,1,1];Street_0[1,-1,1,1]",
					"Country_0[1,-1,1,1]"
			];

		}
		// /ContractBusinessPartner/PartnerPostalAddress/AddressRoot/MustContainCountry
		if (infos != null) {
			infos[4] = [
					"AddressType_1[1,1,1,1];City_1[1,1,1,1];Country_1[1,1,1,1];HouseNumber_1[1,1,1,1];Location_1[1,1,1,1];PostCode_1[1,1,1,1];PostalAddress_1[1,1,1,1];Street_1[1,1,1,1]",
					"Country_1[1,1,1,1]"
			];

		}
		// /ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired&/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired
		if (infos != null) {
			infos[5] = [
					"attachment_id_1[-1,1,-1,1];category_1[-1,1,-1,1];content_1[-1,1,-1,1];description_1[-1,1,-1,1];internal_filename_1[-1,1,-1,1];mime_type_1[-1,1,-1,1];original_filename_1[-1,1,-1,1];size_1[-1,1,-1,1]",
					"internal_filename_1[-1,1,-1,1];mime_type_1[-1,1,-1,1]"
			];

		}

	};



	Meta_PfFeld_contractcdm.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_contractcdm;
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

var Meta_contractcdm = (function () {
	function Meta_contractcdm() {
		this._metaFelder = new Meta_Felder_contractcdm();
		this._metaRegeln = new Meta_Regel_contractcdm();
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
					.put("ModelId", "ContractCDM")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest")
					.put("cdm.queryRoot", "Contract").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_contractcdm._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_contractcdm.getInstance() instead of new.");
		}
		Meta_contractcdm._instance = this;
		var container = Meta_contractcdm._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_contractcdm.LEGAL_CHARACTERS,
			Meta_contractcdm.LEGAL_GRAPHEME_TRIE,
			() => E_contractcdm.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_contractcdm(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_contractcdm, "RUNTIME_VERSION", { get: function () { return "31.1"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contractcdm, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contractcdm, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contractcdm, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contractcdm, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_contractcdm.getInstance = function () {
		return Meta_contractcdm._instance;
	};
	Meta_contractcdm.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_contractcdm.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_contractcdm._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/ContractRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ContractDates", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ChangeLog", 10000, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ChangeLog/Changes", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ChangeLog/Changes/SubChanges", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner", 1, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "ContractBusinessPartner")
			.put("cdm.sourceRole", "Contract")
			.put("cdm.targetRole", "Partner")
			.put("cdm.targetDocumentModel", "BusinessPartnerSuper").build()).build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/BusinessPartnerRoot/Attachment", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/BusinessPartnerRoot/Employment", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerAddresses", 100, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "PartnerAddresses")
			.put("cdm.sourceRole", "Partner")
			.put("cdm.targetRole", "Address")
			.put("cdm.targetDocumentModel", "Address").build()).build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerAddresses/AddressRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerAddresses/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerAddresses/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerPostalAddress", 1, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "PartnerPostalAddress")
			.put("cdm.sourceRole", "Partner")
			.put("cdm.targetRole", "Address")
			.put("cdm.targetDocumentModel", "Address").build()).build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerPostalAddress/AddressRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerPostalAddress/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/PartnerPostalAddress/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner", 100, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "ContractCoInsuredPartner")
			.put("cdm.sourceRole", "Contract")
			.put("cdm.targetRole", "Partner")
			.put("cdm.targetDocumentModel", "BusinessPartnerSuper").build()).build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/BusinessPartnerRoot/Employment", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship/CoInsuredRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/CalculatedFields", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_contractcdm.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_contractcdm.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_contractcdm._constructor = (function () {
		if (Meta_contractcdm.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_contractcdm.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_contractcdm._instance = new Meta_contractcdm();
	})();
	return Meta_contractcdm;
}());var E_contractcdm = (function () {
	function E_contractcdm() {
		if (E_contractcdm._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_contractcdm.getInstance() instead of new.");
		}
		E_contractcdm._valueCalcObject = this;
		this._feldData = Meta_contractcdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_contractcdm.getInstance = function () {
		return this._valueCalcObject;
	};


	E_contractcdm.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_contractcdm._valueCalcObject = new E_contractcdm();
	return E_contractcdm;
}());
var C_contractcdm = (function () {
	function C_contractcdm() {
		if (C_contractcdm._checkObject) {
			throw new Error("Instantiation failed: Use C_contractcdm.getInstance() instead of new.");
		}
		C_contractcdm._checkObject = this;
		this._feldData = Meta_contractcdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_contractcdm.getInstance = function () {
		return this._checkObject;
	};

	C_contractcdm.prototype.calc_tax_0 = function (controller) {
		// Berechnung für das Feld '/ContractBusinessPartner/BusinessPartnerRoot/Employment/tax'
		var start = [1, 1, 1, 1];
		var ende = [1, 1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax_0").isField(true).idx(1).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income_0").isField(true).idx(1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/ContractBusinessPartner/BusinessPartnerRoot/Employment/TaxComputation'
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
	C_contractcdm.prototype.calc_HasBusinessPartner = function (controller) {
		// Berechnung für das Feld '/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner'
		var start = [1, 1, 1];
		var ende = [1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("HasBusinessPartner").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/PartnerPostalAddress").isField(false).idx(1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("t_docRef_1").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon3).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/ContractBusinessPartner/PartnerPostalAddress/HasbusinessPartnerComp'
					if ((controller.mindestensEinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon2).isKnownAndTrue())) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("true", id);
						continue;
					}
				} catch (e) {
					var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
					controller.markiereAlsFehlerhaftBerechnet(id);
					continue;
				}
				try {// Berechnungsalternative aus Regel '/ContractBusinessPartner/PartnerPostalAddress/HasbusinessPartnerComp'
					if ((controller.mindestensEinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue())) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("false", id);
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
	C_contractcdm.prototype.calc_HasPostalAddress = function (controller) {
		// Berechnung für das Feld '/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress'
		var start = [1, 1, 1];
		var ende = [1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("HasPostalAddress").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/PartnerPostalAddress").isField(false).idx(1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("docRef_1").isField(true).idx(1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon3).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddressComp'
					if ((controller.mindestensEinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.mindestensEinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue())) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("true", id);
						continue;
					}
				} catch (e) {
					var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
					controller.markiereAlsFehlerhaftBerechnet(id);
					continue;
				}
				try {// Berechnungsalternative aus Regel '/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddressComp'
					if ((controller.mindestensEinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon2).isKnownAndTrue())) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("false", id);
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
	C_contractcdm.prototype.calc_tax_1 = function (controller) {
		// Berechnung für das Feld '/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax'
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax_1").isField(true).idx(-1).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("/ContractCoInsuredPartner").isField(false).idx(-1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("income_1").isField(true).idx(-1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon3).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/TaxComputation'
					if (controller.mindestensEinVordruckAngegeben("ContractCoInsuredPartner", idx.getIndexes()[0], idx, rtIdCon2).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert(controller.runden(controller.feldWertAlsZahl(idx, rtIdCon3).multiply(controller.makeDecimal("0.20")), 2), id);
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
	C_contractcdm.prototype.calc_ID_0 = function (controller) {
		// Berechnung für das Feld '/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID'
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, 1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("ID_0").isField(true).idx(-1).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("/ContractCoInsuredPartner").isField(false).idx(-1).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/ContractCoInsuredPartner/relationship/CoInsuredRoot/AddID'
					if (controller.mindestensEinVordruckAngegeben("ContractCoInsuredPartner", idx.getIndexes()[0], idx, rtIdCon2).isKnownAndTrue()) {
						var id = RuntimeController.makeIdentifier(idx, rtIdCon1);
						controller.handleBerechnetenWert("0000-0000-0000-0000", id);
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


	C_contractcdm.CALCULATED_FIELD_DEPENDENCIES = new Map([
		["/ContractBusinessPartner/BusinessPartnerRoot/Employment/tax", new Set(["/ContractBusinessPartner/BusinessPartnerRoot/Employment/income"])],
		["/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner", new Set(["/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Location", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modifiedAt", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modelReference", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostalAddress", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/City", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostCode", "/ContractBusinessPartner/PartnerPostalAddress/__meta/creator", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Country", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modelVersion", "/ContractBusinessPartner/t_docRef", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modifier", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/AddressType", "/ContractBusinessPartner/PartnerPostalAddress/__meta/docRef", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/HouseNumber", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Street", "/ContractBusinessPartner/PartnerPostalAddress/__meta/createdAt"])],
		["/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress", new Set(["/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Location", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modifiedAt", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modelReference", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostalAddress", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/City", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostCode", "/ContractBusinessPartner/PartnerPostalAddress/__meta/creator", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Country", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modelVersion", "/ContractBusinessPartner/PartnerPostalAddress/__meta/modifier", "/ContractBusinessPartner/PartnerPostalAddress/__meta/docRef", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/AddressType", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/HouseNumber", "/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Street", "/ContractBusinessPartner/PartnerPostalAddress/__meta/createdAt"])],
		["/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax", new Set(["/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/income"])],
		["/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID", new Set()]
	]);

	C_contractcdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set([
		"/ContractBusinessPartner/BusinessPartnerRoot/Employment/income",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/AddressType",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/City",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Country",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/HouseNumber",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Location",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostCode",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/PostalAddress",
		"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/Street",
		"/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner",
		"/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/createdAt",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/creator",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/docRef",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/modelReference",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/modelVersion",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/modifiedAt",
		"/ContractBusinessPartner/PartnerPostalAddress/__meta/modifier",
		"/ContractBusinessPartner/t_docRef",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/attachment_id",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/category",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/content",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/description",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/internal_filename",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/mime_type",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/original_filename",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/size",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/CustomerDiscount",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/income",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Industry",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Name",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/PersonOrEntity",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/StartOfRelationship",
		"/ContractCoInsuredPartner/__meta/createdAt",
		"/ContractCoInsuredPartner/__meta/creator",
		"/ContractCoInsuredPartner/__meta/docRef",
		"/ContractCoInsuredPartner/__meta/modelReference",
		"/ContractCoInsuredPartner/__meta/modelVersion",
		"/ContractCoInsuredPartner/__meta/modifiedAt",
		"/ContractCoInsuredPartner/__meta/modifier",
		"/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID",
		"/ContractCoInsuredPartner/relationship/CoInsuredRoot/Name",
		"/ContractCoInsuredPartner/relationship/CoInsuredRoot/Role",
		"/ContractCoInsuredPartner/relationship/__meta/createdAt",
		"/ContractCoInsuredPartner/relationship/__meta/creator",
		"/ContractCoInsuredPartner/relationship/__meta/docRef",
		"/ContractCoInsuredPartner/relationship/__meta/modelReference",
		"/ContractCoInsuredPartner/relationship/__meta/modelVersion",
		"/ContractCoInsuredPartner/relationship/__meta/modifiedAt",
		"/ContractCoInsuredPartner/relationship/__meta/modifier"
	]);

	C_contractcdm.CALC_FUNC_NAME_MAP = new Map([
		["/ContractBusinessPartner/BusinessPartnerRoot/Employment/tax", "calc_tax_0"],
		["/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner", "calc_HasBusinessPartner"],
		["/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress", "calc_HasPostalAddress"],
		["/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax", "calc_tax_1"],
		["/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID", "calc_ID_0"]
	]);

	C_contractcdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [
		"/ContractBusinessPartner/BusinessPartnerRoot/Employment/tax",
		"/ContractBusinessPartner/PartnerPostalAddress/HasBusinessPartner",
		"/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddress",
		"/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/tax",
		"/ContractCoInsuredPartner/relationship/CoInsuredRoot/ID"
	];

	C_contractcdm.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_contractcdm.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_contractcdm.prototype.hasCalculation = function(fullFieldName) {
        return C_contractcdm.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_contractcdm.prototype.getDependencyGraph = function() {
        return C_contractcdm.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_contractcdm.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_contractcdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_contractcdm.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_contractcdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_contractcdm._checkObject = new C_contractcdm();
	return C_contractcdm;
}());
var R_contractcdm = (function () {
	function R_contractcdm() {
		this._feldData = Meta_contractcdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_contractcdm.getInstance = function () {
		return R_contractcdm._checkObject;
	};
	R_contractcdm.prototype.mvk_AttachmentInternalFilenameRequired_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/BusinessPartnerRoot/Attachment").isField(false).idx(1).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("internal_filename_0").isField(true).idx(1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("internal_filename_0", idx, "ErrorR29", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("internal_filename_0", iter.get(), "ErrorR29", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_AttachmentMimeTypeRequired_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/BusinessPartnerRoot/Attachment").isField(false).idx(1).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("mime_type_0").isField(true).idx(1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("mime_type_0", idx, "ErrorR30", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("mime_type_0", iter.get(), "ErrorR30", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_AttachmentIdOrContentFilled_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/BusinessPartnerRoot/Attachment").isField(false).idx(1).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("attachment_id_0").isField(true).idx(1).idx(1).idx(-1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("content_0").isField(true).idx(1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("content_0", idx, "ErrorR31", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("content_0", iter.get(), "ErrorR31", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_SizeOfContentFilled_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("content_0").isField(true).idx(1).idx(1).idx(-1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("size_0").isField(true).idx(1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("content_0", idx, "ErrorR32", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("content_0", iter.get(), "ErrorR32", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_TaxComputation_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/BusinessPartnerRoot/Employment/TaxComputation", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, 1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax_0").isField(true).idx(1).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income_0").isField(true).idx(1).idx(1).idx(1).idx(1).build();
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
						controller.addValidatorMessage("tax_0", idx, "TaxComputation", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("tax_0", iter.get(), "TaxComputation", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_MustContainCountry_0 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/PartnerAddresses/AddressRoot/MustContainCountry", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, Math.min(controller.getMaxGesetzterKontext(1), 100), 1, 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/PartnerAddresses/AddressRoot").isField(false).idx(1).idx(-1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("Country_0").isField(true).idx(1).idx(-1).idx(1).idx(1).build();
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
						controller.addValidatorMessage("Country_0", idx, "Error rule_7c66e", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("Country_0", iter.get(), "Error rule_7c66e", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_MustContainCountry_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/MustContainCountry", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [1, 1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/PartnerPostalAddress/AddressRoot").isField(false).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("Country_1").isField(true).idx(1).idx(1).idx(1).idx(1).build();
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
						controller.addValidatorMessage("Country_1", idx, "Error rule_7c66e", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("Country_1", iter.get(), "Error rule_7c66e", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_HasPostalAddressComp = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/PartnerPostalAddress/HasPostalAddressComp", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("HasPostalAddress").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/PartnerPostalAddress").isField(false).idx(1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("docRef_1").isField(true).idx(1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();
		var allRtIdCon3 = new ListBuilder().add(rtIdCon3).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&(((controller.alleKontexteAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.mindestensEinFeldAngegeben(idx, allRtIdCon3).isKnownAndTrue())&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "true").isKnownAndTrue())||((controller.alleKontexteAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon3).isKnownAndTrue())&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "false").isKnownAndTrue()))) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.alleKontexteAngegeben(idx, allRtIdCon2);
						var tb2 = controller.mindestensEinFeldAngegeben(idx, allRtIdCon3);
						var tb3 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "true");
						var tb4 = controller.alleKontexteAngegeben(idx, allRtIdCon2);
						var tb5 = controller.keinFeldAngegeben(idx, allRtIdCon3);
						var tb6 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "false");
						var valErg = tb0.combineUND(tb1.combineUND(tb2).combineUND(tb3).combineODER(tb4.combineUND(tb5).combineUND(tb6)));
						controller.addValidatorMessage("HasPostalAddress", idx, "HasPostalAddressComp", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("HasPostalAddress", iter.get(), "HasPostalAddressComp", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_HasbusinessPartnerComp = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractBusinessPartner/PartnerPostalAddress/HasbusinessPartnerComp", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1];
		var ende = [1, 1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("HasBusinessPartner").isField(true).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("/ContractBusinessPartner/PartnerPostalAddress").isField(false).idx(1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("t_docRef_1").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();
		var allRtIdCon2 = new ListBuilder().add(rtIdCon2).build();
		var allRtIdCon3 = new ListBuilder().add(rtIdCon3).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&(((controller.alleKontexteAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.alleFelderAngegeben(idx, allRtIdCon3).isKnownAndTrue())&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "true").isKnownAndTrue())||((controller.alleKontexteAngegeben(idx, allRtIdCon2).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon3).isKnownAndTrue())&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "false").isKnownAndTrue()))) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.alleKontexteAngegeben(idx, allRtIdCon2);
						var tb2 = controller.alleFelderAngegeben(idx, allRtIdCon3);
						var tb3 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "true");
						var tb4 = controller.alleKontexteAngegeben(idx, allRtIdCon2);
						var tb5 = controller.keinFeldAngegeben(idx, allRtIdCon3);
						var tb6 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "false");
						var valErg = tb0.combineUND(tb1.combineUND(tb2).combineUND(tb3).combineODER(tb4.combineUND(tb5).combineUND(tb6)));
						controller.addValidatorMessage("HasBusinessPartner", idx, "HasbusinessPartnerComp", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("HasBusinessPartner", iter.get(), "HasbusinessPartnerComp", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_AttachmentInternalFilenameRequired_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment").isField(false).idx(-1).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("internal_filename_1").isField(true).idx(-1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("internal_filename_1", idx, "ErrorR29", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("internal_filename_1", iter.get(), "ErrorR29", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_AttachmentMimeTypeRequired_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment").isField(false).idx(-1).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("mime_type_1").isField(true).idx(-1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("mime_type_1", idx, "ErrorR30", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("mime_type_1", iter.get(), "ErrorR30", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_AttachmentIdOrContentFilled_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment").isField(false).idx(-1).idx(1).idx(-1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("attachment_id_1").isField(true).idx(-1).idx(1).idx(-1).idx(1).build();
		var rtIdCon3 = RtIdentifierTemplate.builder().unqNm("content_1").isField(true).idx(-1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("content_1", idx, "ErrorR31", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("content_1", iter.get(), "ErrorR31", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_SizeOfContentFilled_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment/SizeOfContentFilled", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, Math.min(controller.getMaxGesetzterKontext(2), 99), 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("content_1").isField(true).idx(-1).idx(1).idx(-1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("size_1").isField(true).idx(-1).idx(1).idx(-1).idx(1).build();
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
						controller.addValidatorMessage("content_1", idx, "ErrorR32", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("content_1", iter.get(), "ErrorR32", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_TaxComputation_1 = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractCoInsuredPartner/BusinessPartnerRoot/Employment/TaxComputation", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, 1, 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("tax_1").isField(true).idx(-1).idx(1).idx(1).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("income_1").isField(true).idx(-1).idx(1).idx(1).idx(1).build();
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
						controller.addValidatorMessage("tax_1", idx, "TaxComputation", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("tax_1", iter.get(), "TaxComputation", e);
			}
		}
	};
	R_contractcdm.prototype.mvk_AddID = function (controller, indices) {
		controller.initFuerRegelpruefung("/ContractCoInsuredPartner/relationship/CoInsuredRoot/AddID", PruefErgebnisTyp.Fehler);
		var start = [1, 1, 1, 1];
		var ende = [Math.min(controller.getMaxGesetzterKontext(0), 100), 1, 1, 1];

		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
		if (!iter.checkAndSetWiederholungen(indices)) {
			return;
		}

		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("ID_0").isField(true).idx(-1).idx(1).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "0000-0000-0000-0000").isKnownAndTrue()) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "0000-0000-0000-0000");
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("ID_0", idx, "AddID", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("ID_0", iter.get(), "AddID", e);
			}
		}
	};


	R_contractcdm.vordruckF__ContractBusinessPartner = function (controller) {
		/* Aufruf der Regeln fuer Vordruck ContractBusinessPartner
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("ContractBusinessPartner", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("ContractBusinessPartner", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruckF__ContractBusinessPartner_lfdNr(controller, indices);
				}
			}
		}
	};
	R_contractcdm.vordruckF__ContractBusinessPartner_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck ContractBusinessPartner für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentInternalFilenameRequired_0(controller, indices);
		controller.logMessung("mvk_AttachmentInternalFilenameRequired_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentMimeTypeRequired_0(controller, indices);
		controller.logMessung("mvk_AttachmentMimeTypeRequired_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentIdOrContentFilled_0(controller, indices);
		controller.logMessung("mvk_AttachmentIdOrContentFilled_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_SizeOfContentFilled_0(controller, indices);
		controller.logMessung("mvk_SizeOfContentFilled_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_TaxComputation_0(controller, indices);
		controller.logMessung("mvk_TaxComputation_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_MustContainCountry_0(controller, indices);
		controller.logMessung("mvk_MustContainCountry_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_MustContainCountry_1(controller, indices);
		controller.logMessung("mvk_MustContainCountry_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_HasPostalAddressComp(controller, indices);
		controller.logMessung("mvk_HasPostalAddressComp", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_HasbusinessPartnerComp(controller, indices);
		controller.logMessung("mvk_HasbusinessPartnerComp", indices[0], startMesspunkt);
	};
	R_contractcdm.vordruckF__ContractCoInsuredPartner = function (controller) {
		/* Aufruf der Regeln fuer Vordruck ContractCoInsuredPartner
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("ContractCoInsuredPartner", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 100);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("ContractCoInsuredPartner", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruckF__ContractCoInsuredPartner_lfdNr(controller, indices);
				}
			}
		}
	};
	R_contractcdm.vordruckF__ContractCoInsuredPartner_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck ContractCoInsuredPartner für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentInternalFilenameRequired_1(controller, indices);
		controller.logMessung("mvk_AttachmentInternalFilenameRequired_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentMimeTypeRequired_1(controller, indices);
		controller.logMessung("mvk_AttachmentMimeTypeRequired_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentIdOrContentFilled_1(controller, indices);
		controller.logMessung("mvk_AttachmentIdOrContentFilled_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_SizeOfContentFilled_1(controller, indices);
		controller.logMessung("mvk_SizeOfContentFilled_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_TaxComputation_1(controller, indices);
		controller.logMessung("mvk_TaxComputation_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AddID(controller, indices);
		controller.logMessung("mvk_AddID", indices[0], startMesspunkt);
	};
	R_contractcdm.vordruck__ContractBusinessPartner = function (controller) {
		/* Aufruf der Regeln fuer Vordruck ContractBusinessPartner
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("ContractBusinessPartner", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("ContractBusinessPartner", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruck__ContractBusinessPartner_lfdNr(controller, indices);
				}
			}
		}
	};
	R_contractcdm.vordruck__ContractBusinessPartner_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck ContractBusinessPartner für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentInternalFilenameRequired_0(controller, indices);
		controller.logMessung("mvk_AttachmentInternalFilenameRequired_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentMimeTypeRequired_0(controller, indices);
		controller.logMessung("mvk_AttachmentMimeTypeRequired_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentIdOrContentFilled_0(controller, indices);
		controller.logMessung("mvk_AttachmentIdOrContentFilled_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_SizeOfContentFilled_0(controller, indices);
		controller.logMessung("mvk_SizeOfContentFilled_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_TaxComputation_0(controller, indices);
		controller.logMessung("mvk_TaxComputation_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_MustContainCountry_0(controller, indices);
		controller.logMessung("mvk_MustContainCountry_0", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_MustContainCountry_1(controller, indices);
		controller.logMessung("mvk_MustContainCountry_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_HasPostalAddressComp(controller, indices);
		controller.logMessung("mvk_HasPostalAddressComp", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_HasbusinessPartnerComp(controller, indices);
		controller.logMessung("mvk_HasbusinessPartnerComp", indices[0], startMesspunkt);
	};
	R_contractcdm.vordruck__ContractCoInsuredPartner = function (controller) {
		/* Aufruf der Regeln fuer Vordruck ContractCoInsuredPartner
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("ContractCoInsuredPartner", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 100);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("ContractCoInsuredPartner", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruck__ContractCoInsuredPartner_lfdNr(controller, indices);
				}
			}
		}
	};
	R_contractcdm.vordruck__ContractCoInsuredPartner_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck ContractCoInsuredPartner für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentInternalFilenameRequired_1(controller, indices);
		controller.logMessung("mvk_AttachmentInternalFilenameRequired_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentMimeTypeRequired_1(controller, indices);
		controller.logMessung("mvk_AttachmentMimeTypeRequired_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AttachmentIdOrContentFilled_1(controller, indices);
		controller.logMessung("mvk_AttachmentIdOrContentFilled_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_SizeOfContentFilled_1(controller, indices);
		controller.logMessung("mvk_SizeOfContentFilled_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_TaxComputation_1(controller, indices);
		controller.logMessung("mvk_TaxComputation_1", indices[0], startMesspunkt);
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AddID(controller, indices);
		controller.logMessung("mvk_AddID", indices[0], startMesspunkt);
	};



	R_contractcdm.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_contractcdm["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_contractcdm, controller);
		}
	};

	R_contractcdm.prototype.validatePreliminaryRules = function(controller) {
};

	R_contractcdm.prototype.validiereVoll = function (controller, modus) {
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
	R_contractcdm.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_contractcdm();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_contractcdm();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_contractcdm();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_contractcdm();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_contractcdm();
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
	R_contractcdm.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_contractcdm.vordruck__ContractBusinessPartner(controller);
		R_contractcdm.vordruck__ContractCoInsuredPartner(controller);
};
	R_contractcdm.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_contractcdm.vordruckF__ContractBusinessPartner(controller);
		R_contractcdm.vordruckF__ContractCoInsuredPartner(controller);
};
	R_contractcdm.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_contractcdm.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_contractcdm._checkObject = new R_contractcdm();
	return R_contractcdm;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_contractcdm = (function () {
	function TVCheckFehler_contractcdm() {
		this._checkObject = R_contractcdm.getInstance();
	}
	TVCheckFehler_contractcdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 108) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [37,108]
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

	TVCheckFehler_contractcdm.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
		var startMesspunkt = -1;
		switch (interneFeldNr) {
			case 37:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentInternalFilenameRequired_0(controller, indices);
				controller.logMessung("mvk_AttachmentInternalFilenameRequired_0", indices[0], startMesspunkt);
				break;
			case 38:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentIdOrContentFilled_0(controller, indices);
				controller.logMessung("mvk_AttachmentIdOrContentFilled_0", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_SizeOfContentFilled_0(controller, indices);
				controller.logMessung("mvk_SizeOfContentFilled_0", indices[0], startMesspunkt);
				break;
			case 41:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentMimeTypeRequired_0(controller, indices);
				controller.logMessung("mvk_AttachmentMimeTypeRequired_0", indices[0], startMesspunkt);
				break;
			case 49:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_TaxComputation_0(controller, indices);
				controller.logMessung("mvk_TaxComputation_0", indices[0], startMesspunkt);
				break;
			case 58:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_MustContainCountry_0(controller, indices);
				controller.logMessung("mvk_MustContainCountry_0", indices[0], startMesspunkt);
				break;
			case 74:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_MustContainCountry_1(controller, indices);
				controller.logMessung("mvk_MustContainCountry_1", indices[0], startMesspunkt);
				break;
			case 76:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_HasPostalAddressComp(controller, indices);
				controller.logMessung("mvk_HasPostalAddressComp", indices[0], startMesspunkt);
				break;
			case 77:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_HasbusinessPartnerComp(controller, indices);
				controller.logMessung("mvk_HasbusinessPartnerComp", indices[0], startMesspunkt);
				break;
			case 93:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentInternalFilenameRequired_1(controller, indices);
				controller.logMessung("mvk_AttachmentInternalFilenameRequired_1", indices[0], startMesspunkt);
				break;
			case 94:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentIdOrContentFilled_1(controller, indices);
				controller.logMessung("mvk_AttachmentIdOrContentFilled_1", indices[0], startMesspunkt);
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_SizeOfContentFilled_1(controller, indices);
				controller.logMessung("mvk_SizeOfContentFilled_1", indices[0], startMesspunkt);
				break;
			case 97:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AttachmentMimeTypeRequired_1(controller, indices);
				controller.logMessung("mvk_AttachmentMimeTypeRequired_1", indices[0], startMesspunkt);
				break;
			case 105:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_TaxComputation_1(controller, indices);
				controller.logMessung("mvk_TaxComputation_1", indices[0], startMesspunkt);
				break;
			case 108:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AddID(controller, indices);
				controller.logMessung("mvk_AddID", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 108) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [37,108].");
			}
			break;
		}
	};

	return TVCheckFehler_contractcdm;
}());
var TVCheckHinweis_contractcdm = (function () {
	function TVCheckHinweis_contractcdm() {
		this._checkObject = R_contractcdm.getInstance();
	}
	TVCheckHinweis_contractcdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_contractcdm;
}());
var TVCheckInfo_contractcdm = (function () {
	function TVCheckInfo_contractcdm() {
		this._checkObject = R_contractcdm.getInstance();
	}
	TVCheckInfo_contractcdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_contractcdm;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_contractcdm = (function () {
	function TVCheckAlle_contractcdm() {
		this._tvFehlerChecker = new TVCheckFehler_contractcdm();
		this._tvHinweisChecker = new TVCheckHinweis_contractcdm();
		this._tvInfoChecker = new TVCheckInfo_contractcdm();
	}
	TVCheckAlle_contractcdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_contractcdm;
}());
var TVCheckPreliminary_contractcdm = (function () {
	function TVCheckPreliminary_contractcdm() {
		this._checkObject = R_contractcdm.getInstance();
	}
	TVCheckPreliminary_contractcdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_contractcdm;
}());
var ERValidator_contractcdm = (function () {
	function ERValidator_contractcdm() {
		if (ERValidator_contractcdm._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_contractcdm.getInstance() instead of new.");
		}
		ERValidator_contractcdm._instance = this;
	}
	ERValidator_contractcdm.getInstance = function () {
		return ERValidator_contractcdm._instance;
	};
	ERValidator_contractcdm.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_contractcdm.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_contractcdm.getInstance(), modus, ERValidator_contractcdm._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_contractcdm.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_contractcdm.getInstance(), modus, ERValidator_contractcdm._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_contractcdm.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_contractcdm.getInstance(), modus, ERValidator_contractcdm._metaData, logger, C_contractcdm.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_contractcdm._instance = new ERValidator_contractcdm();
	ERValidator_contractcdm._metaData = Meta_contractcdm.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_contractcdm._constructor = (function () {
	})();
	return ERValidator_contractcdm;
}());

return {
	validator: ERValidator_contractcdm,
	meta: Meta_contractcdm
};
