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
var Meta_Felder_coinsuredadditionalfields = (function () {
	function Meta_Felder_coinsuredadditionalfields() {
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
	Meta_Felder_coinsuredadditionalfields.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_coinsuredadditionalfields.prototype._initFeldtypen = function () {
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
			this._mehrsprachigeFehlertexte.get(1),
			["EXP", "CON", "LAW"],
			this._enumerationOberflaechenWerte.get(1),
			this._enumerationCategoryValues.get(1),
			""
		)
	);
	this._metaFormate.set(
		2,
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
	Meta_Felder_coinsuredadditionalfields.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"Name",
			"/CoInsuredRoot/Name",
			false,
			false,
			this._metaFormate.get(0),
			"coinsuredadditionalfieldsNameValidate",
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
			this._errorTexts4Req.get(0)
		)
	);
	this._felder.push(
		new Feld_t(
			1,
			"ID",
			"/CoInsuredRoot/ID",
			false,
			false,
			this._metaFormate.get(0),
			"coinsuredadditionalfieldsIDValidate",
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
			"Role",
			"/CoInsuredRoot/Role",
			false,
			false,
			this._metaFormate.get(1),
			"coinsuredadditionalfieldsRoleValidate",
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
			"coinsuredadditionalfieldsdocRefValidate",
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
			"coinsuredadditionalfieldsmodelReferenceValidate",
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
			"coinsuredadditionalfieldsmodelVersionValidate",
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
			"coinsuredadditionalfieldscreatorValidate",
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
			this._metaFormate.get(2),
			"coinsuredadditionalfieldscreatedAtValidate",
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
			"coinsuredadditionalfieldsmodifierValidate",
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
			this._metaFormate.get(2),
			"coinsuredadditionalfieldsmodifiedAtValidate",
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
	Meta_Felder_coinsuredadditionalfields.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(1, new Map());

	};
	Meta_Felder_coinsuredadditionalfields.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(1, new Map());
	this._enumerationOberflaechenWerte.get(1).set("de", MetaFeldUtil.createPairList(["Experte", "Berater", "Anwalt"], ["EXP", "CON", "LAW"]));
	this._enumerationOberflaechenWerte.get(1).set("en", MetaFeldUtil.createPairList(["Expert", "Consultant", "Barrister"], ["EXP", "CON", "LAW"]));
	this._enumerationOberflaechenWerte.get(1).set("en_US", MetaFeldUtil.createPairList(["Third Party Expert", "Chief Consultant Officer", "Attorney"], ["EXP", "CON", "LAW"]));

	};


	return Meta_Felder_coinsuredadditionalfields;
}());
var Meta_Regel_coinsuredadditionalfields = (function () {
	function Meta_Regel_coinsuredadditionalfields() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_coinsuredadditionalfields.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_coinsuredadditionalfields.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_coinsuredadditionalfields.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_coinsuredadditionalfields.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_coinsuredadditionalfields.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_coinsuredadditionalfields.ANZAHL_REGELN = 1;

	Meta_Regel_coinsuredadditionalfields.prototype._initRegelRefs = function () {
	// Init Daten zur Regel '/CoInsuredRoot/AddID'
	this._refIdentifier[0] = [
			new Identifier_t(1, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[0]= [
			new Identifier_t(1, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	};

	Meta_Regel_coinsuredadditionalfields.prototype._initRegelFehlertexte = function () {
	// Init Daten zur Regel '/CoInsuredRoot/AddID'
	this._mehrsprachigeFehlertexte.get(0).set("de", "error text for computation of AddID");this._mehrsprachigeFehlertexte.get(0).set("en", "error text for computation of AddID");this._mehrsprachigeFehlertexte.get(0).set("en_US", "error text for computation of AddID");
	this._fehlertexteParameterMapping.set(0, new MapBuilder().build());
	};

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	Meta_Regel_coinsuredadditionalfields.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("AddID", "/CoInsuredRoot/AddID", "AddID", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/CoInsuredRoot/ID", true, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_coinsuredadditionalfields;
}());
var Meta_PfFeld_coinsuredadditionalfields = (function () {
	function Meta_PfFeld_coinsuredadditionalfields(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_coinsuredadditionalfields.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};


	Meta_PfFeld_coinsuredadditionalfields.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_coinsuredadditionalfields;
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

var Meta_coinsuredadditionalfields = (function () {
	function Meta_coinsuredadditionalfields() {
		this._metaFelder = new Meta_Felder_coinsuredadditionalfields();
		this._metaRegeln = new Meta_Regel_coinsuredadditionalfields();
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
					.put("ModelId", "CoInsuredAdditionalFields")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_coinsuredadditionalfields._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_coinsuredadditionalfields.getInstance() instead of new.");
		}
		Meta_coinsuredadditionalfields._instance = this;
		var container = Meta_coinsuredadditionalfields._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_coinsuredadditionalfields.LEGAL_CHARACTERS,
			Meta_coinsuredadditionalfields.LEGAL_GRAPHEME_TRIE,
			() => E_coinsuredadditionalfields.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_coinsuredadditionalfields(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_coinsuredadditionalfields, "RUNTIME_VERSION", { get: function () { return "31.1"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_coinsuredadditionalfields, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_coinsuredadditionalfields, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_coinsuredadditionalfields, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_coinsuredadditionalfields, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_coinsuredadditionalfields.getInstance = function () {
		return Meta_coinsuredadditionalfields._instance;
	};
	Meta_coinsuredadditionalfields.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_coinsuredadditionalfields.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_coinsuredadditionalfields._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/CoInsuredRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_coinsuredadditionalfields.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_coinsuredadditionalfields.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_coinsuredadditionalfields._constructor = (function () {
		if (Meta_coinsuredadditionalfields.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_coinsuredadditionalfields.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_coinsuredadditionalfields._instance = new Meta_coinsuredadditionalfields();
	})();
	return Meta_coinsuredadditionalfields;
}());var E_coinsuredadditionalfields = (function () {
	function E_coinsuredadditionalfields() {
		if (E_coinsuredadditionalfields._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_coinsuredadditionalfields.getInstance() instead of new.");
		}
		E_coinsuredadditionalfields._valueCalcObject = this;
		this._feldData = Meta_coinsuredadditionalfields.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_coinsuredadditionalfields.getInstance = function () {
		return this._valueCalcObject;
	};


	E_coinsuredadditionalfields.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_coinsuredadditionalfields._valueCalcObject = new E_coinsuredadditionalfields();
	return E_coinsuredadditionalfields;
}());
var C_coinsuredadditionalfields = (function () {
	function C_coinsuredadditionalfields() {
		if (C_coinsuredadditionalfields._checkObject) {
			throw new Error("Instantiation failed: Use C_coinsuredadditionalfields.getInstance() instead of new.");
		}
		C_coinsuredadditionalfields._checkObject = this;
		this._feldData = Meta_coinsuredadditionalfields.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_coinsuredadditionalfields.getInstance = function () {
		return this._checkObject;
	};

	C_coinsuredadditionalfields.prototype.calc_ID = function (controller) {
		// Berechnung für das Feld '/CoInsuredRoot/ID'
		var start = [1, 1];
		var ende = [1, 1];
		var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);


		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("ID").isField(true).idx(1).idx(1).build();
		try {
			var innerIterator = iter.iterator();
			while (innerIterator.hasNext()) {
				var idx = innerIterator.next();
				try {// Berechnungsalternative aus Regel '/CoInsuredRoot/AddID'
					if (true) {
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


	C_coinsuredadditionalfields.CALCULATED_FIELD_DEPENDENCIES = new Map([
		["/CoInsuredRoot/ID", new Set()]
	]);

	C_coinsuredadditionalfields.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set();

	C_coinsuredadditionalfields.CALC_FUNC_NAME_MAP = new Map([
		["/CoInsuredRoot/ID", "calc_ID"]
	]);

	C_coinsuredadditionalfields.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [
		"/CoInsuredRoot/ID"
	];

	C_coinsuredadditionalfields.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_coinsuredadditionalfields.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_coinsuredadditionalfields.prototype.hasCalculation = function(fullFieldName) {
        return C_coinsuredadditionalfields.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_coinsuredadditionalfields.prototype.getDependencyGraph = function() {
        return C_coinsuredadditionalfields.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_coinsuredadditionalfields.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_coinsuredadditionalfields.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_coinsuredadditionalfields.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_coinsuredadditionalfields.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_coinsuredadditionalfields._checkObject = new C_coinsuredadditionalfields();
	return C_coinsuredadditionalfields;
}());
var R_coinsuredadditionalfields = (function () {
	function R_coinsuredadditionalfields() {
		this._feldData = Meta_coinsuredadditionalfields.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_coinsuredadditionalfields.getInstance = function () {
		return R_coinsuredadditionalfields._checkObject;
	};
	R_coinsuredadditionalfields.prototype.mvk_AddID = function (controller, indices) {
		controller.initFuerRegelpruefung("/CoInsuredRoot/AddID", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("ID").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon1).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.alleFelderAngegeben(idx, allRtIdCon1).isKnownAndTrue()&&controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "0000-0000-0000-0000").isKnownAndTrue()) {
						var tb0 = controller.alleFelderAngegeben(idx, allRtIdCon1);
						var tb1 = controller.vergleicheSTRING(VglOp.NE, controller.feldWert(idx, rtIdCon1), "0000-0000-0000-0000");
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("ID", idx, "AddID", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("ID", iter.get(), "AddID", e);
			}
		}
	};


	R_coinsuredadditionalfields.vordruckF__CoInsuredRoot = function (controller) {
		/* Aufruf der Regeln fuer Vordruck CoInsuredRoot
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruckF__CoInsuredRoot_lfdNr(controller, indices);
				}
			}
		}
	};
	R_coinsuredadditionalfields.vordruckF__CoInsuredRoot_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck CoInsuredRoot für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AddID(controller, indices);
		controller.logMessung("mvk_AddID", indices[0], startMesspunkt);
	};
	R_coinsuredadditionalfields.vordruck__CoInsuredRoot = function (controller) {
		/* Aufruf der Regeln fuer Vordruck CoInsuredRoot
		H: nur Hinweise
		F: nur Fehler
		kein Zusatz: fuer alle
		*/
		if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", 0).isKnownAndTrue()) {
		    var maxLfdNummer = Math.min(controller.getMaxGesetzterKontext(0), 1);
			for (var l = 1; l <= maxLfdNummer; l++) {
				if (controller.mindestensEinVordruckAngegeben("CoInsuredRoot", l).isKnownAndTrue()) {
					var indices = [l];
					this.vordruck__CoInsuredRoot_lfdNr(controller, indices);
				}
			}
		}
	};
	R_coinsuredadditionalfields.vordruck__CoInsuredRoot_lfdNr = function (controller, indices) {
	/* Aufruf der Regeln fuer Vordruck CoInsuredRoot für eine spezifische lfdNr. */
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_AddID(controller, indices);
		controller.logMessung("mvk_AddID", indices[0], startMesspunkt);
	};



	R_coinsuredadditionalfields.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_coinsuredadditionalfields["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_coinsuredadditionalfields, controller);
		}
	};

	R_coinsuredadditionalfields.prototype.validatePreliminaryRules = function(controller) {
};

	R_coinsuredadditionalfields.prototype.validiereVoll = function (controller, modus) {
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
	R_coinsuredadditionalfields.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_coinsuredadditionalfields();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_coinsuredadditionalfields();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_coinsuredadditionalfields();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_coinsuredadditionalfields();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_coinsuredadditionalfields();
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
	R_coinsuredadditionalfields.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_coinsuredadditionalfields.vordruck__CoInsuredRoot(controller);
};
	R_coinsuredadditionalfields.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_coinsuredadditionalfields.vordruckF__CoInsuredRoot(controller);
};
	R_coinsuredadditionalfields.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_coinsuredadditionalfields.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_coinsuredadditionalfields._checkObject = new R_coinsuredadditionalfields();
	return R_coinsuredadditionalfields;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_coinsuredadditionalfields = (function () {
	function TVCheckFehler_coinsuredadditionalfields() {
		this._checkObject = R_coinsuredadditionalfields.getInstance();
	}
	TVCheckFehler_coinsuredadditionalfields.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 1) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [1,1]
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

	TVCheckFehler_coinsuredadditionalfields.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
		var startMesspunkt = -1;
		switch (interneFeldNr) {
			case 1:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_AddID(controller, indices);
				controller.logMessung("mvk_AddID", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 1) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [1,1].");
			}
			break;
		}
	};

	return TVCheckFehler_coinsuredadditionalfields;
}());
var TVCheckHinweis_coinsuredadditionalfields = (function () {
	function TVCheckHinweis_coinsuredadditionalfields() {
		this._checkObject = R_coinsuredadditionalfields.getInstance();
	}
	TVCheckHinweis_coinsuredadditionalfields.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_coinsuredadditionalfields;
}());
var TVCheckInfo_coinsuredadditionalfields = (function () {
	function TVCheckInfo_coinsuredadditionalfields() {
		this._checkObject = R_coinsuredadditionalfields.getInstance();
	}
	TVCheckInfo_coinsuredadditionalfields.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_coinsuredadditionalfields;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_coinsuredadditionalfields = (function () {
	function TVCheckAlle_coinsuredadditionalfields() {
		this._tvFehlerChecker = new TVCheckFehler_coinsuredadditionalfields();
		this._tvHinweisChecker = new TVCheckHinweis_coinsuredadditionalfields();
		this._tvInfoChecker = new TVCheckInfo_coinsuredadditionalfields();
	}
	TVCheckAlle_coinsuredadditionalfields.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_coinsuredadditionalfields;
}());
var TVCheckPreliminary_coinsuredadditionalfields = (function () {
	function TVCheckPreliminary_coinsuredadditionalfields() {
		this._checkObject = R_coinsuredadditionalfields.getInstance();
	}
	TVCheckPreliminary_coinsuredadditionalfields.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_coinsuredadditionalfields;
}());
var ERValidator_coinsuredadditionalfields = (function () {
	function ERValidator_coinsuredadditionalfields() {
		if (ERValidator_coinsuredadditionalfields._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_coinsuredadditionalfields.getInstance() instead of new.");
		}
		ERValidator_coinsuredadditionalfields._instance = this;
	}
	ERValidator_coinsuredadditionalfields.getInstance = function () {
		return ERValidator_coinsuredadditionalfields._instance;
	};
	ERValidator_coinsuredadditionalfields.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_coinsuredadditionalfields.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_coinsuredadditionalfields.getInstance(), modus, ERValidator_coinsuredadditionalfields._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_coinsuredadditionalfields.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_coinsuredadditionalfields.getInstance(), modus, ERValidator_coinsuredadditionalfields._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_coinsuredadditionalfields.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_coinsuredadditionalfields.getInstance(), modus, ERValidator_coinsuredadditionalfields._metaData, logger, C_coinsuredadditionalfields.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_coinsuredadditionalfields._instance = new ERValidator_coinsuredadditionalfields();
	ERValidator_coinsuredadditionalfields._metaData = Meta_coinsuredadditionalfields.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_coinsuredadditionalfields._constructor = (function () {
	})();
	return ERValidator_coinsuredadditionalfields;
}());

return {
	validator: ERValidator_coinsuredadditionalfields,
	meta: Meta_coinsuredadditionalfields
};
