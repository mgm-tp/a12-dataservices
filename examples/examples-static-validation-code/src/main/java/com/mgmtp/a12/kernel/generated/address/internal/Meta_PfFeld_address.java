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
package com.mgmtp.a12.kernel.generated.address.internal;

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.MetaIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.MetaLokalePflichtInfo;

/*
 * Enthält Metadaten über die Pflichtfeldinformationen.
 *
 */
public class  Meta_PfFeld_address {

    private final IMetaDataValidierung metaDataValidierung;

    Meta_PfFeld_address(IMetaDataValidierung metaDataValidierung) {
		this.metaDataValidierung = metaDataValidierung;
	}

    void addLokalePflichtFeldInfos(Set<MetaLokalePflichtInfo> additivUndPflichtfelder) {

    	String[][] infos = new String[0][];

        for (String [] info : infos) {
            addLokalePflichtFeldInfo(
            		additivUndPflichtfelder,
                    info[0],
                    info[1],
                    Meta_address.getMetaFSMenge(info[2]),
                    Meta_address.getMetaFSMenge(info[3]));
        }

    }


	private void addLokalePflichtFeldInfo(Set<MetaLokalePflichtInfo> additivUndPflichtfelder, String lfdNrMuster, String zeileMuster,
			Set<MetaIdentifier> additiv,
			Set<MetaIdentifier> pflichtfelder) {
		MetaLokalePflichtInfo info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this.metaDataValidierung, additiv, pflichtfelder, lfdNrMuster, zeileMuster);
		additivUndPflichtfelder.add(info);
	}

}