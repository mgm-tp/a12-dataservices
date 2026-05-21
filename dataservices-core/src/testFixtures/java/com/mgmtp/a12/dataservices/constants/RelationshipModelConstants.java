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

public interface RelationshipModelConstants {

	String CONTRACT_BUSINESS_PARTNER_MODEL = "ContractBusinessPartner";
	String CONTRACT_COINSURED_BUSINESS_PARTNER_MODEL = "ContractCoInsuredPartner";
	String CONTRACT_COINSURED_BUSINESS_PARTNER_INVALID_MODEL = "ContractCoInsuredPartnerWrongRoleName";
	String PARTNER_ADDRESSES_MODEL = "PartnerAddresses";
	String PARTNER_POSTAL_ADDRESS_MODEL = "PartnerPostalAddress";
	String PRODUCT_BRAND_RM = "ProductBrand";
	String PRODUCT_BUNDLE_RM = "ProductBundle";
	String CO_INSURED_ADDITIONAL_FIELDS_INVALID_RM = "+CoInsuredAdditionalFields";
	String LEGACY_RM_INVALID = "LegacyRelationshipMetaModel";
	// Relationship Model - Names
	String PRODUCT_CAMPAIGN_RM = "ProductCampaign";

	interface RoleConstants {

		// Relationships - Roles
		String BUNDLE_ROLE = "BundleRole";
		String PRODUCT_ROLE = "ProductRole";
		String BRAND_ROLE = "BrandRole";
		String CAMPAIGN_ROLE = "CampaignRole";
		String PARTNER_ROLE = "Partner";
		String CONTRACT_ROLE = "Contract";
		String ADDRESS_ROLE = "Address";
	}
}
