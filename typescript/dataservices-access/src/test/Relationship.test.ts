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
import { strictEqual } from "node:assert/strict";

import { isRelationshipModel, Relationship } from "../Relationship/index.js";

import { loadResource } from "./utils/ObjectUtils.js";

suite("Relationship tests", () => {
	suite("Relationship Model", () => {
		const relationshipModel = loadResource(
			"./src/test/resources/models/ContractBusinessPartner.json"
		);

		test("Check Relationship Model", () => {
			strictEqual(isRelationshipModel(relationshipModel), true);
		});

		test("Check Relationship Model - Missing Header", () => {
			const { header, ...relationship } = relationshipModel;
			strictEqual(isRelationshipModel(relationship), false);
		});

		test("Check Relationship Model - Missing Content", () => {
			const { content, ...relationship } = relationshipModel;
			strictEqual(isRelationshipModel(relationship), false);
		});

		test("Check Relationship Model - Other type of model", () => {
			const relationship = {
				...relationshipModel,
				header: { ...relationshipModel.header, modelType: "document" }
			};
			strictEqual(isRelationshipModel(relationship), false);
		});

		test("Check Relationship Model - Non-object value", () => {
			strictEqual(isRelationshipModel(null), false);
			strictEqual(isRelationshipModel(undefined), false);
			strictEqual(isRelationshipModel(1), false);
			strictEqual(isRelationshipModel(""), false);
			strictEqual(isRelationshipModel(/\w/), false);
		});
	});

	suite("Relationship", () => {
		const linkEntitySpec = {
			role: "Contract",
			docRef: "Contract/9999"
		};

		const linkDescriptor = {
			relationshipModel: "ContractBusinessPartner",
			entities: [linkEntitySpec, linkEntitySpec],
			predecessorLinkRef: "SomeValue",
			position: "TOP"
		};

		suite("Link Reference", () => {
			const linkReference = {
				linkDescriptor: linkDescriptor,
				id: "12345"
			};

			test("Check Link Reference", () => {
				strictEqual(Relationship.LinkRef.isInstance(linkReference), true);
			});

			test("Check Link Reference - Missing linkDescriptor", () => {
				const { linkDescriptor, ...linkRef } = linkReference;
				strictEqual(Relationship.LinkRef.isInstance(linkRef), false);
			});

			test("Check Link Reference - Invalid id", () => {
				const invalidLinkRef = { ...linkReference, id: 12345 };
				strictEqual(
					Relationship.LinkRef.isInstance(invalidLinkRef),
					false,
					"Check for non-string id!"
				);

				const nullIdLinkRef = { ...linkReference, id: null };
				strictEqual(Relationship.LinkRef.isInstance(nullIdLinkRef), false, "Check for null id!");

				const undefinedIdLinkRef = { ...linkReference, id: undefined };
				strictEqual(
					Relationship.LinkRef.isInstance(undefinedIdLinkRef),
					false,
					"Check for undefined id!"
				);
			});
		});

		suite("Link Descriptor", () => {
			const linkDescriptor = {
				relationshipModel: "ContractBusinessPartner",
				entities: [
					{
						role: "Contract",
						docRef: "Contract/9999"
					},
					{
						role: "Partner",
						docRef: "BusinessPartner/9999"
					}
				],
				predecessorLinkRef: "SomeValue",
				position: "TOP"
			};

			test("Check Link Descriptor", () => {
				strictEqual(Relationship.LinkDescriptor.isInstance(linkDescriptor), true);
			});

			test("Check Link Descriptor - Missing relationshipModel", () => {
				const { relationshipModel, ...descriptor } = linkDescriptor;
				strictEqual(Relationship.LinkDescriptor.isInstance(descriptor), false);
			});

			test("Check Link Descriptor - Missing entities", () => {
				const { entities, ...descriptor } = linkDescriptor;
				strictEqual(Relationship.LinkDescriptor.isInstance(descriptor), false);
			});

			test("Check Link Descriptor - Invalid position", () => {
				const invalidDescriptor = { ...linkDescriptor, position: "INVALID" };
				strictEqual(
					Relationship.LinkDescriptor.isInstance(invalidDescriptor),
					false,
					"Check for invalid position!"
				);

				const nullPositionDescriptor = { ...linkDescriptor, position: null };
				strictEqual(
					Relationship.LinkDescriptor.isInstance(nullPositionDescriptor),
					true,
					"Check for null position!"
				);

				const undefinedPositionDescriptor = { ...linkDescriptor, position: undefined };
				strictEqual(
					Relationship.LinkDescriptor.isInstance(undefinedPositionDescriptor),
					false,
					"Check for undefined position!"
				);

				const { position, ...descriptorWithoutPosition } = linkDescriptor;
				strictEqual(
					Relationship.LinkDescriptor.isInstance(descriptorWithoutPosition),
					true,
					"Check for missing position!"
				);
			});

			test("Check Link Descriptor - Invalid predecessor link reference", () => {
				const invalidDescriptor = { ...linkDescriptor, predecessorLinkRef: 123 };
				strictEqual(
					Relationship.LinkDescriptor.isInstance(invalidDescriptor),
					false,
					"Check for non-string predecessorLinkRef!"
				);

				const nullPredecessorLinkRefDescriptor = { ...linkDescriptor, predecessorLinkRef: null };
				strictEqual(
					Relationship.LinkDescriptor.isInstance(nullPredecessorLinkRefDescriptor),
					true,
					"Check for null predecessorLinkRef!"
				);

				const undefinedPredecessorLinkRefDescriptor = {
					...linkDescriptor,
					predecessorLinkRef: undefined
				};

				strictEqual(
					Relationship.LinkDescriptor.isInstance(undefinedPredecessorLinkRefDescriptor),
					false,
					"Check for undefined predecessorLinkRef!"
				);

				const { predecessorLinkRef, ...descriptorWithoutPredecessorLinkRef } = linkDescriptor;
				strictEqual(
					Relationship.LinkDescriptor.isInstance(descriptorWithoutPredecessorLinkRef),
					true,
					"Check for missing predecessorLinkRef!"
				);
			});
		});

		suite("Link Entity Spec", () => {
			test("Check Link Entity Spec", () => {
				strictEqual(Relationship.LinkEntitySpec.isInstance(linkEntitySpec), true);
			});

			test("Check Link Entity Spec - Missing role", () => {
				const { role, ...entitySpec } = linkEntitySpec;
				strictEqual(Relationship.LinkEntitySpec.isInstance(entitySpec), false);
			});

			test("Check Link Entity Spec - Missing docRef", () => {
				const { docRef, ...entitySpec } = linkEntitySpec;
				strictEqual(Relationship.LinkEntitySpec.isInstance(entitySpec), false);
			});

			test("Check Link Entity Spec - Non-string role", () => {
				const invalidEntitySpec = { ...linkEntitySpec, role: 123 };
				strictEqual(Relationship.LinkEntitySpec.isInstance(invalidEntitySpec), false);
			});

			test("Check Link Entity Spec - Non-string docRef", () => {
				const invalidEntitySpec = { ...linkEntitySpec, docRef: 123 };
				strictEqual(Relationship.LinkEntitySpec.isInstance(invalidEntitySpec), false);
			});
		});

		suite("Link Reference Response", () => {
			const linkDescriptorResponse = {
				relationshipModel: "ContractBusinessPartner",
				entities: [
					{
						role: "Contract",
						docRef: "Contract/9999",
						modelName: "Contract"
					},
					{
						role: "Partner",
						docRef: "BusinessPartner/9999",
						modelName: "BusinessPartner"
					}
				],
				predecessorLinkRef: "SomeValue",
				position: "TOP"
			};

			const linkReference = {
				linkDescriptor: linkDescriptorResponse,
				id: "12345"
			};

			test("Check Link Reference Response", () => {
				strictEqual(Relationship.LinkRefResponse.isInstance(linkReference), true);
			});

			test("Check Link Reference Response - Missing linkDescriptor", () => {
				const { linkDescriptor, ...linkRef } = linkReference;
				strictEqual(Relationship.LinkRefResponse.isInstance(linkRef), false);
			});

			test("Check Link Reference Response - Invalid id", () => {
				const invalidLinkRef = { ...linkReference, id: 12345 };
				strictEqual(
					Relationship.LinkRefResponse.isInstance(invalidLinkRef),
					true,
					"Check for non-string id!"
				);

				const nullIdLinkRef = { ...linkReference, id: null };
				strictEqual(
					Relationship.LinkRefResponse.isInstance(nullIdLinkRef),
					true,
					"Check for null id!"
				);

				const undefinedIdLinkRef = { ...linkReference, id: undefined };
				strictEqual(
					Relationship.LinkRefResponse.isInstance(undefinedIdLinkRef),
					true,
					"Check for undefined id!"
				);
			});
		});

		suite("Link Descriptor Response", () => {
			const linkDescriptor = {
				relationshipModel: "ContractBusinessPartner",
				entities: [
					{
						role: "Contract",
						docRef: "Contract/9999",
						modelName: "Contract"
					},
					{
						role: "Partner",
						docRef: "BusinessPartner/9999",
						modelName: "BusinessPartner"
					}
				],
				predecessorLinkRef: "SomeValue",
				position: "TOP"
			};

			test("Check Link Descriptor Response", () => {
				strictEqual(Relationship.LinkDescriptorResponse.isInstance(linkDescriptor), true);
			});

			test("Check Link Descriptor Response - Missing relationshipModel", () => {
				const { relationshipModel, ...descriptor } = linkDescriptor;
				strictEqual(Relationship.LinkDescriptorResponse.isInstance(descriptor), false);
			});

			test("Check Link Descriptor Response - Missing entities", () => {
				const { entities, ...descriptor } = linkDescriptor;
				strictEqual(Relationship.LinkDescriptorResponse.isInstance(descriptor), false);
			});

			test("Check Link Descriptor Response - Invalid position", () => {
				const invalidDescriptor = { ...linkDescriptor, position: "INVALID" };
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(invalidDescriptor),
					false,
					"Check for invalid position!"
				);

				const nullPositionDescriptor = { ...linkDescriptor, position: null };
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(nullPositionDescriptor),
					true,
					"Check for null position!"
				);

				const undefinedPositionDescriptor = { ...linkDescriptor, position: undefined };
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(undefinedPositionDescriptor),
					false,
					"Check for undefined position!"
				);

				const { position, ...descriptorWithoutPosition } = linkDescriptor;
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(descriptorWithoutPosition),
					true,
					"Check for missing position!"
				);
			});

			test("Check Link Descriptor Response - Invalid predecessor link reference", () => {
				const invalidDescriptor = { ...linkDescriptor, predecessorLinkRef: 123 };
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(invalidDescriptor),
					false,
					"Check for non-string predecessorLinkRef!"
				);

				const nullPredecessorLinkRefDescriptor = { ...linkDescriptor, predecessorLinkRef: null };
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(nullPredecessorLinkRefDescriptor),
					true,
					"Check for null predecessorLinkRef!"
				);

				const undefinedPredecessorLinkRefDescriptor = {
					...linkDescriptor,
					predecessorLinkRef: undefined
				};

				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(undefinedPredecessorLinkRefDescriptor),
					false,
					"Check for undefined predecessorLinkRef!"
				);

				const { predecessorLinkRef, ...descriptorWithoutPredecessorLinkRef } = linkDescriptor;
				strictEqual(
					Relationship.LinkDescriptorResponse.isInstance(descriptorWithoutPredecessorLinkRef),
					true,
					"Check for missing predecessorLinkRef!"
				);
			});
		});

		suite("Link Entity Spec Response", () => {
			const linkEntitySpec = {
				role: "Contract",
				docRef: "Contract/9999",
				modelName: "Contract"
			};

			test("Check Link Entity Spec Response", () => {
				strictEqual(Relationship.LinkEntitySpecResponse.isInstance(linkEntitySpec), true);
			});

			test("Check Link Entity Spec Response - Missing role", () => {
				const { role, ...entitySpec } = linkEntitySpec;
				strictEqual(Relationship.LinkEntitySpecResponse.isInstance(entitySpec), false);
			});

			test("Check Link Entity Spec Response - Missing docRef", () => {
				const { docRef, ...entitySpec } = linkEntitySpec;
				strictEqual(Relationship.LinkEntitySpecResponse.isInstance(entitySpec), false);
			});

			test("Check Link Entity Spec Response - Non-string role", () => {
				const invalidEntitySpec = { ...linkEntitySpec, role: 123 };
				strictEqual(Relationship.LinkEntitySpecResponse.isInstance(invalidEntitySpec), false);
			});

			test("Check Link Entity Spec Response - Non-string docRef", () => {
				const invalidEntitySpec = { ...linkEntitySpec, docRef: 123 };
				strictEqual(Relationship.LinkEntitySpecResponse.isInstance(invalidEntitySpec), false);
			});
		});

		suite("Candidate", () => {
			const linkDescriptorResponse = {
				relationshipModel: "ContractBusinessPartner",
				entities: [
					{
						role: "Contract",
						docRef: "Contract/9999",
						modelName: "Contract"
					},
					{
						role: "Partner",
						docRef: "BusinessPartner/9999",
						modelName: "BusinessPartner"
					}
				],
				predecessorLinkRef: "SomeValue",
				position: "TOP"
			};

			const candidate = {
				linkRef: {
					linkDescriptor: linkDescriptorResponse,
					id: "12345"
				},
				document: {
					name: "Sample Document"
				}
			};

			test("Check Candidate", () => {
				strictEqual(Relationship.Candidate.isInstance(candidate), true);
			});

			test("Check Candidate - Missing linkRef", () => {
				const { linkRef, ...candidateWithoutLinkRef } = candidate;
				strictEqual(Relationship.Candidate.isInstance(candidateWithoutLinkRef), false);
			});

			test("Check Candidate - Missing document", () => {
				const { document, ...candidateWithoutDocument } = candidate;
				strictEqual(Relationship.Candidate.isInstance(candidateWithoutDocument), false);
			});

			test("Check Candidate - Invalid linkRef", () => {
				const invalidCandidate = { ...candidate, linkRef: null };
				strictEqual(Relationship.Candidate.isInstance(invalidCandidate), false);
			});

			test("Check Candidate - Invalid document", () => {
				const invalidCandidate = { ...candidate, document: null };
				strictEqual(
					Relationship.Candidate.isInstance(invalidCandidate),
					false,
					"Check for null document!"
				);

				const invalidCandidate2 = { ...candidate, document: 123 };
				strictEqual(
					Relationship.Candidate.isInstance(invalidCandidate2),
					false,
					"Check for non-object document!"
				);
			});
		});

		suite("Link With Document", () => {
			const linkDescriptorResponse = {
				relationshipModel: "ContractBusinessPartner",
				entities: [
					{
						role: "Contract",
						docRef: "Contract/9999",
						modelName: "Contract"
					},
					{
						role: "Partner",
						docRef: "BusinessPartner/9999",
						modelName: "BusinessPartner"
					}
				],
				predecessorLinkRef: "SomeValue",
				position: "TOP"
			};

			const linkWithDocument = {
				linkRef: {
					linkDescriptor: linkDescriptorResponse,
					id: "12345"
				},
				document: {
					name: "Sample Document"
				}
			};

			test("Check Link With Document", () => {
				strictEqual(Relationship.LinkWithDocument.isInstance(linkWithDocument), true);
			});

			test("Check Link With Document - Missing linkRef", () => {
				const { linkRef, ...linkWithDocumentWithoutLinkRef } = linkWithDocument;
				strictEqual(
					Relationship.LinkWithDocument.isInstance(linkWithDocumentWithoutLinkRef),
					false
				);
			});

			test("Check Link With Document - Missing document", () => {
				const { document, ...linkWithDocumentWithoutDocument } = linkWithDocument;
				strictEqual(
					Relationship.LinkWithDocument.isInstance(linkWithDocumentWithoutDocument),
					false
				);
			});

			test("Check Link With Document - Invalid linkRef", () => {
				const invalidLinkWithDocument = { ...linkWithDocument, linkRef: null };
				strictEqual(Relationship.LinkWithDocument.isInstance(invalidLinkWithDocument), false);
			});

			test("Check Link With Document - Invalid document", () => {
				const invalidLinkWithDocument = { ...linkWithDocument, document: null };
				strictEqual(
					Relationship.LinkWithDocument.isInstance(invalidLinkWithDocument),
					false,
					"Check for null document!"
				);

				const invalidLinkWithDocument2 = { ...linkWithDocument, document: 123 };
				strictEqual(
					Relationship.LinkWithDocument.isInstance(invalidLinkWithDocument2),
					false,
					"Check for non-object document!"
				);
			});
		});
	});
});
