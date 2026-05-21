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
package com.mgmtp.a12.kernel.generated.businesspartnersuper.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Identifier_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Identifier_t.ReferenzTyp;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Regel_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;

/*
 * Enthält Metadaten über die Regeln.
 *
 */
public class  Meta_Regel_businesspartnersuper {
	private ArrayList<Regel_t> regeln = new ArrayList<Regel_t>();

	public ArrayList<Regel_t> getRegeln(){
		return regeln;
	}

	private static final int ANZAHL_REGELN = 5;

	//speichert pro Regel die referenzierten Identifier
	private Identifier_t refIdentifier[][] = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
	private Identifier_t refAuslassungsIdentifier[][] = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
	private String refAuslassungsVordrucke[][] = new String[ANZAHL_REGELN][];

	//stores the referenced entities per rule (can contain groups)
	private RtIdentifierTemplate[][] refEntity = new RtIdentifierTemplate[ANZAHL_REGELN][];

	//stores the referenced entities per rule, which could cause omission errors (may contain groups)
	private RtIdentifierTemplate[][] refAuslassungsEntity = new RtIdentifierTemplate[ANZAHL_REGELN][];

	private List<Map<Locale, String>> mehrsprachigeFehlertexte = new ArrayList<Map<Locale, String>>();
	{
		for (int i = 0; i < ANZAHL_REGELN; i++) {
			mehrsprachigeFehlertexte.add(new HashMap<Locale, String>());
		}
	}

