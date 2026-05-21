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
		2,
		new FormatDefinitionJaNein(
			"true", // jaWert
			"false" // neinWert

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
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		5,
		new FormatDefinitionDatum(
			"HH:mm:ss", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		6,
		new FormatDefinitionDatum(
			"yyyy", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		7,
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.JAHR_OPTIONAL // TeilbekanntArt
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
			"ContractValue",
			"/ContractRoot/ContractValue",
			false,
			false,
			this._metaFormate.get(1),
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
			this._errorTexts4Req.get(1)
		)
	);
	this._felder.push(
		new Feld_t(
			2,
			"Valid",
			"/ContractRoot/Valid",
			false,
			false,
			this._metaFormate.get(2),
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
			this._errorTexts4Req.get(2)
		)
	);
	this._felder.push(
		new Feld_t(
			3,
			"Type",
			"/ContractRoot/Type",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(3)
		)
	);
	this._felder.push(
		new Feld_t(
			4,
			"ContractStartDate",
			"/ContractRoot/ContractDates/ContractStartDate",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(4)
		)
	);
	this._felder.push(
		new Feld_t(
			5,
			"ContractEndDate",
			"/ContractRoot/ContractDates/ContractEndDate",
			false,
			false,
			this._metaFormate.get(4),
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
			this._errorTexts4Req.get(5)
		)
	);
	this._felder.push(
		new Feld_t(
			6,
			"ContractEndTime",
			"/ContractRoot/ContractDates/ContractEndTime",
			false,
			false,
			this._metaFormate.get(5),
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
			this._errorTexts4Req.get(6)
		)
	);
	this._felder.push(
		new Feld_t(
			7,
			"ContractReviewDate",
			"/ContractRoot/ContractDates/ContractReviewDate",
			false,
			false,
			this._metaFormate.get(6),
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
			this._errorTexts4Req.get(7)
		)
	);
	this._felder.push(
		new Feld_t(
			8,
			"LastPremiumPaidDate",
			"/ContractRoot/ContractDates/LastPremiumPaidDate",
			false,
			false,
			this._metaFormate.get(7),
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
			this._errorTexts4Req.get(8)
		)
	);
	this._felder.push(
		new Feld_t(
			9,
			"CoveragePeriod",
			"/ContractRoot/ContractDates/CoveragePeriod",
			false,
			false,
			this._metaFormate.get(8),
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
			this._errorTexts4Req.get(9)
		)
	);
	this._felder.push(
		new Feld_t(
			10,
			"Number",
			"/ContractRoot/ChangeLog/Number",
			false,
			false,
			this._metaFormate.get(9),
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
			this._errorTexts4Req.get(10)
		)
	);
	this._felder.push(
		new Feld_t(
			11,
			"ChangeTimestamp",
			"/ContractRoot/ChangeLog/ChangeTimestamp",
			false,
			false,
			this._metaFormate.get(4),
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
			this._errorTexts4Req.get(11)
		)
	);
	this._felder.push(
		new Feld_t(
			12,
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
			this._errorTexts4Req.get(12)
		)
	);
	this._felder.push(
		new Feld_t(
			13,
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
			this._errorTexts4Req.get(13)
		)
	);
	this._felder.push(
		new Feld_t(
			14,
			"Status_1",
			"/ContractRoot/ChangeLog/Status",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(14)
		)
	);
	this._felder.push(
		new Feld_t(
			15,
			"Priority_1",
			"/ContractRoot/ChangeLog/Priority",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(15)
		)
	);
	this._felder.push(
		new Feld_t(
			16,
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
			this._errorTexts4Req.get(16)
		)
	);
	this._felder.push(
		new Feld_t(
			17,
			"Details",
			"/ContractRoot/ChangeLog/Changes/Details",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmDetailsValidate",
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
			this._errorTexts4Req.get(17)
		)
	);
	this._felder.push(
		new Feld_t(
			18,
			"Priority_0",
			"/ContractRoot/ChangeLog/Changes/Priority",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(18)
		)
	);
	this._felder.push(
		new Feld_t(
			19,
			"Status_0",
			"/ContractRoot/ChangeLog/Changes/Status",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(19)
		)
	);
	this._felder.push(
		new Feld_t(
			20,
			"Name_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Name",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmName_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Name").put("de", "Name").build())
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
			"Industry_0",
			"/ContractBusinessPartner/BusinessPartnerRoot/Industry",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmIndustry_0Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Industry").put("de", "Industrie").build())
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
			this._errorTexts4Req.get(22)
		)
	);
	this._felder.push(
		new Feld_t(
			23,
			"AddressType_0",
			"/ContractBusinessPartner/PartnerAddresses/AddressRoot/AddressType",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(23)
		)
	);
	this._felder.push(
		new Feld_t(
			24,
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
			this._errorTexts4Req.get(24)
		)
	);
	this._felder.push(
		new Feld_t(
			25,
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
			this._errorTexts4Req.get(25)
		)
	);
	this._felder.push(
		new Feld_t(
			26,
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
			this._errorTexts4Req.get(26)
		)
	);
	this._felder.push(
		new Feld_t(
			27,
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
			this._errorTexts4Req.get(27)
		)
	);
	this._felder.push(
		new Feld_t(
			28,
			"createdAt_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(28)
		)
	);
	this._felder.push(
		new Feld_t(
			29,
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
			this._errorTexts4Req.get(29)
		)
	);
	this._felder.push(
		new Feld_t(
			30,
			"modifiedAt_0",
			"/ContractBusinessPartner/PartnerAddresses/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(30)
		)
	);
	this._felder.push(
		new Feld_t(
			31,
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
			this._errorTexts4Req.get(31)
		)
	);
	this._felder.push(
		new Feld_t(
			32,
			"AddressType_1",
			"/ContractBusinessPartner/PartnerPostalAddress/AddressRoot/AddressType",
			false,
			false,
			this._metaFormate.get(0),
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
			this._errorTexts4Req.get(32)
		)
	);
	this._felder.push(
		new Feld_t(
			33,
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
			this._errorTexts4Req.get(33)
		)
	);
	this._felder.push(
		new Feld_t(
			34,
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
			this._errorTexts4Req.get(34)
		)
	);
	this._felder.push(
		new Feld_t(
			35,
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
			this._errorTexts4Req.get(35)
		)
	);
	this._felder.push(
		new Feld_t(
			36,
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
			this._errorTexts4Req.get(36)
		)
	);
	this._felder.push(
		new Feld_t(
			37,
			"createdAt_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(37)
		)
	);
	this._felder.push(
		new Feld_t(
			38,
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
			this._errorTexts4Req.get(38)
		)
	);
	this._felder.push(
		new Feld_t(
			39,
			"modifiedAt_1",
			"/ContractBusinessPartner/PartnerPostalAddress/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(39)
		)
	);
	this._felder.push(
		new Feld_t(
			40,
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
			this._errorTexts4Req.get(40)
		)
	);
	this._felder.push(
		new Feld_t(
			41,
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
			this._errorTexts4Req.get(41)
		)
	);
	this._felder.push(
		new Feld_t(
			42,
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
			this._errorTexts4Req.get(42)
		)
	);
	this._felder.push(
		new Feld_t(
			43,
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
			this._errorTexts4Req.get(43)
		)
	);
	this._felder.push(
		new Feld_t(
			44,
			"createdAt_2",
			"/ContractBusinessPartner/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(44)
		)
	);
	this._felder.push(
		new Feld_t(
			45,
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
			this._errorTexts4Req.get(45)
		)
	);
	this._felder.push(
		new Feld_t(
			46,
			"modifiedAt_2",
			"/ContractBusinessPartner/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(46)
		)
	);
	this._felder.push(
		new Feld_t(
			47,
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
			this._errorTexts4Req.get(47)
		)
	);
	this._felder.push(
		new Feld_t(
			48,
			"Industry_1",
			"/ContractCoInsuredPartner/BusinessPartnerRoot/Industry",
			false,
			false,
			this._metaFormate.get(0),
			"contractcdmIndustry_1Validate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Industry").build())
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
			this._errorTexts4Req.get(49)
		)
	);
	this._felder.push(
		new Feld_t(
			50,
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
			this._errorTexts4Req.get(50)
		)
	);
	this._felder.push(
		new Feld_t(
			51,
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
			this._errorTexts4Req.get(51)
		)
	);
	this._felder.push(
		new Feld_t(
			52,
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
			this._errorTexts4Req.get(52)
		)
	);
	this._felder.push(
		new Feld_t(
			53,
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
			this._errorTexts4Req.get(53)
		)
	);
	this._felder.push(
		new Feld_t(
			54,
			"createdAt_4",
			"/ContractCoInsuredPartner/relationship/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(54)
		)
	);
	this._felder.push(
		new Feld_t(
			55,
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
			this._errorTexts4Req.get(55)
		)
	);
	this._felder.push(
		new Feld_t(
			56,
			"modifiedAt_4",
			"/ContractCoInsuredPartner/relationship/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(56)
		)
	);
	this._felder.push(
		new Feld_t(
			57,
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
			this._errorTexts4Req.get(57)
		)
	);
	this._felder.push(
		new Feld_t(
			58,
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
			this._errorTexts4Req.get(58)
		)
	);
	this._felder.push(
		new Feld_t(
			59,
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
			this._errorTexts4Req.get(59)
		)
	);
	this._felder.push(
		new Feld_t(
			60,
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
			this._errorTexts4Req.get(60)
		)
	);
	this._felder.push(
		new Feld_t(
			61,
			"createdAt_3",
			"/ContractCoInsuredPartner/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(61)
		)
	);
	this._felder.push(
		new Feld_t(
			62,
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
			this._errorTexts4Req.get(62)
		)
	);
	this._felder.push(
		new Feld_t(
			63,
			"modifiedAt_3",
			"/ContractCoInsuredPartner/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(63)
		)
	);
	this._felder.push(
		new Feld_t(
			64,
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
			this._errorTexts4Req.get(64)
		)
	);
	this._felder.push(
		new Feld_t(
			65,
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
			this._errorTexts4Req.get(65)
		)
	);
	this._felder.push(
		new Feld_t(
			66,
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
			this._errorTexts4Req.get(66)
		)
	);
	this._felder.push(
		new Feld_t(
			67,
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
			this._errorTexts4Req.get(67)
		)
	);
	this._felder.push(
		new Feld_t(
			68,
			"createdAt_5",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(68)
		)
	);
	this._felder.push(
		new Feld_t(
			69,
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
			this._errorTexts4Req.get(69)
		)
	);
	this._felder.push(
		new Feld_t(
			70,
			"modifiedAt_5",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
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
			this._errorTexts4Req.get(70)
		)
	);
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
	}
	Meta_Regel_contractcdm.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_contractcdm.ANZAHL_REGELN = 0;



	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */

	return Meta_Regel_contractcdm;
}());
var Meta_PfFeld_contractcdm = (function () {
	function Meta_PfFeld_contractcdm(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_contractcdm.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
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
				"28.4.0", // Produkt-Version
				".", // dezimaltrenner
				["en", "en_US", "de"], // unterstuetzte Sprachen
				MetaDataHelper.createSetOfStrings(), //  Namen der Applikationsbedingungen
				MetaDataHelper.createSetOfStrings(), // Names of custom field types
				// additional Values
				new MapBuilder()
					.put("BaseYear", null)
					.put("TimeZone", "UTC")
					.put("ModelId", "ContractCompactCDM")
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
	Object.defineProperty(Meta_contractcdm, "RUNTIME_VERSION", { get: function () { return "30.8"; },
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

		result.push(new Container_t("/ContractBusinessPartner", 1, [], new MapBuilder()
			.put("Annotation", new MapBuilder()
			.put("cdm.relationship", "ContractBusinessPartner")
			.put("cdm.sourceRole", "Contract")
			.put("cdm.targetRole", "Partner")
			.put("cdm.targetDocumentModel", "BusinessPartnerSuper").build()).build(), new Set()));

		result.push(new Container_t("/ContractBusinessPartner/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

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

		result.push(new Container_t("/ContractCoInsuredPartner/relationship", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship/CoInsuredRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/relationship/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/ContractCoInsuredPartner/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));

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


	C_contractcdm.CALCULATED_FIELD_DEPENDENCIES = new Map();

	C_contractcdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set();

	C_contractcdm.CALC_FUNC_NAME_MAP = new Map();

	C_contractcdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [];

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
};
	R_contractcdm.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
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
			// The model does not contain any rules of severity 'ERROR'.
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
