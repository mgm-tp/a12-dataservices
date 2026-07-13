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
package com.mgmtp.a12.kernel.generated.recursivebusinesspartnercdm.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.IDynEnumsIntern;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.*;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VersionCompatibilityCheck;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.TrieNode;
public class Meta_recursivebusinesspartnercdm implements IMetaModel, IMetaEntityInternal {

	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	private static final String RUNTIME_VERSION = new String("31.1");

    private final MetaModelImpl meta;
    private Meta_Felder_recursivebusinesspartnercdm metaFelder = new Meta_Felder_recursivebusinesspartnercdm();
    private Meta_Regel_recursivebusinesspartnercdm metaRegeln = new Meta_Regel_recursivebusinesspartnercdm();
    private final List<Locale> unterstuetzteSprachen = Arrays.asList( Locale.of("en"), Locale.of("de") );

    private static final Map<Locale, PresentationInformationValues> DEFAULT_PRESENTATION_INFO = new MapBuilder<Locale, PresentationInformationValues>().put(Locale.of("de"), new PresentationInformationValues(".", ",", "-", "JMT", PresentationInformation.FALSE, "~", "dd.MM.yyyy HH:mm", "HH:mm")).put(Locale.of("en"), new PresentationInformationValues(".", ",", "-", "JMT", PresentationInformation.FALSE, "~", "MM/dd/yyyy HH:mm", "HH:mm")).build();

	public static final int[] LEGAL_CHARACTERS_START = null;
	public static final int[] LEGAL_CHARACTERS_END = null;
	public static final int[][] LEGAL_CHARACTERS = null;
	public static final TrieNode LEGAL_GRAPHEME_TRIE = new TrieNode(new HashMap<>(), false);

	// Der Konstruktor braucht den bereits sortierten LEGAL_CHARACTERS-Array. 
    private static final Meta_recursivebusinesspartnercdm instance = new Meta_recursivebusinesspartnercdm();

	static {
	}

    public static Meta_recursivebusinesspartnercdm getInstance() {
        return instance;
    }

    public Object getValue(String key, Object... params) {
		return meta.getValue(key, params);
    }

    public String[] getValidKeys() {
        return meta.getValidKeys();
    }

    public Set<String> getValidKeysInternal() {
        return meta.getValidKeysInternal();
    }