	private void initRegelRefs_0(){
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	refIdentifier[0] = new Identifier_t[]{
			new Identifier_t( 0, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/original_filename"),
			new Identifier_t( 1, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/internal_filename"),
			new Identifier_t( 2, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/content"),
			new Identifier_t( 3, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/attachment_id"),
			new Identifier_t( 4, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/size"),
			new Identifier_t( 5, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/mime_type"),
			new Identifier_t( 6, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/category"),
			new Identifier_t( 7, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/description")
	};

	refAuslassungsIdentifier[0]= new Identifier_t[]{
			new Identifier_t( 1, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/internal_filename")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule0RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("internal_filename").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
	  	final RtIdentifierTemplate metaRule0RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).usb(-2).idx(1).idx(-1).build();

	    refEntity[0] = new RtIdentifierTemplate[]{
	    		metaRule0RefsrtIdCon1,
	    		metaRule0RefsrtIdCon2
	    };

	    refAuslassungsEntity[0]= new RtIdentifierTemplate[]{
	    		metaRule0RefsrtIdCon1
	    };
	}
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	refIdentifier[1] = new Identifier_t[]{
			new Identifier_t( 0, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/original_filename"),
			new Identifier_t( 1, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/internal_filename"),
			new Identifier_t( 2, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/content"),
			new Identifier_t( 3, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/attachment_id"),
			new Identifier_t( 4, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/size"),
			new Identifier_t( 5, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/mime_type"),
			new Identifier_t( 6, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/category"),
			new Identifier_t( 7, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/description")
	};

	refAuslassungsIdentifier[1]= new Identifier_t[]{
			new Identifier_t( 5, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/mime_type")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule1RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("mime_type").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
	  	final RtIdentifierTemplate metaRule1RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).usb(-2).idx(1).idx(-1).build();

	    refEntity[1] = new RtIdentifierTemplate[]{
	    		metaRule1RefsrtIdCon1,
	    		metaRule1RefsrtIdCon2
	    };

	    refAuslassungsEntity[1]= new RtIdentifierTemplate[]{
	    		metaRule1RefsrtIdCon1
	    };
	}
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	refIdentifier[2] = new Identifier_t[]{
			new Identifier_t( 0, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/original_filename"),
			new Identifier_t( 1, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/internal_filename"),
			new Identifier_t( 2, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/content"),
			new Identifier_t( 3, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/attachment_id"),
			new Identifier_t( 4, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/size"),
			new Identifier_t( 5, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/mime_type"),
			new Identifier_t( 6, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/category"),
			new Identifier_t( 7, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/description")
	};

	refAuslassungsIdentifier[2]= new Identifier_t[]{
			new Identifier_t( 2, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/content"),
			new Identifier_t( 3, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/attachment_id")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule2RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("attachment_id").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
	  	final RtIdentifierTemplate metaRule2RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("content").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
	  	final RtIdentifierTemplate metaRule2RefsrtIdCon3 = RtIdentifierTemplate.builder().unqNm("/BusinessPartnerRoot/Attachment").isField(false).usb(-2).idx(1).idx(-1).build();

	    refEntity[2] = new RtIdentifierTemplate[]{
	    		metaRule2RefsrtIdCon1,
	    		metaRule2RefsrtIdCon2,
	    		metaRule2RefsrtIdCon3
	    };

	    refAuslassungsEntity[2]= new RtIdentifierTemplate[]{
	    		metaRule2RefsrtIdCon1,
	    		metaRule2RefsrtIdCon2
	    };
	}
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	refIdentifier[3] = new Identifier_t[]{
			new Identifier_t( 2, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/content"),
			new Identifier_t( 4, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/size")
	};

	refAuslassungsIdentifier[3]= new Identifier_t[]{
			new Identifier_t( 4, new long[]{1, IIdentifier.ITERATION, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Attachment/size")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule3RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("size").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();
	  	final RtIdentifierTemplate metaRule3RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("content").isField(true).usb(-2).idx(1).idx(-1).idx(1).build();

	    refEntity[3] = new RtIdentifierTemplate[]{
	    		metaRule3RefsrtIdCon1,
	    		metaRule3RefsrtIdCon2
	    };

	    refAuslassungsEntity[3]= new RtIdentifierTemplate[]{
	    		metaRule3RefsrtIdCon1
	    };
	}
	// Init Daten zur Regel '/BusinessPartnerRoot/Employment/TaxComputation'
	refIdentifier[4] = new Identifier_t[]{
			new Identifier_t( 12, new long[]{1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Employment/income"),
			new Identifier_t( 13, new long[]{1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Employment/tax")
	};

	refAuslassungsIdentifier[4]= new Identifier_t[]{
			new Identifier_t( 12, new long[]{1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Employment/income"),
			new Identifier_t( 13, new long[]{1, 1, 1}, IIdentifier.USB_NOT_SPECIFIED, ReferenzTyp.KEINE_BERECHNUNG, null, null, "/BusinessPartnerRoot/Employment/tax")
	};

	{
	    // filter (decl & refs) omitted for referenced entities
	  	final RtIdentifierTemplate metaRule4RefsrtIdCon1 = RtIdentifierTemplate.builder().unqNm("income").isField(true).usb(-2).idx(1).idx(1).idx(1).build();
	  	final RtIdentifierTemplate metaRule4RefsrtIdCon2 = RtIdentifierTemplate.builder().unqNm("tax").isField(true).usb(-2).idx(1).idx(1).idx(1).build();

	    refEntity[4] = new RtIdentifierTemplate[]{
	    		metaRule4RefsrtIdCon1,
	    		metaRule4RefsrtIdCon2
	    };

	    refAuslassungsEntity[4]= new RtIdentifierTemplate[]{
	    		metaRule4RefsrtIdCon1,
	    		metaRule4RefsrtIdCon2
	    };
	}
	}


	private void initRegelFehlertexte_0(){
	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired'
	mehrsprachigeFehlertexte.get(0).put(new Locale("de"), "Internal Error: Field $internal_filename@-2@1@e@1$ of customType attachment is not filled.");
	mehrsprachigeFehlertexte.get(0).put(new Locale("en"), "Internal Error: Field $internal_filename@-2@1@e@1$ of customType attachment is not filled.");

	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired'
	mehrsprachigeFehlertexte.get(1).put(new Locale("de"), "Internal Error: Field $mime_type@-2@1@e@1$ of customType attachment is not filled.");
	mehrsprachigeFehlertexte.get(1).put(new Locale("en"), "Internal Error: Field $mime_type@-2@1@e@1$ of customType attachment is not filled.");

	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled'
	mehrsprachigeFehlertexte.get(2).put(new Locale("de"), "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");
	mehrsprachigeFehlertexte.get(2).put(new Locale("en"), "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.");

	// Init Daten zur Regel '/BusinessPartnerRoot/Attachment/SizeOfContentFilled'
	mehrsprachigeFehlertexte.get(3).put(new Locale("de"), "Internal Error: If the content is filled, the size must be also filled.");
	mehrsprachigeFehlertexte.get(3).put(new Locale("en"), "Internal Error: If the content is filled, the size must be also filled.");

	// Init Daten zur Regel '/BusinessPartnerRoot/Employment/TaxComputation'
	mehrsprachigeFehlertexte.get(4).put(new Locale("de"), "Berechnungsfehler für TaxComputation");
	mehrsprachigeFehlertexte.get(4).put(new Locale("en"), "Computation error for TaxComputation");

	}


	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	private void initRegeln_0(){
	regeln.add(new Regel_t("AttachmentInternalFilenameRequired", "/BusinessPartnerRoot/Attachment/AttachmentInternalFilenameRequired", "ErrorR29", mehrsprachigeFehlertexte.get(0), "Fehler", refIdentifier[0], refAuslassungsIdentifier[0], refEntity[0], refAuslassungsEntity[0], null,"/BusinessPartnerRoot/Attachment/internal_filename", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	regeln.add(new Regel_t("AttachmentMimeTypeRequired", "/BusinessPartnerRoot/Attachment/AttachmentMimeTypeRequired", "ErrorR30", mehrsprachigeFehlertexte.get(1), "Fehler", refIdentifier[1], refAuslassungsIdentifier[1], refEntity[1], refAuslassungsEntity[1], null,"/BusinessPartnerRoot/Attachment/mime_type", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	regeln.add(new Regel_t("AttachmentIdOrContentFilled", "/BusinessPartnerRoot/Attachment/AttachmentIdOrContentFilled", "ErrorR31", mehrsprachigeFehlertexte.get(2), "Fehler", refIdentifier[2], refAuslassungsIdentifier[2], refEntity[2], refAuslassungsEntity[2], null,"/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	regeln.add(new Regel_t("SizeOfContentFilled", "/BusinessPartnerRoot/Attachment/SizeOfContentFilled", "ErrorR32", mehrsprachigeFehlertexte.get(3), "Fehler", refIdentifier[3], refAuslassungsIdentifier[3], refEntity[3], refAuslassungsEntity[3], null,"/BusinessPartnerRoot/Attachment/content", false, false, new MapBuilder<String, Object>().build(), Set.of()));
	regeln.add(new Regel_t("TaxComputation", "/BusinessPartnerRoot/Employment/TaxComputation", "TaxComputation", mehrsprachigeFehlertexte.get(4), "Fehler", refIdentifier[4], refAuslassungsIdentifier[4], refEntity[4], refAuslassungsEntity[4], null,"/BusinessPartnerRoot/Employment/tax", true, false, new MapBuilder<String, Object>().build(), Set.of()));
	}


  	public Meta_Regel_businesspartnersuper() {
        initRegelRefs_0();

        initRegelFehlertexte_0();

        initRegeln_0();

	}
}