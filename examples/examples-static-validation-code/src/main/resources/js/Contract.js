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
var Meta_Felder_contract = (function () {
	function Meta_Felder_contract() {
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
	Meta_Felder_contract.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_contract.prototype._initFeldtypen = function () {
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
	};

	/*
	 * Die übergebenen Parameter bedeuten (in aufsteigender Reihenfolge):
	 * interne Nummer, eindeutiger name, voller Pfadname, pflichtFeld, mandatoryForRepeatableGroup, Format,
	 * Formatdefinition, javaScriptName, vordruckPflichtfeld (lfd. Nummern), pflichtfeld
	 * (lfd. Nummern), zusätzliche Schlüssel-Werte Paare, berechnungsUebertragServer
	 */
	Meta_Felder_contract.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"ContractName",
			"/ContractRoot/ContractName",
			false,
			false,
			this._metaFormate.get(0),
			"contractContractNameValidate",
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
			"contractContractDescriptionValidate",
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
			"contractLengthOfContractValidate",
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
			"contractContractValueValidate",
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
			"contractLiabilityValidate",
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
			"contractCostToCustomerValidate",
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
			"contractNoOfCoInsuredCustomersValidate",
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
			"contractMaxDiscountValidate",
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
			"contractCostPerCoInsuredValidate",
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
			"contractValidValidate",
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
			"contractTypeValidate",
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
			"contractContractStartDateValidate",
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
			"contractContractEndDateValidate",
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
			"contractContractEndTimeValidate",
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
			"contractContractReviewDateValidate",
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
			"contractLastPremiumPaidDateValidate",
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
			"contractCoveragePeriodValidate",
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
			"ID_2",
			"/ContractRoot/ChangeLog/ID",
			false,
			false,
			this._metaFormate.get(12),
			"contractID_2Validate",
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
			"contractNumberValidate",
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
			"contractChangeTimestampValidate",
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
			"contractDescriptionValidate",
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
			"contractUserValidate",
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
			"contractStatus_2Validate",
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
			"contractPriority_2Validate",
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
			"ID_0",
			"/ContractRoot/ChangeLog/Changes/ID",
			false,
			false,
			this._metaFormate.get(12),
			"contractID_0Validate",
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
			"contractTitleValidate",
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
			"contractDetails_0Validate",
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
			"contractPriority_0Validate",
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
			"contractStatus_0Validate",
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
			"contractLastModifiedValidate",
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
			"ID_1",
			"/ContractRoot/ChangeLog/Changes/SubChanges/ID",
			false,
			false,
			this._metaFormate.get(12),
			"contractID_1Validate",
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
			"contractTItleValidate",
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
			"contractDetails_1Validate",
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
			"contractPriority_1Validate",
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
			"contractStatus_1Validate",
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
			"contractCreatedAtValidate",
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
			"docRef",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"contractdocRefValidate",
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
			this._errorTexts4Req.get(36)
		)
	);
	this._felder.push(
		new Feld_t(
			37,
			"modelReference",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"contractmodelReferenceValidate",
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
			this._errorTexts4Req.get(37)
		)
	);
	this._felder.push(
		new Feld_t(
			38,
			"modelVersion",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"contractmodelVersionValidate",
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
			this._errorTexts4Req.get(38)
		)
	);
	this._felder.push(
		new Feld_t(
			39,
			"creator",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"contractcreatorValidate",
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
			this._errorTexts4Req.get(39)
		)
	);
	this._felder.push(
		new Feld_t(
			40,
			"createdAt",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractcreatedAtValidate",
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
			this._errorTexts4Req.get(40)
		)
	);
	this._felder.push(
		new Feld_t(
			41,
			"modifier",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"contractmodifierValidate",
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
			this._errorTexts4Req.get(41)
		)
	);
	this._felder.push(
		new Feld_t(
			42,
			"modifiedAt",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(6),
			"contractmodifiedAtValidate",
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
			this._errorTexts4Req.get(42)
		)
	);
	};
	Meta_Felder_contract.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(5, new Map());

	this._mehrsprachigeFehlertexte.set(12, new Map());
	this._mehrsprachigeFehlertexte.get(12).set("de", "Muster wird nicht erfüllt");this._mehrsprachigeFehlertexte.get(12).set("en", "Pattern is not met");this._mehrsprachigeFehlertexte.get(12).set("en_US", "Pattern is not met");
	this._mehrsprachigeFehlertexte.set(14, new Map());

	this._mehrsprachigeFehlertexte.set(15, new Map());

	};
	Meta_Felder_contract.prototype._initEnumerationOberflaechenWerte = function () {
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

	};
	Meta_Felder_contract.prototype._initEnumerationCategoryValues = function () {
	this._enumerationCategoryValues.set(14, new MapBuilder().put("dummy", new MapBuilder().put("Status", "Status").build()).put("draft", new MapBuilder().put("Status", "Status").build()).put("reviewed", new MapBuilder().put("Status", "Status").build()).put("reverted", new MapBuilder().put("Status", "Status").build()).put("approved", new MapBuilder().put("Status", "Status").build()).put("done", new MapBuilder().put("Status", "Status").build()).put("deployed", new MapBuilder().put("Status", "Status").build()).build());

	};


	return Meta_Felder_contract;
}());
var Meta_Regel_contract = (function () {
	function Meta_Regel_contract() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_contract.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_contract.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_contract.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_contract.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
	}
	Meta_Regel_contract.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_contract.ANZAHL_REGELN = 0;



	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */

	return Meta_Regel_contract;
}());
var Meta_PfFeld_contract = (function () {
	function Meta_PfFeld_contract(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_contract.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};


	Meta_PfFeld_contract.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_contract;
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

var Meta_contract = (function () {
	function Meta_contract() {
		this._metaFelder = new Meta_Felder_contract();
		this._metaRegeln = new Meta_Regel_contract();
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
					.put("ModelId", "Contract")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest,ModelRead").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_contract._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_contract.getInstance() instead of new.");
		}
		Meta_contract._instance = this;
		var container = Meta_contract._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_contract.LEGAL_CHARACTERS,
			Meta_contract.LEGAL_GRAPHEME_TRIE,
			() => E_contract.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_contract(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_contract, "RUNTIME_VERSION", { get: function () { return "30.8"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contract, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contract, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contract, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_contract, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_contract.getInstance = function () {
		return Meta_contract._instance;
	};
	Meta_contract.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_contract.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_contract._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/ContractRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ContractDates", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ChangeLog", 10000, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ChangeLog/Changes", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractRoot/ChangeLog/Changes/SubChanges", 99, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_contract.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_contract.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_contract._constructor = (function () {
		if (Meta_contract.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_contract.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_contract._instance = new Meta_contract();
	})();
	return Meta_contract;
}());var E_contract = (function () {
	function E_contract() {
		if (E_contract._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_contract.getInstance() instead of new.");
		}
		E_contract._valueCalcObject = this;
		this._feldData = Meta_contract.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_contract.getInstance = function () {
		return this._valueCalcObject;
	};


	E_contract.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_contract._valueCalcObject = new E_contract();
	return E_contract;
}());
var C_contract = (function () {
	function C_contract() {
		if (C_contract._checkObject) {
			throw new Error("Instantiation failed: Use C_contract.getInstance() instead of new.");
		}
		C_contract._checkObject = this;
		this._feldData = Meta_contract.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_contract.getInstance = function () {
		return this._checkObject;
	};


	C_contract.CALCULATED_FIELD_DEPENDENCIES = new Map();

	C_contract.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set();

	C_contract.CALC_FUNC_NAME_MAP = new Map();

	C_contract.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [];

	C_contract.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_contract.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_contract.prototype.hasCalculation = function(fullFieldName) {
        return C_contract.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_contract.prototype.getDependencyGraph = function() {
        return C_contract.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_contract.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_contract.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_contract.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_contract.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_contract._checkObject = new C_contract();
	return C_contract;
}());
var R_contract = (function () {
	function R_contract() {
		this._feldData = Meta_contract.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_contract.getInstance = function () {
		return R_contract._checkObject;
	};



	R_contract.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_contract["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_contract, controller);
		}
	};

	R_contract.prototype.validatePreliminaryRules = function(controller) {
};

	R_contract.prototype.validiereVoll = function (controller, modus) {
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
	R_contract.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_contract();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_contract();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_contract();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_contract();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_contract();
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
	R_contract.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
};
	R_contract.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
};
	R_contract.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_contract.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_contract._checkObject = new R_contract();
	return R_contract;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_contract = (function () {
	function TVCheckFehler_contract() {
		this._checkObject = R_contract.getInstance();
	}
	TVCheckFehler_contract.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'ERROR'.
	};


	return TVCheckFehler_contract;
}());
var TVCheckHinweis_contract = (function () {
	function TVCheckHinweis_contract() {
		this._checkObject = R_contract.getInstance();
	}
	TVCheckHinweis_contract.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_contract;
}());
var TVCheckInfo_contract = (function () {
	function TVCheckInfo_contract() {
		this._checkObject = R_contract.getInstance();
	}
	TVCheckInfo_contract.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_contract;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_contract = (function () {
	function TVCheckAlle_contract() {
		this._tvFehlerChecker = new TVCheckFehler_contract();
		this._tvHinweisChecker = new TVCheckHinweis_contract();
		this._tvInfoChecker = new TVCheckInfo_contract();
	}
	TVCheckAlle_contract.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_contract;
}());
var TVCheckPreliminary_contract = (function () {
	function TVCheckPreliminary_contract() {
		this._checkObject = R_contract.getInstance();
	}
	TVCheckPreliminary_contract.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_contract;
}());
var ERValidator_contract = (function () {
	function ERValidator_contract() {
		if (ERValidator_contract._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_contract.getInstance() instead of new.");
		}
		ERValidator_contract._instance = this;
	}
	ERValidator_contract.getInstance = function () {
		return ERValidator_contract._instance;
	};
	ERValidator_contract.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_contract.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_contract.getInstance(), modus, ERValidator_contract._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_contract.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_contract.getInstance(), modus, ERValidator_contract._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_contract.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_contract.getInstance(), modus, ERValidator_contract._metaData, logger, C_contract.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_contract._instance = new ERValidator_contract();
	ERValidator_contract._metaData = Meta_contract.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_contract._constructor = (function () {
	})();
	return ERValidator_contract;
}());

return {
	validator: ERValidator_contract,
	meta: Meta_contract
};
