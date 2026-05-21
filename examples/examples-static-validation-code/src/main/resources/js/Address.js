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
var Meta_Felder_address = (function () {
	function Meta_Felder_address() {
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
	Meta_Felder_address.prototype.getFelder = function () {
		return this._felder;
	};

	Meta_Felder_address.prototype._initFeldtypen = function () {
	this._metaFormate.set(
		0,
		new FormatDefinitionString(
			this._mehrsprachigeFehlertexte.get(0),
			["Residential", "Commercial", "Other"],
			this._enumerationOberflaechenWerte.get(0),
			this._enumerationCategoryValues.get(0),
			""
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
			false, // zeilenUmbruch
			false, // noValueValidation
			this._stringHintLists.get(1)
		)
	);
	this._metaFormate.set(
		2,
		new FormatDefinitionJa(
			"true"

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
	Meta_Felder_address.prototype._initFelder = function () {
	this._felder.push(
		new Feld_t(
			0,
			"AddressType",
			"/AddressRoot/AddressType",
			false,
			false,
			this._metaFormate.get(0),
			"addressAddressTypeValidate",
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
			this._errorTexts4Req.get(0)
		)
	);
	this._felder.push(
		new Feld_t(
			1,
			"Location",
			"/AddressRoot/Location",
			false,
			false,
			this._metaFormate.get(1),
			"addressLocationValidate",
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
			this._errorTexts4Req.get(1)
		)
	);
	this._felder.push(
		new Feld_t(
			2,
			"Street",
			"/AddressRoot/Street",
			false,
			false,
			this._metaFormate.get(1),
			"addressStreetValidate",
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
			this._errorTexts4Req.get(2)
		)
	);
	this._felder.push(
		new Feld_t(
			3,
			"HouseNumber",
			"/AddressRoot/HouseNumber",
			false,
			false,
			this._metaFormate.get(1),
			"addressHouseNumberValidate",
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
			this._errorTexts4Req.get(3)
		)
	);
	this._felder.push(
		new Feld_t(
			4,
			"City",
			"/AddressRoot/City",
			false,
			false,
			this._metaFormate.get(1),
			"addressCityValidate",
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
			this._errorTexts4Req.get(4)
		)
	);
	this._felder.push(
		new Feld_t(
			5,
			"PostCode",
			"/AddressRoot/PostCode",
			false,
			false,
			this._metaFormate.get(1),
			"addressPostCodeValidate",
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
			this._errorTexts4Req.get(5)
		)
	);
	this._felder.push(
		new Feld_t(
			6,
			"Country",
			"/AddressRoot/Country",
			false,
			false,
			this._metaFormate.get(1),
			"addressCountryValidate",
			[0],
			[],
			new MapBuilder()
				.put("DruckText", new MapBuilder().put("en", "Country").put("de", "Land").build())
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
			"PostalAddress",
			"/AddressRoot/PostalAddress",
			false,
			false,
			this._metaFormate.get(2),
			"addressPostalAddressValidate",
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
			this._errorTexts4Req.get(7)
		)
	);
	this._felder.push(
		new Feld_t(
			8,
			"docRef",
			"/__meta/docRef",
			false,
			false,
			this._metaFormate.get(1),
			"addressdocRefValidate",
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
			this._errorTexts4Req.get(8)
		)
	);
	this._felder.push(
		new Feld_t(
			9,
			"modelReference",
			"/__meta/modelReference",
			false,
			false,
			this._metaFormate.get(1),
			"addressmodelReferenceValidate",
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
			this._errorTexts4Req.get(9)
		)
	);
	this._felder.push(
		new Feld_t(
			10,
			"modelVersion",
			"/__meta/modelVersion",
			false,
			false,
			this._metaFormate.get(1),
			"addressmodelVersionValidate",
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
			this._errorTexts4Req.get(10)
		)
	);
	this._felder.push(
		new Feld_t(
			11,
			"creator",
			"/__meta/creator",
			false,
			false,
			this._metaFormate.get(1),
			"addresscreatorValidate",
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
			this._errorTexts4Req.get(11)
		)
	);
	this._felder.push(
		new Feld_t(
			12,
			"createdAt",
			"/__meta/createdAt",
			false,
			false,
			this._metaFormate.get(3),
			"addresscreatedAtValidate",
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
			this._errorTexts4Req.get(12)
		)
	);
	this._felder.push(
		new Feld_t(
			13,
			"modifier",
			"/__meta/modifier",
			false,
			false,
			this._metaFormate.get(1),
			"addressmodifierValidate",
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
			this._errorTexts4Req.get(13)
		)
	);
	this._felder.push(
		new Feld_t(
			14,
			"modifiedAt",
			"/__meta/modifiedAt",
			false,
			false,
			this._metaFormate.get(3),
			"addressmodifiedAtValidate",
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
			this._errorTexts4Req.get(14)
		)
	);
	};
	Meta_Felder_address.prototype._initFeldFehlertexte = function () {
	this._mehrsprachigeFehlertexte.set(0, new Map());

	};
	Meta_Felder_address.prototype._initEnumerationOberflaechenWerte = function () {
	this._enumerationOberflaechenWerte.set(0, new Map());
	this._enumerationOberflaechenWerte.get(0).set("de", MetaFeldUtil.createPairList(["Wohnen", "Kommerziell", "Andere"], ["Residential", "Commercial", "Other"]));
	this._enumerationOberflaechenWerte.get(0).set("en", MetaFeldUtil.createPairList(["Residential", "Commercial", "Other"], ["Residential", "Commercial", "Other"]));
	this._enumerationOberflaechenWerte.get(0).set("en_US", MetaFeldUtil.createPairList(["Urban", "Retail", "Another"], ["Residential", "Commercial", "Other"]));

	};


	return Meta_Felder_address;
}());
var Meta_Regel_address = (function () {
	function Meta_Regel_address() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_address.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_address.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_address.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i < Meta_Regel_address.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		this._initRegelRefs();
		this._initRegelFehlertexte();
		this._initRegeln();
	}
	Meta_Regel_address.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_address.ANZAHL_REGELN = 1;

	Meta_Regel_address.prototype._initRegelRefs = function () {
	// Init Daten zur Regel '/AddressRoot/MustContainCountry'
	this._refIdentifier[0] = [
			new Identifier_t(6, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];

	this._refAuslassungsIdentifier[0]= [
			new Identifier_t(6, [1, 1], ReferenzTyp.KEINE_BERECHNUNG, null, null)];


	};

	Meta_Regel_address.prototype._initRegelFehlertexte = function () {
	// Init Daten zur Regel '/AddressRoot/MustContainCountry'
	this._mehrsprachigeFehlertexte.get(0).set("de", "Kein Land angegeben");this._mehrsprachigeFehlertexte.get(0).set("en", "No country provided");
	this._fehlertexteParameterMapping.set(0, new MapBuilder().build());
	};

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	Meta_Regel_address.prototype._initRegeln = function () {
	this._regeln.push(new Regel_t("MustContainCountry", "/AddressRoot/MustContainCountry", "Error rule_7c66e", this._mehrsprachigeFehlertexte.get(0), this._fehlertexteParameterMapping.get(0), "Fehler", this._refIdentifier[0], this._refAuslassungsIdentifier[0], null, "/AddressRoot/Country", false, false, new MapBuilder().build(), new Set()));

	};

	return Meta_Regel_address;
}());
var Meta_PfFeld_address = (function () {
	function Meta_PfFeld_address(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_address.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};


	Meta_PfFeld_address.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_address;
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

var Meta_address = (function () {
	function Meta_address() {
		this._metaFelder = new Meta_Felder_address();
		this._metaRegeln = new Meta_Regel_address();
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
					.put("ModelId", "Address")
					.put("Annotation", new MapBuilder()
					.put("roles", "admin,guest,ModelRead").build()).build(),
				new Set(["TimeZone", "BaseYear", "ModelId"])
			);
		if (Meta_address._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_address.getInstance() instead of new.");
		}
		Meta_address._instance = this;
		var container = Meta_address._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_address.LEGAL_CHARACTERS,
			Meta_address.LEGAL_GRAPHEME_TRIE,
			() => E_address.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_address(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_address, "RUNTIME_VERSION", { get: function () { return "30.8"; },
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_address, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_address, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_address, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_address, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});

	Meta_address.getInstance = function () {
		return Meta_address._instance;
	};
	Meta_address.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i < arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_address.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_address._initContainer = function () {
		var result = new Array();
		result.push(new Container_t("/AddressRoot", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta", 1, [], new MapBuilder().build(), new Set()));

		result.push(new Container_t("/__meta/extensions", 1, [], new MapBuilder().build(), new Set()));


		return result;
	};
	Meta_address.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_address.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_address._constructor = (function () {
		if (Meta_address.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_address.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_address._instance = new Meta_address();
	})();
	return Meta_address;
}());var E_address = (function () {
	function E_address() {
		if (E_address._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_address.getInstance() instead of new.");
		}
		E_address._valueCalcObject = this;
		this._feldData = Meta_address.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_address.getInstance = function () {
		return this._valueCalcObject;
	};


	E_address.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
		}
		return result;
	};

	E_address._valueCalcObject = new E_address();
	return E_address;
}());
var C_address = (function () {
	function C_address() {
		if (C_address._checkObject) {
			throw new Error("Instantiation failed: Use C_address.getInstance() instead of new.");
		}
		C_address._checkObject = this;
		this._feldData = Meta_address.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_address.getInstance = function () {
		return this._checkObject;
	};


	C_address.CALCULATED_FIELD_DEPENDENCIES = new Map();

	C_address.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = new Set();

	C_address.CALC_FUNC_NAME_MAP = new Map();

	C_address.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = [];

	C_address.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_address.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_address.prototype.hasCalculation = function(fullFieldName) {
        return C_address.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_address.prototype.getDependencyGraph = function() {
        return C_address.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_address.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_address.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_address.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_address.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_address._checkObject = new C_address();
	return C_address;
}());
var R_address = (function () {
	function R_address() {
		this._feldData = Meta_address.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_address.getInstance = function () {
		return R_address._checkObject;
	};
	R_address.prototype.mvk_MustContainCountry = function (controller, indices) {
		controller.initFuerRegelpruefung("/AddressRoot/MustContainCountry", PruefErgebnisTyp.Fehler);
		var start = [1, 1];
		var ende = [1, 1];

		var iter = new EbenenIterator(start, ende);
		var rtIdCon1 = RtIdentifierTemplate.builder().unqNm("/AddressRoot").isField(false).idx(1).build();
		var rtIdCon2 = RtIdentifierTemplate.builder().unqNm("Country").isField(true).idx(1).idx(1).build();
		var allRtIdCon1 = new ListBuilder().add(rtIdCon2).build();

		try {
			var innerIter = iter.iterator();
			while (innerIter.hasNext()) {
				var idx = innerIter.next();
				if (controller.mindestensEinVordruckAngegeben("AddressRoot", 1, idx, rtIdCon1).isKnownAndTrue()&&controller.keinFeldAngegeben(idx, allRtIdCon1).isKnownAndTrue()) {
						var tb0 = controller.mindestensEinVordruckAngegeben("AddressRoot", 1, idx, rtIdCon1);
						var tb1 = controller.keinFeldAngegeben(idx, allRtIdCon1);
						var valErg = tb0.combineUND(tb1);
						controller.addValidatorMessage("Country", idx, "Error rule_7c66e", valErg.fehlerTyp);

				}
			}
		}
		catch (e) {
		    if (e instanceof CustomConditionException) {
		        throw e;
		    } else {
			    controller.addValidatorMessage("Country", iter.get(), "Error rule_7c66e", e);
			}
		}
	};


	R_address.vordruckF__global = function (controller) {
	/* Aufruf der Regeln fuer Vordruck global für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_MustContainCountry(controller, indices);
		controller.logMessung("mvk_MustContainCountry", indices[0], startMesspunkt);
	};
	R_address.vordruck__global = function (controller) {
	/* Aufruf der Regeln fuer Vordruck global für eine spezifische lfdNr. */
		var indices = [0];
		var startMesspunkt = -1;
		startMesspunkt = controller.startMesspunkt();
		this._checkObject.mvk_MustContainCountry(controller, indices);
		controller.logMessung("mvk_MustContainCountry", indices[0], startMesspunkt);
	};



	R_address.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_address["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_address, controller);
		}
	};

	R_address.prototype.validatePreliminaryRules = function(controller) {
};

	R_address.prototype.validiereVoll = function (controller, modus) {
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
	R_address.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_address();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_address();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_address();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_address();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_address();
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
	R_address.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		R_address.vordruck__global(controller);
};
	R_address.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		R_address.vordruckF__global(controller);
};
	R_address.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
};
	R_address.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
};
	R_address._checkObject = new R_address();
	return R_address;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_address = (function () {
	function TVCheckFehler_address() {
		this._checkObject = R_address.getInstance();
	}
	TVCheckFehler_address.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		if (interneFeldNr <= 6) {
			this._fuehreAus_0(controller, interneFeldNr, indices);
		}

	};

	/**
	* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
	* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [6,6]
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

	TVCheckFehler_address.prototype._fuehreAus_0 = function(controller, interneFeldNr, indices) {
		var startMesspunkt = -1;
		switch (interneFeldNr) {
			case 6:
				startMesspunkt = controller.startMesspunkt();
				this._checkObject.mvk_MustContainCountry(controller, indices);
				controller.logMessung("mvk_MustContainCountry", indices[0], startMesspunkt);
				break;
			default:
			if (interneFeldNr <= 6) {
				/* In der Methode werden nur interne Nummern von Feldern
				 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
				 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
				 */
			} else {
				throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [6,6].");
			}
			break;
		}
	};

	return TVCheckFehler_address;
}());
var TVCheckHinweis_address = (function () {
	function TVCheckHinweis_address() {
		this._checkObject = R_address.getInstance();
	}
	TVCheckHinweis_address.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'WARNING'.
	};
	return TVCheckHinweis_address;
}());
var TVCheckInfo_address = (function () {
	function TVCheckInfo_address() {
		this._checkObject = R_address.getInstance();
	}
	TVCheckInfo_address.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any rules of severity 'INFO'.
	};
	return TVCheckInfo_address;
}());
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_address = (function () {
	function TVCheckAlle_address() {
		this._tvFehlerChecker = new TVCheckFehler_address();
		this._tvHinweisChecker = new TVCheckHinweis_address();
		this._tvInfoChecker = new TVCheckInfo_address();
	}
	TVCheckAlle_address.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_address;
}());
var TVCheckPreliminary_address = (function () {
	function TVCheckPreliminary_address() {
		this._checkObject = R_address.getInstance();
	}
	TVCheckPreliminary_address.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
			// The model does not contain any preliminary rules.
	};
	return TVCheckPreliminary_address;
}());
var ERValidator_address = (function () {
	function ERValidator_address() {
		if (ERValidator_address._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_address.getInstance() instead of new.");
		}
		ERValidator_address._instance = this;
	}
	ERValidator_address.getInstance = function () {
		return ERValidator_address._instance;
	};
	ERValidator_address.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
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
	ERValidator_address.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_address.getInstance(), modus, ERValidator_address._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_address.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_address.getInstance(), modus, ERValidator_address._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_address.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_address.getInstance(), modus, ERValidator_address._metaData, logger, C_address.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_address._instance = new ERValidator_address();
	ERValidator_address._metaData = Meta_address.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_address._constructor = (function () {
	})();
	return ERValidator_address;
}());

return {
	validator: ERValidator_address,
	meta: Meta_address
};