	private static void initContainer_0(Container_t[] result){
	     result[0] = new Container_t("/BusinessPartnerRoot", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[1] = new Container_t("/BusinessPartnerRoot/Attachment", Long.valueOf(99), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[2] = new Container_t("/BusinessPartnerRoot/Employment", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[3] = new Container_t("/ContractBusinessPartner", Long.valueOf(100), new String[] {}, new MapBuilder<String, Object>().put("Annotation", new MapBuilder<String, Object>().put("cdm.relationship", "ContractBusinessPartner").put("cdm.sourceRole", "Partner").put("cdm.targetRole", "Contract").put("cdm.targetDocumentModel", "Contract").build()).build(), Set.of());
	     result[4] = new Container_t("/ContractBusinessPartner/ContractRoot", Long.valueOf(100), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[5] = new Container_t("/ContractBusinessPartner/ContractRoot/ContractDates", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[6] = new Container_t("/ContractBusinessPartner/ContractRoot/ChangeLog", Long.valueOf(10000), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[7] = new Container_t("/ContractBusinessPartner/ContractRoot/ChangeLog/Changes", Long.valueOf(99), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[8] = new Container_t("/ContractBusinessPartner/ContractRoot/ChangeLog/Changes/SubChanges", Long.valueOf(99), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[9] = new Container_t("/ContractBusinessPartner/ContractAmendment", Long.valueOf(100), new String[] {}, new MapBuilder<String, Object>().put("Annotation", new MapBuilder<String, Object>().put("cdm.relationship", "ContractAmendment").put("cdm.sourceRole", "Contract").put("cdm.targetRole", "Amendment").put("cdm.targetDocumentModel", "Contract").build()).build(), Set.of());
	     result[10] = new Container_t("/ContractBusinessPartner/ContractAmendment/ContractDates", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[11] = new Container_t("/ContractBusinessPartner/ContractAmendment/ChangeLog", Long.valueOf(10000), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[12] = new Container_t("/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes", Long.valueOf(99), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[13] = new Container_t("/ContractBusinessPartner/ContractAmendment/ChangeLog/Changes/SubChanges", Long.valueOf(99), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[14] = new Container_t("/ContractBusinessPartner/ContractAmendment/__meta", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[15] = new Container_t("/ContractBusinessPartner/ContractAmendment/__meta/extensions", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[16] = new Container_t("/ContractBusinessPartner/__meta", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[17] = new Container_t("/ContractBusinessPartner/__meta/extensions", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[18] = new Container_t("/__meta", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	     result[19] = new Container_t("/__meta/extensions", Long.valueOf(1), new String[] {}, new MapBuilder<String, Object>().build(), Set.of());
	}


	static Container_t[] initContainer() {
		Container_t[] result = new Container_t[20];
		initContainer_0(result);

		return result;
	};

	private Model_t da = new Model_t (
		"29.4.0", //Produkt-Version
	    '.', //dezimaltrenner
		Arrays.asList( Locale.of("en"), Locale.of("de") ), // unterstuetzte Sprachen
		MetaDataHelper.createSetOfStrings(), // Namen der Applikationsbedingungen
		MetaDataHelper.createSetOfStrings(), // Names of custom field types
		null,
	    // additional Values:
	    new MapBuilder<String, Object>().put("BaseYear", null).put("TimeZone", "UTC").put("ModelId", "RecursiveBusinessPartnerCDM").put("Annotation", new MapBuilder<String, Object>().put("roles", "admin").put("cdm.queryRoot", "BusinessPartnerSuper").build()).build(),
	    Set.of("TimeZone", "BaseYear", "ModelId")
		);


	static Set<MetaIdentifier> getMetaFSMenge(String beschreibung) {
        Set<MetaIdentifier> result = new HashSet<MetaIdentifier>();
        String[] felder = beschreibung.split(";");
        for (int i=0; i < felder.length; i++) {
            if (felder[i].length() > 0) {
                result.add(MetaIdentifier.parse(felder[i]));
            }
        }
        return result;
    }

	@SuppressWarnings("unchecked")
	private Meta_recursivebusinesspartnercdm() {
		Container_t[] container = initContainer();

	    meta = new MetaModelImpl(
					da,
					metaFelder.getFelder().toArray(new Feld_t[metaFelder.getFelder().size()]), 
					metaRegeln.getRegeln().toArray(new Regel_t[metaRegeln.getRegeln().size()]),
					container,
					LEGAL_CHARACTERS,
					LEGAL_GRAPHEME_TRIE,
					DEFAULT_PRESENTATION_INFO,
					false,
					E_recursivebusinesspartnercdm::getInstance,
					ERValidator_recursivebusinesspartnercdm::getInstance);
    	meta.addWert(IExtmpMetaKeysBase.MODEL_XMLWRITER, null);
        meta.addWert(IExtmpMetaKeysBase.MODEL_UXMLWRITER, null);
        meta.addWert(IExtmpMetaKeysBase.MODEL_XMLREADER, null);
		Meta_PfFeld_recursivebusinesspartnercdm metaPlfFeldInfo = new Meta_PfFeld_recursivebusinesspartnercdm (getMetaDataValidierung());
		metaPlfFeldInfo.addLokalePflichtFeldInfos((Set<MetaLokalePflichtInfo>) meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
    }

	public IMetaDataValidierung getMetaDataValidierung() {
		return meta.getMetaDataValidierung();
	}

	public boolean isSpracheUnterstuetzt(Locale sprache) {
		return unterstuetzteSprachen.contains(sprache);
	}
}
