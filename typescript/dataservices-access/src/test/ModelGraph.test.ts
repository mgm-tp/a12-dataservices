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

import { isRelationshipModel, ModelGraph } from "../Relationship/index.js";

import insurance_model_graph from "./resources/models/insurance_model_graph.js";

suite("Model Graph Test", () => {
	const insuranceModelGraph = insurance_model_graph;

	test("Model Graph Instance", () => {
		strictEqual(ModelGraph.isInstance(insuranceModelGraph), true);

		const { documentModels, ...modelGraphWithoutDocModels } = insuranceModelGraph;
		strictEqual(
			ModelGraph.isInstance(modelGraphWithoutDocModels),
			false,
			"Check ModelGraph without documentModels!"
		);

		const { composeDocumentModels, ...modelGraphWithoutComposeDocModels } = insuranceModelGraph;
		strictEqual(
			ModelGraph.isInstance(modelGraphWithoutComposeDocModels),
			false,
			"Check ModelGraph without composeDocumentModels!"
		);

		const { genericModels, ...modelGraphWithoutGenericModels } = insuranceModelGraph;
		strictEqual(
			ModelGraph.isInstance(modelGraphWithoutGenericModels),
			true,
			"Check ModelGraph without genericModels!"
		);

		const { relationshipModels, ...modelGraphWithoutRelationshipModels } = insuranceModelGraph;
		strictEqual(
			ModelGraph.isInstance(modelGraphWithoutRelationshipModels),
			false,
			"Check ModelGraph without relationshipModels!"
		);
	});

	test("Document Model Instance", () => {
		insuranceModelGraph.documentModels.forEach(item => {
			strictEqual(ModelGraph.DocumentModel.isInstance(item), true, `Check for ${item.modelId}!`);

			const { modelId, ...modelWithoutID } = item;
			strictEqual(
				ModelGraph.DocumentModel.isInstance(modelWithoutID),
				false,
				`Check for ${item.modelId} without modelId!`
			);

			const { relations, ...modelWithoutRelations } = item;
			strictEqual(
				ModelGraph.DocumentModel.isInstance(modelWithoutRelations),
				false,
				`Check for ${item.modelId} without relations!`
			);

			const { subTypes, ...modelWithoutSubTypes } = item;
			strictEqual(
				ModelGraph.DocumentModel.isInstance(modelWithoutSubTypes),
				false,
				`Check for ${item.modelId} without subTypes!`
			);
		});
	});

	test("Composed Document Models Instance", () => {
		insuranceModelGraph.composeDocumentModels.forEach(item => {
			strictEqual(
				ModelGraph.ComposeDocumentModel.isInstance(item),
				true,
				`Check for ${item.modelId}!`
			);

			const { modelId, ...modelWithoutID } = item;
			strictEqual(
				ModelGraph.ComposeDocumentModel.isInstance(modelWithoutID),
				false,
				`Check for ${item.modelId} without modelId!`
			);

			const { rootDocumentModelId, ...modelWithoutRootDocID } = item;
			strictEqual(
				ModelGraph.ComposeDocumentModel.isInstance(modelWithoutRootDocID),
				false,
				`Check for ${item.modelId} without rootDocumentModelId!`
			);
		});
	});

	test("Other Models Instance", () => {
		insuranceModelGraph.genericModels.forEach(item => {
			strictEqual(ModelGraph.OtherModel.isInstance(item), true, `Check for ${item.modelId}!`);

			const { modelId, ...modelWithoutID } = item;
			strictEqual(
				ModelGraph.OtherModel.isInstance(modelWithoutID),
				false,
				`Check for ${item.modelId} without modelId!`
			);

			const { type, ...modelWithoutType } = item;
			strictEqual(
				ModelGraph.OtherModel.isInstance(modelWithoutType),
				false,
				`Check for ${item.modelId} without type!`
			);
		});
	});

	test("Relationship Models Instance", () => {
		insuranceModelGraph.relationshipModels.forEach(item => {
			strictEqual(isRelationshipModel(item), true, `Check for ${item.header.id}!`);

			const { header, ...modelWithoutHeader } = item;
			strictEqual(
				isRelationshipModel(modelWithoutHeader),
				false,
				`Check for ${item.header.id} without header!`
			);

			const { content, ...modelWithoutContent } = item;
			strictEqual(
				isRelationshipModel(modelWithoutContent),
				false,
				`Check for ${item.header.id} without content!`
			);
		});
	});

	suite("Validate Model Graph Instance", () => {
		test("Model Graph without Relationship Models", () => {
			const modelGraph = { ...insuranceModelGraph, relationshipModels: [] };
			strictEqual(ModelGraph.isInstance(modelGraph), true);
		});

		test("Model Graph with invalid Document Models", () => {
			const modelGraph = {
				...insurance_model_graph,
				documentModels: [{ modelId: "BusinessPartner" }]
			};
			strictEqual(ModelGraph.isInstance(modelGraph), false);
		});

		test("Model Graph with invalid Relationship Models", () => {
			const modelGraph = {
				...insurance_model_graph,
				relationshipModels: [{ header: { id: "ContractBusinesspartnerPartner" } }]
			};
			strictEqual(ModelGraph.isInstance(modelGraph), false);
		});
	});
});
