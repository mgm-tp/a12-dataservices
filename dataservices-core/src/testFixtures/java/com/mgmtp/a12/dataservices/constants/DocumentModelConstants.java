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
package com.mgmtp.a12.dataservices.constants;

public interface DocumentModelConstants {
	// Document Models Names
	String CONTRACT_CDM_MODEL = "ContractCDM";
	String ANONYMIZED_BUSINESS_PARTNER_CDM_MODEL = "AnonymizedBusinessPartnerCDM";
	String BUSINESS_PARTNER_LTD_MODEL = "BusinessPartnerLTD";
	String BUSINESS_PARTNER_SUPER_MODEL = "BusinessPartnerSuper";
	String CAMPAIGN_MODEL_NAME = "Campaign";
	String PRODUCT_MODEL_NAME = "Product";
	String DOMAIN_BUNDLE_MODEL_NAME = "DomainBundle";
	String BRAND_MODEL_NAME = "Brand";
	String SUPER_TYPE_MODEL = "DateTestModel";
	String SUBTYPE_MODEL2 = "DateTestSubType2";
	String SUBTYPE_MODEL3 = "DateTestSubType3";
	String CONTRACT_DOCUMENT_MODEL = "Contract";
	String BUSINESS_PARTNER_INVALID_MODEL = "1BusinessPartner";
	String BUSINESS_PARTNER_SUPER_INVALID_MODEL = "0BusinessPartnerSuper";
	String SUBTYPE_MODEL1 = "DateTestSubType1";
	String BUSINESS_PARTNER_CDM = "AnonymizedBusinessPartnerCDM";
	String BUSINESS_PARTNER_DOCUMENT_MODEL = "BusinessPartner";
	String COINSURED_ADDITIONAL_FIELDS_MODEL = "CoInsuredAdditionalFields";
	String CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL = "ContractAutomotive";
	String ADDRESS_DOCUMENT_MODEL = "Address";
	String BUSINESS_PARTNER_MULTIPLE_ATTACHMENT_IDS = "BusinessPartnerWithMultipleAttachmentIds";
	String NATURAL_PERSON_CDM = "NaturalPersonCDM";
	String NATURAL_PERSON_CDM_WRONG_NESTED_MODEL = NATURAL_PERSON_CDM + "WrongNestedModel";
	String NATURAL_PERSON_CDM_WRONG_RM = NATURAL_PERSON_CDM + "WrongRm";
	String NATURAL_PERSON_CDM_WRONG_CRD = NATURAL_PERSON_CDM + "WrongCrd";

	interface FieldConstants {

		// Document field paths
		String CO_INSURED_ADDITIONAL_FIELDS_ROOT = "CoInsuredRoot";
	}

	interface SearchConstants {
		 String EN_LOCALE = "en";
	}

}
