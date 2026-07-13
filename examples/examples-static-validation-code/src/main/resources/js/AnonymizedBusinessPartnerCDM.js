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
var Meta_Felder_anonymizedbusinesspartnercdm = (function () {
	function Meta_Felder_anonymizedbusinesspartnercdm() {
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
	Meta_Felder_anonymizedbusinesspartnercdm.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_anonymizedbusinesspartnercdm.prototype._initFeldtypen = function () {
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
		new FormatDefinitionDatum(
			"yyyy-MM-dd", // Datumsformat
			false, // Zusatzprüfung
			DatumTeilbekanntArt.BEKANNT // TeilbekanntArt
		)
	);
	this._metaFormate.set(
		2,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(2),
			["100%", "90%", "80%", "50%"],
			this._enumerationOberflaechenWerte.get(2),
			this._enumerationCategoryValues.get(2),
			""
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
	};

	/*
	 * Die übergebenen Parameter bedeuten (in aufsteigender Reihenfolge):
	 * interne Nummer, eindeutiger name, voller Pfadname, pflichtFeld, mandatoryForRepeatableGroup, Format,
	 * Formatdefinition, javaScriptName, vordruckPflichtfeld (lfd. Nummern), pflichtfeld
	 * (lfd. Nummern), zusätzliche Schlüssel-Werte Paare, berechnungsUebertragServer
	 */
	Meta_Felder_anonymizedbusinesspartnercdm.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"Industry",
			"/BusinessPartnerRoot/Industry",
			false,
			false,
			this._metaFormate.get(0),
			"anonymizedbusinesspartnercdmIndustryValidate",
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
			this._errorTexts4Req.get(0)
		)
	);
	this._felder.push(
		new Feld_t(
			1,
			"StartOfRelationship",
			"/BusinessPartnerRoot/StartOfRelationship",
			false,
			false,
			this._metaFormate.get(1),
			"anonymizedbusinesspartnercdmStartOfRelationshipValidate",
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
			this._errorTexts4Req.get(1)
		)
	);
	this._felder.push(
		new Feld_t(
			2,
			"CustomerDiscount",
			"/BusinessPartnerRoot/CustomerDiscount",
			false,
			false,
			this._metaFormate.get(2),
			"anonymizedbusinesspartnercdmCustomerDiscountValidate",
			[],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Customer Discount").build())
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
			"docRef",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(0),
			"anonymizedbusinesspartnercdmdocRefValidate",
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
			this._errorTexts4Req.get(3)
		)
	);
	this._felder.push(
		new Feld_t(
			4,
			"modelReference",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(0),
			"anonymizedbusinesspartnercdmmodelReferenceValidate",
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
			this._errorTexts4Req.get(4)
		)
	);
	this._felder.push(
		new Feld_t(
			5,
			"modelVersion",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(0),
			"anonymizedbusinesspartnercdmmodelVersionValidate",
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
			this._errorTexts4Req.get(5)
		)
	);
	this._felder.push(
		new Feld_t(
			6,
			"creator",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(0),
			"anonymizedbusinesspartnercdmcreatorValidate",
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
			this._errorTexts4Req.get(6)
		)
	);
	this._felder.push(
		new Feld_t(
			7,
			"createdAt",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
			"anonymizedbusinesspartnercdmcreatedAtValidate",
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
			this._errorTexts4Req.get(7)
		)
	);
	this._felder.push(
		new Feld_t(
			8,
			"modifier",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(0),
			"anonymizedbusinesspartnercdmmodifierValidate",
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
			this._errorTexts4Req.get(8)
		)
	);
	this._felder.push(
		new Feld_t(
			9,
			"modifiedAt",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
			"anonymizedbusinesspartnercdmmodifiedAtValidate",
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
			this._errorTexts4Req.get(9)
		)
	);
	};
	Meta_Felder_anonymizedbusinesspartnercdm.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(2, new Map());

	};
	Meta_Felder_anonymizedbusinesspartnercdm.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(2, new Map());
	this._enumerationOberflaechenWerte.get(2).set("en", MetaFeldUtil.createPairList(["no discount", "10% discount", "20% discount", "50% discount"], ["100%", "90%", "80%", "50%"]));
	this._enumerationOberflaechenWerte.get(2).set("en_US", MetaFeldUtil.createPairList(["100%", "90%", "80%", "50%"], ["100%", "90%", "80%", "50%"]));

	};


	return Meta_Felder_anonymizedbusinesspartnercdm;
}());
var Meta_Regel_anonymizedbusinesspartnercdm = (function () {
	function Meta_Regel_anonymizedbusinesspartnercdm() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_anonymizedbusinesspartnercdm.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_anonymizedbusinesspartnercdm.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_anonymizedbusinesspartnercdm.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_anonymizedbusinesspartnercdm.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
	}
	Meta_Regel_anonymizedbusinesspartnercdm.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_anonymizedbusinesspartnercdm.ANZAHL_REGELN = 0;



	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */

	return Meta_Regel_anonymizedbusinesspartnercdm;
}());
var Meta_PfFeld_anonymizedbusinesspartnercdm = (function () {
	function Meta_PfFeld_anonymizedbusinesspartnercdm(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_anonymizedbusinesspartnercdm.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};


	Meta_PfFeld_anonymizedbusinesspartnercdm.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_anonymizedbusinesspartnercdm;
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

var Meta_anonymizedbusinesspartnercdm = (function () {
	function Meta_anonymizedbusinesspartnercdm() {
		this._metaFelder = new Meta_Felder_anonymizedbusinesspartnercdm();
		this._metaRegeln = new Meta_Regel_anonymizedbusinesspartnercdm();
		this._unterstuetzteSprachen = ["en", "en_US"];
		this._da = new Model_t(
				"29.4.0", // Produkt-Version
				".", // dezimaltrenner
				["en", "en_US"], // unterstuetzte Sprachen
				MetaDataHelper.createSetOfStrings(), //  Namen der Applikationsbedingungen
				MetaDataHelper.createSetOfStrings(), // Names of custom field types
				// additional Values
				new MapBuilder()
					.put("BaseYear", null)
					.put("TimeZone", "UTC")
					.put("ModelId", "AnonymizedBusinessPartnerCDM")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest")
					.put("cdm.queryRoot", "BusinessPartner").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_anonymizedbusinesspartnercdm._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_anonymizedbusinesspartnercdm.getInstance() instead of new.");
		}
		Meta_anonymizedbusinesspartnercdm._instance = this;
		var container = Meta_anonymizedbusinesspartnercdm._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_anonymizedbusinesspartnercdm.LEGAL_CHARACTERS,
			Meta_anonymizedbusinesspartnercdm.LEGAL_GRAPHEME_TRIE,
			() => E_anonymizedbusinesspartnercdm.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_anonymizedbusinesspartnercdm(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_anonymizedbusinesspartnercdm, "RUNTIME_VERSION", { get: function () { return "31.1"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_anonymizedbusinesspartnercdm, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_anonymizedbusinesspartnercdm, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_anonymizedbusinesspartnercdm, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_anonymizedbusinesspartnercdm, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_anonymizedbusinesspartnercdm.getInstance = function () {
		return Meta_anonymizedbusinesspartnercdm._instance;
	};
	Meta_anonymizedbusinesspartnercdm.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_anonymizedbusinesspartnercdm.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_anonymizedbusinesspartnercdm._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/BusinessPartnerRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_anonymizedbusinesspartnercdm.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_anonymizedbusinesspartnercdm.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_anonymizedbusinesspartnercdm._constructor = (function () {
		if (Meta_anonymizedbusinesspartnercdm.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_anonymizedbusinesspartnercdm.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_anonymizedbusinesspartnercdm._instance = new Meta_anonymizedbusinesspartnercdm();
	})();
	return Meta_anonymizedbusinesspartnercdm;
}());var E_anonymizedbusinesspartnercdm = (function () {
	function E_anonymizedbusinesspartnercdm() {
		if (E_anonymizedbusinesspartnercdm._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_anonymizedbusinesspartnercdm.getInstance() instead of new.");
		}
		E_anonymizedbusinesspartnercdm._valueCalcObject = this;
		this._feldData = Meta_anonymizedbusinesspartnercdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_anonymizedbusinesspartnercdm.getInstance = function () {
		return this._valueCalcObject;
	};


	E_anonymizedbusinesspartnercdm.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_anonymizedbusinesspartnercdm._valueCalcObject = new E_anonymizedbusinesspartnercdm();
	return E_anonymizedbusinesspartnercdm;
}());
var C_anonymizedbusinesspartnercdm = (function () {
	function C_anonymizedbusinesspartnercdm() {
		if (C_anonymizedbusinesspartnercdm._checkObject) {
			throw new Error("Instantiation failed: Use C_anonymizedbusinesspartnercdm.getInstance() instead of new.");
		}
		C_anonymizedbusinesspartnercdm._checkObject = this;
		this._feldData = Meta_anonymizedbusinesspartnercdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_anonymizedbusinesspartnercdm.getInstance = function () {
		return this._checkObject;
	};


	C_anonymizedbusinesspartnercdm.CALCULATED_FIELD_DEPENDENCIES = new Map();

	C_anonymizedbusinesspartnercdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set();

	C_anonymizedbusinesspartnercdm.CALC_FUNC_NAME_MAP = new Map();

	C_anonymizedbusinesspartnercdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [];

	C_anonymizedbusinesspartnercdm.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_anonymizedbusinesspartnercdm.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_anonymizedbusinesspartnercdm.prototype.hasCalculation = function(fullFieldName) {
        return C_anonymizedbusinesspartnercdm.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_anonymizedbusinesspartnercdm.prototype.getDependencyGraph = function() {
        return C_anonymizedbusinesspartnercdm.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_anonymizedbusinesspartnercdm.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_anonymizedbusinesspartnercdm.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_anonymizedbusinesspartnercdm.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_anonymizedbusinesspartnercdm.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_anonymizedbusinesspartnercdm._checkObject = new C_anonymizedbusinesspartnercdm();
	return C_anonymizedbusinesspartnercdm;
}());
var R_anonymizedbusinesspartnercdm = (function () {
	function R_anonymizedbusinesspartnercdm() {
		this._feldData = Meta_anonymizedbusinesspartnercdm.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_anonymizedbusinesspartnercdm.getInstance = function () {
		return R_anonymizedbusinesspartnercdm._checkObject;
	};



	R_anonymizedbusinesspartnercdm.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_anonymizedbusinesspartnercdm["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_anonymizedbusinesspartnercdm, controller);
		}
	};

	R_anonymizedbusinesspartnercdm.prototype.validatePreliminaryRules = function(controller) {
};

	R_anonymizedbusinesspartnercdm.prototype.validiereVoll = function (controller, modus) {
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
	R_anonymizedbusinesspartnercdm.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_anonymizedbusinesspartnercdm();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_anonymizedbusinesspartnercdm();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_anonymizedbusinesspartnercdm();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_anonymizedbusinesspartnercdm();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_anonymizedbusinesspartnercdm();
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
	R_anonymizedbusinesspartnercdm.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
};
	R_anonymizedbusinesspartnercdm.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
};
	R_anonymizedbusinesspartnercdm.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_anonymizedbusinesspartnercdm.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_anonymizedbusinesspartnercdm._checkObject = new R_anonymizedbusinesspartnercdm();
	return R_anonymizedbusinesspartnercdm;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_anonymizedbusinesspartnercdm = (function () {
	function TVCheckFehler_anonymizedbusinesspartnercdm() {
		this._checkObject = R_anonymizedbusinesspartnercdm.getInstance();
	}
	TVCheckFehler_anonymizedbusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'ERROR'.
	};


	return TVCheckFehler_anonymizedbusinesspartnercdm;
}());
var TVCheckHinweis_anonymizedbusinesspartnercdm = (function () {
	function TVCheckHinweis_anonymizedbusinesspartnercdm() {
		this._checkObject = R_anonymizedbusinesspartnercdm.getInstance();
	}
	TVCheckHinweis_anonymizedbusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_anonymizedbusinesspartnercdm;
}());
var TVCheckInfo_anonymizedbusinesspartnercdm = (function () {
	function TVCheckInfo_anonymizedbusinesspartnercdm() {
		this._checkObject = R_anonymizedbusinesspartnercdm.getInstance();
	}
	TVCheckInfo_anonymizedbusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_anonymizedbusinesspartnercdm;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_anonymizedbusinesspartnercdm = (function () {
	function TVCheckAlle_anonymizedbusinesspartnercdm() {
		this._tvFehlerChecker = new TVCheckFehler_anonymizedbusinesspartnercdm();
		this._tvHinweisChecker = new TVCheckHinweis_anonymizedbusinesspartnercdm();
		this._tvInfoChecker = new TVCheckInfo_anonymizedbusinesspartnercdm();
	}
	TVCheckAlle_anonymizedbusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_anonymizedbusinesspartnercdm;
}());
var TVCheckPreliminary_anonymizedbusinesspartnercdm = (function () {
	function TVCheckPreliminary_anonymizedbusinesspartnercdm() {
		this._checkObject = R_anonymizedbusinesspartnercdm.getInstance();
	}
	TVCheckPreliminary_anonymizedbusinesspartnercdm.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_anonymizedbusinesspartnercdm;
}());
var ERValidator_anonymizedbusinesspartnercdm = (function () {
	function ERValidator_anonymizedbusinesspartnercdm() {
		if (ERValidator_anonymizedbusinesspartnercdm._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_anonymizedbusinesspartnercdm.getInstance() instead of new.");
		}
		ERValidator_anonymizedbusinesspartnercdm._instance = this;
	}
	ERValidator_anonymizedbusinesspartnercdm.getInstance = function () {
		return ERValidator_anonymizedbusinesspartnercdm._instance;
	};
	ERValidator_anonymizedbusinesspartnercdm.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_anonymizedbusinesspartnercdm.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_anonymizedbusinesspartnercdm.getInstance(), modus, ERValidator_anonymizedbusinesspartnercdm._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_anonymizedbusinesspartnercdm.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_anonymizedbusinesspartnercdm.getInstance(), modus, ERValidator_anonymizedbusinesspartnercdm._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_anonymizedbusinesspartnercdm.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_anonymizedbusinesspartnercdm.getInstance(), modus, ERValidator_anonymizedbusinesspartnercdm._metaData, logger, C_anonymizedbusinesspartnercdm.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_anonymizedbusinesspartnercdm._instance = new ERValidator_anonymizedbusinesspartnercdm();
	ERValidator_anonymizedbusinesspartnercdm._metaData = Meta_anonymizedbusinesspartnercdm.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_anonymizedbusinesspartnercdm._constructor = (function () {
	})();
	return ERValidator_anonymizedbusinesspartnercdm;
}());

return {
	validator: ERValidator_anonymizedbusinesspartnercdm,
	meta: Meta_anonymizedbusinesspartnercdm
};
