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
package com.mgmtp.a12.kernel.generated.relationshipmetamodel.internal;

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.MetaIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.MetaLokalePflichtInfo;

/*
 * Enthält Metadaten über die Pflichtfeldinformationen.
 *
 */
public class  Meta_PfFeld_relationshipmetamodel {

    private final IMetaDataValidierung metaDataValidierung;

    Meta_PfFeld_relationshipmetamodel(IMetaDataValidierung metaDataValidierung) {
		this.metaDataValidierung = metaDataValidierung;
	}

    void addLokalePflichtFeldInfos(Set<MetaLokalePflichtInfo> additivUndPflichtfelder) {

    	String[][] infos = new String[6][];
    	addLokalePflichtFeldInfos_0(infos);


        for (String [] info : infos) {
            addLokalePflichtFeldInfo(
            		additivUndPflichtfelder,
                    info[0],
                    info[1],
                    Meta_relationshipmetamodel.getMetaFSMenge(info[2]),
                    Meta_relationshipmetamodel.getMetaFSMenge(info[3]));
        }

    }

    private void addLokalePflichtFeldInfos_0(String[][] infos) {
    	// /header/locales/vk_vkcode_req
    	infos[0] = new String[] {
    			"0-0",  // Iterations-Intervall für LfdNr
    			"0-0", // Iterations-Intervall für Zeile
    			"code[1,-1,1]",
    			"code[1,-1,1]"
    		};

    	   // /content/labels/vk_vklocale_req
    	infos[1] = new String[] {
    			"0-0",  // Iterations-Intervall für LfdNr
    			"0-0", // Iterations-Intervall für Zeile
    			"locale_1[1,-1,1];text_1[1,-1,1]",
    			"locale_1[1,-1,1]"
    		};

    	   // /header/annotations/vk_vkname_req
    	infos[2] = new String[] {
    			"0-0",  // Iterations-Intervall für LfdNr
    			"0-0", // Iterations-Intervall für Zeile
    			"name[1,-1,1];value[1,-1,1]",
    			"name[1,-1,1]"
    		};

    	   // /header/labels/vk_vklocale_req
    	infos[3] = new String[] {
    			"0-0",  // Iterations-Intervall für LfdNr
    			"0-0", // Iterations-Intervall für Zeile
    			"locale_2[1,-1,1];text_2[1,-1,1]",
    			"locale_2[1,-1,1]"
    		};

    	   // /header/modelReferences/vk_vkmodelType_req&/header/modelReferences/vk_vkreference_req
    	infos[4] = new String[] {
    			"0-0",  // Iterations-Intervall für LfdNr
    			"0-0", // Iterations-Intervall für Zeile
    			"alias[1,-1,1];modelType_0[1,-1,1];purpose[1,-1,1];reference[1,-1,1]",
    			"modelType_0[1,-1,1];reference[1,-1,1]"
    		};

    	   // /content/entityCharacteristics/linkConstraints/multiplicity/vk_vkunbounded_req&/content/entityCharacteristics/vk_vkdocumentModel_req&/content/entityCharacteristics/vk_vkordered_req&/content/entityCharacteristics/vk_vkrole_req
    	infos[5] = new String[] {
    			"0-0",  // Iterations-Intervall für LfdNr
    			"0-0", // Iterations-Intervall für Zeile
    			"documentModel[1,-1,1];locale_0[1,-1,0,1];ordered[1,-1,1];role[1,-1,1];text_0[1,-1,0,1];unbounded[1,-1,1,1,1];upperLimit[1,-1,1,1,1]",
    			"documentModel[1,-1,1];ordered[1,-1,1];role[1,-1,1];unbounded[1,-1,1,1,1]"
    		};

    	   
    }



	private void addLokalePflichtFeldInfo(Set<MetaLokalePflichtInfo> additivUndPflichtfelder, String lfdNrMuster, String zeileMuster,
			Set<MetaIdentifier> additiv,
			Set<MetaIdentifier> pflichtfelder) {
		MetaLokalePflichtInfo info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this.metaDataValidierung, additiv, pflichtfelder, lfdNrMuster, zeileMuster);
		additivUndPflichtfelder.add(info);
	}

}