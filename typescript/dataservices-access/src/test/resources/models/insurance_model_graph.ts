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
export default {
	genericModels: [
		{
			modelId: "BusinessPartnerForm",
			displayLabels: [],
			modelReferences: [
				{
					modelType: "document",
					reference: "BusinessPartner",
					purpose: "data binding",
					alias: "BusinessPartner"
				}
			],
			type: "form"
		}
	],
	documentModels: [
		{
			modelId: "BusinessPartnerSuper",
			displayLabels: [
				{
					locale: "de",
					text: "Geschäftspartner Super"
				},
				{
					locale: "en",
					text: "Business Partner Super"
				}
			],
			relations: [
				"PartnerPostalAddress",
				"PartnerAddresses",
				"ContractBusinessPartner",
				"ContractCoInsuredPartner"
			],
			subTypes: ["BusinessPartner", "BusinessPartnerLTD"],
			abstractModel: true
		},
		{
			modelId: "BusinessPartner",
			displayLabels: [
				{
					locale: "de",
					text: "Geschäftspartner"
				},
				{
					locale: "en",
					text: "Business Partner"
				}
			],
			relations: [],
			subTypes: [],
			abstractModel: false
		},
		{
			modelId: "BusinessPartnerLTD",
			displayLabels: [
				{
					locale: "de",
					text: "Geschäftspartner Limited"
				},
				{
					locale: "en",
					text: "Business Partner Limited"
				}
			],
			relations: [],
			subTypes: []
		}
	],
	composeDocumentModels: [
		{
			modelId: "ContractCDM",
			displayLabels: [
				{
					locale: "de",
					text: "Vertrag CDM"
				},
				{
					locale: "en",
					text: "Contract CDM"
				}
			],
			modelReferences: [
				{
					modelType: "document",
					reference: "BusinessPartnerSuper",
					purpose: "include",
					alias: "BusinessPartnerSuper"
				},
				{
					modelType: "document",
					reference: "Address",
					purpose: "include",
					alias: "Address"
				},
				{
					modelType: "document",
					reference: "CoInsuredAdditionalFields",
					purpose: "include",
					alias: "CoInsuredAdditionalFields"
				},
				{
					modelType: "document",
					reference: "Contract",
					purpose: "include",
					alias: "Contract"
				}
			],
			rootDocumentModelId: "Contract"
		}
	],
	relationshipModels: [
		{
			header: {
				id: "ContractCoInsuredPartner",
				modelType: "relationship",
				modelVersion: "3.0.0",
				locales: [
					{
						code: "de"
					},
					{
						code: "en"
					},
					{
						code: "en_US"
					}
				],
				modelReferences: [
					{
						modelType: "document",
						reference: "Contract",
						purpose: "Document model",
						alias: "Contract"
					},
					{
						modelType: "document",
						reference: "CoInsuredAdditionalFields",
						purpose: "Link Document model",
						alias: "Link Model"
					},
					{
						modelType: "document",
						reference: "BusinessPartnerSuper",
						purpose: "Document model",
						alias: "BusinessPartnerSuper"
					}
				],
				annotations: [
					{
						name: "roles",
						value: "admin,guest,ModelRead"
					}
				],
				labels: [
					{
						locale: "de",
						text: "Vertrag und Mitversicherter"
					},
					{
						locale: "en",
						text: "Contract and CoInsured Partner"
					}
				]
			},
			content: {
				labels: [
					{
						locale: "en",
						text: "Contract and CoInsured Partner"
					},
					{
						locale: "de",
						text: "Vertrag und Mitversicherter"
					}
				],
				associationType: "SHARED",
				linkDocumentModel: "CoInsuredAdditionalFields",
				duplicatesAllowed: false,
				entityCharacteristics: [
					{
						role: "Contract",
						labels: [
							{
								locale: "en",
								text: "Contract"
							},
							{
								locale: "de",
								text: "Vertrag"
							}
						],
						documentModel: "Contract",
						ordered: true,
						navigable: true,
						linkConstraints: {
							multiplicity: {
								lowerLimit: 0,
								unbounded: true,
								upperLimit: null
							}
						},
						candidateConstraints: {
							population: null,
							populationParameters: null
						}
					},
					{
						role: "Partner",
						labels: [
							{
								locale: "en",
								text: "Co-insured Business Partners"
							},
							{
								locale: "de",
								text: "Mitversicherte Geschäftspartner"
							}
						],
						documentModel: "BusinessPartnerSuper",
						ordered: true,
						navigable: true,
						linkConstraints: {
							multiplicity: {
								lowerLimit: 1,
								unbounded: false,
								upperLimit: 5
							}
						},
						candidateConstraints: {
							population: null,
							populationParameters: null
						}
					}
				],
				storage: "EXTERNAL",
				embeddedGroupPath: null
			}
		}
	]
};
